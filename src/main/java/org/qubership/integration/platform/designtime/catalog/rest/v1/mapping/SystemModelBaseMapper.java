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

package org.qubership.integration.platform.designtime.catalog.rest.v1.mapping;

import org.qubership.integration.platform.catalog.persistence.configs.entity.system.SystemModel;
import org.qubership.integration.platform.catalog.persistence.configs.entity.system.SystemModelLabel;
import org.qubership.integration.platform.catalog.util.MapperUtils;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.SystemModelBaseDTO;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.SystemModelLabelDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        MapperUtils.class,
        ChainBaseMapper.class
})
public interface SystemModelBaseMapper {

    @Mapping(target = "specificationGroupId", source = "systemModel.specificationGroup.id")
    @Mapping(target = "systemId", source = "systemModel.specificationGroup.system.id")
    SystemModelBaseDTO toDTO(SystemModel systemModel);

    List<SystemModelBaseDTO> toDTOs(List<SystemModel> systemModels);

    SystemModelLabel asLabelRequest(SystemModelLabelDTO snapshotLabel);
    List<SystemModelLabel> asLabelRequests(List<SystemModelLabelDTO> snapshotLabel);
    SystemModelLabelDTO asLabelResponse(SystemModelLabel snapshotLabel);
    List<SystemModelLabelDTO> asLabelResponse(List<SystemModelLabel> snapshotLabel);
}