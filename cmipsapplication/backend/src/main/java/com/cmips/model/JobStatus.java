package com.cmips.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class JobStatus {

    private String jobId;
    private String status;
    private Integer progress;
    private Long totalRecords;
    private Long processedRecords;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime estimatedCompletionTime;

    private String userRole;
    private String reportType;
    private String targetSystem;
    private String dataFormat;
    private String jobSource;

    public JobStatus() {}

    public JobStatus(String jobId, String status, Integer progress) {
        this.jobId = jobId;
        this.status = status;
        this.progress = progress;
    }

    public JobStatus(String jobId, String status, Integer progress, Long totalRecords, Long processedRecords,
                    String errorMessage, LocalDateTime createdAt, LocalDateTime startedAt, LocalDateTime completedAt,
                    LocalDateTime estimatedCompletionTime, String userRole, String reportType, String targetSystem,
                    String dataFormat, String jobSource) {
        this.jobId = jobId;
        this.status = status;
        this.progress = progress;
        this.totalRecords = totalRecords;
        this.processedRecords = processedRecords;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.estimatedCompletionTime = estimatedCompletionTime;
        this.userRole = userRole;
        this.reportType = reportType;
        this.targetSystem = targetSystem;
        this.dataFormat = dataFormat;
        this.jobSource = jobSource;
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

    public Long getProcessedRecords() { return processedRecords; }
    public void setProcessedRecords(Long processedRecords) { this.processedRecords = processedRecords; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getEstimatedCompletionTime() { return estimatedCompletionTime; }
    public void setEstimatedCompletionTime(LocalDateTime estimatedCompletionTime) { this.estimatedCompletionTime = estimatedCompletionTime; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }

    public String getDataFormat() { return dataFormat; }
    public void setDataFormat(String dataFormat) { this.dataFormat = dataFormat; }

    public String getJobSource() { return jobSource; }
    public void setJobSource(String jobSource) { this.jobSource = jobSource; }

    // Helper methods
    public boolean isCompleted() { return "COMPLETED".equals(status); }
    public boolean isFailed() { return "FAILED".equals(status); }
    public boolean isProcessing() { return "PROCESSING".equals(status); }
    public boolean isQueued() { return "QUEUED".equals(status); }
    public boolean isCancelled() { return "CANCELLED".equals(status); }

    public String getStatusDescription() {
        switch (status) {
            case "QUEUED": return "Job is queued for processing";
            case "PROCESSING": return "Job is currently being processed";
            case "COMPLETED": return "Job completed successfully";
            case "FAILED": return "Job failed: " + (errorMessage != null ? errorMessage : "Unknown error");
            case "CANCELLED": return "Job was cancelled";
            default: return "Unknown status: " + status;
        }
    }
}
