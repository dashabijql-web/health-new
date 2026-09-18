package com.xzkj.health.controller;

import com.xzkj.health.model.dto.PressurePage;
import com.xzkj.health.model.dto.PressureRecord;
import com.xzkj.health.service.PressureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pressure")
public class PressureController {
    private final PressureService pressureService;

    public PressureController(PressureService pressureService) {
        this.pressureService = pressureService;
    }

    @GetMapping("/list")
    public PressurePage list(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return pressureService.list(empCode, startTime, endTime, page, size);
    }

    @GetMapping("/trend")
    public List<PressureRecord> trend(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return pressureService.trend(empCode, startTime, endTime);
    }

    @PostMapping("/create")
    public PressureRecord create(@RequestBody PressureCreateRequest request) {
        return pressureService.create(request.getEmpCode(), request.getPressure(), request.getRecordTime());
    }
}
