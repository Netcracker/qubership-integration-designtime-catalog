package org.qubership.integration.platform.designtime.catalog.rest.v1.dto.template;

import lombok.Data;

@Data
public class TemplateUsageDTO {
    private String templateId;
    private String chainId;
    private String chainName;
    private String elementId;
    private String elementName;
    private String tabId;
    private String tabName;
}
