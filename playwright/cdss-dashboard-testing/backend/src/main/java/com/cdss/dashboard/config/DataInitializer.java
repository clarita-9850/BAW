package com.cdss.dashboard.config;

import com.cdss.dashboard.entity.County;
import com.cdss.dashboard.entity.DemographicRecord;
import com.cdss.dashboard.repository.CountyRepository;
import com.cdss.dashboard.repository.DemographicRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final DemographicRecordRepository demographicRecordRepository;
    private final CountyRepository countyRepository;

    private final String[] COUNTIES = {
            "Alameda", "Alpine", "Amador", "Butte", "Calaveras", "Colusa", "Contra Costa",
            "Del Norte", "El Dorado", "Fresno", "Glenn", "Humboldt", "Imperial", "Inyo",
            "Kern", "Kings", "Lake", "Lassen", "Los Angeles", "Madera", "Marin", "Mariposa",
            "Mendocino", "Merced", "Modoc", "Mono", "Monterey", "Napa", "Nevada", "Orange",
            "Placer", "Plumas", "Riverside", "Sacramento", "San Benito", "San Bernardino",
            "San Diego", "San Francisco", "San Joaquin", "San Luis Obispo", "San Mateo",
            "Santa Barbara", "Santa Clara", "Santa Cruz", "Shasta", "Sierra", "Siskiyou",
            "Solano", "Sonoma", "Stanislaus", "Sutter", "Tehama", "Trinity", "Tulare",
            "Tuolumne", "Ventura", "Yolo", "Yuba"
    };

    private final Map<String, Long> COUNTY_POPULATIONS = Map.ofEntries(
            Map.entry("Los Angeles", 10014009L),
            Map.entry("San Diego", 3298634L),
            Map.entry("Orange", 3186989L),
            Map.entry("Riverside", 2470546L),
            Map.entry("San Bernardino", 2181654L),
            Map.entry("Santa Clara", 1936259L),
            Map.entry("Alameda", 1682353L),
            Map.entry("Sacramento", 1552058L),
            Map.entry("Contra Costa", 1153526L),
            Map.entry("Fresno", 1008654L),
            Map.entry("Kern", 900202L),
            Map.entry("San Francisco", 873965L),
            Map.entry("Ventura", 846006L),
            Map.entry("San Mateo", 764442L),
            Map.entry("San Joaquin", 762148L)
    );

    private final String[] ETHNICITIES = {"Asian", "Black or African American", "Hispanic or Latino", "White", "Other"};
    private final String[] GENDERS = {"Male", "Female"};
    private final String[] AGE_GROUPS = {"0-17", "18-64", "65-74", "75+"};
    private final String[] RACES = {"Asian", "Black or African American", "White", "American Indian", "Pacific Islander", "Other"};
    private final String[] ABD_CATEGORIES = {"Aged", "Blind", "Disabled"};
    private final String[] CASE_TYPES = {"PM", "PS"};

    @Override
    public void run(String... args) {
        log.info("Initializing dummy data for CDSS Dashboard Testing...");

        // Initialize counties
        initializeCounties();

        // Initialize demographic records
        initializeDemographicRecords();

        log.info("Data initialization complete. Total records: {}", demographicRecordRepository.count());
    }

    private void initializeCounties() {
        if (countyRepository.count() > 0) {
            return;
        }

        List<County> counties = new ArrayList<>();
        for (int i = 0; i < COUNTIES.length; i++) {
            String countyName = COUNTIES[i];
            counties.add(County.builder()
                    .countyCode(String.format("%02d", i + 1))
                    .countyName(countyName)
                    .population(COUNTY_POPULATIONS.getOrDefault(countyName, 100000L + new Random().nextInt(500000)))
                    .region(getRegion(countyName))
                    .build());
        }
        countyRepository.saveAll(counties);
        log.info("Initialized {} counties", counties.size());
    }

    private String getRegion(String countyName) {
        List<String> southern = Arrays.asList("Los Angeles", "San Diego", "Orange", "Riverside", "San Bernardino", "Ventura", "Imperial");
        List<String> bayArea = Arrays.asList("San Francisco", "Alameda", "Contra Costa", "San Mateo", "Santa Clara", "Marin", "Sonoma", "Napa", "Solano");
        if (southern.contains(countyName)) return "Southern";
        if (bayArea.contains(countyName)) return "Bay Area";
        return "Other";
    }

    private void initializeDemographicRecords() {
        if (demographicRecordRepository.count() > 0) {
            return;
        }

        Random random = new Random(42); // Seed for reproducibility
        List<DemographicRecord> records = new ArrayList<>();

        // Generate records for major counties with all demographic combinations
        String[] majorCounties = {"Los Angeles", "San Diego", "Sacramento", "San Francisco", "Fresno", "Alameda", "Orange", "Riverside"};

        for (String countyName : majorCounties) {
            String countyCode = String.format("%02d", Arrays.asList(COUNTIES).indexOf(countyName) + 1);
            Long countyPopulation = COUNTY_POPULATIONS.getOrDefault(countyName, 500000L);

            for (String ethnicity : ETHNICITIES) {
                for (String gender : GENDERS) {
                    for (String ageGroup : AGE_GROUPS) {
                        for (String abd : ABD_CATEGORIES) {
                            for (String caseType : CASE_TYPES) {
                                // Generate realistic-looking data
                                long baseCount = 100 + random.nextInt(5000);
                                double ethnicityMultiplier = getEthnicityMultiplier(ethnicity, countyName);
                                double ageMultiplier = getAgeMultiplier(ageGroup);
                                double abdMultiplier = getAbdMultiplier(abd);

                                long individualCount = (long) (baseCount * ethnicityMultiplier * ageMultiplier * abdMultiplier);

                                records.add(DemographicRecord.builder()
                                        .countyCode(countyCode)
                                        .countyName(countyName)
                                        .race(getRaceFromEthnicity(ethnicity))
                                        .ethnicity(ethnicity)
                                        .gender(gender)
                                        .ageGroup(ageGroup)
                                        .agedBlindDisabled(abd)
                                        .severelyImpaired(random.nextDouble() < 0.15)
                                        .evvStatus(random.nextDouble() < 0.7 ? "Verified" : "Pending")
                                        .caseType(caseType)
                                        .individualCount(individualCount)
                                        .countyPopulation(countyPopulation)
                                        .authorizedHours(individualCount * (50 + random.nextDouble() * 150))
                                        .perCapitaRate((double) individualCount / countyPopulation * 1000)
                                        .applicationsReceived((long) (individualCount * 0.3) + random.nextInt(100))
                                        .applicationsApproved((long) (individualCount * 0.25) + random.nextInt(80))
                                        .applicationsDenied((long) (individualCount * 0.05) + random.nextInt(20))
                                        .activeErrorRate(random.nextDouble() * 5)
                                        .federalPersons((long) (individualCount * 0.4))
                                        .nonAssistanceHours(individualCount * random.nextDouble() * 30)
                                        .nonAssistancePersons((long) (individualCount * 0.3))
                                        .publicAssistanceHours(individualCount * random.nextDouble() * 50)
                                        .publicAssistancePersons((long) (individualCount * 0.6))
                                        .build());
                            }
                        }
                    }
                }
            }
        }

        // Save in batches
        int batchSize = 500;
        for (int i = 0; i < records.size(); i += batchSize) {
            int end = Math.min(i + batchSize, records.size());
            demographicRecordRepository.saveAll(records.subList(i, end));
            log.info("Saved records {} to {}", i, end);
        }

        log.info("Initialized {} demographic records", records.size());
    }

    private double getEthnicityMultiplier(String ethnicity, String county) {
        // Simulate realistic demographic distribution
        return switch (ethnicity) {
            case "Hispanic or Latino" -> county.equals("Los Angeles") ? 1.5 : 1.0;
            case "Asian" -> county.equals("San Francisco") || county.equals("Alameda") ? 1.4 : 0.8;
            case "White" -> 1.0;
            case "Black or African American" -> county.equals("Alameda") || county.equals("Sacramento") ? 1.2 : 0.7;
            default -> 0.5;
        };
    }

    private double getAgeMultiplier(String ageGroup) {
        return switch (ageGroup) {
            case "0-17" -> 0.6;
            case "18-64" -> 1.0;
            case "65-74" -> 1.3;
            case "75+" -> 1.5;
            default -> 1.0;
        };
    }

    private double getAbdMultiplier(String abd) {
        return switch (abd) {
            case "Aged" -> 1.2;
            case "Blind" -> 0.3;
            case "Disabled" -> 1.5;
            default -> 1.0;
        };
    }

    private String getRaceFromEthnicity(String ethnicity) {
        return switch (ethnicity) {
            case "Asian" -> "Asian";
            case "Black or African American" -> "Black or African American";
            case "Hispanic or Latino" -> "White"; // Hispanic is ethnicity, not race
            case "White" -> "White";
            default -> "Other";
        };
    }
}
