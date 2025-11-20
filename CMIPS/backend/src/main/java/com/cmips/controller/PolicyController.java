package com.cmips.controller;

import com.cmips.dto.PolicyRequest;
import com.cmips.entity.Policy;
import com.cmips.service.PolicyEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/policies")
@CrossOrigin(origins = "*")
public class PolicyController {
    
    @Autowired
    private PolicyEngineService policyEngineService;
    
    /**
     * Get all policies
     * GET /api/policies
     */
    @GetMapping
    public ResponseEntity<List<Policy>> getAllPolicies(HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<Policy> policies = policyEngineService.getAllPolicies();
            return ResponseEntity.ok(policies);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get policy by ID
     * GET /api/policies/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Policy> getPolicy(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Optional<Policy> policy = policyEngineService.getPolicyById(id);
            if (policy.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(policy.get());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Create new policy
     * POST /api/policies
     */
    @PostMapping
    public ResponseEntity<Policy> createPolicy(@Valid @RequestBody PolicyRequest request, 
                                             HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Policy policy = new Policy();
            policy.setRole(request.getRole());
            policy.setResource(request.getResource());
            policy.setAction(request.getAction());
            policy.setAllowed(request.getAllowed());
            policy.setDescription(request.getDescription());
            policy.setPriority(request.getPriority());
            policy.setActive(request.getActive());
            
            Policy createdPolicy = policyEngineService.createPolicy(policy);
            return ResponseEntity.ok(createdPolicy);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update policy
     * PUT /api/policies/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Policy> updatePolicy(@PathVariable Long id, 
                                             @Valid @RequestBody PolicyRequest request,
                                             HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Optional<Policy> existingPolicy = policyEngineService.getPolicyById(id);
            if (existingPolicy.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Policy policy = existingPolicy.get();
            policy.setRole(request.getRole());
            policy.setResource(request.getResource());
            policy.setAction(request.getAction());
            policy.setAllowed(request.getAllowed());
            policy.setDescription(request.getDescription());
            policy.setPriority(request.getPriority());
            policy.setActive(request.getActive());
            
            Policy updatedPolicy = policyEngineService.updatePolicy(policy);
            return ResponseEntity.ok(updatedPolicy);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Delete policy
     * DELETE /api/policies/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            Optional<Policy> policy = policyEngineService.getPolicyById(id);
            if (policy.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            policyEngineService.deletePolicy(id);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get policies by role
     * GET /api/policies/role/{role}
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<Policy>> getPoliciesByRole(@PathVariable String role, 
                                                         HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String userRole = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<Policy> policies = policyEngineService.getPoliciesByRole(role);
            return ResponseEntity.ok(policies);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get policies by resource
     * GET /api/policies/resource/{resource}
     */
    @GetMapping("/resource/{resource}")
    public ResponseEntity<List<Policy>> getPoliciesByResource(@PathVariable String resource, 
                                                            HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<Policy> policies = policyEngineService.getPoliciesByResource(resource);
            return ResponseEntity.ok(policies);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Test policy access
     * GET /api/policies/test?role={role}&resource={resource}&action={action}
     */
    @GetMapping("/test")
    public ResponseEntity<Boolean> testPolicyAccess(@RequestParam String role, 
                                                  @RequestParam String resource, 
                                                  @RequestParam String action,
                                                  HttpServletRequest httpRequest) {
        try {
            // Check if user has admin privileges
            String userRole = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            boolean allowed = policyEngineService.isAllowed(role, resource, action);
            return ResponseEntity.ok(allowed);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
