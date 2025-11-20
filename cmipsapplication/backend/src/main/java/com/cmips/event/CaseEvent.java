package com.cmips.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseEvent {
    private String caseId;
    private String owner;
    private String recipient;
    private String location;
    private Instant createdAt;
}

