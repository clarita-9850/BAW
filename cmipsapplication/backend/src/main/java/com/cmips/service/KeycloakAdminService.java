package com.cmips.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for interacting with Keycloak Admin API
 * Allows programmatic management of users, roles, policies, and permissions
 */
@Service
public class KeycloakAdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminService.class);
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.resource}")
    private String clientId;
    
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    
    private String adminToken;
    private long tokenExpiryTime = 0;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Get admin access token (cached and refreshed when needed)
     */
    private String getAdminToken() {
        // Check if token is still valid (with 10 second buffer)
        if (adminToken != null && System.currentTimeMillis() < tokenExpiryTime - 10000) {
            return adminToken;
        }
        
        logger.info("Getting new admin access token from Keycloak");
        
        try {
            // Request token using client credentials
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "client_credentials");
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);
            
            String tokenUrl = keycloakServerUrl + "realms/" + realm + "/protocol/openid-connect/token";
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                adminToken = (String) response.getBody().get("access_token");
                Integer expiresIn = (Integer) response.getBody().get("expires_in");
                tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000L);
                
                logger.info("Successfully obtained admin token, expires in {} seconds", expiresIn);
                return adminToken;
            }
            
            throw new RuntimeException("Failed to get admin token");
            
        } catch (Exception e) {
            logger.error("Error getting admin token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to authenticate with Keycloak admin API", e);
        }
    }
    
    /**
     * Create HTTP headers with admin authentication
     */
    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getAdminToken());
        return headers;
    }
    
    // ============================== USER MANAGEMENT ==============================
    
    /**
     * Create a new user in Keycloak
     */
    public String createUser(String username, String email, String password, String firstName, String lastName) {
        logger.info("Creating user: {}", username);
        
        try {
            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("username", username);
            userRequest.put("email", email);
            userRequest.put("enabled", true);
            userRequest.put("emailVerified", true);
            
            if (firstName != null) {
                userRequest.put("firstName", firstName);
            }
            if (lastName != null) {
                userRequest.put("lastName", lastName);
            }
            
            // Add credentials
            List<Map<String, Object>> credentials = new ArrayList<>();
            Map<String, Object> credential = new HashMap<>();
            credential.put("type", "password");
            credential.put("value", password);
            credential.put("temporary", false);
            credentials.add(credential);
            userRequest.put("credentials", credentials);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userRequest, getAuthHeaders());
            
            String url = keycloakServerUrl + "admin/realms/" + realm + "/users";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                // Extract user ID from Location header
                String location = response.getHeaders().getFirst("Location");
                String userId = location.substring(location.lastIndexOf('/') + 1);
                logger.info("Successfully created user with ID: {}", userId);
                return userId;
            }
            
            throw new RuntimeException("Failed to create user");
            
        } catch (HttpClientErrorException e) {
            logger.error("Error creating user: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    /**
     * Get all users
     */
    public List<Map<String, Object>> getAllUsers() {
        logger.info("Getting all users from Keycloak");
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/users";
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Retrieved {} users", response.getBody().size());
                return response.getBody();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error getting users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get users", e);
        }
    }
    
    /**
     * Delete a user
     */
    public void deleteUser(String userId) {
        logger.info("Deleting user: {}", userId);
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/users/" + userId;
            
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            logger.info("Successfully deleted user: {}", userId);
            
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }
    
    // ============================== ROLE MANAGEMENT ==============================
    
    /**
     * Get all realm roles
     */
    public List<Map<String, Object>> getAllRoles() {
        logger.info("Getting all realm roles");
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/roles";
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Retrieved {} roles", response.getBody().size());
                return response.getBody();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error getting roles: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get roles", e);
        }
    }
    
    /**
     * Get a specific role by name
     */
    public Map<String, Object> getRoleByName(String roleName) {
        logger.info("Getting role: {}", roleName);
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/roles/" + roleName;
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("Error getting role: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Create a new realm role
     */
    public String createRole(String roleName, String description) {
        logger.info("Creating role: {}", roleName);
        
        try {
            Map<String, Object> roleData = new HashMap<>();
            roleData.put("name", roleName);
            roleData.put("description", description != null ? description : "");
            roleData.put("composite", false);
            roleData.put("clientRole", false);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(roleData, getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/roles";
            
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                logger.info("Successfully created role: {}", roleName);
                return roleName; // Return role name as ID for simplicity
            }
            
            throw new RuntimeException("Failed to create role");
            
        } catch (Exception e) {
            logger.error("Error creating role: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create role: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete a realm role
     */
    public void deleteRole(String roleName) {
        logger.info("Deleting role: {}", roleName);
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/roles/" + roleName;
            
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                logger.info("Successfully deleted role: {}", roleName);
            } else {
                throw new RuntimeException("Failed to delete role");
            }
            
        } catch (Exception e) {
            logger.error("Error deleting role: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete role: " + e.getMessage(), e);
        }
    }

    /**
     * Assign realm role to user
     */
    public void assignRoleToUser(String userId, String roleName) {
        logger.info("Assigning role {} to user {}", roleName, userId);
        
        try {
            // First, get the role details
            Map<String, Object> role = getRoleByName(roleName);
            if (role == null) {
                throw new RuntimeException("Role not found: " + roleName);
            }
            
            // Assign role to user
            List<Map<String, Object>> roles = new ArrayList<>();
            roles.add(role);
            
            HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(roles, getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
            
            restTemplate.postForEntity(url, entity, Void.class);
            logger.info("Successfully assigned role {} to user {}", roleName, userId);
            
        } catch (Exception e) {
            logger.error("Error assigning role: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to assign role", e);
        }
    }
    
    /**
     * Get user's assigned roles
     */
    public List<Map<String, Object>> getUserRoles(String userId) {
        logger.info("Getting roles for user: {}", userId);
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error getting user roles: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get user roles", e);
        }
    }
    
    // ============================== AUTHORIZATION MANAGEMENT ==============================
    
    /**
     * Get client internal ID (needed for authorization API calls)
     * Uses query-clients permission to search for client by clientId
     */
    private String getClientInternalId() {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            // This endpoint requires 'query-clients' role, not just 'view-clients'
            String url = keycloakServerUrl + "admin/realms/" + realm + "/clients?clientId=" + clientId;
            
            logger.debug("Requesting client ID for: {}", clientId);
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().isEmpty()) {
                Map<String, Object> client = (Map<String, Object>) response.getBody().get(0);
                String internalId = (String) client.get("id");
                logger.info("Found client internal ID: {} for clientId: {}", internalId, clientId);
                return internalId;
            }
            
            throw new RuntimeException("Client not found: " + clientId);
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.error("403 Forbidden when getting client ID. Service account needs 'query-clients' role from realm-management.");
                logger.error("Current client: {}, Make sure 'realm-management query-clients' role is assigned to service account", clientId);
            }
            logger.error("Error getting client ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get client ID: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error getting client ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get client ID", e);
        }
    }
    
    /**
     * Get cmips-backend client internal ID (for authorization resources)
     * Authorization resources (policies, permissions, resources) are stored in cmips-backend client
     */
    private String getBackendClientInternalId() {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/clients?clientId=cmips-backend";
            
            logger.debug("Requesting client ID for: cmips-backend");
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().isEmpty()) {
                Map<String, Object> client = (Map<String, Object>) response.getBody().get(0);
                String internalId = (String) client.get("id");
                logger.info("Found backend client internal ID: {} for clientId: cmips-backend", internalId);
                return internalId;
            }
            
            throw new RuntimeException("Client not found: cmips-backend");
            
        } catch (Exception e) {
            logger.error("Error getting backend client ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get backend client ID", e);
        }
    }
    
    /**
     * Get all resources
     */
    public List<Map<String, Object>> getAllResources() {
        logger.info("Getting all resources");
        
        try {
            String clientInternalId = getBackendClientInternalId(); // Use cmips-backend for authorization resources
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + "/authz/resource-server/resource";
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Retrieved {} resources", response.getBody().size());
                return response.getBody();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error getting resources: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get resources", e);
        }
    }
    
    /**
     * Get all policies
     */
    public List<Map<String, Object>> getAllPolicies() {
        logger.info("Getting all policies");
        
        try {
            String clientInternalId = getBackendClientInternalId(); // Use cmips-backend for authorization resources
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + "/authz/resource-server/policy";
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Retrieved {} policies", response.getBody().size());
                return response.getBody();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error getting policies: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get policies", e);
        }
    }
    
    /**
     * Create a role-based policy
     */
    public String createRolePolicy(String policyName, String description, String roleName) {
        logger.info("Creating role policy: {}", policyName);
        
        try {
            // Get role details
            Map<String, Object> role = getRoleByName(roleName);
            if (role == null) {
                throw new RuntimeException("Role not found: " + roleName);
            }
            
            String clientInternalId = getBackendClientInternalId(); // Use cmips-backend for authorization resources
            
            Map<String, Object> policyRequest = new HashMap<>();
            policyRequest.put("name", policyName);
            policyRequest.put("description", description);
            policyRequest.put("type", "role");
            policyRequest.put("logic", "POSITIVE");
            policyRequest.put("decisionStrategy", "UNANIMOUS");
            
            // Add role to policy
            List<Map<String, Object>> roles = new ArrayList<>();
            Map<String, Object> roleConfig = new HashMap<>();
            roleConfig.put("id", role.get("id"));
            roleConfig.put("required", true);
            roles.add(roleConfig);
            policyRequest.put("roles", roles);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(policyRequest, getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + "/authz/resource-server/policy/role";
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                String policyId = (String) response.getBody().get("id");
                logger.info("Successfully created policy with ID: {}", policyId);
                return policyId;
            }
            
            throw new RuntimeException("Failed to create policy");
            
        } catch (Exception e) {
            logger.error("Error creating policy: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create policy", e);
        }
    }
    
    /**
     * Get all permissions
     */
    public List<Map<String, Object>> getAllPermissions() {
        logger.info("Getting all permissions");
        
        try {
            String clientInternalId = getBackendClientInternalId(); // Use cmips-backend for authorization resources
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + "/authz/resource-server/permission";
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Retrieved {} permissions", response.getBody().size());
                return response.getBody();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error getting permissions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get permissions", e);
        }
    }
    
    /**
     * Create a scope-based permission
     */
    public String createScopePermission(String permissionName, String description, 
                                       String resourceId, List<String> scopeIds, List<String> policyIds) {
        logger.info("Creating scope permission: {}", permissionName);
        
        try {
            String clientInternalId = getBackendClientInternalId(); // Use cmips-backend for authorization resources
            
            Map<String, Object> permissionRequest = new HashMap<>();
            permissionRequest.put("name", permissionName);
            permissionRequest.put("description", description);
            permissionRequest.put("type", "scope");
            permissionRequest.put("decisionStrategy", "AFFIRMATIVE");
            permissionRequest.put("resources", Collections.singletonList(resourceId));
            permissionRequest.put("scopes", scopeIds);
            permissionRequest.put("policies", policyIds);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(permissionRequest, getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + "/authz/resource-server/permission/scope";
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                String permissionId = (String) response.getBody().get("id");
                logger.info("Successfully created permission with ID: {}", permissionId);
                return permissionId;
            }
            
            throw new RuntimeException("Failed to create permission");
            
        } catch (Exception e) {
            logger.error("Error creating permission: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create permission", e);
        }
    }
    
    /**
     * Update resource attributes (for field-level permissions)
     */
    public void updateResourceAttributes(String resourceId, Map<String, List<String>> attributes) {
        logger.info("Updating resource attributes for resource: {}", resourceId);
        logger.info("Attributes to update: {}", attributes);
        
        try {
            String clientInternalId = getBackendClientInternalId(); // Use cmips-backend for authorization resources
            
            // First, get the existing resource
            HttpEntity<Void> getEntity = new HttpEntity<>(getAuthHeaders());
            String getUrl = keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + 
                           "/authz/resource-server/resource/" + resourceId;
            ResponseEntity<Map> getResponse = restTemplate.exchange(getUrl, HttpMethod.GET, getEntity, Map.class);
            
            if (getResponse.getStatusCode() == HttpStatus.OK && getResponse.getBody() != null) {
                Map<String, Object> resource = getResponse.getBody();
                
                // Log the current attributes to understand the format
                Object currentAttributes = resource.get("attributes");
                logger.info("Current attributes format: {}", currentAttributes);
                
                // Keycloak expects attributes as comma-separated strings, not arrays
                // This prevents Keycloak from creating multiple separate entries
                Map<String, Object> keycloakAttributes = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    
                    // Clean and deduplicate values
                    List<String> cleanValues = values.stream()
                        .map(String::trim)
                        .filter(v -> !v.isEmpty())
                        .distinct() // Remove duplicates
                        .collect(Collectors.toList());
                    
                    // Convert to comma-separated string to prevent multiple entries
                    String commaSeparatedValue = String.join(",", cleanValues);
                    keycloakAttributes.put(key, commaSeparatedValue);
                }
                
                logger.info("Converted attributes for Keycloak: {}", keycloakAttributes);
                resource.put("attributes", keycloakAttributes);
                
                // Update the resource
                HttpEntity<Map<String, Object>> putEntity = new HttpEntity<>(resource, getAuthHeaders());
                ResponseEntity<Void> putResponse = restTemplate.exchange(getUrl, HttpMethod.PUT, putEntity, Void.class);
                
                logger.info("Update response status: {}", putResponse.getStatusCode());
                logger.info("Successfully updated resource attributes");
            }
            
        } catch (Exception e) {
            logger.error("Error updating resource attributes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update resource attributes", e);
        }
    }
    
    /**
     * Get raw resource data from Keycloak (for debugging)
     */
    public Map<String, Object> getRawResource(String resourceId) {
        logger.info("Getting raw resource data for: {}", resourceId);
        
        try {
            String clientInternalId = getBackendClientInternalId(); // Use cmips-backend for authorization resources
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + 
                        "/authz/resource-server/resource/" + resourceId;
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Retrieved raw resource data");
                return response.getBody();
            }
            
            return new HashMap<>();
            
        } catch (Exception e) {
            logger.error("Error getting raw resource: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get raw resource", e);
        }
    }
    
    // ============================== GROUP MANAGEMENT ==============================
    
    /**
     * Get all groups
     */
    public List<Map<String, Object>> getAllGroups() {
        logger.info("Getting all groups");
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/groups";
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Retrieved {} groups", response.getBody().size());
                return response.getBody();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error getting groups: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get groups", e);
        }
    }
    
    /**
     * Create a new group
     */
    public String createGroup(String groupName, String parentGroupId) {
        logger.info("Creating group: {} with parent: {}", groupName, parentGroupId);
        
        try {
            Map<String, Object> groupRequest = new HashMap<>();
            groupRequest.put("name", groupName);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(groupRequest, getAuthHeaders());
            String url;
            
            if (parentGroupId != null && !parentGroupId.isEmpty()) {
                url = keycloakServerUrl + "admin/realms/" + realm + "/groups/" + parentGroupId + "/children";
            } else {
                url = keycloakServerUrl + "admin/realms/" + realm + "/groups";
            }
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                String groupId = (String) response.getBody().get("id");
                logger.info("Successfully created group with ID: {}", groupId);
                return groupId;
            }
            
            // If creation returns 201 with Location header, extract ID from header
            String location = response.getHeaders().getFirst("Location");
            if (location != null) {
                String groupId = location.substring(location.lastIndexOf('/') + 1);
                logger.info("Successfully created group with ID: {} (from Location header)", groupId);
                return groupId;
            }
            
            throw new RuntimeException("Failed to create group");
            
        } catch (Exception e) {
            logger.error("Error creating group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create group", e);
        }
    }
    
    /**
     * Delete a group
     */
    public void deleteGroup(String groupId) {
        logger.info("Deleting group: {}", groupId);
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/groups/" + groupId;
            
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            logger.info("Successfully deleted group: {}", groupId);
            
        } catch (Exception e) {
            logger.error("Error deleting group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete group", e);
        }
    }
    
    /**
     * Get group by name
     */
    public String getGroupByName(String groupName) {
        logger.info("Getting group by name: {}", groupName);
        
        try {
            List<Map<String, Object>> groups = getAllGroups();
            return findGroupByName(groups, groupName);
            
        } catch (Exception e) {
            logger.error("Error getting group by name: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Recursively find group by name in the groups tree
     */
    private String findGroupByName(List<Map<String, Object>> groups, String groupName) {
        for (Map<String, Object> group : groups) {
            if (groupName.equals(group.get("name"))) {
                return (String) group.get("id");
            }
            
            // Check sub-groups
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> subGroups = (List<Map<String, Object>>) group.get("subGroups");
            if (subGroups != null && !subGroups.isEmpty()) {
                String found = findGroupByName(subGroups, groupName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    /**
     * Get group members
     */
    public List<Map<String, Object>> getGroupMembers(String groupId) {
        logger.info("Getting members for group: {}", groupId);
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/groups/" + groupId + "/members";
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Retrieved {} members for group {}", response.getBody().size(), groupId);
                return response.getBody();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error getting group members: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get group members", e);
        }
    }
    
    /**
     * Add user to group
     */
    public void addUserToGroup(String userId, String groupId) {
        logger.info("Adding user {} to group {}", userId, groupId);
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/users/" + userId + "/groups/" + groupId;
            
            restTemplate.put(url, entity);
            logger.info("Successfully added user {} to group {}", userId, groupId);
            
        } catch (Exception e) {
            logger.error("Error adding user to group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add user to group", e);
        }
    }
    
    /**
     * Remove user from group
     */
    public void removeUserFromGroup(String userId, String groupId) {
        logger.info("Removing user {} from group {}", userId, groupId);
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/users/" + userId + "/groups/" + groupId;
            
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            logger.info("Successfully removed user {} from group {}", userId, groupId);
            
        } catch (Exception e) {
            logger.error("Error removing user from group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove user from group", e);
        }
    }
    
    /**
     * Get user's groups
     */
    public List<Map<String, Object>> getUserGroups(String userId) {
        logger.info("Getting groups for user: {}", userId);
        
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            String url = keycloakServerUrl + "admin/realms/" + realm + "/users/" + userId + "/groups";
            
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Retrieved {} groups for user {}", response.getBody().size(), userId);
                return response.getBody();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error getting user groups: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get user groups", e);
        }
    }
}


