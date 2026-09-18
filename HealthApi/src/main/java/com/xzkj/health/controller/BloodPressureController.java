package com.xzkj.health.controller;

import com.xzkj.health.model.dto.BloodPressurePage;
import com.xzkj.health.model.dto.BloodPressureRecord;
import com.xzkj.health.service.BloodPressureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blood-pressure")
public class BloodPressureController {
    private final BloodPressureService bloodPressureService;

    public BloodPressureController(BloodPressureService bloodPressureService) {
        this.bloodPressureService = bloodPressureService;
    }

    @GetMapping("/list")
    public BloodPressurePage list(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return bloodPressureService.list(empCode, startTime, endTime, page, size);
    }

    @GetMapping("/trend")
    public List<BloodPressureRecord> trend(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return bloodPressureService.trend(empCode, startTime, endTime);
    }

    @PostMapping("/create")
    public BloodPressureRecord create(@RequestBody BloodPressureCreateRequest request) {
        return bloodPressureService.create(
                request.getEmpCode(),
                request.getSystolic(),
                request.getDiastolic(),
                request.getRecordTime());
    }
}
