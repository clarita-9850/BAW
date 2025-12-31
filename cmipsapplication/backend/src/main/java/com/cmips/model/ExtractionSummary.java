package com.cmips.model;

import java.time.LocalDateTime;
import java.util.Map;

public class ExtractionSummary {
    private int totalRecords;
    private int maskedRecords;
    private String userRole;
    private String reportType;
    private LocalDateTime extractionTimestamp;
    private Map<String, Object> fieldVisibility;
    private Map<String, Object> statusDistribution;
    private Map<String, Object> projectDistribution;
    private Map<String, Object> providerDistribution;
    private Map<String, Object> countyDistribution;

    public ExtractionSummary() {}

    public ExtractionSummary(int totalRecords, int maskedRecords, String userRole, String reportType,
                             LocalDateTime extractionTimestamp, Map<String, Object> fieldVisibility,
                             Map<String, Object> statusDistribution, Map<String, Object> projectDistribution,
                             Map<String, Object> providerDistribution, Map<String, Object> countyDistribution) {
        this.totalRecords = totalRecords;
        this.maskedRecords = maskedRecords;
        this.userRole = userRole;
        this.reportType = reportType;
        this.extractionTimestamp = extractionTimestamp;
        this.fieldVisibility = fieldVisibility;
        this.statusDistribution = statusDistribution;
        this.projectDistribution = projectDistribution;
        this.providerDistribution = providerDistribution;
        this.countyDistribution = countyDistribution;
    }

    // Getters and Setters
    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getMaskedRecords() { return maskedRecords; }
    public void setMaskedRecords(int maskedRecords) { this.maskedRecords = maskedRecords; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public LocalDateTime getExtractionTimestamp() { return extractionTimestamp; }
    public void setExtractionTimestamp(LocalDateTime extractionTimestamp) { this.extractionTimestamp = extractionTimestamp; }

    public Map<String, Object> getFieldVisibility() { return fieldVisibility; }
    public void setFieldVisibility(Map<String, Object> fieldVisibility) { this.fieldVisibility = fieldVisibility; }

    public Map<String, Object> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Object> statusDistribution) { this.statusDistribution = statusDistribution; }

    public Map<String, Object> getProjectDistribution() { return projectDistribution; }
    public void setProjectDistribution(Map<String, Object> projectDistribution) { this.projectDistribution = projectDistribution; }

    public Map<String, Object> getProviderDistribution() { return providerDistribution; }
    public void setProviderDistribution(Map<String, Object> providerDistribution) { this.providerDistribution = providerDistribution; }

    public Map<String, Object> getCountyDistribution() { return countyDistribution; }
    public void setCountyDistribution(Map<String, Object> countyDistribution) { this.countyDistribution = countyDistribution; }
}

