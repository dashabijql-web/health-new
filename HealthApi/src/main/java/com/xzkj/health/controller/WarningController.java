package com.xzkj.health.controller;

import com.xzkj.health.model.dto.WarningGenerationResult;
import com.xzkj.health.model.dto.WarningActionResult;
import com.xzkj.health.model.dto.WarningIncidentState;
import com.xzkj.health.model.dto.WarningSourceCatalog;
import com.xzkj.health.model.dto.WarningRecordPage;
import com.xzkj.health.model.dto.WarningRecordView;
import com.xzkj.health.service.HealthWarningService;
import com.xzkj.health.service.WarningClassificationService;
import com.xzkj.health.service.WarningQueryService;
import com.xzkj.health.service.WarningLifecycleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/warning")
public class WarningController {
    private final HealthWarningService healthWarningService;
    private final WarningClassificationService warningClassificationService;
    private final WarningQueryService warningQueryService;
    private final WarningLifecycleService warningLifecycleService;

    public WarningController(HealthWarningService healthWarningService,
                             WarningClassificationService warningClassificationService,
                             WarningQueryService warningQueryService,
                             WarningLifecycleService warningLifecycleService) {
        this.healthWarningService = healthWarningService;
        this.warningClassificationService = warningClassificationService;
        this.warningQueryService = warningQueryService;
        this.warningLifecycleService = warningLifecycleService;
    }

    @GetMapping("/classifications")
    public List<WarningSourceCatalog> classifications() {
        return warningClassificationService.catalog();
    }

    @GetMapping("/list")
    public WarningRecordPage list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String eventSource,
                                  @RequestParam(required = false) String warningLevel,
                                  @RequestParam(required = false) Boolean handled,
                                  @RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "20") Integer size) {
        return warningQueryService.list(keyword, eventSource, warningLevel, handled,
                startDate, endDate, page, size);
    }

    @GetMapping("/detail/{id}")
    public WarningRecordView detail(@PathVariable Long id,
                                    @RequestParam String createTime) {
        return warningQueryService.detail(id, createTime);
    }

    @GetMapping("/state/{id}")
    public WarningIncidentState state(@PathVariable Long id,
                                      @RequestParam String occurredAt) {
        return warningLifecycleService.state(id, occurredAt);
    }

    @PutMapping("/{id}/ack")
    public WarningActionResult acknowledge(@PathVariable Long id,
                                           @RequestBody WarningActionRequest request) {
        requireActionRequest(request);
        return warningLifecycleService.acknowledge(id, request.getOccurredAt(), request.getRemark());
    }

    @PutMapping("/{id}/assign")
    public WarningActionResult assign(@PathVariable Long id,
                                      @RequestBody WarningActionRequest request) {
        requireActionRequest(request);
        return warningLifecycleService.assign(id, request.getOccurredAt(), request.getOwnerUserId(),
                request.getSlaMinutes(), request.getRemark());
    }

    @PutMapping("/{id}/resolve")
    public WarningActionResult resolve(@PathVariable Long id,
                                       @RequestBody WarningActionRequest request) {
        requireActionRequest(request);
        return warningLifecycleService.resolve(id, request.getOccurredAt(), request.getRemark());
    }

    @PutMapping("/{id}/close")
    public WarningActionResult close(@PathVariable Long id,
                                     @RequestBody WarningActionRequest request) {
        requireActionRequest(request);
        return warningLifecycleService.close(id, request.getOccurredAt(), request.getRemark());
    }

    @PutMapping("/{id}/false-alarm")
    public WarningActionResult falseAlarm(@PathVariable Long id,
                                          @RequestBody WarningActionRequest request) {
        requireActionRequest(request);
        return warningLifecycleService.falseAlarm(id, request.getOccurredAt(), request.getRemark());
    }

    @PostMapping("/generate")
    public WarningGenerationResult generate(@RequestBody HealthThresholdEvaluateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("判断参数不能为空");
        }
        return healthWarningService.evaluateAndCreate(
                request.getEmpCode(), request.getConfigType(), request.getValue());
    }

    private void requireActionRequest(WarningActionRequest request) {
        if (request == null || request.getOccurredAt() == null || request.getOccurredAt().isBlank()) {
            throw new IllegalArgumentException("预警发生时间不能为空");
        }
    }
}
