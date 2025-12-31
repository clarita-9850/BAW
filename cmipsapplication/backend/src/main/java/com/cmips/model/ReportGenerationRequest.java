package com.cmips.model;

import java.time.LocalDate;
import java.util.List;

public class ReportGenerationRequest {
    private String userRole;
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> statusFilter;
    private List<String> providerFilter;
    private List<String> projectFilter;
    private String outputFormat = "JSON";
    private boolean includeSummary = true;
    private boolean includeCharts = false;

    // Access control fields
    private String userCounty;
    private String districtId;

    // Pagination fields
    private Integer page = 0;  // 0-based page number
    private Integer pageSize = 1000;  // Records per page

    public ReportGenerationRequest() {}

    public ReportGenerationRequest(String userRole, String reportType, LocalDate startDate, LocalDate endDate,
                                   List<String> statusFilter, List<String> providerFilter, List<String> projectFilter,
                                   String outputFormat, boolean includeSummary, boolean includeCharts,
                                   String userCounty, String districtId, Integer page, Integer pageSize) {
        this.userRole = userRole;
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.statusFilter = statusFilter;
        this.providerFilter = providerFilter;
        this.projectFilter = projectFilter;
        this.outputFormat = outputFormat;
        this.includeSummary = includeSummary;
        this.includeCharts = includeCharts;
        this.userCounty = userCounty;
        this.districtId = districtId;
        this.page = page;
        this.pageSize = pageSize;
    }

    // Helper method to get DateRange (for compatibility with IHSS pipeline services)
    public PipelineExtractionRequest.DateRange getDateRange() {
        if (startDate != null || endDate != null) {
            return new PipelineExtractionRequest.DateRange(startDate, endDate);
        }
        return null;
    }

    // Getters and Setters
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<String> getStatusFilter() { return statusFilter; }
    public void setStatusFilter(List<String> statusFilter) { this.statusFilter = statusFilter; }

    public List<String> getProviderFilter() { return providerFilter; }
    public void setProviderFilter(List<String> providerFilter) { this.providerFilter = providerFilter; }

    public List<String> getProjectFilter() { return projectFilter; }
    public void setProjectFilter(List<String> projectFilter) { this.projectFilter = projectFilter; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public boolean isIncludeSummary() { return includeSummary; }
    public void setIncludeSummary(boolean includeSummary) { this.includeSummary = includeSummary; }

    public boolean isIncludeCharts() { return includeCharts; }
    public void setIncludeCharts(boolean includeCharts) { this.includeCharts = includeCharts; }

    public String getUserCounty() { return userCounty; }
    public void setUserCounty(String userCounty) { this.userCounty = userCounty; }

    public String getDistrictId() { return districtId; }
    public void setDistrictId(String districtId) { this.districtId = districtId; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
