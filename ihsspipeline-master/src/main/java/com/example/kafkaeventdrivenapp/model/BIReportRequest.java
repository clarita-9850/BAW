package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BIReportRequest {
    
    private String userRole;
    private String reportType;
    private String targetSystem; // BUSINESS_OBJECTS, CRYSTAL_REPORTS, etc.
    private String dataFormat; // JSON, XML, CSV, EXCEL
    private String countyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer chunkSize = 1000;
    private Integer priority = 5; // 1-10, higher number = higher priority
    private Map<String, Object> additionalFilters;
    private String reportName;
    private String description;
    
    // Constructor for basic requests
    public BIReportRequest(String userRole, String reportType, String targetSystem) {
        this.userRole = userRole;
        this.reportType = reportType;
        this.targetSystem = targetSystem;
        this.dataFormat = "JSON";
        this.chunkSize = 1000;
        this.priority = 5;
    }
    
    // Constructor with date range
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
}
