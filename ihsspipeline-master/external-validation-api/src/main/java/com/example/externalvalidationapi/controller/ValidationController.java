package com.example.externalvalidationapi.controller;

import com.example.externalvalidationapi.model.SsnValidationRequest;
import com.example.externalvalidationapi.model.SsnValidationResponse;
import com.example.externalvalidationapi.model.ValidationRequest;
import com.example.externalvalidationapi.model.ValidationResponse;
import com.example.externalvalidationapi.service.SsnDatasetService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/api/validation")
@CrossOrigin(origins = "*")
public class ValidationController {

    private final Random random = new Random();
    private static final double MAX_HOURS_THRESHOLD = 40.0;
    private final SsnDatasetService ssnDatasetService;

    public ValidationController(SsnDatasetService ssnDatasetService) {
        this.ssnDatasetService = ssnDatasetService;
    }

    /**
     * 3rd Party API endpoint for timesheet validation
     * Business Rule: If total hours <= 40, APPROVE; If > 40, REJECT
     */
    @PostMapping("/validate")
    public ValidationResponse validateTimesheet(@RequestBody ValidationRequest request) {
        System.out.println("🔍 External Validation API: Processing timesheet " + request.getTimesheetId());
        System.out.println("📊 Total Hours: " + request.getTotalHours());
        System.out.println("👤 Provider: " + request.getProviderId());
        System.out.println("📅 Week Ending: " + request.getWeekEnding());
        
        // Simulate some processing time (1-3 seconds)
        try {
            Thread.sleep(1000 + random.nextInt(2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ValidationResponse response = new ValidationResponse();
        response.setTimesheetId(request.getTimesheetId());
        response.setTotalHours(request.getTotalHours());
        response.setValidatedAt(LocalDateTime.now());
        response.setValidatorId("EXTERNAL_VALIDATOR_" + (1000 + random.nextInt(9000)));

        // Business Logic: Hours validation
        if (request.getTotalHours() <= MAX_HOURS_THRESHOLD) {
            response.setValidationResult("APPROVED");
            response.setReason("Hours within acceptable limit");
            response.setMessage("Timesheet approved for further review. Total hours: " + request.getTotalHours() + 
                              " (within " + MAX_HOURS_THRESHOLD + " hour limit)");
            response.setRequiresRevision(false);
            System.out.println("✅ External Validation: APPROVED - Hours within limit");
        } else {
            response.setValidationResult("REJECTED");
            response.setReason("Hours exceed maximum threshold of " + MAX_HOURS_THRESHOLD + " hours");
            response.setMessage("Timesheet rejected. Please revise hours to " + MAX_HOURS_THRESHOLD + 
                              " or less for approval. Current hours: " + request.getTotalHours());
            response.setRequiresRevision(true);
            System.out.println("❌ External Validation: REJECTED - Hours exceed limit");
        }

        System.out.println("📤 External Validation API: Response sent for " + request.getTimesheetId());
        return response;
    }

    /**
     * Health check endpoint for the external validation service
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "External Validation Service is running and healthy";
    }

    /**
     * Get validation rules endpoint
     */
    @GetMapping("/rules")
    public String getValidationRules() {
        return "Validation Rules: Maximum hours allowed = " + MAX_HOURS_THRESHOLD + " hours per week";
    }

    /**
     * Get service status endpoint
     */
    @GetMapping("/status")
    public String getServiceStatus() {
        return "External Validation API Service - Status: ACTIVE, Version: 1.0.0";
    }

    /**
     * Simulate service downtime (for testing resilience)
     */
    @PostMapping("/simulate-downtime")
    public String simulateDowntime() {
        System.out.println("⚠️ External Validation API: Simulating downtime...");
        return "Service downtime simulation activated";
    }

    /**
     * SSN + First Name verification mock endpoint
     */
    @PostMapping("/ssn")
    public SsnValidationResponse verifySsn(@RequestBody SsnValidationRequest request) {
        System.out.println("🔐 External Validation API: Verifying SSN " + request.getSsn() +
            " for first name " + request.getFirstName());

        SsnValidationResponse response = new SsnValidationResponse();
        response.setMatchedSsn(request.getSsn());

        if (request.getSsn() == null || request.getFirstName() == null) {
            response.setValid(false);
            response.setStatus("FAILED");
            response.setMessage("SSN and first name are required");
            return response;
        }

        String canonicalSsn = request.getSsn().trim();
        String canonicalFirstName = request.getFirstName().trim();
        String mappedFirstName = ssnDatasetService.getFirstNameForSsn(canonicalSsn);

        if (mappedFirstName != null && mappedFirstName.equalsIgnoreCase(canonicalFirstName)) {
            response.setValid(true);
            response.setStatus("VERIFIED");
            response.setMessage("SSN verification successful");
            response.setMatchedFirstName(mappedFirstName);
        } else if (mappedFirstName != null) {
            response.setValid(false);
            response.setStatus("FAILED");
            response.setMessage("First name does not match records for this SSN");
            response.setMatchedFirstName(mappedFirstName);
        } else {
            response.setValid(false);
            response.setStatus("NOT_FOUND");
            response.setMessage("SSN not found in verification records");
        }

        return response;
    }
}
