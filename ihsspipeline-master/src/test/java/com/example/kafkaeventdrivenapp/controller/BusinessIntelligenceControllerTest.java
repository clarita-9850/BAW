package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.config.JobProcessingProperties;
import com.example.kafkaeventdrivenapp.model.BIReportRequest;
import com.example.kafkaeventdrivenapp.model.JobStatus;
import com.example.kafkaeventdrivenapp.model.ReportResult;
import com.example.kafkaeventdrivenapp.service.JobQueueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BusinessIntelligenceControllerTest {

    private MockMvc mockMvc;
    private final StubJobQueueService jobQueueService = new StubJobQueueService();
    private final StubJobProcessingProperties jobProcessingProperties = new StubJobProcessingProperties();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        BusinessIntelligenceController controller = new BusinessIntelligenceController();
        ReflectionTestUtils.setField(controller, "jobQueueService", jobQueueService);
        ReflectionTestUtils.setField(controller, "jobProcessingProperties", jobProcessingProperties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void generateBIReportReturnsJobId() throws Exception {
        String jwtToken = createMockJwtToken("ADMIN", null);
        
        Map<String, Object> request = Map.of(
                "userRole", "ADMIN",
                "reportType", "TIMESHEET_REPORT",
                "targetSystem", "BUSINESS_OBJECTS",
                "dataFormat", "JSON"
        );

        mockMvc.perform(post("/api/bi/reports/generate")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.message").value("Report job queued successfully"));

        assertNotNull(jobQueueService.getLastQueuedRequest());
        assertEquals("ADMIN", jobQueueService.getLastQueuedRequest().getUserRole());
        assertEquals("TIMESHEET_REPORT", jobQueueService.getLastQueuedRequest().getReportType());
    }

    @Test
    void generateBIReportWithDateRange() throws Exception {
        String jwtToken = createMockJwtToken("SUPERVISOR", "Alameda");
        
        Map<String, Object> request = Map.of(
                "userRole", "SUPERVISOR",
                "reportType", "ANALYTICS_REPORT",
                "targetSystem", "CRYSTAL_REPORTS",
                "dataFormat", "CSV",
                "startDate", "2024-01-01",
                "endDate", "2024-01-31",
                "countyId", "Alameda"
        );

        mockMvc.perform(post("/api/bi/reports/generate")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        BIReportRequest captured = jobQueueService.getLastQueuedRequest();
        assertEquals(LocalDate.parse("2024-01-01"), captured.getStartDate());
        assertEquals(LocalDate.parse("2024-01-31"), captured.getEndDate());
        assertEquals("Alameda", captured.getCountyId());
    }

    @Test
    void getJobStatusReturnsJobDetails() throws Exception {
        JobStatus jobStatus = new JobStatus("JOB_123", "PROCESSING", 50);
        jobStatus.setUserRole("CENTRAL_WORKER");
        jobStatus.setReportType("TIMESHEET_REPORT");
        jobQueueService.setJobStatus("JOB_123", jobStatus);

        mockMvc.perform(get("/api/bi/jobs/JOB_123/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.jobId").value("JOB_123"))
                .andExpect(jsonPath("$.jobStatus.status").value("PROCESSING"))
                .andExpect(jsonPath("$.jobStatus.progress").value(50));
    }

    @Test
    void getJobStatusReturnsNotFoundForInvalidJob() throws Exception {
        jobQueueService.setJobStatus("INVALID", null);

        mockMvc.perform(get("/api/bi/jobs/INVALID/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getJobResultReturnsResultWhenCompleted() throws Exception {
        JobStatus completedStatus = new JobStatus("JOB_456", "COMPLETED", 100);
        jobQueueService.setJobStatus("JOB_456", completedStatus);

        ReportResult result = new ReportResult();
        result.setJobId("JOB_456");
        result.setStatus("SUCCESS");
        jobQueueService.setJobResult("JOB_456", result);

        mockMvc.perform(get("/api/bi/jobs/JOB_456/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.result.jobId").value("JOB_456"));
    }

    @Test
    void getJobResultReturnsBadRequestWhenNotCompleted() throws Exception {
        JobStatus processingStatus = new JobStatus("JOB_789", "PROCESSING", 30);
        jobQueueService.setJobStatus("JOB_789", processingStatus);

        mockMvc.perform(get("/api/bi/jobs/JOB_789/result"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("Job not completed yet")));
    }

    @Test
    void getAllJobsStatusReturnsList() throws Exception {
        JobStatus job1 = new JobStatus("JOB_1", "COMPLETED", 100);
        JobStatus job2 = new JobStatus("JOB_2", "PROCESSING", 50);
        jobQueueService.addJob(job1);
        jobQueueService.addJob(job2);

        mockMvc.perform(get("/api/bi/jobs/status/ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.jobs").isArray())
                .andExpect(jsonPath("$.jobs.length()").value(2));
    }

    @Test
    void generateBIReportHandlesServiceException() throws Exception {
        String jwtToken = createMockJwtToken("ADMIN", null);
        jobQueueService.setShouldThrowException(true);

        Map<String, Object> request = Map.of(
                "userRole", "ADMIN",
                "reportType", "TIMESHEET_REPORT",
                "targetSystem", "BUSINESS_OBJECTS"
        );

        mockMvc.perform(post("/api/bi/reports/generate")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("Failed to create report job")));
    }

    private static final class StubJobQueueService extends JobQueueService {
        private BIReportRequest lastQueuedRequest;
        private java.util.Map<String, JobStatus> jobStatuses = new java.util.HashMap<>();
        private java.util.Map<String, ReportResult> jobResults = new java.util.HashMap<>();
        private java.util.List<JobStatus> allJobs = new java.util.ArrayList<>();
        private boolean shouldThrowException = false;

        BIReportRequest getLastQueuedRequest() {
            return lastQueuedRequest;
        }

        void setJobStatus(String jobId, JobStatus status) {
            if (status != null) {
                jobStatuses.put(jobId, status);
            } else {
                jobStatuses.remove(jobId);
            }
        }

        void setJobResult(String jobId, ReportResult result) {
            jobResults.put(jobId, result);
        }

        void addJob(JobStatus job) {
            allJobs.add(job);
        }

        void setShouldThrowException(boolean shouldThrow) {
            this.shouldThrowException = shouldThrow;
        }

        @Override
        public String queueReportJob(BIReportRequest request) {
            if (shouldThrowException) {
                throw new RuntimeException("Service error");
            }
            this.lastQueuedRequest = request;
            return "JOB_" + System.currentTimeMillis();
        }

        @Override
        public String queueReportJob(BIReportRequest request, String jwtToken) {
            if (shouldThrowException) {
                throw new RuntimeException("Service error");
            }
            this.lastQueuedRequest = request;
            return "JOB_" + System.currentTimeMillis();
        }

        @Override
        public JobStatus getJobStatus(String jobId) {
            return jobStatuses.get(jobId);
        }

        @Override
        public ReportResult getJobResult(String jobId) {
            return jobResults.get(jobId);
        }

        @Override
        public java.util.List<JobStatus> getAllJobs() {
            return allJobs;
        }
    }

    /**
     * Helper method to create a mock JWT token for testing
     */
    private String createMockJwtToken(String role, String countyId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("preferred_username", "testuser");
            Map<String, Object> realmAccess = new HashMap<>();
            realmAccess.put("roles", List.of(role));
            payload.put("realm_access", realmAccess);
            if (countyId != null) {
                payload.put("countyId", countyId);
            }
            
            String payloadJson = objectMapper.writeValueAsString(payload);
            String encodedPayload = java.util.Base64.getUrlEncoder().encodeToString(payloadJson.getBytes());
            return "header." + encodedPayload + ".signature";
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock JWT token", e);
        }
    }

    private static final class StubJobProcessingProperties extends JobProcessingProperties {
        StubJobProcessingProperties() {
            super();
        }

        @Override
        public int normalizeChunkSize(Integer requestedChunkSize) {
            return requestedChunkSize != null ? requestedChunkSize : 500;
        }
    }
}

