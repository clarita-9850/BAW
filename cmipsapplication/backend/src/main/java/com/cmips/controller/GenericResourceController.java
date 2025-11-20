package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.service.KeycloakPolicyEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Generic Resource Controller - Demonstrates Low-Code Backend Architecture
 * 
 * This controller shows how to create a generic resource handler that works
 * for any resource type. All authorization decisions are made by Keycloak
 * policies, not by hardcoded logic in the backend.
 * 
 * This approach allows you to:
 * 1. Add new resources without changing backend code
 * 2. Modify authorization rules in Keycloak without code deployment
 * 3. Implement complex policies without backend complexity
 */
@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "http://localhost:3000")
public class GenericResourceController {

    private static final Logger logger = LoggerFactory.getLogger(GenericResourceController.class);

    @Autowired
    private KeycloakPolicyEvaluationService keycloakPolicyEvaluationService;

    /**
     * Generic CREATE endpoint - works for any resource type
     * Authorization is handled by Keycloak policies
     */
    @PostMapping("/{resourceType}")
    @RequirePermission(resource = "#{@resourceType} Resource", scope = "create", message = "You don't have permission to create this resource")
    public ResponseEntity<?> createResource(
            @PathVariable String resourceType,
            @RequestBody Map<String, Object> request) {
        
        logger.info("Creating {} resource: {}", resourceType, request);
        
        // Here you would implement generic business logic
        // For now, just return success
        return ResponseEntity.ok(Map.of(
            "message", "Resource created successfully",
            "resourceType", resourceType,
            "data", request
        ));
    }

    /**
     * Generic READ endpoint - works for any resource type
     */
    @GetMapping("/{resourceType}")
    @RequirePermission(resource = "#{@resourceType} Resource", scope = "read", message = "You don't have permission to read this resource")
    public ResponseEntity<?> getResources(@PathVariable String resourceType) {
        
        logger.info("Getting {} resources", resourceType);
        
        // Here you would implement generic business logic
        return ResponseEntity.ok(Map.of(
            "message", "Resources retrieved successfully",
            "resourceType", resourceType,
            "data", "[]" // Empty array for demo
        ));
    }

    /**
     * Generic UPDATE endpoint - works for any resource type
     */
    @PutMapping("/{resourceType}/{id}")
    @RequirePermission(resource = "#{@resourceType} Resource", scope = "update", message = "You don't have permission to update this resource")
    public ResponseEntity<?> updateResource(
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        
        logger.info("Updating {} resource with ID {}: {}", resourceType, id, request);
        
        // Here you would implement generic business logic
        return ResponseEntity.ok(Map.of(
            "message", "Resource updated successfully",
            "resourceType", resourceType,
            "id", id,
            "data", request
        ));
    }

    /**
     * Generic DELETE endpoint - works for any resource type
     */
    @DeleteMapping("/{resourceType}/{id}")
    @RequirePermission(resource = "#{@resourceType} Resource", scope = "delete", message = "You don't have permission to delete this resource")
    public ResponseEntity<?> deleteResource(
            @PathVariable String resourceType,
            @PathVariable String id) {
        
        logger.info("Deleting {} resource with ID {}", resourceType, id);
        
        // Here you would implement generic business logic
        return ResponseEntity.ok(Map.of(
            "message", "Resource deleted successfully",
            "resourceType", resourceType,
            "id", id
        ));
    }

    /**
     * Generic ACTION endpoint - works for any custom action
     * Example: POST /api/resources/timesheets/123/approve
     */
    @PostMapping("/{resourceType}/{id}/{action}")
    @RequirePermission(resource = "#{@resourceType} Resource", scope = "#{@action}", message = "You don't have permission to perform this action")
    public ResponseEntity<?> performAction(
            @PathVariable String resourceType,
            @PathVariable String id,
            @PathVariable String action,
            @RequestBody(required = false) Map<String, Object> request) {
        
        logger.info("Performing action {} on {} resource with ID {}: {}", action, resourceType, id, request);
        
        // Here you would implement generic business logic
        return ResponseEntity.ok(Map.of(
            "message", "Action performed successfully",
            "resourceType", resourceType,
            "id", id,
            "action", action,
            "data", request != null ? request : Map.of()
        ));
    }

    /**
     * Get user's granted scopes for a resource type
     * Useful for dynamic UI rendering
     */
    @GetMapping("/{resourceType}/scopes")
    @RequirePermission(resource = "#{@resourceType} Resource", scope = "read", message = "You don't have permission to read this resource")
    public ResponseEntity<?> getGrantedScopes(@PathVariable String resourceType) {
        
        logger.info("Getting granted scopes for {} resource", resourceType);
        
        var grantedScopes = keycloakPolicyEvaluationService.getGrantedScopes(resourceType + " Resource");
        
        return ResponseEntity.ok(Map.of(
            "resourceType", resourceType,
            "grantedScopes", grantedScopes
        ));
    }
}
