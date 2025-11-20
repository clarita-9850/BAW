package com.cmips.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {
    private Long taskId;
    private String title;
    private String assignedTo;
    private String status;
    private String previousStatus;
}

