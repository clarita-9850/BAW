package com.cmips.controller;

import com.cmips.dto.TimesheetCreateRequest;
import com.cmips.dto.TimesheetResponse;
import com.cmips.dto.TimesheetUpdateRequest;
import com.cmips.entity.Timesheet;
import com.cmips.entity.TimesheetStatus;
import com.cmips.service.FieldLevelAuthorizationService;
import com.cmips.service.KeycloakAuthorizationService;
import com.cmips.service.TimesheetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TimesheetController
 * 
 * Tests cover:
 * - Create, Read, Update, Delete operations
 * - Submit, Approve, Reject workflow
 * - Role-based access control
 * - Field-level authorization
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimesheetController Tests")
class TimesheetControllerTest {

    @Mock
    private TimesheetService timesheetService;

    @Mock
    private KeycloakAuthorizationService keycloakAuthzService;

    @Mock
    private FieldLevelAuthorizationService fieldLevelAuthzService;

    @InjectMocks
    private TimesheetController timesheetController;

    private ObjectMapper objectMapper;

    private static final String TEST_USER_ID = "testuser";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        setupMockSecurityContext();
    }

    private void setupMockSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(jwt.getSubject()).thenReturn(TEST_USER_ID);
        when(jwt.getClaimAsString("preferred_username")).thenReturn(TEST_USERNAME);
        
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("PROVIDER"));
        when(jwt.getClaimAsMap("realm_access")).thenReturn(realmAccess);

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should successfully create a timesheet")
    void testCreateTimesheet_Success() {
        // Arrange
        Map<String, Object> requestData = createValidTimesheetRequest();
        TimesheetResponse mockResponse = createMockTimesheetResponse(1L);
        
        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("create")))
                .thenReturn(requestData);
        when(timesheetService.createTimesheet(eq(TEST_USER_ID), any(TimesheetCreateRequest.class)))
                .thenReturn(mockResponse);
        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("read")))
                .thenReturn(convertToMap(mockResponse));

        // Act
        ResponseEntity<?> response = timesheetController.createTimesheet(requestData, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(timesheetService, times(1)).createTimesheet(eq(TEST_USER_ID), any(TimesheetCreateRequest.class));
    }

    @Test
    @DisplayName("Should return BadRequest when validation fails")
    void testCreateTimesheet_ValidationError() {
        // Arrange
        Map<String, Object> requestData = createValidTimesheetRequest();
        
        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("create")))
                .thenReturn(requestData);
        when(timesheetService.createTimesheet(eq(TEST_USER_ID), any(TimesheetCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("Timesheet already exists for this pay period"));

        // Act
        ResponseEntity<?> response = timesheetController.createTimesheet(requestData, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should get all timesheets for PROVIDER role")
    void testGetTimesheets_ProviderRole() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<TimesheetResponse> mockPage = new PageImpl<>(Arrays.asList(
                createMockTimesheetResponse(1L),
                createMockTimesheetResponse(2L)
        ), pageable, 2);

        when(keycloakAuthzService.hasRole(anySet(), eq("PROVIDER"))).thenReturn(true);
        when(timesheetService.getTimesheetsByUserId(eq(TEST_USER_ID), any(Pageable.class)))
                .thenReturn(mockPage);
        when(fieldLevelAuthzService.filterFields(anyList(), eq("Timesheet Resource"), eq("read")))
                .thenReturn(createMockFilteredTimesheetsList());

        // Act
        ResponseEntity<?> response = timesheetController.getTimesheets(pageable, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timesheetService, times(1)).getTimesheetsByUserId(eq(TEST_USER_ID), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get all timesheets for CASE_WORKER role")
    void testGetTimesheets_CaseWorkerRole() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<TimesheetResponse> mockPage = new PageImpl<>(Arrays.asList(
                createMockTimesheetResponse(1L),
                createMockTimesheetResponse(2L)
        ), pageable, 2);

        when(keycloakAuthzService.hasRole(anySet(), eq("PROVIDER"))).thenReturn(false);
        when(keycloakAuthzService.hasRole(anySet(), eq("RECIPIENT"))).thenReturn(false);
        when(keycloakAuthzService.hasRole(anySet(), eq("CASE_WORKER"))).thenReturn(true);
        when(timesheetService.getAllTimesheets(any(Pageable.class))).thenReturn(mockPage);
        when(fieldLevelAuthzService.filterFields(anyList(), eq("Timesheet Resource"), eq("read")))
                .thenReturn(createMockFilteredTimesheetsList());

        // Act
        ResponseEntity<?> response = timesheetController.getTimesheets(pageable, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timesheetService, times(1)).getAllTimesheets(any(Pageable.class));
    }

    @Test
    @DisplayName("Should submit timesheet successfully")
    void testSubmitTimesheet_Success() {
        // Arrange
        Long timesheetId = 1L;
        TimesheetResponse mockResponse = createMockTimesheetResponse(timesheetId);
        mockResponse.setStatus(TimesheetStatus.SUBMITTED);

        when(keycloakAuthzService.hasRole(anySet(), eq("PROVIDER"))).thenReturn(true);
        when(timesheetService.submitTimesheet(eq(timesheetId), eq(TEST_USER_ID)))
                .thenReturn(Optional.of(mockResponse));

        // Act
        ResponseEntity<?> response = timesheetController.submitTimesheet(timesheetId, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timesheetService, times(1)).submitTimesheet(eq(timesheetId), eq(TEST_USER_ID));
    }

    @Test
    @DisplayName("Should return Forbidden when user lacks submit permission")
    void testSubmitTimesheet_Forbidden() {
        // Arrange
        Long timesheetId = 1L;

        when(keycloakAuthzService.hasRole(anySet(), eq("PROVIDER"))).thenReturn(false);

        // Act
        ResponseEntity<?> response = timesheetController.submitTimesheet(timesheetId, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(timesheetService, never()).submitTimesheet(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should approve timesheet successfully")
    void testApproveTimesheet_Success() {
        // Arrange
        Long timesheetId = 1L;
        TimesheetResponse mockResponse = createMockTimesheetResponse(timesheetId);
        mockResponse.setStatus(TimesheetStatus.APPROVED);

        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("approve")))
                .thenReturn(new HashMap<>());
        when(timesheetService.approveTimesheet(eq(timesheetId), eq(TEST_USER_ID)))
                .thenReturn(Optional.of(mockResponse));
        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("read")))
                .thenReturn(convertToMap(mockResponse));

        // Act
        ResponseEntity<?> response = timesheetController.approveTimesheet(timesheetId, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timesheetService, times(1)).approveTimesheet(eq(timesheetId), eq(TEST_USER_ID));
    }

    @Test
    @DisplayName("Should reject timesheet successfully")
    void testRejectTimesheet_Success() {
        // Arrange
        Long timesheetId = 1L;
        Map<String, Object> requestData = Map.of("supervisorComments", "Hours mismatch");
        TimesheetResponse mockResponse = createMockTimesheetResponse(timesheetId);
        mockResponse.setStatus(TimesheetStatus.REJECTED);

        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("reject")))
                .thenReturn(requestData);
        when(timesheetService.rejectTimesheet(eq(timesheetId), eq(TEST_USER_ID), anyString()))
                .thenReturn(Optional.of(mockResponse));
        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("read")))
                .thenReturn(convertToMap(mockResponse));

        // Act
        ResponseEntity<?> response = timesheetController.rejectTimesheet(timesheetId, requestData, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timesheetService, times(1)).rejectTimesheet(eq(timesheetId), eq(TEST_USER_ID), anyString());
    }

    @Test
    @DisplayName("Should update timesheet successfully")
    void testUpdateTimesheet_Success() {
        // Arrange
        Long timesheetId = 1L;
        Map<String, Object> requestData = Map.of(
                "regularHours", 85.0,
                "comments", "Updated hours"
        );
        TimesheetResponse mockResponse = createMockTimesheetResponse(timesheetId);

        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("edit")))
                .thenReturn(requestData);
        when(timesheetService.updateTimesheet(eq(timesheetId), any(TimesheetUpdateRequest.class)))
                .thenReturn(Optional.of(mockResponse));
        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("read")))
                .thenReturn(convertToMap(mockResponse));

        // Act
        ResponseEntity<?> response = timesheetController.updateTimesheet(timesheetId, requestData);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timesheetService, times(1)).updateTimesheet(eq(timesheetId), any(TimesheetUpdateRequest.class));
    }

    @Test
    @DisplayName("Should return NotFound when timesheet does not exist")
    void testUpdateTimesheet_NotFound() {
        // Arrange
        Long timesheetId = 1L;
        Map<String, Object> requestData = Map.of("regularHours", 85.0);

        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("edit")))
                .thenReturn(requestData);
        when(timesheetService.updateTimesheet(eq(timesheetId), any(TimesheetUpdateRequest.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = timesheetController.updateTimesheet(timesheetId, requestData);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should delete timesheet successfully")
    void testDeleteTimesheet_Success() {
        // Arrange
        Long timesheetId = 1L;

        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("delete")))
                .thenReturn(new HashMap<>());
        when(timesheetService.deleteTimesheet(eq(timesheetId), eq(TEST_USER_ID)))
                .thenReturn(true);

        // Act
        ResponseEntity<?> response = timesheetController.deleteTimesheet(timesheetId, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timesheetService, times(1)).deleteTimesheet(eq(timesheetId), eq(TEST_USER_ID));
    }

    @Test
    @DisplayName("Should return NotFound when deleting non-existent timesheet")
    void testDeleteTimesheet_NotFound() {
        // Arrange
        Long timesheetId = 1L;

        when(fieldLevelAuthzService.filterFields(anyMap(), eq("Timesheet Resource"), eq("delete")))
                .thenReturn(new HashMap<>());
        when(timesheetService.deleteTimesheet(eq(timesheetId), eq(TEST_USER_ID)))
                .thenReturn(false);

        // Act
        ResponseEntity<?> response = timesheetController.deleteTimesheet(timesheetId, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should get allowed actions")
    void testGetAllowedActions() {
        // Arrange
        Set<String> allowedActions = Set.of("create", "read", "update", "submit");
        when(fieldLevelAuthzService.getAllowedActions(eq("Timesheet Resource")))
                .thenReturn(allowedActions);

        // Act
        ResponseEntity<?> response = timesheetController.getAllowedActions();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(fieldLevelAuthzService, times(1)).getAllowedActions(eq("Timesheet Resource"));
    }

    // Helper methods
    private Map<String, Object> createValidTimesheetRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", "EMP001");
        request.put("employeeName", "John Doe");
        request.put("department", "IT");
        request.put("location", "CTA");
        request.put("payPeriodStart", "2025-01-01");
        request.put("payPeriodEnd", "2025-01-15");
        request.put("regularHours", 80.0);
        request.put("overtimeHours", 10.0);
        return request;
    }

    private TimesheetResponse createMockTimesheetResponse(Long id) {
        TimesheetResponse response = new TimesheetResponse();
        response.setId(id);
        response.setUserId(TEST_USER_ID);
        response.setEmployeeId("EMP001");
        response.setEmployeeName("John Doe");
        response.setDepartment("IT");
        response.setLocation("CTA");
        response.setPayPeriodStart(LocalDate.of(2025, 1, 1));
        response.setPayPeriodEnd(LocalDate.of(2025, 1, 15));
        response.setRegularHours(new BigDecimal("80.0"));
        response.setOvertimeHours(new BigDecimal("10.0"));
        response.setTotalHours(new BigDecimal("90.0"));
        response.setStatus(TimesheetStatus.DRAFT);
        return response;
    }

    private Map<String, Object> convertToMap(TimesheetResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", response.getId());
        map.put("userId", response.getUserId());
        map.put("employeeId", response.getEmployeeId());
        map.put("employeeName", response.getEmployeeName());
        map.put("department", response.getDepartment());
        map.put("location", response.getLocation());
        map.put("regularHours", response.getRegularHours());
        map.put("status", response.getStatus());
        return map;
    }

    private List<Map<String, Object>> createMockFilteredTimesheetsList() {
        Map<String, Object> timesheet1 = new HashMap<>();
        timesheet1.put("id", 1L);
        timesheet1.put("employeeName", "John Doe");
        
        Map<String, Object> timesheet2 = new HashMap<>();
        timesheet2.put("id", 2L);
        timesheet2.put("employeeName", "Jane Smith");
        
        return Arrays.asList(timesheet1, timesheet2);
    }
}

