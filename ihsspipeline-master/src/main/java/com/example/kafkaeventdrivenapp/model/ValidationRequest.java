package com.example.kafkaeventdrivenapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public class ValidationRequest {
    private String timesheetId;
    private String providerId;
    private String recipientId;
    private double totalHours;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEnding;
    private List<Timesheet.DailyHours> entries;
    private String description;
    private String status;

    // Constructors
    public ValidationRequest() {}

    public ValidationRequest(String timesheetId, String providerId, String recipientId, 
                           double totalHours, LocalDate weekEnding, List<Timesheet.DailyHours> entries, 
                           String description, String status) {
        this.timesheetId = timesheetId;
        this.providerId = providerId;
        this.recipientId = recipientId;
        this.totalHours = totalHours;
        this.weekEnding = weekEnding;
        this.entries = entries;
        this.description = description;
        this.status = status;
    }

    // Getters and Setters
    public String getTimesheetId() {
        return timesheetId;
    }

    public void setTimesheetId(String timesheetId) {
        this.timesheetId = timesheetId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public LocalDate getWeekEnding() {
        return weekEnding;
    }

    public void setWeekEnding(LocalDate weekEnding) {
        this.weekEnding = weekEnding;
    }

    public List<Timesheet.DailyHours> getEntries() {
        return entries;
    }

    public void setEntries(List<Timesheet.DailyHours> entries) {
        this.entries = entries;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ValidationRequest{" +
                "timesheetId='" + timesheetId + '\'' +
                ", providerId='" + providerId + '\'' +
                ", recipientId='" + recipientId + '\'' +
                ", totalHours=" + totalHours +
                ", weekEnding=" + weekEnding +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
