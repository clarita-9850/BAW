package com.example.kafkaeventdrivenapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobStatus {
    
    private String jobId;
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED
    private Integer progress; // 0-100
    private Long totalRecords;
    private Long processedRecords;
    private String errorMessage;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime startedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime completedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime estimatedCompletionTime;
    private String userRole;
    private String reportType;
    private String targetSystem;
    private String dataFormat;
    private String jobSource; // SCHEDULED, MANUAL, API
    
    // Constructor for basic status
    public JobStatus(String jobId, String status, Integer progress) {
        this.jobId = jobId;
        this.status = status;
        this.progress = progress;
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
    
    public boolean isQueued() {
        return "QUEUED".equals(status);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
    
    public String getStatusDescription() {
        switch (status) {
            case "QUEUED":
                return "Job is queued for processing";
            case "PROCESSING":
                return "Job is currently being processed";
            case "COMPLETED":
                return "Job completed successfully";
            case "FAILED":
                return "Job failed: " + (errorMessage != null ? errorMessage : "Unknown error");
            case "CANCELLED":
                return "Job was cancelled";
            default:
                return "Unknown status: " + status;
        }
    }
}
