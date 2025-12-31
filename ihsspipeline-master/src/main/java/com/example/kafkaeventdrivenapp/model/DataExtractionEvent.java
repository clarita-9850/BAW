package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataExtractionEvent {
    private String eventType;
    private String userRole;
    private String reportType;
    private int totalRecords;
    private int maskedRecords;
    private LocalDateTime timestamp;
    private ExtractionSummary summary;
}
