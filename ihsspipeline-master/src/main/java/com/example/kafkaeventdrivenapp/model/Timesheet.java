package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Timesheet {
    private String timesheetId;
    private String providerId;
    private String projectId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyHours> dailyHours;
    private String status; // e.g., SUBMITTED, APPROVED, REJECTED, PENDING_REVISION
    private String comments; // Provider's comments
    private String recipientId; // The ID of the recipient who will review this timesheet
    private double totalHours; // Total hours for the timesheet
    private LocalDate weekEnding; // Week ending date
    private String description; // Description of the timesheet

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyHours {
        private LocalDate date;
        private double hours;
        private String taskDescription;
    }
}
