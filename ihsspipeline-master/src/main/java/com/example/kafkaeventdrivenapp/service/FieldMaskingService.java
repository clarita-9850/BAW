package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.entity.TimesheetEntity;
import com.example.kafkaeventdrivenapp.model.FieldMaskingRule;
import com.example.kafkaeventdrivenapp.model.FieldMaskingRules;
import com.example.kafkaeventdrivenapp.model.MaskedTimesheetData;
import com.example.kafkaeventdrivenapp.model.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FieldMaskingService {

    private final PersistentFieldMaskingService persistentFieldMaskingService;
    private final KeycloakFieldMaskingService keycloakFieldMaskingService;

    private Map<String, List<FieldMaskingRule>> roleBasedRules = new HashMap<>();
    private Map<String, List<String>> roleSelectedFields = new HashMap<>();

    public FieldMaskingService(PersistentFieldMaskingService persistentFieldMaskingService, 
                               KeycloakFieldMaskingService keycloakFieldMaskingService) {
        this.persistentFieldMaskingService = persistentFieldMaskingService;
        this.keycloakFieldMaskingService = keycloakFieldMaskingService;
        System.out.println("🔧 FieldMaskingService: Constructor called - JWT-ONLY approach (no hardcoded rules)");
        System.out.println("✅ FieldMaskingService: Constructor completed - Field visibility rules must come from JWT token only");
    }

    /**
     * Get field masking rules for a specific user role and report type (JWT-ONLY method)
     */
    public FieldMaskingRules getMaskingRules(String userRole, String reportType) {
        System.out.println("🔧 FieldMaskingService: getMaskingRules called (JWT-ONLY method) for role: " + userRole);
        throw new RuntimeException("Legacy method disabled. Use getMaskingRules(userRole, reportType, jwtToken) with JWT token.");
    }

    /**
     * Get field masking rules using Keycloak JWT token (JWT-ONLY method)
     * NO HARDCODED FALLBACKS - Field visibility rules MUST come from JWT token only
     */
    public FieldMaskingRules getMaskingRules(String userRole, String reportType, String jwtToken) {
        System.out.println("🔧 FieldMaskingService: getMaskingRules called (JWT-ONLY method) for role: " + userRole);
        System.out.println("🔧 JWT Token provided: " + (jwtToken != null ? "YES" : "NO"));
        
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            System.err.println("❌ FieldMaskingService: JWT token is required. No fallback methods available.");
            throw new RuntimeException("JWT token is required for field masking rules. Field visibility rules must be configured in Keycloak. No hardcoded fallbacks available.");
        }
        
        try {
            // Check if we need to fetch rules for a specific role (e.g., ADMIN viewing CASE_WORKER rules)
            // In this case, use getMaskingRulesForRole which uses admin token to fetch rules for any role
            UserRole requestedRole = toUserRole(userRole);
            System.out.println("🔧 FieldMaskingService: Requested role: " + requestedRole);
            
            // Try to extract role from JWT to see if it matches requested role
            UserRole jwtRole = null;
            try {
                jwtRole = keycloakFieldMaskingService.extractUserRoleFromJwt(jwtToken);
                System.out.println("🔧 FieldMaskingService: JWT role: " + jwtRole);
            } catch (Exception e) {
                System.out.println("⚠️ FieldMaskingService: Could not extract role from JWT, will use getMaskingRulesForRole");
            }
            
            FieldMaskingRules keycloakRules = null;
            
            // If requested role matches JWT role, use JWT-based method
            // Otherwise, use admin token to fetch rules for the requested role
            if (jwtRole != null && jwtRole.equals(requestedRole)) {
                System.out.println("🔧 FieldMaskingService: JWT role matches requested role, using JWT-based method...");
                keycloakRules = keycloakFieldMaskingService.getMaskingRulesFromToken(jwtToken, reportType);
            } else {
                System.out.println("🔧 FieldMaskingService: JWT role (" + jwtRole + ") differs from requested role (" + requestedRole + "), using admin token method...");
                keycloakRules = keycloakFieldMaskingService.getMaskingRulesForRole(userRole, reportType);
            }
            
            if (keycloakRules != null && keycloakRules.getRules() != null && !keycloakRules.getRules().isEmpty()) {
                System.out.println("✅ FieldMaskingService: Successfully retrieved " + keycloakRules.getRules().size() + " rules from Keycloak");
                keycloakRules.setUserRole(userRole);
                keycloakRules.setReportType(reportType);
                return normalizeRuleMetadata(keycloakRules);
            } else {
                System.err.println("❌ FieldMaskingService: No field masking rules found in Keycloak for role: " + userRole);
                throw new RuntimeException("No field masking rules found in Keycloak. Field visibility rules must be configured in Keycloak for role: " + userRole + ". No hardcoded fallbacks available.");
            }
            
        } catch (RuntimeException e) {
            // Re-throw RuntimeException (our validation errors)
            throw e;
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingService: Error getting masking rules from Keycloak: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to extract field masking rules from Keycloak. Field visibility rules must be configured in Keycloak. No hardcoded fallbacks available.", e);
        }
    }
    
    /**
     * Map specific role names to generic role types for rule lookup
     */
    private UserRole toUserRole(String userRole) {
        return UserRole.from(userRole);
    }

    /**
     * Apply field masking to timesheet data
     */
    public MaskedTimesheetData applyMasking(TimesheetEntity timesheet, FieldMaskingRules rules) {
        MaskedTimesheetData maskedData = new MaskedTimesheetData();
        maskedData.setTimesheetId(timesheet.getId() != null ? timesheet.getId().toString() : null);
        maskedData.setUserRole(rules.getUserRole());
        maskedData.setReportType(rules.getReportType());
        maskedData.setMaskedAt(LocalDateTime.now());

        Map<String, Object> fields = new HashMap<>();

        System.out.println("🔒 FieldMaskingService: Applying masking to timesheet " + timesheet.getId() + " with " + rules.getRules().size() + " rules");

        // NO HARDCODED FALLBACKS - Rules must come from JWT token
        if (rules.getRules() == null || rules.getRules().isEmpty()) {
            System.err.println("❌ FieldMaskingService: No masking rules found in JWT token");
            throw new RuntimeException("No field masking rules found. Field visibility rules must be configured in Keycloak. No hardcoded fallbacks available.");
        }
        
        // Apply rules-based masking from JWT token
        for (FieldMaskingRule rule : rules.getRules()) {
            System.out.println("🔒 FieldMaskingService: Processing rule for field: " + rule.getFieldName() + 
                             ", masking type: " + rule.getMaskingType() + 
                             ", access level: " + rule.getAccessLevel());
            
            // Only include fields that are not hidden
            if (rule.getAccessLevel() != FieldMaskingRule.AccessLevel.HIDDEN_ACCESS) {
                Object value = getFieldValue(timesheet, rule.getFieldName());
                Object maskedValue = applyMaskingRule(value, rule);
                System.out.println("🔒 FieldMaskingService: Field " + rule.getFieldName() + 
                                 " - Original: " + value + " -> Masked: " + maskedValue);
                fields.put(rule.getFieldName(), maskedValue);
            } else {
                System.out.println("🔒 FieldMaskingService: Skipping hidden field: " + rule.getFieldName());
            }
        }

        maskedData.setFields(fields);
        System.out.println("🔒 FieldMaskingService: Applied masking to " + fields.size() + " fields");
        return maskedData;
    }

    /**
     * Apply field masking to a list of timesheet entities (JWT-ONLY method)
     */
    public List<MaskedTimesheetData> applyFieldMasking(List<TimesheetEntity> timesheets, String userRole, String reportType) {
        System.out.println("🔒 FieldMaskingService: applyFieldMasking called (JWT-ONLY method) for role: " + userRole);
        throw new RuntimeException("Legacy method disabled. Use applyFieldMasking(timesheets, userRole, reportType, jwtToken) with JWT token.");
    }

    /**
     * Apply field masking using Keycloak JWT token (JWT-ONLY method)
     */
    public List<MaskedTimesheetData> applyFieldMasking(List<TimesheetEntity> timesheets, String userRole, String reportType, String jwtToken) {
        System.out.println("🔒 FieldMaskingService: Applying field masking (JWT-ONLY method) to " + timesheets.size() + " records for role: " + userRole);
        System.out.println("🔒 JWT Token provided: " + (jwtToken != null ? "YES" : "NO"));
        
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for field masking. No fallback methods available.");
        }
        
        try {
            // Get masking rules using Keycloak JWT token
            FieldMaskingRules rules = getMaskingRules(userRole, reportType, jwtToken);
            
            System.out.println("🔒 FieldMaskingService: Retrieved " + rules.getRules().size() + " masking rules");
            System.out.println("🔒 Rules source: Keycloak JWT");
            
            // Apply masking to each timesheet
            List<MaskedTimesheetData> maskedData = timesheets.stream()
                .map(timesheet -> applyMasking(timesheet, rules))
                .collect(Collectors.toList());
            
            System.out.println("✅ FieldMaskingService: Applied field masking to " + maskedData.size() + " records");
            System.out.println("✅ Field masking completed successfully using JWT-ONLY approach");
            return maskedData;
            
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingService: Error applying field masking: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to apply field masking: " + e.getMessage(), e);
        }
    }

    /**
     * Apply field masking for a recipient role using a system token for authentication
     * This allows using SYSTEM_SCHEDULER token for data access while applying recipient-specific masking
     */
    public List<MaskedTimesheetData> applyFieldMaskingForRecipient(List<TimesheetEntity> timesheets, String recipientRole, String reportType, String systemToken) {
        System.out.println("🔒 FieldMaskingService: Applying field masking for recipient role: " + recipientRole);
        System.out.println("🔒 System token provided: " + (systemToken != null ? "YES" : "NO"));
        System.out.println("🔒 Records to mask: " + timesheets.size());
        
        if (systemToken == null || systemToken.trim().isEmpty()) {
            throw new RuntimeException("System token is required for field masking. No fallback methods available.");
        }
        
        try {
            // Get masking rules based on recipient role (not the system token role)
            // We need to fetch rules for the recipient role, but we'll use a token for a role that has those rules
            // For now, we'll get rules directly for the recipient role
            FieldMaskingRules rules = getMaskingRulesForRecipient(recipientRole, reportType, systemToken);
            
            System.out.println("🔒 FieldMaskingService: Retrieved " + rules.getRules().size() + " masking rules for recipient role: " + recipientRole);
            
            // Apply masking to each timesheet
            List<MaskedTimesheetData> maskedData = timesheets.stream()
                .map(timesheet -> applyMasking(timesheet, rules))
                .collect(Collectors.toList());
            
            System.out.println("✅ FieldMaskingService: Applied field masking to " + maskedData.size() + " records for recipient role: " + recipientRole);
            return maskedData;
            
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingService: Error applying field masking for recipient: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to apply field masking for recipient: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get masking rules for a recipient role (JWT-ONLY method)
     * This method fetches rules based on recipient role from Keycloak
     * NO HARDCODED FALLBACKS - Rules must come from Keycloak
     */
    private FieldMaskingRules getMaskingRulesForRecipient(String recipientRole, String reportType, String systemToken) {
        // Use KeycloakFieldMaskingService to fetch rules for the recipient role
        // The system token is used for authentication, but we fetch rules for recipient role
        try {
            FieldMaskingRules rules = keycloakFieldMaskingService.getMaskingRulesForRole(recipientRole, reportType);
            
            if (rules != null && rules.getRules() != null && !rules.getRules().isEmpty()) {
                return normalizeRuleMetadata(rules);
            }
            
            // NO FALLBACK - Rules must be in Keycloak
            System.err.println("❌ FieldMaskingService: No rules found in Keycloak for recipient role: " + recipientRole);
            throw new RuntimeException("No field masking rules found in Keycloak for recipient role: " + recipientRole + ". Rules must be configured in Keycloak. No hardcoded fallbacks available.");
            
        } catch (RuntimeException e) {
            // Re-throw our validation errors
            throw e;
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingService: Error getting masking rules for recipient role: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get masking rules from Keycloak for recipient role: " + recipientRole + ". No hardcoded fallbacks available.", e);
        }
    }

    /**
     * Get field value from timesheet entity (updated for CMIPS schema)
     */
    private Object getFieldValue(TimesheetEntity timesheet, String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "id":
                return timesheet.getId();
            case "employeeid":
                return timesheet.getEmployeeId();
            case "employeename":
                return timesheet.getEmployeeName();
            case "userid":
                return timesheet.getUserId();
            case "department":
                return timesheet.getDepartment();
            case "location":
                return timesheet.getLocation();
            case "payperiodstart":
                return timesheet.getPayPeriodStart();
            case "payperiodend":
                return timesheet.getPayPeriodEnd();
            case "regularhours":
                return timesheet.getRegularHours() != null ? timesheet.getRegularHours().doubleValue() : null;
            case "overtimehours":
                return timesheet.getOvertimeHours() != null ? timesheet.getOvertimeHours().doubleValue() : null;
            case "sickhours":
                return timesheet.getSickHours() != null ? timesheet.getSickHours().doubleValue() : null;
            case "vacationhours":
                return timesheet.getVacationHours() != null ? timesheet.getVacationHours().doubleValue() : null;
            case "holidayhours":
                return timesheet.getHolidayHours() != null ? timesheet.getHolidayHours().doubleValue() : null;
            case "totalhours":
                return timesheet.getTotalHours() != null ? timesheet.getTotalHours().doubleValue() : null;
            case "status":
                return timesheet.getStatus();
            case "comments":
                return timesheet.getComments();
            case "supervisorcomments":
                return timesheet.getSupervisorComments();
            case "submittedat":
                return timesheet.getSubmittedAt();
            case "submittedby":
                return timesheet.getSubmittedBy();
            case "approvedat":
                return timesheet.getApprovedAt();
            case "approvedby":
                return timesheet.getApprovedBy();
            case "createdat":
                return timesheet.getCreatedAt();
            case "updatedat":
                return timesheet.getUpdatedAt();
            // Legacy field name mappings for backward compatibility with Keycloak rules
            case "timesheetid":
                return timesheet.getId() != null ? timesheet.getId().toString() : null;
            case "providerid":
                return timesheet.getEmployeeId(); // Map providerId -> employeeId
            case "providername":
                return timesheet.getEmployeeName(); // Map providerName -> employeeName
            case "startdate":
                return timesheet.getPayPeriodStart(); // Map startDate -> payPeriodStart
            case "enddate":
                return timesheet.getPayPeriodEnd(); // Map endDate -> payPeriodEnd
            case "servicelocation":
                return timesheet.getLocation(); // Map serviceLocation -> location
            default:
                return null;
        }
    }

    /**
     * Apply masking rule to field value
     */
    private Object applyMaskingRule(Object value, FieldMaskingRule rule) {
        if (value == null) {
            return null;
        }

        switch (rule.getMaskingType()) {
            case NONE:
                return value;
            case HIDDEN:
                return "***HIDDEN***";
            case PARTIAL_MASK:
                return applyPartialMask(value.toString(), rule.getMaskingPattern());
            case HASH_MASK:
                return "HASH_" + Math.abs(value.toString().hashCode());
            case ANONYMIZE:
                return applyAnonymization(value, rule.getFieldName());
            case AGGREGATE:
                return applyAggregation(value, rule.getFieldName());
            default:
                return value;
        }
    }

    /**
     * Apply partial masking (e.g., XXX-XX-1234)
     */
    private String applyPartialMask(String value, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return "***" + value.substring(Math.max(0, value.length() - 4));
        }
        
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (i < pattern.length() && pattern.charAt(i) == 'X') {
                masked.append('*');
            } else {
                masked.append(value.charAt(i));
            }
        }
        return masked.toString();
    }

    /**
     * Apply anonymization based on field type
     */
    private String applyAnonymization(Object value, String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "providerid":
            case "recipientid":
                return "USER_" + Math.abs(value.toString().hashCode() % 10000);
            case "provideremail":
            case "recipientemail":
                return "user" + Math.abs(value.toString().hashCode() % 1000) + "@company.com";
            case "providername":
            case "recipientname":
                return "User " + Math.abs(value.toString().hashCode() % 1000);
            default:
                return "ANONYMIZED_" + Math.abs(value.toString().hashCode() % 1000);
        }
    }

    /**
     * Apply aggregation (e.g., show only ranges)
     */
    private String applyAggregation(Object value, String fieldName) {
        if (value instanceof Number) {
            double num = ((Number) value).doubleValue();
            if (fieldName.toLowerCase().contains("hours")) {
                if (num < 20) return "0-20 hours";
                if (num < 40) return "20-40 hours";
                return "40+ hours";
            }
            if (fieldName.toLowerCase().contains("amount")) {
                if (num < 1000) return "$0-1000";
                if (num < 5000) return "$1000-5000";
                return "$5000+";
            }
        }
        return "AGGREGATED";
    }

    /**
     * Get available masking rules for a user role
     */
    public List<FieldMaskingRule> getAvailableRules(String userRole) {
        // Map specific role names to generic role types for rule lookup
        String roleType = toUserRole(userRole).name();
        return roleBasedRules.getOrDefault(roleType, new ArrayList<>());
    }

    /**
     * Update masking rules for a user role
     */
    public void updateRules(String userRole, List<FieldMaskingRule> rules) {
        // Map specific role names to generic role types for storage
        String roleType = toUserRole(userRole).name();
        roleBasedRules.put(roleType, rules);
        System.out.println("🔧 Updated masking rules for role: " + userRole + " (mapped to: " + roleType + ")");
    }

    /**
     * Update masking rules and selected fields for a user role
     * Saves rules to both Keycloak (primary) and local storage (backup)
     */
    public void updateRules(String userRole, List<FieldMaskingRule> rules, List<String> selectedFields) {
        System.out.println("🔧 FieldMaskingService: updateRules called for role: " + userRole);
        System.out.println("🔧 FieldMaskingService: rules count: " + (rules != null ? rules.size() : "null"));
        System.out.println("🔧 FieldMaskingService: selectedFields: " + selectedFields);
        
        // Convert string role to UserRole enum
        UserRole roleEnum = toUserRole(userRole);
        String roleType = roleEnum.name();
        
        // 1. Save to Keycloak (primary storage - rules will appear in JWT tokens)
        System.out.println("🔐 FieldMaskingService: Saving rules to Keycloak...");
        try {
            keycloakFieldMaskingService.updateRoleAttributesInKeycloak(roleEnum, rules);
            System.out.println("✅ FieldMaskingService: Successfully saved rules to Keycloak");
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingService: Failed to save rules to Keycloak: " + e.getMessage());
            throw new RuntimeException("Failed to save field masking rules to Keycloak: " + e.getMessage(), e);
        }
        
        // 2. Store rules locally (backup storage)
        System.out.println("🔧 FieldMaskingService: Saving rules to local storage (backup)...");
        roleBasedRules.put(roleType, rules);
        if (selectedFields != null) {
            roleSelectedFields.put(roleType, selectedFields);
        }
        
        // 3. Also save to persistent file storage (backup)
        persistentFieldMaskingService.updateRules(userRole, rules, selectedFields);
        
        System.out.println("✅ FieldMaskingService: updateRules completed successfully");
        System.out.println("✅ Rules saved to: Keycloak (primary), Local memory (backup), File storage (backup)");
    }

    /**
     * Get selected fields for a user role
     */
    public List<String> getSelectedFields(String userRole) {
        // First try to get from persistent storage
        List<String> persistentFields = persistentFieldMaskingService.getSelectedFields(userRole);
        if (persistentFields != null && !persistentFields.isEmpty()) {
            return persistentFields;
        }
        
        // Otherwise, fall back to in-memory storage
        String roleType = toUserRole(userRole).name();
        return roleSelectedFields.getOrDefault(roleType, new ArrayList<>());
    }

    /**
     * Sync rules to Keycloak for a specific role
     * NOTE: Field visibility rules should be set from frontend, not backend
     * Frontend will directly call Keycloak Admin API to update role attributes
     * This method is kept for backward compatibility but does nothing
     */
    public boolean syncRulesToKeycloak(String userRole, List<FieldMaskingRule> rules, List<String> selectedFields) {
        System.out.println("ℹ️  FieldMaskingService: syncRulesToKeycloak called but field visibility rules should be set from frontend");
        System.out.println("ℹ️  Frontend should directly call Keycloak Admin API to update role attributes");
        return true; // Return true to maintain compatibility
    }

    // REMOVED: initializeDefaultRules() - No hardcoded field visibility rules allowed
    // Field visibility rules MUST come from JWT token only
    
    // REMOVED: buildDefaultRules() - No hardcoded fallbacks allowed
    // Field visibility rules MUST come from JWT token only

    private FieldMaskingRules normalizeRuleMetadata(FieldMaskingRules source) {
        if (source == null || source.getRules() == null) {
            return source;
        }
        List<FieldMaskingRule> normalized = source.getRules().stream()
            .map(this::cloneRule)
            .collect(Collectors.toList());
        source.setRules(normalized);
        return source;
    }

    private FieldMaskingRule cloneRule(FieldMaskingRule source) {
        if (source == null) {
            return null;
        }
        FieldMaskingRule clone = new FieldMaskingRule();
        clone.setFieldName(source.getFieldName());
        clone.setMaskingType(source.getMaskingType());
        clone.setAccessLevel(source.getAccessLevel());
        clone.setMaskingPattern(source.getMaskingPattern());
        clone.setReportType(source.getReportType());
        clone.setDescription(source.getDescription());
        clone.setEnabled(source.isEnabled());
        return clone;
    }

    // REMOVED: syncDefaultRulesToKeycloak() - No hardcoded rules to sync
    // All rules must come from Keycloak via JWT token

    /**
     * Get masking statistics for a user role
     */
    public Map<String, Object> getMaskingStatistics(String userRole, String reportType) {
        System.out.println("📊 FieldMaskingService: Getting masking statistics for role: " + userRole);
        
        try {
            FieldMaskingRules rules = getMaskingRules(userRole, reportType);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("userRole", userRole);
            statistics.put("reportType", reportType);
            statistics.put("totalRules", rules.getRules().size());
            
            // Count masking types
            Map<String, Long> maskingTypeCounts = rules.getRules().stream()
                .collect(Collectors.groupingBy(
                    rule -> rule.getMaskingType().toString(),
                    Collectors.counting()
                ));
            statistics.put("maskingTypeCounts", maskingTypeCounts);
            
            // Count access levels
            Map<String, Long> accessLevelCounts = rules.getRules().stream()
                .collect(Collectors.groupingBy(
                    rule -> rule.getAccessLevel().toString(),
                    Collectors.counting()
                ));
            statistics.put("accessLevelCounts", accessLevelCounts);
            
            // Get field visibility summary
            Map<String, String> fieldVisibility = rules.getRules().stream()
                .collect(Collectors.toMap(
                    FieldMaskingRule::getFieldName,
                    rule -> rule.getMaskingType().toString(),
                    (existing, replacement) -> existing
                ));
            statistics.put("fieldVisibility", fieldVisibility);
            
            System.out.println("✅ FieldMaskingService: Retrieved masking statistics");
            return statistics;
            
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingService: Error getting masking statistics: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get masking statistics: " + e.getMessage(), e);
        }
    }

    /**
     * Get all available masking types
     */
    public List<String> getAvailableMaskingTypes() {
        return Arrays.asList(
            "NONE",
            "HIDDEN", 
            "PARTIAL_MASK",
            "HASH_MASK",
            "ANONYMIZE",
            "AGGREGATE"
        );
    }

    /**
     * Get all available access levels
     */
    public List<String> getAvailableAccessLevels() {
        return Arrays.asList(
            "FULL_ACCESS",
            "MASKED_ACCESS",
            "HIDDEN_ACCESS"
        );
    }

    // REMOVED: createRule() - No longer needed since we don't create hardcoded rules
    // All field visibility rules must come from JWT token via Keycloak

}
