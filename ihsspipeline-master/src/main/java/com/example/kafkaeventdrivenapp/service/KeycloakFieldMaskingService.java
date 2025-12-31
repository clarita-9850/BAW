package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.FieldMaskingRule;
import com.example.kafkaeventdrivenapp.model.FieldMaskingRules;
import com.example.kafkaeventdrivenapp.model.UserRole;
import com.example.kafkaeventdrivenapp.util.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakFieldMaskingService {

    @Autowired
    private JwtDecoder jwtDecoder;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${KEYCLOAK_BASE_URL:http://cmips-keycloak:8080}")
    private String keycloakBaseUrl;
    @Value("${KEYCLOAK_REALM:cmips}")
    private String keycloakRealm;
    @Value("${keycloak.client-uuid:a7c600a7-1cad-4723-a28f-b8f5aeec9439}")
    private String keycloakClientUuid;
    @Value("${KEYCLOAK_ADMIN_USER:admin}")
    private String keycloakAdminUser;
    @Value("${KEYCLOAK_ADMIN_PASSWORD:admin123}")
    private String keycloakAdminPassword;
    
    private String adminAccessToken = null;
    private long tokenExpiryTime = 0;

    /**
     * Extract field masking rules from JWT token
     * OPTIMIZED APPROACH (Protocol Mapper + Admin API fallback):
     * 1. First tries to extract rules from JWT claims (Protocol Mapper approach - fast, no API calls)
     * 2. Falls back to Keycloak Admin API if JWT doesn't contain rules (backward compatibility)
     *
     * This hybrid approach provides best performance when Protocol Mappers are configured,
     * while maintaining compatibility with existing deployments.
     */
    public FieldMaskingRules getMaskingRulesFromToken(String jwtToken, String reportType) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔐 KeycloakFieldMaskingService.getMaskingRulesFromToken() START");
        System.out.println("🔐 JWT Token provided: " + (jwtToken != null ? "YES (length: " + jwtToken.length() + ")" : "NO"));
        System.out.println("🔐 JWT Token (first 100 chars): " + (jwtToken != null ? jwtToken.substring(0, Math.min(100, jwtToken.length())) + "..." : "NULL"));
        System.out.println("🔐 Report Type: " + reportType);
        
        try {
            // Decode JWT token
            System.out.println("🔐 STEP 1: Decoding JWT token...");
            Jwt jwt = jwtDecoder.decode(jwtToken);
            System.out.println("✅ STEP 1: JWT token decoded successfully");
            System.out.println("🔐 JWT Claims: " + jwt.getClaims().keySet());
            
            // Extract user role
            System.out.println("🔐 STEP 2: Extracting user role from JWT...");
            UserRole userRole = extractUserRole(jwt);
            System.out.println("✅ STEP 2: Extracted user role: " + userRole);
            System.out.println("🔐 UserRole enum value: " + (userRole != null ? userRole.name() : "NULL"));
            
            // STEP 3A: Try to extract field masking rules from JWT claims FIRST (Protocol Mapper approach)
            System.out.println("🔐 STEP 3A: Trying to extract field masking rules from JWT claims...");
            Map<String, Object> rulesMap = extractFieldMaskingRulesFromJWT(jwt);

            // STEP 3B: Fallback to Keycloak Admin API if JWT doesn't have rules
            if (rulesMap == null || rulesMap.isEmpty()) {
                System.out.println("⚠️ STEP 3A: No field masking rules found in JWT claims");
                System.out.println("🔐 STEP 3B: Fetching role attributes from Keycloak Admin API (fallback)...");
                System.out.println("🔐 Keycloak Base URL: " + keycloakBaseUrl);
                System.out.println("🔐 Keycloak Realm: " + keycloakRealm);
                rulesMap = fetchRoleAttributesFromKeycloak(userRole);
                System.out.println("✅ STEP 3B: Fetched rules map from Keycloak Admin API: " + (rulesMap != null ? "SUCCESS (size: " + rulesMap.size() + ")" : "NULL/EMPTY"));
                if (rulesMap != null) {
                    System.out.println("🔐 Rules map keys: " + rulesMap.keySet());
                    System.out.println("🔐 Rules map content: " + rulesMap);
                }
            } else {
                System.out.println("✅ STEP 3A: Using field masking rules from JWT claims (Protocol Mapper approach)");
                System.out.println("✅ Rules found in JWT - NO Admin API call needed!");
                System.out.println("🔐 Rules map keys: " + rulesMap.keySet());
                System.out.println("🔐 Rules map content: " + rulesMap);
            }
            
            // Convert to FieldMaskingRule objects
            System.out.println("🔐 STEP 4: Converting rules map to FieldMaskingRule objects...");
            List<FieldMaskingRule> rules = convertToFieldMaskingRules(rulesMap, reportType);
            System.out.println("✅ STEP 4: Converted to " + rules.size() + " FieldMaskingRule objects");
            for (int i = 0; i < rules.size(); i++) {
                FieldMaskingRule rule = rules.get(i);
                System.out.println("  Rule " + (i+1) + ": field=" + rule.getFieldName() + ", masking=" + rule.getMaskingType() + ", access=" + rule.getAccessLevel() + ", enabled=" + rule.isEnabled());
            }
            
            // Create FieldMaskingRules object
            System.out.println("🔐 STEP 5: Creating FieldMaskingRules object...");
            FieldMaskingRules maskingRules = new FieldMaskingRules();
            maskingRules.setUserRole(userRole.name());
            maskingRules.setReportType(reportType);
            maskingRules.setRules(rules);
            
            System.out.println("✅ STEP 5: Created FieldMaskingRules - userRole: " + maskingRules.getUserRole() + ", reportType: " + maskingRules.getReportType() + ", rules count: " + maskingRules.getRules().size());
            System.out.println("🔐 KeycloakFieldMaskingService.getMaskingRulesFromToken() END - SUCCESS");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("✅ KeycloakFieldMaskingService: Successfully created FieldMaskingRules object");
            System.out.println("✅ User Role: " + maskingRules.getUserRole());
            System.out.println("✅ Report Type: " + maskingRules.getReportType());
            System.out.println("✅ Rules Count: " + maskingRules.getRules().size());
            
            return maskingRules;
            
        } catch (Exception e) {
            System.err.println("❌ KeycloakFieldMaskingService: Error extracting masking rules from JWT: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to extract masking rules from JWT. No fallback methods available.", e);
        }
    }

    /**
     * Extract user role from JWT token string (public method for use by other services)
     */
    public UserRole extractUserRoleFromJwt(String jwtToken) {
        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            return extractUserRole(jwt);
        } catch (Exception e) {
            System.err.println("❌ KeycloakFieldMaskingService: Error extracting role from JWT token: " + e.getMessage());
            throw new RuntimeException("Failed to extract role from JWT token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract user role from JWT
     */
    private UserRole extractUserRole(Jwt jwt) {
        System.out.println("🔐 KeycloakFieldMaskingService: Extracting user role from JWT");
        
        try {
            // Print all available claims for debugging
            System.out.println("🔐 Available JWT claims: " + jwt.getClaims().keySet());
            
            // Try to extract from resource_access (CLIENT roles) first
            System.out.println("🔐 Checking resource_access (CLIENT roles)...");
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                System.out.println("🔐 resource_access found: " + resourceAccess);
                // Check for trial-app client roles
                @SuppressWarnings("unchecked")
                Map<String, Object> trialAppAccess = (Map<String, Object>) resourceAccess.get("trial-app");
                if (trialAppAccess != null) {
                    @SuppressWarnings("unchecked")
                    List<String> clientRoles = (List<String>) trialAppAccess.get("roles");
                    if (clientRoles != null && !clientRoles.isEmpty()) {
                        System.out.println("🔐 CLIENT roles in JWT: " + clientRoles);
                        // Use fromOrNull to safely map roles, filtering out invalid ones
                        Optional<UserRole> userRole = clientRoles.stream()
                            .filter(role -> role != null && !role.trim().isEmpty())
                            .filter(role -> !role.startsWith("default-roles-") &&
                                           !role.equals("offline_access") &&
                                           !role.equals("uma_authorization"))
                            .map(role -> {
                                UserRole mapped = UserRole.fromOrNull(role);
                                if (mapped == null) {
                                    System.out.println("⚠️ KeycloakFieldMaskingService: Skipping invalid CLIENT role: " + role);
                                }
                                return mapped;
                            })
                            .filter(role -> role != null)
                            .findFirst();
                        if (userRole.isPresent()) {
                            System.out.println("✅ Extracted user role from CLIENT roles: " + userRole.get());
                            return userRole.get();
                        }
                    }
                }
            }
            
            // Fallback to realm_access.roles
            System.out.println("🔐 Checking realm_access.roles (fallback)...");
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                System.out.println("🔐 realm_access found: " + realmAccess);
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                if (roles != null && !roles.isEmpty()) {
                    System.out.println("🔐 All roles in JWT: " + roles);
                    
                    // Filter out default Keycloak roles and find the actual user role
                    // Use fromOrNull to safely map roles, filtering out invalid ones
                    Optional<UserRole> userRole = roles.stream()
                        .filter(role -> role != null && !role.trim().isEmpty())
                        .filter(role -> !role.startsWith("default-roles-") &&
                                       !role.equals("offline_access") &&
                                       !role.equals("uma_authorization"))
                        .map(role -> {
                            UserRole mapped = UserRole.fromOrNull(role);
                            if (mapped == null) {
                                System.out.println("⚠️ KeycloakFieldMaskingService: Skipping invalid REALM role: " + role);
                            }
                            return mapped;
                        })
                        .filter(role -> role != null)
                        .findFirst();
                    
                    if (userRole.isPresent()) {
                        System.out.println("✅ Extracted user role: " + userRole.get());
                        return userRole.get();
                    } else {
                        System.out.println("⚠️ No user role found in realm_access.roles, trying username-based extraction...");
                        // Don't return the first role, continue to username-based extraction
                    }
                } else {
                    System.out.println("⚠️ No roles found in realm_access");
                }
            } else {
                System.out.println("⚠️ realm_access not found in JWT");
            }
            
            // Try to extract from preferred_username
            System.out.println("🔐 Checking preferred_username...");
            String username = jwt.getClaimAsString("preferred_username");
            if (username != null) {
                System.out.println("🔐 preferred_username found: " + username);
                UserRole role = UserRole.fromOrNull(username);
                if (role != null) {
                    System.out.println("✅ Mapped username to role: " + role);
                    return role;
                } else {
                    System.out.println("⚠️ Username '" + username + "' does not map to a valid role");
                }
            } else {
                System.out.println("⚠️ preferred_username not found in JWT");
            }
            
            // Try to extract from sub
            System.out.println("🔐 Checking sub...");
            String sub = jwt.getClaimAsString("sub");
            if (sub != null) {
                System.out.println("🔐 sub found: " + sub);
                UserRole role = UserRole.fromOrNull(sub);
                if (role != null) {
                    System.out.println("✅ Mapped sub to role: " + role);
                    return role;
                } else {
                    System.out.println("⚠️ Sub '" + sub + "' does not map to a valid role");
                }
            } else {
                System.out.println("⚠️ sub not found in JWT");
            }
            
            // NO FALLBACK - Role MUST be extracted from JWT token
            System.err.println("❌ No role found in JWT token. Role is required and must be present in JWT.");
            throw new IllegalArgumentException("Role is required - cannot be extracted from JWT token. JWT must contain role information in realm_access.roles, preferred_username, or sub.");
            
        } catch (IllegalArgumentException e) {
            // Re-throw IllegalArgumentException (no role found)
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Error extracting user role from JWT: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to extract user role from JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Extract field masking rules from JWT claims
     * Supports both Protocol Mapper format (List<String>) and legacy JWT format (Map)
     */
    private Map<String, Object> extractFieldMaskingRulesFromJWT(Jwt jwt) {
        System.out.println("🔐 KeycloakFieldMaskingService: Extracting field masking rules from JWT claims");

        try {
            // Try different claim names for field masking rules
            String[] possibleClaimNames = {
                "field_masking_rules",
                "masking_rules",
                "field_rules",
                "custom_attributes",
                "attributes"
            };

            for (String claimName : possibleClaimNames) {
                System.out.println("🔐 Checking claim: " + claimName);
                Object claimValue = jwt.getClaim(claimName);
                if (claimValue != null) {
                    System.out.println("✅ Found claim " + claimName + ": " + claimValue);
                    System.out.println("🔐 Claim value type: " + claimValue.getClass().getSimpleName());

                    // PROTOCOL MAPPER FORMAT: field_masking_rules is a List<String>
                    // Each string is formatted as "fieldName:maskingType:accessLevel:enabled"
                    if (claimValue instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> rulesList = (List<String>) claimValue;
                        System.out.println("✅ Claim is a List with " + rulesList.size() + " entries (Protocol Mapper format)");
                        System.out.println("✅ First rule: " + (rulesList.isEmpty() ? "EMPTY" : rulesList.get(0)));

                        // Wrap the List in a Map with the claim name as key
                        // This format is compatible with convertToFieldMaskingRules()
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put(claimName, rulesList);
                        System.out.println("✅ Returning Protocol Mapper format: { " + claimName + ": List[" + rulesList.size() + "] }");
                        return resultMap;
                    }

                    // LEGACY JWT FORMAT: field_masking_rules is a Map<String, Object>
                    // Each entry is a field name mapped to a rule object
                    if (claimValue instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> claimMap = (Map<String, Object>) claimValue;
                        System.out.println("✅ Claim is a Map with " + claimMap.size() + " entries (legacy JWT format)");
                        System.out.println("✅ Returning legacy JWT format map");
                        return claimMap;
                    }

                    System.out.println("⚠️ Claim is neither List nor Map, type: " + claimValue.getClass().getSimpleName());
                } else {
                    System.out.println("⚠️ Claim " + claimName + " not found or null");
                }
            }

            System.out.println("⚠️ No field masking rules found in JWT claims");
            return null;

        } catch (Exception e) {
            System.err.println("❌ Error extracting field masking rules from JWT: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert Keycloak role attributes to FieldMaskingRule objects
     */
    private List<FieldMaskingRule> convertToFieldMaskingRules(Map<String, Object> rulesMap, String reportType) {
        System.out.println("🔐 KeycloakFieldMaskingService: Converting rules map to FieldMaskingRule objects");
        System.out.println("🔐 Rules map: " + (rulesMap != null ? rulesMap : "NULL"));
        System.out.println("🔐 Report type: " + reportType);
        
        List<FieldMaskingRule> rules = new ArrayList<>();
        
        if (rulesMap == null || rulesMap.isEmpty()) {
            System.out.println("⚠️ Rules map is null or empty, returning empty list");
            return rules;
        }
        
        try {
            // Check if this is Keycloak format (field_masking_rules as list of strings)
            if (rulesMap.containsKey("field_masking_rules")) {
                System.out.println("🔐 Detected Keycloak format - field_masking_rules as list");
                Object fieldMaskingRulesObj = rulesMap.get("field_masking_rules");
                
                if (fieldMaskingRulesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> ruleStrings = (List<String>) fieldMaskingRulesObj;
                    System.out.println("🔐 Found " + ruleStrings.size() + " rule strings");
                    
                    for (String ruleString : ruleStrings) {
                        System.out.println("🔐 Processing rule string: " + ruleString);
                        
                        // Parse format: "fieldName:maskingType:accessLevel:enabled"
                        String[] parts = ruleString.split(":");
                        if (parts.length >= 4) {
                            String fieldName = parts[0];
                            String maskingTypeStr = parts[1];
                            String accessLevelStr = parts[2];
                            String enabledStr = parts[3];
                            
                            FieldMaskingRule rule = new FieldMaskingRule();
                            rule.setFieldName(fieldName);
                            rule.setReportType(reportType);
                            
                            // Set masking type - NO HARDCODED FALLBACKS
                            try {
                                rule.setMaskingType(FieldMaskingRule.MaskingType.valueOf(maskingTypeStr));
                                System.out.println("✅ Set masking type for " + fieldName + ": " + maskingTypeStr);
                            } catch (IllegalArgumentException e) {
                                System.err.println("❌ Invalid masking type for " + fieldName + ": " + maskingTypeStr);
                                throw new IllegalArgumentException("Invalid masking type '" + maskingTypeStr + "' for field '" + fieldName + "' in Keycloak. Valid types must be configured in Keycloak. No hardcoded fallbacks allowed.");
                            }
                            
                            // Set access level - NO HARDCODED FALLBACKS
                            try {
                                rule.setAccessLevel(FieldMaskingRule.AccessLevel.valueOf(accessLevelStr));
                                System.out.println("✅ Set access level for " + fieldName + ": " + accessLevelStr);
                            } catch (IllegalArgumentException e) {
                                System.err.println("❌ Invalid access level for " + fieldName + ": " + accessLevelStr);
                                throw new IllegalArgumentException("Invalid access level '" + accessLevelStr + "' for field '" + fieldName + "' in Keycloak. Valid access levels must be configured in Keycloak. No hardcoded fallbacks allowed.");
                            }
                            
                            // Set enabled flag
                            rule.setEnabled(Boolean.parseBoolean(enabledStr));
                            System.out.println("✅ Set enabled for " + fieldName + ": " + rule.isEnabled());
                            
                            rules.add(rule);
                            System.out.println("✅ Added rule for " + fieldName + ": " + rule);
                        } else {
                            System.err.println("❌ Invalid rule string format: " + ruleString);
                        }
                    }
                } else {
                    System.err.println("❌ field_masking_rules is not a List: " + fieldMaskingRulesObj.getClass().getSimpleName());
                }
            } else {
                // Fallback to original JWT format (field names as keys)
                System.out.println("🔐 Using JWT format - field names as keys");
                for (Map.Entry<String, Object> entry : rulesMap.entrySet()) {
                    String fieldName = entry.getKey();
                    Object ruleData = entry.getValue();
                    
                    System.out.println("🔐 Processing field: " + fieldName + ", rule data: " + ruleData);
                    
                    if (ruleData instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> ruleMap = (Map<String, Object>) ruleData;
                        System.out.println("🔐 Rule map for " + fieldName + ": " + ruleMap);
                        
                        FieldMaskingRule rule = new FieldMaskingRule();
                        rule.setFieldName(fieldName);
                        rule.setReportType(reportType);
                        
                        // Extract masking type
                        String maskingTypeStr = (String) ruleMap.get("maskingType");
                        if (maskingTypeStr != null) {
                            try {
                                rule.setMaskingType(FieldMaskingRule.MaskingType.valueOf(maskingTypeStr));
                                System.out.println("✅ Set masking type for " + fieldName + ": " + maskingTypeStr);
                            } catch (IllegalArgumentException e) {
                                System.err.println("❌ Invalid masking type for " + fieldName + ": " + maskingTypeStr);
                                rule.setMaskingType(FieldMaskingRule.MaskingType.NONE);
                            }
                        } else {
                            System.out.println("⚠️ No masking type for " + fieldName + ", using NONE");
                            rule.setMaskingType(FieldMaskingRule.MaskingType.NONE);
                        }
                        
                        // Extract access level
                        String accessLevelStr = (String) ruleMap.get("accessLevel");
                        if (accessLevelStr != null) {
                            try {
                                rule.setAccessLevel(FieldMaskingRule.AccessLevel.valueOf(accessLevelStr));
                                System.out.println("✅ Set access level for " + fieldName + ": " + accessLevelStr);
                            } catch (IllegalArgumentException e) {
                                System.err.println("❌ Invalid access level for " + fieldName + ": " + accessLevelStr);
                                rule.setAccessLevel(FieldMaskingRule.AccessLevel.FULL_ACCESS);
                            }
                        } else {
                            System.out.println("⚠️ No access level for " + fieldName + ", using FULL_ACCESS");
                            rule.setAccessLevel(FieldMaskingRule.AccessLevel.FULL_ACCESS);
                        }
                        
                        // Extract masking pattern
                        String maskingPattern = (String) ruleMap.get("maskingPattern");
                        rule.setMaskingPattern(maskingPattern);
                        System.out.println("✅ Set masking pattern for " + fieldName + ": " + maskingPattern);
                        
                        // Extract enabled flag
                        Boolean enabled = (Boolean) ruleMap.get("enabled");
                        rule.setEnabled(enabled != null ? enabled : true);
                        System.out.println("✅ Set enabled for " + fieldName + ": " + rule.isEnabled());
                        
                        rules.add(rule);
                        System.out.println("✅ Added rule for " + fieldName + ": " + rule);
                        
                    } else {
                        System.err.println("❌ Rule data for " + fieldName + " is not a Map: " + ruleData.getClass().getSimpleName());
                    }
                }
            }
            
            System.out.println("✅ Successfully converted " + rules.size() + " rules");
            return rules;
            
        } catch (Exception e) {
            System.err.println("❌ Error converting rules map: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // JWT-ONLY: Default rules methods removed - no fallback available

    /**
     * Create a default rule
     */
    private FieldMaskingRule createDefaultRule(String fieldName, String maskingType, String accessLevel) {
        FieldMaskingRule rule = new FieldMaskingRule();
        rule.setFieldName(fieldName);
        rule.setMaskingType(FieldMaskingRule.MaskingType.valueOf(maskingType));
        rule.setAccessLevel(FieldMaskingRule.AccessLevel.valueOf(accessLevel));
        rule.setEnabled(true);
        return rule;
    }

    /**
     * Get masking rules for a specific role (used when system token is used for auth but recipient role needs masking)
     */
    public FieldMaskingRules getMaskingRulesForRole(String recipientRole, String reportType) {
        try {
            System.out.println("🔐 KeycloakFieldMaskingService: Getting masking rules for recipient role: " + recipientRole);
            
            UserRole role = UserRole.from(recipientRole);
            Map<String, Object> rulesMap = fetchRoleAttributesFromKeycloak(role);
            
            if (rulesMap != null && !rulesMap.isEmpty()) {
                List<FieldMaskingRule> rules = convertToFieldMaskingRules(rulesMap, reportType);
                FieldMaskingRules maskingRules = new FieldMaskingRules();
                maskingRules.setUserRole(recipientRole);
                maskingRules.setReportType(reportType);
                maskingRules.setRules(rules);
                return maskingRules;
            }
            
            System.out.println("⚠️ KeycloakFieldMaskingService: No rules found for role: " + recipientRole);
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ KeycloakFieldMaskingService: Error getting masking rules for role: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extract JWT token from HTTP request
     */
    public String extractJwtTokenFromRequest(HttpServletRequest request) {
        System.out.println("🔐 KeycloakFieldMaskingService: Extracting JWT token from request");
        
        try {
            String authHeader = request.getHeader("Authorization");
            System.out.println("🔐 Authorization header: " + (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "NULL"));
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                System.out.println("✅ Extracted JWT token (first 50 chars): " + token.substring(0, Math.min(50, token.length())) + "...");
                return token;
            } else {
                System.out.println("⚠️ No Bearer token found in Authorization header");
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting JWT token from request: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Fetch role attributes directly from Keycloak using admin API
     */
    private Map<String, Object> fetchRoleAttributesFromKeycloak(UserRole userRole) {
        try {
            System.out.println("🔐 [fetchRoleAttributesFromKeycloak] START - role: " + userRole);
            System.out.println("🔐 [fetchRoleAttributesFromKeycloak] UserRole enum: " + (userRole != null ? userRole.name() : "NULL"));
            
            // Get admin access token
            System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Getting admin access token...");
            String accessToken = getAdminAccessToken();
            if (accessToken == null) {
                System.err.println("❌ [fetchRoleAttributesFromKeycloak] Failed to get Keycloak admin access token");
                return null;
            }
            System.out.println("✅ [fetchRoleAttributesFromKeycloak] Got admin access token (length: " + (accessToken != null ? accessToken.length() : 0) + ")");
            
            // Normalize role name
            String normalizedRole = userRole.name();
            System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Normalized role name: " + normalizedRole);
            
            // Fetch from CLIENT roles - attributes are stored on CLIENT roles
            String roleUrl = keycloakBaseUrl + "/admin/realms/" + keycloakRealm + "/clients/" + keycloakClientUuid + "/roles/" + normalizedRole;
            System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Full URL: " + roleUrl);
            System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Keycloak Base URL: " + keycloakBaseUrl);
            System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Keycloak Realm: " + keycloakRealm);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> request = new HttpEntity<>(headers);
            System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Making HTTP GET request...");
            
            ResponseEntity<Map> response = restTemplate.exchange(
                roleUrl, 
                HttpMethod.GET, 
                request, 
                Map.class
            );
            
            System.out.println("🔐 [fetchRoleAttributesFromKeycloak] HTTP Response Status: " + response.getStatusCode());
            System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Response body is null: " + (response.getBody() == null));
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> roleData = response.getBody();
                System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Role data keys: " + roleData.keySet());
                
                Map<String, Object> attributes = (Map<String, Object>) roleData.get("attributes");
                System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Attributes object: " + attributes);
                System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Attributes is null: " + (attributes == null));
                
                if (attributes != null) {
                    System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Attributes keys: " + attributes.keySet());
                    System.out.println("🔐 [fetchRoleAttributesFromKeycloak] Has field_masking_rules: " + attributes.containsKey("field_masking_rules"));
                }
                
                if (attributes != null && attributes.containsKey("field_masking_rules")) {
                    Object rulesObj = attributes.get("field_masking_rules");
                    System.out.println("🔐 [fetchRoleAttributesFromKeycloak] field_masking_rules type: " + (rulesObj != null ? rulesObj.getClass().getName() : "NULL"));
                    System.out.println("🔐 [fetchRoleAttributesFromKeycloak] field_masking_rules value: " + rulesObj);
                    System.out.println("✅ [fetchRoleAttributesFromKeycloak] Found field masking rules in role attributes");
                    return attributes;
                } else {
                    System.out.println("⚠️ [fetchRoleAttributesFromKeycloak] No field masking rules found in role attributes");
                    System.out.println("⚠️ [fetchRoleAttributesFromKeycloak] Available attributes: " + (attributes != null ? attributes.keySet() : "NULL"));
                    return null;
                }
            } else {
                System.err.println("❌ [fetchRoleAttributesFromKeycloak] Failed to fetch role attributes. Status: " + response.getStatusCode());
                if (response.getBody() != null) {
                    System.err.println("❌ [fetchRoleAttributesFromKeycloak] Response body: " + response.getBody());
                }
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ [fetchRoleAttributesFromKeycloak] Error fetching role attributes: " + e.getMessage());
            System.err.println("❌ [fetchRoleAttributesFromKeycloak] Exception type: " + e.getClass().getName());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get admin access token from Keycloak
     */
    private String getAdminAccessToken() {
        // Check if we have a valid token
        if (adminAccessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return adminAccessToken;
        }

        try {
            System.out.println("🔐 KeycloakFieldMaskingService: Getting admin access token from Keycloak");
            
            String tokenUrl = keycloakBaseUrl + "/realms/master/protocol/openid-connect/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            String body = "grant_type=password" +
                         "&client_id=admin-cli" +
                         "&username=" + keycloakAdminUser +
                         "&password=" + keycloakAdminPassword;
            
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();
                adminAccessToken = (String) tokenResponse.get("access_token");
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");
                
                if (adminAccessToken != null && expiresIn != null) {
                    // Set expiry time with 5 minute buffer (300 seconds)
                    tokenExpiryTime = System.currentTimeMillis() + ((expiresIn - 300) * 1000L);
                    System.out.println("✅ KeycloakFieldMaskingService: Successfully obtained admin access token");
                    return adminAccessToken;
                }
            }
            
            System.err.println("❌ KeycloakFieldMaskingService: Failed to get admin access token");
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ KeycloakFieldMaskingService: Error getting admin access token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Convert FieldMaskingRule list to Keycloak format (list of strings)
     * Format: "fieldName:maskingType:accessLevel:enabled"
     */
    private List<String> convertRulesToKeycloakFormat(List<FieldMaskingRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> keycloakRules = new ArrayList<>();
        for (FieldMaskingRule rule : rules) {
            if (rule.getFieldName() == null || rule.getFieldName().trim().isEmpty()) {
                System.err.println("⚠️ Skipping rule with empty fieldName");
                continue;
            }
            
            if (rule.getMaskingType() == null) {
                throw new IllegalArgumentException("MaskingType is required for field '" + rule.getFieldName() + "'. No fallback allowed.");
            }
            
            if (rule.getAccessLevel() == null) {
                throw new IllegalArgumentException("AccessLevel is required for field '" + rule.getFieldName() + "'. No fallback allowed.");
            }
            
            // Format: "fieldName:maskingType:accessLevel:enabled"
            String ruleString = String.format("%s:%s:%s:%s",
                rule.getFieldName(),
                rule.getMaskingType().name(),
                rule.getAccessLevel().name(),
                rule.isEnabled()
            );
            keycloakRules.add(ruleString);
        }
        
        return keycloakRules;
    }
    
    /**
     * Update role attributes in Keycloak with field masking rules
     * This saves the rules to Keycloak so they appear in JWT tokens
     */
    public void updateRoleAttributesInKeycloak(UserRole userRole, List<FieldMaskingRule> rules) {
        System.out.println("🔐 KeycloakFieldMaskingService: updateRoleAttributesInKeycloak() START");
        System.out.println("🔐 Role: " + (userRole != null ? userRole.name() : "NULL"));
        System.out.println("🔐 Rules count: " + (rules != null ? rules.size() : "NULL"));
        
        if (userRole == null) {
            throw new IllegalArgumentException("UserRole cannot be null");
        }
        
        if (rules == null) {
            throw new IllegalArgumentException("Rules list cannot be null");
        }
        
        try {
            // Get admin access token
            System.out.println("🔐 Getting admin access token...");
            String accessToken = getAdminAccessToken();
            if (accessToken == null) {
                throw new IllegalStateException("Failed to get Keycloak admin access token");
            }
            System.out.println("✅ Got admin access token");
            
            // Convert rules to Keycloak format
            System.out.println("🔐 Converting rules to Keycloak format...");
            System.out.println("🔐 Input rules count: " + rules.size());
            for (int i = 0; i < Math.min(5, rules.size()); i++) {
                FieldMaskingRule rule = rules.get(i);
                System.out.println("🔐 Input rule " + (i+1) + ": field=" + rule.getFieldName() + ", masking=" + rule.getMaskingType() + ", access=" + rule.getAccessLevel() + ", enabled=" + rule.isEnabled());
            }
            List<String> keycloakRules = convertRulesToKeycloakFormat(rules);
            System.out.println("✅ Converted " + keycloakRules.size() + " rules to Keycloak format");
            for (int i = 0; i < Math.min(5, keycloakRules.size()); i++) {
                System.out.println("🔐 Keycloak rule " + (i+1) + ": " + keycloakRules.get(i));
            }
            
            // Normalize role name
            String normalizedRole = userRole.name();
            System.out.println("🔐 Normalized role name: " + normalizedRole);
            
            // Get current CLIENT role to preserve existing attributes
            System.out.println("🔐 Fetching current CLIENT role attributes...");
            String getRoleUrl = keycloakBaseUrl + "/admin/realms/" + keycloakRealm + "/clients/" + keycloakClientUuid + "/roles/" + normalizedRole;
            HttpHeaders getHeaders = new HttpHeaders();
            getHeaders.setBearerAuth(accessToken);
            HttpEntity<String> getRequest = new HttpEntity<>(getHeaders);
            
            Map<String, Object> currentRoleData = null;
            try {
                ResponseEntity<Map> getResponse = restTemplate.exchange(
                    getRoleUrl,
                    HttpMethod.GET,
                    getRequest,
                    Map.class
                );
                
                if (getResponse.getStatusCode() == HttpStatus.OK && getResponse.getBody() != null) {
                    currentRoleData = getResponse.getBody();
                    System.out.println("✅ Fetched current CLIENT role attributes");
                } else {
                    System.err.println("❌ Could not fetch CLIENT role (status: " + getResponse.getStatusCode() + ")");
                    System.out.println("⚠️ Will attempt to update anyway with new attributes");
                }
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                // Role doesn't exist - this shouldn't happen if role exists, but retry once
                System.err.println("⚠️ CLIENT role '" + normalizedRole + "' not found (404). Retrying fetch...");
                try {
                    // Wait a moment and retry
                    Thread.sleep(500);
                    ResponseEntity<Map> retryResponse = restTemplate.exchange(
                        getRoleUrl,
                        HttpMethod.GET,
                        getRequest,
                        Map.class
                    );
                    if (retryResponse.getStatusCode() == HttpStatus.OK && retryResponse.getBody() != null) {
                        currentRoleData = retryResponse.getBody();
                        System.out.println("✅ Successfully fetched CLIENT role on retry");
                    } else {
                        throw new IllegalStateException("CLIENT role '" + normalizedRole + "' does not exist. Please ensure the role exists in Keycloak before updating rules.");
                    }
                } catch (Exception retryEx) {
                    throw new IllegalStateException("CLIENT role '" + normalizedRole + "' does not exist. Please ensure the role exists in Keycloak before updating rules.", retryEx);
                }
            } catch (org.springframework.web.client.HttpServerErrorException e) {
                // Handle 5xx errors from Keycloak (server errors) - role exists but Keycloak has internal error
                System.err.println("⚠️ Keycloak server error (5xx) when fetching CLIENT role: " + e.getMessage());
                System.out.println("⚠️ Role exists but Keycloak returned 500. Will proceed with attribute update directly.");
                // Set currentRoleData to empty map - we'll update attributes without preserving existing ones
                currentRoleData = new HashMap<>();
            } catch (Exception e) {
                // For other errors (network issues, etc.), try to proceed anyway
                System.err.println("⚠️ Error fetching CLIENT role: " + e.getMessage());
                System.out.println("⚠️ Will attempt to update role attributes directly without fetching first");
                currentRoleData = new HashMap<>();
            }
            
            // If we don't have role data, proceed with update anyway
            // The PUT request will update the role attributes even if we couldn't fetch first
            if (currentRoleData == null) {
                System.out.println("⚠️ No role data fetched, will proceed with attribute update anyway");
                currentRoleData = new HashMap<>();
            }
            
            // Prepare update payload
            // Keycloak expects attributes as Map<String, List<String>> where each attribute value is a list
            Map<String, List<String>> attributes = new HashMap<>();
            
            // Preserve existing attributes if they exist
            if (currentRoleData != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> existingAttributes = (Map<String, Object>) currentRoleData.get("attributes");
                if (existingAttributes != null) {
                    for (Map.Entry<String, Object> entry : existingAttributes.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        // Keycloak stores attributes as List<String>
                        if (value instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> valueList = (List<String>) value;
                            attributes.put(key, valueList);
                        } else if (value != null) {
                            // Convert single value to list
                            attributes.put(key, Arrays.asList(value.toString()));
                        }
                    }
                }
            }
            
            // Update field_masking_rules attribute
            attributes.put("field_masking_rules", keycloakRules);
            System.out.println("🔐 Setting field_masking_rules attribute with " + keycloakRules.size() + " rules");
            System.out.println("🔐 First 3 rules being saved: " + keycloakRules.subList(0, Math.min(3, keycloakRules.size())));
            
            // Build final update payload
            Map<String, Object> updatePayload = new HashMap<>();
            if (currentRoleData != null) {
                // Preserve all other role properties (name, description, etc.)
                for (Map.Entry<String, Object> entry : currentRoleData.entrySet()) {
                    if (!"attributes".equals(entry.getKey())) {
                        updatePayload.put(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                // For newly created roles, set basic properties
                updatePayload.put("name", normalizedRole);
                updatePayload.put("description", "Client role for " + normalizedRole);
            }
            updatePayload.put("attributes", attributes);
            
            System.out.println("🔐 Update payload: " + updatePayload);
            System.out.println("🔐 Field masking rules to save: " + keycloakRules);
            
            // Update CLIENT role attributes
            String updateRoleUrl = keycloakBaseUrl + "/admin/realms/" + keycloakRealm + "/clients/" + keycloakClientUuid + "/roles/" + normalizedRole;
            HttpHeaders updateHeaders = new HttpHeaders();
            updateHeaders.setBearerAuth(accessToken);
            updateHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> updateRequest = new HttpEntity<>(updatePayload, updateHeaders);
            
            System.out.println("🔐 Making PUT request to: " + updateRoleUrl);
            ResponseEntity<Void> updateResponse = restTemplate.exchange(
                updateRoleUrl,
                HttpMethod.PUT,
                updateRequest,
                Void.class
            );
            
            if (updateResponse.getStatusCode() == HttpStatus.NO_CONTENT || updateResponse.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ Successfully updated role attributes in Keycloak");
                System.out.println("✅ Role: " + normalizedRole);
                System.out.println("✅ Rules saved: " + keycloakRules.size());
                
                // Verify the save by fetching the role again
                System.out.println("🔐 Verifying save by fetching role again...");
                ResponseEntity<Map> verifyResponse = restTemplate.exchange(
                    getRoleUrl,
                    HttpMethod.GET,
                    getRequest,
                    Map.class
                );
                if (verifyResponse.getStatusCode() == HttpStatus.OK && verifyResponse.getBody() != null) {
                    Map<String, Object> verifyRoleData = verifyResponse.getBody();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> verifyAttributes = (Map<String, Object>) verifyRoleData.get("attributes");
                    if (verifyAttributes != null && verifyAttributes.containsKey("field_masking_rules")) {
                        @SuppressWarnings("unchecked")
                        List<String> savedRules = (List<String>) verifyAttributes.get("field_masking_rules");
                        System.out.println("✅ Verification: Found " + savedRules.size() + " rules in Keycloak");
                        System.out.println("✅ Verification: First 3 saved rules: " + savedRules.subList(0, Math.min(3, savedRules.size())));
                    } else {
                        System.err.println("❌ Verification: field_masking_rules not found in saved role!");
                    }
                }
                
                System.out.println("🔐 KeycloakFieldMaskingService: updateRoleAttributesInKeycloak() END - SUCCESS");
            } else {
                throw new IllegalStateException("Failed to update role attributes in Keycloak. Status: " + updateResponse.getStatusCode());
            }
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("❌ KeycloakFieldMaskingService: Error updating role attributes: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ KeycloakFieldMaskingService: Unexpected error updating role attributes: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update role attributes in Keycloak: " + e.getMessage(), e);
        }
    }
}
