package com.cmips.model;

import java.time.LocalDateTime;

public class ReportGenerationResponse {
    private String reportId;
    private String reportType;
    private String userRole;
    private LocalDateTime generatedAt;
    private ReportData data;
    private int totalRecords;
    private String status;
    private String errorMessage;
    private String generatedAtString; // For frontend compatibility

    public ReportGenerationResponse() {}

    public ReportGenerationResponse(String reportId, String reportType, String userRole, LocalDateTime generatedAt,
                                    ReportData data, int totalRecords, String status, String errorMessage,
                                    String generatedAtString) {
        this.reportId = reportId;
        this.reportType = reportType;
        this.userRole = userRole;
        this.generatedAt = generatedAt;
        this.data = data;
        this.totalRecords = totalRecords;
        this.status = status;
        this.errorMessage = errorMessage;
        this.generatedAtString = generatedAtString;
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public ReportData getData() { return data; }
    public void setData(ReportData data) { this.data = data; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getGeneratedAtString() { return generatedAtString; }
    public void setGeneratedAtString(String generatedAtString) { this.generatedAtString = generatedAtString; }
}
