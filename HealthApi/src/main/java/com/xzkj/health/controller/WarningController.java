package com.xzkj.health.controller;

import com.xzkj.health.model.dto.WarningGenerationResult;
import com.xzkj.health.model.dto.WarningSourceCatalog;
import com.xzkj.health.service.HealthWarningService;
import com.xzkj.health.service.WarningClassificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/warning")
public class WarningController {
    private final HealthWarningService healthWarningService;
    private final WarningClassificationService warningClassificationService;

    public WarningController(HealthWarningService healthWarningService,
                             WarningClassificationService warningClassificationService) {
        this.healthWarningService = healthWarningService;
        this.warningClassificationService = warningClassificationService;
    }

    @GetMapping("/classifications")
    public List<WarningSourceCatalog> classifications() {
        return warningClassificationService.catalog();
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
