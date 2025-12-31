package com.cmips.service;

import com.cmips.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportGenerationService {

    
    @Autowired
    private RulesEngineService rulesEngineService;
    
    @Autowired
    private QueryBuilderService queryBuilderService;
    
    @Autowired
    private DataFetchingService dataFetchingService;
    
    @Autowired
    private FieldMaskingService fieldMaskingService;
    
    @Autowired
    private FieldVisibilityService fieldVisibilityService;

    @Autowired
    private EventService eventService;

    public ReportGenerationService() {
        System.out.println("🔧 ReportGenerationService: Constructor called - initializing...");
        try {
            System.out.println("✅ ReportGenerationService: Constructor completed successfully");
        } catch (Exception e) {
            System.err.println("❌ ReportGenerationService: Constructor failed with error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Generate report based on report type and user role (JWT-ONLY method)
     */
    public ReportGenerationResponse generateReport(ReportGenerationRequest request) {
        System.out.println("🚀 ReportGenerationService.generateReport() called (JWT-ONLY method) for role: " + request.getUserRole());
        throw new RuntimeException("Legacy method disabled. Use generateReport(request, jwtToken) with JWT token.");
    }

    /**
     * Generate report using Keycloak JWT token (JWT-ONLY method)
     */
    public ReportGenerationResponse generateReport(ReportGenerationRequest request, String jwtToken) {
        System.out.println("🚀 ReportGenerationService.generateReport() called (JWT-ONLY method) for role: " + request.getUserRole());
        System.out.println("📊 ReportGenerationService: Generating " + request.getReportType() + " report for role: " + request.getUserRole());
        System.out.println("🔐 JWT Token provided: " + (jwtToken != null ? "YES" : "NO"));
        
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required for report generation. No fallback methods available.");
        }
        
        try {
            System.out.println("🔍 ReportGenerationService: Starting JWT-ONLY report generation for role: " + request.getUserRole());
            System.out.println("🔍 ReportGenerationService: JWT token provided: " + (jwtToken != null ? "YES" : "NO"));
            System.out.println("🔍 ReportGenerationService: Request details: userRole=" + request.getUserRole() + ", startDate=" + request.getStartDate() + ", endDate=" + request.getEndDate() + ", countyId=" + request.getUserCounty());
            
            // Step 1: Generate simple table data directly with JWT support
            System.out.println("🔍 ReportGenerationService: Calling generateSimpleReportDataWithJWT...");
            ReportData reportData = generateSimpleReportDataWithJWT(request, jwtToken);
            System.out.println("🔍 ReportGenerationService: generateSimpleReportDataWithJWT returned: " + (reportData != null ? reportData.getRecords().size() + " records" : "NULL"));
            
            // Step 3: Create report response
            ReportGenerationResponse response = new ReportGenerationResponse();
            response.setReportId(UUID.randomUUID().toString());
            response.setReportType(request.getReportType());
            response.setUserRole(request.getUserRole());
            response.setGeneratedAt(LocalDateTime.now());
            response.setData(reportData);
            response.setStatus("SUCCESS");
            // Use totalCount for totalRecords (total records matching query), not current page size
            response.setTotalRecords(reportData.getTotalCount() != null ? reportData.getTotalCount().intValue() : 0);

            // Step 4: Publish report generation event
            publishReportEvent(request, response);

            System.out.println("✅ ReportGenerationService: Report generated successfully (JWT-ONLY method)");
            System.out.println("✅ Report source: Keycloak JWT");
            return response;

        } catch (Exception e) {
            System.err.println("❌ Error generating report (JWT-ONLY method): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate report with JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Create extraction request from report request
     */
    private PipelineExtractionRequest createExtractionRequest(ReportGenerationRequest request) {
        PipelineExtractionRequest extractionRequest = new PipelineExtractionRequest();
        extractionRequest.setUserRole(request.getUserRole());
        extractionRequest.setReportType(request.getReportType());
        extractionRequest.setDateRange(request.getDateRange());
        extractionRequest.setStatusFilter(request.getStatusFilter());
        extractionRequest.setProviderFilter(request.getProviderFilter());
        extractionRequest.setProjectFilter(request.getProjectFilter());
        
        // Set access control fields
        extractionRequest.setUserCounty(request.getUserCounty());
        
        UserRole role = UserRole.from(request.getUserRole());
        boolean enforceCountyAccess = switch (role) {
            case CASE_WORKER, PROVIDER, RECIPIENT, SUPERVISOR -> true; // SUPERVISOR also requires county from JWT token
            default -> false;
        };

        extractionRequest.setEnforceCountyAccess(enforceCountyAccess);

        if (enforceCountyAccess) {
            // For roles that require county access, county MUST be provided in request
            // No fallback to default - if missing, it will fail validation downstream
            if (request.getUserCounty() != null && !request.getUserCounty().isEmpty()) {
                extractionRequest.setUserCounty(request.getUserCounty());
            } else {
                // No fallback - county is required but missing, leave it null to trigger validation error
                System.out.println("⚠️ ReportGenerationService: County is required for role " + role + " but not provided in request");
                extractionRequest.setUserCounty(null);
            }
        } else if (request.getUserCounty() != null && !request.getUserCounty().isEmpty()) {
            // For roles that don't enforce county access, allow optional county filter
            extractionRequest.setUserCounty(request.getUserCounty());
        }
        
        return extractionRequest;
    }

    /**
     * Generate timesheet report data
     */
    private ReportData generateReportData(PipelineExtractionResponse extractionResponse, ReportGenerationRequest request) {
        ReportData reportData = new ReportData();
        reportData.setReportType("TIMESHEET_REPORT");
        reportData.setUserRole(request.getUserRole());
        reportData.setGeneratedAt(LocalDateTime.now());

        List<MaskedTimesheetData> maskedData = extractionResponse.getData();
        
        // Convert masked data to table format with camelCase field names
        List<Map<String, Object>> tableData = new ArrayList<>();
        for (MaskedTimesheetData maskedRecord : maskedData) {
            Map<String, Object> record = new HashMap<>();
            // Use the fields that were already masked by the field masking service
            // Convert field names to camelCase for frontend compatibility
            for (Map.Entry<String, Object> entry : maskedRecord.getFields().entrySet()) {
                String camelCaseKey = convertToCamelCase(entry.getKey());
                record.put(camelCaseKey, entry.getValue());
            }
            tableData.add(record);
        }
        
        reportData.setRecords(tableData);
        reportData.setTotalRecords(maskedData.size());
        reportData.setFieldVisibility(extractionResponse.getSummary().getFieldVisibility());
        reportData.setStatusDistribution(extractionResponse.getSummary().getStatusDistribution());

        return reportData;
    }

    /**
     * Generate simple report data directly without complex pipeline (Legacy method)
     */
    private ReportData generateSimpleReportData(ReportGenerationRequest request) {
        System.out.println("🔍 DEBUG: generateSimpleReportData called (legacy method) for role: " + request.getUserRole());
        
        ReportData reportData = new ReportData();
        reportData.setReportType("TIMESHEET_REPORT");
        reportData.setUserRole(request.getUserRole());
        reportData.setGeneratedAt(LocalDateTime.now());

        // Get simple table data directly from database with role-based filtering
        List<Map<String, Object>> tableData = generateSimpleTableDataFromDB(request);
        System.out.println("🔍 DEBUG: generateSimpleTableDataFromDB returned " + (tableData != null ? tableData.size() : "NULL") + " records");
        
        reportData.setRecords(tableData);
        reportData.setTotalRecords(tableData != null ? tableData.size() : 0);
        
        System.out.println("🔍 DEBUG: reportData.getRecords() = " + reportData.getRecords());
        System.out.println("🔍 DEBUG: reportData.getRecords().size() = " + (reportData.getRecords() != null ? reportData.getRecords().size() : "NULL"));

        return reportData;
    }

    /**
     * Generate simple report data with JWT support (Hybrid method)
     */
    private ReportData generateSimpleReportDataWithJWT(ReportGenerationRequest request, String jwtToken) {
        System.out.println("🔍 DEBUG: generateSimpleReportDataWithJWT called (hybrid method) for role: " + request.getUserRole());
        System.out.println("🔍 JWT Token provided: " + (jwtToken != null ? "YES" : "NO"));
        System.out.println("🔍 Request details: userRole=" + request.getUserRole() + ", startDate=" + request.getStartDate() + ", endDate=" + request.getEndDate() + ", countyId=" + request.getUserCounty());
        
        ReportData reportData = new ReportData();
        reportData.setReportType("TIMESHEET_REPORT");
        reportData.setUserRole(request.getUserRole());
        reportData.setGeneratedAt(LocalDateTime.now());

        // Get simple table data directly from database with JWT-based field masking
        // This method now returns both tableData and totalCount
        Map<String, Object> result = generateSimpleTableDataFromDBWithJWT(request, jwtToken);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tableData = (List<Map<String, Object>>) result.get("tableData");
        long totalCount = ((Number) result.get("totalCount")).longValue();
        
        System.out.println("🔍 DEBUG: generateSimpleTableDataFromDBWithJWT returned " + (tableData != null ? tableData.size() : "NULL") + " records, total count: " + totalCount);
        
        reportData.setRecords(tableData);
        // Use totalCount for totalRecords (total records matching query), not current page size
        reportData.setTotalRecords((int) totalCount);
        reportData.setTotalCount(totalCount);
        
        System.out.println("🔍 DEBUG: reportData.getRecords() = " + reportData.getRecords());
        System.out.println("🔍 DEBUG: reportData.getRecords().size() = " + (reportData.getRecords() != null ? reportData.getRecords().size() : "NULL"));
        System.out.println("🔍 DEBUG: reportData.getTotalCount() = " + reportData.getTotalCount());

        return reportData;
    }

    /**
     * Generate simple table data using the correct 5-stage pipeline
     */
    private List<Map<String, Object>> generateSimpleTableDataFromDB(ReportGenerationRequest request) {
        try {
            System.out.println("📊 ReportGenerationService: Starting 5-stage pipeline for " + request.getUserRole());
            
            // STAGE 1: Extract parameters from request and validate user
            System.out.println("🔍 Stage 1: Parameter extraction and user validation (handled by Keycloak)");
            
            // STAGE 2: Build query with extracted parameters
            System.out.println("🔍 Stage 2: Query building with extracted parameters");
            System.out.println("🔍 Request userRole: " + request.getUserRole());
            System.out.println("🔍 Request userCounty: " + request.getUserCounty());
            System.out.println("🔍 Request startDate: " + request.getStartDate());
            System.out.println("🔍 Request endDate: " + request.getEndDate());
            
            QueryBuilderService.QueryParameters queryParams = queryBuilderService.buildQuery(
                request.getUserRole(),
                request.getUserCounty(),
                request.getStartDate(),
                request.getEndDate(),
                null
            );
            
            // STAGE 3: Fetch data from database
            System.out.println("🔍 Stage 3: Data fetching from database");
            DataFetchingService.DataFetchResult fetchResult = dataFetchingService.fetchData(queryParams);
            if (!fetchResult.isSuccess()) {
                throw new RuntimeException("Data fetching failed: " + fetchResult.getMessage());
            }
            
            // STAGE 4: Apply field masking based on role permissions
            // NOTE: This legacy method is deprecated - use generateSimpleReportDataWithJWT instead
            System.out.println("⚠️ WARNING: generateSimpleTableDataFromDB is deprecated. Use generateSimpleTableDataFromDBWithJWT with JWT token.");
            throw new RuntimeException("Legacy method disabled. This method requires JWT token. Use generateSimpleReportDataWithJWT instead.");
            
        } catch (Exception e) {
            System.err.println("❌ Error in 5-stage pipeline: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Generate simple table data using JWT-based field masking (Hybrid method)
     * Returns a Map containing "tableData" and "totalCount"
     */
    private Map<String, Object> generateSimpleTableDataFromDBWithJWT(ReportGenerationRequest request, String jwtToken) {
        try {
            System.out.println("📊 ReportGenerationService: Starting 5-stage pipeline with JWT support for " + request.getUserRole());
            System.out.println("🔐 JWT Token provided: " + (jwtToken != null ? "YES" : "NO"));
            
            // STAGE 1: Extract parameters from request and validate user
            System.out.println("🔍 Stage 1: Parameter extraction and user validation (handled by Keycloak)");
            
            // STAGE 2: Build query with extracted parameters
            System.out.println("🔍 Stage 2: Query building with extracted parameters");
            System.out.println("🔍 Request userRole: " + request.getUserRole());
            System.out.println("🔍 Request userCounty: " + request.getUserCounty());
            System.out.println("🔍 Request userCounty is null: " + (request.getUserCounty() == null));
            System.out.println("🔍 Request userCounty is empty: " + (request.getUserCounty() != null && request.getUserCounty().trim().isEmpty()));
            System.out.println("🔍 Request startDate: " + request.getStartDate());
            System.out.println("🔍 Request endDate: " + request.getEndDate());
            
            QueryBuilderService.QueryParameters queryParams = queryBuilderService.buildQuery(
                request.getUserRole(),
                request.getUserCounty(),
                request.getStartDate(),
                request.getEndDate(),
                null
            );
            
            System.out.println("🔍 QueryParams countyId after buildQuery: " + queryParams.getCountyId());
            System.out.println("🔍 QueryParams countyId is null: " + (queryParams.getCountyId() == null));
            System.out.println("🔍 QueryParams countyId is empty: " + (queryParams.getCountyId() != null && queryParams.getCountyId().trim().isEmpty()));
            
            // STAGE 3: Fetch data from database with pagination
            System.out.println("🔍 Stage 3: Data fetching from database with pagination");
            int page = request.getPage() != null ? request.getPage() : 0;
            int pageSize = request.getPageSize() != null ? request.getPageSize() : 500;
            
            System.out.println("📄 Processing page " + page + " with page size " + pageSize);
            DataFetchingService.DataFetchResult fetchResult = dataFetchingService.fetchData(queryParams, page, pageSize);
            if (!fetchResult.isSuccess()) {
                throw new RuntimeException("Data fetching failed: " + fetchResult.getMessage());
            }
            
            // Store total count for pagination info
            long totalCount = fetchResult.getTotalCount();
            System.out.println("📊 Total records in date range: " + totalCount);
            
            // STAGE 4: Apply field masking using Keycloak JWT token (process in batches)
            System.out.println("🔍 Stage 4: Field masking using Keycloak JWT token (batch size: " + pageSize + ")");
            List<MaskedTimesheetData> maskedData = fieldMaskingService.applyFieldMasking(
                fetchResult.getData(), 
                request.getUserRole(),
                request.getReportType(),
                jwtToken  // Pass JWT token for Keycloak-based masking
            );
            
            // STAGE 5: Generate report with configured data
            System.out.println("🔍 Stage 5: Generate report with configured data");
            List<Map<String, Object>> tableData = new ArrayList<>();
            
            for (MaskedTimesheetData maskedRecord : maskedData) {
                Map<String, Object> record = new HashMap<>();
                // Use the fields that were already masked by the field masking service
                // Convert field names to camelCase for frontend compatibility
                for (Map.Entry<String, Object> entry : maskedRecord.getFields().entrySet()) {
                    String camelCaseKey = convertToCamelCase(entry.getKey());
                    record.put(camelCaseKey, entry.getValue());
                }
                tableData.add(record);
            }
            
            System.out.println("✅ 5-stage pipeline with JWT completed: Generated " + tableData.size() + " records for " + request.getUserRole() + " (page " + page + ")");
            System.out.println("✅ Field masking source: " + (jwtToken != null ? "Keycloak JWT" : "Fallback Storage"));
            
            // Return both tableData and totalCount
            Map<String, Object> result = new HashMap<>();
            result.put("tableData", tableData);
            result.put("totalCount", totalCount);
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error in 5-stage pipeline with JWT: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate report with JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Convert field name from lowercase to camelCase for frontend compatibility
     */
    private String convertToCamelCase(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }
        
        // Handle specific field mappings
        switch (fieldName.toLowerCase()) {
            case "timesheetid":
                return "timesheetId";
            case "providerid":
                return "providerId";
            case "providername":
                return "providerName";
            case "provideremail":
                return "providerEmail";
            case "recipientid":
            case "recipient_id":
                return "recipientId";
            case "recipientname":
                return "recipientName";
            case "totalhours":
                return "totalHours";
            case "totalamount":
                return "totalAmount";
            case "service_type":
                return "serviceType";
            case "provider_county":
            case "providercounty":
                return "providerCounty";
            case "recipient_county":
            case "recipientcounty":
                return "recipientCounty";
            default:
                // For other fields, convert snake_case to camelCase
                String[] parts = fieldName.split("_");
                if (parts.length == 1) {
                    return parts[0];
                }
                StringBuilder camelCase = new StringBuilder(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    camelCase.append(parts[i].substring(0, 1).toUpperCase())
                            .append(parts[i].substring(1));
                }
                return camelCase.toString();
        }
    }

    /**
     * Generate simple table data with only visible fields for the role
     */
    private List<Map<String, Object>> generateSimpleTableData(List<MaskedTimesheetData> maskedData, ReportGenerationRequest request) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        String userRole = request.getUserRole();
        
        // Get visible fields for this role - using default fields since no JWT token available
        List<String> visibleFields = fieldVisibilityService.getDefaultVisibleFields();
        System.out.println("🔍 Visible fields for " + userRole + ": " + visibleFields);
        
        for (MaskedTimesheetData data : maskedData) {
            Map<String, Object> record = new HashMap<>();
            
            // Add only visible fields
            for (String fieldName : visibleFields) {
                String fieldKey = fieldName.toLowerCase();
                Object value = data.getFields().get(fieldKey);
                record.put(fieldName, value != null ? value : "");
            }
            
            tableData.add(record);
        }
        
        return tableData;
    }

    /**
     * Generate Timesheet Report - Comprehensive timesheet data based on user role
     * Only includes fields that are visible for the specific user role
     */
    private List<Map<String, Object>> generateTimesheetReport(List<MaskedTimesheetData> maskedData, ReportGenerationRequest request) {
        List<Map<String, Object>> records = new ArrayList<>();
        String userRole = request.getUserRole();
        
        // Get visible fields for this role - using default fields since no JWT token available
        List<String> visibleFields = fieldVisibilityService.getDefaultVisibleFields();
        System.out.println("🔍 Visible fields for " + userRole + ": " + visibleFields);
        
        for (MaskedTimesheetData data : maskedData) {
            // Create a Map to only include visible fields
            Map<String, Object> record = new HashMap<>();
            
            // Dynamically add only visible fields
            for (String fieldName : visibleFields) {
                String fieldKey = fieldName.toLowerCase();
                Object value = data.getFields().get(fieldKey);
                if (value != null) {
                    record.put(fieldName, value);
                }
            }
            
            records.add(record);
        }

        // Sort by submission date descending (most recent first)
        records.sort((a, b) -> {
            Object aSubmitted = a.get("submittedAt");
            Object bSubmitted = b.get("submittedAt");
            if (aSubmitted == null && bSubmitted == null) return 0;
            if (aSubmitted == null) return 1;
            if (bSubmitted == null) return -1;
            if (aSubmitted instanceof LocalDateTime && bSubmitted instanceof LocalDateTime) {
                return ((LocalDateTime) bSubmitted).compareTo((LocalDateTime) aSubmitted);
            }
            return 0;
        });
        
        System.out.println("📊 Generated Timesheet Report with " + records.size() + " records for role: " + request.getUserRole());
        return records;
    }


    /**
     * Publish report generation event
     */
    private void publishReportEvent(ReportGenerationRequest request, ReportGenerationResponse response) {
        try {
            ReportGenerationEvent event = new ReportGenerationEvent();
            event.setEventType("REPORT_GENERATED");
            event.setReportId(response.getReportId());
            event.setReportType(request.getReportType());
            event.setUserRole(request.getUserRole());
            event.setTotalRecords(response.getTotalRecords());
            event.setGeneratedAt(LocalDateTime.now());

            eventService.publishEvent("report-generation-events", event);
            System.out.println("📡 Report generation event recorded in application event log");

        } catch (Exception e) {
            System.err.println("❌ Error publishing report event: " + e.getMessage());
        }
    }

    /**
     * Create error response
     */
    private ReportGenerationResponse createErrorResponse(ReportGenerationRequest request, String errorMessage) {
        ReportGenerationResponse response = new ReportGenerationResponse();
        response.setReportId(UUID.randomUUID().toString());
        response.setReportType(request.getReportType());
        response.setUserRole(request.getUserRole());
        response.setGeneratedAt(LocalDateTime.now());
        response.setTotalRecords(0);
        response.setStatus("ERROR");
        response.setErrorMessage(errorMessage);
        return response;
    }
}
