package com.xzkj.health.controller;

import com.xzkj.health.model.dto.HeartRatePage;
import com.xzkj.health.model.dto.HeartRateRecord;
import com.xzkj.health.service.HeartRateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/heart-rate")
public class HeartRateController {
    private final HeartRateService heartRateService;

    public HeartRateController(HeartRateService heartRateService) {
        this.heartRateService = heartRateService;
    }

    @GetMapping("/list")
    public HeartRatePage list(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return heartRateService.list(empCode, startTime, endTime, page, size);
    }

    @GetMapping("/trend")
    public List<HeartRateRecord> trend(
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return heartRateService.trend(empCode, startTime, endTime);
    }

    @PostMapping("/create")
    public HeartRateRecord create(@RequestBody HeartRateCreateRequest request) {
        return heartRateService.create(request.getEmpCode(), request.getHeartRate(), request.getRecordTime());
    }
}
