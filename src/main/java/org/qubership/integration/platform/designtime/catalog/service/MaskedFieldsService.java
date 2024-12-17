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

import org.qubership.integration.platform.catalog.persistence.configs.entity.actionlog.ActionLog;
import org.qubership.integration.platform.catalog.persistence.configs.entity.actionlog.EntityType;
import org.qubership.integration.platform.catalog.persistence.configs.entity.actionlog.LogOperation;
import org.qubership.integration.platform.catalog.persistence.configs.entity.chain.MaskedField;
import org.qubership.integration.platform.catalog.persistence.configs.repository.chain.MaskedFieldRepository;
import org.qubership.integration.platform.catalog.service.ActionsLogService;
import org.qubership.integration.platform.designtime.catalog.configuration.aspect.ChainModification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class MaskedFieldsService {
    private static final String MASKED_FIELD_WITH_ID_NOT_FOUND_MESSAGE = "Can't find masked field with id: ";

    private final MaskedFieldRepository maskedRepository;
    private final ActionsLogService actionLogger;

    @Autowired
    public MaskedFieldsService(MaskedFieldRepository maskedRepository,
                               ActionsLogService actionLogger) {
        this.maskedRepository = maskedRepository;
        this.actionLogger = actionLogger;
    }

    public MaskedField findById(String fieldId) {
        return maskedRepository.findById(fieldId).orElseThrow(() -> new EntityNotFoundException(MASKED_FIELD_WITH_ID_NOT_FOUND_MESSAGE + fieldId));
    }

    @ChainModification
    public MaskedField delete(String fieldId) {
        MaskedField field = findById(fieldId);
        maskedRepository.deleteById(fieldId);
        logMaskedFieldsAction(field, LogOperation.DELETE);
        return field;
    }

    @ChainModification
    public MaskedField create(MaskedField maskedField) {
        maskedField = createOrUpdate(maskedField);
        logMaskedFieldsAction(maskedField, LogOperation.CREATE);
        return maskedField;
    }

    @ChainModification
    public MaskedField update(MaskedField maskedField) {
        maskedField = createOrUpdate(maskedField);
        logMaskedFieldsAction(maskedField, LogOperation.UPDATE);
        return maskedField;
    }

    public MaskedField save(MaskedField maskedField) {
        return maskedRepository.save(maskedField);
    }

    private MaskedField createOrUpdate(MaskedField maskedField) {
        Optional<MaskedField> existingEntity =
                maskedRepository.findFirstByChainIdAndName(maskedField.getChain().getId(), maskedField.getName());
        if (existingEntity.isPresent() && !existingEntity.get().getId().equals(maskedField.getId()))
            throw new EntityExistsException("Field with name " + maskedField.getName() + " already exist");
        return maskedRepository.save(maskedField);
    }

    private void logMaskedFieldsAction(MaskedField field, LogOperation operation) {
        actionLogger.logAction(ActionLog.builder()
                .entityType(EntityType.MASKED_FIELD)
                .entityId(field.getId())
                .entityName(field.getName())
                .parentType(field.getChain() == null ? null : EntityType.CHAIN)
                .parentId(field.getChain() == null ? null : field.getChain().getId())
                .parentName(field.getChain() == null ? null : field.getChain().getName())
                .operation(operation)
                .build());
    }

}