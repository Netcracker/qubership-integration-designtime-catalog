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

package org.qubership.integration.platform.designtime.catalog.rest.v1.dto.kamelet;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Schema(description = "Kamelet creation request object")
public class KameletRequest {
    @Schema(description = "Title for kamelet specification")
    private String title;
    @Schema(description = "Description for kamelet specification")
    private String description;
    @Schema(description = "Labels for kamelet metadata")
    private Map<String, String> labels;
    @Schema(description = "List of required properties")
    private List<String> required;
    @Schema(description = "Input kamelet properties")
    private Map<String, KameletProperty> properties;
    @Schema(description = "Input kamelet specification")
    private String template;
}
