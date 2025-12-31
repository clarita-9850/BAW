package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetApproval {
    private String approvalId;
    private String timesheetId;
    private String recipientId; // ID of the user who approved/rejected
    private String status; // APPROVED, REJECTED, REVISION_REQUESTED
    private String comments; // Recipient's comments
    private LocalDateTime approvalDate;
}
