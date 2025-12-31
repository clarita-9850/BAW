package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.entity.CaseEntity;
import com.example.kafkaeventdrivenapp.model.CaseCreationRequest;
import com.example.kafkaeventdrivenapp.model.CaseCreationResponse;
import com.example.kafkaeventdrivenapp.model.SsnVerificationRequest;
import com.example.kafkaeventdrivenapp.model.SsnVerificationResponse;
import com.example.kafkaeventdrivenapp.service.CaseService;
import com.example.kafkaeventdrivenapp.service.SsnVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/case")
@CrossOrigin(origins = "*")
public class CaseController {
    
    @Autowired
    private CaseService caseService;
    
    @Autowired
    private SsnVerificationService ssnVerificationService;
    
    /**
     * Create new recipient case
     * Requires CASE_WORKER role
     */
    @PostMapping("/create")
    public ResponseEntity<CaseCreationResponse> createCase(@RequestBody CaseCreationRequest request) {
        try {
            System.out.println("➕ CaseController: Case creation request received");
            
            String caseworkerId = getCurrentUsername();
            
            CaseCreationResponse response = caseService.createCase(request, caseworkerId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (IllegalArgumentException e) {
            System.err.println("❌ CaseController: Validation error: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(CaseCreationResponse.failure(e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ CaseController: Error creating case: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(CaseCreationResponse.failure("Error creating case: " + e.getMessage()));
        }
    }
    
    /**
     * Verify SSN + first name with external service
     */
    @PostMapping("/verify-ssn")
    public ResponseEntity<SsnVerificationResponse> verifySsn(@RequestBody SsnVerificationRequest request) {
        if (request.getSsn() == null || request.getFirstName() == null) {
            SsnVerificationResponse response = new SsnVerificationResponse();
            response.setValid(false);
            response.setStatus("FAILED");
            response.setMessage("SSN and first name are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        SsnVerificationResponse response = ssnVerificationService.verifySsn(request);
        if (response == null) {
            SsnVerificationResponse fallback = new SsnVerificationResponse();
            fallback.setValid(false);
            fallback.setStatus("ERROR");
            fallback.setMessage("SSN verification service unavailable");
            return ResponseEntity.internalServerError().body(fallback);
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get case by ID
     * Requires CASE_WORKER role
     */
    @GetMapping("/{id}")
    public ResponseEntity<CaseEntity> getCaseById(@PathVariable Long id) {
        try {
            CaseEntity caseEntity = caseService.getCaseById(id);
            return ResponseEntity.ok(caseEntity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ CaseController: Error getting case: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get cases for a person
     * Requires CASE_WORKER role
     */
    @GetMapping("/person/{personId}")
    public ResponseEntity<List<CaseEntity>> getCasesByPerson(@PathVariable Long personId) {
        try {
            List<CaseEntity> cases = caseService.getCasesByPersonId(personId);
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            System.err.println("❌ CaseController: Error getting cases for person: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get cases assigned to current caseworker
     * Requires CASE_WORKER role
     */
    @GetMapping("/my-cases")
    public ResponseEntity<List<CaseEntity>> getMyCases() {
        try {
            String caseworkerId = getCurrentUsername();
            List<CaseEntity> cases = caseService.getCasesByCaseworker(caseworkerId);
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            System.err.println("❌ CaseController: Error getting my cases: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get active cases for current caseworker
     * Requires CASE_WORKER role
     */
    @GetMapping("/my-cases/active")
    public ResponseEntity<List<CaseEntity>> getMyActiveCases() {
        try {
            String caseworkerId = getCurrentUsername();
            List<CaseEntity> cases = caseService.getActiveCasesByCaseworker(caseworkerId);
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            System.err.println("❌ CaseController: Error getting active cases: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Activate a case
     * Requires CASE_WORKER role
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<CaseEntity> activateCase(@PathVariable Long id) {
        try {
            CaseEntity caseEntity = caseService.activateCase(id);
            return ResponseEntity.ok(caseEntity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ CaseController: Error activating case: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Close a case
     * Requires CASE_WORKER role
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<CaseEntity> closeCase(@PathVariable Long id) {
        try {
            CaseEntity caseEntity = caseService.closeCase(id);
            return ResponseEntity.ok(caseEntity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ CaseController: Error closing case: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get current username from security context
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}

