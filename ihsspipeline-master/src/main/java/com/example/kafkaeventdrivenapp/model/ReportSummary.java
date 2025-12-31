package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummary {
    private int totalRecords;
    private int approvedRecords;
    private int rejectedRecords;
    private int pendingRecords;
    private double totalHours;
    private double totalAmount;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> projectDistribution;
    private Map<String, Long> providerDistribution;
    private Map<String, Integer> fieldVisibility;
}
