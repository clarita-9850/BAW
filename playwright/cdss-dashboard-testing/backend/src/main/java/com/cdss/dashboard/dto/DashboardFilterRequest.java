package com.cdss.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardFilterRequest {

    private List<String> countyCodes;
    private List<String> ethnicities;
    private List<String> genders;
    private List<String> ageGroups;
    private List<String> races;
    private List<String> agedBlindDisabled;
    private List<String> caseTypes;
    private Boolean severelyImpaired;
    private String evvStatus;

    // Dimension selections
    private String dimension1;
    private String dimension2;
    private String dimension3;
    private String dimension4;
    private String dimension5;
    private String dimension6;
    private String dimension7;
    private String dimension8;

    // Measure selections
    private List<String> selectedMeasures;

    // View type
    private String viewType; // "details" or "pivot"
}
