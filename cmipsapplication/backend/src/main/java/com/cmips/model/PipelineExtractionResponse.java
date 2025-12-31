package com.cmips.model;

import java.time.LocalDateTime;
import java.util.List;

public class PipelineExtractionResponse {
    private String extractionId;
    private String userRole;
    private String reportType;
    private LocalDateTime extractedAt;
    private int totalRecords;
    private int maskedRecords;
    private List<MaskedTimesheetData> data;
    private ExtractionSummary summary;
    private String status;
    private String errorMessage;

    public PipelineExtractionResponse() {}

    public PipelineExtractionResponse(String extractionId, String userRole, String reportType,
                                      LocalDateTime extractedAt, int totalRecords, int maskedRecords,
                                      List<MaskedTimesheetData> data, ExtractionSummary summary,
                                      String status, String errorMessage) {
        this.extractionId = extractionId;
        this.userRole = userRole;
        this.reportType = reportType;
        this.extractedAt = extractedAt;
        this.totalRecords = totalRecords;
        this.maskedRecords = maskedRecords;
        this.data = data;
        this.summary = summary;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    // Getters and Setters
    public String getExtractionId() { return extractionId; }
    public void setExtractionId(String extractionId) { this.extractionId = extractionId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public LocalDateTime getExtractedAt() { return extractedAt; }
    public void setExtractedAt(LocalDateTime extractedAt) { this.extractedAt = extractedAt; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getMaskedRecords() { return maskedRecords; }
    public void setMaskedRecords(int maskedRecords) { this.maskedRecords = maskedRecords; }

    public List<MaskedTimesheetData> getData() { return data; }
    public void setData(List<MaskedTimesheetData> data) { this.data = data; }

    public ExtractionSummary getSummary() { return summary; }
    public void setSummary(ExtractionSummary summary) { this.summary = summary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

