package com.cmips.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service for managing Keycloak groups dynamically.
 * Replaces hardcoded county/code mappings with dynamic group discovery.
 * 
 * Features:
 * - Get all groups from Keycloak Admin API (for batch/scheduled reports)
 * - Extract location from JWT token (for user-specific reports)
 * - Filter system groups automatically
 */
@Service
public class KeycloakGroupService {
    
    @Autowired
    private KeycloakAdminService keycloakAdminService;
    
    /**
     * Get all group names from Keycloak Admin API.
     * Use this for batch/scheduled reports that need to process all groups.
     * 
     * @return List of all group names (excluding system groups)
     */
    public List<String> getAllGroupNames() {
        try {
            System.out.println("🔍 KeycloakGroupService: Fetching all groups from Keycloak...");
            
            // Call Keycloak Admin API to get all groups
            List<Map<String, Object>> groups = keycloakAdminService.getAllGroups();
            
            if (groups == null || groups.isEmpty()) {
                System.out.println("⚠️ KeycloakGroupService: No groups found in Keycloak");
                return Collections.emptyList();
            }
            
            // Extract group names and filter out system groups
            List<String> groupNames = new ArrayList<>();
            for (Map<String, Object> group : groups) {
                String name = (String) group.get("name");
                if (name != null && !name.trim().isEmpty() && !isSystemGroup(name)) {
                    groupNames.add(name);
                }
            }
            
            System.out.println("✅ KeycloakGroupService: Found " + groupNames.size() + " groups: " + groupNames);
            return groupNames;
            
        } catch (Exception e) {
            System.err.println("❌ KeycloakGroupService: Error getting groups from Keycloak: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Extract location from JWT token.
     * Use this for user-specific reports where you need the user's assigned location.
     * 
     * Priority:
     * 1. locationId from attributes (set by Keycloak mapper)
     * 2. First non-system group from groups array
     * 
     * @param jwt JWT token from Spring Security context
     * @return Location/group name or null if not found
     */
    public String extractLocationFromJWT(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        
        try {
            // Priority 1: Check for locationId in attributes (set by Keycloak mapper)
            String locationId = jwt.getClaimAsString("locationId");
            if (locationId != null && !locationId.trim().isEmpty()) {
                System.out.println("✅ KeycloakGroupService: Extracted locationId from JWT: " + locationId);
                return locationId;
            }
            
            // Priority 2: Check attributes map (nested structure)
            Map<String, Object> attributes = jwt.getClaimAsMap("attributes");
            if (attributes != null) {
                Object locationIdObj = attributes.get("locationId");
                if (locationIdObj == null) {
                    locationIdObj = attributes.get("countyId"); // Fallback to countyId
                }
                
                if (locationIdObj != null) {
                    if (locationIdObj instanceof List && ((List<?>) locationIdObj).size() > 0) {
                        locationId = ((List<?>) locationIdObj).get(0).toString();
                    } else {
                        locationId = locationIdObj.toString();
                    }
                    
                    if (locationId != null && !locationId.trim().isEmpty()) {
                        System.out.println("✅ KeycloakGroupService: Extracted locationId from attributes: " + locationId);
                        return locationId;
                    }
                }
            }
            
            // Priority 3: Check groups array (fallback)
            List<String> groups = jwt.getClaimAsStringList("groups");
            if (groups != null && !groups.isEmpty()) {
                for (String group : groups) {
                    if (group != null && !group.trim().isEmpty() && !isSystemGroup(group)) {
                        System.out.println("✅ KeycloakGroupService: Extracted location from groups array: " + group);
                        return group;
                    }
                }
            }
            
            System.out.println("⚠️ KeycloakGroupService: No location found in JWT token");
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ KeycloakGroupService: Error extracting location from JWT: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extract location from JWT token string (for cases where Jwt object is not available).
     * 
     * @param jwtToken JWT token as string
     * @return Location/group name or null if not found
     */
    public String extractLocationFromJWTString(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Parse JWT token
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            // Priority 1: Check for locationId
            if (jsonNode.has("locationId")) {
                String locationId = jsonNode.get("locationId").asText();
                if (locationId != null && !locationId.trim().isEmpty()) {
                    return locationId;
                }
            }
            
            // Priority 2: Check attributes.locationId
            if (jsonNode.has("attributes") && jsonNode.get("attributes").has("locationId")) {
                com.fasterxml.jackson.databind.JsonNode locationIdNode = jsonNode.get("attributes").get("locationId");
                if (locationIdNode.isArray() && locationIdNode.size() > 0) {
                    return locationIdNode.get(0).asText();
                } else if (locationIdNode.isTextual()) {
                    return locationIdNode.asText();
                }
            }
            
            // Priority 3: Check attributes.countyId (fallback)
            if (jsonNode.has("attributes") && jsonNode.get("attributes").has("countyId")) {
                com.fasterxml.jackson.databind.JsonNode countyIdNode = jsonNode.get("attributes").get("countyId");
                if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                    return countyIdNode.get(0).asText();
                } else if (countyIdNode.isTextual()) {
                    return countyIdNode.asText();
                }
            }
            
            // Priority 4: Check groups array
            if (jsonNode.has("groups") && jsonNode.get("groups").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode groupNode : jsonNode.get("groups")) {
                    String group = groupNode.asText();
                    if (group != null && !group.trim().isEmpty() && !isSystemGroup(group)) {
                        return group;
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ KeycloakGroupService: Error parsing JWT token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Check if a group name is a system group that should be filtered out.
     * 
     * @param groupName Group name to check
     * @return true if it's a system group, false otherwise
     */
    private boolean isSystemGroup(String groupName) {
        if (groupName == null) {
            return true;
        }
        
        String normalized = groupName.trim().toLowerCase();
        
        // Filter out system groups
        return normalized.startsWith("system-") ||
               normalized.startsWith("default-") ||
               normalized.equals("offline_access") ||
               normalized.equals("uma_authorization") ||
               normalized.startsWith("realm-");
    }
    
    /**
     * Sanitize group name for use in file paths.
     * Replaces spaces and special characters with underscores.
     * 
     * @param groupName Group name to sanitize
     * @return Sanitized group name safe for file paths
     */
    public String sanitizeGroupNameForFile(String groupName) {
        if (groupName == null) {
            return "unknown";
        }
        
        return groupName.trim()
                .replace(" ", "_")
                .replace("/", "_")
                .replace("\\", "_")
                .replace(":", "_")
                .replace("*", "_")
                .replace("?", "_")
                .replace("\"", "_")
                .replace("<", "_")
                .replace(">", "_")
                .replace("|", "_")
                .toLowerCase();
    }
}






