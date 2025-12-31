package com.cmips.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Query Builder Service
 * Builds dynamic queries using Spring Data JPA with basic filters and parameters
 * Note: Role-based access control is now handled by Keycloak
 */
@Service
public class QueryBuilderService {
    
    public QueryBuilderService() {
        System.out.println("🔧 QueryBuilderService: Constructor called - initializing...");
        System.out.println("✅ QueryBuilderService: Constructor completed successfully");
    }

    /**
     * Builds query parameters for data extraction based on filters
     * Note: Role-based access control is now handled by Keycloak
     */
    public QueryParameters buildQuery(String userRole, String countyId,
                                    LocalDate startDate, LocalDate endDate, Map<String, Object> additionalFilters) {
        System.out.println("🔧 QueryBuilderService: Building query for role: " + userRole);
        System.out.println("🔧 QueryBuilderService: County ID: " + countyId);
        System.out.println("🔧 QueryBuilderService: Start Date: " + startDate);
        System.out.println("🔧 QueryBuilderService: End Date: " + endDate);
        
        try {
            QueryParameters queryParams = new QueryParameters();
            
            // Set basic parameters
            queryParams.setUserRole(userRole);
            queryParams.setCountyId(countyId);
            queryParams.setStartDate(startDate);
            queryParams.setEndDate(endDate);
            
            System.out.println("🔧 QueryBuilderService: Set county ID to: " + queryParams.getCountyId());
            
            // Apply date range filters
            applyDateRangeFilters(queryParams, startDate, endDate);
            
            // Apply role-based filters
            applyRoleBasedFilters(queryParams, userRole);
            
            // Apply additional filters
            if (additionalFilters != null && !additionalFilters.isEmpty()) {
                queryParams.setAdditionalFilters(additionalFilters);
            }
            
            System.out.println("✅ QueryBuilderService: Query built successfully");
            return queryParams;
            
        } catch (Exception e) {
            System.err.println("❌ QueryBuilderService: Error building query: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to build query: " + e.getMessage(), e);
        }
    }

    /**
     * Applies date range filters to query parameters
     */
    private void applyDateRangeFilters(QueryParameters queryParams, LocalDate startDate, LocalDate endDate) {
        if (startDate != null) {
            queryParams.addFilter("startDateFilter", startDate);
        }
        
        if (endDate != null) {
            queryParams.addFilter("endDateFilter", endDate);
        }
        
        // Validate date range
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
    
    /**
     * Applies role-based filters to query parameters.
     *
     * IMPORTANT:
     * Role → geography mapping (districtId / countyId) is now handled by KeycloakService
     * and passed through the API. This method intentionally does NOT invent or
     * transform district/county values based on hard-coded role names anymore.
     *
     * The only responsibility here is to keep the existing filters map available
     * for any future non-geographic filters.
     */
    private void applyRoleBasedFilters(QueryParameters queryParams, String userRole) {
        // No-op for now: all geography comes from canonical districtId/countyId
        // that are already set on QueryParameters by the caller.
        System.out.println("🔧 QueryBuilderService: applyRoleBasedFilters() - geography handled upstream (KeycloakService). Role=" + userRole);
    }

    /**
     * Query parameters class
     */
    public static class QueryParameters {
        private String userRole;
        private String userId; // User ID from Keycloak JWT token
        private String countyId; // Used as location filter in new schema
        private LocalDate startDate;
        private LocalDate endDate;
        private Map<String, Object> filters = new HashMap<>();
        private Map<String, Object> additionalFilters = new HashMap<>();

        // Getters and setters
        public String getUserRole() { return userRole; }
        public void setUserRole(String userRole) { this.userRole = userRole; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getCountyId() { return countyId; }
        public void setCountyId(String countyId) { this.countyId = countyId; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public Map<String, Object> getFilters() { return filters; }
        public void setFilters(Map<String, Object> filters) { this.filters = filters; }
        
        public Map<String, Object> getAdditionalFilters() { return additionalFilters; }
        public void setAdditionalFilters(Map<String, Object> additionalFilters) { this.additionalFilters = additionalFilters; }
        
        public void addFilter(String key, Object value) {
            this.filters.put(key, value);
        }
    }
}






