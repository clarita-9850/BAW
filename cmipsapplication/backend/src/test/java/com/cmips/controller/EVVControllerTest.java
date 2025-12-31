package com.cmips.controller;

import com.cmips.entity.EVVRecord;
import com.cmips.service.EVVService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EVVController
 * 
 * Tests cover:
 * - Check-in operations
 * - Check-out operations
 * - EVV record retrieval
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("EVVController Tests")
class EVVControllerTest {

    @Mock
    private EVVService evvService;

    @InjectMocks
    private EVVController evvController;

    private static final String TEST_PROVIDER_ID = "provider1";
    private static final String TEST_RECIPIENT_ID = "recipient1";

    @BeforeEach
    void setUp() {
        // Security context setup is done per test as needed
    }

    private void setupMockSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(TEST_PROVIDER_ID);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should check-in successfully")
    void testCheckIn_Success() {
        // Arrange
        setupMockSecurityContext();
        Map<String, Object> request = createValidCheckInRequest();
        EVVRecord mockEVV = createMockEVVRecord(1L);
        when(evvService.checkIn(eq(TEST_PROVIDER_ID), eq(TEST_RECIPIENT_ID), anyString(), anyDouble(), anyDouble()))
                .thenReturn(mockEVV);

        // Act
        ResponseEntity<EVVRecord> response = evvController.checkIn(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(evvService, times(1)).checkIn(anyString(), anyString(), anyString(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should return Conflict when already checked in")
    void testCheckIn_Conflict() {
        // Arrange
        setupMockSecurityContext();
        Map<String, Object> request = createValidCheckInRequest();
        when(evvService.checkIn(anyString(), anyString(), anyString(), anyDouble(), anyDouble()))
                .thenThrow(new IllegalStateException("Already checked in"));

        // Act
        ResponseEntity<EVVRecord> response = evvController.checkIn(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(evvService, times(1)).checkIn(anyString(), anyString(), anyString(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should check-out successfully")
    void testCheckOut_Success() {
        // Arrange
        Long evvId = 1L;
        Map<String, Object> request = Map.of(
                "latitude", 34.0522,
                "longitude", -118.2437
        );
        EVVRecord mockEVV = createMockEVVRecord(evvId);
        mockEVV.setCheckOutTime(LocalDateTime.now());
        when(evvService.checkOut(eq(evvId), anyDouble(), anyDouble())).thenReturn(mockEVV);

        // Act
        ResponseEntity<EVVRecord> response = evvController.checkOut(evvId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCheckOutTime());
        verify(evvService, times(1)).checkOut(eq(evvId), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should get my EVV records successfully")
    void testGetMyEVVRecords_Success() {
        // Arrange
        setupMockSecurityContext();
        List<EVVRecord> mockRecords = Arrays.asList(
                createMockEVVRecord(1L),
                createMockEVVRecord(2L)
        );
        when(evvService.getProviderEVVRecords(TEST_PROVIDER_ID)).thenReturn(mockRecords);

        // Act
        ResponseEntity<List<EVVRecord>> response = evvController.getMyEVVRecords();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(evvService, times(1)).getProviderEVVRecords(TEST_PROVIDER_ID);
    }

    @Test
    @DisplayName("Should get active check-in successfully")
    void testGetActiveCheckIn_Success() {
        // Arrange
        setupMockSecurityContext();
        EVVRecord mockEVV = createMockEVVRecord(1L);
        when(evvService.getActiveCheckIn(TEST_PROVIDER_ID, TEST_RECIPIENT_ID))
                .thenReturn(Optional.of(mockEVV));

        // Act
        ResponseEntity<EVVRecord> response = evvController.getActiveCheckIn(TEST_RECIPIENT_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(evvService, times(1)).getActiveCheckIn(TEST_PROVIDER_ID, TEST_RECIPIENT_ID);
    }

    @Test
    @DisplayName("Should return NotFound when no active check-in")
    void testGetActiveCheckIn_NotFound() {
        // Arrange
        setupMockSecurityContext();
        when(evvService.getActiveCheckIn(TEST_PROVIDER_ID, TEST_RECIPIENT_ID))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<EVVRecord> response = evvController.getActiveCheckIn(TEST_RECIPIENT_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(evvService, times(1)).getActiveCheckIn(TEST_PROVIDER_ID, TEST_RECIPIENT_ID);
    }

    @Test
    @DisplayName("Should get timesheet EVV records successfully")
    void testGetTimesheetEVVRecords_Success() {
        // Arrange
        Long timesheetId = 1L;
        List<EVVRecord> mockRecords = Arrays.asList(createMockEVVRecord(1L));
        when(evvService.getTimesheetEVVRecords(timesheetId)).thenReturn(mockRecords);

        // Act
        ResponseEntity<List<EVVRecord>> response = evvController.getTimesheetEVVRecords(timesheetId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(evvService, times(1)).getTimesheetEVVRecords(timesheetId);
    }

    // Helper methods
    private Map<String, Object> createValidCheckInRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("recipientId", TEST_RECIPIENT_ID);
        request.put("serviceType", "Personal Care");
        request.put("latitude", 34.0522);
        request.put("longitude", -118.2437);
        return request;
    }

    private EVVRecord createMockEVVRecord(Long id) {
        EVVRecord record = new EVVRecord();
        record.setId(id);
        record.setProviderId(TEST_PROVIDER_ID);
        record.setRecipientId(TEST_RECIPIENT_ID);
        record.setServiceType("Personal Care");
        record.setCheckInTime(LocalDateTime.now());
        record.setCheckInLatitude(34.0522);
        record.setCheckInLongitude(-118.2437);
        record.setStatus("CHECKED_IN");
        return record;
    }
}

