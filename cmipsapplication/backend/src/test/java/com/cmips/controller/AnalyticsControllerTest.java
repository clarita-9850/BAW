package com.cmips.controller;

import com.cmips.entity.Timesheet;
import com.cmips.entity.TimesheetStatus;
import com.cmips.repository.TimesheetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnalyticsController
 * 
 * Tests cover:
 * - Real-time metrics retrieval
 * - Filtering capabilities
 * - Demographic data retrieval
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsController Tests")
class AnalyticsControllerTest {

    @Mock
    private TimesheetRepository timesheetRepository;

    @InjectMocks
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should get real-time metrics successfully")
    void testGetRealTimeMetrics_Success() {
        // Arrange
        List<Timesheet> mockTimesheets = createMockTimesheets();
        when(timesheetRepository.findAll()).thenReturn(mockTimesheets);

        // Act
        ResponseEntity<Map<String, Object>> response = analyticsController.getRealTimeMetrics(
                null, null, null, null, null, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("totalTimesheets"));
        assertTrue(response.getBody().containsKey("pendingApprovals"));
        verify(timesheetRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get real-time metrics with county filter")
    void testGetRealTimeMetrics_WithCountyFilter() {
        // Arrange
        List<Timesheet> mockTimesheets = createMockTimesheets();
        when(timesheetRepository.findAll()).thenReturn(mockTimesheets);

        // Act
        ResponseEntity<Map<String, Object>> response = analyticsController.getRealTimeMetrics(
                null, "CTA", null, null, null, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timesheetRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get real-time metrics with status filter")
    void testGetRealTimeMetrics_WithStatusFilter() {
        // Arrange
        List<Timesheet> mockTimesheets = createMockTimesheets();
        when(timesheetRepository.findAll()).thenReturn(mockTimesheets);

        // Act
        ResponseEntity<Map<String, Object>> response = analyticsController.getRealTimeMetrics(
                null, null, "SUBMITTED", null, null, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timesheetRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get filter options successfully")
    void testGetFilterOptions_Success() {
        // Arrange
        List<Timesheet> mockTimesheets = createMockTimesheets();
        when(timesheetRepository.findAll()).thenReturn(mockTimesheets);

        // Act
        ResponseEntity<Map<String, Object>> response = analyticsController.getFilterOptions();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("counties"));
        assertTrue(response.getBody().containsKey("departments"));
        assertTrue(response.getBody().containsKey("statuses"));
        verify(timesheetRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get demographics by gender successfully")
    void testGetDemographicsByGender_Success() {
        // Arrange
        // Note: Current implementation returns placeholder data

        // Act
        ResponseEntity<Map<String, Object>> response = analyticsController.getDemographicsByGender(null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("status"));
    }

    @Test
    @DisplayName("Should handle repository errors gracefully")
    void testGetRealTimeMetrics_RepositoryError() {
        // Arrange
        when(timesheetRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<Map<String, Object>> response = analyticsController.getRealTimeMetrics(
                null, null, null, null, null, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
    }

    // Helper methods
    private List<Timesheet> createMockTimesheets() {
        List<Timesheet> timesheets = new ArrayList<>();
        
        Timesheet ts1 = new Timesheet();
        ts1.setId(1L);
        ts1.setUserId("user1");
        ts1.setEmployeeId("EMP001");
        ts1.setLocation("CTA");
        ts1.setDepartment("IT");
        ts1.setStatus(TimesheetStatus.SUBMITTED);
        ts1.setCreatedAt(LocalDateTime.now());
        ts1.setSubmittedAt(LocalDateTime.now());
        timesheets.add(ts1);
        
        Timesheet ts2 = new Timesheet();
        ts2.setId(2L);
        ts2.setUserId("user2");
        ts2.setEmployeeId("EMP002");
        ts2.setLocation("CTB");
        ts2.setDepartment("HR");
        ts2.setStatus(TimesheetStatus.APPROVED);
        ts2.setCreatedAt(LocalDateTime.now());
        ts2.setApprovedAt(LocalDateTime.now());
        timesheets.add(ts2);
        
        return timesheets;
    }
}







