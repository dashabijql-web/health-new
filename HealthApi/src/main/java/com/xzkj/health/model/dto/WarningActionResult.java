package com.xzkj.health.model.dto;

public class WarningActionResult {
    private final String actionId;
    private final String action;
    private final String message;
    private final WarningIncidentState incident;

    public WarningActionResult(String actionId, String action, String message,
                               WarningIncidentState incident) {
        this.actionId = actionId;
        this.action = action;
        this.message = message;
        this.incident = incident;
    }

    public String getActionId() { return actionId; }
    public String getAction() { return action; }
    public String getMessage() { return message; }
    public WarningIncidentState getIncident() { return incident; }
}
