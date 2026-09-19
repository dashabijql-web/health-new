package com.xzkj.health.model.dto;

import java.util.List;

public class WarningSourceCatalog {
    private final String source;
    private final String label;
    private final List<WarningCodeCatalogItem> codes;

    public WarningSourceCatalog(String source, String label, List<WarningCodeCatalogItem> codes) {
        this.source = source;
        this.label = label;
        this.codes = codes;
    }

    public String getSource() { return source; }
    public String getLabel() { return label; }
    public List<WarningCodeCatalogItem> getCodes() { return codes; }
}
