package com.cdss.dashboard.service;

import com.cdss.dashboard.dto.DashboardFilterRequest;
import com.cdss.dashboard.dto.DashboardResponse;
import com.cdss.dashboard.dto.DashboardResponse.DashboardTableRow;
import com.cdss.dashboard.dto.DashboardResponse.FilterOptions;
import com.cdss.dashboard.entity.DemographicRecord;
import com.cdss.dashboard.repository.DemographicRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DemographicRecordRepository demographicRecordRepository;

    public DashboardResponse getDashboardData(DashboardFilterRequest request) {
        List<DemographicRecord> records = getFilteredRecords(request);

        // Calculate summary metrics
        long totalIndividuals = records.stream()
                .mapToLong(r -> r.getIndividualCount() != null ? r.getIndividualCount() : 0)
                .sum();

        long totalPopulation = records.stream()
                .mapToLong(r -> r.getCountyPopulation() != null ? r.getCountyPopulation() : 0)
                .sum();

        double totalAuthorizedHours = records.stream()
                .mapToDouble(r -> r.getAuthorizedHours() != null ? r.getAuthorizedHours() : 0)
                .sum();

        double perCapitaRate = totalPopulation > 0
                ? (double) totalIndividuals / totalPopulation * 1000
                : 0;

        double avgAuthorizedHours = totalIndividuals > 0
                ? totalAuthorizedHours / totalIndividuals
                : 0;

        // Build table data based on dimensions
        List<DashboardTableRow> tableData = buildTableData(records, request);

        // Get filter options
        FilterOptions filterOptions = getFilterOptions();

        return DashboardResponse.builder()
                .totalIndividuals(totalIndividuals)
                .totalPopulation(totalPopulation)
                .perCapitaRate(Math.round(perCapitaRate * 100.0) / 100.0)
                .totalAuthorizedHours(Math.round(totalAuthorizedHours * 100.0) / 100.0)
                .avgAuthorizedHours(Math.round(avgAuthorizedHours * 100.0) / 100.0)
                .tableData(tableData)
                .filterOptions(filterOptions)
                .build();
    }

    private List<DemographicRecord> getFilteredRecords(DashboardFilterRequest request) {
        List<DemographicRecord> allRecords = demographicRecordRepository.findAll();

        return allRecords.stream()
                .filter(r -> filterByList(r.getCountyCode(), request.getCountyCodes()))
                .filter(r -> filterByList(r.getEthnicity(), request.getEthnicities()))
                .filter(r -> filterByList(r.getGender(), request.getGenders()))
                .filter(r -> filterByList(r.getAgeGroup(), request.getAgeGroups()))
                .filter(r -> filterByList(r.getRace(), request.getRaces()))
                .filter(r -> filterByList(r.getAgedBlindDisabled(), request.getAgedBlindDisabled()))
                .filter(r -> filterByList(r.getCaseType(), request.getCaseTypes()))
                .filter(r -> request.getSeverelyImpaired() == null ||
                            r.getSeverelyImpaired() == request.getSeverelyImpaired())
                .collect(Collectors.toList());
    }

    private boolean filterByList(String value, List<String> filterValues) {
        if (filterValues == null || filterValues.isEmpty()) {
            return true;
        }
        return filterValues.contains(value);
    }

    private List<DashboardTableRow> buildTableData(List<DemographicRecord> records,
                                                    DashboardFilterRequest request) {
        // Group records by selected dimensions
        List<String> dimensions = getActiveDimensions(request);

        if (dimensions.isEmpty()) {
            // No dimensions selected, aggregate all
            return records.stream()
                    .map(this::recordToTableRow)
                    .collect(Collectors.toList());
        }

        // Group and aggregate based on dimensions
        Map<String, List<DemographicRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(r -> buildGroupKey(r, dimensions)));

        return grouped.entrySet().stream()
                .map(entry -> aggregateRecords(entry.getValue(), dimensions))
                .sorted(Comparator.comparing(DashboardTableRow::getCount).reversed())
                .collect(Collectors.toList());
    }

    private List<String> getActiveDimensions(DashboardFilterRequest request) {
        List<String> dimensions = new ArrayList<>();
        if (request.getDimension1() != null && !request.getDimension1().equals("None")) {
            dimensions.add(request.getDimension1());
        }
        if (request.getDimension2() != null && !request.getDimension2().equals("None")) {
            dimensions.add(request.getDimension2());
        }
        if (request.getDimension3() != null && !request.getDimension3().equals("None")) {
            dimensions.add(request.getDimension3());
        }
        if (request.getDimension4() != null && !request.getDimension4().equals("None")) {
            dimensions.add(request.getDimension4());
        }
        if (request.getDimension5() != null && !request.getDimension5().equals("None")) {
            dimensions.add(request.getDimension5());
        }
        if (request.getDimension6() != null && !request.getDimension6().equals("None")) {
            dimensions.add(request.getDimension6());
        }
        if (request.getDimension7() != null && !request.getDimension7().equals("None")) {
            dimensions.add(request.getDimension7());
        }
        if (request.getDimension8() != null && !request.getDimension8().equals("None")) {
            dimensions.add(request.getDimension8());
        }
        return dimensions;
    }

    private String buildGroupKey(DemographicRecord record, List<String> dimensions) {
        StringBuilder key = new StringBuilder();
        for (String dim : dimensions) {
            key.append(getDimensionValue(record, dim)).append("|");
        }
        return key.toString();
    }

    private String getDimensionValue(DemographicRecord record, String dimension) {
        return switch (dimension.toLowerCase()) {
            case "race" -> record.getRace();
            case "ethnicity" -> record.getEthnicity();
            case "gender" -> record.getGender();
            case "agegroup", "age group" -> record.getAgeGroup();
            case "agedblindisabled", "aged, blind, disabled" -> record.getAgedBlindDisabled();
            case "county" -> record.getCountyName();
            case "casetype", "case type" -> record.getCaseType();
            default -> "";
        };
    }

    private DashboardTableRow aggregateRecords(List<DemographicRecord> records, List<String> dimensions) {
        DemographicRecord first = records.get(0);

        long totalCount = records.stream()
                .mapToLong(r -> r.getIndividualCount() != null ? r.getIndividualCount() : 0)
                .sum();

        long totalPopulation = records.stream()
                .mapToLong(r -> r.getCountyPopulation() != null ? r.getCountyPopulation() : 0)
                .sum();

        double totalHours = records.stream()
                .mapToDouble(r -> r.getAuthorizedHours() != null ? r.getAuthorizedHours() : 0)
                .sum();

        long totalAppsReceived = records.stream()
                .mapToLong(r -> r.getApplicationsReceived() != null ? r.getApplicationsReceived() : 0)
                .sum();

        long totalAppsApproved = records.stream()
                .mapToLong(r -> r.getApplicationsApproved() != null ? r.getApplicationsApproved() : 0)
                .sum();

        long totalAppsDenied = records.stream()
                .mapToLong(r -> r.getApplicationsDenied() != null ? r.getApplicationsDenied() : 0)
                .sum();

        double avgErrorRate = records.stream()
                .mapToDouble(r -> r.getActiveErrorRate() != null ? r.getActiveErrorRate() : 0)
                .average()
                .orElse(0);

        return DashboardTableRow.builder()
                .race(dimensions.contains("Race") ? first.getRace() : null)
                .ethnicity(dimensions.contains("Ethnicity") ? first.getEthnicity() : null)
                .gender(dimensions.contains("Gender") ? first.getGender() : null)
                .ageGroup(dimensions.contains("Age Group") ? first.getAgeGroup() : null)
                .agedBlindDisabled(dimensions.contains("Aged, Blind, Disabled") ? first.getAgedBlindDisabled() : null)
                .caseType(dimensions.contains("Case Type") ? first.getCaseType() : null)
                .countyName(dimensions.contains("County") ? first.getCountyName() : null)
                .count(totalCount)
                .countyPopulation(totalPopulation)
                .authorizedHours(Math.round(totalHours * 100.0) / 100.0)
                .perCapitaRate(totalPopulation > 0 ? Math.round((double) totalCount / totalPopulation * 1000 * 100.0) / 100.0 : 0)
                .applicationsReceived(totalAppsReceived)
                .applicationsApproved(totalAppsApproved)
                .applicationsDenied(totalAppsDenied)
                .activeErrorRate(Math.round(avgErrorRate * 100.0) / 100.0)
                .build();
    }

    private DashboardTableRow recordToTableRow(DemographicRecord record) {
        return DashboardTableRow.builder()
                .race(record.getRace())
                .ethnicity(record.getEthnicity())
                .gender(record.getGender())
                .ageGroup(record.getAgeGroup())
                .agedBlindDisabled(record.getAgedBlindDisabled())
                .caseType(record.getCaseType())
                .countyName(record.getCountyName())
                .count(record.getIndividualCount())
                .countyPopulation(record.getCountyPopulation())
                .authorizedHours(record.getAuthorizedHours())
                .perCapitaRate(record.getPerCapitaRate())
                .applicationsReceived(record.getApplicationsReceived())
                .applicationsApproved(record.getApplicationsApproved())
                .applicationsDenied(record.getApplicationsDenied())
                .activeErrorRate(record.getActiveErrorRate())
                .build();
    }

    public FilterOptions getFilterOptions() {
        return FilterOptions.builder()
                .counties(demographicRecordRepository.findDistinctCountyCodes())
                .ethnicities(demographicRecordRepository.findDistinctEthnicities())
                .genders(demographicRecordRepository.findDistinctGenders())
                .ageGroups(demographicRecordRepository.findDistinctAgeGroups())
                .races(demographicRecordRepository.findDistinctRaces())
                .agedBlindDisabled(demographicRecordRepository.findDistinctAgedBlindDisabled())
                .caseTypes(demographicRecordRepository.findDistinctCaseTypes())
                .dimensions(Arrays.asList(
                        "None", "Race", "Ethnicity", "Gender", "Age Group",
                        "Aged, Blind, Disabled", "County", "Case Type"
                ))
                .measures(Arrays.asList(
                        "(All)", "Active Error Rate", "Active Error Rate - County",
                        "Active Error Rate - Regional", "All Federal Persons",
                        "All SOC Apps submitted", "All Non-Assistance Hours",
                        "All Non-Assistance Persons", "All Public Assistance Hours",
                        "All Public Assistance Persons", "Application Denial Rate",
                        "Applications Approved", "Applications Denied",
                        "Applications Received", "Applications Submitted",
                        "Authorized Hours", "County Population", "Individual Count",
                        "Per Capita Rate"
                ))
                .build();
    }
}
