package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.entity.TimesheetEntity;
import com.example.kafkaeventdrivenapp.model.*;
import com.example.kafkaeventdrivenapp.repository.TimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * County-Based Data Extraction Service
 * Ensures data is fetched by location FIRST, then field masking is applied
 * Updated for CMIPS schema (location instead of county)
 */
@Service
public class CountyBasedDataExtractionService {

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private FieldMaskingService fieldMaskingService;

    @Autowired
    private EventService eventService;

    @Autowired
    private CountyMappingService countyMappingService;

    public CountyBasedDataExtractionService() {
        System.out.println("🔧 CountyBasedDataExtractionService: Constructor called - initializing...");
        try {
            System.out.println("✅ CountyBasedDataExtractionService: Constructor completed successfully");
        } catch (Exception e) {
            System.err.println("❌ CountyBasedDataExtractionService: Constructor failed with error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Extract data with LOCATION FIRST approach
     * 1. First: Query by location
     * 2. Then: Apply field masking
     * 3. Finally: Generate report
     */
    @Transactional(readOnly = true)
    public PipelineExtractionResponse extractDataByCounty(PipelineExtractionRequest request) {
        System.out.println("🔍 CountyBasedDataExtractionService: Starting LOCATION-FIRST data extraction for role: " + request.getUserRole());
        
        try {
            UserRole role = UserRole.from(request.getUserRole());
            // STEP 1: LOCATION BASED DATA FETCHING (PRIORITY)
            List<TimesheetEntity> locationFilteredData = fetchDataByLocation(role, request);
            System.out.println("📍 Location filtered records: " + locationFilteredData.size());

            // STEP 2: Apply additional filters (status, employee, date range)
            List<TimesheetEntity> filteredData = applyAdditionalFilters(locationFilteredData, request);
            System.out.println("🔍 Additional filters applied, records: " + filteredData.size());

            // STEP 3: Apply field masking based on user role
            List<MaskedTimesheetData> maskedData = applyFieldMasking(filteredData, request);
            System.out.println("🔒 Field masking applied to " + maskedData.size() + " records");

            // STEP 4: Generate extraction summary
            ExtractionSummary summary = generateExtractionSummary(filteredData, maskedData, request);

            // STEP 5: Publish extraction event
            publishExtractionEvent(role, request, summary);

            // STEP 6: Return response
            PipelineExtractionResponse response = new PipelineExtractionResponse();
            response.setExtractionId(UUID.randomUUID().toString());
            response.setUserRole(role.name());
            response.setReportType(request.getReportType());
            response.setExtractedAt(LocalDateTime.now());
            response.setTotalRecords(filteredData.size());
            response.setMaskedRecords(maskedData.size());
            response.setData(maskedData);
            response.setSummary(summary);
            response.setStatus("SUCCESS");

            System.out.println("✅ CountyBasedDataExtractionService: LOCATION-FIRST extraction completed successfully");
            return response;

        } catch (Exception e) {
            System.err.println("❌ Error in location-first data extraction: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse(UserRole.from(request.getUserRole()), request, e.getMessage());
        }
    }

    /**
     * STEP 1: Fetch data by location FIRST
     * This is the primary filter - everything else is secondary
     */
    private List<TimesheetEntity> fetchDataByLocation(UserRole role, PipelineExtractionRequest request) {
        List<TimesheetEntity> timesheets = new ArrayList<>();
        String userLocation = request.getUserCounty(); // Using county param as location filter

        System.out.println("📍 LOCATION-FIRST: Fetching data for role: " + role + ", location: " + userLocation);

        try {
            // Special handling for elevated roles when no specific location is requested
            // ADMIN and SYSTEM_SCHEDULER can access all data when no location is specified
            // SUPERVISOR requires county from JWT token - no full access
            if ((role == UserRole.ADMIN || role == UserRole.SYSTEM_SCHEDULER) && (userLocation == null || userLocation.isEmpty())) {
                System.out.println("📍 Elevated role: Fetching all records without location filtering");
                if (request.getDateRange() != null) {
                    timesheets = timesheetRepository.findByDateRange(
                        request.getDateRange().getStartDate(),
                        request.getDateRange().getEndDate()
                    );
                } else {
                    timesheets = timesheetRepository.findAll();
                }
                System.out.println("📍 Elevated role total records: " + timesheets.size());
                return timesheets;
            }
            
            // Determine location access based on user role
            List<String> accessibleLocations = getAccessibleLocationsForRole(role, request);
            System.out.println("📍 Accessible locations for " + role + ": " + accessibleLocations);

            // Fetch data from accessible locations
            for (String location : accessibleLocations) {
                List<TimesheetEntity> locationData;
                String mappedLocation = countyMappingService.normalizeCountyName(location);
                
                if (mappedLocation == null) {
                    System.err.println("❌ Warning: Could not map location " + location + " to database format");
                    continue;
                }
                
                if (request.getDateRange() != null) {
                    locationData = timesheetRepository.findByDateRangeAndLocation(
                        request.getDateRange().getStartDate(),
                        request.getDateRange().getEndDate(),
                        mappedLocation
                    );
                } else {
                    locationData = timesheetRepository.findByLocationOrderByCreatedAtDesc(mappedLocation);
                }
                
                timesheets.addAll(locationData);
                System.out.println("📍 Location " + location + " (mapped to " + mappedLocation + ") records: " + locationData.size());
            }

            System.out.println("📍 Total location-filtered records: " + timesheets.size());
            return timesheets;

        } catch (Exception e) {
            System.err.println("❌ Error fetching data by location: " + e.getMessage());
            throw new RuntimeException("Failed to fetch data by location: " + e.getMessage());
        }
    }

    /**
     * STEP 2: Apply additional filters after location filtering
     */
    private List<TimesheetEntity> applyAdditionalFilters(List<TimesheetEntity> locationData, PipelineExtractionRequest request) {
        List<TimesheetEntity> filteredData = new ArrayList<>(locationData);

        // Apply status filter if provided
        if (request.getStatusFilter() != null && !request.getStatusFilter().isEmpty()) {
            filteredData = filteredData.stream()
                .filter(ts -> request.getStatusFilter().contains(ts.getStatus()))
                .collect(Collectors.toList());
            System.out.println("🔍 Status filter applied, records: " + filteredData.size());
        }

        // Apply employee filter if provided (using employeeId instead of providerId)
        if (request.getProviderFilter() != null && !request.getProviderFilter().isEmpty()) {
            filteredData = filteredData.stream()
                .filter(ts -> request.getProviderFilter().contains(ts.getEmployeeId()))
                .collect(Collectors.toList());
            System.out.println("🔍 Employee filter applied, records: " + filteredData.size());
        }

        // Apply department filter if provided (using department instead of serviceType)
        if (request.getProjectFilter() != null && !request.getProjectFilter().isEmpty()) {
            filteredData = filteredData.stream()
                .filter(ts -> request.getProjectFilter().contains(ts.getDepartment()))
                .collect(Collectors.toList());
            System.out.println("🔍 Department filter applied, records: " + filteredData.size());
        }

        return filteredData;
    }

    /**
     * STEP 3: Apply field masking based on user role and report type
     */
    private List<MaskedTimesheetData> applyFieldMasking(List<TimesheetEntity> rawData, PipelineExtractionRequest request) {
        List<MaskedTimesheetData> maskedData = new ArrayList<>();

        for (TimesheetEntity timesheet : rawData) {
            try {
                // Get field masking rules for this user role and report type
                // Note: JWT token may not be available in request - using empty string as fallback
                FieldMaskingRules rules = fieldMaskingService.getMaskingRules(
                    request.getUserRole(), 
                    request.getReportType(),
                    "" // JWT token not available in PipelineExtractionRequest
                );

                // Apply masking to timesheet data
                MaskedTimesheetData maskedTimesheet = fieldMaskingService.applyMasking(timesheet, rules);
                maskedData.add(maskedTimesheet);

            } catch (Exception e) {
                System.err.println("❌ Error applying masking to timesheet " + timesheet.getId() + ": " + e.getMessage());
                // Continue with next record
            }
        }

        System.out.println("🔒 Field masking applied to " + maskedData.size() + " records");
        return maskedData;
    }

    /**
     * Generate extraction summary
     */
    private ExtractionSummary generateExtractionSummary(List<TimesheetEntity> rawData, List<MaskedTimesheetData> maskedData, PipelineExtractionRequest request) {
        ExtractionSummary summary = new ExtractionSummary();
        summary.setTotalRecords(rawData.size());
        summary.setMaskedRecords(maskedData.size());
        summary.setUserRole(request.getUserRole());
        summary.setReportType(request.getReportType());
        summary.setExtractionTimestamp(LocalDateTime.now());

        // Calculate field visibility statistics
        Map<String, Integer> fieldVisibility = new HashMap<>();
        for (MaskedTimesheetData data : maskedData) {
            for (Map.Entry<String, Object> entry : data.getFields().entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                
                if (value != null && !value.toString().contains("***")) {
                    fieldVisibility.put(fieldName, fieldVisibility.getOrDefault(fieldName, 0) + 1);
                }
            }
        }
        summary.setFieldVisibility(fieldVisibility);

        // Calculate status distribution
        Map<String, Long> statusDistribution = rawData.stream()
            .collect(Collectors.groupingBy(TimesheetEntity::getStatus, Collectors.counting()));
        summary.setStatusDistribution(statusDistribution);

        // Calculate location distribution (using location instead of providerCounty)
        Map<String, Long> locationDistribution = rawData.stream()
            .collect(Collectors.groupingBy(TimesheetEntity::getLocation, Collectors.counting()));
        summary.setCountyDistribution(locationDistribution); // Keeping same field name for compatibility

        System.out.println("📊 Extraction summary generated with location distribution");
        return summary;
    }

    /**
     * Publish extraction event to the unified event log for observability.
     */
    private void publishExtractionEvent(UserRole role, PipelineExtractionRequest request, ExtractionSummary summary) {
        try {
            DataExtractionEvent event = new DataExtractionEvent();
            event.setEventType("LOCATION_BASED_DATA_EXTRACTION_COMPLETED");
            event.setUserRole(role.name());
            event.setReportType(request.getReportType());
            event.setTotalRecords(summary.getTotalRecords());
            event.setMaskedRecords(summary.getMaskedRecords());
            event.setTimestamp(LocalDateTime.now());
            event.setSummary(summary);

            eventService.publishEvent("location-based-extraction-events", event);
            System.out.println("📡 Location-based extraction event recorded in event log");

        } catch (Exception e) {
            System.err.println("❌ Error publishing extraction event: " + e.getMessage());
        }
    }

    /**
     * Create error response
     */
    private PipelineExtractionResponse createErrorResponse(UserRole role, PipelineExtractionRequest request, String errorMessage) {
        PipelineExtractionResponse response = new PipelineExtractionResponse();
        response.setExtractionId(UUID.randomUUID().toString());
        response.setUserRole(role.name());
        response.setReportType(request.getReportType());
        response.setExtractedAt(LocalDateTime.now());
        response.setTotalRecords(0);
        response.setMaskedRecords(0);
        response.setData(new ArrayList<>());
        response.setStatus("ERROR");
        response.setErrorMessage(errorMessage);
        return response;
    }
    
    private List<String> getAccessibleLocationsForRole(UserRole role, PipelineExtractionRequest request) {
        List<String> allowed = request.getAllowedCounties();
        if (allowed != null && !allowed.isEmpty()) {
            return countyMappingService.normalizeCountyList(allowed);
        }

        String userLocation = countyMappingService.normalizeCountyName(request.getUserCounty());
        return switch (role) {
            case ADMIN, SYSTEM_SCHEDULER -> countyMappingService.getAllCounties();
            // SUPERVISOR requires county from JWT token - no full access, same as CASE_WORKER/PROVIDER/RECIPIENT
            case CASE_WORKER, PROVIDER, RECIPIENT, SUPERVISOR -> userLocation != null ? List.of(userLocation) : Collections.emptyList();
            default -> Collections.emptyList();
        };
    }
}
