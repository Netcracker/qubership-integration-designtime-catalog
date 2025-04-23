package org.qubership.integration.platform.designtime.catalog.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.qubership.integration.platform.catalog.model.dto.template.TemplateUsage;
import org.qubership.integration.platform.catalog.persistence.configs.entity.template.Template;
import org.qubership.integration.platform.catalog.persistence.configs.entity.template.TemplateType;
import org.qubership.integration.platform.catalog.persistence.configs.repository.template.TemplateRepository;
import org.qubership.integration.platform.designtime.catalog.exception.exceptions.BadRequestException;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.template.TemplateResponseDTO;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.template.TemplateUsageDTO;
import org.qubership.integration.platform.designtime.catalog.rest.v1.mapping.TemplateMapper;
import org.springframework.stereotype.Service;
import com.google.common.collect.ImmutableMap;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TemplateService {
    public static final String TEMPLATE_WITH_ID_NOT_FOUND_MESSAGE = "Can't find template with id: ";
    public static final String TEMPLATE_WITH_NAME_ALREADY_EXISTS = "Template with name %s already exists";
    public static final String TEMPLATE_TYPE_SHOULD_BE_SET_MESSAGE = "Template type should be set";
    public static final String TEMPLATE_CAN_NOT_BE_DELETED_MESSAGE = "Template (id=%s) can't be deleted since it's in use";

    private final Map<TemplateType, Function<List<String>, List<TemplateUsage>>> getUsagesMethods;

    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;

    public TemplateService(TemplateRepository templateRepository, TemplateMapper templateMapper) {
        this.templateRepository = templateRepository;
        this.templateMapper = templateMapper;
        this.getUsagesMethods = ImmutableMap
                .<TemplateType, Function<List<String>, List<TemplateUsage>>>builder()
                .put(TemplateType.MAPPING, templateIds -> templateRepository.getMappingTemplateUsages(templateIds))
                .build();
    }

    public Template save(Template template) {
        if (!doesTemplateExist(template)) {
            return templateRepository.save(template);
        } else {
            throw new BadRequestException(String.format(TEMPLATE_WITH_NAME_ALREADY_EXISTS, template.getName()));
        }
    }

    public Template findById(String templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException(TEMPLATE_WITH_ID_NOT_FOUND_MESSAGE + templateId));
    }

    public List<TemplateResponseDTO> findByType(TemplateType type) {   
        return type == null ? findAllTemplatesWithUsages() : findTemplatesWithUsages(type);
    }

    private List<TemplateResponseDTO> findAllTemplatesWithUsages() {
        List<TemplateResponseDTO> result = new ArrayList<>();
        for (TemplateType templateType : getUsagesMethods.keySet()) {
            result.addAll(findTemplatesWithUsages(templateType));
        }

        return result;
    }

    public void delete(String templateId) {
        Template template = templateRepository.getReferenceById(templateId);
        Function<List<String>, List<TemplateUsage>> getUsagesFunction = getUsagesMethods.get(template.getType());
        if (getUsagesFunction != null) {
            List<TemplateUsage> usages = getUsagesFunction.apply(Collections.singletonList(templateId));
            if (!usages.isEmpty()) {
                throw new BadRequestException(String.format(TEMPLATE_CAN_NOT_BE_DELETED_MESSAGE, templateId));
            }
        }

        templateRepository.delete(template);
    }

    private List<TemplateResponseDTO> findTemplatesWithUsages(TemplateType type) {
        List<Template> templates = templateRepository.findAllByType(type);
        List<String> templateIds = templates.stream().map(Template::getId).toList();

        List<TemplateUsage> usages = getUsagesMethods.get(type).apply(templateIds);
        return enrichTemplatesWithUsages(templates, usages);
    }

    private List<TemplateResponseDTO> enrichTemplatesWithUsages(List<Template> templates,
            List<TemplateUsage> templateUsages) {
        List<TemplateUsageDTO> templateUsagesDtoList = templateMapper.convertUsages(templateUsages);
        Map<String, List<TemplateUsageDTO>> usagesByTemplateId = CollectionUtils.emptyIfNull(templateUsagesDtoList).stream()
                .collect(Collectors.groupingBy(TemplateUsageDTO::getTemplateId));

        List<TemplateResponseDTO> templatesDTO = templateMapper.asResponse(templates);

        for (TemplateResponseDTO templateDTO : templatesDTO) {
            templateDTO.setUsages(usagesByTemplateId.get(templateDTO.getId()));
        }

        return templatesDTO;
    }

    private boolean doesTemplateExist(Template template) {
        return template.getId() != null
                ? templateRepository.existsByNameAndIdIsNot(template.getName(), template.getId())
                : templateRepository.existsByName(template.getName());
    }
}
