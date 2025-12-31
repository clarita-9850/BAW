package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionSummary {
    private int totalRecords;
    private int maskedRecords;
    private String userRole;
    private String reportType;
    private LocalDateTime extractionTimestamp;
    private Map<String, Integer> fieldVisibility;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> projectDistribution;
    private Map<String, Long> providerDistribution;
    private Map<String, Long> countyDistribution;
}
