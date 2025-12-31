package com.cmips.controller;

import com.cmips.model.FieldMaskingRule;
import com.cmips.model.FieldMaskingRules;
import com.cmips.model.FieldMaskingRequest;
import com.cmips.model.UserRole;
import com.cmips.service.FieldMaskingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/field-masking")
@CrossOrigin(origins = "*")
public class FieldMaskingController {

    @Autowired
    private FieldMaskingService fieldMaskingService;

    /**
     * Get field masking interface for the authenticated user's role
     */
    @GetMapping("/interface/{userRole}")
    public ResponseEntity<Map<String, Object>> getFieldMaskingInterface(
            @PathVariable String userRole,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔒 FieldMaskingController: Getting masking interface for role: " + userRole);
            
            // Extract JWT token from request
            String jwtToken = extractJwtTokenFromRequest(request);
            
            // Get rules from service
            FieldMaskingRules rules = fieldMaskingService.getMaskingRules(userRole, "TIMESHEET_REPORT", jwtToken);
            List<String> selectedFields = fieldMaskingService.getSelectedFields(userRole);
            
            // Create interface data
            Map<String, Object> interfaceData = new HashMap<>();
            interfaceData.put("userRole", userRole);
            interfaceData.put("rules", rules.getRules());
            interfaceData.put("selectedFields", selectedFields);
            interfaceData.put("availableFields", getAvailableFieldsData());
            interfaceData.put("maskingTypes", getMaskingTypes());
            interfaceData.put("accessLevels", getAccessLevels());
            
            response.put("status", "SUCCESS");
            response.put("message", "Field masking interface retrieved successfully");
            response.put("interface", interfaceData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting masking interface: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get field masking interface: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Update field masking rules
     */
    @PostMapping("/update-rules")
    public ResponseEntity<Map<String, Object>> updateFieldMaskingRules(
            @RequestBody FieldMaskingRequest request,
            HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔧 FieldMaskingController: Updating masking rules for role: " + request.getUserRole());
            
            fieldMaskingService.updateRules(
                request.getUserRole(),
                request.getRules(),
                request.getSelectedFields()
            );
            
            response.put("status", "SUCCESS");
            response.put("message", "Field masking rules updated successfully");
            response.put("userRole", request.getUserRole());
            response.put("rulesCount", request.getRules().size());
            
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
     * Get available fields for masking
     */
    @GetMapping("/available-fields")
    public ResponseEntity<Map<String, Object>> getAvailableFields() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> fieldsData = getAvailableFieldsData();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = (List<Map<String, Object>>) fieldsData.get("fields");
            
            response.put("status", "SUCCESS");
            response.put("fields", fields);
            response.put("totalFields", fields != null ? fields.size() : 0);
            
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
     * Get available roles for field masking configuration
     */
    @GetMapping("/available-roles")
    public ResponseEntity<Map<String, Object>> getAvailableRoles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> roles = List.of(
                "ADMIN",
                "SUPERVISOR",
                "CASE_WORKER",
                "PROVIDER",
                "RECIPIENT"
            );
            
            response.put("status", "SUCCESS");
            response.put("roles", roles);
            response.put("totalRoles", roles.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting available roles: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get available roles: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Extract JWT token from HTTP request
     */
    private String extractJwtTokenFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error extracting JWT token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get available fields data for interface
     */
    private Map<String, Object> getAvailableFieldsData() {
        List<Map<String, Object>> fields = List.of(
            Map.of("name", "id", "displayName", "Timesheet ID", "type", "String"),
            Map.of("name", "employeeId", "displayName", "Employee ID", "type", "String"),
            Map.of("name", "employeeName", "displayName", "Employee Name", "type", "String"),
            Map.of("name", "department", "displayName", "Department", "type", "String"),
            Map.of("name", "location", "displayName", "Location", "type", "String"),
            Map.of("name", "totalHours", "displayName", "Total Hours", "type", "Double"),
            Map.of("name", "status", "displayName", "Status", "type", "String"),
            Map.of("name", "submittedAt", "displayName", "Submitted At", "type", "DateTime"),
            Map.of("name", "approvedAt", "displayName", "Approved At", "type", "DateTime")
        );
        
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
            Map.of("value", "PARTIAL_MASK", "displayName", "Partial Mask", "description", "Show partial data"),
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
}

