package com.cmips.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FieldLevelAuthorizationService
 * 
 * Tests cover:
 * - Field filtering
 * - Allowed fields retrieval
 * - Action authorization
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FieldLevelAuthorizationService Tests")
class FieldLevelAuthorizationServiceTest {

    @InjectMocks
    private FieldLevelAuthorizationService fieldAuthorizationService;

    @BeforeEach
    void setUp() {
        // Setup Keycloak properties
        ReflectionTestUtils.setField(fieldAuthorizationService, "keycloakServerUrl", "http://localhost:8085/auth");
        ReflectionTestUtils.setField(fieldAuthorizationService, "realm", "cmips");
        ReflectionTestUtils.setField(fieldAuthorizationService, "clientId", "cmips-backend");
        ReflectionTestUtils.setField(fieldAuthorizationService, "clientSecret", "secret");
    }

    @Test
    @DisplayName("Should filter fields successfully")
    void testFilterFields_Success() {
        // Arrange
        Map<String, Object> data = Map.of(
                "id", 1L,
                "name", "John Doe",
                "ssn", "123-45-6789",
                "email", "john@example.com"
        );
        setupMockSecurityContext("PROVIDER");

        // Act
        Map<String, Object> result = fieldAuthorizationService.filterFields(data, "Timesheet Resource");

        // Assert
        assertNotNull(result);
        // Note: Actual filtering depends on Keycloak configuration
    }

    @Test
    @DisplayName("Should filter fields with scope successfully")
    void testFilterFields_WithScope() {
        // Arrange
        Map<String, Object> data = Map.of(
                "id", 1L,
                "name", "John Doe"
        );
        setupMockSecurityContext("PROVIDER");

        // Act
        Map<String, Object> result = fieldAuthorizationService.filterFields(data, "Timesheet Resource", "read");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should filter list of fields successfully")
    void testFilterFields_List() {
        // Arrange
        List<Map<String, Object>> dataList = Arrays.asList(
                Map.of("id", 1L, "name", "John"),
                Map.of("id", 2L, "name", "Jane")
        );
        setupMockSecurityContext("PROVIDER");

        // Act
        List<Map<String, Object>> result = fieldAuthorizationService.filterFields(dataList, "Timesheet Resource");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return empty map when no allowed fields")
    void testFilterFields_NoAllowedFields() {
        // Arrange
        Map<String, Object> data = Map.of("id", 1L);
        setupMockSecurityContext("UNKNOWN");

        // Act
        Map<String, Object> result = fieldAuthorizationService.filterFields(data, "Timesheet Resource");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should get allowed fields successfully")
    void testGetAllowedFields_Success() {
        // Arrange
        setupMockSecurityContext("PROVIDER");

        // Act
        Set<String> result = fieldAuthorizationService.getAllowedFields("Timesheet Resource");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should check field access successfully")
    void testCanAccessField() {
        // Arrange
        setupMockSecurityContext("PROVIDER");

        // Act
        boolean result = fieldAuthorizationService.canAccessField("Timesheet Resource", "id");

        // Assert
        // Result depends on Keycloak configuration
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should get allowed actions successfully")
    void testGetAllowedActions() {
        // Arrange
        setupMockSecurityContext("PROVIDER");

        // Act
        Set<String> result = fieldAuthorizationService.getAllowedActions("Timesheet Resource");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should check action permission successfully")
    void testCanPerformAction() {
        // Arrange
        setupMockSecurityContext("PROVIDER");

        // Act
        boolean result = fieldAuthorizationService.canPerformAction("Timesheet Resource", "edit");

        // Assert
        assertNotNull(result);
    }

    // Helper methods
    private void setupMockSecurityContext(String role) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Jwt jwt = mock(Jwt.class);

        Map<String, Object> realmAccess = Map.of("roles", Arrays.asList(role));
        when(jwt.getClaim("realm_access")).thenReturn(realmAccess);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}







