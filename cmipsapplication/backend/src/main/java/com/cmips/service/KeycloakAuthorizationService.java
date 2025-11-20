package com.cmips.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class KeycloakAuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakAuthorizationService.class);

    public boolean hasPermission(String accessToken, String resourceName, String scope) {
        return hasTimesheetPermission(accessToken, scope);
    }

    public boolean hasTimesheetPermission(String accessToken, String scope) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                
                // Log the entire JWT for debugging
                logger.info("JWT claims: {}", jwt.getClaims());
                
                // Try different ways to extract roles
                List<String> roles = null;
                
                // Method 1: Try realm_access.roles
                try {
                    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                    if (realmAccess != null) {
                        roles = (List<String>) realmAccess.get("roles");
                        logger.info("Roles from realm_access: {}", roles);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to extract roles from realm_access: {}", e.getMessage());
                }
                
                // Method 2: Try direct roles claim
                if (roles == null) {
                    try {
                        roles = jwt.getClaimAsStringList("roles");
                        logger.info("Roles from direct roles claim: {}", roles);
                    } catch (Exception e) {
                        logger.warn("Failed to extract roles from direct roles claim: {}", e.getMessage());
                    }
                }
                
                // Method 3: Try resource_access
                if (roles == null) {
                    try {
                        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
                        if (resourceAccess != null) {
                            Map<String, Object> cmipsFrontend = (Map<String, Object>) resourceAccess.get("cmips-frontend");
                            if (cmipsFrontend != null) {
                                roles = (List<String>) cmipsFrontend.get("roles");
                                logger.info("Roles from resource_access.cmips-frontend: {}", roles);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to extract roles from resource_access: {}", e.getMessage());
                    }
                }
                
                if (roles == null || roles.isEmpty()) {
                    logger.warn("No roles found in JWT token");
                    return false;
                }
                
                logger.info("Final roles: {}", roles);
                logger.info("Checking permission for scope: {}", scope);
                
                // Check permissions based on roles and scope
                switch (scope) {
                    case "create":
                        if (roles.contains("PROVIDER")) {
                            logger.info("✅ PROVIDER can create timesheets");
                            return true;
                        }
                        break;
                    case "read":
                        if (roles.contains("PROVIDER") || roles.contains("RECIPIENT") || roles.contains("CASE_WORKER")) {
                            logger.info("✅ User can read timesheets");
                            return true;
                        }
                        break;
                    case "submit":
                        if (roles.contains("PROVIDER")) {
                            logger.info("✅ PROVIDER can submit timesheets");
                            return true;
                        }
                        break;
                    case "approve":
                        if (roles.contains("RECIPIENT") || roles.contains("CASE_WORKER")) {
                            logger.info("✅ User can approve timesheets");
                            return true;
                        }
                        break;
                    case "reject":
                        if (roles.contains("CASE_WORKER")) {
                            logger.info("✅ CASE_WORKER can reject timesheets");
                            return true;
                        }
                        break;
                    default:
                        logger.warn("Unknown scope: {}", scope);
                        return false;
                }
                
                logger.warn("❌ Access denied for scope: {} with roles: {}", scope, roles);
                return false;
            }
            
            logger.warn("No valid JWT token found");
            return false;
            
        } catch (Exception e) {
            logger.error("Error checking permission for scope '{}': {}", scope, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if the user has a specific role
     * 
     * @param roles Set of user roles
     * @param role The role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(Set<String> roles, String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Checks if the user has any of the specified roles
     * 
     * @param roles Set of user roles
     * @param roleNames The roles to check
     * @return true if user has any of the roles, false otherwise
     */
    public boolean hasAnyRole(Set<String> roles, String... roleNames) {
        if (roles == null) {
            return false;
        }
        for (String role : roleNames) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
