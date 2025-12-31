package com.example.kafkaeventdrivenapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportJobEntity {
    
    @Id
    @Column(name = "job_id")
    private String jobId;
    
    @Column(name = "user_role", nullable = false)
    private String userRole;
    
    @Column(name = "report_type", nullable = false)
    private String reportType;
    
    @Column(name = "status", nullable = false)
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED
    
    @Column(name = "progress")
    private Integer progress = 0; // 0-100
    
    @Column(name = "total_records")
    private Long totalRecords = 0L;
    
    @Column(name = "processed_records")
    private Long processedRecords = 0L;
    
    @Column(name = "result_path")
    private String resultPath; // Path to generated report file
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData; // JSON of original request
    
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
    
    // BI-specific fields
    @Column(name = "target_system")
    private String targetSystem; // BUSINESS_OBJECTS, CRYSTAL_REPORTS, etc.
    
    @Column(name = "data_format")
    private String dataFormat; // JSON, XML, CSV, EXCEL
    
    @Column(name = "chunk_size")
    private Integer chunkSize = 1000;
    
    @Column(name = "priority")
    private Integer priority = 5; // 1-10, higher number = higher priority
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    // Email delivery fields
    @Column(name = "email_delivered")
    private Boolean emailDelivered = false;
    
    @Column(name = "email_recipients", columnDefinition = "TEXT")
    private String emailRecipients; // Comma-separated list of email addresses
    
    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;
    
    @Column(name = "pdf_file_path")
    private String pdfFilePath; // Path to generated PDF file
    
    @Column(name = "template_used")
    private String templateUsed; // Template used for generation
    
    @Column(name = "job_source")
    private String jobSource; // SCHEDULED, MANUAL, API
    
    @Column(name = "jwt_token", columnDefinition = "TEXT")
    private String jwtToken;
    
    @Column(name = "parent_job_id")
    private String parentJobId; // Track parent job for dependency chains
    
    // Constructor for creating new jobs
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
    
    // Helper methods
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
    
    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }
    
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    public void updateProgress(long processed, long total) {
        this.processedRecords = processed;
        this.totalRecords = total;
        this.progress = total > 0 ? (int) ((processed * 100) / total) : 0;
    }
}
