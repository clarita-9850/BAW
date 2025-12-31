package com.cmips.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KeycloakAuthorizationService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("KeycloakAuthorizationService Tests")
class KeycloakAuthorizationServiceTest {

    @InjectMocks
    private KeycloakAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should return true when PROVIDER has create permission")
    void testHasPermission_ProviderCreate() {
        // Arrange
        setupMockJWTWithRoles(Arrays.asList("PROVIDER"));

        // Act
        boolean result = authorizationService.hasTimesheetPermission("token", "create");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true when PROVIDER has read permission")
    void testHasPermission_ProviderRead() {
        // Arrange
        setupMockJWTWithRoles(Arrays.asList("PROVIDER"));

        // Act
        boolean result = authorizationService.hasTimesheetPermission("token", "read");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true when CASE_WORKER has read permission")
    void testHasPermission_CaseWorkerRead() {
        // Arrange
        setupMockJWTWithRoles(Arrays.asList("CASE_WORKER"));

        // Act
        boolean result = authorizationService.hasTimesheetPermission("token", "read");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user lacks permission")
    void testHasPermission_Denied() {
        // Arrange
        setupMockJWTWithRoles(Arrays.asList("RECIPIENT"));

        // Act
        boolean result = authorizationService.hasTimesheetPermission("token", "create");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when no roles in JWT")
    void testHasPermission_NoRoles() {
        // Arrange
        setupMockJWTWithRoles(Collections.emptyList());

        // Act
        boolean result = authorizationService.hasTimesheetPermission("token", "read");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when no authentication")
    void testHasPermission_NoAuthentication() {
        // Arrange
        setupMockSecurityContext(null);

        // Act
        boolean result = authorizationService.hasTimesheetPermission("token", "read");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should check hasRole successfully")
    void testHasRole_Success() {
        // Arrange
        Set<String> roles = Set.of("ADMIN", "PROVIDER");

        // Act
        boolean result = authorizationService.hasRole(roles, "PROVIDER");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should check hasAnyRole successfully")
    void testHasAnyRole_Success() {
        // Arrange
        Set<String> roles = Set.of("ADMIN", "PROVIDER");

        // Act
        boolean result = authorizationService.hasAnyRole(roles, "PROVIDER", "CASE_WORKER");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when role not found")
    void testHasRole_NotFound() {
        // Arrange
        Set<String> roles = Set.of("ADMIN", "PROVIDER");

        // Act
        boolean result = authorizationService.hasRole(roles, "CASE_WORKER");

        // Assert
        assertFalse(result);
    }

    // Helper methods
    private void setupMockJWTWithRoles(List<String> roles) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Jwt jwt = mock(Jwt.class);

        Map<String, Object> realmAccess = Map.of("roles", roles);
        Map<String, Object> claims = Map.of("realm_access", realmAccess);

        when(jwt.getClaims()).thenReturn(claims);
        when(jwt.getClaim("realm_access")).thenReturn(realmAccess);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupMockSecurityContext(Authentication authentication) {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}







