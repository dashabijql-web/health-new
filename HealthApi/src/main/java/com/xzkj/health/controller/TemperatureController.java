package com.xzkj.health.controller;

import com.xzkj.health.model.dto.TemperaturePage;
import com.xzkj.health.model.dto.TemperatureRecord;
import com.xzkj.health.service.TemperatureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/temperature")
public class TemperatureController {
    private final TemperatureService temperatureService;

    public TemperatureController(TemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    @GetMapping("/list")
    public TemperaturePage list(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return temperatureService.list(empCode, startTime, endTime, page, size);
    }

    @GetMapping("/trend")
    public List<TemperatureRecord> trend(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return temperatureService.trend(empCode, startTime, endTime);
    }

    @PostMapping("/create")
    public TemperatureRecord create(@RequestBody TemperatureCreateRequest request) {
        return temperatureService.create(request.getEmpCode(), request.getTemperature(), request.getRecordTime());
    }
}
