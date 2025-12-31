package com.cmips.model;

import java.time.LocalDateTime;

public class ReportGenerationEvent {
    private String eventType;
    private String reportId;
    private String reportType;
    private String userRole;
    private int totalRecords;
    private LocalDateTime generatedAt;

    public ReportGenerationEvent() {}

    public ReportGenerationEvent(String eventType, String reportId, String reportType, String userRole,
                                 int totalRecords, LocalDateTime generatedAt) {
        this.eventType = eventType;
        this.reportId = reportId;
        this.reportType = reportType;
        this.userRole = userRole;
        this.totalRecords = totalRecords;
        this.generatedAt = generatedAt;
    }

    // Getters and Setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}

