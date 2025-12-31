package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.*;
import com.example.kafkaeventdrivenapp.service.CountyBasedDataExtractionService;
import com.example.kafkaeventdrivenapp.service.FieldMaskingService;
import com.example.kafkaeventdrivenapp.service.ReportGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * County-Based Pipeline Controller
 * Ensures data is fetched by county location FIRST, then field masking is applied
 */
@RestController
@RequestMapping("/api/county-pipeline")
@CrossOrigin(origins = "*")
public class CountyBasedPipelineController {

    @Autowired
    private CountyBasedDataExtractionService countyBasedDataExtractionService;

    @Autowired
    private FieldMaskingService fieldMaskingService;
    
    @Autowired
    private ReportGenerationService reportGenerationService;
    
    @Autowired
    private com.example.kafkaeventdrivenapp.config.ReportTypeProperties reportTypeProperties;
    

    public CountyBasedPipelineController() {
        System.out.println("🔧 CountyBasedPipelineController: Constructor called - initializing...");
        try {
            System.out.println("✅ CountyBasedPipelineController: Constructor completed successfully");
        } catch (Exception e) {
            System.err.println("❌ CountyBasedPipelineController: Constructor failed with error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Extract data with COUNTY-LOCATION FIRST approach
     */
    @PostMapping("/extract")
    public ResponseEntity<Map<String, Object>> extractDataByCounty(@RequestBody PipelineExtractionRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            UserRole role = UserRole.from(request.getUserRole());
            String canonicalRole = role.name();
            request.setUserRole(canonicalRole);
            System.out.println("📍 CountyBasedPipelineController: Starting COUNTY-FIRST data extraction for role: " + canonicalRole);
            
            // Validate county information
            if (request.getUserCounty() == null && role != UserRole.ADMIN) {
                response.put("status", "ERROR");
                response.put("message", "County information is required for data extraction");
                return ResponseEntity.badRequest().body(response);
            }
            
            PipelineExtractionResponse extractionResponse = countyBasedDataExtractionService.extractDataByCounty(request);
            
            if ("SUCCESS".equals(extractionResponse.getStatus())) {
                response.put("status", "SUCCESS");
                response.put("message", "County-based data extraction completed successfully");
                response.put("extractionId", extractionResponse.getExtractionId());
                response.put("totalRecords", extractionResponse.getTotalRecords());
                response.put("maskedRecords", extractionResponse.getMaskedRecords());
                response.put("data", extractionResponse.getData());
                response.put("summary", extractionResponse.getSummary());
                response.put("extractionMethod", "COUNTY_FIRST");
                
                System.out.println("✅ CountyBasedPipelineController: COUNTY-FIRST extraction completed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "ERROR");
                response.put("message", "County-based data extraction failed: " + extractionResponse.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in county-first data extraction: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to extract data by county: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Generate report with COUNTY-LOCATION FIRST approach
     */
    @PostMapping("/generate-report")
    public ResponseEntity<Map<String, Object>> generateReportByCounty(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📊 CountyBasedPipelineController: Generating COUNTY-FIRST report...");
            
            if (countyBasedDataExtractionService == null || reportGenerationService == null) {
                System.err.println("❌ Required services are null!");
                response.put("status", "ERROR");
                response.put("message", "Required services are not loaded");
                return ResponseEntity.internalServerError().body(response);
            }
            
            String userRole = (String) request.get("userRole");
            String userCounty = (String) request.get("userCounty");
            
            if (userRole == null) {
                response.put("status", "ERROR");
                response.put("message", "userRole is required");
                return ResponseEntity.badRequest().body(response);
            }
            UserRole role = UserRole.from(userRole);
            String canonicalRole = role.name();
            if (userCounty == null && role != UserRole.ADMIN) {
                response.put("status", "ERROR");
                response.put("message", "userCounty is required for non-admin roles");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create county-based extraction request
            PipelineExtractionRequest extractionRequest = new PipelineExtractionRequest();
            extractionRequest.setUserRole(canonicalRole);
            extractionRequest.setUserCounty(userCounty);
            extractionRequest.setReportType(reportTypeProperties.getDefaultReportType());
            extractionRequest.setEnforceCountyAccess(true);
            
            // Set date range if provided
            @SuppressWarnings("unchecked")
            Map<String, Object> dateRange = (Map<String, Object>) request.get("dateRange");
            if (dateRange != null && dateRange.get("startDate") != null && dateRange.get("endDate") != null) {
                PipelineExtractionRequest.DateRange dateRangeObj = new PipelineExtractionRequest.DateRange(
                    java.time.LocalDate.parse(dateRange.get("startDate").toString()),
                    java.time.LocalDate.parse(dateRange.get("endDate").toString())
                );
                extractionRequest.setDateRange(dateRangeObj);
            }
            
            // Set additional filters if provided
            @SuppressWarnings("unchecked")
            List<String> statusFilter = (List<String>) request.get("statusFilter");
            if (statusFilter != null) {
                extractionRequest.setStatusFilter(statusFilter);
            }
            
            @SuppressWarnings("unchecked")
            List<String> providerFilter = (List<String>) request.get("providerFilter");
            if (providerFilter != null) {
                extractionRequest.setProviderFilter(providerFilter);
            }
            
            System.out.println("📍 Generating COUNTY-FIRST report for role: " + canonicalRole + ", county: " + userCounty);
            
            // STEP 1: Extract data by county FIRST
            PipelineExtractionResponse extractionResponse = countyBasedDataExtractionService.extractDataByCounty(extractionRequest);
            
            if (!"SUCCESS".equals(extractionResponse.getStatus())) {
                response.put("status", "ERROR");
                response.put("message", "County-based data extraction failed: " + extractionResponse.getErrorMessage());
                return ResponseEntity.internalServerError().body(response);
            }
            
            // STEP 2: Generate report from county-filtered data
            ReportGenerationRequest reportRequest = new ReportGenerationRequest();
            reportRequest.setUserRole(canonicalRole);
            reportRequest.setReportType(reportTypeProperties.getDefaultReportType());
            
            if (dateRange != null && dateRange.get("startDate") != null && dateRange.get("endDate") != null) {
                reportRequest.setStartDate(java.time.LocalDate.parse(dateRange.get("startDate").toString()));
                reportRequest.setEndDate(java.time.LocalDate.parse(dateRange.get("endDate").toString()));
            }
            
            // Create report data from county-filtered and masked data
            ReportData reportData = createReportDataFromCountyExtraction(extractionResponse, reportRequest);
            
            // Create final response
            response.put("status", "SUCCESS");
            response.put("message", "County-first report generated successfully");
            response.put("reportId", java.util.UUID.randomUUID().toString());
            response.put("reportType", reportTypeProperties.getDefaultReportType());
            response.put("userRole", canonicalRole);
            response.put("userCounty", userCounty);
            response.put("generatedAt", java.time.LocalDateTime.now());
            response.put("data", reportData);
            response.put("summary", extractionResponse.getSummary());
            response.put("totalRecords", extractionResponse.getTotalRecords());
            response.put("extractionMethod", "COUNTY_FIRST");
            response.put("countyFiltered", true);
            
            System.out.println("✅ CountyBasedPipelineController: COUNTY-FIRST report generated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error generating county-first report: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to generate county-first report: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Create report data from county extraction response
     */
    private ReportData createReportDataFromCountyExtraction(PipelineExtractionResponse extractionResponse, ReportGenerationRequest reportRequest) {
        ReportData reportData = new ReportData();
        reportData.setReportType(reportTypeProperties.getDefaultReportType());
        reportData.setUserRole(reportRequest.getUserRole());
        reportData.setGeneratedAt(java.time.LocalDateTime.now());

        List<MaskedTimesheetData> maskedData = extractionResponse.getData();
        List<Map<String, Object>> records = new java.util.ArrayList<>();

        // Convert masked data to records (field masking already applied by FieldMaskingService)
        System.out.println("🔍 Converting masked data to records for " + reportRequest.getUserRole());

        for (MaskedTimesheetData data : maskedData) {
            // Create a Map with only the fields from masked data (FieldMaskingService already filtered hidden fields)
            Map<String, Object> record = new HashMap<>();
            
            // Add only the fields from the masked data (FieldMaskingService already handled field visibility)
            record.putAll(data.getFields());
            
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

        reportData.setRecords(records);
        reportData.setTotalRecords(records.size());
        reportData.setFieldVisibility(extractionResponse.getSummary().getFieldVisibility());
        reportData.setStatusDistribution(extractionResponse.getSummary().getStatusDistribution());

        System.out.println("📊 Generated COUNTY-FIRST report with " + records.size() + " records for role: " + reportRequest.getUserRole());
        return reportData;
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
            response.put("extractionMethod", "COUNTY_FIRST");
            
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
     * Get pipeline status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getPipelineStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🔍 CountyBasedPipelineController: Getting COUNTY-FIRST pipeline status...");
            
            // Check if services are loaded
            boolean countyExtractionLoaded = countyBasedDataExtractionService != null;
            boolean fieldMaskingLoaded = fieldMaskingService != null;
            boolean reportGenerationLoaded = reportGenerationService != null;
            
            System.out.println("📊 Service status - CountyExtraction: " + countyExtractionLoaded + 
                             ", FieldMasking: " + fieldMaskingLoaded + 
                             ", ReportGeneration: " + reportGenerationLoaded);
            
            response.put("status", "SUCCESS");
            response.put("message", "County-first data pipeline is operational");
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("extractionMethod", "COUNTY_FIRST");
            response.put("services", Map.of(
                "countyExtraction", countyExtractionLoaded ? "UP" : "DOWN",
                "fieldMasking", fieldMaskingLoaded ? "UP" : "DOWN", 
                "reportGeneration", reportGenerationLoaded ? "UP" : "DOWN"
            ));
            response.put("serviceDetails", Map.of(
                "countyBasedDataExtractionService", countyBasedDataExtractionService != null ? "LOADED" : "NULL",
                "fieldMaskingService", fieldMaskingService != null ? "LOADED" : "NULL",
                "reportGenerationService", reportGenerationService != null ? "LOADED" : "NULL"
            ));
            
            System.out.println("✅ CountyBasedPipelineController: COUNTY-FIRST pipeline status retrieved");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting county-first pipeline status: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get county-first pipeline status: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
