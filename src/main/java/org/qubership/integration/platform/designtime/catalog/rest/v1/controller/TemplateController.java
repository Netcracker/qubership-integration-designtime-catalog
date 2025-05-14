package org.qubership.integration.platform.designtime.catalog.rest.v1.controller;

import java.util.List;

import org.qubership.integration.platform.catalog.persistence.configs.entity.template.Template;
import org.qubership.integration.platform.catalog.persistence.configs.entity.template.TemplateType;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.template.TemplateRequestDTO;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.template.TemplateResponseDTO;
import org.qubership.integration.platform.designtime.catalog.rest.v1.mapping.TemplateMapper;
import org.qubership.integration.platform.designtime.catalog.service.TemplateService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/templates", produces = MediaType.APPLICATION_JSON_VALUE)
public class TemplateController {
    private final TemplateService templateService;
    private final TemplateMapper templateMapper;

    @PostMapping
    @Operation(description = "Create a new template")
    public ResponseEntity<TemplateResponseDTO> createTemplate(@RequestBody TemplateRequestDTO request) {
        Template template = templateMapper.mapRequest(request);
        template = templateService.createIfDoesNotExist(template);

        return ResponseEntity.ok(templateMapper.asResponse(template));
    }

    @GetMapping(value = "/{templateId}", produces = "application/json")
    @Operation(description = "Get specific template")
    public TemplateResponseDTO getTemplate(@PathVariable @Parameter(description = "Template id") String templateId) {
        return templateMapper.asResponse(templateService.findById(templateId));
    }

    @GetMapping(produces = "application/json")
    @Operation(description = "Get all templates")
    public List<TemplateResponseDTO> getTemplates(@RequestParam(required = false) TemplateType type) {
        return templateService.findByType(type);
    }

    @PutMapping(value = "/{templateId}", produces = "application/json")
    @Operation(description = "Modify specified template")
    public ResponseEntity<TemplateResponseDTO> updateTemplate(
            @PathVariable @Parameter(description = "Template id") String templateId,
            @RequestBody @Parameter(description = "Template modifying request object") TemplateRequestDTO templateDto) {
        Template template = templateService.findById(templateId);
        templateMapper.merge(templateDto, template);
        templateService.update(template);

        return ResponseEntity.ok(templateMapper.asResponse(template));
    }

    @DeleteMapping(value = "/{templateId}")
    @Operation(description = "Delete specified template")
    public void deleteTemplate(@PathVariable @Parameter(description = "Template id") String templateId) {
        log.info("Request to delete template {}", templateId);
        templateService.deleteIfNotUsed(templateId);
    }
}
