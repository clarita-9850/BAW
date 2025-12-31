package com.cmips.model;

import java.util.List;

public class FieldMaskingRequest {
    private String userRole;  // Target role for rule updates
    private List<FieldMaskingRule> rules;
    private List<String> selectedFields;
    
    // Default constructor
    public FieldMaskingRequest() {}
    
    // Constructor with parameters
    public FieldMaskingRequest(String userRole, List<FieldMaskingRule> rules, List<String> selectedFields) {
        this.userRole = userRole;
        this.rules = rules;
        this.selectedFields = selectedFields;
    }
    
    // Getters and setters
    public String getUserRole() {
        return userRole;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    
    public List<FieldMaskingRule> getRules() {
        return rules;
    }
    
    public void setRules(List<FieldMaskingRule> rules) {
        this.rules = rules;
    }
    
    public List<String> getSelectedFields() {
        return selectedFields;
    }
    
    public void setSelectedFields(List<String> selectedFields) {
        this.selectedFields = selectedFields;
    }
    
    @Override
    public String toString() {
        return "FieldMaskingRequest{" +
                "userRole='" + userRole + '\'' +
                ", rules=" + (rules != null ? rules.size() : 0) + " rules" +
                ", selectedFields=" + (selectedFields != null ? selectedFields.size() : 0) + " fields" +
                '}';
    }
}

