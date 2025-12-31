package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
