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


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.qubership.integration.platform.catalog.model.library.KameletDescriptionDTO;
import org.qubership.integration.platform.catalog.persistence.configs.entity.Kamelet;
import org.qubership.integration.platform.catalog.service.KameletStorageService;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.kamelet.KameletRequest;
import org.qubership.integration.platform.designtime.catalog.rest.v1.dto.kamelet.KameletResponse;
import org.qubership.integration.platform.designtime.catalog.rest.v1.mapping.KameletMapper;
import org.qubership.integration.platform.designtime.catalog.service.KameletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/v1/kamelet", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
@Tag(name = "kamelet-controller", description = "Kamelet Controller")
public class KameletController {

    private final KameletService kameletService;
    private final KameletMapper kameletMapper;
    private final KameletStorageService kameletStorageService;

    @Autowired
    public KameletController(KameletService kameletService, KameletMapper kameletMapper, KameletStorageService kameletStorageService) {
        this.kameletService = kameletService;
        this.kameletMapper = kameletMapper;
        this.kameletStorageService = kameletStorageService;
    }

    @PostMapping("/custom")
    @Operation(description = "Create blank custom kamelet")
    public ResponseEntity<KameletResponse> createBlankKamelet(@RequestBody @Parameter(description = "Custom kamelet create request object")  KameletRequest kameletRequest) {
        log.info("Request to create new kamelet");
        Kamelet result = kameletService.createKamelet(kameletRequest);

        return ResponseEntity.ok(kameletMapper.toKameletResponseLight(result));
    }

    @GetMapping("/custom/{kameletId}/spec")
    @Operation(description = "Get Kamelet Specification in YML")
    public ResponseEntity<String> getKameletSpecification(@PathVariable @Parameter(description = "Custom kamelet id") String kameletId) {
        if (log.isDebugEnabled()) {
            log.debug("Request to receive yml specification of kamelet with id: {}", kameletId);
        }
        String specification = kameletService.getKameletSpecification(kameletId);
        return ResponseEntity.ok(specification);
    }

    @PutMapping("/custom/{kameletId}")
    @Operation(description = "Update exiting kamelet")
    public ResponseEntity<KameletResponse> updateKamelet(@PathVariable @Parameter(description = "Custom kamelet id") String kameletId,
                                                         @RequestBody @Parameter(description = "Custom kamelet update request object") KameletRequest kameletRequest) {
       Kamelet kamelet = kameletService.findById(kameletId);
       kamelet = kameletService.updateKamelet(kamelet, kameletRequest);

       return ResponseEntity.ok(kameletMapper.toKameletResponseLight(kamelet));
    }


    @DeleteMapping("/custom/{kameletId}")
    @Operation(description = "Delete kamelet")
    public ResponseEntity<Void> deleteKamelet(@PathVariable @Parameter(description = "Custom kamelet id") String kameletId) {
        log.info("Request to remove kamelet with id: {}", kameletId);
        kameletService.deleteKamelet(kameletId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/library")
    public ResponseEntity<List<KameletDescriptionDTO>> getKameletLibrary() {
        List<KameletDescriptionDTO> kameletDescriptionList = kameletStorageService.getKameletsDefinitions();
        return ResponseEntity.ok(kameletDescriptionList);
    }

    @GetMapping("/library/{name}")
    public ResponseEntity<String> getKamelet(@PathVariable String name) {
        String kameletJson = kameletStorageService.getKameletByName(name);
        log.info("Request to retrieve kamelet with name: {}", kameletJson);
        return (kameletJson != null) ? ResponseEntity.ok(kameletJson) : ResponseEntity.notFound().build();
    }

    @GetMapping("/library/{name}/definition")
    public ResponseEntity<String> getKameletSpec(@PathVariable String name) {
        String specJson = kameletStorageService.getKameletSpec(name);
        log.info("Request to retrieve specification of a kamelet with name: {}", specJson);
        return (specJson != null) ? ResponseEntity.ok(specJson) : ResponseEntity.notFound().build();
    }

    @GetMapping("/library/{name}/template")
    public ResponseEntity<String> getKameletTemplate(@PathVariable String name) {
        String templateJson = kameletStorageService.getKameletTemplate(name);
        log.info("Request to retrieve template of a kamelet with name: {}", templateJson);
        return (templateJson != null) ? ResponseEntity.ok(templateJson) : ResponseEntity.notFound().build();
    }

}
