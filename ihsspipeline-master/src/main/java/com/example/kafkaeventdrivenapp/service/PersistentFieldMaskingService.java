package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.FieldMaskingRule;
import com.example.kafkaeventdrivenapp.model.FieldMaskingRules;
import com.example.kafkaeventdrivenapp.model.UserRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PersistentFieldMaskingService {

    private static final String RULES_FILE = "field-masking-rules.json";
    private static final String SELECTED_FIELDS_FILE = "selected-fields.json";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, List<FieldMaskingRule>> roleBasedRules = new HashMap<>();
    private Map<String, List<String>> roleSelectedFields = new HashMap<>();

    public PersistentFieldMaskingService() {
        System.out.println("🔧 PersistentFieldMaskingService: Constructor called - loading persisted rules...");
        try {
            loadPersistedRules();
            System.out.println("✅ PersistentFieldMaskingService: Constructor completed successfully");
        } catch (Exception e) {
            System.err.println("❌ PersistentFieldMaskingService: Constructor failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load persisted rules from file
     */
    private void loadPersistedRules() {
        try {
            // Load rules
            File rulesFile = new File(RULES_FILE);
            if (rulesFile.exists()) {
                roleBasedRules = objectMapper.readValue(rulesFile, new TypeReference<Map<String, List<FieldMaskingRule>>>() {});
                System.out.println("🔧 Loaded persisted rules for " + roleBasedRules.size() + " roles");
            } else {
                System.out.println("🔧 No persisted rules file found, using empty rules");
                roleBasedRules = new HashMap<>();
            }

            // Load selected fields
            File selectedFieldsFile = new File(SELECTED_FIELDS_FILE);
            if (selectedFieldsFile.exists()) {
                roleSelectedFields = objectMapper.readValue(selectedFieldsFile, new TypeReference<Map<String, List<String>>>() {});
                System.out.println("🔧 Loaded persisted selected fields for " + roleSelectedFields.size() + " roles");
            } else {
                System.out.println("🔧 No persisted selected fields file found, using empty fields");
                roleSelectedFields = new HashMap<>();
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading persisted rules: " + e.getMessage());
            roleBasedRules = new HashMap<>();
            roleSelectedFields = new HashMap<>();
        }
    }

    /**
     * Save rules to file
     */
    private void saveRulesToFile() {
        try {
            // Save rules
            objectMapper.writeValue(new File(RULES_FILE), roleBasedRules);
            System.out.println("🔧 Saved rules to file: " + RULES_FILE);
            
            // Save selected fields
            objectMapper.writeValue(new File(SELECTED_FIELDS_FILE), roleSelectedFields);
            System.out.println("🔧 Saved selected fields to file: " + SELECTED_FIELDS_FILE);
            
        } catch (IOException e) {
            System.err.println("❌ Error saving rules to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get field masking rules for a specific user role and report type
     */
    public FieldMaskingRules getMaskingRules(String userRole, String reportType) {
        // Map specific role names to generic role types for rule lookup
        String roleType = mapToGenericRole(userRole);
        List<FieldMaskingRule> rules = roleBasedRules.getOrDefault(roleType, new ArrayList<>());
        
        // Filter rules based on report type
        List<FieldMaskingRule> filteredRules = rules.stream()
            .filter(rule -> rule.getReportType() == null || rule.getReportType().equals(reportType))
            .collect(Collectors.toList());

        FieldMaskingRules maskingRules = new FieldMaskingRules();
        maskingRules.setUserRole(roleType);
        maskingRules.setReportType(reportType);
        maskingRules.setRules(filteredRules);
        
        return maskingRules;
    }

    /**
     * Update masking rules and selected fields for a user role
     */
    public void updateRules(String userRole, List<FieldMaskingRule> rules, List<String> selectedFields) {
        System.out.println("🔧 PersistentFieldMaskingService: updateRules called for role: " + userRole);
        System.out.println("🔧 PersistentFieldMaskingService: rules count: " + (rules != null ? rules.size() : "null"));
        System.out.println("🔧 PersistentFieldMaskingService: selectedFields: " + selectedFields);
        
        // Map specific role names to generic role types for storage
        String roleType = mapToGenericRole(userRole);
        System.out.println("🔧 PersistentFieldMaskingService: mapped role type: " + roleType);
        
        roleBasedRules.put(roleType, rules);
        System.out.println("🔧 PersistentFieldMaskingService: stored rules for role type: " + roleType);
        
        if (selectedFields != null) {
            roleSelectedFields.put(roleType, selectedFields);
            System.out.println("🔧 PersistentFieldMaskingService: stored selected fields for role type: " + roleType);
        }
        
        // Save to file
        saveRulesToFile();
        
        System.out.println("✅ PersistentFieldMaskingService: updateRules completed successfully");
    }

    /**
     * Get selected fields for a user role
     */
    public List<String> getSelectedFields(String userRole) {
        // Map specific role names to generic role types for rule lookup
        String roleType = mapToGenericRole(userRole);
        return roleSelectedFields.getOrDefault(roleType, new ArrayList<>());
    }

    /**
     * Map specific role names to generic role types for rule lookup.
     * NO FALLBACK - role MUST be provided or operation fails.
     * @throws IllegalArgumentException if userRole is null or invalid
     */
    private String mapToGenericRole(String userRole) {
        if (userRole == null || userRole.isBlank()) {
            throw new IllegalArgumentException("User role is required - cannot be null or empty");
        }
        return UserRole.from(userRole).name();
    }
}
