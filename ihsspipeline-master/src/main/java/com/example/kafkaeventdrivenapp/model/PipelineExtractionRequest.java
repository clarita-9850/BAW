package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineExtractionRequest {
    private String userRole;
    private String reportType;
    private DateRange dateRange;
    private List<String> statusFilter;
    private List<String> providerFilter;
    private List<String> projectFilter;
    private List<String> fieldSelection;
    private boolean includeMaskedFields = true;
    private boolean includeHiddenFields = false;
    
    // County-based access control
    private String userCounty;
    private List<String> allowedCounties;
    private boolean enforceCountyAccess = true;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRange {
        private LocalDate startDate;
        private LocalDate endDate;
    }
}
