package com.xzkj.health.controller;

import com.xzkj.health.model.dto.WarningGenerationResult;
import com.xzkj.health.service.HealthWarningService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/warning")
public class WarningController {
    private final HealthWarningService healthWarningService;

    public WarningController(HealthWarningService healthWarningService) {
        this.healthWarningService = healthWarningService;
    }

    @PostMapping("/generate")
    public WarningGenerationResult generate(@RequestBody HealthThresholdEvaluateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("判断参数不能为空");
        }
        return healthWarningService.evaluateAndCreate(
                request.getEmpCode(), request.getConfigType(), request.getValue());
    }
}
