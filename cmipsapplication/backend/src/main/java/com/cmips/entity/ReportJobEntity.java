package com.cmips.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_jobs")
public class ReportJobEntity {

    @Id
    @Column(name = "job_id")
    private String jobId;

    @Column(name = "user_role", nullable = false)
    private String userRole;

    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "progress")
    private Integer progress = 0;

    @Column(name = "total_records")
    private Long totalRecords = 0L;

    @Column(name = "processed_records")
    private Long processedRecords = 0L;

    @Column(name = "result_path")
    private String resultPath;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Column(name = "estimated_completion_time")
    private LocalDateTime estimatedCompletionTime;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "target_system")
    private String targetSystem;

    @Column(name = "data_format")
    private String dataFormat;

    @Column(name = "chunk_size")
    private Integer chunkSize = 1000;

    @Column(name = "priority")
    private Integer priority = 5;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    @Column(name = "job_source")
    private String jobSource;

    @Column(name = "jwt_token", columnDefinition = "TEXT")
    private String jwtToken;

    @Column(name = "parent_job_id")
    private String parentJobId;

    public ReportJobEntity() {}

    public ReportJobEntity(String jobId, String userRole, String reportType, String targetSystem) {
        this.jobId = jobId;
        this.userRole = userRole;
        this.reportType = reportType;
        this.targetSystem = targetSystem;
        this.status = "QUEUED";
        this.progress = 0;
        this.priority = 5;
        this.retryCount = 0;
        this.maxRetries = 3;
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

    public Long getProcessedRecords() { return processedRecords; }
    public void setProcessedRecords(Long processedRecords) { this.processedRecords = processedRecords; }

    public String getResultPath() { return resultPath; }
    public void setResultPath(String resultPath) { this.resultPath = resultPath; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getRequestData() { return requestData; }
    public void setRequestData(String requestData) { this.requestData = requestData; }

    public LocalDateTime getEstimatedCompletionTime() { return estimatedCompletionTime; }
    public void setEstimatedCompletionTime(LocalDateTime estimatedCompletionTime) { this.estimatedCompletionTime = estimatedCompletionTime; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }

    public String getDataFormat() { return dataFormat; }
    public void setDataFormat(String dataFormat) { this.dataFormat = dataFormat; }

    public Integer getChunkSize() { return chunkSize; }
    public void setChunkSize(Integer chunkSize) { this.chunkSize = chunkSize; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }

    public String getJobSource() { return jobSource; }
    public void setJobSource(String jobSource) { this.jobSource = jobSource; }

    public String getJwtToken() { return jwtToken; }
    public void setJwtToken(String jwtToken) { this.jwtToken = jwtToken; }

    public String getParentJobId() { return parentJobId; }
    public void setParentJobId(String parentJobId) { this.parentJobId = parentJobId; }

    // Helper methods
    public boolean isCompleted() { return "COMPLETED".equals(status); }
    public boolean isFailed() { return "FAILED".equals(status); }
    public boolean isProcessing() { return "PROCESSING".equals(status); }
    public boolean canRetry() { return retryCount < maxRetries; }
    public void incrementRetryCount() { this.retryCount++; }

    public void updateProgress(long processed, long total) {
        this.processedRecords = processed;
        this.totalRecords = total;
        this.progress = total > 0 ? (int) ((processed * 100) / total) : 0;
    }
}
