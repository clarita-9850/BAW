package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.ReportGenerationRequest;
import com.example.kafkaeventdrivenapp.model.ReportGenerationResponse;
import com.example.kafkaeventdrivenapp.service.EmailReportService;
import com.example.kafkaeventdrivenapp.service.PDFReportGeneratorService;
import com.example.kafkaeventdrivenapp.service.ReportGenerationService;
import com.example.kafkaeventdrivenapp.service.ScheduledReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportDeliveryControllerTest {

    private MockMvc mockMvc;
    private final StubReportGenerationService reportGenerationService = new StubReportGenerationService();
    private final StubPDFReportGeneratorService pdfReportGeneratorService = new StubPDFReportGeneratorService();
    private final StubEmailReportService emailReportService = new StubEmailReportService();
    private final StubScheduledReportService scheduledReportService = new StubScheduledReportService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReportDeliveryController controller = new ReportDeliveryController();
        ReflectionTestUtils.setField(controller, "reportGenerationService", reportGenerationService);
        ReflectionTestUtils.setField(controller, "pdfReportGeneratorService", pdfReportGeneratorService);
        ReflectionTestUtils.setField(controller, "emailReportService", emailReportService);
        ReflectionTestUtils.setField(controller, "scheduledReportService", scheduledReportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void generateAndEmailReportReturnsSuccess() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "CENTRAL_WORKER",
                "reportType", "TIMESHEET_REPORT",
                "startDate", "2024-01-01",
                "endDate", "2024-01-31",
                "recipients", List.of("test@example.com")
        );

        ReportGenerationResponse reportResponse = new ReportGenerationResponse();
        reportResponse.setStatus("SUCCESS");
        reportResponse.setReportId("report-123");
        com.example.kafkaeventdrivenapp.model.ReportData data = new com.example.kafkaeventdrivenapp.model.ReportData();
        data.setRecords(new ArrayList<>());
        reportResponse.setData(data);
        reportGenerationService.setResponse(reportResponse);
        emailReportService.setEmailSuccess(true);

        mockMvc.perform(post("/api/reports/email")
                        .header("Authorization", "Bearer jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.emailSent").value(true))
                .andExpect(jsonPath("$.reportId").value("report-123"));
    }

    @Test
    void generateAndEmailReportReturnsErrorWhenJwtMissing() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "CENTRAL_WORKER"
        );

        mockMvc.perform(post("/api/reports/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("JWT token is required")));
    }

    @Test
    void generateAndEmailReportReturnsErrorWhenUserRoleMissing() throws Exception {
        Map<String, Object> request = Map.of();

        mockMvc.perform(post("/api/reports/email")
                        .header("Authorization", "Bearer jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("userRole is required")));
    }

    @Test
    void generatePDFReportReturnsSuccess() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "CENTRAL_WORKER",
                "reportType", "TIMESHEET_REPORT",
                "startDate", "2024-01-01",
                "endDate", "2024-01-31"
        );

        ReportGenerationResponse reportResponse = new ReportGenerationResponse();
        reportResponse.setStatus("SUCCESS");
        com.example.kafkaeventdrivenapp.model.ReportData data = new com.example.kafkaeventdrivenapp.model.ReportData();
        data.setRecords(new ArrayList<>());
        reportResponse.setData(data);
        reportGenerationService.setResponse(reportResponse);

        ByteArrayResource pdfResource = new ByteArrayResource("pdf-content".getBytes());
        pdfReportGeneratorService.setPdfResource(pdfResource);

        mockMvc.perform(post("/api/reports/generate-pdf")
                        .header("Authorization", "Bearer jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));
    }

    @Test
    void getScheduledReportStatusReturnsSuccess() throws Exception {
        scheduledReportService.setServiceStatus("ACTIVE");

        mockMvc.perform(get("/api/reports/scheduled/status")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.serviceStatus").value("ACTIVE"));
    }

    @Test
    void triggerScheduledReportReturnsSuccess() throws Exception {
        scheduledReportService.setTriggerSuccess(true);
        Map<String, Object> request = Map.of(
                "userRole", "CENTRAL_WORKER",
                "reportType", "TIMESHEET_REPORT"
        );

        mockMvc.perform(post("/api/reports/scheduled/trigger")
                        .header("Authorization", "Bearer jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void triggerScheduledReportReturnsErrorWhenJwtMissing() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "CENTRAL_WORKER",
                "reportType", "TIMESHEET_REPORT"
        );

        mockMvc.perform(post("/api/reports/scheduled/trigger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("JWT token is required")));
    }

    @Test
    void triggerScheduledReportReturnsErrorWhenJwtEmpty() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "CENTRAL_WORKER",
                "reportType", "TIMESHEET_REPORT"
        );

        mockMvc.perform(post("/api/reports/scheduled/trigger")
                        .header("Authorization", "Bearer ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("JWT token is required")));
    }

    @Test
    void generatePDFReportReturnsErrorWhenJwtMissing() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "CENTRAL_WORKER",
                "reportType", "TIMESHEET_REPORT"
        );

        ReportGenerationResponse reportResponse = new ReportGenerationResponse();
        reportResponse.setStatus("SUCCESS");
        com.example.kafkaeventdrivenapp.model.ReportData data = new com.example.kafkaeventdrivenapp.model.ReportData();
        data.setRecords(new ArrayList<>());
        reportResponse.setData(data);
        reportGenerationService.setResponse(reportResponse);

        mockMvc.perform(post("/api/reports/generate-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("JWT token is required")));
    }

    @Test
    void generatePDFReportReturnsErrorWhenJwtEmpty() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "CENTRAL_WORKER",
                "reportType", "TIMESHEET_REPORT"
        );

        ReportGenerationResponse reportResponse = new ReportGenerationResponse();
        reportResponse.setStatus("SUCCESS");
        com.example.kafkaeventdrivenapp.model.ReportData data = new com.example.kafkaeventdrivenapp.model.ReportData();
        data.setRecords(new ArrayList<>());
        reportResponse.setData(data);
        reportGenerationService.setResponse(reportResponse);

        mockMvc.perform(post("/api/reports/generate-pdf")
                        .header("Authorization", "Bearer ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("JWT token is required")));
    }

    @Test
    void generateAndEmailReportReturnsErrorWhenJwtEmpty() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "CENTRAL_WORKER",
                "reportType", "TIMESHEET_REPORT"
        );

        mockMvc.perform(post("/api/reports/email")
                        .header("Authorization", "Bearer ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("JWT token is required")));
    }

    // Stub Services
    private static final class StubReportGenerationService extends ReportGenerationService {
        private ReportGenerationResponse response;

        StubReportGenerationService() {
            super();
        }

        void setResponse(ReportGenerationResponse response) {
            this.response = response;
        }

        @Override
        public ReportGenerationResponse generateReport(ReportGenerationRequest request, String jwtToken) {
            return response != null ? response : createSuccessResponse();
        }

        private ReportGenerationResponse createSuccessResponse() {
            ReportGenerationResponse resp = new ReportGenerationResponse();
            resp.setStatus("SUCCESS");
            resp.setReportId("default-report-id");
            resp.setTotalRecords(0);
            com.example.kafkaeventdrivenapp.model.ReportData data = new com.example.kafkaeventdrivenapp.model.ReportData();
            data.setRecords(new ArrayList<>());
            resp.setData(data);
            return resp;
        }
    }

    private static final class StubPDFReportGeneratorService extends PDFReportGeneratorService {
        private ByteArrayResource pdfResource;

        StubPDFReportGeneratorService() {
            super();
        }

        void setPdfResource(ByteArrayResource pdfResource) {
            this.pdfResource = pdfResource;
        }

        @Override
        public byte[] generatePDFReport(String reportType, String userRole, List<Map<String, Object>> reportData,
                                        Map<String, Object> additionalData, String jwtToken) {
            ByteArrayResource resource = pdfResource != null ? pdfResource : new ByteArrayResource("default-pdf".getBytes());
            return resource.getByteArray();
        }
    }

    private static final class StubEmailReportService extends EmailReportService {
        private boolean emailSuccess;

        StubEmailReportService() {
            super();
        }

        void setEmailSuccess(boolean emailSuccess) {
            this.emailSuccess = emailSuccess;
        }

        public boolean sendReportEmail(String reportType, String userRole, List<Map<String, Object>> reportData,
                                      Map<String, Object> additionalData, List<String> recipients, String jwtToken) {
            return emailSuccess;
        }

        public boolean sendScheduledReportEmail(String reportType, String userRole, List<Map<String, Object>> reportData,
                                              Map<String, Object> additionalData, String jwtToken) {
            return emailSuccess;
        }
    }

    private static final class StubScheduledReportService extends ScheduledReportService {
        private boolean triggerSuccess;
        private String serviceStatus = "INACTIVE";

        StubScheduledReportService() {
            super();
        }

        void setTriggerSuccess(boolean triggerSuccess) {
            this.triggerSuccess = triggerSuccess;
        }

        void setServiceStatus(String status) {
            this.serviceStatus = status;
        }

        @Override
        public String getScheduledReportServiceStatus() {
            return serviceStatus;
        }

        @Override
        public boolean triggerScheduledReport(String userRole, String reportType, String jwtToken) {
            return triggerSuccess;
        }
    }
}

