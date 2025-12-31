package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResult {
    
    private String jobId;
    private String status;
    private String resultPath; // Path to generated report file
    private Long totalRecords;
    private Long processedRecords;
    private String dataFormat;
    private LocalDateTime completedAt;
    private String downloadUrl;
    private String fileName;
    private Long fileSize;
    private String checksum;
    
    // Constructor for basic result
    public ReportResult(String jobId, String status, String resultPath) {
        this.jobId = jobId;
        this.status = status;
        this.resultPath = resultPath;
    }
    
    // Constructor with download info
    public ReportResult(String jobId, String status, String resultPath, 
                       String downloadUrl, String fileName, Long fileSize) {
        this.jobId = jobId;
        this.status = status;
        this.resultPath = resultPath;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
}
