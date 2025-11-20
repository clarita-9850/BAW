package com.cmips.service;

import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.List;

/**
 * Generic Keycloak Policy Evaluation Service
 * 
 * This service handles ALL authorization decisions by querying Keycloak's
 * Authorization Services. It provides a low-code approach where all
 * authorization logic is managed in Keycloak policies, not in the backend code.
 * 
 * The backend only executes business logic - all authorization decisions
 * are made by Keycloak's policy evaluation engine.
 */
@Service
public class KeycloakPolicyEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakPolicyEvaluationService.class);

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    /**
     * Evaluates if the current user has permission for a specific resource and scope
     * This is the PRIMARY authorization method - all authorization decisions go through here
     * 
     * @param resourceName The resource name (e.g., "Timesheet Resource", "EVV Resource")
     * @param scope The scope/action (e.g., "create", "read", "update", "delete", "approve")
     * @return true if Keycloak grants permission, false otherwise
     */
    public boolean evaluatePermission(String resourceName, String scope) {
        logger.info("Evaluating permission: {}:{} for current user", resourceName, scope);
        
        try {
            String accessToken = getCurrentUserAccessToken();
            if (accessToken == null) {
                logger.warn("No access token found - denying access");
                return false;
            }

            // Query Keycloak Authorization Services for permission
            boolean hasPermission = queryKeycloakForPermission(accessToken, resourceName, scope);
            
            logger.info("Permission evaluation result: {}:{} = {}", resourceName, scope, hasPermission ? "GRANTED" : "DENIED");
            return hasPermission;

        } catch (Exception e) {
            logger.error("Error evaluating permission for {}:{} - {}", resourceName, scope, e.getMessage(), e);
            // Fail securely - deny access if Keycloak is unavailable
            return false;
        }
    }

    /**
     * Gets all granted scopes for a specific resource
     * Useful for field-level authorization and dynamic UI rendering
     * 
     * @param resourceName The resource name
     * @return Set of granted scopes for the resource
     */
    public Set<String> getGrantedScopes(String resourceName) {
        logger.info("Getting granted scopes for resource: {}", resourceName);
        
        try {
            String accessToken = getCurrentUserAccessToken();
            if (accessToken == null) {
                logger.warn("No access token found - returning empty scopes");
                return new HashSet<>();
            }

            // Query Keycloak for all granted scopes for this resource
            Set<String> grantedScopes = queryKeycloakForGrantedScopes(accessToken, resourceName);
            
            logger.info("Granted scopes for {}: {}", resourceName, grantedScopes);
            return grantedScopes;

        } catch (Exception e) {
            logger.error("Error getting granted scopes for {} - {}", resourceName, e.getMessage(), e);
            return new HashSet<>();
        }
    }

    /**
     * Evaluates multiple permissions at once
     * Useful for complex authorization scenarios
     * 
     * @param resourceName The resource name
     * @param scopes Array of scopes to evaluate
     * @return true if ALL scopes are granted, false if ANY scope is denied
     */
    public boolean evaluateAllPermissions(String resourceName, String... scopes) {
        logger.info("Evaluating multiple permissions for {}: {}", resourceName, String.join(", ", scopes));
        
        for (String scope : scopes) {
            if (!evaluatePermission(resourceName, scope)) {
                logger.info("Permission denied for scope: {}:{}", resourceName, scope);
                return false;
            }
        }
        
        logger.info("All permissions granted for {}: {}", resourceName, String.join(", ", scopes));
        return true;
    }

    /**
     * Evaluates if user has ANY of the specified permissions
     * 
     * @param resourceName The resource name
     * @param scopes Array of scopes to evaluate
     * @return true if ANY scope is granted, false if ALL scopes are denied
     */
    public boolean evaluateAnyPermission(String resourceName, String... scopes) {
        logger.info("Evaluating any permission for {}: {}", resourceName, String.join(", ", scopes));
        
        for (String scope : scopes) {
            if (evaluatePermission(resourceName, scope)) {
                logger.info("Permission granted for scope: {}:{}", resourceName, scope);
                return true;
            }
        }
        
        logger.info("No permissions granted for {}: {}", resourceName, String.join(", ", scopes));
        return false;
    }

    /**
     * Gets the current user's access token from Spring Security context
     * 
     * @return Access token string or null if not found
     */
    private String getCurrentUserAccessToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getTokenValue();
            }
        } catch (Exception e) {
            logger.error("Error extracting access token from JWT", e);
        }
        return null;
    }

    /**
     * Queries Keycloak Authorization Services for a specific permission
     * This is where the actual authorization decision is made by Keycloak
     * 
     * @param accessToken User's access token
     * @param resourceName The resource name
     * @param scope The scope/action
     * @return true if Keycloak grants the permission
     */
    private boolean queryKeycloakForPermission(String accessToken, String resourceName, String scope) {
        try {
            // Configure Keycloak Authorization Services client
            Configuration configuration = new Configuration(
                keycloakServerUrl,
                realm,
                clientId,
                Map.of("secret", clientSecret),
                null
            );
            
            AuthzClient authzClient = AuthzClient.create(configuration);
            
            // Request authorization for specific resource and scope
            AuthorizationRequest request = new AuthorizationRequest();
            request.setAudience(clientId);
            request.addPermission(resourceName + "#" + scope);
            
            // Query Keycloak for authorization decision
            AuthorizationResponse response = authzClient.authorization(accessToken).authorize(request);
            
            if (response != null && response.getToken() != null) {
                logger.debug("Keycloak granted permission for {}:{}", resourceName, scope);
                return true;
            }
            
            logger.debug("Keycloak denied permission for {}:{}", resourceName, scope);
            return false;
            
        } catch (Exception e) {
            logger.warn("Keycloak authorization request failed for {}:{} - {}", resourceName, scope, e.getMessage());
            return false;
        }
    }

    /**
     * Queries Keycloak for all granted scopes for a resource
     * 
     * @param accessToken User's access token
     * @param resourceName The resource name
     * @return Set of granted scopes
     */
    private Set<String> queryKeycloakForGrantedScopes(String accessToken, String resourceName) {
        Set<String> grantedScopes = new HashSet<>();
        
        try {
            // Configure Keycloak Authorization Services client
            Configuration configuration = new Configuration(
                keycloakServerUrl,
                realm,
                clientId,
                Map.of("secret", clientSecret),
                null
            );
            
            AuthzClient authzClient = AuthzClient.create(configuration);
            
            // Request authorization for the resource (all scopes)
            AuthorizationRequest request = new AuthorizationRequest();
            request.setAudience(clientId);
            request.addPermission(resourceName);
            
            // Query Keycloak for authorization decision
            AuthorizationResponse response = authzClient.authorization(accessToken).authorize(request);
            
            if (response != null && response.getToken() != null) {
                // Parse the authorization token to extract granted scopes
                grantedScopes = extractScopesFromAuthToken(response.getToken());
            }
            
        } catch (Exception e) {
            logger.warn("Error querying Keycloak for granted scopes: {}", e.getMessage());
        }
        
        return grantedScopes;
    }

    /**
     * Extracts scopes from Keycloak's authorization token
     * 
     * @param authToken Authorization token from Keycloak
     * @return Set of granted scopes
     */
    private Set<String> extractScopesFromAuthToken(String authToken) {
        Set<String> scopes = new HashSet<>();
        
        try {
            // Parse JWT token to extract authorization information
            String[] tokenParts = authToken.split("\\.");
            if (tokenParts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[1]));
                Map<String, Object> claims = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class);
                
                Map<String, Object> authorization = (Map<String, Object>) claims.get("authorization");
                if (authorization != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> permissions = (List<Map<String, Object>>) authorization.get("permissions");
                    
                    if (permissions != null) {
                        for (Map<String, Object> permission : permissions) {
                            @SuppressWarnings("unchecked")
                            List<String> permissionScopes = (List<String>) permission.get("scopes");
                            if (permissionScopes != null) {
                                scopes.addAll(permissionScopes);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing authorization token", e);
        }
        
        return scopes;
    }

    /**
     * Gets the current user ID from the JWT token
     * 
     * @return User ID or null if not found
     */
    public String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getSubject();
            }
        } catch (Exception e) {
            logger.error("Error extracting user ID from JWT", e);
        }
        return null;
    }

    /**
     * Gets the current user's roles from the JWT token
     * 
     * @return Set of user roles
     */
    public Set<String> getCurrentUserRoles() {
        Set<String> roles = new HashSet<>();
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                if (realmAccess != null && realmAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> rolesList = (List<String>) realmAccess.get("roles");
                    roles.addAll(rolesList);
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting roles from JWT", e);
        }
        
        return roles;
    }
}
