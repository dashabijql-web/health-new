package com.xzkj.health.controller;

import com.xzkj.health.model.dto.DevicePage;
import com.xzkj.health.model.entity.Device;
import com.xzkj.health.service.DeviceService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/device")
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/list")
    public DevicePage list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return deviceService.list(keyword, page, size);
    }

    @PostMapping("/create")
    public Device create(@RequestBody Device device) {
        return deviceService.create(device);
    }

    @PutMapping("/update")
    public Device update(@RequestBody Device device) {
        return deviceService.update(device);
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return Map.of("message", "删除成功");
    }

    @PostMapping("/{id}/bind")
    public Map<String, String> bind(@PathVariable Long id, @RequestBody BindRequest request) {
        deviceService.bind(id, request.getEmpCode());
        return Map.of("message", "绑定成功");
    }

    @PostMapping("/{id}/unbind")
    public Map<String, String> unbind(@PathVariable Long id) {
        deviceService.unbind(id);
        return Map.of("message", "解绑成功");
    }
}
