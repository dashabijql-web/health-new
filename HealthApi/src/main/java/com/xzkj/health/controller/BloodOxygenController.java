package com.xzkj.health.controller;

import com.xzkj.health.model.dto.BloodOxygenPage;
import com.xzkj.health.model.dto.BloodOxygenRecord;
import com.xzkj.health.service.BloodOxygenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blood-oxygen")
public class BloodOxygenController {
    private final BloodOxygenService bloodOxygenService;

    public BloodOxygenController(BloodOxygenService bloodOxygenService) {
        this.bloodOxygenService = bloodOxygenService;
    }

    @GetMapping("/list")
    public BloodOxygenPage list(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return bloodOxygenService.list(empCode, startTime, endTime, page, size);
    }

    @GetMapping("/trend")
    public List<BloodOxygenRecord> trend(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return bloodOxygenService.trend(empCode, startTime, endTime);
    }

    @PostMapping("/create")
    public BloodOxygenRecord create(@RequestBody BloodOxygenCreateRequest request) {
        return bloodOxygenService.create(request.getEmpCode(), request.getBloodOxygen(), request.getRecordTime());
    }
}
