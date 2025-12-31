package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.config.JobProcessingProperties;
import com.example.kafkaeventdrivenapp.model.BIReportRequest;
import com.example.kafkaeventdrivenapp.model.JobStatus;
import com.example.kafkaeventdrivenapp.service.JobQueueService;
import com.example.kafkaeventdrivenapp.service.ScheduledReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bi")
@CrossOrigin(origins = "*")
public class BusinessIntelligenceController {

    @Autowired
    private JobQueueService jobQueueService;
    
    @Autowired
    private JobProcessingProperties jobProcessingProperties;
    
    @Autowired
    private ScheduledReportService scheduledReportService;
    
    @Autowired
    private com.example.kafkaeventdrivenapp.config.ReportTypeProperties reportTypeProperties;

    /**
     * Generate BI report (creates a job and returns job ID)
     */
    @PostMapping("/reports/generate")
    public ResponseEntity<Map<String, Object>> generateBIReport(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("🔍 BusinessIntelligenceController: Creating BI report job");
            
            // SECURITY: JWT token is REQUIRED for creating batch jobs
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.err.println("❌ BusinessIntelligenceController: Missing or invalid Authorization header");
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Authentication required. Please login first.");
                return ResponseEntity.status(401).body(response);
            }
            
            // Extract JWT token from Authorization header
            String jwtToken = authHeader.substring(7);
            
            // SECURITY: Extract user info from JWT token (source of truth for role and permissions)
            Map<String, Object> userInfo = extractUserInfoFromJWT(jwtToken);
            if (userInfo == null) {
                System.err.println("❌ BusinessIntelligenceController: Failed to extract user info from JWT token");
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Invalid or expired token. Please login again.");
                return ResponseEntity.status(401).body(response);
            }
            
            // SECURITY: Use role and countyId from JWT token (ignore request body)
            String userRole = (String) userInfo.get("role");
            String countyId = (String) userInfo.get("countyId");
            
            System.out.println("🔐 BusinessIntelligenceController: User info from token - role: " + userRole + 
                              ", countyId: " + countyId);
            
            // Convert request to BIReportRequest
            BIReportRequest biRequest = new BIReportRequest();
            
            // SECURITY: Set role from JWT token (not from request body)
            biRequest.setUserRole(userRole);
            
            // Other fields from request (these are safe as they don't affect permissions)
            biRequest.setReportType((String) request.get("reportType"));
            biRequest.setTargetSystem((String) request.get("targetSystem"));
            biRequest.setDataFormat((String) request.get("dataFormat"));
            Integer requestedChunkSize = extractChunkSize(request.get("chunkSize"));
            biRequest.setChunkSize(jobProcessingProperties.normalizeChunkSize(requestedChunkSize));
            biRequest.setPriority((Integer) request.getOrDefault("priority", 5));
            
            // SECURITY: Use countyId from token if available, otherwise from request
            // (For county workers, token values take precedence)
            if (countyId != null) {
                biRequest.setCountyId(countyId);
                System.out.println("🔐 BusinessIntelligenceController: Using countyId from token: " + countyId);
            } else if (request.containsKey("countyId")) {
                biRequest.setCountyId((String) request.get("countyId"));
            }
            
            // Date filters from request (safe, these are just filters)
            if (request.containsKey("startDate")) {
                biRequest.setStartDate(java.time.LocalDate.parse((String) request.get("startDate")));
            }
            if (request.containsKey("endDate")) {
                biRequest.setEndDate(java.time.LocalDate.parse((String) request.get("endDate")));
            }
            
            // Queue the job with JWT token
            String jobId = jobQueueService.queueReportJob(biRequest, jwtToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Report job queued successfully");
            response.put("jobId", jobId);
            response.put("estimatedCompletionTime", "Job queued for processing");
            
            System.out.println("✅ BusinessIntelligenceController: Job created with ID: " + jobId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error creating BI report job: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to create report job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private Integer extractChunkSize(Object chunkSizeValue) {
        if (chunkSizeValue instanceof Number numberValue) {
            return numberValue.intValue();
        }
        return null;
    }

    /**
     * Get job status (real implementation)
     */
    @GetMapping("/jobs/{jobId}/status")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        try {
            System.out.println("🔍 BusinessIntelligenceController: Getting job status for: " + jobId);
            
            JobStatus jobStatus = jobQueueService.getJobStatus(jobId);
            
            if (jobStatus != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "SUCCESS");
                response.put("jobId", jobStatus.getJobId());
                response.put("jobStatus", jobStatus);
                response.put("message", "Job status retrieved successfully");
                
                System.out.println("✅ BusinessIntelligenceController: Job status retrieved: " + jobStatus.getStatus());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Job not found: " + jobId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error getting job status: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to get job status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get job result (real implementation)
     */
    @GetMapping("/jobs/{jobId}/result")
    public ResponseEntity<Map<String, Object>> getJobResult(@PathVariable String jobId) {
        try {
            System.out.println("🔍 BusinessIntelligenceController: Getting job result for: " + jobId);
            
            JobStatus jobStatus = jobQueueService.getJobStatus(jobId);
            
            if (jobStatus != null) {
                if (jobStatus.isCompleted()) {
                    // Get the actual result data
                    com.example.kafkaeventdrivenapp.model.ReportResult result = jobQueueService.getJobResult(jobId);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "SUCCESS");
                    response.put("jobId", jobId);
                    response.put("result", result);
                    response.put("message", "Job result retrieved successfully");
                    
                    System.out.println("✅ BusinessIntelligenceController: Job result retrieved for: " + jobId);
                    return ResponseEntity.ok(response);
                } else {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "ERROR");
                    response.put("message", "Job not completed yet. Status: " + jobStatus.getStatus());
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Job not found: " + jobId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error getting job result: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to get job result: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Download report file
     */
    @GetMapping("/jobs/{jobId}/download")
    public ResponseEntity<?> downloadReport(@PathVariable String jobId) {
        try {
            System.out.println("🔍 BusinessIntelligenceController: Downloading report for job: " + jobId);
            
            JobStatus jobStatus = jobQueueService.getJobStatus(jobId);
            
            if (jobStatus == null || !jobStatus.isCompleted()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Job not found or not completed yet. Status: " + (jobStatus != null ? jobStatus.getStatus() : "NOT_FOUND"));
                return ResponseEntity.badRequest().body(response);
            }
            
            com.example.kafkaeventdrivenapp.model.ReportResult result = jobQueueService.getJobResult(jobId);
            if (result == null || result.getResultPath() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Report file not found for job: " + jobId);
                return ResponseEntity.notFound().build();
            }
            
            String filePath = result.getResultPath();
            System.out.println("🔍 BusinessIntelligenceController: Attempting to read file: " + filePath);
            
            // Handle both absolute paths and relative paths
            File file = new File(filePath);
            if (!file.exists() && !filePath.startsWith("/")) {
                // Try relative to current working directory
                file = new File(System.getProperty("user.dir"), filePath);
                System.out.println("🔍 BusinessIntelligenceController: Trying relative path: " + file.getAbsolutePath());
            }
            
            if (!file.exists()) {
                System.err.println("❌ BusinessIntelligenceController: File not found: " + filePath + " (absolute: " + file.getAbsolutePath() + ")");
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Report file not found on disk: " + filePath);
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type based on file extension
            String contentType = "application/octet-stream";
            String filename = file.getName();
            if (filename.endsWith(".json")) {
                contentType = "application/json";
            } else if (filename.endsWith(".csv")) {
                contentType = "text/csv";
            } else if (filename.endsWith(".xml")) {
                contentType = "application/xml";
            } else if (filename.endsWith(".pdf")) {
                contentType = "application/pdf";
            }
            
            System.out.println("✅ BusinessIntelligenceController: Serving file: " + filePath + " (" + file.length() + " bytes)");
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(new org.springframework.core.io.FileSystemResource(file));
                    
        } catch (Exception e) {
            System.err.println("❌ Error downloading report: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to download report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Cancel job (real implementation)
     */
    @PostMapping("/jobs/{jobId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelJob(@PathVariable String jobId) {
        try {
            System.out.println("🔍 BusinessIntelligenceController: Cancelling job: " + jobId);
            
            boolean cancelled = jobQueueService.cancelJob(jobId);
            
            Map<String, Object> response = new HashMap<>();
            if (cancelled) {
                response.put("status", "SUCCESS");
                response.put("message", "Job cancelled successfully");
                System.out.println("✅ BusinessIntelligenceController: Job cancelled: " + jobId);
            } else {
                response.put("status", "ERROR");
                response.put("message", "Job could not be cancelled. It may already be completed or not found.");
            }
            response.put("jobId", jobId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error cancelling job: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to cancel job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get all jobs status (real implementation)
     * Filters jobs by user's county based on JWT token
     */
    @GetMapping("/jobs/status/ALL")
    public ResponseEntity<Map<String, Object>> getAllJobsStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("🔍 BusinessIntelligenceController: Getting all jobs status");
            
            // Get JWT token from Spring Security Authentication (already validated by SecurityConfig)
            String jwtToken = null;
            Map<String, Object> userInfo = null;
            
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    jwtToken = jwt.getTokenValue();
                    System.out.println("🔐 BusinessIntelligenceController: JWT token extracted from SecurityContext");
                    userInfo = extractUserInfoFromJwtObject(jwt);
                } else {
                    System.out.println("⚠️ BusinessIntelligenceController: No JWT in SecurityContext, trying Authorization header fallback");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        jwtToken = authHeader.substring(7);
                        userInfo = extractUserInfoFromJWT(jwtToken);
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ BusinessIntelligenceController: Error getting JWT from SecurityContext: " + e.getMessage());
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    jwtToken = authHeader.substring(7);
                    userInfo = extractUserInfoFromJWT(jwtToken);
                }
            }
            
            String userRole = userInfo != null ? (String) userInfo.get("role") : null;
            String userCounty = userInfo != null ? (String) userInfo.get("countyId") : null;
            
            System.out.println("🔍 BusinessIntelligenceController: User role: " + userRole + ", county: " + userCounty);
            
            // Filter jobs based on user's county and role
            List<JobStatus> allJobs = jobQueueService.getAllJobsFilteredByCounty(userRole, userCounty, jwtToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("jobs", allJobs);
            response.put("message", "Jobs retrieved successfully");
            
            System.out.println("✅ BusinessIntelligenceController: Retrieved " + allJobs.size() + " jobs (filtered by county)");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting all jobs status: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to get jobs status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get jobs by status (real implementation)
     */
    @GetMapping("/jobs/status/{status}")
    public ResponseEntity<Map<String, Object>> getJobsByStatus(@PathVariable String status) {
        try {
            System.out.println("🔍 BusinessIntelligenceController: Getting jobs by status: " + status);
            
            List<JobStatus> jobs = jobQueueService.getJobsByStatus(status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("jobs", jobs);
            response.put("message", "Jobs retrieved successfully");
            
            System.out.println("✅ BusinessIntelligenceController: Retrieved " + jobs.size() + " jobs with status: " + status);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting jobs by status: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to get jobs by status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get report types
     */
    @GetMapping("/report-types")
    public ResponseEntity<Map<String, Object>> getReportTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        // Get report types from configuration instead of hardcoding
        response.put("reportTypes", reportTypeProperties.getAll().toArray(new String[0]));
        return ResponseEntity.ok(response);
    }

    /**
     * Get target systems
     */
    @GetMapping("/target-systems")
    public ResponseEntity<Map<String, Object>> getTargetSystems() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("targetSystems", new String[]{"POWER_BI", "TABLEAU", "CRYSTAL_REPORTS", "BUSINESS_OBJECTS"});
        return ResponseEntity.ok(response);
    }

    /**
     * Extract user info from Spring Security JWT object (preferred method)
     */
    private Map<String, Object> extractUserInfoFromJwtObject(Jwt jwt) {
        Map<String, Object> userInfo = new java.util.HashMap<>();
        
        try {
            System.out.println("🔐 BusinessIntelligenceController: Extracting user info from JWT object");
            System.out.println("🔍 BusinessIntelligenceController: All JWT claim names: " + jwt.getClaims().keySet());
            System.out.println("🔍 BusinessIntelligenceController: JWT claims: " + jwt.getClaims());
            
            // Extract role - EXACT same logic as DataPipelineController (which works)
            String extractedRole = null;
            
            // First, try to extract from resource_access (CLIENT roles)
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            System.out.println("🔍 BusinessIntelligenceController: resource_access = " + resourceAccess);
            if (resourceAccess == null) {
                System.err.println("❌ BusinessIntelligenceController: resource_access is NULL in JWT!");
            } else {
                System.out.println("✅ BusinessIntelligenceController: resource_access exists, keys: " + resourceAccess.keySet());
                @SuppressWarnings("unchecked")
                Map<String, Object> trialAppAccess = (Map<String, Object>) resourceAccess.get("trial-app");
                System.out.println("🔍 BusinessIntelligenceController: trial-app access = " + trialAppAccess);
                if (trialAppAccess == null) {
                    System.err.println("❌ BusinessIntelligenceController: trial-app is NULL in resource_access! Available clients: " + resourceAccess.keySet());
                } else {
                    @SuppressWarnings("unchecked")
                    List<String> clientRoles = (List<String>) trialAppAccess.get("roles");
                    System.out.println("🔍 BusinessIntelligenceController: clientRoles = " + clientRoles);
                    if (clientRoles == null || clientRoles.isEmpty()) {
                        System.err.println("❌ BusinessIntelligenceController: clientRoles is NULL or EMPTY! trialAppAccess keys: " + trialAppAccess.keySet());
                    } else {
                        for (String role : clientRoles) {
                            if (role != null && !role.trim().isEmpty()) {
                                try {
                                    extractedRole = com.example.kafkaeventdrivenapp.util.RoleMapper.canonicalName(role);
                                    System.out.println("🔐 BusinessIntelligenceController: Extracted CLIENT role from JWT object: " + role + " -> " + extractedRole);
                                    break;
                                } catch (IllegalArgumentException e) {
                                    System.err.println("⚠️ BusinessIntelligenceController: Role '" + role + "' is not a valid UserRole, trying next role");
                                    continue;
                                }
                            }
                        }
                    }
                }
            }
            
            // Fallback to realm_access.roles
            if (extractedRole == null) {
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                System.out.println("🔍 BusinessIntelligenceController: realm_access = " + realmAccess);
                if (realmAccess != null) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) realmAccess.get("roles");
                    System.out.println("🔍 BusinessIntelligenceController: realm roles = " + roles);
                    if (roles != null) {
                        for (String role : roles) {
                            if (role != null && !role.trim().isEmpty() && 
                                !role.startsWith("default-roles-") &&
                                !role.equals("offline_access") &&
                                !role.equals("uma_authorization")) {
                                try {
                                    extractedRole = com.example.kafkaeventdrivenapp.util.RoleMapper.canonicalName(role);
                                    System.out.println("🔐 BusinessIntelligenceController: Extracted REALM role from JWT object: " + role + " -> " + extractedRole);
                                    break;
                                } catch (IllegalArgumentException e) {
                                    System.err.println("⚠️ BusinessIntelligenceController: Role '" + role + "' is not a valid UserRole, trying next role");
                                    continue;
                                }
                            }
                        }
                    }
                }
            }
            
            if (extractedRole != null) {
                userInfo.put("role", extractedRole);
                System.out.println("✅ BusinessIntelligenceController: Successfully extracted role: " + extractedRole);
            } else {
                System.err.println("❌ BusinessIntelligenceController: FAILED to extract role from JWT. Available claims: " + jwt.getClaims().keySet());
                System.err.println("❌ BusinessIntelligenceController: resource_access = " + jwt.getClaimAsMap("resource_access"));
                System.err.println("❌ BusinessIntelligenceController: realm_access = " + jwt.getClaimAsMap("realm_access"));
            }
            
            // Extract username
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null) {
                userInfo.put("username", preferredUsername);
            }
            
            // Extract countyId - try direct field first (both camelCase and snake_case)
            String countyId = jwt.getClaimAsString("countyId");
            if (countyId == null || countyId.trim().isEmpty()) {
                // Try snake_case format
                countyId = jwt.getClaimAsString("county_id");
            }
            if (countyId != null && !countyId.trim().isEmpty()) {
                userInfo.put("countyId", countyId);
                System.out.println("✅ BusinessIntelligenceController: Found countyId directly in JWT object: " + countyId);
            } else {
                // Try attributes.countyId or attributes.county_id
                Map<String, Object> attributes = jwt.getClaimAsMap("attributes");
                if (attributes != null) {
                    Object countyIdObj = attributes.get("countyId");
                    if (countyIdObj == null) {
                        countyIdObj = attributes.get("county_id");
                    }
                    if (countyIdObj != null) {
                        if (countyIdObj instanceof List && ((List<?>) countyIdObj).size() > 0) {
                            countyId = ((List<?>) countyIdObj).get(0).toString();
                        } else {
                            countyId = countyIdObj.toString();
                        }
                        if (countyId != null && !countyId.trim().isEmpty()) {
                            userInfo.put("countyId", countyId);
                            System.out.println("✅ BusinessIntelligenceController: Extracted countyId from attributes in JWT object: " + countyId);
                        }
                    }
                }
            }
            
            // NO FALLBACK - countyId MUST be in JWT token
            if (!userInfo.containsKey("countyId")) {
                System.err.println("❌ BusinessIntelligenceController: countyId NOT FOUND in JWT token. Token must contain countyId in attributes.countyId.");
                System.err.println("❌ BusinessIntelligenceController: JWT claims available: " + jwt.getClaims().keySet());
                // Do NOT set default - let it fail explicitly
            }
            
            System.out.println("🔐 BusinessIntelligenceController: Final extracted userInfo from JWT object: " + userInfo);
            return userInfo;
            
        } catch (Exception e) {
            System.err.println("❌ BusinessIntelligenceController: Error extracting user info from JWT object: " + e.getMessage());
            System.err.println("❌ BusinessIntelligenceController: Exception type: " + e.getClass().getName());
            e.printStackTrace();
            // Return empty map instead of null to avoid NPE
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", "Failed to extract user info: " + e.getMessage());
            return errorInfo;
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
            
            Map<String, Object> userInfo = new java.util.HashMap<>();
            
            // Extract role - same logic as DataPipelineController: check client roles first, then realm roles
            String extractedRole = null;
            
            // First, try to extract from resource_access (CLIENT roles) - this is the primary source
            if (jsonNode.has("resource_access")) {
                com.fasterxml.jackson.databind.JsonNode resourceAccess = jsonNode.get("resource_access");
                if (resourceAccess.has("trial-app") && resourceAccess.get("trial-app").has("roles")) {
                    com.fasterxml.jackson.databind.JsonNode clientRoles = resourceAccess.get("trial-app").get("roles");
                    if (clientRoles.isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode roleNode : clientRoles) {
                            String role = roleNode.asText();
                            if (role != null && !role.trim().isEmpty()) {
                                // Use RoleMapper to get canonical role name
                                extractedRole = com.example.kafkaeventdrivenapp.util.RoleMapper.canonicalName(role);
                                System.out.println("🔐 BusinessIntelligenceController: Extracted CLIENT role: " + role + " -> " + extractedRole);
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
                            extractedRole = com.example.kafkaeventdrivenapp.util.RoleMapper.canonicalName(role);
                            System.out.println("🔐 BusinessIntelligenceController: Extracted REALM role: " + role + " -> " + extractedRole);
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
            
            // Extract countyId from attributes (Keycloak custom attributes) - support both camelCase and snake_case
            if (jsonNode.has("countyId")) {
                userInfo.put("countyId", jsonNode.get("countyId").asText());
            } else if (jsonNode.has("county_id")) {
                userInfo.put("countyId", jsonNode.get("county_id").asText());
            } else if (jsonNode.has("attributes")) {
                com.fasterxml.jackson.databind.JsonNode attributes = jsonNode.get("attributes");
                if (attributes.has("countyId")) {
                    com.fasterxml.jackson.databind.JsonNode countyIdNode = attributes.get("countyId");
                    if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                        userInfo.put("countyId", countyIdNode.get(0).asText());
                    } else if (countyIdNode.isTextual()) {
                        userInfo.put("countyId", countyIdNode.asText());
                    }
                } else if (attributes.has("county_id")) {
                    com.fasterxml.jackson.databind.JsonNode countyIdNode = attributes.get("county_id");
                    if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                        userInfo.put("countyId", countyIdNode.get(0).asText());
                    } else if (countyIdNode.isTextual()) {
                        userInfo.put("countyId", countyIdNode.asText());
                    }
                }
            }
            
            // NO FALLBACK - countyId MUST be in JWT token
            if (!userInfo.containsKey("countyId")) {
                System.err.println("❌ BusinessIntelligenceController: countyId NOT FOUND in JWT token. Token must contain countyId in attributes.countyId.");
                java.util.List<String> fieldNames = new java.util.ArrayList<>();
                jsonNode.fieldNames().forEachRemaining(fieldNames::add);
                System.err.println("❌ BusinessIntelligenceController: JWT payload keys: " + fieldNames);
                // Do NOT set default - let it fail explicitly
            }
            
            return userInfo;
        } catch (Exception e) {
            System.err.println("❌ BusinessIntelligenceController: Error extracting user info from JWT: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Start test scheduler - triggers jobs every 2 minutes for 10 minutes
     * Each job is DAILY_REPORT (CASE_WORKER) which will trigger DAILY_SUMMARY as dependent job
     */
    @PostMapping("/test-scheduler/start")
    public ResponseEntity<Map<String, Object>> startTestScheduler() {
        try {
            System.out.println("🚀 BusinessIntelligenceController: Starting test scheduler");
            
            scheduledReportService.startTestScheduler();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Test scheduler started. Jobs will be triggered every 2 minutes for 10 minutes (5 jobs total).");
            response.put("details", Map.of(
                "duration", "10 minutes",
                "interval", "2 minutes",
                "totalJobs", 5,
                "jobType", "DAILY_REPORT (CASE_WORKER)",
                "dependentJob", "DAILY_SUMMARY (CASE_WORKER)"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error starting test scheduler: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to start test scheduler: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Stop test scheduler
     */
    @PostMapping("/test-scheduler/stop")
    public ResponseEntity<Map<String, Object>> stopTestScheduler() {
        try {
            System.out.println("⏹️  BusinessIntelligenceController: Stopping test scheduler");
            
            scheduledReportService.stopTestScheduler();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Test scheduler stopped");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error stopping test scheduler: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to stop test scheduler: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get test scheduler status
     */
    @GetMapping("/test-scheduler/status")
    public ResponseEntity<Map<String, Object>> getTestSchedulerStatus() {
        try {
            String status = scheduledReportService.getTestSchedulerStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("schedulerStatus", status);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting test scheduler status: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to get test scheduler status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Reset test scheduler counters
     */
    @PostMapping("/test-scheduler/reset")
    public ResponseEntity<Map<String, Object>> resetTestScheduler() {
        try {
            System.out.println("🔄 BusinessIntelligenceController: Resetting test scheduler");
            
            scheduledReportService.resetTestScheduler();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Test scheduler reset");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error resetting test scheduler: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to reset test scheduler: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
