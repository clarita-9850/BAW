package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.entity.TimesheetEntity;
import com.example.kafkaeventdrivenapp.model.Timesheet;
import com.example.kafkaeventdrivenapp.service.EventService;
import com.example.kafkaeventdrivenapp.service.TimesheetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProviderControllerTest {

    private MockMvc mockMvc;

    private final CapturingTimesheetService timesheetService = new CapturingTimesheetService();
    private final CapturingEventService eventService = new CapturingEventService();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ProviderController controller = new ProviderController();
        ReflectionTestUtils.setField(controller, "timesheetService", timesheetService);
        ReflectionTestUtils.setField(controller, "eventService", eventService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void submitTimesheetPersistsAndPublishesEvent() throws Exception {
        Map<String, Object> request = Map.of(
                "timesheetId", "ts-123",
                "providerId", "provider-abc",
                "totalHours", 7.5
        );

        mockMvc.perform(post("/api/provider/submit-timesheet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Timesheet submitted successfully: ts-123")));

        Timesheet captured = timesheetService.getLastSaved();
        assertEquals("ts-123", captured.getTimesheetId());
        assertEquals("SUBMITTED", captured.getStatus());

        assertEquals("TIMESHEET_SUBMITTED", eventService.getLastTopic());
    }

    private static final class CapturingTimesheetService extends TimesheetService {
        private Timesheet lastSaved;

        Timesheet getLastSaved() {
            return lastSaved;
        }

        @Override
        public TimesheetEntity saveTimesheet(Timesheet timesheet) {
            this.lastSaved = timesheet;
            return null;
        }

        @Override
        public TimesheetEntity updateTimesheetStatus(Long timesheetId, String status) {
            return null;
        }
    }

    private static final class CapturingEventService extends EventService {
        private String lastTopic;

        String getLastTopic() {
            return lastTopic;
        }

        @Override
        public void publishEvent(String topic, Object event) {
            this.lastTopic = topic;
        }
    }
}

