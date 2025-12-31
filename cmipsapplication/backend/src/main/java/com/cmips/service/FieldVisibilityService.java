package com.cmips.service;

import com.cmips.model.FieldMaskingRule;
import com.cmips.model.FieldMaskingRules;
import com.cmips.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FieldVisibilityService {

    @Autowired
    private FieldMaskingService fieldMaskingService;

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
     */
    public List<String> getVisibleFields(String userRole, String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for field visibility. No fallback methods available.");
        }
        
        String canonicalRole = UserRole.from(userRole).name();
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(canonicalRole, "TIMESHEET_REPORT", jwtToken);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        System.out.println("🔍 FieldVisibilityService: Getting visible fields for role: " + canonicalRole);
        System.out.println("🔍 FieldVisibilityService: Found " + rules.size() + " rules");
        
        List<String> visibleFields;
        if (rules == null || rules.isEmpty()) {
            System.out.println("⚠️ FieldVisibilityService: No masking rules available, falling back to default field list");
            visibleFields = getDefaultVisibleFields();
        } else {
            visibleFields = rules.stream()
                    .filter(rule -> rule.getAccessLevel() != FieldMaskingRule.AccessLevel.HIDDEN_ACCESS)
                    .map(FieldMaskingRule::getFieldName)
                    .map(this::convertToCamelCase)
                    .collect(Collectors.toList());
        }
        
        System.out.println("🔍 FieldVisibilityService: Visible fields: " + visibleFields);
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
     * Get hidden fields for a specific role
     * These fields should not be displayed in the report at all
     */
    public List<String> getHiddenFields(String userRole) {
        String canonicalRole = UserRole.from(userRole).name();
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(canonicalRole, "TIMESHEET_REPORT", null);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        if (rules == null || rules.isEmpty()) {
            return new ArrayList<>();
        }
        
        return rules.stream()
                .filter(rule -> rule.getAccessLevel() == FieldMaskingRule.AccessLevel.HIDDEN_ACCESS)
                .map(FieldMaskingRule::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * Get masked fields for a specific role
     * These fields are visible but with masked values
     */
    public List<String> getMaskedFields(String userRole) {
        String canonicalRole = UserRole.from(userRole).name();
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(canonicalRole, "TIMESHEET_REPORT", null);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        if (rules == null || rules.isEmpty()) {
            return new ArrayList<>();
        }
        
        return rules.stream()
                .filter(rule -> rule.getAccessLevel() == FieldMaskingRule.AccessLevel.MASKED_ACCESS)
                .map(FieldMaskingRule::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * Get full access fields for a specific role
     * These fields are visible with original values
     */
    public List<String> getFullAccessFields(String userRole) {
        String canonicalRole = UserRole.from(userRole).name();
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(canonicalRole, "TIMESHEET_REPORT", null);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        if (rules == null || rules.isEmpty()) {
            return new ArrayList<>();
        }
        
        return rules.stream()
                .filter(rule -> rule.getAccessLevel() == FieldMaskingRule.AccessLevel.FULL_ACCESS)
                .map(FieldMaskingRule::getFieldName)
                .collect(Collectors.toList());
    }

    public List<String> getDefaultVisibleFields() {
        return Arrays.asList(
                "timesheetId",
                "providerId",
                "providerName",
                "providerEmail",
                "providerDepartment",
                "providerGender",
                "providerDateOfBirth",
                "providerEthnicity",
                "providerCounty",
                "recipientId",
                "recipientName",
                "recipientEmail",
                "recipientGender",
                "recipientDateOfBirth",
                "recipientEthnicity",
                "recipientCounty",
                "startDate",
                "endDate",
                "totalHours",
                "hourlyRate",
                "totalAmount",
                "status",
                "createdAt",
                "approvedAt",
                "approvalComments",
                "serviceLocation",
                "serviceType",
                "serviceCategory",
                "priorityLevel"
        );
    }

    /**
     * Get field visibility summary for a role
     */
    public Map<String, Object> getFieldVisibilitySummary(String userRole) {
        Map<String, Object> summary = new HashMap<>();
        
        // Use default fields when no JWT token available
        List<String> visibleFields = getDefaultVisibleFields();
        List<String> hiddenFields = getHiddenFields(userRole);
        List<String> maskedFields = getMaskedFields(userRole);
        List<String> fullAccessFields = getFullAccessFields(userRole);
        
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
     * Check if a specific field should be visible for a role
     */
    public boolean isFieldVisible(String userRole, String fieldName) {
        List<String> visibleFields = getDefaultVisibleFields();
        return visibleFields.contains(fieldName.toLowerCase());
    }

    /**
     * Get field access level for a specific field and role
     */
    public String getFieldAccessLevel(String userRole, String fieldName) {
        FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(UserRole.from(userRole).name(), "TIMESHEET_REPORT", null);
        List<FieldMaskingRule> rules = maskingRules.getRules();
        
        if (rules == null || rules.isEmpty()) {
            return "FULL_ACCESS"; // Default to full access if no rules
        }
        
        Optional<FieldMaskingRule> rule = rules.stream()
                .filter(r -> r.getFieldName().equalsIgnoreCase(fieldName))
                .findFirst();
        
        if (rule.isPresent()) {
            return rule.get().getAccessLevel().toString();
        }
        
        return "FULL_ACCESS"; // Default to full access if not found
    }

    /**
     * Get all available field names across all roles
     */
    public List<String> getAllAvailableFields() {
        Set<String> allFields = new HashSet<>();
        
        for (UserRole role : UserRole.values()) {
            FieldMaskingRules maskingRules = fieldMaskingService.getMaskingRules(role.name(), "TIMESHEET_REPORT", null);
            List<FieldMaskingRule> rules = maskingRules.getRules();
            if (rules != null && !rules.isEmpty()) {
                allFields.addAll(rules.stream()
                        .map(FieldMaskingRule::getFieldName)
                        .collect(Collectors.toList()));
            }
        }
        
        // If no rules found, return default fields
        if (allFields.isEmpty()) {
            return getDefaultVisibleFields();
        }
        
        return new ArrayList<>(allFields);
    }
}

