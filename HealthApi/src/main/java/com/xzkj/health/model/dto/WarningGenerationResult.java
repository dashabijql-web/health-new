package com.xzkj.health.model.dto;

import com.xzkj.health.model.entity.WarningRecord;

public class WarningGenerationResult {
    private final HealthThresholdEvaluation evaluation;
    private final boolean created;
    private final WarningRecord warning;

    public WarningGenerationResult(HealthThresholdEvaluation evaluation,
                                   boolean created, WarningRecord warning) {
        this.evaluation = evaluation;
        this.created = created;
        this.warning = warning;
    }

    public HealthThresholdEvaluation getEvaluation() { return evaluation; }
    public boolean isCreated() { return created; }
    public WarningRecord getWarning() { return warning; }
}
