package com.xzkj.health.model.dto;

import java.util.List;

public class HeartRatePage {
    private List<HeartRateRecord> list;
    private long total;
    private int page;
    private int size;

    public HeartRatePage(List<HeartRateRecord> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<HeartRateRecord> getList() {
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
