package com.cmips.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Central registry for California county names plus normalization helpers for
 * legacy identifiers used throughout the trial application.
 */
@Service
public class CountyMappingService {

    private static final List<String> CALIFORNIA_COUNTIES = Collections.unmodifiableList(Arrays.asList(
        "Alameda", "Alpine", "Amador", "Butte", "Calaveras", "Colusa", "Contra Costa", "Del Norte",
        "El Dorado", "Fresno", "Glenn", "Humboldt", "Imperial", "Inyo", "Kern", "Kings",
        "Lake", "Lassen", "Los Angeles", "Madera", "Marin", "Mariposa", "Mendocino", "Merced",
        "Modoc", "Mono", "Monterey", "Napa", "Nevada", "Orange", "Placer", "Plumas",
        "Riverside", "Sacramento", "San Benito", "San Bernardino", "San Diego", "San Francisco",
        "San Joaquin", "San Luis Obispo", "San Mateo", "Santa Barbara", "Santa Clara", "Santa Cruz",
        "Shasta", "Sierra", "Siskiyou", "Solano", "Sonoma", "Stanislaus", "Sutter", "Tehama",
        "Trinity", "Tulare", "Tuolumne", "Ventura", "Yolo", "Yuba"
    ));

    private static final Map<String, String> NORMALIZED_NAME_LOOKUP;

    static {
        Map<String, String> lookup = new HashMap<>();

        for (String county : CALIFORNIA_COUNTIES) {
            String upper = county.toUpperCase(Locale.ROOT);
            lookup.put(upper, county);
            lookup.put(upper.replace(" COUNTY", ""), county);
            lookup.put(upper.replace(" ", ""), county);
            lookup.put(upper.replace(" ", "_"), county);
        }

        // County code mappings (CT1-CT5)
        lookup.put("CT1", "Orange");
        lookup.put("CT2", "Sacramento");
        lookup.put("CT3", "Riverside");
        lookup.put("CT4", "Los Angeles");
        lookup.put("CT5", "Alameda");

        NORMALIZED_NAME_LOOKUP = Collections.unmodifiableMap(lookup);
    }

    /**
     * Returns an immutable list of all California counties.
     */
    public List<String> getAllCounties() {
        return CALIFORNIA_COUNTIES;
    }

    /**
     * Normalize any raw county label to its canonical California county name.
     * Returns null when the input is null or blank.
     */
    public String normalizeCountyName(String rawCounty) {
        if (rawCounty == null) {
            return null;
        }

        String trimmed = rawCounty.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        String upper = trimmed.toUpperCase(Locale.ROOT);
        String canonical = NORMALIZED_NAME_LOOKUP.get(upper);
        if (canonical != null) {
            return canonical;
        }

        // Fall back to original casing when we do not recognise the county yet.
        return trimmed;
    }

    /**
     * Normalize and filter an arbitrary list of counties to known California counties.
     */
    public List<String> normalizeCountyList(Collection<String> counties) {
        if (counties == null || counties.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (String county : counties) {
            String canonical = normalizeCountyName(county);
            if (canonical != null && CALIFORNIA_COUNTIES.contains(canonical)) {
                normalized.add(canonical);
            }
        }
        return normalized;
    }

    /**
     * Returns true if the supplied county is a recognised California county (after normalization).
     */
    public boolean isKnownCounty(String county) {
        String canonical = normalizeCountyName(county);
        return canonical != null && CALIFORNIA_COUNTIES.contains(canonical);
    }
}






