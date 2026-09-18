package com.xzkj.health.controller;

import com.xzkj.health.model.dto.SleepPage;
import com.xzkj.health.model.dto.SleepRecord;
import com.xzkj.health.service.SleepService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sleep")
public class SleepController {
    private final SleepService sleepService;

    public SleepController(SleepService sleepService) {
        this.sleepService = sleepService;
    }

    @GetMapping("/list")
    public SleepPage list(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return sleepService.list(empCode, startTime, endTime, page, size);
    }

    @GetMapping("/trend")
    public List<SleepRecord> trend(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return sleepService.trend(empCode, startTime, endTime);
    }

    @PostMapping("/create")
    public SleepRecord create(@RequestBody SleepCreateRequest request) {
        return sleepService.create(request.getEmpCode(), request.getSleepMinutes(), request.getRecordTime());
    }
}
