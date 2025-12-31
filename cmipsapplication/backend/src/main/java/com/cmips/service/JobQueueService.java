package com.cmips.service;

import com.cmips.entity.ReportJobEntity;
import com.cmips.model.BIReportRequest;
import com.cmips.model.JobStatus;
import com.cmips.model.ReportResult;
import com.cmips.repository.ReportJobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JobQueueService {
    
    @Autowired
    private ReportJobRepository jobRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public JobQueueService() {
        System.out.println("🔧 JobQueueService: Constructor called - initializing...");
        System.out.println("✅ JobQueueService: Constructor completed successfully");
    }
    
    /**
     * Queue a new report generation job with JWT token
     */
    public String queueReportJob(BIReportRequest request, String jwtToken) {
        System.out.println("📋 JobQueueService: Queuing new report job for role: " + request.getUserRole());
        
        try {
            // Generate unique job ID
            String jobId = "JOB_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
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
            job.setJobSource("MANUAL");
            
            // Estimate completion time
            LocalDateTime estimatedCompletion = estimateCompletionTime(request);
            job.setEstimatedCompletionTime(estimatedCompletion);
            
            // Save job to database
            jobRepository.save(job);
            
            System.out.println("✅ JobQueueService: Job queued successfully with ID: " + jobId);
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
            return convertToJobStatus(job);
            
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
                job.setCompletedAt(LocalDateTime.now());
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
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("❌ Error getting jobs by status: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get jobs by status", e);
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
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("❌ Error getting all jobs: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get all jobs", e);
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
                    job.setStartedAt(LocalDateTime.now());
                } else if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                    job.setCompletedAt(LocalDateTime.now());
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
                job.setCompletedAt(LocalDateTime.now());
                jobRepository.save(job);

                System.out.println("✅ JobQueueService: Job result set - " + jobId + " -> " + resultPath);
            }
        } catch (Exception e) {
            System.err.println("❌ Error setting job result: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set job source (MANUAL or SCHEDULED)
     */
    public void setJobSource(String jobId, String source) {
        try {
            Optional<ReportJobEntity> jobOpt = jobRepository.findByJobId(jobId);
            if (jobOpt.isPresent()) {
                ReportJobEntity job = jobOpt.get();
                job.setJobSource(source);
                jobRepository.save(job);

                System.out.println("✅ JobQueueService: Job source set - " + jobId + " -> " + source);
            }
        } catch (Exception e) {
            System.err.println("❌ Error setting job source: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper methods
    private LocalDateTime estimateCompletionTime(BIReportRequest request) {
        int baseMinutes = 2;
        
        switch (request.getReportType()) {
            case "DAILY_REPORT":
                return LocalDateTime.now().plusMinutes(baseMinutes);
            case "WEEKLY_REPORT":
                return LocalDateTime.now().plusMinutes(baseMinutes * 2);
            case "MONTHLY_REPORT":
                return LocalDateTime.now().plusMinutes(baseMinutes * 5);
            case "QUARTERLY_REPORT":
                return LocalDateTime.now().plusMinutes(baseMinutes * 10);
            case "ANNUAL_REPORT":
                return LocalDateTime.now().plusMinutes(baseMinutes * 20);
            default:
                return LocalDateTime.now().plusMinutes(baseMinutes * 3);
        }
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
        status.setProgress(job.getProgress());
        status.setTotalRecords(job.getTotalRecords());
        status.setProcessedRecords(job.getProcessedRecords());
        status.setErrorMessage(job.getErrorMessage());
        status.setCreatedAt(job.getCreatedAt());
        status.setStartedAt(job.getStartedAt());
        status.setCompletedAt(job.getCompletedAt());
        status.setEstimatedCompletionTime(job.getEstimatedCompletionTime());
        status.setUserRole(job.getUserRole());
        status.setReportType(job.getReportType());
        status.setTargetSystem(job.getTargetSystem());
        status.setDataFormat(job.getDataFormat());
        status.setJobSource(job.getJobSource());
        return status;
    }
}

