package org.qubership.integration.platform.designtime.catalog.rest.v2.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.qubership.integration.platform.catalog.mapping.UserMapper;
import org.qubership.integration.platform.catalog.persistence.configs.entity.chain.Folder;
import org.qubership.integration.platform.catalog.util.MapperUtils;
import org.qubership.integration.platform.designtime.catalog.rest.v2.dto.CreateFolderRequest;
import org.qubership.integration.platform.designtime.catalog.rest.v2.dto.FolderItem;
import org.qubership.integration.platform.designtime.catalog.rest.v2.dto.UpdateFolderRequest;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {
                MapperUtils.class,
                UserMapper.class
        }
)
public interface FolderItemMapper {
    Folder asFolder(CreateFolderRequest request);

    Folder asFolder(UpdateFolderRequest request);

    @Mapping(source = "parentFolder.id", target = "parentId")
    @Mapping(target = "itemType", constant = "FOLDER")
    FolderItem asFolderItem(Folder folder);
}
