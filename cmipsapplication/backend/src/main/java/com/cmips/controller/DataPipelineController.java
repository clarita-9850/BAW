package com.cmips.controller;

import com.cmips.model.*;
import com.cmips.service.FieldMaskingService;
import com.cmips.service.FieldVisibilityService;
import com.cmips.service.ReportGenerationService;
import com.cmips.service.RulesEngineService;
import com.cmips.service.QueryBuilderService;
import com.cmips.service.DataFetchingService;
import com.cmips.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pipeline")
@CrossOrigin(origins = "*")
public class DataPipelineController {

    @Autowired
    private ReportGenerationService reportGenerationService;
    
    
    @Autowired
    private RulesEngineService rulesEngineService;
    
    @Autowired
    private QueryBuilderService queryBuilderService;
    
    @Autowired
    private DataFetchingService dataFetchingService;
    
    @Autowired
    private FieldMaskingService fieldMaskingService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private FieldVisibilityService fieldVisibilityService;

    public DataPipelineController() {
        System.out.println("🔧 DataPipelineController: Constructor called - initializing...");
        try {
            System.out.println("✅ DataPipelineController: Constructor completed successfully");
        } catch (Exception e) {
            System.err.println("❌ DataPipelineController: Constructor failed with error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Enhanced 5-Stage Data Processing Pipeline
     */
    @PostMapping("/extract-enhanced")
    public ResponseEntity<Map<String, Object>> extractDataEnhanced(@RequestBody PipelineExtractionRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔍 DataPipelineController: Starting enhanced 5-stage pipeline for role: " + request.getUserRole());
            
            // Get JWT token for field masking
            String jwtToken = null;
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    jwtToken = jwt.getTokenValue();
                } else {
                    String authHeader = httpRequest.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        jwtToken = authHeader.substring(7);
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not extract JWT token: " + e.getMessage());
            }
            
            // Stage 1: Role Validation (Now handled by Keycloak)
            System.out.println("🔍 Stage 1: Role Validation - Handled by Keycloak");
            
            // Stage 2: Rules Engine Trigger
            System.out.println("🔍 Stage 2: Rules Engine Trigger");
            RulesEngineService.AccessPattern accessPattern = rulesEngineService.determineAccessPattern(request.getUserRole());
            System.out.println("🔍 Access pattern determined: " + accessPattern.getAccessType());
            
            // Stage 3: Query Building
            System.out.println("🔍 Stage 3: Query Building");
            QueryBuilderService.QueryParameters queryParams = queryBuilderService.buildQuery(
                request.getUserRole(),
                request.getUserCounty(),
                request.getDateRange() != null ? request.getDateRange().getStartDate() : null,
                request.getDateRange() != null ? request.getDateRange().getEndDate() : null,
                null
            );
            
            // Stage 4: Data Fetching
            System.out.println("🔍 Stage 4: Data Fetching");
            DataFetchingService.DataFetchResult fetchResult = dataFetchingService.fetchData(queryParams);
            if (!fetchResult.isSuccess()) {
                response.put("status", "ERROR");
                response.put("message", "Data fetching failed: " + fetchResult.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }
            
            // Stage 5: Field Masking Application
            System.out.println("🔍 Stage 5: Field Masking Application");
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "JWT token is required for field masking");
                return ResponseEntity.badRequest().body(response);
            }
            List<MaskedTimesheetData> maskedData = fieldMaskingService.applyFieldMasking(
                fetchResult.getData(), 
                request.getUserRole(),
                request.getReportType(),
                jwtToken
            );
            
            // Stage 6: Convert masked data to response format (field masking already handled in Stage 5)
            System.out.println("🔍 Stage 6: Convert masked data to response format");
            List<Map<String, Object>> responseData = new ArrayList<>();
            
            for (MaskedTimesheetData data : maskedData) {
                // Create a Map with only the fields from masked data (field masking already applied)
                Map<String, Object> record = new HashMap<>();
                
                // Add only the fields from the masked data (FieldMaskingService already filtered hidden fields)
                record.putAll(data.getFields());
                
                responseData.add(record);
            }
            
            response.put("status", "SUCCESS");
            response.put("message", "Enhanced 6-stage pipeline completed successfully");
            response.put("totalRecords", fetchResult.getRecordCount());
            response.put("maskedRecords", maskedData.size());
            response.put("responseRecords", responseData.size());
            response.put("data", responseData);
            
            // Publish completion event
            eventService.publishEvent("ENHANCED_PIPELINE_COMPLETED", Map.of(
                "userRole", request.getUserRole(),
                "totalRecords", fetchResult.getRecordCount(),
                "maskedRecords", maskedData.size(),
                "reportType", request.getReportType()
            ));
            
            System.out.println("✅ DataPipelineController: Enhanced 5-stage pipeline completed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error in enhanced pipeline: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Enhanced pipeline failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get field masking rules for a user role
     */
    @GetMapping("/masking-rules/{userRole}")
    public ResponseEntity<Map<String, Object>> getMaskingRules(@PathVariable String userRole) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔒 DataPipelineController: Getting masking rules for role: " + userRole);
            
            List<FieldMaskingRule> rules = fieldMaskingService.getAvailableRules(userRole);
            
            response.put("status", "SUCCESS");
            response.put("userRole", userRole);
            response.put("rules", rules);
            response.put("totalRules", rules.size());
            
            System.out.println("✅ DataPipelineController: Retrieved " + rules.size() + " masking rules");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting masking rules: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get masking rules: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Update field masking rules for a user role
     */
    @PostMapping("/masking-rules/{userRole}")
    public ResponseEntity<Map<String, Object>> updateMaskingRules(
            @PathVariable String userRole, 
            @RequestBody List<FieldMaskingRule> rules) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔧 DataPipelineController: Updating masking rules for role: " + userRole);
            
            fieldMaskingService.updateRules(userRole, rules);
            
            response.put("status", "SUCCESS");
            response.put("message", "Masking rules updated successfully");
            response.put("userRole", userRole);
            response.put("totalRules", rules.size());
            
            System.out.println("✅ DataPipelineController: Updated " + rules.size() + " masking rules");
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
     * Get available user roles
     */
    @GetMapping("/user-roles")
    public ResponseEntity<Map<String, Object>> getUserRoles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> roles = Arrays.stream(UserRole.values())
                    .map(UserRole::name)
                    .collect(Collectors.toList());
            
            response.put("status", "SUCCESS");
            response.put("roles", roles);
            response.put("totalRoles", roles.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting user roles: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get user roles: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get available counties for access control
     */
    @GetMapping("/counties")
    public ResponseEntity<Map<String, Object>> getAvailableCounties() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> counties = List.of(
                "CT1", "CT2", "CT3", "CT4", "CT5"
            );
            
            response.put("status", "SUCCESS");
            response.put("counties", counties);
            response.put("totalCounties", counties.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting counties: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get counties: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get available report types
     */
    @GetMapping("/report-types")
    public ResponseEntity<Map<String, Object>> getReportTypes() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> reportTypes = List.of(
                "PAYROLL_REPORT",
                "PROJECT_REPORT", 
                "PROVIDER_REPORT",
                "HR_REPORT",
                "GENERIC_REPORT"
            );
            
            response.put("status", "SUCCESS");
            response.put("reportTypes", reportTypes);
            response.put("totalTypes", reportTypes.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting report types: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get report types: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Generate report with field masking
     */
    @PostMapping("/generate-report")
    public ResponseEntity<Map<String, Object>> generateReport(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📊 DataPipelineController: Generating report using enhanced pipeline...");
            
            // Get JWT token from Spring Security Authentication (already validated by SecurityConfig)
            String jwtToken = null;
            Map<String, Object> userInfo = null;
            
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("🔍 DataPipelineController: Authentication object: " + (authentication != null ? authentication.getClass().getSimpleName() : "NULL"));
                System.out.println("🔍 DataPipelineController: Authentication principal: " + (authentication != null && authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getSimpleName() : "NULL"));
                
                if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    jwtToken = jwt.getTokenValue();
                    System.out.println("🔐 DataPipelineController: JWT token extracted from SecurityContext (token length: " + (jwtToken != null ? jwtToken.length() : 0) + ")");
                    
                    // Extract user info directly from JWT object (more reliable than manual parsing)
                    userInfo = extractUserInfoFromJwtObject(jwt);
                    System.out.println("🔍 DataPipelineController: UserInfo extracted from JWT object: " + userInfo);
                } else {
                    System.out.println("⚠️ DataPipelineController: No JWT in SecurityContext (auth=" + (authentication != null ? "exists" : "null") + "), trying Authorization header fallback");
                    // Fallback to manual extraction from header
                    String authHeader = httpRequest.getHeader("Authorization");
                    System.out.println("🔍 DataPipelineController: Authorization header: " + (authHeader != null ? (authHeader.length() > 50 ? authHeader.substring(0, 50) + "..." : authHeader) : "NULL"));
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        jwtToken = authHeader.substring(7);
                        System.out.println("🔐 DataPipelineController: Extracting from Authorization header (token length: " + jwtToken.length() + ")");
                        userInfo = extractUserInfoFromJWT(jwtToken);
                        System.out.println("🔍 DataPipelineController: UserInfo extracted from header JWT: " + userInfo);
                    } else {
                        System.out.println("⚠️ DataPipelineController: No valid Authorization header found");
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ DataPipelineController: Error getting JWT from SecurityContext: " + e.getMessage());
                e.printStackTrace();
                // Fallback to manual extraction
                String authHeader = httpRequest.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    jwtToken = authHeader.substring(7);
                    System.out.println("🔐 DataPipelineController: Fallback - extracting from header (token length: " + jwtToken.length() + ")");
                    userInfo = extractUserInfoFromJWT(jwtToken);
                }
            }
            if (userInfo == null || userInfo.get("role") == null) {
                response.put("status", "ERROR");
                response.put("message", "Unable to resolve user role from JWT token");
                return ResponseEntity.badRequest().body(response);
            }

            String userRole = userInfo.get("role").toString();
            System.out.println("🔐 DataPipelineController: Enforcing user role from JWT token: " + userRole);
            if (request.containsKey("userRole")) {
                System.out.println("⚠️ DataPipelineController: userRole provided in request body will be ignored for security");
            }
            
            // Use ReportGenerationService to generate proper report format
            String tokenCountyId = userInfo.get("countyId") != null ? userInfo.get("countyId").toString() : null;
            // Accept both countyId and userCounty from frontend
            String requestedCountyId = request.get("countyId") != null ? request.get("countyId").toString() : 
                                      (request.get("userCounty") != null ? request.get("userCounty").toString() : null);

            System.out.println("🔍 DataPipelineController: tokenCountyId=" + tokenCountyId + ", requestedCountyId=" + requestedCountyId);
            System.out.println("🔍 DataPipelineController: Full userInfo map: " + userInfo);
            String countyId = resolveCountyId(userRole, tokenCountyId, requestedCountyId);
            System.out.println("🔍 DataPipelineController: resolved countyId=" + countyId);
            System.out.println("🔍 DataPipelineController: requiresCountyBinding(" + userRole + ")=" + requiresCountyBinding(userRole));
            
            if (requiresCountyBinding(userRole) && (countyId == null || countyId.trim().isEmpty())) {
                System.err.println("❌ DataPipelineController: County validation failed for role " + userRole);
                System.err.println("❌ DataPipelineController: tokenCountyId=" + tokenCountyId);
                System.err.println("❌ DataPipelineController: requestedCountyId=" + requestedCountyId);
                System.err.println("❌ DataPipelineController: resolved countyId=" + countyId);
                System.err.println("❌ DataPipelineController: userInfo=" + userInfo);
                response.put("status", "ERROR");
                response.put("message", "County is required for role " + userRole + " and must come from the authenticated user context.");
                return ResponseEntity.badRequest().body(response);
            }
            String startDate = (String) request.get("startDate");
            String endDate = (String) request.get("endDate");
            
            ReportGenerationRequest reportRequest = new ReportGenerationRequest();
            reportRequest.setUserRole(userRole);
            reportRequest.setReportType("TIMESHEET_REPORT");
            reportRequest.setUserCounty(countyId);
            if (startDate != null) {
                reportRequest.setStartDate(java.time.LocalDate.parse(startDate));
            }
            if (endDate != null) {
                reportRequest.setEndDate(java.time.LocalDate.parse(endDate));
            }
            
            // Set pagination parameters (default: first page, 500 records per page for better memory management)
            Object pageObj = request.get("page");
            Object pageSizeObj = request.get("pageSize");
            Integer page = pageObj != null ? (pageObj instanceof Integer ? (Integer) pageObj : Integer.parseInt(pageObj.toString())) : 0;
            Integer pageSize = pageSizeObj != null ? (pageSizeObj instanceof Integer ? (Integer) pageSizeObj : Integer.parseInt(pageSizeObj.toString())) : 500;
            reportRequest.setPage(page);
            reportRequest.setPageSize(pageSize);
            System.out.println("📄 DataPipelineController: Pagination set - page: " + page + ", pageSize: " + pageSize);
            
            // JWT token is required for report generation
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "JWT token is required for report generation");
                return ResponseEntity.badRequest().body(response);
            }
            
            System.out.println("🔐 DataPipelineController: Using JWT-ONLY method with JWT token");
            ReportGenerationResponse reportResponse = reportGenerationService.generateReport(reportRequest, jwtToken);
            
            if ("SUCCESS".equals(reportResponse.getStatus())) {
                System.out.println("🔍 DEBUG: reportResponse.getData() = " + reportResponse.getData());
                System.out.println("🔍 DEBUG: reportResponse.getData().getRecords() = " + (reportResponse.getData() != null ? reportResponse.getData().getRecords() : "NULL"));
                System.out.println("🔍 DEBUG: records size = " + (reportResponse.getData() != null && reportResponse.getData().getRecords() != null ? reportResponse.getData().getRecords().size() : "NULL"));
                
                // Get visible fields for this role using JWT token
                List<String> visibleFields = fieldVisibilityService.getVisibleFields(userRole, jwtToken);
                System.out.println("🔍 DEBUG: Visible fields for " + userRole + ": " + visibleFields);
                
                response.put("status", "SUCCESS");
                response.put("message", "Report generated successfully");
                response.put("reportId", reportResponse.getReportId());
                response.put("visibleFields", visibleFields); // Add visible fields to response
                
                if (reportResponse.getData() != null && reportResponse.getData().getRecords() != null) {
                    response.put("data", reportResponse.getData().getRecords());
                    
                    // Calculate total pages and include pagination info
                    long totalCount = reportResponse.getData().getTotalCount();
                    int reportResponseTotalRecords = reportResponse.getTotalRecords();
                    System.out.println("🔍 DEBUG: reportResponse.getTotalRecords() = " + reportResponseTotalRecords);
                    System.out.println("🔍 DEBUG: reportResponse.getData().getTotalCount() = " + totalCount);
                    System.out.println("🔍 DEBUG: Setting response totalRecords to: " + totalCount);
                    response.put("totalRecords", totalCount); // Use totalCount instead of current page size
                    int calculatedPageSize = pageSizeObj != null ? (pageSizeObj instanceof Integer ? (Integer) pageSizeObj : Integer.parseInt(pageSizeObj.toString())) : 500;
                    int totalPages = totalCount > 0 ? (int) Math.ceil((double) totalCount / calculatedPageSize) : 0;
                    int currentPage = page + 1; // Convert 0-based to 1-based for display
                    
                    response.put("totalCount", totalCount);
                    response.put("totalPages", totalPages);
                    response.put("currentPage", currentPage);
                    response.put("pageSize", calculatedPageSize);
                    
                    System.out.println("📊 Pagination info - Total count: " + totalCount + ", Total pages: " + totalPages + ", Current page: " + currentPage);
                } else {
                    System.out.println("❌ ERROR: reportResponse.getData() or getRecords() is NULL!");
                    response.put("data", new ArrayList<>());
                    response.put("totalRecords", 0);
                    int calculatedPageSize = pageSizeObj != null ? (pageSizeObj instanceof Integer ? (Integer) pageSizeObj : Integer.parseInt(pageSizeObj.toString())) : 500;
                    response.put("totalCount", 0);
                    response.put("totalPages", 0);
                    response.put("currentPage", 1);
                    response.put("pageSize", calculatedPageSize);
                }
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Report generation failed: " + reportResponse.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error generating report: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to generate report: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private boolean requiresCountyBinding(String userRole) {
        if (userRole == null) return false;
        String normalized = userRole.toUpperCase();
        // SUPERVISOR, CASE_WORKER, PROVIDER, RECIPIENT all require county binding
        return normalized.contains("SUPERVISOR") || 
               normalized.contains("COUNTY") || 
               normalized.contains("CASE_WORKER") || 
               normalized.contains("PROVIDER") || 
               normalized.contains("RECIPIENT");
    }

    private String resolveCountyId(String userRole, String tokenCounty, String requestedCounty) {
        String normalizedRole = userRole != null ? userRole.toUpperCase() : "";
        
        // For SUPERVISOR: MUST use token county (enforce county restriction)
        if (normalizedRole.contains("SUPERVISOR")) {
            if (tokenCounty != null && !tokenCounty.trim().isEmpty()) {
                // If requested county doesn't match token county, use token county (enforce restriction)
                if (requestedCounty != null && !requestedCounty.equalsIgnoreCase(tokenCounty)) {
                    System.out.println("⚠️ DataPipelineController: SUPERVISOR requested county " + requestedCounty + " overridden by token county " + tokenCounty);
                }
                System.out.println("🔍 DataPipelineController: SUPERVISOR using token county: " + tokenCounty);
                return tokenCounty;
            }
            // SUPERVISOR requires county from JWT token - no fallback to all data
            System.out.println("⚠️ DataPipelineController: SUPERVISOR requires county from JWT token but none found");
            return null; // Will trigger error in requiresCountyBinding check
        }
        
        // For CASE_WORKER: MUST use token county (enforce county restriction)
        if (normalizedRole.contains("CASE_WORKER")) {
            if (tokenCounty != null && !tokenCounty.trim().isEmpty()) {
                // If requested county doesn't match token county, use token county (enforce restriction)
                if (requestedCounty != null && !requestedCounty.equalsIgnoreCase(tokenCounty)) {
                    System.out.println("⚠️ DataPipelineController: CASE_WORKER requested county " + requestedCounty + " overridden by token county " + tokenCounty);
                }
                System.out.println("🔍 DataPipelineController: CASE_WORKER using token county: " + tokenCounty);
                return tokenCounty;
            }
            // CASE_WORKER requires county from JWT token - no fallback
            System.out.println("⚠️ DataPipelineController: CASE_WORKER requires county from JWT token but none found");
            return null; // Will trigger error in requiresCountyBinding check
        }
        
        // For PROVIDER: MUST use token county (enforce county restriction)
        if (normalizedRole.contains("PROVIDER")) {
            if (tokenCounty != null && !tokenCounty.trim().isEmpty()) {
                // If requested county doesn't match token county, use token county (enforce restriction)
                if (requestedCounty != null && !requestedCounty.equalsIgnoreCase(tokenCounty)) {
                    System.out.println("⚠️ DataPipelineController: PROVIDER requested county " + requestedCounty + " overridden by token county " + tokenCounty);
                }
                System.out.println("🔍 DataPipelineController: PROVIDER using token county: " + tokenCounty);
                return tokenCounty;
            }
            // PROVIDER requires county from JWT token - no fallback
            System.out.println("⚠️ DataPipelineController: PROVIDER requires county from JWT token but none found");
            return null; // Will trigger error in requiresCountyBinding check
        }
        
        // For RECIPIENT: MUST use token county (enforce county restriction)
        if (normalizedRole.contains("RECIPIENT")) {
            if (tokenCounty != null && !tokenCounty.trim().isEmpty()) {
                // If requested county doesn't match token county, use token county (enforce restriction)
                if (requestedCounty != null && !requestedCounty.equalsIgnoreCase(tokenCounty)) {
                    System.out.println("⚠️ DataPipelineController: RECIPIENT requested county " + requestedCounty + " overridden by token county " + tokenCounty);
                }
                System.out.println("🔍 DataPipelineController: RECIPIENT using token county: " + tokenCounty);
                return tokenCounty;
            }
            // RECIPIENT requires county from JWT token - no fallback
            System.out.println("⚠️ DataPipelineController: RECIPIENT requires county from JWT token but none found");
            return null; // Will trigger error in requiresCountyBinding check
        }
        
        // For other roles: Use token county if available, otherwise requested county
        if (tokenCounty != null && !tokenCounty.trim().isEmpty()) {
            if (requestedCounty != null && !requestedCounty.equalsIgnoreCase(tokenCounty)) {
                System.out.println("⚠️ DataPipelineController: Requested county " + requestedCounty + " overridden by token county " + tokenCounty);
            }
            return tokenCounty;
        }
        // Central workers may specify county via request to expand scope
        if (normalizedRole.contains("CENTRAL") || normalizedRole.contains("ADMIN")) {
            return requestedCounty;
        }
        return requestedCounty;
    }


    /**
     * Get pipeline status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getPipelineStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔍 DataPipelineController: Getting pipeline status...");
            
            // Check if services are loaded
            boolean fieldMaskingLoaded = fieldMaskingService != null;
            boolean reportGenerationLoaded = reportGenerationService != null;
            // Role validation now handled by Keycloak
            boolean rulesEngineLoaded = rulesEngineService != null;
            boolean queryBuilderLoaded = queryBuilderService != null;
            boolean dataFetchingLoaded = dataFetchingService != null;
            
            System.out.println("📊 Service status - FieldMasking: " + fieldMaskingLoaded + 
                             ", ReportGeneration: " + reportGenerationLoaded +
                             ", Keycloak: UP (Authentication handled by Keycloak)" +
                             ", RulesEngine: " + rulesEngineLoaded +
                             ", QueryBuilder: " + queryBuilderLoaded +
                             ", DataFetching: " + dataFetchingLoaded);
            
            response.put("status", "SUCCESS");
            response.put("message", "Enhanced 5-stage data pipeline is operational");
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("services", Map.of(
                "fieldMasking", fieldMaskingLoaded ? "UP" : "DOWN", 
                "reportGeneration", reportGenerationLoaded ? "UP" : "DOWN",
                "keycloak", "UP (Authentication handled by Keycloak)",
                "rulesEngine", rulesEngineLoaded ? "UP" : "DOWN",
                "queryBuilder", queryBuilderLoaded ? "UP" : "DOWN",
                "dataFetching", dataFetchingLoaded ? "UP" : "DOWN"
            ));
            response.put("serviceDetails", Map.of(
                "fieldMaskingService", fieldMaskingService != null ? "LOADED" : "NULL",
                "reportGenerationService", reportGenerationService != null ? "LOADED" : "NULL",
                "keycloakService", "LOADED (Authentication handled by Keycloak)",
                "rulesEngineService", rulesEngineService != null ? "LOADED" : "NULL",
                "queryBuilderService", queryBuilderService != null ? "LOADED" : "NULL",
                "dataFetchingService", dataFetchingService != null ? "LOADED" : "NULL"
            ));
            
            System.out.println("✅ DataPipelineController: Pipeline status retrieved");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting pipeline status: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get pipeline status: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("stackTrace", e.getStackTrace());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get field visibility information for a specific role
     */
    @GetMapping("/field-visibility/{userRole}")
    public ResponseEntity<Map<String, Object>> getFieldVisibility(@PathVariable String userRole) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> visibilitySummary = fieldVisibilityService.getFieldVisibilitySummary(userRole);
            response.put("status", "SUCCESS");
            response.put("data", visibilitySummary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to get field visibility: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get all available fields across all roles
     */
    @GetMapping("/available-fields")
    public ResponseEntity<Map<String, Object>> getAvailableFields() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> availableFields = fieldVisibilityService.getAllAvailableFields();
            response.put("status", "SUCCESS");
            response.put("availableFields", availableFields);
            response.put("totalFields", availableFields.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to get available fields: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Compare multiple roles
     */
    @PostMapping("/compare-roles")
    public ResponseEntity<Map<String, Object>> compareRoles(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) request.get("roles");
            String startDate = (String) request.get("startDate");
            String endDate = (String) request.get("endDate");
            String countyId = (String) request.get("countyId");
            
            if (roles == null || roles.isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "No roles provided for comparison");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> comparison = new HashMap<>();
            
            for (String role : roles) {
                try {
                    // Generate report for each role
                    Map<String, Object> roleRequest = new HashMap<>();
                    roleRequest.put("userRole", role);
                    roleRequest.put("startDate", startDate);
                    roleRequest.put("endDate", endDate);
                    roleRequest.put("countyId", countyId);
                    
                    ResponseEntity<Map<String, Object>> roleResponse = generateReport(roleRequest, httpRequest);
                    Map<String, Object> roleData = roleResponse.getBody();
                    
                    if (roleData != null && "SUCCESS".equals(roleData.get("status"))) {
                        Map<String, Object> roleSummary = new HashMap<>();
                        roleSummary.put("recordCount", roleData.get("totalRecords"));
                        
                        // Get field visibility information for this role
                        try {
                            Map<String, Object> fieldVisibility = fieldVisibilityService.getFieldVisibilitySummary(role);
                            roleSummary.put("fieldVisibility", fieldVisibility);
                            System.out.println("🔍 Field visibility for " + role + ": " + fieldVisibility.get("visibleFields"));
                        } catch (Exception e) {
                            System.err.println("Could not get field visibility for role " + role + ": " + e.getMessage());
                        }
                        
                        // Get the data object for records extraction
                        Object dataObj = roleData.get("data");
                        roleSummary.put("status", "SUCCESS");
                        
                        // Include the actual records for detailed comparison
                        if (dataObj != null) {
                            try {
                                // Check if dataObj is a ReportData instance
                                if (dataObj instanceof com.cmips.model.ReportData) {
                                    com.cmips.model.ReportData reportData = (com.cmips.model.ReportData) dataObj;
                                    if (reportData.getRecords() != null) {
                                        roleSummary.put("records", reportData.getRecords());
                                        roleSummary.put("recordCount", reportData.getRecords().size());
                                        System.out.println("✅ Successfully extracted " + reportData.getRecords().size() + " records for role: " + role);
                                    }
                                } else if (dataObj instanceof java.util.List) {
                                    // Handle direct ArrayList/List response
                                    java.util.List<?> recordsList = (java.util.List<?>) dataObj;
                                    roleSummary.put("records", recordsList);
                                    roleSummary.put("recordCount", recordsList.size());
                                    System.out.println("✅ Successfully extracted " + recordsList.size() + " records from List for role: " + role);
                                } else {
                                    // Fallback: dataObj might be a Map or other structure
                                    System.out.println("⚠️ dataObj is not ReportData or List, type: " + dataObj.getClass().getSimpleName());
                                    roleSummary.put("records", new java.util.ArrayList<>());
                                    roleSummary.put("recordCount", 0);
                                }
                            } catch (Exception e) {
                                System.err.println("Could not extract records from data object: " + e.getMessage());
                                roleSummary.put("records", new java.util.ArrayList<>());
                                roleSummary.put("recordCount", 0);
                            }
                        }
                        
                        comparison.put(role, roleSummary);
                    } else {
                        Map<String, Object> roleSummary = new HashMap<>();
                        roleSummary.put("recordCount", 0);
                        roleSummary.put("totalHours", 0);
                        roleSummary.put("totalAmount", 0);
                        roleSummary.put("status", "ERROR");
                        roleSummary.put("message", roleData != null ? roleData.get("message") : "Unknown error");
                        comparison.put(role, roleSummary);
                    }
                } catch (Exception e) {
                    Map<String, Object> roleSummary = new HashMap<>();
                    roleSummary.put("recordCount", 0);
                    roleSummary.put("totalHours", 0);
                    roleSummary.put("totalAmount", 0);
                    roleSummary.put("status", "ERROR");
                    roleSummary.put("message", e.getMessage());
                    comparison.put(role, roleSummary);
                }
            }
            
            response.put("status", "SUCCESS");
            response.put("comparison", comparison);
            response.put("totalRoles", roles.size());
            response.put("generatedAt", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to compare roles: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Extract user info from Spring Security JWT object (preferred method)
     */
    private Map<String, Object> extractUserInfoFromJwtObject(Jwt jwt) {
        Map<String, Object> userInfo = new java.util.HashMap<>();
        
        try {
            System.out.println("🔐 DataPipelineController: Extracting user info from JWT object");
            System.out.println("🔍 DataPipelineController: All JWT claim names: " + jwt.getClaims().keySet());
            System.out.println("🔍 DataPipelineController: JWT claims: " + jwt.getClaims());
            
            // Extract role - same logic as SecurityConfig
            String extractedRole = null;
            
            // First, try to extract from resource_access (CLIENT roles)
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cmipsAppAccess = (Map<String, Object>) resourceAccess.get("cmips-frontend");
                if (cmipsAppAccess != null) {
                    @SuppressWarnings("unchecked")
                    List<String> clientRoles = (List<String>) cmipsAppAccess.get("roles");
                    if (clientRoles != null && !clientRoles.isEmpty()) {
                        for (String role : clientRoles) {
                            if (role != null && !role.trim().isEmpty()) {
                                extractedRole = com.cmips.util.RoleMapper.canonicalName(role);
                                System.out.println("🔐 DataPipelineController: Extracted CLIENT role from JWT object: " + role + " -> " + extractedRole);
                                break;
                            }
                        }
                    }
                }
            }
            
            // Fallback to realm_access.roles
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
                                extractedRole = com.cmips.util.RoleMapper.canonicalName(role);
                                System.out.println("🔐 DataPipelineController: Extracted REALM role from JWT object: " + role + " -> " + extractedRole);
                                break;
                            }
                        }
                    }
                }
            }
            
            if (extractedRole != null) {
                userInfo.put("role", extractedRole);
            }
            
            // Extract username
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null) {
                userInfo.put("username", preferredUsername);
                System.out.println("🔐 DataPipelineController: Extracted username from JWT object: " + preferredUsername);
            }
            
            // Extract countyId - try direct field first
            // Support both camelCase (countyId) and snake_case (county_id) formats
            System.out.println("🔍 DataPipelineController: Attempting to extract countyId from JWT object...");
            String countyId = jwt.getClaimAsString("countyId");
            System.out.println("🔍 DataPipelineController: Direct countyId claim: " + countyId);
            
            // If not found, try snake_case
            if (countyId == null || countyId.trim().isEmpty()) {
                countyId = jwt.getClaimAsString("county_id");
                System.out.println("🔍 DataPipelineController: Direct county_id claim: " + countyId);
            }
            
            if (countyId != null && !countyId.trim().isEmpty()) {
                userInfo.put("countyId", countyId);
                System.out.println("✅ DataPipelineController: Found countyId directly in JWT object: " + countyId);
            } else {
                // Try attributes.countyId or attributes.county_id
                System.out.println("🔍 DataPipelineController: Checking attributes map for countyId...");
                Map<String, Object> attributes = jwt.getClaimAsMap("attributes");
                if (attributes != null) {
                    System.out.println("🔍 DataPipelineController: Attributes map exists, keys: " + attributes.keySet());
                    Object countyIdObj = attributes.get("countyId");
                    if (countyIdObj == null) {
                        // Try snake_case
                        countyIdObj = attributes.get("county_id");
                    }
                    System.out.println("🔍 DataPipelineController: countyId from attributes: " + countyIdObj + " (type: " + (countyIdObj != null ? countyIdObj.getClass().getSimpleName() : "null") + ")");
                    if (countyIdObj != null) {
                        if (countyIdObj instanceof List && ((List<?>) countyIdObj).size() > 0) {
                            countyId = ((List<?>) countyIdObj).get(0).toString();
                            System.out.println("🔍 DataPipelineController: Extracted countyId from list: " + countyId);
                        } else {
                            countyId = countyIdObj.toString();
                            System.out.println("🔍 DataPipelineController: Extracted countyId from object: " + countyId);
                        }
                        if (countyId != null && !countyId.trim().isEmpty()) {
                            userInfo.put("countyId", countyId);
                            System.out.println("✅ DataPipelineController: Extracted countyId from attributes in JWT object: " + countyId);
                        } else {
                            System.out.println("⚠️ DataPipelineController: countyId extracted but is null or empty");
                        }
                    } else {
                        System.out.println("⚠️ DataPipelineController: countyId not found in attributes map");
                    }
                } else {
                    System.out.println("⚠️ DataPipelineController: No attributes map in JWT object");
                }
            }
            
            // NO FALLBACK - countyId MUST be in JWT token
            if (!userInfo.containsKey("countyId")) {
                System.err.println("❌ DataPipelineController: countyId NOT FOUND in JWT token. Token must contain countyId or county_id.");
                System.err.println("❌ DataPipelineController: JWT claims available: " + jwt.getClaims().keySet());
                // Do NOT set default - let it fail explicitly
            }
            
            System.out.println("🔐 DataPipelineController: Final extracted userInfo from JWT object: " + userInfo);
            return userInfo;
            
        } catch (Exception e) {
            System.err.println("❌ DataPipelineController: Error extracting user info from JWT object: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extract user info from JWT token string (fallback method)
     * NOTE: JWT parsing is now done directly since KeycloakService is removed
     */
    private Map<String, Object> extractUserInfoFromJWT(String jwtToken) {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return null;
        }
        try {
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            
            // Debug: Log JWT payload structure to help diagnose county extraction issues
            java.util.List<String> payloadKeys = new java.util.ArrayList<>();
            jsonNode.fieldNames().forEachRemaining(payloadKeys::add);
            System.out.println("🔍 DataPipelineController: JWT payload keys: " + payloadKeys);
            if (jsonNode.has("attributes")) {
                System.out.println("🔍 DataPipelineController: JWT has 'attributes' field: " + jsonNode.get("attributes").toString());
            }
            if (jsonNode.has("preferred_username")) {
                System.out.println("🔍 DataPipelineController: JWT preferred_username: " + jsonNode.get("preferred_username").asText());
            }
            
            Map<String, Object> userInfo = new java.util.HashMap<>();
            
            // Extract role - same logic as SecurityConfig: check client roles first, then realm roles
            String extractedRole = null;
            
            // First, try to extract from resource_access (CLIENT roles) - this is the primary source
            if (jsonNode.has("resource_access")) {
                com.fasterxml.jackson.databind.JsonNode resourceAccess = jsonNode.get("resource_access");
                if (resourceAccess.has("cmips-frontend") && resourceAccess.get("cmips-frontend").has("roles")) {
                    com.fasterxml.jackson.databind.JsonNode clientRoles = resourceAccess.get("cmips-frontend").get("roles");
                    if (clientRoles.isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode roleNode : clientRoles) {
                            String role = roleNode.asText();
                            if (role != null && !role.trim().isEmpty()) {
                                // Use RoleMapper to get canonical role name
                                extractedRole = com.cmips.util.RoleMapper.canonicalName(role);
                                System.out.println("🔐 DataPipelineController: Extracted CLIENT role: " + role + " -> " + extractedRole);
                                break; // Use first valid client role
                            }
                        }
                    }
                }
            }
            
            // Fallback to realm_access.roles if no client roles found
            if (extractedRole == null && jsonNode.has("realm_access") && jsonNode.get("realm_access").has("roles")) {
                com.fasterxml.jackson.databind.JsonNode roles = jsonNode.get("realm_access").get("roles");
                if (roles.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode roleNode : roles) {
                        String role = roleNode.asText();
                        // Filter out null, default roles, and system roles
                        if (role != null && !role.trim().isEmpty() && 
                            !role.startsWith("default-roles-") &&
                            !role.equals("offline_access") &&
                            !role.equals("uma_authorization")) {
                            extractedRole = com.cmips.util.RoleMapper.canonicalName(role);
                            System.out.println("🔐 DataPipelineController: Extracted REALM role: " + role + " -> " + extractedRole);
                            break; // Use first valid realm role
                        }
                    }
                }
            }
            
            if (extractedRole != null) {
                userInfo.put("role", extractedRole);
            }
            
            // Extract username
            if (jsonNode.has("preferred_username")) {
                userInfo.put("username", jsonNode.get("preferred_username").asText());
            }
            
            // Extract countyId from attributes (Keycloak custom attributes)
            // Support both camelCase (countyId) and snake_case (county_id) formats
            System.out.println("🔍 DataPipelineController: Attempting to extract countyId from JWT...");
            String extractedCountyId = null;
            
            // Check for camelCase first
            if (jsonNode.has("countyId")) {
                extractedCountyId = jsonNode.get("countyId").asText();
                System.out.println("✅ DataPipelineController: Found countyId directly in JWT: " + extractedCountyId);
            } else if (jsonNode.has("county_id")) {
                // Check for snake_case
                extractedCountyId = jsonNode.get("county_id").asText();
                System.out.println("✅ DataPipelineController: Found county_id directly in JWT: " + extractedCountyId);
            } else if (jsonNode.has("attributes")) {
                com.fasterxml.jackson.databind.JsonNode attributes = jsonNode.get("attributes");
                System.out.println("🔍 DataPipelineController: JWT has 'attributes' object, checking for countyId...");
                if (attributes.has("countyId")) {
                    com.fasterxml.jackson.databind.JsonNode countyIdNode = attributes.get("countyId");
                    System.out.println("🔍 DataPipelineController: Found countyId in attributes: " + countyIdNode.toString());
                    if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                        extractedCountyId = countyIdNode.get(0).asText();
                    } else if (countyIdNode.isTextual()) {
                        extractedCountyId = countyIdNode.asText();
                    }
                } else if (attributes.has("county_id")) {
                    // Check for snake_case in attributes
                    com.fasterxml.jackson.databind.JsonNode countyIdNode = attributes.get("county_id");
                    System.out.println("🔍 DataPipelineController: Found county_id in attributes: " + countyIdNode.toString());
                    if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                        extractedCountyId = countyIdNode.get(0).asText();
                    } else if (countyIdNode.isTextual()) {
                        extractedCountyId = countyIdNode.asText();
                    }
                } else {
                    System.out.println("⚠️ DataPipelineController: 'attributes' object exists but no 'countyId' or 'county_id' field found");
                    // Log all attribute keys for debugging
                    if (attributes.isObject()) {
                        java.util.List<String> attributeKeys = new java.util.ArrayList<>();
                        attributes.fieldNames().forEachRemaining(attributeKeys::add);
                        System.out.println("🔍 DataPipelineController: Available attribute keys: " + attributeKeys);
                    }
                }
            }
            
            if (extractedCountyId != null) {
                userInfo.put("countyId", extractedCountyId);
                System.out.println("✅ DataPipelineController: Extracted countyId: " + extractedCountyId);
            } else {
                System.out.println("⚠️ DataPipelineController: JWT has no 'countyId' field and no 'attributes' object");
            }
            
            // NO FALLBACK - countyId MUST be in JWT token
            if (!userInfo.containsKey("countyId")) {
                System.err.println("❌ DataPipelineController: countyId NOT FOUND in JWT token. Token must contain countyId in attributes.countyId.");
                java.util.List<String> fieldNames = new java.util.ArrayList<>();
                jsonNode.fieldNames().forEachRemaining(fieldNames::add);
                System.err.println("❌ DataPipelineController: JWT payload keys: " + fieldNames);
                // Do NOT set default - let it fail explicitly
            }
            
            System.out.println("🔐 DataPipelineController: Final extracted userInfo: " + userInfo);
            return userInfo;
        } catch (Exception e) {
            System.err.println("❌ DataPipelineController: Error extracting user info from JWT: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
