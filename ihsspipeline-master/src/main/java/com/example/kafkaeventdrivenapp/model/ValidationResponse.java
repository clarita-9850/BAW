package com.example.kafkaeventdrivenapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ValidationResponse {
    private String timesheetId;
    private String validationResult; // APPROVED, REJECTED
    private String reason;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validatedAt;
    private String validatorId;
    private double totalHours;
    private boolean requiresRevision;

    // Constructors
    public ValidationResponse() {}

    public ValidationResponse(String timesheetId, String validationResult, String reason, 
                            String message, LocalDateTime validatedAt, String validatorId, 
                            double totalHours, boolean requiresRevision) {
        this.timesheetId = timesheetId;
        this.validationResult = validationResult;
        this.reason = reason;
        this.message = message;
        this.validatedAt = validatedAt;
        this.validatorId = validatorId;
        this.totalHours = totalHours;
        this.requiresRevision = requiresRevision;
    }

    // Getters and Setters
    public String getTimesheetId() {
        return timesheetId;
    }

    public void setTimesheetId(String timesheetId) {
        this.timesheetId = timesheetId;
    }

    public String getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(String validationResult) {
        this.validationResult = validationResult;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public String getValidatorId() {
        return validatorId;
    }

    public void setValidatorId(String validatorId) {
        this.validatorId = validatorId;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public boolean isRequiresRevision() {
        return requiresRevision;
    }

    public void setRequiresRevision(boolean requiresRevision) {
        this.requiresRevision = requiresRevision;
    }

    @Override
    public String toString() {
        return "ValidationResponse{" +
                "timesheetId='" + timesheetId + '\'' +
                ", validationResult='" + validationResult + '\'' +
                ", reason='" + reason + '\'' +
                ", message='" + message + '\'' +
                ", validatedAt=" + validatedAt +
                ", validatorId='" + validatorId + '\'' +
                ", totalHours=" + totalHours +
                ", requiresRevision=" + requiresRevision +
                '}';
    }
}
