package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRecord {
    private String timesheetId;
    private String providerId;
    private String providerName;
    private String providerEmail;
    private String providerDepartment;
    private String recipientId;
    private String recipientName;
    private String recipientEmail;
    private String projectId;
    private String projectName;
    private Double projectBudget;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double totalHours;
    private Double hourlyRate;
    private Double totalAmount;
    private String status;
    private String comments;
    private String description;
    private String rejectionReason;
    private Integer revisionCount;
    private String validationResult;
    private String validationMessage;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private String approvalComments;
}
