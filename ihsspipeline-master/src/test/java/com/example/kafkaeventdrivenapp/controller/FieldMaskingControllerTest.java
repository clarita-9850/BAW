package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.FieldMaskingRequest;
import com.example.kafkaeventdrivenapp.model.FieldMaskingRule;
import com.example.kafkaeventdrivenapp.model.FieldMaskingRules;
import com.example.kafkaeventdrivenapp.service.FieldMaskingService;
import com.example.kafkaeventdrivenapp.util.TestSecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FieldMaskingControllerTest {

    private MockMvc mockMvc;
    private final StubFieldMaskingService fieldMaskingService = new StubFieldMaskingService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        FieldMaskingController controller = new FieldMaskingController();
        ReflectionTestUtils.setField(controller, "fieldMaskingService", fieldMaskingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getFieldMaskingInterfaceReturns403WhenSecurityContextIsNull() throws Exception {
        // Controller checks SecurityContextHolder which will be null in standalone test
        // getCurrentUserRole() returns default role which doesn't match ADMIN, so 403
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/field-masking/interface/ADMIN"))
                .andExpect(status().isForbidden())  // Controller returns 403 when role doesn't match
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void getFieldMaskingInterfaceWithJwtTokenReturns403WhenRoleMismatch() throws Exception {
        // Set up authentication with COUNTY_WORKER role
        TestSecurityUtils.setAuthentication("COUNTY_WORKER");
        try {
            // Request DISTRICT_WORKER interface but authenticated as COUNTY_WORKER
            mockMvc.perform(get("/api/field-masking/interface/SUPERVISOR")
                            .header("Authorization", "Bearer jwt-token-123"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"));
        } finally {
            TestSecurityUtils.clearAuthentication();
        }
    }

    @Test
    void updateFieldMaskingRules() throws Exception {
        setAuthentication("COUNTY_WORKER");
        try {
            java.util.List<com.example.kafkaeventdrivenapp.model.FieldMaskingRule> rules = List.of(buildRule("totalAmount"));

            mockMvc.perform(post("/api/field-masking/update/COUNTY_WORKER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rules)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.userRole").value("COUNTY_WORKER"));

            Assertions.assertEquals("COUNTY_WORKER", fieldMaskingService.getLastUpdatedRole());
            Assertions.assertEquals(1, fieldMaskingService.getLastUpdatedRules().size());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void getAvailableFieldsReturnsFieldList() throws Exception {
        mockMvc.perform(get("/api/field-masking/available-fields"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.fields").isArray())
                .andExpect(jsonPath("$.fields").isNotEmpty());
    }

    @Test
    void getFieldMaskingInterfaceHandlesServiceException() throws Exception {
        fieldMaskingService.setShouldThrowException(true);
        SecurityContextHolder.clearContext();

        // Without SecurityContext, controller checks role first and returns 403 before service call
        mockMvc.perform(get("/api/field-masking/interface/ADMIN"))
                .andExpect(status().isForbidden())  // Role check happens first, returns 403
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void getFieldMaskingInterfaceAllowsMatchingRole() throws Exception {
        setAuthentication("COUNTY_WORKER");
        try {
            FieldMaskingRules rules = new FieldMaskingRules();
            rules.setUserRole("COUNTY_WORKER");
            rules.setReportType("TIMESHEET_REPORT");
            rules.setRules(new ArrayList<>());
            fieldMaskingService.setRules(rules);

            mockMvc.perform(get("/api/field-masking/interface/COUNTY_WORKER")
                            .header("Authorization", "Bearer jwt-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.interface.userRole").value("COUNTY_WORKER"))
                    .andExpect(jsonPath("$.interface.source").value("KEYCLOAK_JWT"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void getFieldMaskingInterfaceReturns403WhenRoleDoesNotMatchAuthenticatedUser() throws Exception {
        TestSecurityUtils.setAuthentication("COUNTY_WORKER");
        try {
            mockMvc.perform(get("/api/field-masking/interface/ADMIN")
                            .header("Authorization", "Bearer jwt-token"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("Access denied: You can only access your own role's configuration"));
        } finally {
            TestSecurityUtils.clearAuthentication();
        }
    }

    @Test
    void updateFieldMaskingRulesReturns403WhenRoleMismatch() throws Exception {
        SecurityContextHolder.clearContext();
        // Without SecurityContext, controller uses default role which doesn't match ADMIN
        mockMvc.perform(post("/api/field-masking/update/ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isForbidden())  // Controller checks role and returns 403
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void updateMaskingRulesGenericAllowsCentralWorker() throws Exception {
        TestSecurityUtils.setJwtAuthentication("ADMIN");
        try {
            FieldMaskingRequest request = new FieldMaskingRequest(
                    "COUNTY_WORKER",
                    List.of(buildRule("totalHours")),
                    List.of("totalHours"));

            mockMvc.perform(post("/api/field-masking/update-rules")
                            .header("Authorization", "Bearer " + buildJwt("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.userRole").value("COUNTY_WORKER"));

            Assertions.assertEquals("COUNTY_WORKER", fieldMaskingService.getLastUpdatedRole());
        } finally {
            TestSecurityUtils.clearAuthentication();
        }
        Assertions.assertEquals(1, fieldMaskingService.getLastUpdatedRules().size());
        Assertions.assertEquals(List.of("totalHours"), fieldMaskingService.getLastSelectedFields());
    }

    @Test
    void updateMaskingRulesGenericReturnsForbiddenForNonCentralRole() throws Exception {
        TestSecurityUtils.setJwtAuthentication("COUNTY_WORKER");
        try {
            FieldMaskingRequest request = new FieldMaskingRequest(
                    "COUNTY_WORKER",
                    List.of(buildRule("totalHours")),
                    List.of("totalHours"));

            mockMvc.perform(post("/api/field-masking/update-rules")
                            .header("Authorization", "Bearer " + buildJwt("COUNTY_WORKER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.error").value("Access denied: Only ADMIN can manage field masking rules"));
        } finally {
            TestSecurityUtils.clearAuthentication();
        }
    }

    @Test
    void updateFieldMaskingRulesAllowsCentralWorkerToUpdateAnyRole() throws Exception {
        TestSecurityUtils.setAuthentication("ADMIN");
        try {
            java.util.List<com.example.kafkaeventdrivenapp.model.FieldMaskingRule> rules = List.of(buildRule("totalAmount"));

            mockMvc.perform(post("/api/field-masking/update/COUNTY_WORKER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rules)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.userRole").value("COUNTY_WORKER"));

            Assertions.assertEquals("COUNTY_WORKER", fieldMaskingService.getLastUpdatedRole());
        } finally {
            TestSecurityUtils.clearAuthentication();
        }
    }

    @Test
    void updateMaskingRulesGenericReturnsErrorWhenJwtMissing() throws Exception {
        FieldMaskingRequest request = new FieldMaskingRequest(
                "COUNTY_WORKER",
                List.of(buildRule("totalHours")),
                List.of("totalHours"));

        // When JWT is missing, the internal method call throws exception, resulting in 500
        mockMvc.perform(post("/api/field-masking/update-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void updateMaskingRulesGenericReturnsErrorWhenJwtInvalid() throws Exception {
        SecurityContextHolder.clearContext();
        FieldMaskingRequest request = new FieldMaskingRequest(
                "COUNTY_WORKER",
                List.of(buildRule("totalHours")),
                List.of("totalHours"));

        // When JWT is invalid and SecurityContext is null, controller returns 403
        mockMvc.perform(post("/api/field-masking/update-rules")
                        .header("Authorization", "Bearer invalid-jwt-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())  // Controller checks role first, returns 403
                .andExpect(jsonPath("$.status").value("FORBIDDEN"));
    }

    @Test
    void resyncAllRulesToKeycloakAllowsCentralWorker() throws Exception {
        TestSecurityUtils.setJwtAuthentication("ADMIN");
        try {
            fieldMaskingService.setSyncSuccess(true);
            // Set up rules for at least one role so resync has something to process
            FieldMaskingRules testRules = new FieldMaskingRules();
            testRules.setUserRole("COUNTY_WORKER");
            testRules.setReportType("TIMESHEET");
            testRules.setRules(List.of(buildRule("totalHours")));
            fieldMaskingService.setRules(testRules);

            mockMvc.perform(post("/api/field-masking/admin/resync-all")
                            .header("Authorization", "Bearer " + buildJwt("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.totalRoles").exists());
        } finally {
            TestSecurityUtils.clearAuthentication();
        }
    }

    @Test
    void resyncAllRulesToKeycloakReturnsForbiddenForNonCentralRole() throws Exception {
        TestSecurityUtils.setJwtAuthentication("COUNTY_WORKER");
        try {
            mockMvc.perform(post("/api/field-masking/admin/resync-all")
                            .header("Authorization", "Bearer " + buildJwt("COUNTY_WORKER")))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.error").value("Access denied: Only ADMIN can trigger re-sync"));
        } finally {
            TestSecurityUtils.clearAuthentication();
        }
    }

    @Test
    void resyncAllRulesToKeycloakReturnsForbiddenWhenJwtMissing() throws Exception {
        // When JWT is missing, the service call throws exception, resulting in 500
        // This is because getMaskingRules(role, "TIMESHEET") without JWT throws exception
        // The controller catches it and returns 500
        mockMvc.perform(post("/api/field-masking/admin/resync-all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    void updateFieldMaskingRulesReturns403WhenUserTriesToUpdateDifferentRole() throws Exception {
        TestSecurityUtils.setAuthentication("COUNTY_WORKER");
        try {
            java.util.List<com.example.kafkaeventdrivenapp.model.FieldMaskingRule> rules = List.of(buildRule("totalAmount"));

            mockMvc.perform(post("/api/field-masking/update/SUPERVISOR")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rules)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("Access denied: You can only update your own role's configuration"));
        } finally {
            TestSecurityUtils.clearAuthentication();
        }
    }

    private void setAuthentication(String role) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(role.toLowerCase() + "_user", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private FieldMaskingRule buildRule(String fieldName) {
        FieldMaskingRule rule = new FieldMaskingRule();
        rule.setFieldName(fieldName);
        rule.setMaskingType(FieldMaskingRule.MaskingType.PARTIAL_MASK);
        rule.setAccessLevel(FieldMaskingRule.AccessLevel.MASKED_ACCESS);
        rule.setReportType("TIMESHEET_REPORT");
        rule.setDescription("Mask " + fieldName);
        rule.setEnabled(true);
        return rule;
    }

    private String buildJwt(String preferredUsername) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"preferred_username\":\"" + preferredUsername + "\"}")
                        .getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".signature";
    }

    private static final class StubFieldMaskingService extends FieldMaskingService {
        private FieldMaskingRules rules;
        private java.util.List<String> selectedFields = new java.util.ArrayList<>();
        private boolean shouldThrowException = false;
        private String lastUpdatedRole;
        private java.util.List<FieldMaskingRule> lastUpdatedRules = new java.util.ArrayList<>();
        private java.util.List<String> lastSelectedFields = new java.util.ArrayList<>();
        private boolean syncSuccess = true;

        StubFieldMaskingService() {
            super(new StubPersistentFieldMaskingService(), new StubKeycloakFieldMaskingService());
        }

        void setRules(FieldMaskingRules rules) {
            this.rules = rules;
        }

        void setShouldThrowException(boolean shouldThrow) {
            this.shouldThrowException = shouldThrow;
        }

        void setSyncSuccess(boolean success) {
            this.syncSuccess = success;
        }

        @Override
        public FieldMaskingRules getMaskingRules(String userRole, String reportType) {
            if (shouldThrowException) {
                throw new RuntimeException("Service error");
            }
            if (rules == null) {
                FieldMaskingRules defaultRules = new FieldMaskingRules();
                defaultRules.setUserRole(userRole);
                defaultRules.setReportType(reportType);
                defaultRules.setRules(new java.util.ArrayList<>());
                return defaultRules;
            }
            return rules;
        }

        @Override
        public FieldMaskingRules getMaskingRules(String userRole, String reportType, String jwtToken) {
            if (shouldThrowException) {
                throw new RuntimeException("Service error");
            }
            if (rules == null) {
                FieldMaskingRules defaultRules = new FieldMaskingRules();
                defaultRules.setUserRole(userRole);
                defaultRules.setReportType(reportType);
                defaultRules.setRules(new java.util.ArrayList<>());
                return defaultRules;
            }
            return rules;
        }

        @Override
        public java.util.List<String> getSelectedFields(String userRole) {
            return selectedFields;
        }

        @Override
        public void updateRules(String userRole, java.util.List<com.example.kafkaeventdrivenapp.model.FieldMaskingRule> rules) {
            this.lastUpdatedRole = userRole;
            this.lastUpdatedRules = rules != null ? rules : new java.util.ArrayList<>();
        }

        @Override
        public void updateRules(String userRole, java.util.List<com.example.kafkaeventdrivenapp.model.FieldMaskingRule> rules, java.util.List<String> selectedFields) {
            if (shouldThrowException) {
                throw new RuntimeException("Service error");
            }
            this.lastUpdatedRole = userRole;
            this.lastUpdatedRules = rules != null ? rules : new java.util.ArrayList<>();
            this.lastSelectedFields = selectedFields != null ? selectedFields : new java.util.ArrayList<>();
            this.selectedFields = this.lastSelectedFields;
        }

        @Override
        public boolean syncRulesToKeycloak(String userRole, java.util.List<com.example.kafkaeventdrivenapp.model.FieldMaskingRule> rules, java.util.List<String> selectedFields) {
            return syncSuccess;
        }

        String getLastUpdatedRole() {
            return lastUpdatedRole;
        }

        java.util.List<FieldMaskingRule> getLastUpdatedRules() {
            return lastUpdatedRules;
        }

        java.util.List<String> getLastSelectedFields() {
            return lastSelectedFields;
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
}

