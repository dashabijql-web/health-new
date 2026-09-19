package com.xzkj.health.model.dto;

public class WarningCodeCatalogItem {
    private final String code;
    private final String label;

    public WarningCodeCatalogItem(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
}
