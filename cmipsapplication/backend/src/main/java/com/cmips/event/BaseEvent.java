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
public class BaseEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String userId;
    private String source;
    private Object payload;
    private Metadata metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private String traceId;
        private String correlationId;
        private String version;
    }
}




