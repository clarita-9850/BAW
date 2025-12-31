package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.County;
import com.example.kafkaeventdrivenapp.model.UserAccess;
import com.example.kafkaeventdrivenapp.service.DistrictCountyService;
import com.example.kafkaeventdrivenapp.service.CountyCodeMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * County Access Controller
 * Handles county access control - determining which counties a user can access based on their role and JWT token
 */
@RestController
@RequestMapping("/api/county")
@CrossOrigin(origins = "*")
public class CountyAccessController {

    @Autowired
    private DistrictCountyService districtCountyService;
    
    @Autowired
    private CountyCodeMappingService countyCodeMappingService;

    @GetMapping("/counties")
    public ResponseEntity<Map<String, Object>> getAllCounties() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<County> counties = districtCountyService.getAllCounties();
            response.put("status", "SUCCESS");
            response.put("counties", counties);
            response.put("totalCounties", counties.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to get counties: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/user-access/{userRole}")
    public ResponseEntity<Map<String, Object>> getUserAccess(@PathVariable String userRole) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserAccess userAccess = districtCountyService.getUserAccess(userRole);
            if (userAccess == null) {
                response.put("status", "ERROR");
                response.put("message", "User role not found: " + userRole);
                return ResponseEntity.status(404).body(response);
            }
            response.put("status", "SUCCESS");
            response.put("userAccess", userAccess);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to get user access: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/accessible-counties/{userRole}")
    public ResponseEntity<Map<String, Object>> getAccessibleCounties(
            @PathVariable String userRole,
            HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Extract JWT token from Authorization header
            String jwtToken = null;
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
                System.out.println("🔐 CountyAccessController: JWT token extracted from Authorization header");
            } else {
                System.out.println("⚠️ CountyAccessController: No Authorization header found");
            }
            
            // Extract user county from JWT token
            String userCounty = extractCountyFromJWT(jwtToken);
            System.out.println("📍 CountyAccessController: Extracted county from JWT: " + userCounty + " for role: " + userRole);
            
            List<String> accessibleCounties = districtCountyService.getAccessibleCounties(userRole, userCounty);
            System.out.println("✅ CountyAccessController: Returning " + accessibleCounties.size() + " accessible counties: " + accessibleCounties);
            
            response.put("status", "SUCCESS");
            response.put("userRole", userRole);
            response.put("accessibleCounties", accessibleCounties);
            response.put("totalCounties", accessibleCounties.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ CountyAccessController: Error getting accessible counties: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "ERROR");
            response.put("message", "Failed to get accessible counties: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Extract county from JWT token
     */
    private String extractCountyFromJWT(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return null;
        }
        try {
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            // Extract countyId from attributes (Keycloak custom attributes)
            // Check both county_id (snake_case from Protocol Mapper) and countyId (camelCase)
            if (jsonNode.has("county_id")) {
                return jsonNode.get("county_id").asText();
            } else if (jsonNode.has("countyId")) {
                return jsonNode.get("countyId").asText();
            } else if (jsonNode.has("attributes") && jsonNode.get("attributes").has("countyId")) {
                com.fasterxml.jackson.databind.JsonNode countyIdNode = jsonNode.get("attributes").get("countyId");
                if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                    return countyIdNode.get(0).asText();
                } else if (countyIdNode.isTextual()) {
                    return countyIdNode.asText();
                }
            } else if (jsonNode.has("attributes") && jsonNode.get("attributes").has("county_id")) {
                com.fasterxml.jackson.databind.JsonNode countyIdNode = jsonNode.get("attributes").get("county_id");
                if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                    return countyIdNode.get(0).asText();
                } else if (countyIdNode.isTextual()) {
                    return countyIdNode.asText();
                }
            }
            
            // NO FALLBACK - countyId MUST be in JWT token
            System.err.println("❌ CountyAccessController: countyId NOT FOUND in JWT token. Token must contain countyId in attributes.countyId.");
            java.util.List<String> fieldNames = new java.util.ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(fieldNames::add);
            System.err.println("❌ CountyAccessController: JWT payload keys: " + fieldNames);
            return null; // Explicitly fail - no default
        } catch (Exception e) {
            System.err.println("⚠️ CountyAccessController: Error extracting county from JWT: " + e.getMessage());
            return null;
        }
    }

    @GetMapping("/access-check/{userRole}/{countyName}")
    public ResponseEntity<Map<String, Object>> checkCountyAccess(@PathVariable String userRole,
                                                                 @PathVariable String countyName) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean canAccess = districtCountyService.canAccessCounty(userRole, countyName);
            response.put("status", "SUCCESS");
            response.put("userRole", userRole);
            response.put("countyName", countyName);
            response.put("canAccess", canAccess);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to check county access: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

