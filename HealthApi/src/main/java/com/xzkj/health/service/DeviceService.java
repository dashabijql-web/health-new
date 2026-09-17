package com.xzkj.health.service;

import com.xzkj.health.mapper.DeviceMapper;
import com.xzkj.health.model.dto.DevicePage;
import com.xzkj.health.model.entity.Device;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public Device create(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("设备信息不能为空");
        }
        applyImeiAndType(device, device.getImei(), device.getDeviceType(), null);
        device.setId(null);
        if (device.getStatus() == null) {
            device.setStatus(0);
        }
        if (device.getOnlineStatus() == null) {
            device.setOnlineStatus(0);
        }
        device.setCreateTime(LocalDateTime.now());
        deviceMapper.insert(device);
        return device;
    }

    public Device update(Device device) {
        if (device == null || device.getId() == null) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
        Device existing = deviceMapper.selectById(device.getId());
        if (existing == null) {
            throw new IllegalArgumentException("设备不存在");
        }
        applyImeiAndType(existing, device.getImei(), device.getDeviceType(), existing.getId());
        deviceMapper.updateById(existing);
        return existing;
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
        Device existing = deviceMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("设备不存在");
        }
        if (deviceMapper.countCurrentBindings(id) > 0) {
            throw new IllegalArgumentException("设备已绑定职工，请先解绑");
        }
        deviceMapper.deleteById(id);
    }

    private void applyImeiAndType(Device target, String imei, String deviceType, Long excludeId) {
        String normalizedImei = imei == null ? "" : imei.trim();
        if (normalizedImei.isEmpty()) {
            throw new IllegalArgumentException("IMEI不能为空");
        }
        if (!normalizedImei.matches("\\d{15}")) {
            throw new IllegalArgumentException("IMEI必须是15位数字");
        }
        if (deviceMapper.countByImei(normalizedImei, excludeId) > 0) {
            throw new IllegalArgumentException("IMEI已存在");
        }
        String normalizedType = deviceType == null || deviceType.isBlank() ? "watch" : deviceType.trim();
        target.setImei(normalizedImei);
        target.setDeviceType(normalizedType);
    }
}
