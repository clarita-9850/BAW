package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationEvent {
    private String eventType;
    private String reportId;
    private String reportType;
    private String userRole;
    private int totalRecords;
    private LocalDateTime generatedAt;
}
