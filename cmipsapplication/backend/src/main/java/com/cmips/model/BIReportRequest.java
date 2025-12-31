package com.cmips.model;

import java.time.LocalDate;
import java.util.Map;

public class BIReportRequest {

    private String userRole;
    private String reportType;
    private String targetSystem;
    private String dataFormat;
    private String countyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer chunkSize = 1000;
    private Integer priority = 5;
    private Map<String, Object> additionalFilters;
    private String reportName;
    private String description;

    public BIReportRequest() {}

    public BIReportRequest(String userRole, String reportType, String targetSystem) {
        this.userRole = userRole;
        this.reportType = reportType;
        this.targetSystem = targetSystem;
        this.dataFormat = "JSON";
        this.chunkSize = 1000;
        this.priority = 5;
    }

    public BIReportRequest(String userRole, String reportType, String targetSystem,
                          LocalDate startDate, LocalDate endDate) {
        this.userRole = userRole;
        this.reportType = reportType;
        this.targetSystem = targetSystem;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dataFormat = "JSON";
        this.chunkSize = 1000;
        this.priority = 5;
    }

    public BIReportRequest(String userRole, String reportType, String targetSystem, String dataFormat,
                          String countyId, LocalDate startDate, LocalDate endDate, Integer chunkSize,
                          Integer priority, Map<String, Object> additionalFilters, String reportName, String description) {
        this.userRole = userRole;
        this.reportType = reportType;
        this.targetSystem = targetSystem;
        this.dataFormat = dataFormat;
        this.countyId = countyId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.chunkSize = chunkSize;
        this.priority = priority;
        this.additionalFilters = additionalFilters;
        this.reportName = reportName;
        this.description = description;
    }

    // Getters and Setters
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }

    public String getDataFormat() { return dataFormat; }
    public void setDataFormat(String dataFormat) { this.dataFormat = dataFormat; }

    public String getCountyId() { return countyId; }
    public void setCountyId(String countyId) { this.countyId = countyId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getChunkSize() { return chunkSize; }
    public void setChunkSize(Integer chunkSize) { this.chunkSize = chunkSize; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Map<String, Object> getAdditionalFilters() { return additionalFilters; }
    public void setAdditionalFilters(Map<String, Object> additionalFilters) { this.additionalFilters = additionalFilters; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
