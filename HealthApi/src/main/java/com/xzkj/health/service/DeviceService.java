package com.xzkj.health.service;

import com.xzkj.health.mapper.DeviceMapper;
import com.xzkj.health.model.dto.DevicePage;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 50;

    private final DeviceMapper deviceMapper;

    public DeviceService(DeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

    public DevicePage list(String keyword, Integer page, Integer size) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        int safePage = page == null || page < 1 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = (safePage - 1) * safeSize;
        long total = deviceMapper.countList(normalized);
        return new DevicePage(deviceMapper.findPage(normalized, offset, safeSize), total, safePage, safeSize);
    }
}
