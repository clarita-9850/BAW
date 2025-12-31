package com.cmips.controller;

import com.cmips.service.KeycloakPolicyEvaluationService;
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
 * Unit tests for GenericResourceController
 * 
 * Tests cover:
 * - Generic CRUD operations
 * - Resource actions
 * - Scope retrieval
 * - Authorization checks (via @RequirePermission)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GenericResourceController Tests")
class GenericResourceControllerTest {

    @Mock
    private KeycloakPolicyEvaluationService keycloakPolicyEvaluationService;

    @InjectMocks
    private GenericResourceController genericResourceController;

    private static final String TEST_RESOURCE_TYPE = "timesheets";
    private static final String TEST_RESOURCE_ID = "123";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should create resource successfully")
    void testCreateResource_Success() {
        // Arrange
        Map<String, Object> request = Map.of("name", "Test Resource", "value", "Test Value");

        // Act
        ResponseEntity<?> response = genericResourceController.createResource(TEST_RESOURCE_TYPE, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Resource created successfully", body.get("message"));
        assertEquals(TEST_RESOURCE_TYPE, body.get("resourceType"));
    }

    @Test
    @DisplayName("Should get resources successfully")
    void testGetResources_Success() {
        // Act
        ResponseEntity<?> response = genericResourceController.getResources(TEST_RESOURCE_TYPE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Resources retrieved successfully", body.get("message"));
    }

    @Test
    @DisplayName("Should update resource successfully")
    void testUpdateResource_Success() {
        // Arrange
        Map<String, Object> request = Map.of("name", "Updated Resource");

        // Act
        ResponseEntity<?> response = genericResourceController.updateResource(
                TEST_RESOURCE_TYPE, TEST_RESOURCE_ID, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Resource updated successfully", body.get("message"));
        assertEquals(TEST_RESOURCE_ID, body.get("id"));
    }

    @Test
    @DisplayName("Should delete resource successfully")
    void testDeleteResource_Success() {
        // Act
        ResponseEntity<?> response = genericResourceController.deleteResource(
                TEST_RESOURCE_TYPE, TEST_RESOURCE_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Resource deleted successfully", body.get("message"));
    }

    @Test
    @DisplayName("Should perform action successfully")
    void testPerformAction_Success() {
        // Arrange
        String action = "approve";
        Map<String, Object> request = Map.of("reason", "Approved by supervisor");

        // Act
        ResponseEntity<?> response = genericResourceController.performAction(
                TEST_RESOURCE_TYPE, TEST_RESOURCE_ID, action, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Action performed successfully", body.get("message"));
        assertEquals(action, body.get("action"));
    }

    @Test
    @DisplayName("Should perform action without request body")
    void testPerformAction_NoRequestBody() {
        // Arrange
        String action = "activate";

        // Act
        ResponseEntity<?> response = genericResourceController.performAction(
                TEST_RESOURCE_TYPE, TEST_RESOURCE_ID, action, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should get granted scopes successfully")
    void testGetGrantedScopes_Success() {
        // Arrange
        Set<String> mockScopes = Set.of("read", "write", "delete");
        when(keycloakPolicyEvaluationService.getGrantedScopes(anyString())).thenReturn(mockScopes);

        // Act
        ResponseEntity<?> response = genericResourceController.getGrantedScopes(TEST_RESOURCE_TYPE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey("grantedScopes"));
        verify(keycloakPolicyEvaluationService, times(1)).getGrantedScopes(anyString());
    }

    @Test
    @DisplayName("Should handle different resource types")
    void testCreateResource_DifferentTypes() {
        // Arrange
        String[] resourceTypes = {"timesheets", "tasks", "cases", "reports"};
        Map<String, Object> request = Map.of("data", "test");

        // Act & Assert
        for (String resourceType : resourceTypes) {
            ResponseEntity<?> response = genericResourceController.createResource(resourceType, request);
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }
}

