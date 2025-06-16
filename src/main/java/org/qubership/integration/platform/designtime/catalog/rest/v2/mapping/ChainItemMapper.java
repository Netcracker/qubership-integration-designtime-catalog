package org.qubership.integration.platform.designtime.catalog.rest.v2.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.qubership.integration.platform.catalog.mapping.UserMapper;
import org.qubership.integration.platform.catalog.persistence.configs.entity.chain.Chain;
import org.qubership.integration.platform.catalog.util.MapperUtils;
import org.qubership.integration.platform.catalog.util.StringTrimmer;
import org.qubership.integration.platform.designtime.catalog.rest.v1.mapping.ChainLabelsMapper;
import org.qubership.integration.platform.designtime.catalog.rest.v2.dto.ChainItem;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {
                MapperUtils.class,
                UserMapper.class,
                StringTrimmer.class,
                ChainLabelsMapper.class
        })
public interface ChainItemMapper {
    @Mapping(source = "parentFolder.id", target = "parentId")
    @Mapping(target = "itemType", constant = "CHAIN")
    ChainItem asChainItem(Chain chain);
}
