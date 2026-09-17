package com.xzkj.health.service;

import com.xzkj.health.mapper.DeviceMapper;
import com.xzkj.health.mapper.DeviceUserMapper;
import com.xzkj.health.mapper.EmployeeMapper;
import com.xzkj.health.model.dto.DevicePage;
import com.xzkj.health.model.entity.Device;
import com.xzkj.health.model.entity.DeviceUser;
import com.xzkj.health.model.entity.Employee;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DeviceService {
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_SIZE = 20;
    static final int MAX_SIZE = 50;

    private final DeviceMapper deviceMapper;
    private final DeviceUserMapper deviceUserMapper;
    private final EmployeeMapper employeeMapper;

    public DeviceService(DeviceMapper deviceMapper,
                         DeviceUserMapper deviceUserMapper,
                         EmployeeMapper employeeMapper) {
        this.deviceMapper = deviceMapper;
        this.deviceUserMapper = deviceUserMapper;
        this.employeeMapper = employeeMapper;
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

    @Transactional
    public void bind(Long deviceId, String empCode) {
        if (deviceId == null) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("设备不存在");
        }
        if (deviceUserMapper.selectCurrentByDevice(deviceId) != null) {
            throw new IllegalArgumentException("设备已绑定职工，请先解绑");
        }
        String normalizedCode = empCode == null ? "" : empCode.trim();
        if (normalizedCode.isEmpty()) {
            throw new IllegalArgumentException("工号不能为空");
        }
        Employee employee = employeeMapper.findByEmpCode(normalizedCode);
        if (employee == null) {
            throw new IllegalArgumentException("职工不存在");
        }
        if (deviceUserMapper.selectCurrentByEmployee(employee.getId()) != null) {
            throw new IllegalArgumentException("该职工已绑定其他设备");
        }

        DeviceUser binding = new DeviceUser();
        binding.setDeviceId(deviceId);
        binding.setEmpId(employee.getId());
        binding.setRealName(employee.getEmpName());
        binding.setBindTime(LocalDateTime.now());
        binding.setCurrent(true);
        binding.setBindType(1);
        deviceUserMapper.insert(binding);
    }

    @Transactional
    public void unbind(Long deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
        DeviceUser current = deviceUserMapper.selectCurrentByDevice(deviceId);
        if (current == null) {
            throw new IllegalArgumentException("设备未绑定职工");
        }
        current.setUnbindTime(LocalDateTime.now());
        current.setCurrent(false);
        deviceUserMapper.updateById(current);
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
