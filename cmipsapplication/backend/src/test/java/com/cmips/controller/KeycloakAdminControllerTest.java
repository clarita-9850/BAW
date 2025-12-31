package com.cmips.controller;

import com.cmips.dto.CreatePermissionRequest;
import com.cmips.dto.CreatePolicyRequest;
import com.cmips.dto.CreateUserRequest;
import com.cmips.service.KeycloakAdminService;
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
 * Unit tests for KeycloakAdminController
 * 
 * Tests cover:
 * - User management (create, delete, list)
 * - Role management (create, delete, list, assign)
 * - Group management (create, delete, list, members)
 * - Policy and permission management
 * - Resource management
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("KeycloakAdminController Tests")
class KeycloakAdminControllerTest {

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private KeycloakAdminController keycloakAdminController;

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_ROLE_ID = "role-456";
    private static final String TEST_GROUP_ID = "group-789";
    private static final String TEST_RESOURCE_ID = "resource-123";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    // ============================== USER MANAGEMENT TESTS ==============================

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUser_Success() {
        // Arrange
        CreateUserRequest request = createValidUserRequest();
        when(keycloakAdminService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(TEST_USER_ID);
        doNothing().when(keycloakAdminService).assignRoleToUser(anyString(), anyString());

        // Act
        ResponseEntity<?> response = keycloakAdminController.createUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        verify(keycloakAdminService, times(1)).createUser(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return BadRequest when username is missing")
    void testCreateUser_MissingUsername() {
        // Arrange
        CreateUserRequest request = createValidUserRequest();
        request.setUsername(null);

        // Act
        ResponseEntity<?> response = keycloakAdminController.createUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(keycloakAdminService, never()).createUser(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should get all users successfully")
    void testGetAllUsers_Success() {
        // Arrange
        List<Map<String, Object>> mockUsers = createMockUsers();
        when(keycloakAdminService.getAllUsers()).thenReturn(mockUsers);

        // Act
        ResponseEntity<?> response = keycloakAdminController.getAllUsers();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        verify(keycloakAdminService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser_Success() {
        // Arrange
        doNothing().when(keycloakAdminService).deleteUser(TEST_USER_ID);

        // Act
        ResponseEntity<?> response = keycloakAdminController.deleteUser(TEST_USER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).deleteUser(TEST_USER_ID);
    }

    // ============================== ROLE MANAGEMENT TESTS ==============================

    @Test
    @DisplayName("Should get all roles successfully")
    void testGetAllRoles_Success() {
        // Arrange
        List<Map<String, Object>> mockRoles = createMockRoles();
        when(keycloakAdminService.getAllRoles()).thenReturn(mockRoles);

        // Act
        ResponseEntity<?> response = keycloakAdminController.getAllRoles();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).getAllRoles();
    }

    @Test
    @DisplayName("Should create role successfully")
    void testCreateRole_Success() {
        // Arrange
        Map<String, String> request = Map.of("name", "NEW_ROLE", "description", "Test role");
        when(keycloakAdminService.createRole(anyString(), anyString())).thenReturn(TEST_ROLE_ID);

        // Act
        ResponseEntity<?> response = keycloakAdminController.createRole(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).createRole(anyString(), anyString());
    }

    @Test
    @DisplayName("Should assign role to user successfully")
    void testAssignRoleToUser_Success() {
        // Arrange
        Map<String, String> request = Map.of("roleName", "ADMIN");
        doNothing().when(keycloakAdminService).assignRoleToUser(TEST_USER_ID, "ADMIN");

        // Act
        ResponseEntity<?> response = keycloakAdminController.assignRoleToUser(TEST_USER_ID, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).assignRoleToUser(TEST_USER_ID, "ADMIN");
    }

    @Test
    @DisplayName("Should get user roles successfully")
    void testGetUserRoles_Success() {
        // Arrange
        List<Map<String, Object>> mockRoles = createMockRoles();
        when(keycloakAdminService.getUserRoles(TEST_USER_ID)).thenReturn(mockRoles);

        // Act
        ResponseEntity<?> response = keycloakAdminController.getUserRoles(TEST_USER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).getUserRoles(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should delete role successfully")
    void testDeleteRole_Success() {
        // Arrange
        doNothing().when(keycloakAdminService).deleteRole(TEST_ROLE_ID);

        // Act
        ResponseEntity<?> response = keycloakAdminController.deleteRole(TEST_ROLE_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).deleteRole(TEST_ROLE_ID);
    }

    // ============================== GROUP MANAGEMENT TESTS ==============================

    @Test
    @DisplayName("Should get all groups successfully")
    void testGetAllGroups_Success() {
        // Arrange
        List<Map<String, Object>> mockGroups = createMockGroups();
        when(keycloakAdminService.getAllGroups()).thenReturn(mockGroups);

        // Act
        ResponseEntity<?> response = keycloakAdminController.getAllGroups();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).getAllGroups();
    }

    @Test
    @DisplayName("Should create group successfully")
    void testCreateGroup_Success() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("name", "CTA");
        request.put("parentGroupId", null);
        when(keycloakAdminService.createGroup(anyString(), any())).thenReturn(TEST_GROUP_ID);

        // Act
        ResponseEntity<?> response = keycloakAdminController.createGroup(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).createGroup(anyString(), any());
    }

    @Test
    @DisplayName("Should add user to group successfully")
    void testAddUserToGroup_Success() {
        // Arrange
        doNothing().when(keycloakAdminService).addUserToGroup(TEST_USER_ID, TEST_GROUP_ID);

        // Act
        ResponseEntity<?> response = keycloakAdminController.addUserToGroup(TEST_USER_ID, TEST_GROUP_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).addUserToGroup(TEST_USER_ID, TEST_GROUP_ID);
    }

    @Test
    @DisplayName("Should get user groups successfully")
    void testGetUserGroups_Success() {
        // Arrange
        List<Map<String, Object>> mockGroups = createMockGroups();
        when(keycloakAdminService.getUserGroups(TEST_USER_ID)).thenReturn(mockGroups);

        // Act
        ResponseEntity<?> response = keycloakAdminController.getUserGroups(TEST_USER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).getUserGroups(TEST_USER_ID);
    }

    // ============================== POLICY AND PERMISSION TESTS ==============================

    @Test
    @DisplayName("Should get all policies successfully")
    void testGetAllPolicies_Success() {
        // Arrange
        List<Map<String, Object>> mockPolicies = createMockPolicies();
        when(keycloakAdminService.getAllPolicies()).thenReturn(mockPolicies);

        // Act
        ResponseEntity<?> response = keycloakAdminController.getAllPolicies();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).getAllPolicies();
    }

    @Test
    @DisplayName("Should create policy successfully")
    void testCreatePolicy_Success() {
        // Arrange
        CreatePolicyRequest request = createValidPolicyRequest();
        when(keycloakAdminService.createRolePolicy(anyString(), anyString(), anyString()))
                .thenReturn("policy-123");

        // Act
        ResponseEntity<?> response = keycloakAdminController.createPolicy(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(keycloakAdminService, times(1)).createRolePolicy(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should get all permissions successfully")
    void testGetAllPermissions_Success() {
        // Arrange
        List<Map<String, Object>> mockPermissions = createMockPermissions();
        when(keycloakAdminService.getAllPermissions()).thenReturn(mockPermissions);

        // Act
        ResponseEntity<?> response = keycloakAdminController.getAllPermissions();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).getAllPermissions();
    }

    @Test
    @DisplayName("Should create permission successfully")
    void testCreatePermission_Success() {
        // Arrange
        CreatePermissionRequest request = createValidPermissionRequest();
        when(keycloakAdminService.createScopePermission(anyString(), anyString(), anyString(), anyList(), anyList()))
                .thenReturn("permission-123");

        // Act
        ResponseEntity<?> response = keycloakAdminController.createPermission(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(keycloakAdminService, times(1)).createScopePermission(anyString(), anyString(), anyString(), anyList(), anyList());
    }

    // ============================== RESOURCE MANAGEMENT TESTS ==============================

    @Test
    @DisplayName("Should get all resources successfully")
    void testGetAllResources_Success() {
        // Arrange
        List<Map<String, Object>> mockResources = createMockResources();
        when(keycloakAdminService.getAllResources()).thenReturn(mockResources);

        // Act
        ResponseEntity<?> response = keycloakAdminController.getAllResources();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).getAllResources();
    }

    @Test
    @DisplayName("Should update resource attributes successfully")
    void testUpdateResourceAttributes_Success() {
        // Arrange
        Map<String, List<String>> attributes = Map.of(
                "role_scope_fields", Arrays.asList("id", "employeeName", "totalHours")
        );
        doNothing().when(keycloakAdminService).updateResourceAttributes(anyString(), anyMap());

        // Act
        ResponseEntity<?> response = keycloakAdminController.updateResourceAttributes(TEST_RESOURCE_ID, attributes);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(keycloakAdminService, times(1)).updateResourceAttributes(anyString(), anyMap());
    }

    // ============================== ERROR HANDLING TESTS ==============================

    @Test
    @DisplayName("Should handle service errors gracefully")
    void testCreateUser_ServiceError() {
        // Arrange
        CreateUserRequest request = createValidUserRequest();
        when(keycloakAdminService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Keycloak error"));

        // Act
        ResponseEntity<?> response = keycloakAdminController.createUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // Helper methods
    private CreateUserRequest createValidUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setRole("ADMIN");
        return request;
    }

    private List<Map<String, Object>> createMockUsers() {
        return Arrays.asList(
                Map.of("id", TEST_USER_ID, "username", "user1", "email", "user1@example.com"),
                Map.of("id", "user-456", "username", "user2", "email", "user2@example.com")
        );
    }

    private List<Map<String, Object>> createMockRoles() {
        return Arrays.asList(
                Map.of("id", TEST_ROLE_ID, "name", "ADMIN", "description", "Administrator role"),
                Map.of("id", "role-789", "name", "CASE_WORKER", "description", "Case worker role")
        );
    }

    private List<Map<String, Object>> createMockGroups() {
        return Arrays.asList(
                Map.of("id", TEST_GROUP_ID, "name", "CTA", "path", "/CTA"),
                Map.of("id", "group-456", "name", "CTB", "path", "/CTB")
        );
    }

    private List<Map<String, Object>> createMockPolicies() {
        return Arrays.asList(
                Map.of("id", "policy-123", "name", "Admin Policy", "type", "role")
        );
    }

    private List<Map<String, Object>> createMockPermissions() {
        return Arrays.asList(
                Map.of("id", "permission-123", "name", "Timesheet Read Permission", "type", "scope")
        );
    }

    private List<Map<String, Object>> createMockResources() {
        return Arrays.asList(
                Map.of("id", TEST_RESOURCE_ID, "name", "Timesheet Resource", "type", "urn:timesheet")
        );
    }

    private CreatePolicyRequest createValidPolicyRequest() {
        CreatePolicyRequest request = new CreatePolicyRequest();
        request.setName("Test Policy");
        request.setDescription("Test policy description");
        request.setRoleName("ADMIN");
        return request;
    }

    private CreatePermissionRequest createValidPermissionRequest() {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setName("Test Permission");
        request.setDescription("Test permission description");
        request.setResourceId(TEST_RESOURCE_ID);
        request.setScopeIds(Arrays.asList("read", "write"));
        request.setPolicyIds(Arrays.asList("policy-123"));
        return request;
    }
}

