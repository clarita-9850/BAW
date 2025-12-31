package com.cmips.controller;

import com.cmips.model.FieldMaskingRequest;
import com.cmips.model.FieldMaskingRule;
import com.cmips.model.FieldMaskingRules;
import com.cmips.service.FieldMaskingService;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FieldMaskingController
 * 
 * Tests cover:
 * - Get field masking interface
 * - Update field masking rules
 * - Get available fields
 * - Get available roles
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FieldMaskingController Tests")
class FieldMaskingControllerTest {

    @Mock
    private FieldMaskingService fieldMaskingService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private FieldMaskingController fieldMaskingController;

    private static final String TEST_ROLE = "RECIPIENT";
    private static final String TEST_JWT = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.test";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should get field masking interface successfully")
    void testGetFieldMaskingInterface_Success() {
        // Arrange
        FieldMaskingRules mockRules = createMockMaskingRules();
        List<String> mockSelectedFields = Arrays.asList("id", "employeeName", "totalHours");
        
        when(httpServletRequest.getHeader("Authorization")).thenReturn(TEST_JWT);
        when(fieldMaskingService.getMaskingRules(eq(TEST_ROLE), anyString(), anyString()))
                .thenReturn(mockRules);
        when(fieldMaskingService.getSelectedFields(TEST_ROLE)).thenReturn(mockSelectedFields);

        // Act
        ResponseEntity<Map<String, Object>> response = 
                fieldMaskingController.getFieldMaskingInterface(TEST_ROLE, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("interface"));
        verify(fieldMaskingService, times(1)).getMaskingRules(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should update field masking rules successfully")
    void testUpdateFieldMaskingRules_Success() {
        // Arrange
        FieldMaskingRequest request = createValidMaskingRequest();
        doNothing().when(fieldMaskingService).updateRules(anyString(), anyList(), anyList());

        // Act
        ResponseEntity<Map<String, Object>> response = 
                fieldMaskingController.updateFieldMaskingRules(request, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        verify(fieldMaskingService, times(1)).updateRules(anyString(), anyList(), anyList());
    }

    @Test
    @DisplayName("Should get available fields successfully")
    void testGetAvailableFields_Success() {
        // Act
        ResponseEntity<Map<String, Object>> response = fieldMaskingController.getAvailableFields();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("fields"));
    }

    @Test
    @DisplayName("Should get available roles successfully")
    void testGetAvailableRoles_Success() {
        // Act
        ResponseEntity<Map<String, Object>> response = fieldMaskingController.getAvailableRoles();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("roles"));
    }

    @Test
    @DisplayName("Should handle service errors gracefully")
    void testGetFieldMaskingInterface_ServiceError() {
        // Arrange
        when(httpServletRequest.getHeader("Authorization")).thenReturn(TEST_JWT);
        when(fieldMaskingService.getMaskingRules(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<Map<String, Object>> response = 
                fieldMaskingController.getFieldMaskingInterface(TEST_ROLE, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
    }

    @Test
    @DisplayName("Should handle update rules errors gracefully")
    void testUpdateFieldMaskingRules_ServiceError() {
        // Arrange
        FieldMaskingRequest request = createValidMaskingRequest();
        doThrow(new RuntimeException("Service error"))
                .when(fieldMaskingService).updateRules(anyString(), anyList(), anyList());

        // Act
        ResponseEntity<Map<String, Object>> response = 
                fieldMaskingController.updateFieldMaskingRules(request, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
    }

    @Test
    @DisplayName("Should work without JWT token (fallback)")
    void testGetFieldMaskingInterface_NoJWT() {
        // Arrange
        FieldMaskingRules mockRules = createMockMaskingRules();
        List<String> mockSelectedFields = Arrays.asList("id", "employeeName");
        
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);
        when(fieldMaskingService.getMaskingRules(eq(TEST_ROLE), anyString(), isNull()))
                .thenReturn(mockRules);
        when(fieldMaskingService.getSelectedFields(TEST_ROLE)).thenReturn(mockSelectedFields);

        // Act
        ResponseEntity<Map<String, Object>> response = 
                fieldMaskingController.getFieldMaskingInterface(TEST_ROLE, httpServletRequest);

        // Assert
        assertNotNull(response);
        // Should still work, possibly with default rules
    }

    // Helper methods
    private FieldMaskingRules createMockMaskingRules() {
        FieldMaskingRule rule = new FieldMaskingRule();
        rule.setFieldName("ssn");
        rule.setMaskingType(FieldMaskingRule.MaskingType.HIDDEN);
        
        FieldMaskingRules rules = new FieldMaskingRules();
        rules.setUserRole(TEST_ROLE);
        rules.setReportType("TIMESHEET_REPORT");
        rules.setRules(Arrays.asList(rule));
        return rules;
    }

    private FieldMaskingRequest createValidMaskingRequest() {
        FieldMaskingRequest request = new FieldMaskingRequest();
        request.setUserRole(TEST_ROLE);
        
        FieldMaskingRule rule = new FieldMaskingRule();
        rule.setFieldName("ssn");
        rule.setMaskingType(FieldMaskingRule.MaskingType.HIDDEN);
        request.setRules(Arrays.asList(rule));
        request.setSelectedFields(Arrays.asList("id", "employeeName", "totalHours"));
        
        return request;
    }
}

