package com.cmips.model;

import java.time.LocalDateTime;

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

    public ReportRecord() {}

    public ReportRecord(String timesheetId, String providerId, String providerName, String providerEmail,
                        String providerDepartment, String recipientId, String recipientName, String recipientEmail,
                        String projectId, String projectName, Double projectBudget, LocalDateTime startDate,
                        LocalDateTime endDate, Double totalHours, Double hourlyRate, Double totalAmount,
                        String status, String comments, String description, String rejectionReason,
                        Integer revisionCount, String validationResult, String validationMessage,
                        LocalDateTime submittedAt, LocalDateTime approvedAt, String approvalComments) {
        this.timesheetId = timesheetId;
        this.providerId = providerId;
        this.providerName = providerName;
        this.providerEmail = providerEmail;
        this.providerDepartment = providerDepartment;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.recipientEmail = recipientEmail;
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectBudget = projectBudget;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalHours = totalHours;
        this.hourlyRate = hourlyRate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.comments = comments;
        this.description = description;
        this.rejectionReason = rejectionReason;
        this.revisionCount = revisionCount;
        this.validationResult = validationResult;
        this.validationMessage = validationMessage;
        this.submittedAt = submittedAt;
        this.approvedAt = approvedAt;
        this.approvalComments = approvalComments;
    }

    // Getters and Setters
    public String getTimesheetId() { return timesheetId; }
    public void setTimesheetId(String timesheetId) { this.timesheetId = timesheetId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProviderEmail() { return providerEmail; }
    public void setProviderEmail(String providerEmail) { this.providerEmail = providerEmail; }

    public String getProviderDepartment() { return providerDepartment; }
    public void setProviderDepartment(String providerDepartment) { this.providerDepartment = providerDepartment; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public Double getProjectBudget() { return projectBudget; }
    public void setProjectBudget(Double projectBudget) { this.projectBudget = projectBudget; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Double getTotalHours() { return totalHours; }
    public void setTotalHours(Double totalHours) { this.totalHours = totalHours; }

    public Double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Integer getRevisionCount() { return revisionCount; }
    public void setRevisionCount(Integer revisionCount) { this.revisionCount = revisionCount; }

    public String getValidationResult() { return validationResult; }
    public void setValidationResult(String validationResult) { this.validationResult = validationResult; }

    public String getValidationMessage() { return validationMessage; }
    public void setValidationMessage(String validationMessage) { this.validationMessage = validationMessage; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getApprovalComments() { return approvalComments; }
    public void setApprovalComments(String approvalComments) { this.approvalComments = approvalComments; }
}

