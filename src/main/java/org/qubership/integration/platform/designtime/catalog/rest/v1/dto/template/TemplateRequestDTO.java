package org.qubership.integration.platform.designtime.catalog.rest.v1.dto.template;

import java.util.HashMap;
import java.util.Map;

import org.qubership.integration.platform.catalog.persistence.configs.entity.template.TemplateType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateRequestDTO {
    private String name;
    private String description;
    private TemplateType type;
    private Map<String, Object> properties = new HashMap<>();
}
