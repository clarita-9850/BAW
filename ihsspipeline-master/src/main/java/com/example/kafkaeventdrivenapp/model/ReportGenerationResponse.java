package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationResponse {
    private String reportId;
    private String reportType;
    private String userRole;
    private LocalDateTime generatedAt;
    private ReportData data;
    private ExtractionSummary summary;
    private int totalRecords;
    private String status;
    private String errorMessage;
}
