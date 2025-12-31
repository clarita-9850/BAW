package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.FieldMaskingRule;
import com.example.kafkaeventdrivenapp.model.FieldMaskingRules;
import com.example.kafkaeventdrivenapp.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FieldVisibilityService {

    @Autowired
    private FieldMaskingService fieldMaskingService;
    
    @Autowired
    private com.example.kafkaeventdrivenapp.config.ReportTypeProperties reportTypeProperties;

    /**
     * Get visible fields for a specific role based on field masking rules (JWT-ONLY method)
     * Only fields with FULL_ACCESS or MASKED_ACCESS should be visible
     * Fields with HIDDEN_ACCESS should not be shown at all
     */
    public List<String> getVisibleFields(String userRole) {
        System.out.println("🔍 FieldVisibilityService: getVisibleFields called (JWT-ONLY method) for role: " + userRole);
        throw new RuntimeException("Legacy method disabled. Use getVisibleFields(userRole, jwtToken) with JWT token.");
    }
    
    /**
     * Get visible fields for a specific role based on field masking rules (JWT-ONLY method)
     * Field visibility rules MUST come from JWT token - no hardcoded fallbacks
     */
    public List<String> getVisibleFields(String userRole, String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for field visibility. No fallback methods available.");
        }
        
        String canonicalRole = UserRole.from(userRole).name();
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(canonicalRole, reportTypeProperties.getDefaultReportType(), jwtToken);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        System.out.println("🔍 FieldVisibilityService: Getting visible fields for role: " + canonicalRole);
        System.out.println("🔍 FieldVisibilityService: Found " + rules.size() + " rules from JWT");
        
        if (rules == null || rules.isEmpty()) {
            System.err.println("❌ FieldVisibilityService: No masking rules found in JWT token for role: " + canonicalRole);
            throw new RuntimeException("No field masking rules found in JWT token. Field visibility rules must be configured in Keycloak. No fallback available.");
        }
        
        List<String> visibleFields = rules.stream()
                .filter(rule -> rule.getAccessLevel() != FieldMaskingRule.AccessLevel.HIDDEN_ACCESS)
                .map(FieldMaskingRule::getFieldName)
                .map(this::convertToCamelCase)
                .collect(Collectors.toList());
        
        System.out.println("🔍 FieldVisibilityService: Visible fields from JWT: " + visibleFields);
        return visibleFields;
    }
    
    /**
     * Convert field name from lowercase to camelCase for ReportRecord mapping
     */
    private String convertToCamelCase(String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "timesheetid": return "timesheetId";
            case "providerid": return "providerId";
            case "providername": return "providerName";
            case "provideremail": return "providerEmail";
            case "providerdepartment": return "providerDepartment";
            case "providergender": return "providerGender";
            case "providerdateofbirth": return "providerDateOfBirth";
            case "providerethnicity": return "providerEthnicity";
            case "providercounty": return "providerCounty";
            case "recipientid": return "recipientId";
            case "recipientname": return "recipientName";
            case "recipientemail": return "recipientEmail";
            case "recipientgender": return "recipientGender";
            case "recipientdateofbirth": return "recipientDateOfBirth";
            case "recipientethnicity": return "recipientEthnicity";
            case "recipientcounty": return "recipientCounty";
            case "startdate": return "startDate";
            case "enddate": return "endDate";
            case "totalhours": return "totalHours";
            case "hourlyrate": return "hourlyRate";
            case "totalamount": return "totalAmount";
            case "submittedat": return "submittedAt";
            case "approvedat": return "approvedAt";
            case "approvalcomments": return "approvalComments";
            case "rejectionreason": return "rejectionReason";
            case "revisioncount": return "revisionCount";
            case "validationresult": return "validationResult";
            case "validationmessage": return "validationMessage";
            case "service_type": return "serviceType";
            case "provider_county": return "providerCounty";
            case "recipient_county": return "recipientCounty";
            case "project_county": return "projectCounty";
            case "district_id": return "districtId";
            case "district_name": return "districtName";
            case "service_location": return "serviceLocation";
            case "service_category": return "serviceCategory";
            case "priority_level": return "priorityLevel";
            default: return fieldName;
        }
    }

    /**
     * Get hidden fields for a specific role (JWT-ONLY method)
     * These fields should not be displayed in the report at all
     * Field visibility rules MUST come from JWT token
     */
    public List<String> getHiddenFields(String userRole, String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for field visibility. No fallback methods available.");
        }
        
        String canonicalRole = UserRole.from(userRole).name();
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(canonicalRole, reportTypeProperties.getDefaultReportType(), jwtToken);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        if (rules == null || rules.isEmpty()) {
            throw new RuntimeException("No field masking rules found in JWT token. Field visibility rules must be configured in Keycloak.");
        }
        
        return rules.stream()
                .filter(rule -> rule.getAccessLevel() == FieldMaskingRule.AccessLevel.HIDDEN_ACCESS)
                .map(FieldMaskingRule::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * Get masked fields for a specific role (JWT-ONLY method)
     * These fields are visible but with masked values
     * Field visibility rules MUST come from JWT token
     */
    public List<String> getMaskedFields(String userRole, String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for field visibility. No fallback methods available.");
        }
        
        String canonicalRole = UserRole.from(userRole).name();
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(canonicalRole, reportTypeProperties.getDefaultReportType(), jwtToken);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        if (rules == null || rules.isEmpty()) {
            throw new RuntimeException("No field masking rules found in JWT token. Field visibility rules must be configured in Keycloak.");
        }
        
        return rules.stream()
                .filter(rule -> rule.getAccessLevel() == FieldMaskingRule.AccessLevel.MASKED_ACCESS)
                .map(FieldMaskingRule::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * Get full access fields for a specific role (JWT-ONLY method)
     * These fields are visible with original values
     * Field visibility rules MUST come from JWT token
     */
    public List<String> getFullAccessFields(String userRole, String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for field visibility. No fallback methods available.");
        }
        
        String canonicalRole = UserRole.from(userRole).name();
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(canonicalRole, reportTypeProperties.getDefaultReportType(), jwtToken);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        if (rules == null || rules.isEmpty()) {
            throw new RuntimeException("No field masking rules found in JWT token. Field visibility rules must be configured in Keycloak.");
        }
        
        return rules.stream()
                .filter(rule -> rule.getAccessLevel() == FieldMaskingRule.AccessLevel.FULL_ACCESS)
                .map(FieldMaskingRule::getFieldName)
                .collect(Collectors.toList());
    }

    // REMOVED: getDefaultVisibleFields() - No hardcoded fallbacks allowed
    // Field visibility MUST come from JWT token only

    /**
     * Get field visibility summary for a role (JWT-ONLY method)
     * Field visibility rules MUST come from JWT token
     */
    public Map<String, Object> getFieldVisibilitySummary(String userRole, String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for field visibility. No fallback methods available.");
        }
        
        Map<String, Object> summary = new HashMap<>();
        
        List<String> visibleFields = getVisibleFields(userRole, jwtToken);
        List<String> hiddenFields = getHiddenFields(userRole, jwtToken);
        List<String> maskedFields = getMaskedFields(userRole, jwtToken);
        List<String> fullAccessFields = getFullAccessFields(userRole, jwtToken);
        
        summary.put("userRole", UserRole.from(userRole).name());
        summary.put("totalFields", visibleFields.size() + hiddenFields.size());
        summary.put("visibleFields", visibleFields);
        summary.put("hiddenFields", hiddenFields);
        summary.put("maskedFields", maskedFields);
        summary.put("fullAccessFields", fullAccessFields);
        summary.put("visibleFieldCount", visibleFields.size());
        summary.put("hiddenFieldCount", hiddenFields.size());
        summary.put("maskedFieldCount", maskedFields.size());
        summary.put("fullAccessFieldCount", fullAccessFields.size());
        
        return summary;
    }

    /**
     * Check if a specific field should be visible for a role (JWT-ONLY method)
     * Field visibility rules MUST come from JWT token
     */
    public boolean isFieldVisible(String userRole, String fieldName, String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for field visibility. No fallback methods available.");
        }
        
        List<String> visibleFields = getVisibleFields(userRole, jwtToken);
        return visibleFields.contains(fieldName.toLowerCase());
    }

    /**
     * Get field access level for a specific field and role (JWT-ONLY method)
     * Field visibility rules MUST come from JWT token
     */
    public String getFieldAccessLevel(String userRole, String fieldName, String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for field visibility. No fallback methods available.");
        }
        
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(UserRole.from(userRole).name(), reportTypeProperties.getDefaultReportType(), jwtToken);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        if (rules == null || rules.isEmpty()) {
            throw new RuntimeException("No field masking rules found in JWT token. Field visibility rules must be configured in Keycloak.");
        }
        
        Optional<FieldMaskingRule> rule = rules.stream()
                .filter(r -> r.getFieldName().equalsIgnoreCase(fieldName))
                .findFirst();
        
        if (rule.isPresent()) {
            return rule.get().getAccessLevel().toString();
        }
        
        // If field not found in JWT rules, it's hidden (no hardcoded defaults)
        return "HIDDEN_ACCESS";
    }

    /**
     * Get all available field names for a specific role from JWT token (JWT-ONLY method)
     * Field visibility rules MUST come from JWT token - cannot aggregate across all roles without JWT
     */
    public List<String> getAllAvailableFields(String userRole, String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for field visibility. No fallback methods available.");
        }
        
        String canonicalRole = UserRole.from(userRole).name();
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(canonicalRole, reportTypeProperties.getDefaultReportType(), jwtToken);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        if (rules == null || rules.isEmpty()) {
            throw new RuntimeException("No field masking rules found in JWT token. Field visibility rules must be configured in Keycloak.");
        }
        
        return rules.stream()
                .map(FieldMaskingRule::getFieldName)
                .collect(Collectors.toList());
    }
}
