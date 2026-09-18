package com.xzkj.health.model.dto;

import java.util.List;

public class BloodPressurePage {
    private List<BloodPressureRecord> list;
    private long total;
    private int page;
    private int size;

    public BloodPressurePage(List<BloodPressureRecord> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<BloodPressureRecord> getList() {
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
