package com.cmips.controller;

import com.cmips.dto.TimesheetCreateRequest;
import com.cmips.dto.TimesheetResponse;
import com.cmips.dto.TimesheetUpdateRequest;
import com.cmips.service.FieldLevelAuthorizationService;
import com.cmips.service.KeycloakAuthorizationService;
import com.cmips.service.TimesheetService;
import com.cmips.annotation.RequirePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/timesheets")
@CrossOrigin(origins = "http://localhost:3000")
public class TimesheetController {

    private static final Logger logger = LoggerFactory.getLogger(TimesheetController.class);

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private KeycloakAuthorizationService keycloakAuthzService;

    @Autowired
    private FieldLevelAuthorizationService fieldLevelAuthzService;

    @PostMapping
    @RequirePermission(resource = "Timesheet Resource", scope = "create", message = "You don't have permission to create timesheets")
    public ResponseEntity<?> createTimesheet(@RequestBody Map<String, Object> requestData, HttpServletRequest httpRequest) {
        try {
            String userId = getCurrentUserId();
            
            // Apply field-level filtering for create access
            logger.info("Applying field-level filtering for create access to create timesheet");
            
            // Filter request fields based on Keycloak resource attributes with CREATE scope
            Map<String, Object> filteredRequestMap = fieldLevelAuthzService.filterFields(
                requestData, "Timesheet Resource", "create");
            
            logger.info("Create field filtering applied. Original: {} fields, Filtered: {} fields", 
                requestData.size(), filteredRequestMap.size());
            
            // Convert filtered Map to TimesheetCreateRequest
            TimesheetCreateRequest filteredRequest = convertMapToTimesheetCreateRequest(filteredRequestMap);
            
            TimesheetResponse response = timesheetService.createTimesheet(userId, filteredRequest);

            // Apply read filtering to the response
            Map<String, Object> responseMap = convertTimesheetResponseToMap(response);
            Map<String, Object> filteredResponseMap = fieldLevelAuthzService.filterFields(
                responseMap, "Timesheet Resource", "read");
            
            logger.info("Timesheet created successfully with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(filteredResponseMap);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating timesheet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"error\":\"Validation error\",\"message\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error creating timesheet", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error\",\"message\":\"Failed to create timesheet\"}");
        }
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitTimesheet(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            String userId = getCurrentUserId();
            Set<String> roles = getCurrentUserRoles();

            if (!keycloakAuthzService.hasRole(roles, "PROVIDER")) {
                logger.warn("Access denied for timesheet submission for user {} with roles {}", userId, roles);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\":\"Access denied\",\"message\":\"You don't have permission to submit timesheets\"}");
            }

            return timesheetService.submitTimesheet(id, userId)
                .map(response -> {
                    logger.info("Timesheet {} submitted successfully", id);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            logger.error("Error submitting timesheet {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error\",\"message\":\"Failed to submit timesheet\"}");
        }
    }

    @PostMapping("/{id}/approve")
    @RequirePermission(resource = "Timesheet Resource", scope = "approve", message = "You don't have permission to approve timesheets")
    public ResponseEntity<?> approveTimesheet(@PathVariable Long id,
                                             @RequestBody(required = false) Map<String, Object> requestData,
                                             HttpServletRequest httpRequest) {
        try {
            String userId = getCurrentUserId();
            
            // Apply field-level filtering for approve scope
            logger.info("Applying field-level filtering for approve scope on timesheet {}", id);
            
            // Filter request fields based on Keycloak resource attributes with APPROVE scope
            Map<String, Object> filteredRequestMap = new HashMap<>();
            if (requestData != null && !requestData.isEmpty()) {
                filteredRequestMap = fieldLevelAuthzService.filterFields(
                    requestData, "Timesheet Resource", "approve");
                
                logger.info("Approve field filtering applied. Original: {} fields, Filtered: {} fields", 
                    requestData.size(), filteredRequestMap.size());
            }
            
            // Extract comments if present in filtered data
            String comments = filteredRequestMap.containsKey("supervisorComments") 
                ? filteredRequestMap.get("supervisorComments").toString() 
                : null;

            Optional<TimesheetResponse> approvedTimesheetOpt = timesheetService.approveTimesheet(id, userId);
            if (!approvedTimesheetOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"Timesheet not found\"}");
            }
            TimesheetResponse approvedTimesheet = approvedTimesheetOpt.get();
            
            // Apply read filtering to the response
            Map<String, Object> responseMap = convertTimesheetResponseToMap(approvedTimesheet);
            Map<String, Object> filteredResponseMap = fieldLevelAuthzService.filterFields(
                responseMap, "Timesheet Resource", "read");
            
            logger.info("Timesheet {} approved successfully by user {}", id, userId);
            return ResponseEntity.ok(filteredResponseMap);

        } catch (Exception e) {
            logger.error("Error approving timesheet {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error\",\"message\":\"Failed to approve timesheet\"}");
        }
    }

    @PostMapping("/{id}/reject")
    @RequirePermission(resource = "Timesheet Resource", scope = "reject", message = "You don't have permission to reject timesheets")
    public ResponseEntity<?> rejectTimesheet(@PathVariable Long id,
                                           @RequestBody(required = false) Map<String, Object> requestData,
                                           HttpServletRequest httpRequest) {
        try {
            String userId = getCurrentUserId();
            
            // Apply field-level filtering for reject scope
            logger.info("Applying field-level filtering for reject scope on timesheet {}", id);
            
            // Filter request fields based on Keycloak resource attributes with REJECT scope
            Map<String, Object> filteredRequestMap = new HashMap<>();
            if (requestData != null && !requestData.isEmpty()) {
                filteredRequestMap = fieldLevelAuthzService.filterFields(
                    requestData, "Timesheet Resource", "reject");
                
                logger.info("Reject field filtering applied. Original: {} fields, Filtered: {} fields", 
                    requestData.size(), filteredRequestMap.size());
            }
            
            // Extract comments if present in filtered data
            String comments = filteredRequestMap.containsKey("supervisorComments") 
                ? filteredRequestMap.get("supervisorComments").toString() 
                : null;

            Optional<TimesheetResponse> rejectedTimesheetOpt = timesheetService.rejectTimesheet(id, userId, comments);
            if (!rejectedTimesheetOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"Timesheet not found\"}");
            }
            TimesheetResponse rejectedTimesheet = rejectedTimesheetOpt.get();
            
            // Apply read filtering to the response
            Map<String, Object> responseMap = convertTimesheetResponseToMap(rejectedTimesheet);
            Map<String, Object> filteredResponseMap = fieldLevelAuthzService.filterFields(
                responseMap, "Timesheet Resource", "read");
            
            logger.info("Timesheet {} rejected successfully by user {}", id, userId);
            return ResponseEntity.ok(filteredResponseMap);

        } catch (Exception e) {
            logger.error("Error rejecting timesheet {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error\",\"message\":\"Failed to reject timesheets\"}");
        }
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resource = "Timesheet Resource", scope = "delete", message = "You don't have permission to delete timesheets")
    public ResponseEntity<?> deleteTimesheet(@PathVariable Long id,
                                           @RequestBody(required = false) Map<String, Object> requestData,
                                           HttpServletRequest httpRequest) {
        try {
            String userId = getCurrentUserId();
            
            // Apply field-level filtering for delete access if request data is provided
            if (requestData != null && !requestData.isEmpty()) {
                logger.info("Applying field-level filtering for delete access to timesheet {}", id);
                
                // Filter request fields based on Keycloak resource attributes with DELETE scope
                Map<String, Object> filteredRequestMap = fieldLevelAuthzService.filterFields(
                    requestData, "Timesheet Resource", "delete");
                
                logger.info("Delete field filtering applied. Original: {} fields, Filtered: {} fields", 
                    requestData.size(), filteredRequestMap.size());
            }
            
            boolean deleted = timesheetService.deleteTimesheet(id, userId);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"Timesheet not found or you don't have permission to delete it\"}");
            }
            
            logger.info("Timesheet {} deleted successfully by user {}", id, userId);
            return ResponseEntity.ok("{\"message\":\"Timesheet deleted successfully\"}");
            
        } catch (Exception e) {
            logger.error("Error deleting timesheet {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error\",\"message\":\"Failed to delete timesheet\"}");
        }
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit", message = "You don't have permission to update timesheets")
    public ResponseEntity<?> updateTimesheet(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        try {
            String userId = getCurrentUserId();
            
            // Apply field-level filtering for edit access
            logger.info("Applying field-level filtering for edit access to timesheet {}", id);
            
            // Filter request fields based on Keycloak resource attributes with EDIT scope
            Map<String, Object> filteredRequestMap = fieldLevelAuthzService.filterFields(
                requestData, "Timesheet Resource", "edit");
            
            logger.info("Edit field filtering applied. Original: {} fields, Filtered: {} fields", 
                requestData.size(), filteredRequestMap.size());
            
            // Convert filtered Map to TimesheetUpdateRequest
            TimesheetUpdateRequest filteredRequest = convertMapToTimesheetUpdateRequest(filteredRequestMap);
            
            Optional<TimesheetResponse> updatedTimesheetOpt = timesheetService.updateTimesheet(id, filteredRequest);
            if (!updatedTimesheetOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"Timesheet not found\"}");
            }
            TimesheetResponse updatedTimesheet = updatedTimesheetOpt.get();
            
            // Apply read filtering to the response
            Map<String, Object> responseMap = convertTimesheetResponseToMap(updatedTimesheet);
            Map<String, Object> filteredResponseMap = fieldLevelAuthzService.filterFields(
                responseMap, "Timesheet Resource", "read");
            
            return ResponseEntity.ok(filteredResponseMap);
            
        } catch (Exception e) {
            logger.error("Error updating timesheet", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error\",\"message\":\"Failed to update timesheet\"}");
        }
    }

    @GetMapping
    @RequirePermission(resource = "Timesheet Resource", scope = "read", message = "You don't have permission to read timesheets")
    public ResponseEntity<?> getTimesheets(Pageable pageable, HttpServletRequest httpRequest) {
        try {
            String userId = getCurrentUserId();
            Set<String> roles = getCurrentUserRoles();
            Page<TimesheetResponse> timesheets;

            if (keycloakAuthzService.hasRole(roles, "PROVIDER")) {
                timesheets = timesheetService.getTimesheetsByUserId(userId, pageable);
                logger.info("Provider {} requested their timesheets. Found {} timesheets.", userId, timesheets.getTotalElements());
            } else if (keycloakAuthzService.hasRole(roles, "RECIPIENT")) {
                // Recipients should see timesheets submitted to them (or all submitted for now)
                timesheets = timesheetService.getSubmittedTimesheets(pageable);
                logger.info("Recipient {} requested submitted timesheets. Found {} timesheets.", userId, timesheets.getTotalElements());
            } else if (keycloakAuthzService.hasRole(roles, "CASE_WORKER") || keycloakAuthzService.hasRole(roles, "SUPERVISOR")) {
                timesheets = timesheetService.getAllTimesheets(pageable);
                logger.info("Case Worker/Supervisor {} requested all timesheets. Found {} timesheets.", userId, timesheets.getTotalElements());
            } else {
                logger.warn("Access denied for timesheet read for user {} with roles {}", userId, roles);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\":\"Access denied\",\"message\":\"You don't have permission to read timesheets\"}");
            }

            // Apply field-level filtering based on user's scopes
            logger.info("Applying field-level filtering to {} timesheets", timesheets.getContent().size());
            
            // Convert TimesheetResponse objects to Maps for filtering
            List<Map<String, Object>> timesheetMaps = timesheets.getContent().stream()
                .map(this::convertTimesheetResponseToMap)
                .collect(java.util.stream.Collectors.toList());
            
            // Filter fields based on Keycloak resource attributes with READ scope
            List<Map<String, Object>> filteredTimesheetMaps = fieldLevelAuthzService.filterFields(
                timesheetMaps, "Timesheet Resource", "read"); // Using "read" scope for GET requests
            
            logger.info("Field filtering applied. Original: {} items, Filtered: {} items", 
                timesheetMaps.size(), filteredTimesheetMaps.size());
            
            // Get allowed actions for the current user
            Set<String> allowedActions = fieldLevelAuthzService.getAllowedActions("Timesheet Resource");
            logger.info("Allowed actions for user: {}", allowedActions);
            
            // Return filtered data with allowed actions
            Map<String, Object> response = new HashMap<>();
            response.put("content", filteredTimesheetMaps);
            response.put("totalElements", timesheets.getTotalElements());
            response.put("totalPages", timesheets.getTotalPages());
            response.put("size", timesheets.getSize());
            response.put("number", timesheets.getNumber());
            response.put("first", timesheets.isFirst());
            response.put("last", timesheets.isLast());
            response.put("numberOfElements", filteredTimesheetMaps.size());
            response.put("allowedActions", allowedActions); // Include allowed actions in response
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting timesheets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error\",\"message\":\"Failed to get timesheets\"}");
        }
    }
    
    @GetMapping("/actions")
    @RequirePermission(resource = "Timesheet Resource", scope = "read", message = "You don't have permission to read timesheets")
    public ResponseEntity<?> getAllowedActions() {
        try {
            // Get allowed actions for the current user from Keycloak attributes
            Set<String> allowedActions = fieldLevelAuthzService.getAllowedActions("Timesheet Resource");
            
            Map<String, Object> response = new HashMap<>();
            response.put("actions", allowedActions);
            
            logger.info("Returned allowed actions for current user: {}", allowedActions);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting allowed actions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error\",\"message\":\"Failed to get allowed actions\"}");
        }
    }

    private String extractAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Authentication object: {}", authentication);
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            logger.info("JWT token: subject={}, preferred_username={}, name={}", 
                jwt.getSubject(), jwt.getClaimAsString("preferred_username"), jwt.getClaimAsString("name"));
            
            // Try different ways to get user ID
            String userId = jwt.getSubject();
            if (userId == null || userId.isEmpty()) {
                userId = jwt.getClaimAsString("preferred_username");
            }
            if (userId == null || userId.isEmpty()) {
                userId = jwt.getClaimAsString("name");
            }
            if (userId == null || userId.isEmpty()) {
                userId = jwt.getClaimAsString("email");
            }
            
            logger.info("Final user ID: {}", userId);
            return userId;
        }
        logger.warn("Could not extract user ID from authentication context");
        return null;
    }

    private Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) realmAccess.get("roles");
                return new HashSet<>(rolesList);
            }
        }
        return Set.of();
    }

    /**
     * Converts TimesheetResponse to Map for field filtering
     */
    private Map<String, Object> convertTimesheetResponseToMap(TimesheetResponse timesheet) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", timesheet.getId());
        map.put("userId", timesheet.getUserId());
        map.put("employeeId", timesheet.getEmployeeId());
        map.put("employeeName", timesheet.getEmployeeName());
        map.put("department", timesheet.getDepartment());
        map.put("location", timesheet.getLocation());
        map.put("payPeriodStart", timesheet.getPayPeriodStart());
        map.put("payPeriodEnd", timesheet.getPayPeriodEnd());
        map.put("regularHours", timesheet.getRegularHours());
        map.put("overtimeHours", timesheet.getOvertimeHours());
        map.put("holidayHours", timesheet.getHolidayHours());
        map.put("sickHours", timesheet.getSickHours());
        map.put("vacationHours", timesheet.getVacationHours());
        map.put("totalHours", timesheet.getTotalHours());
        map.put("status", timesheet.getStatus());
        map.put("comments", timesheet.getComments());
        map.put("supervisorComments", timesheet.getSupervisorComments());
        map.put("approvedBy", timesheet.getApprovedBy());
        map.put("approvedAt", timesheet.getApprovedAt());
        map.put("submittedBy", timesheet.getSubmittedBy());
        map.put("submittedAt", timesheet.getSubmittedAt());
        map.put("createdAt", timesheet.getCreatedAt());
        map.put("updatedAt", timesheet.getUpdatedAt());
        return map;
    }
    
    /**
     * Converts Map to TimesheetUpdateRequest for update operations
     */
    private TimesheetUpdateRequest convertMapToTimesheetUpdateRequest(Map<String, Object> map) {
        TimesheetUpdateRequest request = new TimesheetUpdateRequest();
        
        // Only set fields that exist in TimesheetUpdateRequest class
        if (map.containsKey("regularHours")) {
            request.setRegularHours(map.get("regularHours") != null ? 
                new java.math.BigDecimal(map.get("regularHours").toString()) : null);
        }
        if (map.containsKey("overtimeHours")) {
            request.setOvertimeHours(map.get("overtimeHours") != null ? 
                new java.math.BigDecimal(map.get("overtimeHours").toString()) : null);
        }
        if (map.containsKey("holidayHours")) {
            request.setHolidayHours(map.get("holidayHours") != null ? 
                new java.math.BigDecimal(map.get("holidayHours").toString()) : null);
        }
        if (map.containsKey("sickHours")) {
            request.setSickHours(map.get("sickHours") != null ? 
                new java.math.BigDecimal(map.get("sickHours").toString()) : null);
        }
        if (map.containsKey("vacationHours")) {
            request.setVacationHours(map.get("vacationHours") != null ? 
                new java.math.BigDecimal(map.get("vacationHours").toString()) : null);
        }
        if (map.containsKey("status")) {
            request.setStatus(map.get("status") != null ? 
                com.cmips.entity.TimesheetStatus.valueOf(map.get("status").toString()) : null);
        }
        if (map.containsKey("comments")) {
            request.setComments(map.get("comments") != null ? map.get("comments").toString() : null);
        }
        if (map.containsKey("supervisorComments")) {
            request.setSupervisorComments(map.get("supervisorComments") != null ? 
                map.get("supervisorComments").toString() : null);
        }
        
        return request;
    }
    
    /**
     * Converts Map to TimesheetCreateRequest for create operations
     */
    private TimesheetCreateRequest convertMapToTimesheetCreateRequest(Map<String, Object> map) {
        TimesheetCreateRequest request = new TimesheetCreateRequest();
        
        // Set fields that exist in TimesheetCreateRequest class
        if (map.containsKey("employeeId")) {
            request.setEmployeeId(map.get("employeeId") != null ? map.get("employeeId").toString() : null);
        }
        if (map.containsKey("employeeName")) {
            request.setEmployeeName(map.get("employeeName") != null ? map.get("employeeName").toString() : null);
        }
        if (map.containsKey("department")) {
            request.setDepartment(map.get("department") != null ? map.get("department").toString() : null);
        }
        if (map.containsKey("location")) {
            request.setLocation(map.get("location") != null ? map.get("location").toString() : null);
        }
        if (map.containsKey("payPeriodStart")) {
            request.setPayPeriodStart(map.get("payPeriodStart") != null ? 
                java.time.LocalDate.parse(map.get("payPeriodStart").toString()) : null);
        }
        if (map.containsKey("payPeriodEnd")) {
            request.setPayPeriodEnd(map.get("payPeriodEnd") != null ? 
                java.time.LocalDate.parse(map.get("payPeriodEnd").toString()) : null);
        }
        if (map.containsKey("regularHours")) {
            request.setRegularHours(map.get("regularHours") != null ? 
                new java.math.BigDecimal(map.get("regularHours").toString()) : null);
        }
        if (map.containsKey("overtimeHours")) {
            request.setOvertimeHours(map.get("overtimeHours") != null ? 
                new java.math.BigDecimal(map.get("overtimeHours").toString()) : null);
        }
        if (map.containsKey("holidayHours")) {
            request.setHolidayHours(map.get("holidayHours") != null ? 
                new java.math.BigDecimal(map.get("holidayHours").toString()) : null);
        }
        if (map.containsKey("sickHours")) {
            request.setSickHours(map.get("sickHours") != null ? 
                new java.math.BigDecimal(map.get("sickHours").toString()) : null);
        }
        if (map.containsKey("vacationHours")) {
            request.setVacationHours(map.get("vacationHours") != null ? 
                new java.math.BigDecimal(map.get("vacationHours").toString()) : null);
        }
        if (map.containsKey("comments")) {
            request.setComments(map.get("comments") != null ? map.get("comments").toString() : null);
        }
        
        return request;
    }
}
