package com.xzkj.health.model.dto;

public class WarningRecordView {
    private Long id;
    private String userCode;
    private String empName;
    private String warningType;
    private String indicatorName;
    private String indicatorValue;
    private String warningLevel;
    private String eventSource;
    private String eventCode;
    private String deviceImei;
    private String thresholdSnapshot;
    private Boolean handled;
    private String handleTime;
    private String handleBy;
    private String remark;
    private String createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }
    public String getWarningType() { return warningType; }
    public void setWarningType(String warningType) { this.warningType = warningType; }
    public String getIndicatorName() { return indicatorName; }
    public void setIndicatorName(String indicatorName) { this.indicatorName = indicatorName; }
    public String getIndicatorValue() { return indicatorValue; }
    public void setIndicatorValue(String indicatorValue) { this.indicatorValue = indicatorValue; }
    public String getWarningLevel() { return warningLevel; }
    public void setWarningLevel(String warningLevel) { this.warningLevel = warningLevel; }
    public String getEventSource() { return eventSource; }
    public void setEventSource(String eventSource) { this.eventSource = eventSource; }
    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }
    public String getDeviceImei() { return deviceImei; }
    public void setDeviceImei(String deviceImei) { this.deviceImei = deviceImei; }
    public String getThresholdSnapshot() { return thresholdSnapshot; }
    public void setThresholdSnapshot(String thresholdSnapshot) { this.thresholdSnapshot = thresholdSnapshot; }
    public Boolean getHandled() { return handled; }
    public void setHandled(Boolean handled) { this.handled = handled; }
    public String getHandleTime() { return handleTime; }
    public void setHandleTime(String handleTime) { this.handleTime = handleTime; }
    public String getHandleBy() { return handleBy; }
    public void setHandleBy(String handleBy) { this.handleBy = handleBy; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
