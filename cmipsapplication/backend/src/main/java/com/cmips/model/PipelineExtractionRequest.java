package com.cmips.model;

import java.time.LocalDate;
import java.util.List;

public class PipelineExtractionRequest {
    private String userRole;
    private String reportType;
    private DateRange dateRange;
    private List<String> statusFilter;
    private List<String> providerFilter;
    private List<String> projectFilter;
    private List<String> fieldSelection;
    private boolean includeMaskedFields = true;
    private boolean includeHiddenFields = false;

    // County-based access control
    private String userCounty;
    private List<String> allowedCounties;
    private boolean enforceCountyAccess = true;

    public PipelineExtractionRequest() {}

    public PipelineExtractionRequest(String userRole, String reportType, DateRange dateRange,
                                     List<String> statusFilter, List<String> providerFilter,
                                     List<String> projectFilter, List<String> fieldSelection,
                                     boolean includeMaskedFields, boolean includeHiddenFields,
                                     String userCounty, List<String> allowedCounties, boolean enforceCountyAccess) {
        this.userRole = userRole;
        this.reportType = reportType;
        this.dateRange = dateRange;
        this.statusFilter = statusFilter;
        this.providerFilter = providerFilter;
        this.projectFilter = projectFilter;
        this.fieldSelection = fieldSelection;
        this.includeMaskedFields = includeMaskedFields;
        this.includeHiddenFields = includeHiddenFields;
        this.userCounty = userCounty;
        this.allowedCounties = allowedCounties;
        this.enforceCountyAccess = enforceCountyAccess;
    }

    // Getters and Setters
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public DateRange getDateRange() { return dateRange; }
    public void setDateRange(DateRange dateRange) { this.dateRange = dateRange; }

    public List<String> getStatusFilter() { return statusFilter; }
    public void setStatusFilter(List<String> statusFilter) { this.statusFilter = statusFilter; }

    public List<String> getProviderFilter() { return providerFilter; }
    public void setProviderFilter(List<String> providerFilter) { this.providerFilter = providerFilter; }

    public List<String> getProjectFilter() { return projectFilter; }
    public void setProjectFilter(List<String> projectFilter) { this.projectFilter = projectFilter; }

    public List<String> getFieldSelection() { return fieldSelection; }
    public void setFieldSelection(List<String> fieldSelection) { this.fieldSelection = fieldSelection; }

    public boolean isIncludeMaskedFields() { return includeMaskedFields; }
    public void setIncludeMaskedFields(boolean includeMaskedFields) { this.includeMaskedFields = includeMaskedFields; }

    public boolean isIncludeHiddenFields() { return includeHiddenFields; }
    public void setIncludeHiddenFields(boolean includeHiddenFields) { this.includeHiddenFields = includeHiddenFields; }

    public String getUserCounty() { return userCounty; }
    public void setUserCounty(String userCounty) { this.userCounty = userCounty; }

    public List<String> getAllowedCounties() { return allowedCounties; }
    public void setAllowedCounties(List<String> allowedCounties) { this.allowedCounties = allowedCounties; }

    public boolean isEnforceCountyAccess() { return enforceCountyAccess; }
    public void setEnforceCountyAccess(boolean enforceCountyAccess) { this.enforceCountyAccess = enforceCountyAccess; }

    // Inner class for DateRange
    public static class DateRange {
        private LocalDate startDate;
        private LocalDate endDate;

        public DateRange() {}

        public DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
}

