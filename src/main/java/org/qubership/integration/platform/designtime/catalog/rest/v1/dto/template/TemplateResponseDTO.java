package org.qubership.integration.platform.designtime.catalog.rest.v1.dto.template;

import java.util.HashMap;
import java.util.Map;

import org.qubership.integration.platform.catalog.model.dto.BaseResponse;
import org.qubership.integration.platform.catalog.persistence.configs.entity.template.TemplateType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class TemplateResponseDTO extends BaseResponse {
    private TemplateType type;
    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();
}
