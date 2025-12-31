package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportData {
    private String reportType;
    private String userRole;
    private LocalDateTime generatedAt;
    private List<Map<String, Object>> records;
    private Map<String, Object> reportContent; // New field for formatted report
    private int totalRecords; // Records in current page
    private long totalCount; // Total records in date range (for pagination)
    private Map<String, Integer> fieldVisibility;
    private Map<String, Long> statusDistribution;
    private ReportSummary summary;
}
