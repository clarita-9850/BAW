package com.cmips.event;

import java.time.Instant;

public class BaseEvent {
    private String eventId;
    private String eventType;
    private Instant timestamp;
    private String userId;
    private String source;
    private Object payload;
    private Metadata metadata;

    public BaseEvent() {}

    public BaseEvent(String eventId, String eventType, Instant timestamp, String userId,
                    String source, Object payload, Metadata metadata) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.userId = userId;
        this.source = source;
        this.payload = payload;
        this.metadata = metadata;
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }

    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    // Builder
    public static BaseEventBuilder builder() { return new BaseEventBuilder(); }

    public static class BaseEventBuilder {
        private String eventId;
        private String eventType;
        private Instant timestamp;
        private String userId;
        private String source;
        private Object payload;
        private Metadata metadata;

        public BaseEventBuilder eventId(String eventId) { this.eventId = eventId; return this; }
        public BaseEventBuilder eventType(String eventType) { this.eventType = eventType; return this; }
        public BaseEventBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public BaseEventBuilder userId(String userId) { this.userId = userId; return this; }
        public BaseEventBuilder source(String source) { this.source = source; return this; }
        public BaseEventBuilder payload(Object payload) { this.payload = payload; return this; }
        public BaseEventBuilder metadata(Metadata metadata) { this.metadata = metadata; return this; }

        public BaseEvent build() {
            return new BaseEvent(eventId, eventType, timestamp, userId, source, payload, metadata);
        }
    }

    public static class Metadata {
        private String traceId;
        private String correlationId;
        private String version;

        public Metadata() {}

        public Metadata(String traceId, String correlationId, String version) {
            this.traceId = traceId;
            this.correlationId = correlationId;
            this.version = version;
        }

        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }

        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public static MetadataBuilder builder() { return new MetadataBuilder(); }

        public static class MetadataBuilder {
            private String traceId;
            private String correlationId;
            private String version;

            public MetadataBuilder traceId(String traceId) { this.traceId = traceId; return this; }
            public MetadataBuilder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
            public MetadataBuilder version(String version) { this.version = version; return this; }

            public Metadata build() {
                return new Metadata(traceId, correlationId, version);
            }
        }
    }
}
