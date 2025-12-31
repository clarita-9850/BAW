package com.cmips.model;

import java.time.LocalDateTime;

public class ReportResult {
    private String jobId;
    private String status;
    private String resultPath;
    private Long totalRecords;
    private Long processedRecords;
    private String dataFormat;
    private LocalDateTime completedAt;

    public ReportResult() {}

    public ReportResult(String jobId, String status, String resultPath, Long totalRecords,
                       Long processedRecords, String dataFormat, LocalDateTime completedAt) {
        this.jobId = jobId;
        this.status = status;
        this.resultPath = resultPath;
        this.totalRecords = totalRecords;
        this.processedRecords = processedRecords;
        this.dataFormat = dataFormat;
        this.completedAt = completedAt;
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResultPath() { return resultPath; }
    public void setResultPath(String resultPath) { this.resultPath = resultPath; }

    public Long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Long totalRecords) { this.totalRecords = totalRecords; }

    public Long getProcessedRecords() { return processedRecords; }
    public void setProcessedRecords(Long processedRecords) { this.processedRecords = processedRecords; }

    public String getDataFormat() { return dataFormat; }
    public void setDataFormat(String dataFormat) { this.dataFormat = dataFormat; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
