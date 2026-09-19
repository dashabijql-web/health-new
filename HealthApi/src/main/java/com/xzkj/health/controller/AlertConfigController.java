package com.xzkj.health.controller;

import com.xzkj.health.model.entity.AlertConfig;
import com.xzkj.health.service.AlertConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alert-config")
public class AlertConfigController {
    private final AlertConfigService alertConfigService;

    public AlertConfigController(AlertConfigService alertConfigService) {
        this.alertConfigService = alertConfigService;
    }

    @GetMapping("/list")
    public List<AlertConfig> list() {
        return alertConfigService.list();
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
