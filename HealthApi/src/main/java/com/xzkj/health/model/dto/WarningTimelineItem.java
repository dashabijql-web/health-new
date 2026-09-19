package com.xzkj.health.model.dto;

public class WarningTimelineItem {
    private String actionId;
    private String action;
    private String result;
    private String operator;
    private String target;
    private String remark;
    private String createdAt;

    public String getActionId() { return actionId; }
    public void setActionId(String actionId) { this.actionId = actionId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
