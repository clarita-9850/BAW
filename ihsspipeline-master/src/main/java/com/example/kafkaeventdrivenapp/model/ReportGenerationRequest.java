package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationRequest {
    private String userRole;
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> statusFilter;
    private List<String> providerFilter;
    private List<String> projectFilter;
    private String outputFormat = "JSON";
    private boolean includeSummary = true;
    private boolean includeCharts = false;
    
    // Access control fields
    private String userCounty;
    
    // Pagination fields
    private Integer page = 0;  // 0-based page number
    private Integer pageSize = 1000;  // Records per page

    public PipelineExtractionRequest.DateRange getDateRange() {
        if (startDate != null && endDate != null) {
            return new PipelineExtractionRequest.DateRange(startDate, endDate);
        }
        return null;
    }
}
