package com.xzkj.health.controller;

import com.xzkj.health.model.entity.AlertConfig;
import com.xzkj.health.model.dto.EffectiveAlertConfig;
import com.xzkj.health.model.dto.HealthThresholdEvaluation;
import com.xzkj.health.service.AlertConfigService;
import com.xzkj.health.service.HealthThresholdService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alert-config")
public class AlertConfigController {
    private final AlertConfigService alertConfigService;
    private final HealthThresholdService healthThresholdService;

    public AlertConfigController(AlertConfigService alertConfigService,
                                 HealthThresholdService healthThresholdService) {
        this.alertConfigService = alertConfigService;
        this.healthThresholdService = healthThresholdService;
    }

    @GetMapping("/list")
    public List<AlertConfig> list() {
        return alertConfigService.list();
    }

    @GetMapping("/effective")
    public EffectiveAlertConfig effective(@RequestParam String empCode,
                                          @RequestParam Integer configType) {
        return alertConfigService.effective(empCode, configType);
    }

    @PostMapping("/evaluate")
    public HealthThresholdEvaluation evaluate(@RequestBody HealthThresholdEvaluateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("判断参数不能为空");
        }
        return healthThresholdService.evaluate(request.getEmpCode(), request.getConfigType(), request.getValue());
    }

    @PutMapping("/update")
    public AlertConfig update(@RequestBody AlertConfig config) {
        return alertConfigService.update(config);
    }

    @PutMapping("/toggle/{id}")
    public AlertConfig toggle(@PathVariable Long id) {
        return alertConfigService.toggle(id);
    }
}
