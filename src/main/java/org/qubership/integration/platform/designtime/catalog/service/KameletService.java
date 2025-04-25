/*
 * Copyright 2024-2025 NetCracker Technology Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qubership.integration.platform.designtime.catalog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.v1.KameletBuilder;
import org.apache.camel.v1.KameletSpec;
import org.apache.camel.v1.KameletSpecBuilder;
import org.apache.camel.v1.kameletspec.*;
import org.apache.camel.v1.kameletspec.datatypes.Types;
import org.qubership.integration.platform.catalog.persistence.configs.entity.Kamelet;
import org.qubership.integration.platform.catalog.persistence.configs.entity.actionlog.ActionLog;
import org.qubership.integration.platform.catalog.persistence.configs.entity.actionlog.EntityType;
import org.qubership.integration.platform.catalog.persistence.configs.entity.actionlog.LogOperation;
import org.qubership.integration.platform.catalog.persistence.configs.repository.QIPKameletRepository;
import org.qubership.integration.platform.catalog.service.ActionsLogService;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.kamelet.KameletProperty;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.kamelet.KameletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class KameletService {
    public static final String KAMELET_WITH_ID_NOT_FOUND = "Cannot find Kamelet with id: ";
    private final ObjectMapper objectMapper;
    private final ActionsLogService actionLogger;
    private final QIPKameletRepository qipKameletRepository;

    @Autowired
    public KameletService(
            @Qualifier("kameletYamlMapper") ObjectMapper objectMapper,
            ActionsLogService actionLogger,
            QIPKameletRepository qipKameletRepository)
    {
        this.objectMapper = objectMapper;
        this.actionLogger = actionLogger;
        this.qipKameletRepository = qipKameletRepository;
    }

    @Transactional
    public Kamelet createKamelet (KameletRequest kameletRequest) {
        //Section metadata:
        ObjectMeta objectMeta = new ObjectMetaBuilder()
                .withName(getNameFromTitle(kameletRequest.getTitle()))
                .withLabels(kameletRequest.getLabels())
                .build();

        //Section spec:definition
        Definition definition = new DefinitionBuilder()
                .withTitle(kameletRequest.getTitle())
                .withDescription(kameletRequest.getDescription())
                .build();

        //Section spec:dataType
        //TODO recive types from request
        Map<String, Types> types = new HashMap<>();
        Types type = new Types();
        types.put("out", type);
        //TODO end
        DataTypes dataTypes = new DataTypesBuilder()
                .withTypes(types)
                .build();
        Map<String,DataTypes> dataTypesMap = new HashMap<>();
        dataTypesMap.put("out", dataTypes);


        //Section spec:template
        Template template = getTemplate(kameletRequest.getTemplate());

        //Section spec:
        KameletSpec spec = new KameletSpecBuilder()
                .withDefinition(definition)
                .withDataTypes(dataTypesMap)
                .withTemplate(template)
                .build();

        //Apache Kamelet
        org.apache.camel.v1.Kamelet apacheKamelet = new KameletBuilder()
                .withMetadata(objectMeta)
                .withSpec(spec)
                .build();

        //QIP Kamelet
        Kamelet qipKamelet = new Kamelet();
        qipKamelet.setName(kameletRequest.getTitle());
        qipKamelet.setSpecification(getKameletSpecification(apacheKamelet));

        qipKamelet = qipKameletRepository.save(qipKamelet);

        logKameletAction(qipKamelet, LogOperation.CREATE);
        return qipKamelet;
    }

    public Kamelet findById(String kameletId) {
        return qipKameletRepository.findById(kameletId)
                .orElseThrow(() -> new EntityNotFoundException(KAMELET_WITH_ID_NOT_FOUND + kameletId));
    }

    @Transactional
    public Kamelet updateKamelet(Kamelet kamelet, KameletRequest kameletRequest) {
        org.apache.camel.v1.Kamelet apacheKamelet = getKamelet(kamelet);
        //Section spec:definition
        Definition kameletDefinition = apacheKamelet.getSpec().getDefinition();

        //Section spec:definition:required
        kameletDefinition.setRequired(kameletRequest.getRequired());

        //Section spec:definition:properties
        kameletDefinition.setProperties(getProperties(kameletRequest.getProperties()));

        //Section spec:template
        Template template = getTemplate(kameletRequest.getTemplate());
        apacheKamelet.getSpec().setTemplate(template);


        kamelet.setSpecification(getKameletSpecification(apacheKamelet));
        kamelet =  qipKameletRepository.save(kamelet);
        logKameletAction(kamelet,LogOperation.UPDATE);
        return kamelet;
    }

    public String getKameletSpecification(String kameletId) {
        Kamelet kamelet = findById(kameletId);
        return kamelet.getSpecification();
    }

    public String getKameletDefinition(String kameletId) {
        Kamelet kamelet = findById(kameletId);
        org.apache.camel.v1.Kamelet apacheKamelet = getKamelet(kamelet);
        try {
           return objectMapper.writeValueAsString(apacheKamelet.getSpec().getDefinition());
        } catch (JsonProcessingException e) {
            //TODO Correct exception
            throw new RuntimeException(e);
        }
    }

    public String getKameletTemplate(String kameletId) {
        Kamelet kamelet = findById(kameletId);
        org.apache.camel.v1.Kamelet apacheKamelet = getKamelet(kamelet);
        try {
           return objectMapper.writeValueAsString(apacheKamelet.getSpec().getTemplate());
        } catch (JsonProcessingException e) {
            //TODO Correct exception
            throw new RuntimeException(e);
        }

    }

    public void deleteKamelet(String kameletId) {
        Kamelet kamelet = findById(kameletId);
        qipKameletRepository.deleteById(kameletId);
        logKameletAction(kamelet, LogOperation.DELETE);
    }

    private String getNameFromTitle(String kameletTitle) {
        return kameletTitle.trim().toLowerCase().replaceAll("\\s+","-");
    }

    private Template getTemplate(String templateString) {
        Template template = null;
        try {
            template = templateString != null ? objectMapper.readValue(templateString, Template.class) : null;
        } catch (JsonProcessingException e) {
            //TODO Correct exception
            throw new RuntimeException(e);
        }
        return template;
    }

    private String getKameletSpecification(org.apache.camel.v1.Kamelet kamelet) {
        String kameletSpec = null;
        try {
            kameletSpec = objectMapper.writeValueAsString(kamelet);
        } catch (JsonProcessingException e) {
            //TODO Correct exception
            throw new RuntimeException(e);
        }
        return kameletSpec;
    }

    private  org.apache.camel.v1.Kamelet getKamelet(Kamelet kamelet) {
        try {
            return objectMapper.readValue(kamelet.getSpecification(),  org.apache.camel.v1.Kamelet.class);
        } catch (JsonProcessingException e) {
            //TODO Correct exception
            throw new RuntimeException(e);
        }
    }

    private Map<String, org.apache.camel.v1.kameletspec.definition.Properties> getProperties(Map<String, KameletProperty> properties) {
        Map<String,org.apache.camel.v1.kameletspec.definition.Properties> result = new HashMap<>();
        properties.forEach((key, value) -> {
            org.apache.camel.v1.kameletspec.definition.Properties prop = new org.apache.camel.v1.kameletspec.definition.Properties();
            prop.setType(value.getType());
            prop.setDescription(value.getDescription());
            prop.setTitle(value.getTitle());
            result.put(key, prop);
        });
        return result;
    }

    private void logKameletAction(Kamelet kamelet, LogOperation operation) {
        actionLogger.logAction(ActionLog.builder()
                .entityType(EntityType.KAMELET)
                .entityId(kamelet.getId())
                .entityName(kamelet.getName())
                .operation(operation)
                .build()
        );
    }
}
