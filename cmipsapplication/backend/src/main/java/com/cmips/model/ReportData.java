package com.cmips.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ReportData {
    private String reportType;
    private String userRole;
    private LocalDateTime generatedAt;
    private List<Map<String, Object>> records;
    private Integer totalRecords;
    private Long totalCount; // For pagination
    private Map<String, Object> fieldVisibility;
    private Map<String, Object> statusDistribution;

    public ReportData() {}

    public ReportData(String reportType, String userRole, LocalDateTime generatedAt,
                      List<Map<String, Object>> records, Integer totalRecords, Long totalCount,
                      Map<String, Object> fieldVisibility, Map<String, Object> statusDistribution) {
        this.reportType = reportType;
        this.userRole = userRole;
        this.generatedAt = generatedAt;
        this.records = records;
        this.totalRecords = totalRecords;
        this.totalCount = totalCount;
        this.fieldVisibility = fieldVisibility;
        this.statusDistribution = statusDistribution;
    }

    // Getters and Setters
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public List<Map<String, Object>> getRecords() { return records; }
    public void setRecords(List<Map<String, Object>> records) { this.records = records; }

    public Integer getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }

    public Long getTotalCount() { return totalCount; }
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }

    public Map<String, Object> getFieldVisibility() { return fieldVisibility; }
    public void setFieldVisibility(Map<String, Object> fieldVisibility) { this.fieldVisibility = fieldVisibility; }

    public Map<String, Object> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Object> statusDistribution) { this.statusDistribution = statusDistribution; }
}
