package com.xzkj.health.model.dto;

import java.util.List;

public class PressurePage {
    private final List<PressureRecord> list;
    private final long total;
    private final int page;
    private final int size;

    public PressurePage(List<PressureRecord> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<PressureRecord> getList() {
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
