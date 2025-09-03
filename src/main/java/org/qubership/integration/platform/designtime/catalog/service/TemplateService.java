package org.qubership.integration.platform.designtime.catalog.service;

import java.util.List;

import org.qubership.integration.platform.catalog.persistence.configs.entity.template.Template;
import org.qubership.integration.platform.catalog.persistence.configs.entity.template.TemplateType;
import org.qubership.integration.platform.catalog.persistence.configs.repository.template.TemplateRepository;
import org.qubership.integration.platform.designtime.catalog.exception.exceptions.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {
    public static final String TEMPLATE_WITH_ID_NOT_FOUND_MESSAGE = "Can't find template with id: ";
    public static final String TEMPLATE_WITH_NAME_ALREADY_EXISTS = "Template with name %s already exists";

    private final TemplateRepository templateRepository;

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

    public List<Template> findAllByType(TemplateType type) {
        return type == null ? templateRepository.findAll() : templateRepository.findAllByType(type);
    }

    @Transactional
    public void delete(String templateId) {
        Template template = templateRepository.getReferenceById(templateId);
        templateRepository.delete(template);
    }

    private boolean doesTemplateExist(Template template) {
        return template.getId() != null
                ? templateRepository.existsByNameAndIdIsNot(template.getName(), template.getId())
                : templateRepository.existsByName(template.getName());
    }
}
