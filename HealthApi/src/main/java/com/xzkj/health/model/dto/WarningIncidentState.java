package com.xzkj.health.model.dto;

public class WarningIncidentState {
    private Long warningId;
    private String occurredAt;
    private String status;
    private Long ownerUserId;
    private String ownerName;
    private String ownerDept;
    private String slaDueAt;
    private Integer slaMinutes;
    private String updatedAt;

    public Long getWarningId() { return warningId; }
    public void setWarningId(Long warningId) { this.warningId = warningId; }
    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = occurredAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerDept() { return ownerDept; }
    public void setOwnerDept(String ownerDept) { this.ownerDept = ownerDept; }
    public String getSlaDueAt() { return slaDueAt; }
    public void setSlaDueAt(String slaDueAt) { this.slaDueAt = slaDueAt; }
    public Integer getSlaMinutes() { return slaMinutes; }
    public void setSlaMinutes(Integer slaMinutes) { this.slaMinutes = slaMinutes; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
