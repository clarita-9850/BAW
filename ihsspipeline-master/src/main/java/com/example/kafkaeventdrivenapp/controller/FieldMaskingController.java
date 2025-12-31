package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.FieldMaskingRule;
import com.example.kafkaeventdrivenapp.model.FieldMaskingRules;
import com.example.kafkaeventdrivenapp.model.FieldMaskingRequest;
import com.example.kafkaeventdrivenapp.model.UserRole;
import com.example.kafkaeventdrivenapp.service.FieldMaskingService;
import com.example.kafkaeventdrivenapp.entity.TimesheetEntity;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.Column;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/field-masking")
@CrossOrigin(origins = "*")
public class FieldMaskingController {

    @Autowired
    private FieldMaskingService fieldMaskingService;
    
    @Autowired
    private com.example.kafkaeventdrivenapp.config.ReportTypeProperties reportTypeProperties;

    public FieldMaskingController() {
        System.out.println("🔧 FieldMaskingController: Constructor called - initializing...");
        try {
            System.out.println("✅ FieldMaskingController: Constructor completed successfully");
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingController: Constructor failed with error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get current user's role from authentication context
     * Prioritizes ADMIN role and filters out system/default roles
     */
    private String getCurrentUserRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                if (authentication.getPrincipal() instanceof Jwt jwt) {
                    // First check client roles (trial-app) - these are more specific
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
                    if (resourceAccess != null && resourceAccess.containsKey("trial-app")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> trialApp = (Map<String, Object>) resourceAccess.get("trial-app");
                        if (trialApp != null && trialApp.containsKey("roles")) {
                            @SuppressWarnings("unchecked")
                            List<String> clientRoles = (List<String>) trialApp.get("roles");
                            if (clientRoles != null && !clientRoles.isEmpty()) {
                                // Filter out system roles and prioritize ADMIN
                                List<String> validClientRoles = clientRoles.stream()
                                        .filter(role -> role != null && !role.trim().isEmpty())
                                        .filter(role -> !role.startsWith("default-roles-"))
                                        .filter(role -> !role.equals("offline_access"))
                                        .filter(role -> !role.equals("uma_authorization"))
                                        .map(String::toUpperCase)
                                        .collect(java.util.stream.Collectors.toList());
                                
                                // Prioritize ADMIN if present
                                if (validClientRoles.contains("ADMIN")) {
                                    return "ADMIN";
                                }
                                if (!validClientRoles.isEmpty()) {
                                    return UserRole.from(validClientRoles.get(0)).name();
                                }
                            }
                        }
                    }
                    
                    // Fallback to realm roles
                    @SuppressWarnings("unchecked")
                    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                    if (realmAccess != null && realmAccess.containsKey("roles")) {
                        @SuppressWarnings("unchecked")
                        List<String> realmRoles = (List<String>) realmAccess.get("roles");
                        if (realmRoles != null && !realmRoles.isEmpty()) {
                            // Filter out system roles and prioritize ADMIN
                            List<String> validRealmRoles = realmRoles.stream()
                                    .filter(role -> role != null && !role.trim().isEmpty())
                                    .filter(role -> !role.startsWith("default-roles-"))
                                    .filter(role -> !role.equals("offline_access"))
                                    .filter(role -> !role.equals("uma_authorization"))
                                    .map(String::toUpperCase)
                                    .collect(java.util.stream.Collectors.toList());
                            
                            // Prioritize ADMIN if present
                            if (validRealmRoles.contains("ADMIN")) {
                                return "ADMIN";
                            }
                            if (!validRealmRoles.isEmpty()) {
                                return UserRole.from(validRealmRoles.get(0)).name();
                            }
                        }
                    }
                    
                    String preferredUsername = jwt.getClaimAsString("preferred_username");
                    if (preferredUsername != null) {
                        return UserRole.from(preferredUsername).name();
                    }
                }

                return authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(authority -> authority.replace("ROLE_", ""))
                        .filter(role -> !role.equals("AUTHENTICATED")) // Filter out generic auth role
                        .map(UserRole::fromOrNull)
                        .filter(role -> role != null)
                        .map(UserRole::name)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No valid role found in authentication - role is required"));
            }
            throw new IllegalStateException("No authentication found - user must be authenticated with a valid role");
        } catch (IllegalStateException e) {
            // Re-throw our own exception
            throw e;
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingController: Error getting user role: " + e.getMessage());
            throw new IllegalStateException("Failed to extract role from JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Get field masking interface for the authenticated user's role (Hybrid method - default)
     */
    @GetMapping("/interface/{userRole}")
    public ResponseEntity<Map<String, Object>> getFieldMaskingInterface(
            @PathVariable String userRole,
            HttpServletRequest request) {
        // Verify that the requested role matches the authenticated user's role
        String currentUserRole = getCurrentUserRole();
        UserRole currentRole = UserRole.from(currentUserRole);
        UserRole requestedRole = UserRole.from(userRole);
        if (!currentRole.equals(requestedRole) && currentRole != UserRole.ADMIN) {
            return ResponseEntity.status(403).body(Map.of(
                "status", "ERROR",
                "message", "Access denied: You can only access your own role's configuration"
            ));
        }
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔒 FieldMaskingController: Getting masking interface for role: " + userRole);
            
            // Extract JWT token from request - REQUIRED, NO FALLBACK
            String jwtToken = extractJwtTokenFromRequest(request);
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                System.err.println("❌ FieldMaskingController: JWT token is required but not found");
                response.put("status", "ERROR");
                response.put("message", "JWT token is required. No fallback available.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            System.out.println("🔒 FieldMaskingController: JWT token extracted successfully");
            
            // Get rules from Keycloak - If no rules exist, return empty rules for configuration
            FieldMaskingRules keycloakRules = null;
            List<String> selectedFields = new ArrayList<>();
            List<FieldMaskingRule> rules = new ArrayList<>();
            
            try {
                keycloakRules = fieldMaskingService.getMaskingRules(userRole, reportTypeProperties.getDefaultReportType(), jwtToken);
                if (keycloakRules != null && keycloakRules.getRules() != null && !keycloakRules.getRules().isEmpty()) {
                    rules = keycloakRules.getRules();
                    System.out.println("✅ FieldMaskingController: Found " + rules.size() + " existing rules for role: " + userRole);
                    
                    // Extract selected fields from rules (fields that are not hidden)
                    selectedFields = rules.stream()
                            .filter(rule -> rule.getAccessLevel() != com.example.kafkaeventdrivenapp.model.FieldMaskingRule.AccessLevel.HIDDEN_ACCESS)
                            .map(com.example.kafkaeventdrivenapp.model.FieldMaskingRule::getFieldName)
                            .collect(Collectors.toList());
                    
                    // Ensure all available fields have rules (merge saved rules with available fields)
                    // This ensures consistency - if a field was added after rules were saved, it gets a default rule
                    List<FieldMetadata> availableFields = getAvailableFieldsList();
                    Map<String, FieldMaskingRule> rulesMap = rules.stream()
                            .collect(Collectors.toMap(
                                FieldMaskingRule::getFieldName,
                                rule -> rule,
                                (existing, replacement) -> existing
                            ));
                    
                    // Add default rules for any available fields that don't have saved rules
                    for (FieldMetadata field : availableFields) {
                        if (!rulesMap.containsKey(field.getName())) {
                            FieldMaskingRule defaultRule = new FieldMaskingRule();
                            defaultRule.setFieldName(field.getName());
                            defaultRule.setMaskingType(FieldMaskingRule.MaskingType.NONE);
                            defaultRule.setAccessLevel(FieldMaskingRule.AccessLevel.FULL_ACCESS);
                            defaultRule.setEnabled(true);
                            defaultRule.setReportType(reportTypeProperties.getDefaultReportType());
                            rules.add(defaultRule);
                            System.out.println("ℹ️ FieldMaskingController: Added default rule for field: " + field.getName());
                        }
                    }
                    
                    System.out.println("✅ FieldMaskingController: Total rules after merge: " + rules.size());
                } else {
                    System.out.println("ℹ️ FieldMaskingController: No rules found for role: " + userRole + " - returning empty rules for configuration");
                }
            } catch (RuntimeException e) {
                // If no rules exist in Keycloak, return empty rules so frontend can configure them
                if (e.getMessage() != null && e.getMessage().contains("No field masking rules found")) {
                    System.out.println("ℹ️ FieldMaskingController: No rules configured yet for role: " + userRole + " - returning empty rules for initial configuration");
                } else {
                    // Re-throw other runtime exceptions
                    throw e;
                }
            }
            
            // Create interface data - always return interface even if rules are empty
            Map<String, Object> interfaceData = new HashMap<>();
            interfaceData.put("userRole", userRole);
            interfaceData.put("rules", rules);
            interfaceData.put("selectedFields", selectedFields);
            interfaceData.put("availableFields", getAvailableFieldsData());
            interfaceData.put("maskingTypes", getMaskingTypes());
            interfaceData.put("accessLevels", getAccessLevels());
            interfaceData.put("source", rules.isEmpty() ? "EMPTY_CONFIGURATION" : "KEYCLOAK_JWT");
            
            // Log first few rules being returned to frontend
            if (!rules.isEmpty()) {
                System.out.println("📤 FieldMaskingController: Returning " + rules.size() + " rules to frontend");
                System.out.println("📤 FieldMaskingController: First 5 rules being returned:");
                for (int i = 0; i < Math.min(5, rules.size()); i++) {
                    FieldMaskingRule rule = rules.get(i);
                    System.out.println("  Rule " + (i+1) + ": field=" + rule.getFieldName() 
                        + ", masking=" + rule.getMaskingType() 
                        + ", access=" + rule.getAccessLevel() 
                        + ", enabled=" + rule.isEnabled());
                }
            }
            
            response.put("status", "SUCCESS");
            response.put("message", rules.isEmpty() 
                ? "No field masking rules configured yet. You can configure them below." 
                : "Field masking interface retrieved successfully");
            response.put("interface", interfaceData);
            
            System.out.println("✅ FieldMaskingController: Retrieved masking interface (rules: " + rules.size() + ")");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting masking interface: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get field masking interface: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * Extract JWT token from HTTP request
     */
    private String extractJwtTokenFromRequest(HttpServletRequest request) {
        System.out.println("🔒 FieldMaskingController: Extracting JWT token from request");
        
        try {
            String authHeader = request.getHeader("Authorization");
            System.out.println("🔒 Authorization header: " + (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "NULL"));
            
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
     * Update field masking rules
     */
    @PostMapping("/update/{userRole}")
    public ResponseEntity<Map<String, Object>> updateFieldMaskingRules(
            @PathVariable String userRole,
            @RequestBody List<FieldMaskingRule> rules) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔧 FieldMaskingController: Updating masking rules for role: " + userRole);
            
            String currentUserRole = getCurrentUserRole();
            UserRole currentRole = UserRole.from(currentUserRole);
            UserRole targetRole = UserRole.from(userRole);
            if (!currentRole.equals(targetRole) && currentRole != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", "ERROR",
                    "message", "Access denied: You can only update your own role's configuration",
                    "currentUserRole", currentUserRole
                ));
            }
            
            fieldMaskingService.updateRules(userRole, rules);
            
            response.put("status", "SUCCESS");
            response.put("message", "Field masking rules updated successfully");
            response.put("userRole", userRole);
            response.put("totalRules", rules.size());
            
            System.out.println("✅ FieldMaskingController: Updated " + rules.size() + " masking rules");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error updating masking rules: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to update masking rules: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }


    /**
     * Get available fields for masking - dynamically extracted from TimesheetEntity
     * NO HARDCODED FIELDS - Fields come from database schema
     */
    @GetMapping("/available-fields")
    public ResponseEntity<Map<String, Object>> getAvailableFields() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔍 FieldMaskingController: Getting available fields from TimesheetEntity...");
            
            // Dynamically extract fields from TimesheetEntity using reflection
            List<Map<String, Object>> fields = extractFieldsFromEntity();
            
            System.out.println("✅ FieldMaskingController: Extracted " + fields.size() + " fields from TimesheetEntity");
            
            response.put("status", "SUCCESS");
            response.put("fields", fields);
            response.put("totalFields", fields.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting available fields: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get available fields: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Dynamically extract field metadata from TimesheetEntity using reflection
     * NO HARDCODED FIELDS - Always matches database schema
     */
    private List<Map<String, Object>> extractFieldsFromEntity() {
        List<Map<String, Object>> fields = new ArrayList<>();
        
        Field[] entityFields = TimesheetEntity.class.getDeclaredFields();
        
        for (Field field : entityFields) {
            // Skip JPA internal fields and synthetic fields
            if (field.isSynthetic()) {
                continue;
            }
            
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            
            // Convert camelCase to lowercase for field masking (matches getFieldValue logic)
            String fieldNameLower = fieldName.toLowerCase();
            
            // Get display name from @Column annotation or generate from field name
            String displayName = fieldName;
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
                // Use column name as base, convert to display name
                displayName = convertToDisplayName(columnAnnotation.name());
            } else {
                displayName = convertToDisplayName(fieldName);
            }
            
            // Map Java types to frontend types
            String type = mapJavaTypeToFrontendType(fieldType);
            
            Map<String, Object> fieldInfo = new HashMap<>();
            fieldInfo.put("name", fieldNameLower);
            fieldInfo.put("displayName", displayName);
            fieldInfo.put("type", type);
            fieldInfo.put("javaType", fieldType.getSimpleName());
            
            fields.add(fieldInfo);
        }
        
        return fields;
    }
    
    /**
     * Convert snake_case or camelCase to Display Name
     */
    private String convertToDisplayName(String name) {
        // Handle snake_case
        if (name.contains("_")) {
            String[] parts = name.split("_");
            StringBuilder display = new StringBuilder();
            for (String part : parts) {
                if (display.length() > 0) display.append(" ");
                display.append(part.substring(0, 1).toUpperCase())
                       .append(part.substring(1).toLowerCase());
            }
            return display.toString();
        }
        
        // Handle camelCase
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                display.append(" ");
            }
            display.append(i == 0 ? Character.toUpperCase(c) : c);
        }
        return display.toString();
    }
    
    /**
     * Map Java types to frontend type strings
     */
    private String mapJavaTypeToFrontendType(Class<?> javaType) {
        if (javaType == String.class) {
            return "String";
        } else if (javaType == Long.class || javaType == long.class || 
                   javaType == Integer.class || javaType == int.class) {
            return "Integer";
        } else if (javaType == BigDecimal.class || 
                   javaType == Double.class || javaType == double.class ||
                   javaType == Float.class || javaType == float.class) {
            return "Double";
        } else if (javaType == LocalDate.class) {
            return "Date";
        } else if (javaType == LocalDateTime.class) {
            return "DateTime";
        } else if (javaType == Boolean.class || javaType == boolean.class) {
            return "Boolean";
        } else {
            return "String"; // Default
        }
    }

    /**
     * Get available fields data for interface - dynamically from TimesheetEntity
     * NO HARDCODED FIELDS - Always matches database schema
     */
    /**
     * Simple class to represent field metadata
     */
    private static class FieldMetadata {
        private String name;
        private String type;
        
        public FieldMetadata(String name, String type) {
            this.name = name;
            this.type = type;
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
    }
    
    /**
     * Get available fields as a list of FieldMetadata objects
     */
    private List<FieldMetadata> getAvailableFieldsList() {
        List<Map<String, Object>> fields = extractFieldsFromEntity();
        return fields.stream()
                .map(field -> new FieldMetadata(
                    (String) field.get("name"),
                    (String) field.get("type")
                ))
                .collect(Collectors.toList());
    }
    
    private Map<String, Object> getAvailableFieldsData() {
        List<Map<String, Object>> fields = extractFieldsFromEntity();
        
        Map<String, Object> result = new HashMap<>();
        result.put("fields", fields);
        result.put("totalCount", fields.size());
        return result;
    }

    /**
     * Get masking types
     */
    private List<Map<String, Object>> getMaskingTypes() {
        return List.of(
            Map.of("value", "NONE", "displayName", "No Masking", "description", "Show complete data"),
            Map.of("value", "HIDDEN", "displayName", "Hidden", "description", "Hide field completely"),
            Map.of("value", "PARTIAL_MASK", "displayName", "Partial Mask", "description", "Show partial data (e.g., XXX-XX-1234)"),
            Map.of("value", "HASH_MASK", "displayName", "Hash Mask", "description", "Show hash value"),
            Map.of("value", "ANONYMIZE", "displayName", "Anonymize", "description", "Replace with generic value"),
            Map.of("value", "AGGREGATE", "displayName", "Aggregate", "description", "Show aggregated data only")
        );
    }

    /**
     * Get access levels
     */
    private List<Map<String, Object>> getAccessLevels() {
        return List.of(
            Map.of("value", "FULL_ACCESS", "displayName", "Full Access", "description", "Show complete data"),
            Map.of("value", "MASKED_ACCESS", "displayName", "Masked Access", "description", "Show masked data"),
            Map.of("value", "HIDDEN_ACCESS", "displayName", "Hidden Access", "description", "Hide field completely")
        );
    }

    /**
     * Apply test masking to sample data
     */
    private Object applyTestMasking(Object value, FieldMaskingRule rule) {
        if (value == null) {
            return null;
        }

        switch (rule.getMaskingType()) {
            case NONE:
                return value;
            case HIDDEN:
                return "***HIDDEN***";
            case PARTIAL_MASK:
                String strValue = value.toString();
                if (strValue.length() > 4) {
                    return "***" + strValue.substring(strValue.length() - 4);
                }
                return "***";
            case HASH_MASK:
                return "HASH_" + Math.abs(value.toString().hashCode());
            case ANONYMIZE:
                return "ANONYMIZED_" + Math.abs(value.toString().hashCode() % 1000);
            case AGGREGATE:
                return "AGGREGATED";
            default:
                return value;
        }
    }


    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Extract user role from JWT token
     * Prioritizes client roles, then realm roles, filters out system roles
     */
    private String extractUserRoleFromJWT(String jwtToken, String requestedRole) {
        if (jwtToken == null) return null;
        try {
            // Parse JWT and extract role
            String[] parts = jwtToken.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            
            // Parse JSON payload
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            // First check client roles (trial-app) - these are more specific
            if (jsonNode.has("resource_access") && jsonNode.get("resource_access").has("trial-app")) {
                JsonNode trialApp = jsonNode.get("resource_access").get("trial-app");
                if (trialApp.has("roles")) {
                    for (JsonNode node : trialApp.get("roles")) {
                        String role = node.asText();
                        // Filter out system roles
                        if (role != null && !role.startsWith("default-roles-") 
                            && !role.equals("offline_access") 
                            && !role.equals("uma_authorization")) {
                            try {
                                return UserRole.from(role).name();
                            } catch (IllegalArgumentException e) {
                                // Skip invalid roles, continue to next
                            }
                        }
                    }
                }
            }
            
            // Fallback to realm_access roles
            if (jsonNode.has("realm_access") && jsonNode.get("realm_access").has("roles")) {
                for (JsonNode node : jsonNode.get("realm_access").get("roles")) {
                    String role = node.asText();
                    // Filter out system roles and prioritize ADMIN
                    if (role != null && !role.startsWith("default-roles-") 
                        && !role.equals("offline_access") 
                        && !role.equals("uma_authorization")) {
                        // Prioritize ADMIN if present
                        if ("ADMIN".equalsIgnoreCase(role)) {
                            return "ADMIN";
                        }
                        try {
                            UserRole mapped = UserRole.from(role);
                            if (mapped != null) {
                                return mapped.name();
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid roles, continue to next
                        }
                    }
                }
            }
            
            // NO FALLBACK - Role MUST be in JWT token
            System.err.println("❌ FieldMaskingController: No valid role found in JWT token");
            throw new IllegalStateException("Role is required - no valid role found in JWT token");
        } catch (IllegalStateException e) {
            // Re-throw our own exception
            throw e;
        } catch (Exception e) {
            System.err.println("Error extracting role from JWT: " + e.getMessage());
            throw new IllegalStateException("Failed to extract role from JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * New single endpoint for updating field masking rules
     * Only ADMIN can access this endpoint
     */
    @PostMapping("/update-rules")
    public ResponseEntity<Map<String, Object>> updateMaskingRulesGeneric(
            @RequestBody FieldMaskingRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Extract target role from request body
            String targetRole = request.getUserRole();
            if (targetRole == null || targetRole.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User role is required in request body",
                    "status", "BAD_REQUEST"
                ));
            }
            
            // Extract current user's role from JWT
            String jwtToken = extractJwtFromRequest(httpRequest);
            String currentUserRole = extractUserRoleFromJWT(jwtToken, targetRole);
            
            System.out.println("🔐 FieldMaskingController: Current user role: " + currentUserRole);
            System.out.println("🔐 FieldMaskingController: Target role: " + targetRole);
            
            // Authorization: ONLY ADMIN can update field masking rules
            if (!UserRole.ADMIN.name().equalsIgnoreCase(currentUserRole)) {
                System.out.println("❌ FieldMaskingController: Access denied for role: " + currentUserRole);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Access denied: Only ADMIN can manage field masking rules",
                    "status", "FORBIDDEN",
                    "currentUserRole", currentUserRole
                ));
            }
            
            // Delegate to internal method
            return updateMaskingRulesInternal(targetRole, request, httpRequest);
            
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingController: Error in updateMaskingRulesGeneric: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal server error: " + e.getMessage(),
                "status", "ERROR"
            ));
        }
    }

    /**
     * Internal method to handle common rule update logic
     */
    private ResponseEntity<Map<String, Object>> updateMaskingRulesInternal(
            String userRole,
            FieldMaskingRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            System.out.println("📝 FieldMaskingController: Updating rules for role: " + userRole);
            System.out.println("📝 FieldMaskingController: Payload summary - rules="
                    + (request.getRules() != null ? request.getRules().size() : 0)
                    + ", selectedFields="
                    + (request.getSelectedFields() != null ? request.getSelectedFields().size() : 0));
            
            // Log first few rules to see what's being received
            if (request.getRules() != null && !request.getRules().isEmpty()) {
                System.out.println("📝 FieldMaskingController: First 5 rules received from frontend:");
                for (int i = 0; i < Math.min(5, request.getRules().size()); i++) {
                    FieldMaskingRule rule = request.getRules().get(i);
                    System.out.println("  Rule " + (i+1) + ": field=" + rule.getFieldName() 
                        + ", masking=" + rule.getMaskingType() 
                        + ", access=" + rule.getAccessLevel() 
                        + ", enabled=" + rule.isEnabled());
                }
            }
            
            // CRITICAL FIX: Ensure ALL available fields have rules
            // Fields not in selectedFields should be set to HIDDEN_ACCESS
            List<FieldMetadata> availableFields = getAvailableFieldsList();
            List<FieldMaskingRule> allRules = new ArrayList<>();
            
            // Create a set of selected (visible) field names for quick lookup
            Set<String> selectedFieldsSet = new HashSet<>();
            if (request.getSelectedFields() != null) {
                for (String fieldName : request.getSelectedFields()) {
                    selectedFieldsSet.add(fieldName.toLowerCase());
                }
            }
            
            // Create a map of fields from the request for quick lookup
            Map<String, FieldMaskingRule> requestRulesMap = new HashMap<>();
            if (request.getRules() != null) {
                for (FieldMaskingRule rule : request.getRules()) {
                    requestRulesMap.put(rule.getFieldName().toLowerCase(), rule);
                }
            }
            
            // For each available field, either use the rule from request or create HIDDEN_ACCESS rule
            for (FieldMetadata field : availableFields) {
                String fieldName = field.getName().toLowerCase();
                FieldMaskingRule rule;
                
                if (requestRulesMap.containsKey(fieldName)) {
                    // Field has a rule in the request - use it but check if it should be hidden
                    rule = requestRulesMap.get(fieldName);
                    
                    // CRITICAL: If field is NOT in selectedFields, it should be HIDDEN_ACCESS
                    if (!selectedFieldsSet.contains(fieldName)) {
                        System.out.println("🔒 FieldMaskingController: Field " + field.getName() + " is not in selectedFields - setting to HIDDEN_ACCESS");
                        rule.setAccessLevel(FieldMaskingRule.AccessLevel.HIDDEN_ACCESS);
                        rule.setEnabled(false);
                    }
                } else {
                    // Field not in request - set to HIDDEN_ACCESS
                    rule = new FieldMaskingRule();
                    rule.setFieldName(field.getName());
                    rule.setMaskingType(FieldMaskingRule.MaskingType.NONE);
                    rule.setAccessLevel(FieldMaskingRule.AccessLevel.HIDDEN_ACCESS);
                    rule.setEnabled(false);
                    rule.setReportType(reportTypeProperties.getDefaultReportType());
                    System.out.println("🔒 FieldMaskingController: Added HIDDEN_ACCESS rule for field: " + field.getName());
                }
                
                allRules.add(rule);
            }
            
            // Count visible vs hidden rules
            long visibleCount = allRules.stream()
                .filter(r -> r.getAccessLevel() != FieldMaskingRule.AccessLevel.HIDDEN_ACCESS)
                .count();
            long hiddenCount = allRules.size() - visibleCount;
            
            System.out.println("📝 FieldMaskingController: Total rules to save: " + allRules.size() 
                + " (visible: " + visibleCount + ", hidden: " + hiddenCount + ")");
            
            // Update rules using service with ALL rules (visible + hidden)
            fieldMaskingService.updateRules(
                userRole,
                allRules,
                request.getSelectedFields()
            );
            System.out.println("📝 FieldMaskingController: fieldMaskingService.updateRules completed for role: " + userRole);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Field masking rules updated successfully");
            response.put("userRole", userRole);
            response.put("rulesCount", request.getRules().size());
            
            System.out.println("✅ FieldMaskingController: Rules updated for role: " + userRole);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingController: Error updating rules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to update rules: " + e.getMessage(),
                "status", "ERROR"
            ));
        }
    }

    /**
     * Admin endpoint to manually trigger re-sync of all rules to Keycloak
     * Only accessible by ADMIN
     */
    @PostMapping("/admin/resync-all")
    public ResponseEntity<Map<String, Object>> resyncAllRulesToKeycloak(
            HttpServletRequest httpRequest) {
        
        try {
            // Extract JWT token - REQUIRED, NO FALLBACK
            String jwtToken = extractJwtTokenFromRequest(httpRequest);
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "ERROR");
                errorResponse.put("message", "JWT token is required for resync operation. No fallback available.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // Extract current user's role from JWT
            String currentUserRole = extractUserRoleFromJWT(jwtToken, null);
            
            System.out.println("🔐 FieldMaskingController: Resync request from user role: " + currentUserRole);
            
            // Authorization: ONLY ADMIN can trigger re-sync
            if (!UserRole.ADMIN.name().equalsIgnoreCase(currentUserRole)) {
                System.out.println("❌ FieldMaskingController: Access denied for role: " + currentUserRole);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Access denied: Only ADMIN can trigger re-sync",
                    "status", "FORBIDDEN",
                    "currentUserRole", currentUserRole
                ));
            }
            
            System.out.println("🔄 FieldMaskingController: Starting manual re-sync of all rules to Keycloak...");
            
            // Get all roles and their current rules from JWT - REQUIRED, NO FALLBACK
            Map<String, Object> syncResults = new HashMap<>();
            List<String> roles = Arrays.asList(
                UserRole.ADMIN.name(),
                UserRole.SUPERVISOR.name(),
                UserRole.CASE_WORKER.name(),
                UserRole.PROVIDER.name(),
                UserRole.RECIPIENT.name()
            );
            
            int successCount = 0;
            int failureCount = 0;
            
            for (String role : roles) {
                try {
                    System.out.println("🔄 FieldMaskingController: Re-syncing rules for role: " + role);
                    
                    // Get current rules from Keycloak JWT - REQUIRED, NO FALLBACK
                    FieldMaskingRules currentRules = fieldMaskingService.getMaskingRules(role, reportTypeProperties.getDefaultReportType(), jwtToken);
                    
                    if (currentRules == null || currentRules.getRules() == null || currentRules.getRules().isEmpty()) {
                        System.out.println("⚠️ FieldMaskingController: No rules found in JWT for role: " + role + ", skipping");
                        syncResults.put(role, "NO_RULES_IN_JWT");
                        continue;
                    }
                    
                    // Extract selected fields from rules (non-hidden fields)
                    List<String> selectedFields = currentRules.getRules().stream()
                            .filter(rule -> rule.getAccessLevel() != com.example.kafkaeventdrivenapp.model.FieldMaskingRule.AccessLevel.HIDDEN_ACCESS)
                            .map(com.example.kafkaeventdrivenapp.model.FieldMaskingRule::getFieldName)
                            .collect(Collectors.toList());
                    
                    // Trigger sync to Keycloak
                    boolean syncSuccess = fieldMaskingService.syncRulesToKeycloak(role, currentRules.getRules(), selectedFields);
                    
                    if (syncSuccess) {
                        successCount++;
                        syncResults.put(role, "SUCCESS");
                        System.out.println("✅ FieldMaskingController: Successfully re-synced rules for role: " + role);
                    } else {
                        failureCount++;
                        syncResults.put(role, "FAILED");
                        System.err.println("❌ FieldMaskingController: Failed to re-sync rules for role: " + role);
                    }
                    
                } catch (Exception e) {
                    failureCount++;
                    syncResults.put(role, "ERROR: " + e.getMessage());
                    System.err.println("❌ FieldMaskingController: Error re-syncing rules for role " + role + ": " + e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "COMPLETED");
            response.put("message", "Manual re-sync completed");
            response.put("totalRoles", roles.size());
            response.put("successCount", successCount);
            response.put("failureCount", failureCount);
            response.put("syncResults", syncResults);
            
            System.out.println("🔄 FieldMaskingController: Manual re-sync completed - Success: " + successCount + ", Failures: " + failureCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingController: Error in manual re-sync: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Failed to trigger re-sync: " + e.getMessage(),
                "status", "ERROR"
            ));
        }
    }
}
