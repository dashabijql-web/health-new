package com.xzkj.health.model.dto;

import java.util.List;

public class TemperaturePage {
    private final List<TemperatureRecord> list;
    private final long total;
    private final int page;
    private final int size;

    public TemperaturePage(List<TemperatureRecord> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<TemperatureRecord> getList() {
        return list;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
