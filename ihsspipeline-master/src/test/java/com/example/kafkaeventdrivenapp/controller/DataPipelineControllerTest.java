package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.*;
import com.example.kafkaeventdrivenapp.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DataPipelineControllerTest {

    private MockMvc mockMvc;
    private final StubReportGenerationService reportGenerationService = new StubReportGenerationService();
    private final StubRulesEngineService rulesEngineService = new StubRulesEngineService();
    private final StubQueryBuilderService queryBuilderService = new StubQueryBuilderService();
    private final StubDataFetchingService dataFetchingService = new StubDataFetchingService();
    private final StubFieldMaskingService fieldMaskingService = new StubFieldMaskingService();
    private final StubEventService eventService = new StubEventService();
    private final StubFieldVisibilityService fieldVisibilityService = new StubFieldVisibilityService();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        DataPipelineController controller = new DataPipelineController();
        ReflectionTestUtils.setField(controller, "reportGenerationService", reportGenerationService);
        ReflectionTestUtils.setField(controller, "rulesEngineService", rulesEngineService);
        ReflectionTestUtils.setField(controller, "queryBuilderService", queryBuilderService);
        ReflectionTestUtils.setField(controller, "dataFetchingService", dataFetchingService);
        ReflectionTestUtils.setField(controller, "fieldMaskingService", fieldMaskingService);
        ReflectionTestUtils.setField(controller, "eventService", eventService);
        ReflectionTestUtils.setField(controller, "fieldVisibilityService", fieldVisibilityService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void extractDataEnhancedReturnsSuccess() throws Exception {
        PipelineExtractionRequest request = new PipelineExtractionRequest();
        request.setUserRole("CENTRAL_WORKER");
        request.setReportType("TIMESHEET_REPORT");
        PipelineExtractionRequest.DateRange dateRange = new PipelineExtractionRequest.DateRange(
                LocalDate.parse("2024-01-01"),
                LocalDate.parse("2024-01-31")
        );
        request.setDateRange(dateRange);

        DataFetchingService.DataFetchResult fetchResult = new DataFetchingService.DataFetchResult(
                true, "Success", new ArrayList<>(), 10, 10L
        );
        dataFetchingService.setFetchResult(fetchResult);

        List<MaskedTimesheetData> maskedData = new ArrayList<>();
        fieldMaskingService.setMaskedData(maskedData);

        mockMvc.perform(post("/api/pipeline/extract-enhanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.totalRecords").exists())
                .andExpect(jsonPath("$.maskedRecords").exists());

        assertNotNull(queryBuilderService.getLastQueryParams());
        assertEquals("CENTRAL_WORKER", queryBuilderService.getLastQueryParams().getUserRole());
        assertEquals("ENHANCED_PIPELINE_COMPLETED", eventService.getLastTopic());
    }

    @Test
    void getMaskingRulesForRole() throws Exception {
        FieldMaskingRules rules = new FieldMaskingRules();
        rules.setUserRole("COUNTY_WORKER");
        rules.setReportType("TIMESHEET_REPORT");
        rules.setRules(new ArrayList<>());
        fieldMaskingService.setRules(rules);

        String jwtToken = createMockJwtToken("CASE_WORKER", null);
        mockMvc.perform(get("/api/pipeline/masking-rules/COUNTY_WORKER")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.userRole").value("COUNTY_WORKER"));
    }

    @Test
    void getUserRolesReturnsList() throws Exception {
        mockMvc.perform(get("/api/pipeline/user-roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void getCountiesReturnsList() throws Exception {
        mockMvc.perform(get("/api/pipeline/counties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.counties").isArray());
    }

    @Test
    void getReportTypesReturnsList() throws Exception {
        mockMvc.perform(get("/api/pipeline/report-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.reportTypes").isArray());
    }

    @Test
    void generateReportReturnsSuccess() throws Exception {
        // Create a mock JWT token with ADMIN role
        String jwtPayload = "{\"preferred_username\":\"admin\",\"realm_access\":{\"roles\":[\"ADMIN\"]}}";
        String jwtToken = "header." + java.util.Base64.getUrlEncoder().encodeToString(jwtPayload.getBytes()) + ".signature";
        
        Map<String, Object> request = Map.of(
                "userRole", "ADMIN",
                "reportType", "TIMESHEET_REPORT",
                "startDate", "2024-01-01",
                "endDate", "2024-01-31"
        );

        ReportGenerationResponse response = new ReportGenerationResponse();
        response.setStatus("SUCCESS");
        response.setTotalRecords(5);
        response.setReportId("report-123");
        reportGenerationService.setResponse(response);

        mockMvc.perform(post("/api/pipeline/generate-report")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.reportId").exists());
    }

    @Test
    void generateReportReturnsBadRequestWhenJwtMissing() throws Exception {
        // No JWT token provided
        
        Map<String, Object> request = Map.of(
                "userRole", "ADMIN",
                "reportType", "TIMESHEET_REPORT"
        );

        mockMvc.perform(post("/api/pipeline/generate-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())  // Controller returns 400 when userInfo is null
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Unable to resolve user role from JWT token"));
    }

    @Test
    void getFieldVisibilityForRole() throws Exception {
        List<String> visibleFields = List.of("timesheetId", "totalHours", "status");
        fieldVisibilityService.setVisibleFields(visibleFields);

        String jwtToken = createMockJwtToken("CASE_WORKER", null);
        mockMvc.perform(get("/api/pipeline/field-visibility/COUNTY_WORKER")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.visibleFields").isArray())
                .andExpect(jsonPath("$.data.totalVisibleFields").value(visibleFields.size()));
    }

    @Test
    void getAvailableFieldsReturnsList() throws Exception {
        mockMvc.perform(get("/api/pipeline/available-fields"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.availableFields").isArray())
                .andExpect(jsonPath("$.totalFields").value(5));
    }

    @Test
    void compareRolesReturnsAggregatedSummary() throws Exception {
        // Create a mock JWT token with ADMIN role
        String jwtToken = createMockJwtToken("ADMIN", null);
        fieldVisibilityService.setVisibleFields(List.of("timesheetId", "totalHours", "status", "totalAmount", "approvedAt"));
        
        // Set up report generation service to return success
        ReportGenerationResponse adminResponse = new ReportGenerationResponse();
        adminResponse.setStatus("SUCCESS");
        adminResponse.setTotalRecords(10);
        adminResponse.setReportId("admin-report");
        reportGenerationService.setResponse(adminResponse);

        Map<String, Object> request = Map.of(
                "roles", List.of("ADMIN", "COUNTY_WORKER"),
                "startDate", "2024-01-01",
                "endDate", "2024-01-31"
        );

        mockMvc.perform(post("/api/pipeline/compare-roles")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.comparison.ADMIN.status").value("SUCCESS"))
                .andExpect(jsonPath("$.comparison.COUNTY_WORKER.fieldVisibility.totalVisibleFields").value(5));
    }

    @Test
    void compareRolesReturnsBadRequestWhenRolesMissing() throws Exception {
        Map<String, Object> request = Map.of(
                "roles", List.of()
        );

        String jwtToken = createMockJwtToken("ADMIN", null);
        mockMvc.perform(post("/api/pipeline/compare-roles")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("No roles provided for comparison"));
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
            resp.setTotalRecords(0);
            resp.setReportId("stub-report-id");
            ReportData data = new ReportData();
            data.setRecords(new ArrayList<>());
            data.setTotalCount(0);
            resp.setData(data);
            return resp;
        }
    }

    private static final class StubRulesEngineService extends RulesEngineService {
        private RulesEngineService.AccessPattern accessPattern;

        StubRulesEngineService() {
            super();
        }

        @Override
        public AccessPattern determineAccessPattern(String userRole) {
            if (accessPattern != null) {
                return accessPattern;
            }
            return new AccessPattern("FULL_ACCESS", java.util.Set.of(), java.util.Set.of(), java.util.Set.of(), "UNMASKED", "HIGH");
        }
    }

    private static final class StubQueryBuilderService extends QueryBuilderService {
        private QueryParameters lastQueryParams;

        StubQueryBuilderService() {
            super();
        }

        QueryParameters getLastQueryParams() {
            return lastQueryParams;
        }

        @Override
        public QueryParameters buildQuery(String userRole, String countyId,
                                         LocalDate startDate, LocalDate endDate, Map<String, Object> additionalFilters) {
            QueryParameters params = new QueryParameters();
            params.setUserRole(userRole);
            params.setCountyId(countyId);
            params.setStartDate(startDate);
            params.setEndDate(endDate);
            this.lastQueryParams = params;
            return params;
        }
    }

    private static final class StubDataFetchingService extends DataFetchingService {
        private DataFetchResult fetchResult;

        StubDataFetchingService() {
            super();
        }

        void setFetchResult(DataFetchResult fetchResult) {
            this.fetchResult = fetchResult;
        }

        @Override
        public DataFetchResult fetchData(QueryBuilderService.QueryParameters queryParams) {
            return fetchResult != null ? fetchResult : new DataFetchResult(true, "Success", new ArrayList<>(), 0, 0L);
        }
    }

    private static final class StubFieldMaskingService extends FieldMaskingService {
        private FieldMaskingRules rules;
        private List<MaskedTimesheetData> maskedData;

        StubFieldMaskingService() {
            super(new StubPersistentFieldMaskingService(), new StubKeycloakFieldMaskingService());
        }

        void setRules(FieldMaskingRules rules) {
            this.rules = rules;
        }

        void setMaskedData(List<MaskedTimesheetData> maskedData) {
            this.maskedData = maskedData;
        }

        @Override
        public FieldMaskingRules getMaskingRules(String userRole, String reportType, String jwtToken) {
            if (rules != null) {
                return rules;
            }
            FieldMaskingRules defaultRules = new FieldMaskingRules();
            defaultRules.setUserRole(userRole);
            defaultRules.setReportType(reportType);
            defaultRules.setRules(new ArrayList<>());
            return defaultRules;
        }

        @Override
        public List<MaskedTimesheetData> applyFieldMasking(List<com.example.kafkaeventdrivenapp.entity.TimesheetEntity> timesheets,
                                                          String userRole, String reportType) {
            return maskedData != null ? maskedData : new ArrayList<>();
        }
    }

    private static final class StubEventService extends EventService {
        private String lastTopic;
        private Object lastEvent;

        String getLastTopic() {
            return lastTopic;
        }

        @Override
        public void publishEvent(String topic, Object event) {
            this.lastTopic = topic;
            this.lastEvent = event;
        }
    }

    private static final class StubFieldVisibilityService extends FieldVisibilityService {
        private List<String> visibleFields;

        StubFieldVisibilityService() {
            super();
        }

        void setVisibleFields(List<String> visibleFields) {
            this.visibleFields = visibleFields;
        }

        @Override
        public List<String> getVisibleFields(String userRole, String jwtToken) {
            return visibleFields != null ? visibleFields : new ArrayList<>();
        }

        public Map<String, Object> getFieldVisibilitySummary(String userRole) {
            Map<String, Object> summary = new java.util.HashMap<>();
            summary.put("userRole", userRole);
            summary.put("visibleFields", visibleFields != null ? visibleFields : new ArrayList<>());
            summary.put("totalVisibleFields", visibleFields != null ? visibleFields.size() : 0);
            return summary;
        }

        public List<String> getAllAvailableFields() {
            return List.of("timesheetId", "providerName", "totalHours", "status", "totalAmount");
        }
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
}

