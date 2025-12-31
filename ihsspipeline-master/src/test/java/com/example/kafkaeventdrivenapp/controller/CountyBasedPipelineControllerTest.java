package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.PipelineExtractionRequest;
import com.example.kafkaeventdrivenapp.model.PipelineExtractionResponse;
import com.example.kafkaeventdrivenapp.model.ReportGenerationResponse;
import com.example.kafkaeventdrivenapp.service.CountyBasedDataExtractionService;
import com.example.kafkaeventdrivenapp.service.FieldMaskingService;
import com.example.kafkaeventdrivenapp.service.ReportGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CountyBasedPipelineControllerTest {

    private MockMvc mockMvc;
    private final StubCountyBasedDataExtractionService extractionService = new StubCountyBasedDataExtractionService();
    private final StubFieldMaskingService fieldMaskingService = new StubFieldMaskingService();
    private final StubReportGenerationService reportGenerationService = new StubReportGenerationService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        CountyBasedPipelineController controller = new CountyBasedPipelineController();
        ReflectionTestUtils.setField(controller, "countyBasedDataExtractionService", extractionService);
        ReflectionTestUtils.setField(controller, "fieldMaskingService", fieldMaskingService);
        ReflectionTestUtils.setField(controller, "reportGenerationService", reportGenerationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void extractDataByCountyReturnsSuccess() throws Exception {
        PipelineExtractionRequest request = new PipelineExtractionRequest();
        request.setUserRole("COUNTY_WORKER");
        request.setUserCounty("COUNTY_A");
        request.setReportType("TIMESHEET_REPORT");

        PipelineExtractionResponse response = createExtractionResponse();
        response.setTotalRecords(10);
        response.setMaskedRecords(10);
        response.setData(new ArrayList<>());
        extractionService.setResponse(response);

        mockMvc.perform(post("/api/county-pipeline/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.extractionMethod").value("COUNTY_FIRST"))
                .andExpect(jsonPath("$.totalRecords").exists());

        assertNotNull(extractionService.getLastRequest());
        assertEquals("COUNTY_A", extractionService.getLastRequest().getUserCounty());
    }

    @Test
    void extractDataByCountyReturnsErrorWhenCountyMissing() throws Exception {
        PipelineExtractionRequest request = new PipelineExtractionRequest();
        request.setUserRole("COUNTY_WORKER");
        request.setUserCounty(null);
        request.setReportType("TIMESHEET_REPORT");

        mockMvc.perform(post("/api/county-pipeline/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("County information is required")));
    }

    @Test
    void generateReportByCountyReturnsSuccess() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "COUNTY_WORKER",
                "userCounty", "COUNTY_A",
                "dateRange", Map.of(
                        "startDate", "2024-01-01",
                        "endDate", "2024-01-31"
                )
        );

        PipelineExtractionResponse extractionResponse = createExtractionResponse();
        extractionService.setResponse(extractionResponse);

        ReportGenerationResponse reportResponse = new ReportGenerationResponse();
        reportResponse.setStatus("SUCCESS");
        reportResponse.setTotalRecords(5);
        reportResponse.setReportId("county-report-1");
        com.example.kafkaeventdrivenapp.model.ReportData data = new com.example.kafkaeventdrivenapp.model.ReportData();
        data.setRecords(new ArrayList<>());
        reportResponse.setData(data);
        reportGenerationService.setResponse(reportResponse);

        mockMvc.perform(post("/api/county-pipeline/generate-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.extractionMethod").value("COUNTY_FIRST"));
    }

    @Test
    void generateReportByCountyReturnsErrorWhenCountyMissing() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "COUNTY_WORKER"
        );

        mockMvc.perform(post("/api/county-pipeline/generate-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("userCounty is required")));
    }

    @Test
    void generateReportByCountyAllowsCentralWorkerWithoutCounty() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "ADMIN"
        );

        PipelineExtractionResponse extractionResponse = createExtractionResponse();
        extractionService.setResponse(extractionResponse);

        ReportGenerationResponse reportResponse = new ReportGenerationResponse();
        reportResponse.setStatus("SUCCESS");
        reportResponse.setReportId("county-report-2");
        com.example.kafkaeventdrivenapp.model.ReportData data = new com.example.kafkaeventdrivenapp.model.ReportData();
        data.setRecords(new ArrayList<>());
        reportResponse.setData(data);
        reportGenerationService.setResponse(reportResponse);

        mockMvc.perform(post("/api/county-pipeline/generate-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void extractDataByCountyAllowsCentralWorkerWithoutCounty() throws Exception {
        PipelineExtractionRequest request = new PipelineExtractionRequest();
        request.setUserRole("ADMIN");
        request.setUserCounty(null);
        request.setReportType("TIMESHEET_REPORT");

        PipelineExtractionResponse response = createExtractionResponse();
        response.setTotalRecords(10);
        response.setMaskedRecords(10);
        response.setData(new ArrayList<>());
        extractionService.setResponse(response);

        mockMvc.perform(post("/api/county-pipeline/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void extractDataByCountyReturnsErrorWhenDistrictWorkerMissingCounty() throws Exception {
        PipelineExtractionRequest request = new PipelineExtractionRequest();
        request.setUserRole("DISTRICT_WORKER");
        request.setUserCounty(null);
        request.setReportType("TIMESHEET_REPORT");

        mockMvc.perform(post("/api/county-pipeline/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("County information is required")));
    }

    @Test
    void generateReportByCountyReturnsErrorWhenDistrictWorkerMissingCounty() throws Exception {
        Map<String, Object> request = Map.of(
                "userRole", "DISTRICT_WORKER"
        );

        mockMvc.perform(post("/api/county-pipeline/generate-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("userCounty is required")));
    }

    @Test
    void extractDataByCountyReturnsErrorWhenProviderMissingCounty() throws Exception {
        PipelineExtractionRequest request = new PipelineExtractionRequest();
        request.setUserRole("PROVIDER");
        request.setUserCounty(null);
        request.setReportType("TIMESHEET_REPORT");

        mockMvc.perform(post("/api/county-pipeline/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("County information is required")));
    }

    private static final class StubCountyBasedDataExtractionService extends CountyBasedDataExtractionService {
        private PipelineExtractionRequest lastRequest;
        private PipelineExtractionResponse response;

        StubCountyBasedDataExtractionService() {
            super();
        }

        void setResponse(PipelineExtractionResponse response) {
            this.response = response;
        }

        PipelineExtractionRequest getLastRequest() {
            return lastRequest;
        }

        @Override
        public PipelineExtractionResponse extractDataByCounty(PipelineExtractionRequest request) {
            this.lastRequest = request;
            return response != null ? response : createSuccessResponse();
        }

        private PipelineExtractionResponse createSuccessResponse() {
            PipelineExtractionResponse resp = new PipelineExtractionResponse();
            resp.setStatus("SUCCESS");
            resp.setTotalRecords(0);
            resp.setMaskedRecords(0);
            resp.setData(new ArrayList<>());
            resp.setSummary(createSummary());
            return resp;
        }
    }

    private static final class StubFieldMaskingService extends FieldMaskingService {
        StubFieldMaskingService() {
            super(new StubPersistentFieldMaskingService(), new StubKeycloakFieldMaskingService());
        }
    }

    private static final class StubReportGenerationService extends ReportGenerationService {
        private ReportGenerationResponse response;

        StubReportGenerationService() {
            super();
        }

        void setResponse(ReportGenerationResponse response) {
            this.response = response;
        }

        @Override
        public ReportGenerationResponse generateReport(com.example.kafkaeventdrivenapp.model.ReportGenerationRequest request, String jwtToken) {
            return response != null ? response : createSuccessResponse();
        }

        private ReportGenerationResponse createSuccessResponse() {
            ReportGenerationResponse resp = new ReportGenerationResponse();
            resp.setStatus("SUCCESS");
            resp.setTotalRecords(0);
            resp.setReportId("stub-county-report");
            com.example.kafkaeventdrivenapp.model.ReportData data = new com.example.kafkaeventdrivenapp.model.ReportData();
            data.setRecords(new ArrayList<>());
            resp.setData(data);
            return resp;
        }
    }

    private static PipelineExtractionResponse createExtractionResponse() {
        PipelineExtractionResponse response = new PipelineExtractionResponse();
        response.setStatus("SUCCESS");
        response.setTotalRecords(5);
        response.setMaskedRecords(5);
        response.setData(new ArrayList<>());
        response.setSummary(createSummary());
        return response;
    }

    private static com.example.kafkaeventdrivenapp.model.ExtractionSummary createSummary() {
        com.example.kafkaeventdrivenapp.model.ExtractionSummary summary = new com.example.kafkaeventdrivenapp.model.ExtractionSummary();
        summary.setFieldVisibility(new HashMap<>());
        summary.setStatusDistribution(new HashMap<>());
        summary.setProviderDistribution(new HashMap<>());
        summary.setCountyDistribution(new HashMap<>());
        return summary;
    }

    private static final class StubPersistentFieldMaskingService extends com.example.kafkaeventdrivenapp.service.PersistentFieldMaskingService {
        StubPersistentFieldMaskingService() {
            super();
        }
    }

    private static final class StubKeycloakFieldMaskingService extends com.example.kafkaeventdrivenapp.service.KeycloakFieldMaskingService {
        StubKeycloakFieldMaskingService() {
            super();
        }
    }
}

