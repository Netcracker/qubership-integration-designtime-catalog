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

package org.qubership.integration.platform.designtime.catalog.rest.v1.controller;

import org.qubership.integration.platform.catalog.persistence.configs.entity.chain.MaskedField;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.masking.MaskedFieldDTO;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.masking.MaskedFieldsResponse;
import org.qubership.integration.platform.designtime.catalog.rest.v1.mapping.MaskedFieldsMapper;
import org.qubership.integration.platform.designtime.catalog.service.ChainService;
import org.qubership.integration.platform.designtime.catalog.service.MaskedFieldsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/v1/chains/{chainId}/masking")
@CrossOrigin(origins = "*")
@Tag(name = "masked-fields-controller", description = "Masked Fields Controller")
public class MaskedFieldsController {
    private final ChainService chainService;
    private final MaskedFieldsService maskedService;
    private final MaskedFieldsMapper mapper;

    @Autowired
    public MaskedFieldsController(MaskedFieldsService maskedService, ChainService chainService, MaskedFieldsMapper mapper) {
        this.maskedService = maskedService;
        this.chainService = chainService;
        this.mapper = mapper;
    }

    @GetMapping()
    @Operation(description = "Get masked fields for specified chain")
    public ResponseEntity<MaskedFieldsResponse> findByChainId(@PathVariable @Parameter(description = "Chain id") String chainId) {
        if (log.isDebugEnabled()) {
            log.debug("Request to find masked fields for chain: {}", chainId);
        }
        return ResponseEntity.ok(mapper.asResponse(chainService.findById(chainId)));
    }

    @PostMapping()
    @Operation(description = "Create new masked field for specified chain")
    public ResponseEntity<MaskedFieldDTO> createField(@PathVariable @Parameter(description = "Chain id") String chainId,
                                                      @RequestBody @Parameter(description = "Masked field data") MaskedFieldDTO maskedField) {
        log.info("Request to create masked fields for chain: {}", chainId);
        return ResponseEntity.ok(mapper.asDto(maskedService.create(mapper.asEntity(chainService.findById(chainId), maskedField))));
    }

    @DeleteMapping("/field/{fieldId}")
    @Operation(description = "Delete specified masked field from a chain")
    public ResponseEntity<Void> deleteField(@PathVariable @Parameter(description = "Chain id") String chainId,
                                            @PathVariable @Parameter(description = "Masked field id") String fieldId) {
        log.info("Request to delete masked field {} for chain: {}", fieldId, chainId);
        maskedService.delete(fieldId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/field/{fieldId}")
    @Operation(description = "Update specified masked field in a chain")
    public ResponseEntity<MaskedFieldDTO> updateField(
            @PathVariable @Parameter(description = "Chain id") String chainId,
            @PathVariable @Parameter(description = "Masked field id") String fieldId,
            @RequestBody @Parameter(description = "Masked field data") MaskedFieldDTO maskedField) {
        log.info("Request to update masked field {} for chain: {}", fieldId, chainId);
        MaskedField entity = maskedService.findById(fieldId);
        entity.merge(mapper.asEntity(maskedField));
        return ResponseEntity.ok(mapper.asDto(maskedService.update(entity)));
    }
}