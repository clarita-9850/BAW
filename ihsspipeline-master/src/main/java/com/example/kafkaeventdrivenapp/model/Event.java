package com.example.kafkaeventdrivenapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String eventId;
    private String eventType;
    private String source;
    private Object data;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    public Event(String eventType, String source, Object data) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventType = eventType;
        this.source = source;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}
