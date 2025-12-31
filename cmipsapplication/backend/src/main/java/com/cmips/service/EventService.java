package com.cmips.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EventService {

    private final ObjectMapper objectMapper;
    private final CopyOnWriteArrayList<String> eventLog = new CopyOnWriteArrayList<>();

    protected EventService() {
        this(new ObjectMapper());
    }

    @Autowired
    public EventService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Persist a generic payload as an event log entry.
     */
    public void publishEvent(String topic, Object event) {
        recordEvent(topic, event);
    }

    public List<String> getEventLog() {
        return List.copyOf(eventLog);
    }

    public void clearEventLog() {
        eventLog.clear();
    }

    private void recordEvent(String topic, Object payload) {
        try {
            String serialized = objectMapper.writeValueAsString(payload);
            String entry = String.format("[%s] %s -> %s",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                topic,
                serialized
            );
            eventLog.add(entry);
            System.out.println("📣 EventService: " + entry);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }
}






