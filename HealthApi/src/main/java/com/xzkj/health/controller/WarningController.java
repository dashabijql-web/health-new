package com.xzkj.health.controller;

import com.xzkj.health.model.dto.WarningGenerationResult;
import com.xzkj.health.model.dto.WarningSourceCatalog;
import com.xzkj.health.model.dto.WarningRecordPage;
import com.xzkj.health.model.dto.WarningRecordView;
import com.xzkj.health.service.HealthWarningService;
import com.xzkj.health.service.WarningClassificationService;
import com.xzkj.health.service.WarningQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    public WarningController(HealthWarningService healthWarningService,
                             WarningClassificationService warningClassificationService,
                             WarningQueryService warningQueryService) {
        this.healthWarningService = healthWarningService;
        this.warningClassificationService = warningClassificationService;
        this.warningQueryService = warningQueryService;
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

    @PostMapping("/generate")
    public WarningGenerationResult generate(@RequestBody HealthThresholdEvaluateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("判断参数不能为空");
        }
        return healthWarningService.evaluateAndCreate(
                request.getEmpCode(), request.getConfigType(), request.getValue());
    }
}
