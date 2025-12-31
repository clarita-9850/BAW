package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.entity.TimesheetEntity;
import com.example.kafkaeventdrivenapp.model.Event;
import com.example.kafkaeventdrivenapp.model.TimesheetApproval;
import com.example.kafkaeventdrivenapp.service.EventService;
import com.example.kafkaeventdrivenapp.service.TimesheetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecipientControllerTest {

    private MockMvc mockMvc;
    private final CapturingTimesheetService timesheetService = new CapturingTimesheetService();
    private final CapturingEventService eventService = new CapturingEventService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RecipientController controller = new RecipientController();
        ReflectionTestUtils.setField(controller, "timesheetService", timesheetService);
        ReflectionTestUtils.setField(controller, "eventService", eventService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void approveTimesheetUpdatesStatusAndPublishesEvent() throws Exception {
        TimesheetApproval approval = new TimesheetApproval();
        approval.setTimesheetId("123");
        approval.setRecipientId("recipient-1");

        mockMvc.perform(post("/api/recipient/approve-timesheet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approval)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Timesheet approved successfully: 123")));

        assertEquals("123", timesheetService.getLastUpdatedTimesheetId());
        assertEquals("APPROVED", timesheetService.getLastStatus());
        assertEquals("timesheet-approvals", eventService.getLastTopic());
        assertNotNull(eventService.getLastEvent());
    }

    @Test
    void rejectTimesheetUpdatesStatusWithComments() throws Exception {
        TimesheetApproval approval = new TimesheetApproval();
        approval.setTimesheetId("456");
        approval.setRecipientId("recipient-1");
        approval.setComments("Missing required information");

        mockMvc.perform(post("/api/recipient/reject-timesheet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approval)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Timesheet rejected: 456")));

        assertEquals("456", timesheetService.getLastUpdatedTimesheetId());
        assertEquals("REJECTED", timesheetService.getLastStatus());
        assertEquals("Missing required information", timesheetService.getLastRejectionReason());
    }

    @Test
    void requestRevisionUpdatesStatusAndPublishesEvent() throws Exception {
        TimesheetApproval approval = new TimesheetApproval();
        approval.setTimesheetId("789");
        approval.setRecipientId("recipient-1");
        approval.setComments("Please add more details");

        mockMvc.perform(post("/api/recipient/request-revision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approval)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Revision requested for timesheet: 789")));

        assertEquals("789", timesheetService.getLastUpdatedTimesheetId());
        assertEquals("REVISION_REQUESTED", timesheetService.getLastStatus());
    }

    @Test
    void sendReminderPublishesReminderEvent() throws Exception {
        mockMvc.perform(post("/api/recipient/send-reminder/999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reminder sent for timesheet: 999")));

        assertEquals("timesheet-reminders", eventService.getLastTopic());
        assertNotNull(eventService.getLastEvent());
    }

    @Test
    void getPendingTimesheetsReturnsMockResponse() throws Exception {
        mockMvc.perform(get("/api/recipient/pending-timesheets"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No pending timesheets found")));
    }

    @Test
    void approveTimesheetHandlesServiceException() throws Exception {
        timesheetService.setShouldThrowException(true);

        TimesheetApproval approval = new TimesheetApproval();
        approval.setTimesheetId("ts-error");
        approval.setRecipientId("recipient-1");

        mockMvc.perform(post("/api/recipient/approve-timesheet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approval)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Failed to approve timesheet")));
    }

    private static final class CapturingTimesheetService extends TimesheetService {
        private String lastUpdatedTimesheetId;
        private String lastStatus;
        private String lastRejectionReason;
        private boolean shouldThrowException = false;

        String getLastUpdatedTimesheetId() {
            return lastUpdatedTimesheetId;
        }

        String getLastStatus() {
            return lastStatus;
        }

        String getLastRejectionReason() {
            return lastRejectionReason;
        }

        void setShouldThrowException(boolean shouldThrow) {
            this.shouldThrowException = shouldThrow;
        }

        @Override
        public TimesheetEntity updateTimesheetStatus(Long timesheetId, String status) {
            if (shouldThrowException) {
                throw new RuntimeException("Service error");
            }
            this.lastUpdatedTimesheetId = timesheetId != null ? timesheetId.toString() : null;
            this.lastStatus = status;
            return null;
        }

        @Override
        public TimesheetEntity updateRejectionReason(Long timesheetId, String rejectionReason) {
            this.lastRejectionReason = rejectionReason;
            return null;
        }
    }

    private static final class CapturingEventService extends EventService {
        private String lastTopic;
        private Event lastEvent;

        String getLastTopic() {
            return lastTopic;
        }

        Event getLastEvent() {
            return lastEvent;
        }

        @Override
        public void publishEvent(String topic, Event event) {
            this.lastTopic = topic;
            this.lastEvent = event;
        }
    }
}

