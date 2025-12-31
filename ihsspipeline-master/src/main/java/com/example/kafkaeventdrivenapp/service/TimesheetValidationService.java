package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.ValidationRequest;
import com.example.kafkaeventdrivenapp.model.ValidationResponse;
import com.example.kafkaeventdrivenapp.model.Timesheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TimesheetValidationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${EXTERNAL_VALIDATION_URL}")
    private String externalValidationUrl;
    
    private static final double MAX_HOURS_THRESHOLD = 40.0;

    /**
     * Validate timesheet using external 3rd party API
     */
    public ValidationResponse validateTimesheet(Timesheet timesheet) {
        System.out.println("🔍 TimesheetValidationService: Starting validation for timesheet " + timesheet.getTimesheetId());
        
        // Create validation request
        ValidationRequest request = createValidationRequest(timesheet);
        
        try {
            // Call external validation API
            System.out.println("📡 Calling external validation API at: " + externalValidationUrl);
            ValidationResponse response = restTemplate.postForObject(externalValidationUrl, request, ValidationResponse.class);
            
            if (response != null) {
                System.out.println("✅ External validation completed: " + response.getValidationResult());
                return response;
            } else {
                return createErrorResponse(timesheet.getTimesheetId(), "External validation service returned null response");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error calling external validation API: " + e.getMessage());
            return createErrorResponse(timesheet.getTimesheetId(), "External validation service unavailable: " + e.getMessage());
        }
    }

    /**
     * Create validation request from timesheet
     */
    private ValidationRequest createValidationRequest(Timesheet timesheet) {
        ValidationRequest request = new ValidationRequest();
        request.setTimesheetId(timesheet.getTimesheetId());
        request.setProviderId(timesheet.getProviderId());
        request.setRecipientId(timesheet.getRecipientId());
        request.setTotalHours(timesheet.getTotalHours());
        request.setWeekEnding(timesheet.getWeekEnding());
        request.setEntries(timesheet.getDailyHours());
        request.setDescription(timesheet.getDescription());
        request.setStatus(timesheet.getStatus());
        
        return request;
    }

    /**
     * Create error response when external validation fails
     */
    private ValidationResponse createErrorResponse(String timesheetId, String errorMessage) {
        ValidationResponse response = new ValidationResponse();
        response.setTimesheetId(timesheetId);
        response.setValidationResult("ERROR");
        response.setReason("External validation service error");
        response.setMessage(errorMessage);
        response.setValidatedAt(LocalDateTime.now());
        response.setValidatorId("SYSTEM_ERROR");
        response.setRequiresRevision(true);
        
        return response;
    }

    /**
     * Check if timesheet requires revision based on validation response
     */
    public boolean requiresRevision(ValidationResponse response) {
        return "REJECTED".equals(response.getValidationResult()) || 
               "ERROR".equals(response.getValidationResult());
    }

    /**
     * Check if timesheet is approved for further review
     */
    public boolean isApprovedForReview(ValidationResponse response) {
        return "APPROVED".equals(response.getValidationResult());
    }

    /**
     * Get next status based on validation response
     */
    public String getNextStatus(ValidationResponse response) {
        if (isApprovedForReview(response)) {
            return "PENDING_REVIEW";
        } else if (requiresRevision(response)) {
            return "REVISION_REQUIRED";
        } else {
            return "VALIDATION_PENDING";
        }
    }
}
