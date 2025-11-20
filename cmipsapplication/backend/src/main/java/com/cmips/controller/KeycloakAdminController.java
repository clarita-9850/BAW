package com.cmips.controller;

import com.cmips.dto.CreatePermissionRequest;
import com.cmips.dto.CreatePolicyRequest;
import com.cmips.dto.CreateUserRequest;
import com.cmips.service.KeycloakAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Keycloak Administration
 * Provides user-friendly endpoints to manage Keycloak programmatically
 * 
 * Security: Only accessible to users with ADMIN or CASE_WORKER roles
 */
@RestController
@RequestMapping("/api/admin/keycloak")
@CrossOrigin(origins = "*")
public class KeycloakAdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminController.class);
    
    @Autowired
    private KeycloakAdminService keycloakAdminService;
    
    // ============================== USER MANAGEMENT ==============================
    
    /**
     * Create a new user
     * POST /api/admin/keycloak/users
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            logger.info("Admin request to create user: {}", request.getUsername());
            
            // Validate request
            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
            }
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }
            
            // Create user
            String userId = keycloakAdminService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
            );
            
            // Assign role if provided
            if (request.getRole() != null && !request.getRole().isEmpty()) {
                keycloakAdminService.assignRoleToUser(userId, request.getRole());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User created successfully");
            response.put("userId", userId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create user", "message", e.getMessage()));
        }
    }
    
    /**
     * Get all users
     * GET /api/admin/keycloak/users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            logger.info("Admin request to get all users");
            List<Map<String, Object>> users = keycloakAdminService.getAllUsers();
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            logger.error("Error getting users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get users", "message", e.getMessage()));
        }
    }
    
    /**
     * Delete a user
     * DELETE /api/admin/keycloak/users/{userId}
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            logger.info("Admin request to delete user: {}", userId);
            keycloakAdminService.deleteUser(userId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "User deleted successfully"));
            
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete user", "message", e.getMessage()));
        }
    }
    
    // ============================== ROLE MANAGEMENT ==============================
    
    /**
     * Get all roles
     * GET /api/admin/keycloak/roles
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        try {
            logger.info("Admin request to get all roles");
            List<Map<String, Object>> roles = keycloakAdminService.getAllRoles();
            return ResponseEntity.ok(roles);
            
        } catch (Exception e) {
            logger.error("Error getting roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get roles", "message", e.getMessage()));
        }
    }
    
    /**
     * Assign role to user
     * POST /api/admin/keycloak/users/{userId}/roles
     */
    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<?> assignRoleToUser(@PathVariable String userId, @RequestBody Map<String, String> request) {
        try {
            String roleName = request.get("roleName");
            if (roleName == null || roleName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Role name is required"));
            }
            
            logger.info("Admin request to assign role {} to user {}", roleName, userId);
            keycloakAdminService.assignRoleToUser(userId, roleName);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Role assigned successfully"));
            
        } catch (Exception e) {
            logger.error("Error assigning role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to assign role", "message", e.getMessage()));
        }
    }
    
    /**
     * Get user's roles
     * GET /api/admin/keycloak/users/{userId}/roles
     */
    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable String userId) {
        try {
            logger.info("Admin request to get roles for user: {}", userId);
            List<Map<String, Object>> roles = keycloakAdminService.getUserRoles(userId);
            return ResponseEntity.ok(roles);
            
        } catch (Exception e) {
            logger.error("Error getting user roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get user roles", "message", e.getMessage()));
        }
    }
    
    // ============================== AUTHORIZATION MANAGEMENT ==============================
    
    /**
     * Get all resources
     * GET /api/admin/keycloak/resources
     */
    @GetMapping("/resources")
    public ResponseEntity<?> getAllResources() {
        try {
            logger.info("Admin request to get all resources");
            List<Map<String, Object>> resources = keycloakAdminService.getAllResources();
            return ResponseEntity.ok(resources);
            
        } catch (Exception e) {
            logger.error("Error getting resources: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get resources", "message", e.getMessage()));
        }
    }
    
    // ============================== ROLE MANAGEMENT ==============================
    
    /**
     * Create a new role
     * POST /api/admin/keycloak/roles
     */
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody Map<String, String> request) {
        try {
            logger.info("Admin request to create role: {}", request.get("name"));
            
            // Validate request
            if (request.get("name") == null || request.get("name").isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Role name is required"));
            }
            
            String roleId = keycloakAdminService.createRole(
                request.get("name"),
                request.get("description")
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Role created successfully");
            response.put("roleId", roleId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create role", "message", e.getMessage()));
        }
    }
    
    /**
     * Delete a role
     * DELETE /api/admin/keycloak/roles/{roleId}
     */
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<?> deleteRole(@PathVariable String roleId) {
        try {
            logger.info("Admin request to delete role: {}", roleId);
            
            keycloakAdminService.deleteRole(roleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Role deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete role", "message", e.getMessage()));
        }
    }
    
    /**
     * Get all policies
     * GET /api/admin/keycloak/policies
     */
    @GetMapping("/policies")
    public ResponseEntity<?> getAllPolicies() {
        try {
            logger.info("Admin request to get all policies");
            List<Map<String, Object>> policies = keycloakAdminService.getAllPolicies();
            return ResponseEntity.ok(policies);
            
        } catch (Exception e) {
            logger.error("Error getting policies: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get policies", "message", e.getMessage()));
        }
    }
    
    /**
     * Create a role-based policy
     * POST /api/admin/keycloak/policies
     */
    @PostMapping("/policies")
    public ResponseEntity<?> createPolicy(@RequestBody CreatePolicyRequest request) {
        try {
            logger.info("Admin request to create policy: {}", request.getName());
            
            // Validate request
            if (request.getName() == null || request.getName().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Policy name is required"));
            }
            if (request.getRoleName() == null || request.getRoleName().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Role name is required"));
            }
            
            String policyId = keycloakAdminService.createRolePolicy(
                request.getName(),
                request.getDescription(),
                request.getRoleName()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Policy created successfully");
            response.put("policyId", policyId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error creating policy: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create policy", "message", e.getMessage()));
        }
    }
    
    /**
     * Get all permissions
     * GET /api/admin/keycloak/permissions
     */
    @GetMapping("/permissions")
    public ResponseEntity<?> getAllPermissions() {
        try {
            logger.info("Admin request to get all permissions");
            List<Map<String, Object>> permissions = keycloakAdminService.getAllPermissions();
            return ResponseEntity.ok(permissions);
            
        } catch (Exception e) {
            logger.error("Error getting permissions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get permissions", "message", e.getMessage()));
        }
    }
    
    /**
     * Create a scope-based permission
     * POST /api/admin/keycloak/permissions
     */
    @PostMapping("/permissions")
    public ResponseEntity<?> createPermission(@RequestBody CreatePermissionRequest request) {
        try {
            logger.info("Admin request to create permission: {}", request.getName());
            
            // Validate request
            if (request.getName() == null || request.getName().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Permission name is required"));
            }
            if (request.getResourceId() == null || request.getResourceId().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Resource ID is required"));
            }
            if (request.getScopeIds() == null || request.getScopeIds().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "At least one scope is required"));
            }
            if (request.getPolicyIds() == null || request.getPolicyIds().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "At least one policy is required"));
            }
            
            String permissionId = keycloakAdminService.createScopePermission(
                request.getName(),
                request.getDescription(),
                request.getResourceId(),
                request.getScopeIds(),
                request.getPolicyIds()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Permission created successfully");
            response.put("permissionId", permissionId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error creating permission: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create permission", "message", e.getMessage()));
        }
    }
    
    /**
     * Update resource attributes (for field-level permissions)
     * PUT /api/admin/keycloak/resources/{resourceId}/attributes
     */
    @PutMapping("/resources/{resourceId}/attributes")
    public ResponseEntity<?> updateResourceAttributes(
            @PathVariable String resourceId,
            @RequestBody Map<String, List<String>> attributes) {
        try {
            logger.info("Admin request to update resource attributes for: {}", resourceId);
            keycloakAdminService.updateResourceAttributes(resourceId, attributes);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Resource attributes updated successfully"));
            
        } catch (Exception e) {
            logger.error("Error updating resource attributes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update resource attributes", "message", e.getMessage()));
        }
    }
    
    /**
     * Test endpoint to verify KeycloakAdminService is working
     * GET /api/admin/keycloak/test
     */
    @GetMapping("/test")
    public ResponseEntity<?> testAdminService() {
        try {
            logger.info("Testing KeycloakAdminService");
            
            // Test getting users
            List<Map<String, Object>> users = keycloakAdminService.getAllUsers();
            List<Map<String, Object>> roles = keycloakAdminService.getAllRoles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "KeycloakAdminService is working!");
            response.put("userCount", users.size());
            response.put("roleCount", roles.size());
            response.put("users", users);
            response.put("roles", roles);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error testing admin service: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "KeycloakAdminService test failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Test endpoint to examine Keycloak resource format
     * GET /api/admin/keycloak/test-resource-format/{resourceId}
     */
    @GetMapping("/test-resource-format/{resourceId}")
    public ResponseEntity<?> testResourceFormat(@PathVariable String resourceId) {
        try {
            logger.info("Testing resource format for: {}", resourceId);
            
            // Get the raw resource from Keycloak
            Map<String, Object> rawResource = keycloakAdminService.getRawResource(resourceId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "rawResource", rawResource,
                "attributes", rawResource.get("attributes"),
                "attributesType", rawResource.get("attributes") != null ? rawResource.get("attributes").getClass().getSimpleName() : "null"
            ));
            
        } catch (Exception e) {
            logger.error("Error testing resource format: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to test resource format", "message", e.getMessage()));
        }
    }
    
    // ============================== GROUP MANAGEMENT ==============================
    
    /**
     * Get all groups
     * GET /api/admin/keycloak/groups
     */
    @GetMapping("/groups")
    public ResponseEntity<?> getAllGroups() {
        try {
            List<Map<String, Object>> groups = keycloakAdminService.getAllGroups();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            logger.error("Error getting groups: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get groups", "message", e.getMessage()));
        }
    }
    
    /**
     * Create a new group
     * POST /api/admin/keycloak/groups
     */
    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> request) {
        try {
            String groupName = (String) request.get("name");
            String parentGroupId = (String) request.get("parentGroupId");
            
            if (groupName == null || groupName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Group name is required"));
            }
            
            String groupId = keycloakAdminService.createGroup(groupName, parentGroupId);
            return ResponseEntity.ok(Map.of("success", true, "groupId", groupId, "message", "Group created successfully"));
            
        } catch (Exception e) {
            logger.error("Error creating group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create group", "message", e.getMessage()));
        }
    }
    
    /**
     * Delete a group
     * DELETE /api/admin/keycloak/groups/{groupId}
     */
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable String groupId) {
        try {
            keycloakAdminService.deleteGroup(groupId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Group deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete group", "message", e.getMessage()));
        }
    }
    
    /**
     * Get group members
     * GET /api/admin/keycloak/groups/{groupId}/members
     */
    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable String groupId) {
        try {
            List<Map<String, Object>> members = keycloakAdminService.getGroupMembers(groupId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            logger.error("Error getting group members: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get group members", "message", e.getMessage()));
        }
    }
    
    /**
     * Add user to group
     * PUT /api/admin/keycloak/users/{userId}/groups/{groupId}
     */
    @PutMapping("/users/{userId}/groups/{groupId}")
    public ResponseEntity<?> addUserToGroup(@PathVariable String userId, @PathVariable String groupId) {
        try {
            keycloakAdminService.addUserToGroup(userId, groupId);
            return ResponseEntity.ok(Map.of("success", true, "message", "User added to group successfully"));
        } catch (Exception e) {
            logger.error("Error adding user to group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to add user to group", "message", e.getMessage()));
        }
    }
    
    /**
     * Remove user from group
     * DELETE /api/admin/keycloak/users/{userId}/groups/{groupId}
     */
    @DeleteMapping("/users/{userId}/groups/{groupId}")
    public ResponseEntity<?> removeUserFromGroup(@PathVariable String userId, @PathVariable String groupId) {
        try {
            keycloakAdminService.removeUserFromGroup(userId, groupId);
            return ResponseEntity.ok(Map.of("success", true, "message", "User removed from group successfully"));
        } catch (Exception e) {
            logger.error("Error removing user from group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to remove user from group", "message", e.getMessage()));
        }
    }
    
    /**
     * Get user's groups
     * GET /api/admin/keycloak/users/{userId}/groups
     */
    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<?> getUserGroups(@PathVariable String userId) {
        try {
            List<Map<String, Object>> groups = keycloakAdminService.getUserGroups(userId);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            logger.error("Error getting user groups: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get user groups", "message", e.getMessage()));
        }
    }
}

