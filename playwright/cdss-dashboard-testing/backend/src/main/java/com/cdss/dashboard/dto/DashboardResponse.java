package com.cdss.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    // Summary metrics
    private Long totalIndividuals;
    private Long totalPopulation;
    private Double perCapitaRate;
    private Double totalAuthorizedHours;
    private Double avgAuthorizedHours;

    // Table data
    private List<DashboardTableRow> tableData;

    // Available filter options
    private FilterOptions filterOptions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardTableRow {
        private String race;
        private String ethnicity;
        private String gender;
        private String ageGroup;
        private String agedBlindDisabled;
        private String caseType;
        private String countyName;
        private Long count;
        private Long countyPopulation;
        private Double authorizedHours;
        private Double perCapitaRate;
        private Long applicationsReceived;
        private Long applicationsApproved;
        private Long applicationsDenied;
        private Double activeErrorRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilterOptions {
        private List<String> counties;
        private List<String> ethnicities;
        private List<String> genders;
        private List<String> ageGroups;
        private List<String> races;
        private List<String> agedBlindDisabled;
        private List<String> caseTypes;
        private List<String> dimensions;
        private List<String> measures;
    }
}
