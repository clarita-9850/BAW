package com.cmips.model;

import java.util.List;

public class FieldMaskingRules {
    private String userRole;
    private String reportType;
    private List<FieldMaskingRule> rules;

    public FieldMaskingRules() {}

    public FieldMaskingRules(String userRole, String reportType, List<FieldMaskingRule> rules) {
        this.userRole = userRole;
        this.reportType = reportType;
        this.rules = rules;
    }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public List<FieldMaskingRule> getRules() { return rules; }
    public void setRules(List<FieldMaskingRule> rules) { this.rules = rules; }
}

