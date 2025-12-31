package com.cmips.model;

import java.time.LocalDateTime;
import java.util.Map;

public class MaskedTimesheetData {
    private String timesheetId;
    private String userRole;
    private String reportType;
    private LocalDateTime maskedAt;
    private Map<String, Object> fields;

    public MaskedTimesheetData() {}

    public MaskedTimesheetData(String timesheetId, String userRole, String reportType,
                               LocalDateTime maskedAt, Map<String, Object> fields) {
        this.timesheetId = timesheetId;
        this.userRole = userRole;
        this.reportType = reportType;
        this.maskedAt = maskedAt;
        this.fields = fields;
    }

    // Getters and Setters
    public String getTimesheetId() { return timesheetId; }
    public void setTimesheetId(String timesheetId) { this.timesheetId = timesheetId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public LocalDateTime getMaskedAt() { return maskedAt; }
    public void setMaskedAt(LocalDateTime maskedAt) { this.maskedAt = maskedAt; }

    public Map<String, Object> getFields() { return fields; }
    public void setFields(Map<String, Object> fields) { this.fields = fields; }
}

