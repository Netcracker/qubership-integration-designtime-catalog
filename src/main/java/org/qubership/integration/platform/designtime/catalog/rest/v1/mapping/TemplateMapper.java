package org.qubership.integration.platform.designtime.catalog.rest.v1.mapping;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.qubership.integration.platform.catalog.model.dto.template.TemplateUsage;
import org.qubership.integration.platform.catalog.persistence.configs.entity.template.Template;
import org.qubership.integration.platform.catalog.util.MapperUtils;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.template.TemplateRequestDTO;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.template.TemplateResponseDTO;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.template.TemplateUsageDTO;

@Mapper(componentModel = "spring", 
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {
        MapperUtils.class
    }
)
public interface TemplateMapper {

    Template mapRequest(TemplateRequestDTO request);

    @Mapping(target="id", source="id")
    @Mapping(target="name", source="name")
    @Mapping(target="description", source="description")
    @Mapping(target="properties", source="properties")
    TemplateResponseDTO asResponse(Template template);

    List<TemplateResponseDTO> asResponse(List<Template> templates);

    void merge(TemplateRequestDTO templateDto, @MappingTarget Template template);

    List<TemplateUsageDTO> convertUsages(List<TemplateUsage> source);
}
