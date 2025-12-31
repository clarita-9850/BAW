package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaskedTimesheetData {
    private String timesheetId;
    private String userRole;
    private String reportType;
    private LocalDateTime maskedAt;
    private Map<String, Object> fields;
}
