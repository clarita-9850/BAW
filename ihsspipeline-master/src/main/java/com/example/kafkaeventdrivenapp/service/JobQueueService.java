package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.entity.ReportJobEntity;
import com.example.kafkaeventdrivenapp.model.*;
import com.example.kafkaeventdrivenapp.repository.ReportJobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobQueueService {
    
    @Autowired
    private ReportJobRepository jobRepository;
    
    @Autowired
    private com.example.kafkaeventdrivenapp.config.ReportTypeProperties reportTypeProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public JobQueueService() {
        System.out.println("🔧 JobQueueService: Constructor called - initializing...");
        try {
            System.out.println("✅ JobQueueService: Constructor completed successfully");
        } catch (Exception e) {
            System.err.println("❌ JobQueueService: Constructor failed with error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Queue a new report generation job
     */
    public String queueReportJob(BIReportRequest request) {
        return queueReportJob(request, null);
    }
    
    /**
     * Queue a new report generation job with JWT token
     */
    public String queueReportJob(BIReportRequest request, String jwtToken) {
        return queueReportJob(request, jwtToken, null);
    }
    
    /**
     * Queue a new report generation job with JWT token and optional parent job ID
     */
    public String queueReportJob(BIReportRequest request, String jwtToken, String parentJobId) {
        boolean isDependentJob = parentJobId != null && !parentJobId.isEmpty();
        
        if (isDependentJob) {
            System.out.println("   └─ [QUEUEING DEPENDENT JOB]");
            System.out.println("      └─ Parent Job ID: " + parentJobId);
        } else {
            System.out.println("📋 JobQueueService: Queuing new report job for role: " + request.getUserRole());
        }
        
        try {
            // Generate unique job ID
            String jobId = "JOB_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            if (isDependentJob) {
                System.out.println("      └─ Generated Dependent Job ID: " + jobId);
            }
            
            // Create job entity
            ReportJobEntity job = new ReportJobEntity(
                jobId,
                request.getUserRole(),
                request.getReportType(),
                request.getTargetSystem()
            );
            
            // Set additional properties
            job.setDataFormat(request.getDataFormat());
            job.setChunkSize(request.getChunkSize());
            job.setPriority(request.getPriority());
            job.setRequestData(serializeRequest(request));
            job.setJwtToken(jwtToken);
            
            // Set parent job ID if provided (for dependency tracking)
            if (isDependentJob) {
                job.setParentJobId(parentJobId);
                System.out.println("      └─ Linked to parent job: " + parentJobId);
                System.out.println("      └─ Report Type: " + request.getReportType());
                System.out.println("      └─ User Role: " + request.getUserRole());
                System.out.println("      └─ Target System: " + request.getTargetSystem());
                System.out.println("      └─ Status: QUEUED (will start processing when worker thread claims it)");
            }
            
            // Estimate completion time based on report type and data volume
            LocalDateTime estimatedCompletion = estimateCompletionTime(request);
            job.setEstimatedCompletionTime(estimatedCompletion);
            
            // Save job to database (scheduler will pick it up)
            jobRepository.save(job);
            
            if (isDependentJob) {
                System.out.println("      ✅ Dependent job queued successfully!");
                System.out.println("         └─ Dependent Job ID: " + jobId);
                System.out.println("         └─ Parent Job ID: " + parentJobId);
                System.out.println("         └─ Job will start processing when BackgroundProcessingService claims it");
            } else {
                System.out.println("✅ JobQueueService: Job queued successfully with ID: " + jobId);
            }
            
            return jobId;
            
        } catch (Exception e) {
            System.err.println("❌ Error queuing report job: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to queue report job", e);
        }
    }
    
    /**
     * Get job status by job ID
     */
    public JobStatus getJobStatus(String jobId) {
        System.out.println("📊 JobQueueService: Getting status for job: " + jobId);
        
        try {
            Optional<ReportJobEntity> jobOpt = jobRepository.findByJobId(jobId);
            
            if (jobOpt.isEmpty()) {
                throw new RuntimeException("Job not found: " + jobId);
            }
            
            ReportJobEntity job = jobOpt.get();
            
            // Use convertToJobStatus to ensure userRole and reportType are properly extracted
            JobStatus status = convertToJobStatus(job);
            
            System.out.println("✅ JobQueueService: Job status retrieved - " + job.getStatus() + " (" + job.getProgress() + "%)");
            System.out.println("✅ JobQueueService: userRole: " + status.getUserRole() + ", reportType: " + status.getReportType());
            return status;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting job status: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get job status", e);
        }
    }
    
    /**
     * Get job result if completed
     */
    public ReportResult getJobResult(String jobId) {
        System.out.println("📄 JobQueueService: Getting result for job: " + jobId);
        
        try {
            Optional<ReportJobEntity> jobOpt = jobRepository.findByJobId(jobId);
            
            if (jobOpt.isEmpty()) {
                throw new RuntimeException("Job not found: " + jobId);
            }
            
            ReportJobEntity job = jobOpt.get();
            
            if (!job.isCompleted()) {
                throw new RuntimeException("Job not completed yet. Status: " + job.getStatus());
            }
            
            ReportResult result = new ReportResult();
            result.setJobId(jobId);
            result.setStatus(job.getStatus());
            result.setResultPath(job.getResultPath());
            result.setTotalRecords(job.getTotalRecords());
            result.setProcessedRecords(job.getProcessedRecords());
            result.setDataFormat(job.getDataFormat());
            result.setCompletedAt(job.getCompletedAt());
            
            System.out.println("✅ JobQueueService: Job result retrieved for job: " + jobId);
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting job result: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get job result", e);
        }
    }
    
    /**
     * Cancel a job
     */
    public boolean cancelJob(String jobId) {
        System.out.println("❌ JobQueueService: Cancelling job: " + jobId);
        
        try {
            Optional<ReportJobEntity> jobOpt = jobRepository.findByJobId(jobId);
            
            if (jobOpt.isEmpty()) {
                return false;
            }
            
            ReportJobEntity job = jobOpt.get();
            
            // Only cancel if job is queued or processing
            if ("QUEUED".equals(job.getStatus()) || "PROCESSING".equals(job.getStatus())) {
                job.setStatus("CANCELLED");
                job.setCompletedAt(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());
                jobRepository.save(job);
                
                System.out.println("✅ JobQueueService: Job cancelled successfully: " + jobId);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Error cancelling job: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get jobs by status
     */
    public List<JobStatus> getJobsByStatus(String status) {
        System.out.println("📋 JobQueueService: Getting jobs with status: " + status);
        
        try {
            List<ReportJobEntity> jobs = jobRepository.findByStatus(status);
            
            return jobs.stream()
                .map(this::convertToJobStatus)
                .toList();
                
        } catch (Exception e) {
            System.err.println("❌ Error getting jobs by status: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get jobs by status", e);
        }
    }
    
    /**
     * Get jobs by user role
     */
    public List<JobStatus> getJobsByUserRole(String userRole) {
        System.out.println("📋 JobQueueService: Getting jobs for user role: " + userRole);
        
        try {
            List<ReportJobEntity> jobs = jobRepository.findByUserRole(userRole);
            
            return jobs.stream()
                .map(this::convertToJobStatus)
                .toList();
                
        } catch (Exception e) {
            System.err.println("❌ Error getting jobs by user role: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get jobs by user role", e);
        }
    }
    
    /**
     * Get all jobs
     */
    public List<JobStatus> getAllJobs() {
        System.out.println("📋 JobQueueService: Getting all jobs");
        
        try {
            List<ReportJobEntity> jobs = jobRepository.findAll();
            
            return jobs.stream()
                .map(this::convertToJobStatus)
                .toList();
                
        } catch (Exception e) {
            System.err.println("❌ Error getting all jobs: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get all jobs", e);
        }
    }
    
    /**
     * Get all jobs filtered by user's county and role
     */
    public List<JobStatus> getAllJobsFilteredByCounty(String userRole, String userCounty, String userJwtToken) {
        System.out.println("📋 JobQueueService: Getting all jobs filtered by county - role: " + userRole + ", county: " + userCounty);
        
        try {
            List<ReportJobEntity> allJobs = jobRepository.findAll();
            
            // ADMIN and SYSTEM_SCHEDULER can see all jobs
            if (userRole != null && (userRole.equals("ADMIN") || userRole.equals("SYSTEM_SCHEDULER"))) {
                System.out.println("📋 JobQueueService: User is " + userRole + " - returning all jobs");
                return allJobs.stream()
                    .map(this::convertToJobStatus)
                    .toList();
            }
            
            // For other roles, filter by county AND userRole
            return allJobs.stream()
                .filter(job -> {
                    try {
                        // Extract job's userRole - this is the role the report was generated FOR
                        String jobUserRole = job.getUserRole();
                        
                        // If job's userRole is null/empty, try to extract from requestData
                        if (jobUserRole == null || jobUserRole.trim().isEmpty()) {
                            try {
                                BIReportRequest request = deserializeRequest(job);
                                if (request != null && request.getUserRole() != null) {
                                    jobUserRole = request.getUserRole();
                                }
                            } catch (Exception e) {
                                // Ignore deserialization errors
                            }
                        }
                        
                        // Extract county from job's JWT token or request data
                        String jobCounty = extractCountyFromJob(job);
                        System.out.println("🔍 JobQueueService: Job " + job.getJobId() + " - jobUserRole: " + jobUserRole + ", jobCounty: " + jobCounty + ", userCounty: " + userCounty);
                        
                        // SUPERVISOR can only see jobs generated FOR supervisors
                        if (userRole != null && userRole.equals("SUPERVISOR")) {
                            // Must match supervisor role
                            if (jobUserRole == null || !jobUserRole.equalsIgnoreCase("SUPERVISOR")) {
                                System.out.println("🔍 JobQueueService: Job " + job.getJobId() + " filtered out - role mismatch: " + jobUserRole);
                                return false;
                            }
                            // County check - "ALL" means visible to all counties
                            if (userCounty == null || userCounty.trim().isEmpty()) {
                                System.out.println("⚠️ JobQueueService: Supervisor has no county restriction - showing all supervisor jobs");
                                return true; // Supervisor with no county restriction sees all supervisor jobs
                            }
                            // If jobCounty is "ALL", it's visible to all users
                            if (jobCounty != null && jobCounty.equalsIgnoreCase("ALL")) {
                                System.out.println("🔍 JobQueueService: Job " + job.getJobId() + " has county=ALL - visible to all");
                                return true;
                            }
                            boolean countyMatches = jobCounty != null && jobCounty.equals(userCounty);
                            System.out.println("🔍 JobQueueService: Job " + job.getJobId() + " county match: " + countyMatches + " (jobCounty=" + jobCounty + ", userCounty=" + userCounty + ")");
                            return countyMatches;
                        }
                        
                        // CASE_WORKER can only see jobs generated FOR case workers
                        if (userRole != null && userRole.equals("CASE_WORKER")) {
                            // Must match case worker role
                            if (jobUserRole == null || !jobUserRole.equalsIgnoreCase("CASE_WORKER")) {
                                return false;
                            }
                            // County check - "ALL" means visible to all counties
                            if (jobCounty != null && jobCounty.equalsIgnoreCase("ALL")) {
                                System.out.println("🔍 JobQueueService: Job " + job.getJobId() + " has county=ALL - visible to all");
                                return true;
                            }
                            // County must match
                            return jobCounty != null && jobCounty.equals(userCounty);
                        }
                        
                        // Default: hide jobs if role not recognized
                        System.out.println("🔍 JobQueueService: Job " + job.getJobId() + " filtered out - userRole is null or unrecognized: " + userRole);
                        return false;
                    } catch (Exception e) {
                        System.err.println("⚠️ JobQueueService: Error filtering job " + job.getJobId() + ": " + e.getMessage());
                        return false; // Hide jobs that can't be parsed
                    }
                })
                .map(this::convertToJobStatus)
                .toList();
                
        } catch (Exception e) {
            System.err.println("❌ Error getting filtered jobs: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get filtered jobs", e);
        }
    }
    
    /**
     * Extract county from job's JWT token or request data
     */
    private String extractCountyFromJob(ReportJobEntity job) {
        // Try to extract from JWT token first
        if (job.getJwtToken() != null && !job.getJwtToken().trim().isEmpty()) {
            try {
                String[] parts = job.getJwtToken().split("\\.");
                if (parts.length >= 2) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
                    
                    // Check for countyId in various formats (camelCase, snake_case, attributes)
                    if (jsonNode.has("countyId")) {
                        return jsonNode.get("countyId").asText();
                    }
                    // Check for county_id (snake_case) at root level
                    if (jsonNode.has("county_id")) {
                        return jsonNode.get("county_id").asText();
                    }
                    // Check for countyId in attributes
                    if (jsonNode.has("attributes") && jsonNode.get("attributes").has("countyId")) {
                        com.fasterxml.jackson.databind.JsonNode countyIdNode = jsonNode.get("attributes").get("countyId");
                        if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                            return countyIdNode.get(0).asText();
                        }
                    }
                    // Check for county_id in attributes
                    if (jsonNode.has("attributes") && jsonNode.get("attributes").has("county_id")) {
                        com.fasterxml.jackson.databind.JsonNode countyIdNode = jsonNode.get("attributes").get("county_id");
                        if (countyIdNode.isArray() && countyIdNode.size() > 0) {
                            return countyIdNode.get(0).asText();
                        } else if (countyIdNode.isTextual()) {
                            return countyIdNode.asText();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ JobQueueService: Error parsing JWT token for job " + job.getJobId() + ": " + e.getMessage());
            }
        }
        
        // Try to extract from request data
        if (job.getRequestData() != null && !job.getRequestData().trim().isEmpty()) {
            try {
                System.out.println("🔍 JobQueueService: Extracting county from request data for job " + job.getJobId());
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode requestNode = mapper.readTree(job.getRequestData());
                java.util.List<String> fieldNames = new java.util.ArrayList<>();
                requestNode.fieldNames().forEachRemaining(fieldNames::add);
                System.out.println("🔍 JobQueueService: Request data keys: " + fieldNames);
                
                // Check for countyId in request data (set by ScheduledReportService)
                if (requestNode.has("countyId")) {
                    String countyId = requestNode.get("countyId").asText();
                    System.out.println("✅ JobQueueService: Found countyId in request data: " + countyId);
                    return countyId;
                }
                if (requestNode.has("userCounty")) {
                    String userCounty = requestNode.get("userCounty").asText();
                    System.out.println("✅ JobQueueService: Found userCounty in request data: " + userCounty);
                    return userCounty;
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        
        return null;
    }
    
    /**
     * Get next job for processing (highest priority, oldest first)
     */
    public Optional<ReportJobEntity> getNextJobForProcessing() {
        try {
            List<ReportJobEntity> queuedJobs = jobRepository.findQueuedJobsByPriority();
            return queuedJobs.stream().findFirst();
        } catch (Exception e) {
            System.err.println("❌ Error getting next job for processing: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Update job status
     */
    public void updateJobStatus(String jobId, String status, String errorMessage) {
        try {
            Optional<ReportJobEntity> jobOpt = jobRepository.findByJobId(jobId);
            if (jobOpt.isPresent()) {
                ReportJobEntity job = jobOpt.get();
                job.setStatus(status);
                job.setErrorMessage(errorMessage);
                
                if ("PROCESSING".equals(status) && job.getStartedAt() == null) {
                    job.setStartedAt(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());
                } else if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                    job.setCompletedAt(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());
                }
                
                jobRepository.save(job);
                System.out.println("✅ JobQueueService: Job status updated - " + jobId + " -> " + status);
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating job status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Attempt to claim a queued job for processing.
     * @return updated job entity if successful, otherwise empty.
     */
    public Optional<ReportJobEntity> markJobAsProcessing(String jobId) {
        try {
            Optional<ReportJobEntity> jobOpt = jobRepository.findByJobId(jobId);
            if (jobOpt.isPresent()) {
                ReportJobEntity job = jobOpt.get();
                if (!"QUEUED".equals(job.getStatus())) {
                    return Optional.empty();
                }
                job.setStatus("PROCESSING");
                job.setStartedAt(LocalDateTime.now());
                jobRepository.save(job);
                System.out.println("⏳ JobQueueService: Job claimed for processing - " + jobId);
                return Optional.of(job);
            }
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("❌ Error claiming job for processing: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    public BIReportRequest deserializeRequest(ReportJobEntity job) {
        if (job.getRequestData() == null) {
            return null;
        }
        try {
            return objectMapper.readValue(job.getRequestData(), BIReportRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize stored report request for job " + job.getJobId(), e);
        }
    }
    
    /**
     * Update job progress
     */
    public void updateJobProgress(String jobId, long processed, long total) {
        try {
            Optional<ReportJobEntity> jobOpt = jobRepository.findByJobId(jobId);
            if (jobOpt.isPresent()) {
                ReportJobEntity job = jobOpt.get();
                job.updateProgress(processed, total);
                jobRepository.save(job);
                
                System.out.println("📊 JobQueueService: Job progress updated - " + jobId + " -> " + processed + "/" + total + " (" + job.getProgress() + "%)");
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating job progress: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set job result path
     */
    public void setJobResult(String jobId, String resultPath) {
        try {
            Optional<ReportJobEntity> jobOpt = jobRepository.findByJobId(jobId);
            if (jobOpt.isPresent()) {
                ReportJobEntity job = jobOpt.get();
                job.setResultPath(resultPath);
                job.setStatus("COMPLETED");
                job.setCompletedAt(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime());
                
                // Ensure progress is set to 100% for completed jobs
                if (job.getProgress() == null || job.getProgress() < 100) {
                    job.setProgress(100);
                }
                
                // Set processed records equal to total records if not already set
                if (job.getTotalRecords() != null && job.getTotalRecords() > 0) {
                    if (job.getProcessedRecords() == null || job.getProcessedRecords() < job.getTotalRecords()) {
                        job.setProcessedRecords(job.getTotalRecords());
                    }
                }
                
                jobRepository.save(job);
                
                System.out.println("✅ JobQueueService: Job result set - " + jobId + " -> " + resultPath + " (progress: 100%)");
            }
        } catch (Exception e) {
            System.err.println("❌ Error setting job result: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set job source (SCHEDULED, MANUAL, API)
     */
    public void setJobSource(String jobId, String jobSource) {
        try {
            Optional<ReportJobEntity> jobOpt = jobRepository.findByJobId(jobId);
            if (jobOpt.isPresent()) {
                ReportJobEntity job = jobOpt.get();
                job.setJobSource(jobSource);
                jobRepository.save(job);
                
                System.out.println("✅ JobQueueService: Job source set - " + jobId + " -> " + jobSource);
            }
        } catch (Exception e) {
            System.err.println("❌ Error setting job source: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper methods
    private LocalDateTime estimateCompletionTime(BIReportRequest request) {
        // Get estimated duration from configuration instead of hardcoded switch
        long estimatedMinutes = reportTypeProperties.getEstimatedDuration(request.getReportType());
        return ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime().plusMinutes(estimatedMinutes);
    }
    
    private String serializeRequest(BIReportRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize report request", e);
        }
    }
    
    private JobStatus convertToJobStatus(ReportJobEntity job) {
        JobStatus status = new JobStatus();
        status.setJobId(job.getJobId());
        status.setStatus(job.getStatus());
        
        // Ensure progress is always set - default to 0 if null, or 100 if completed
        Integer progress = job.getProgress();
        if (progress == null) {
            progress = "COMPLETED".equals(job.getStatus()) ? 100 : 0;
        } else if ("COMPLETED".equals(job.getStatus()) && progress < 100) {
            progress = 100; // Ensure completed jobs show 100%
        }
        status.setProgress(progress);
        
        status.setTotalRecords(job.getTotalRecords() != null ? job.getTotalRecords() : 0L);
        status.setProcessedRecords(job.getProcessedRecords() != null ? job.getProcessedRecords() : 0L);
        status.setErrorMessage(job.getErrorMessage());
        status.setCreatedAt(job.getCreatedAt());
        status.setStartedAt(job.getStartedAt());
        status.setCompletedAt(job.getCompletedAt());
        status.setEstimatedCompletionTime(job.getEstimatedCompletionTime());
        
        // Set userRole and reportType - use entity fields first, fallback to requestData if null/empty
        String userRole = job.getUserRole();
        String reportType = job.getReportType();
        
        // Check if fields are null or empty (null-safe check)
        boolean userRoleEmpty = (userRole == null || userRole.trim().isEmpty());
        boolean reportTypeEmpty = (reportType == null || reportType.trim().isEmpty());
        
        // If fields are null/empty, try to extract from requestData
        if (userRoleEmpty || reportTypeEmpty) {
            try {
                BIReportRequest request = deserializeRequest(job);
                if (request != null) {
                    if (userRoleEmpty) {
                        String extractedRole = request.getUserRole();
                        if (extractedRole != null && !extractedRole.trim().isEmpty()) {
                            userRole = extractedRole;
                            System.out.println("📋 JobQueueService: Extracted userRole from requestData: " + userRole);
                        }
                    }
                    if (reportTypeEmpty) {
                        String extractedType = request.getReportType();
                        if (extractedType != null && !extractedType.trim().isEmpty()) {
                            reportType = extractedType;
                            System.out.println("📋 JobQueueService: Extracted reportType from requestData: " + reportType);
                        }
                    }
                } else {
                    System.out.println("⚠️ JobQueueService: requestData is null for job " + job.getJobId());
                }
            } catch (Exception e) {
                System.err.println("❌ JobQueueService: Error deserializing requestData for job " + job.getJobId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Always set the values, even if null (frontend will show "—")
        // Null-safe check before setting
        status.setUserRole((userRole != null && !userRole.trim().isEmpty()) ? userRole : null);
        status.setReportType((reportType != null && !reportType.trim().isEmpty()) ? reportType : null);
        
        System.out.println("📋 JobQueueService: Final values for job " + job.getJobId() + " - userRole: " + status.getUserRole() + ", reportType: " + status.getReportType());
        status.setTargetSystem(job.getTargetSystem());
        status.setDataFormat(job.getDataFormat());
        status.setJobSource(job.getJobSource());
        return status;
    }
}
