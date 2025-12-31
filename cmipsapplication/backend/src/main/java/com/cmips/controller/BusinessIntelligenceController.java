package com.cmips.controller;

import com.cmips.model.BIReportRequest;
import com.cmips.model.JobStatus;
import com.cmips.service.JobQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Generate BI report (creates a job and returns job ID)
     */
    @PostMapping("/reports/generate")
    public ResponseEntity<Map<String, Object>> generateBIReport(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("🔍 BusinessIntelligenceController: Creating BI report job");
            
            // SECURITY: JWT token is REQUIRED
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Authentication required. Please login first.");
                return ResponseEntity.status(401).body(response);
            }
            
            // Extract JWT token
            String jwtToken = authHeader.substring(7);
            
            // Extract user info from JWT token
            Map<String, Object> userInfo = extractUserInfoFromJWT(jwtToken);
            if (userInfo == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Invalid or expired token. Please login again.");
                return ResponseEntity.status(401).body(response);
            }
            
            // Use role from JWT token
            String userRole = (String) userInfo.get("role");
            
            // Convert request to BIReportRequest
            BIReportRequest biRequest = new BIReportRequest();
            biRequest.setUserRole(userRole);
            biRequest.setReportType((String) request.get("reportType"));
            biRequest.setTargetSystem((String) request.get("targetSystem"));
            biRequest.setDataFormat((String) request.get("dataFormat"));
            biRequest.setCountyId((String) request.get("countyId"));
            biRequest.setPriority((Integer) request.getOrDefault("priority", 5));
            
            // Queue the job with JWT token
            String jobId = jobQueueService.queueReportJob(biRequest, jwtToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Report job queued successfully");
            response.put("jobId", jobId);
            
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

    /**
     * Get job status
     */
    @GetMapping("/jobs/{jobId}/status")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        try {
            JobStatus jobStatus = jobQueueService.getJobStatus(jobId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("jobId", jobStatus.getJobId());
            response.put("jobStatus", jobStatus);
            response.put("message", "Job status retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to get job status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get job result
     */
    @GetMapping("/jobs/{jobId}/result")
    public ResponseEntity<Map<String, Object>> getJobResult(@PathVariable String jobId) {
        try {
            JobStatus jobStatus = jobQueueService.getJobStatus(jobId);
            
            if (jobStatus != null && jobStatus.isCompleted()) {
                com.cmips.model.ReportResult result = jobQueueService.getJobResult(jobId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "SUCCESS");
                response.put("jobId", jobId);
                response.put("result", result);
                response.put("message", "Job result retrieved successfully");
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Job not completed yet. Status: " + (jobStatus != null ? jobStatus.getStatus() : "NOT_FOUND"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
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
            JobStatus jobStatus = jobQueueService.getJobStatus(jobId);
            
            if (jobStatus == null || !jobStatus.isCompleted()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Job not found or not completed yet.");
                return ResponseEntity.badRequest().body(response);
            }
            
            com.cmips.model.ReportResult result = jobQueueService.getJobResult(jobId);
            if (result == null || result.getResultPath() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Report file not found for job: " + jobId);
                return ResponseEntity.notFound().build();
            }
            
            String filePath = result.getResultPath();
            File file = new File(filePath);
            
            if (!file.exists()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Report file not found on disk: " + filePath);
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type
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
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(new org.springframework.core.io.FileSystemResource(file));
                    
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to download report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Cancel job
     */
    @PostMapping("/jobs/{jobId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelJob(@PathVariable String jobId) {
        try {
            boolean cancelled = jobQueueService.cancelJob(jobId);
            
            Map<String, Object> response = new HashMap<>();
            if (cancelled) {
                response.put("status", "SUCCESS");
                response.put("message", "Job cancelled successfully");
            } else {
                response.put("status", "ERROR");
                response.put("message", "Job could not be cancelled. It may already be completed or not found.");
            }
            response.put("jobId", jobId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to cancel job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get all jobs status
     */
    @GetMapping("/jobs/status/ALL")
    public ResponseEntity<Map<String, Object>> getAllJobsStatus() {
        try {
            List<JobStatus> allJobs = jobQueueService.getAllJobs();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("jobs", allJobs);
            response.put("message", "Jobs retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to get jobs status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get jobs by status
     */
    @GetMapping("/jobs/status/{status}")
    public ResponseEntity<Map<String, Object>> getJobsByStatus(@PathVariable String status) {
        try {
            List<JobStatus> jobs = jobQueueService.getJobsByStatus(status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("jobs", jobs);
            response.put("message", "Jobs retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to get jobs by status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Extract user info from JWT token
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
            
            // Extract role from realm_access
            if (jsonNode.has("realm_access") && jsonNode.get("realm_access").has("roles")) {
                com.fasterxml.jackson.databind.JsonNode roles = jsonNode.get("realm_access").get("roles");
                if (roles.isArray() && roles.size() > 0) {
                    userInfo.put("role", roles.get(0).asText());
                }
            }
            
            // Extract username
            if (jsonNode.has("preferred_username")) {
                userInfo.put("username", jsonNode.get("preferred_username").asText());
            }
            
            return userInfo;
        } catch (Exception e) {
            System.err.println("❌ BusinessIntelligenceController: Error extracting user info from JWT: " + e.getMessage());
            return null;
        }
    }
}

