package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.repository.TimesheetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * Analytics Controller for Real-Time Dashboard Metrics
 * Provides REST API endpoints for Tableau and other analytics tools
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private TimesheetRepository timesheetRepository;

    /**
     * Extract user info (role and county) from JWT token
     */
    private Map<String, Object> extractUserInfoFromJwt() {
        Map<String, Object> userInfo = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                
                // Extract role from client roles first
                String extractedRole = null;
                Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
                if (resourceAccess != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> trialAppAccess = (Map<String, Object>) resourceAccess.get("trial-app");
                    if (trialAppAccess != null) {
                        @SuppressWarnings("unchecked")
                        List<String> clientRoles = (List<String>) trialAppAccess.get("roles");
                        if (clientRoles != null && !clientRoles.isEmpty()) {
                            for (String role : clientRoles) {
                                if (role != null && !role.trim().isEmpty()) {
                                    extractedRole = com.example.kafkaeventdrivenapp.util.RoleMapper.canonicalName(role);
                                    break;
                                }
                            }
                        }
                    }
                }
                
                // Fallback to realm roles
                if (extractedRole == null) {
                    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                    if (realmAccess != null) {
                        @SuppressWarnings("unchecked")
                        List<String> roles = (List<String>) realmAccess.get("roles");
                        if (roles != null) {
                            for (String role : roles) {
                                if (role != null && !role.trim().isEmpty() && 
                                    !role.startsWith("default-roles-") &&
                                    !role.equals("offline_access") &&
                                    !role.equals("uma_authorization")) {
                                    extractedRole = com.example.kafkaeventdrivenapp.util.RoleMapper.canonicalName(role);
                                    break;
                                }
                            }
                        }
                    }
                }
                
                if (extractedRole != null) {
                    userInfo.put("role", extractedRole);
                }
                
                // Extract countyId from JWT
                String countyId = jwt.getClaimAsString("countyId");
                if (countyId == null || countyId.trim().isEmpty()) {
                    Map<String, Object> attributes = jwt.getClaimAsMap("attributes");
                    if (attributes != null) {
                        Object countyIdObj = attributes.get("countyId");
                        if (countyIdObj != null) {
                            if (countyIdObj instanceof List && ((List<?>) countyIdObj).size() > 0) {
                                countyId = ((List<?>) countyIdObj).get(0).toString();
                            } else {
                                countyId = countyIdObj.toString();
                            }
                        }
                    }
                }
                
                if (countyId != null && !countyId.trim().isEmpty()) {
                    userInfo.put("countyId", countyId);
                }
            }
        } catch (Exception e) {
            log.error("Error extracting user info from JWT", e);
        }
        return userInfo;
    }

    /**
     * Resolve county filter based on JWT token and request parameter
     * Enforces county restrictions for SUPERVISOR, CASE_WORKER, PROVIDER, RECIPIENT
     */
    private String resolveCountyFilter(String requestedCounty, String userRole, String tokenCounty) {
        String normalizedRole = userRole != null ? userRole.toUpperCase() : "";
        
        // Roles that require county from JWT token
        if (normalizedRole.contains("SUPERVISOR") || normalizedRole.contains("CASE_WORKER") || 
            normalizedRole.contains("PROVIDER") || normalizedRole.contains("RECIPIENT")) {
            if (tokenCounty != null && !tokenCounty.trim().isEmpty()) {
                log.debug("Enforcing county restriction for role {}: using token county {}", userRole, tokenCounty);
                return tokenCounty;
            }
            log.warn("Role {} requires county from JWT token but none found", userRole);
            return null; // Will cause filtering to fail
        }
        
        // For other roles, prefer token county but allow requested county
        if (tokenCounty != null && !tokenCounty.trim().isEmpty()) {
            return tokenCounty;
        }
        
        return normalizeParam(requestedCounty);
    }

    /**
     * Get real-time metrics for dashboard
     * Returns key performance indicators updated in near real-time
     */
    @GetMapping("/realtime-metrics")
    public ResponseEntity<Map<String, Object>> getRealTimeMetrics(
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priorityLevel,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String startYear,
            @RequestParam(required = false) String endYear) {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Extract user info from JWT
            Map<String, Object> userInfo = extractUserInfoFromJwt();
            String userRole = userInfo.get("role") != null ? userInfo.get("role").toString() : null;
            String tokenCounty = userInfo.get("countyId") != null ? userInfo.get("countyId").toString() : null;
            
            // Resolve county filter with JWT enforcement
            String countyFilter = resolveCountyFilter(county, userRole, tokenCounty);
            
            String districtFilter = normalizeParam(districtId);
            String statusFilter = normalizeParam(status);
            String priorityFilter = normalizeParam(priorityLevel);
            String serviceTypeFilter = normalizeParam(serviceType);
            
            LocalDateTime createdAfter = resolveStartDate(startYear);
            LocalDateTime createdBefore = resolveEndDate(endYear);
            
            LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
            if (createdAfter != null && createdAfter.isAfter(startOfToday)) {
                startOfToday = createdAfter;
            }
            
            LocalDate weekStartDate = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
            LocalDateTime weekStart = weekStartDate.atStartOfDay();
            if (createdAfter != null && createdAfter.isAfter(weekStart)) {
                weekStart = createdAfter;
            }
            LocalDateTime weekEnd = LocalDate.now().atTime(23, 59, 59);
            if (createdBefore != null && createdBefore.isBefore(weekEnd)) {
                weekEnd = createdBefore;
            }
            
            // Total timesheets submitted today (using available repository methods)
            long totalTimesheetsToday = timesheetRepository.countCreatedAfterWithFilters(
                startOfToday,
                createdBefore,
                countyFilter, // location
                serviceTypeFilter, // department
                statusFilter
            );
            
            // Pending approvals (using available repository methods)
            long pendingApprovals = timesheetRepository.countPendingApprovalsWithFilters(
                countyFilter, // location
                serviceTypeFilter, // department
                statusFilter,
                createdAfter,
                createdBefore
            );
            
            // Distinct employees (using available repository methods)
            long distinctEmployees = timesheetRepository.countDistinctEmployeesWithFilters(
                countyFilter, // location
                serviceTypeFilter, // department
                statusFilter,
                createdAfter,
                createdBefore
            );
            long totalParticipants = distinctEmployees; // Simplified for new schema
            
            // Total approved amount - NOT AVAILABLE in new schema (no total_amount field)
            Double totalApprovedAmountToday = 0.0;
            
            // Total approved amount this week - NOT AVAILABLE in new schema
            Double totalApprovedAmountThisWeek = 0.0;
            
            // Average approval time (in hours) - using available repository methods
            Double avgApprovalTime = 0.0;
            if (statusFilter == null || "APPROVED".equalsIgnoreCase(statusFilter)) {
                avgApprovalTime = timesheetRepository.avgApprovalTimeHoursWithFilters(
                    countyFilter, // location
                    serviceTypeFilter, // department
                    createdAfter,
                    createdBefore
                );
                if (avgApprovalTime == null) {
                    avgApprovalTime = 0.0;
                }
            }
            // Build response
            metrics.put("totalTimesheetsToday", totalTimesheetsToday);
            metrics.put("pendingApprovals", pendingApprovals);
            metrics.put("totalParticipants", totalParticipants);
            metrics.put("distinctEmployees", distinctEmployees);
            metrics.put("distinctProviders", distinctEmployees); // Using employees for backward compatibility
            metrics.put("distinctRecipients", 0L); // Not available in new schema
            metrics.put("totalApprovedAmountToday", totalApprovedAmountToday);
            metrics.put("totalApprovedAmountThisWeek", totalApprovedAmountThisWeek);
            metrics.put("avgApprovalTimeHours", avgApprovalTime);
            metrics.put("lastUpdated", LocalDateTime.now());
            metrics.put("status", "SUCCESS");
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            error.put("lastUpdated", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get metrics by district
     */
    @GetMapping("/metrics-by-district")
    public ResponseEntity<Map<String, Object>> getMetricsByDistrict(
            @RequestParam(required = false) String districtId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Implementation would query by district
            // This is a placeholder - implement based on your repository methods
            response.put("status", "SUCCESS");
            response.put("message", "District metrics endpoint - implement based on your needs");
            response.put("lastUpdated", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get metrics by county
     */
    @GetMapping("/metrics-by-county")
    public ResponseEntity<Map<String, Object>> getMetricsByCounty(
            @RequestParam(required = false) String county) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Implementation would query by county
            response.put("status", "SUCCESS");
            response.put("message", "County metrics endpoint - implement based on your needs");
            response.put("lastUpdated", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get demographic analytics - gender distribution
     */
    @GetMapping("/demographics/gender")
    public ResponseEntity<Map<String, Object>> getGenderDistribution(
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priorityLevel,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String startYear,
            @RequestParam(required = false) String endYear) {
        Map<String, Object> response = new HashMap<>();
        try {
            String districtFilter = normalizeParam(districtId);
            String countyFilter = normalizeParam(county);
            String statusFilter = normalizeParam(status);
            String priorityFilter = normalizeParam(priorityLevel);
            String serviceTypeFilter = normalizeParam(serviceType);
            LocalDateTime createdAfter = resolveStartDate(startYear);
            LocalDateTime createdBefore = resolveEndDate(endYear);
            
            // Extract user info from JWT for county filtering
            Map<String, Object> userInfo = extractUserInfoFromJwt();
            String userRole = userInfo.get("role") != null ? userInfo.get("role").toString() : null;
            String tokenCounty = userInfo.get("countyId") != null ? userInfo.get("countyId").toString() : null;
            String resolvedCountyFilter = resolveCountyFilter(countyFilter, userRole, tokenCounty);
            
            // Query gender distribution from actual database
            List<Object[]> providerGender = timesheetRepository.countByProviderGender();
            List<Object[]> recipientGender = timesheetRepository.countByRecipientGender();
            
            Map<String, Long> providerMap = new LinkedHashMap<>();
            Map<String, Long> recipientMap = new LinkedHashMap<>();
            
            for (Object[] row : providerGender) {
                providerMap.put((String) row[0], ((Number) row[1]).longValue());
            }
            for (Object[] row : recipientGender) {
                recipientMap.put((String) row[0], ((Number) row[1]).longValue());
            }
            
            response.put("provider", providerMap);
            response.put("recipient", recipientMap);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to compute county member counts", e);
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get demographic analytics - ethnicity distribution
     */
    @GetMapping("/demographics/ethnicity")
    public ResponseEntity<Map<String, Object>> getEthnicityDistribution(
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priorityLevel,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String startYear,
            @RequestParam(required = false) String endYear) {
        Map<String, Object> response = new HashMap<>();
        try {
            String districtFilter = normalizeParam(districtId);
            String countyFilter = normalizeParam(county);
            String statusFilter = normalizeParam(status);
            String priorityFilter = normalizeParam(priorityLevel);
            String serviceTypeFilter = normalizeParam(serviceType);
            LocalDateTime createdAfter = resolveStartDate(startYear);
            LocalDateTime createdBefore = resolveEndDate(endYear);
            
            // Extract user info from JWT for county filtering
            Map<String, Object> userInfo = extractUserInfoFromJwt();
            String userRole = userInfo.get("role") != null ? userInfo.get("role").toString() : null;
            String tokenCounty = userInfo.get("countyId") != null ? userInfo.get("countyId").toString() : null;
            String resolvedCountyFilter = resolveCountyFilter(countyFilter, userRole, tokenCounty);
            
            // Query ethnicity distribution from actual database
            List<Object[]> providerEthnicity = timesheetRepository.countByProviderEthnicity();
            List<Object[]> recipientEthnicity = timesheetRepository.countByRecipientEthnicity();
            
            Map<String, Long> providerMap = new LinkedHashMap<>();
            Map<String, Long> recipientMap = new LinkedHashMap<>();
            
            for (Object[] row : providerEthnicity) {
                providerMap.put((String) row[0], ((Number) row[1]).longValue());
            }
            for (Object[] row : recipientEthnicity) {
                recipientMap.put((String) row[0], ((Number) row[1]).longValue());
            }
            
            response.put("provider", providerMap);
            response.put("recipient", recipientMap);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get demographic analytics - age group distribution
     */
    @GetMapping("/demographics/age")
    public ResponseEntity<Map<String, Object>> getAgeGroupDistribution(
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priorityLevel,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String startYear,
            @RequestParam(required = false) String endYear) {
        Map<String, Object> response = new HashMap<>();
        try {
            String districtFilter = normalizeParam(districtId);
            String countyFilter = normalizeParam(county);
            String statusFilter = normalizeParam(status);
            String priorityFilter = normalizeParam(priorityLevel);
            String serviceTypeFilter = normalizeParam(serviceType);
            LocalDateTime createdAfter = resolveStartDate(startYear);
            LocalDateTime createdBefore = resolveEndDate(endYear);
            
            // Extract user info from JWT for county filtering
            Map<String, Object> userInfo = extractUserInfoFromJwt();
            String userRole = userInfo.get("role") != null ? userInfo.get("role").toString() : null;
            String tokenCounty = userInfo.get("countyId") != null ? userInfo.get("countyId").toString() : null;
            String resolvedCountyFilter = resolveCountyFilter(countyFilter, userRole, tokenCounty);
            
            // Query age group distribution from actual database
            List<Object[]> providerAge = timesheetRepository.countByProviderAgeGroup();
            List<Object[]> recipientAge = timesheetRepository.countByRecipientAgeGroup();
            
            Map<String, Long> providerMap = new LinkedHashMap<>();
            Map<String, Long> recipientMap = new LinkedHashMap<>();
            
            for (Object[] row : providerAge) {
                providerMap.put((String) row[0], ((Number) row[1]).longValue());
            }
            for (Object[] row : recipientAge) {
                recipientMap.put((String) row[0], ((Number) row[1]).longValue());
            }
            
            response.put("provider", providerMap);
            response.put("recipient", recipientMap);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Ad-hoc dataset for configurable reports
     */
    @GetMapping("/adhoc-data")
    public ResponseEntity<Map<String, Object>> getAdhocDataset(
            @RequestParam(required = false, defaultValue = "200") Integer limit,
            @RequestParam(required = false) String providerGender,
            @RequestParam(required = false) String providerAgeGroup,
            @RequestParam(required = false) String providerEthnicity,
            @RequestParam(required = false) String recipientGender,
            @RequestParam(required = false) String recipientAgeGroup,
            @RequestParam(required = false) String recipientEthnicity,
            @RequestParam(required = false) String county) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Extract user info from JWT
            Map<String, Object> userInfo = extractUserInfoFromJwt();
            String userRole = userInfo.get("role") != null ? userInfo.get("role").toString() : null;
            String tokenCounty = userInfo.get("countyId") != null ? userInfo.get("countyId").toString() : null;
            
            // Resolve county filter with JWT enforcement
            String countyFilter = resolveCountyFilter(county, userRole, tokenCounty);
            
            int sanitizedLimit = (limit == null) ? 200 : Math.max(10, Math.min(limit, 1000));
            
            // Columns aligned with actual database schema (TimesheetEntity) including demographic fields
            List<String> columns = List.of(
                    "id",
                    "employeeId",
                    "employeeName",
                    "userId",
                    "department",
                    "location",
                    "payPeriodStart",
                    "payPeriodEnd",
                    "regularHours",
                    "overtimeHours",
                    "sickHours",
                    "vacationHours",
                    "holidayHours",
                    "totalHours",
                    "status",
                    "comments",
                    "supervisorComments",
                    "submittedAt",
                    "submittedBy",
                    "approvedAt",
                    "approvedBy",
                    "createdAt",
                    "updatedAt",
                    // Demographic fields
                    "providerGender",
                    "providerEthnicity",
                    "providerAgeGroup",
                    "providerDateOfBirth",
                    "recipientGender",
                    "recipientEthnicity",
                    "recipientAgeGroup",
                    "recipientDateOfBirth"
            );

            // Query actual timesheet data from database with county filter
            // Use pagination to prevent OutOfMemoryError
            List<com.example.kafkaeventdrivenapp.entity.TimesheetEntity> timesheets;
            if (countyFilter != null && !countyFilter.trim().isEmpty()) {
                // Use paginated query to limit memory usage
                timesheets = timesheetRepository.findByLocationWithPagination(countyFilter, 0, sanitizedLimit);
            } else {
                // Use limit query for safety
                timesheets = timesheetRepository.findMostRecentWithLimit(sanitizedLimit);
            }

            // Convert entities to maps
            List<Map<String, Object>> data = new ArrayList<>();
            for (com.example.kafkaeventdrivenapp.entity.TimesheetEntity timesheet : timesheets) {
                Map<String, Object> record = new LinkedHashMap<>();
                record.put("id", timesheet.getId());
                record.put("employeeId", timesheet.getEmployeeId());
                record.put("employeeName", timesheet.getEmployeeName());
                record.put("userId", timesheet.getUserId());
                record.put("department", timesheet.getDepartment());
                record.put("location", timesheet.getLocation());
                record.put("payPeriodStart", timesheet.getPayPeriodStart());
                record.put("payPeriodEnd", timesheet.getPayPeriodEnd());
                record.put("regularHours", timesheet.getRegularHours() != null ? timesheet.getRegularHours().doubleValue() : null);
                record.put("overtimeHours", timesheet.getOvertimeHours() != null ? timesheet.getOvertimeHours().doubleValue() : null);
                record.put("sickHours", timesheet.getSickHours() != null ? timesheet.getSickHours().doubleValue() : null);
                record.put("vacationHours", timesheet.getVacationHours() != null ? timesheet.getVacationHours().doubleValue() : null);
                record.put("holidayHours", timesheet.getHolidayHours() != null ? timesheet.getHolidayHours().doubleValue() : null);
                record.put("totalHours", timesheet.getTotalHours() != null ? timesheet.getTotalHours().doubleValue() : null);
                record.put("status", timesheet.getStatus());
                record.put("comments", timesheet.getComments());
                record.put("supervisorComments", timesheet.getSupervisorComments());
                record.put("submittedAt", timesheet.getSubmittedAt());
                record.put("submittedBy", timesheet.getSubmittedBy());
                record.put("approvedAt", timesheet.getApprovedAt());
                record.put("approvedBy", timesheet.getApprovedBy());
                record.put("createdAt", timesheet.getCreatedAt());
                record.put("updatedAt", timesheet.getUpdatedAt());
                // Demographic fields
                record.put("providerGender", timesheet.getProviderGender());
                record.put("providerEthnicity", timesheet.getProviderEthnicity());
                record.put("providerAgeGroup", timesheet.getProviderAgeGroup());
                record.put("providerDateOfBirth", timesheet.getProviderDateOfBirth());
                record.put("recipientGender", timesheet.getRecipientGender());
                record.put("recipientEthnicity", timesheet.getRecipientEthnicity());
                record.put("recipientAgeGroup", timesheet.getRecipientAgeGroup());
                record.put("recipientDateOfBirth", timesheet.getRecipientDateOfBirth());
                // Also add snake_case versions for frontend compatibility
                record.put("provider_gender", timesheet.getProviderGender());
                record.put("provider_ethnicity", timesheet.getProviderEthnicity());
                record.put("provider_age_group", timesheet.getProviderAgeGroup());
                record.put("recipient_gender", timesheet.getRecipientGender());
                record.put("recipient_ethnicity", timesheet.getRecipientEthnicity());
                record.put("recipient_age_group", timesheet.getRecipientAgeGroup());
                data.add(record);
            }

            response.put("columns", columns);
            response.put("rows", data);
            response.put("count", data.size());
            response.put("status", "SUCCESS");
            response.put("limit", sanitizedLimit);
            response.put("lastUpdated", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("lastUpdated", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/adhoc-stats")
    public ResponseEntity<Map<String, Object>> getAdhocStats(
            @RequestParam(required = false) String providerGender,
            @RequestParam(required = false) String providerAgeGroup,
            @RequestParam(required = false) String providerEthnicity,
            @RequestParam(required = false) String recipientGender,
            @RequestParam(required = false) String recipientAgeGroup,
            @RequestParam(required = false) String recipientEthnicity,
            @RequestParam(required = false) String county) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Extract user info from JWT
            Map<String, Object> userInfo = extractUserInfoFromJwt();
            String userRole = userInfo.get("role") != null ? userInfo.get("role").toString() : null;
            String tokenCounty = userInfo.get("countyId") != null ? userInfo.get("countyId").toString() : null;
            
            // Resolve county filter with JWT enforcement
            String countyFilter = resolveCountyFilter(county, userRole, tokenCounty);

            // Calculate stats from actual database data
            // Use limited queries to prevent OutOfMemoryError - max 10,000 records for stats calculation
            List<com.example.kafkaeventdrivenapp.entity.TimesheetEntity> timesheets;
            if (countyFilter != null && !countyFilter.trim().isEmpty()) {
                // Use pagination to limit results - fetch first 10,000 records
                timesheets = timesheetRepository.findByLocationWithPagination(countyFilter, 0, 10000);
            } else {
                // Use limited query instead of findAll() to prevent memory issues
                timesheets = timesheetRepository.findMostRecentWithLimit(10000);
            }

            // Calculate statistics
            long totalRecords = timesheets.size();
            double totalHours = timesheets.stream()
                .filter(t -> t.getTotalHours() != null)
                .mapToDouble(t -> t.getTotalHours().doubleValue())
                .sum();
            double avgHours = totalRecords > 0 ? totalHours / totalRecords : 0.0;

            Map<String, Object> statsMap = new HashMap<>();
            statsMap.put("totalRecords", totalRecords);
            statsMap.put("totalHours", totalHours);
            statsMap.put("totalAmount", 0.0); // Not available in schema
            statsMap.put("avgHours", avgHours);
            statsMap.put("avgAmount", 0.0); // Not available in schema

            response.put("stats", statsMap);
            response.put("status", "SUCCESS");
            response.put("lastUpdated", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("lastUpdated", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/adhoc-filters")
    public ResponseEntity<Map<String, Object>> getAdhocFilterOptions() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Extract user info from JWT to filter available options
            Map<String, Object> userInfo = extractUserInfoFromJwt();
            String userRole = userInfo.get("role") != null ? userInfo.get("role").toString() : null;
            String tokenCounty = userInfo.get("countyId") != null ? userInfo.get("countyId").toString() : null;
            
            // Get filter options from actual database
            // Locations (counties) - filtered by user's county if restricted
            List<String> locations;
            if (tokenCounty != null && !tokenCounty.trim().isEmpty() && 
                (userRole != null && (userRole.contains("SUPERVISOR") || userRole.contains("CASE_WORKER") || 
                                      userRole.contains("PROVIDER") || userRole.contains("RECIPIENT")))) {
                locations = List.of(tokenCounty); // Only user's county
            } else {
                locations = timesheetRepository.findDistinctLocations();
            }
            
            // Departments
            List<String> departments = timesheetRepository.findDistinctDepartments();
            
            // Statuses
            List<String> statuses = timesheetRepository.findDistinctStatuses();
            
            // Demographic fields - provider and recipient specific
            List<String> providerGenders = timesheetRepository.findDistinctProviderGenders();
            List<String> recipientGenders = timesheetRepository.findDistinctRecipientGenders();
            List<String> providerEthnicities = timesheetRepository.findDistinctProviderEthnicities();
            List<String> recipientEthnicities = timesheetRepository.findDistinctRecipientEthnicities();
            List<String> providerAgeGroups = timesheetRepository.findDistinctProviderAgeGroups();
            List<String> recipientAgeGroups = timesheetRepository.findDistinctRecipientAgeGroups();
            
            // Combined (for backward compatibility)
            List<String> genders = timesheetRepository.findDistinctGenders();
            List<String> ethnicities = timesheetRepository.findDistinctEthnicities();
            List<String> ageGroups = timesheetRepository.findDistinctAgeGroups();
            
            response.put("providerGenders", providerGenders);
            response.put("recipientGenders", recipientGenders);
            response.put("providerEthnicities", providerEthnicities);
            response.put("recipientEthnicities", recipientEthnicities);
            response.put("providerAgeGroups", providerAgeGroups);
            response.put("recipientAgeGroups", recipientAgeGroups);
            // Backward compatibility
            response.put("genders", genders);
            response.put("ethnicities", ethnicities);
            response.put("ageGroups", ageGroups);
            response.put("locations", locations);
            response.put("departments", departments);
            response.put("statuses", statuses);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting adhoc filter options", e);
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/adhoc-breakdowns")
    public ResponseEntity<Map<String, Object>> getAdhocBreakdowns(
            @RequestParam(required = false) String providerGender,
            @RequestParam(required = false) String providerAgeGroup,
            @RequestParam(required = false) String providerEthnicity,
            @RequestParam(required = false) String recipientGender,
            @RequestParam(required = false) String recipientAgeGroup,
            @RequestParam(required = false) String recipientEthnicity) {
        Map<String, Object> response = new HashMap<>();
        try {
            String providerGenderFilter = normalizeParam(providerGender);
            String providerAgeGroupFilter = normalizeParam(providerAgeGroup);
            String providerEthnicityFilter = normalizeParam(providerEthnicity);
            String recipientGenderFilter = normalizeParam(recipientGender);
            String recipientAgeGroupFilter = normalizeParam(recipientAgeGroup);
            String recipientEthnicityFilter = normalizeParam(recipientEthnicity);

            // Get breakdowns from actual database
            // Combine provider and recipient data for overall breakdowns
            List<Object[]> providerGenderBreakdown = timesheetRepository.countByProviderGender();
            List<Object[]> recipientGenderBreakdown = timesheetRepository.countByRecipientGender();
            List<Object[]> providerEthnicityBreakdown = timesheetRepository.countByProviderEthnicity();
            List<Object[]> recipientEthnicityBreakdown = timesheetRepository.countByRecipientEthnicity();
            List<Object[]> providerAgeGroupBreakdown = timesheetRepository.countByProviderAgeGroup();
            List<Object[]> recipientAgeGroupBreakdown = timesheetRepository.countByRecipientAgeGroup();

            // Convert to maps and combine provider + recipient
            Map<String, Long> genderMap = new LinkedHashMap<>();
            for (Object[] row : providerGenderBreakdown) {
                String gender = (String) row[0];
                long count = ((Number) row[1]).longValue();
                genderMap.put(gender, genderMap.getOrDefault(gender, 0L) + count);
            }
            for (Object[] row : recipientGenderBreakdown) {
                String gender = (String) row[0];
                long count = ((Number) row[1]).longValue();
                genderMap.put(gender, genderMap.getOrDefault(gender, 0L) + count);
            }
            
            Map<String, Long> ethnicityMap = new LinkedHashMap<>();
            for (Object[] row : providerEthnicityBreakdown) {
                String ethnicity = (String) row[0];
                long count = ((Number) row[1]).longValue();
                ethnicityMap.put(ethnicity, ethnicityMap.getOrDefault(ethnicity, 0L) + count);
            }
            for (Object[] row : recipientEthnicityBreakdown) {
                String ethnicity = (String) row[0];
                long count = ((Number) row[1]).longValue();
                ethnicityMap.put(ethnicity, ethnicityMap.getOrDefault(ethnicity, 0L) + count);
            }
            
            Map<String, Long> ageGroupMap = new LinkedHashMap<>();
            for (Object[] row : providerAgeGroupBreakdown) {
                String ageGroup = (String) row[0];
                long count = ((Number) row[1]).longValue();
                ageGroupMap.put(ageGroup, ageGroupMap.getOrDefault(ageGroup, 0L) + count);
            }
            for (Object[] row : recipientAgeGroupBreakdown) {
                String ageGroup = (String) row[0];
                long count = ((Number) row[1]).longValue();
                ageGroupMap.put(ageGroup, ageGroupMap.getOrDefault(ageGroup, 0L) + count);
            }

            response.put("gender", genderMap);
            response.put("ethnicity", ethnicityMap);
            response.put("ageGroup", ageGroupMap);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/adhoc-crosstab")
    public ResponseEntity<Map<String, Object>> getAdhocCrosstab(
            @RequestParam(required = false) String providerGender,
            @RequestParam(required = false) String providerAgeGroup,
            @RequestParam(required = false) String providerEthnicity,
            @RequestParam(required = false) String recipientGender,
            @RequestParam(required = false) String recipientAgeGroup,
            @RequestParam(required = false) String recipientEthnicity) {
        Map<String, Object> response = new HashMap<>();
        try {
            String providerGenderFilter = normalizeParam(providerGender);
            String providerAgeGroupFilter = normalizeParam(providerAgeGroup);
            String providerEthnicityFilter = normalizeParam(providerEthnicity);
            String recipientGenderFilter = normalizeParam(recipientGender);
            String recipientAgeGroupFilter = normalizeParam(recipientAgeGroup);
            String recipientEthnicityFilter = normalizeParam(recipientEthnicity);

            // Get cross-tabulations - NOT AVAILABLE in new schema (no demographic fields)
            List<Object[]> genderEthnicity = new ArrayList<>();
            List<Object[]> genderAge = new ArrayList<>();
            List<Object[]> ethnicityAge = new ArrayList<>();

            // Convert to list of maps for easier frontend consumption
            List<Map<String, Object>> genderEthnicityList = new ArrayList<>();
            for (Object[] row : genderEthnicity) {
                Map<String, Object> item = new HashMap<>();
                item.put("gender", row[0]);
                item.put("ethnicity", row[1]);
                item.put("count", ((Number) row[2]).longValue());
                genderEthnicityList.add(item);
            }
            
            List<Map<String, Object>> genderAgeList = new ArrayList<>();
            for (Object[] row : genderAge) {
                Map<String, Object> item = new HashMap<>();
                item.put("gender", row[0]);
                item.put("ageGroup", row[1]);
                item.put("count", ((Number) row[2]).longValue());
                genderAgeList.add(item);
            }
            
            List<Map<String, Object>> ethnicityAgeList = new ArrayList<>();
            for (Object[] row : ethnicityAge) {
                Map<String, Object> item = new HashMap<>();
                item.put("ethnicity", row[0]);
                item.put("ageGroup", row[1]);
                item.put("count", ((Number) row[2]).longValue());
                ethnicityAgeList.add(item);
            }

            response.put("genderEthnicity", genderEthnicityList);
            response.put("genderAge", genderAgeList);
            response.put("ethnicityAge", ethnicityAgeList);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get timesheet counts by year
     */
    @GetMapping("/timesheets-by-year")
    public ResponseEntity<Map<String, Object>> getTimesheetsByYear(
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priorityLevel,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String startYear,
            @RequestParam(required = false) String endYear) {
        Map<String, Object> response = new HashMap<>();
        try {
            String districtFilter = normalizeParam(districtId);
            String countyFilter = normalizeParam(county);
            String statusFilter = normalizeParam(status);
            String priorityFilter = normalizeParam(priorityLevel);
            String serviceTypeFilter = normalizeParam(serviceType);
            LocalDateTime createdAfter = resolveStartDate(startYear);
            LocalDateTime createdBefore = resolveEndDate(endYear);
            
            // Year data - Need to implement using available methods
            // For now, returning empty list
            List<Object[]> yearData = new ArrayList<>();
            Map<String, Long> yearMap = new HashMap<>();
            
            for (Object[] row : yearData) {
                yearMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
            }
            
            response.put("data", yearMap);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get member counts by county for Tableau dashboard
     */
    @GetMapping("/county-member-counts")
    public ResponseEntity<Map<String, Object>> getCountyMemberCounts(
            @RequestParam(required = false) String districtId,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priorityLevel,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String startYear,
            @RequestParam(required = false) String endYear) {
        Map<String, Object> response = new HashMap<>();
        try {
            String districtFilter = normalizeParam(districtId);
            String countyFilter = normalizeParam(county);
            String statusFilter = normalizeParam(status);
            String priorityFilter = normalizeParam(priorityLevel);
            String serviceTypeFilter = normalizeParam(serviceType);
            LocalDateTime createdAfter = resolveStartDate(startYear);
            LocalDateTime createdBefore = resolveEndDate(endYear);

            log.info("County member count request -> districtId={}, county={}, status={}, priorityLevel={}, serviceType={}, startYear={}, endYear={}",
                    districtFilter, countyFilter, statusFilter, priorityFilter, serviceTypeFilter, startYear, endYear);

            // Member counts by county - Need to implement using available methods
            // For now, returning empty list
            List<Object[]> rawCounts = new ArrayList<>();

            List<Map<String, Object>> countyData = new ArrayList<>();
            long totalMembers = 0;
            long totalProviders = 0;
            long totalRecipients = 0;

            for (Object[] row : rawCounts) {
                String countyName = row[0] != null ? row[0].toString() : "Unknown";
                long providerCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                long recipientCount = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                long totalCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;

                log.debug("Member count row -> county={}, providerCount={}, recipientCount={}, totalCount={}", 
                        countyName, providerCount, recipientCount, totalCount);

                Map<String, Object> countyEntry = new LinkedHashMap<>();
                countyEntry.put("county", countyName);
                countyEntry.put("providerCount", providerCount);
                countyEntry.put("recipientCount", recipientCount);
                countyEntry.put("memberCount", totalCount);
                countyData.add(countyEntry);

                totalMembers += totalCount;
                totalProviders += providerCount;
                totalRecipients += recipientCount;
            }

            Map<String, Object> success = new HashMap<>();
            success.put("status", "SUCCESS");
            success.put("totalMembers", totalMembers);
            success.put("totalProviders", totalProviders);
            success.put("totalRecipients", totalRecipients);
            success.put("counties", countyData);

            Map<String, Object> filters = new LinkedHashMap<>();
            filters.put("districtId", districtFilter);
            filters.put("county", countyFilter);
            filters.put("status", statusFilter);
            filters.put("priorityLevel", priorityFilter);
            filters.put("serviceType", serviceTypeFilter);
            filters.put("startYear", startYear);
            filters.put("endYear", endYear);
            success.put("filters", filters);

            success.put("generatedAt", LocalDateTime.now());
            log.info("County member count response -> totalMembers={}, totalProviders={}, totalRecipients={}, countyCount={}", 
                    totalMembers, totalProviders, totalRecipients, countyData.size());
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            log.error("Failed to compute county member counts", e);
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Provide distinct filter options for dashboard controls
     */
    @GetMapping("/filters")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Districts - NOT AVAILABLE in new schema (no district field)
            response.put("districts", new ArrayList<>());
            // Counties -> Locations in new schema
            response.put("counties", timesheetRepository.findDistinctLocations());
            response.put("statuses", timesheetRepository.findDistinctStatuses());
            // Priority levels - NOT AVAILABLE in new schema
            response.put("priorityLevels", new ArrayList<>());
            // Service types -> Departments in new schema
            response.put("serviceTypes", timesheetRepository.findDistinctDepartments());
            response.put("status", "SUCCESS");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Health check for analytics endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "analytics");
        health.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(health);
    }
    
    private String normalizeParam(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "all".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }
    
    private LocalDateTime resolveStartDate(String startYear) {
        Integer parsed = parseYear(startYear);
        if (parsed == null) {
            return null;
        }
        return LocalDate.of(parsed, 1, 1).atStartOfDay();
    }
    
    private LocalDateTime resolveEndDate(String endYear) {
        Integer parsed = parseYear(endYear);
        if (parsed == null) {
            return null;
        }
        return LocalDate.of(parsed, 12, 31).atTime(23, 59, 59);
    }
    
    private Integer parseYear(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            double numeric = Double.parseDouble(trimmed);
            return (int) Math.floor(numeric);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

