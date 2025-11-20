package com.cmips.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetEvent {
    private Long timesheetId;
    private String providerId;
    private String errorMessage;
    private String priority;
}

