package com.cmips.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for mapping county codes (CT1-CT5) to county names and vice versa.
 * Centralizes county code mapping logic for the application.
 */
@Service
public class CountyCodeMappingService {
    
    // County Code to County Name Mapping
    private static final Map<String, String> COUNTY_CODE_TO_NAME = Map.of(
        "CT1", "Orange",
        "CT2", "Sacramento",
        "CT3", "Riverside",
        "CT4", "Los Angeles",
        "CT5", "Alameda"
    );
    
    // County Name to County Code Mapping (reverse lookup)
    private static final Map<String, String> COUNTY_NAME_TO_CODE;
    
    static {
        Map<String, String> reverseMap = new HashMap<>();
        for (Map.Entry<String, String> entry : COUNTY_CODE_TO_NAME.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }
        COUNTY_NAME_TO_CODE = Collections.unmodifiableMap(reverseMap);
    }
    
    // List of all valid county codes
    private static final List<String> ALL_COUNTY_CODES = List.of("CT1", "CT2", "CT3", "CT4", "CT5");
    
    /**
     * Get county name from county code
     * @param countyCode County code (CT1, CT2, CT3, CT4, CT5)
     * @return County name (e.g., "Orange") or null if code is invalid
     */
    public String getCountyName(String countyCode) {
        if (countyCode == null) {
            return null;
        }
        String normalized = countyCode.trim().toUpperCase();
        return COUNTY_CODE_TO_NAME.get(normalized);
    }
    
    /**
     * Get county code from county name
     * @param countyName County name (e.g., "Orange", "Sacramento")
     * @return County code (e.g., "CT1") or null if name is not mapped
     */
    public String getCountyCode(String countyName) {
        if (countyName == null) {
            return null;
        }
        String normalized = countyName.trim();
        return COUNTY_NAME_TO_CODE.get(normalized);
    }
    
    /**
     * Get all valid county codes
     * @return List of county codes [CT1, CT2, CT3, CT4, CT5]
     */
    public List<String> getAllCountyCodes() {
        return new ArrayList<>(ALL_COUNTY_CODES);
    }
    
    /**
     * Check if a county code is valid
     * @param countyCode County code to validate
     * @return true if code is valid (CT1-CT5), false otherwise
     */
    public boolean isValidCountyCode(String countyCode) {
        if (countyCode == null) {
            return false;
        }
        String normalized = countyCode.trim().toUpperCase();
        return COUNTY_CODE_TO_NAME.containsKey(normalized);
    }
    
    /**
     * Extract county code from username pattern: {role}_{countyCode}
     * Examples: caseworker_CT1 -> CT1, supervisor_CT2 -> CT2
     * @param username Username to parse
     * @return County code if found, null otherwise
     */
    public String extractCountyCodeFromUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        String normalized = username.trim().toUpperCase(); // Convert to uppercase for case-insensitive matching
        
        // Check if username ends with a county code pattern (CT1-CT5) - case insensitive
        for (String code : ALL_COUNTY_CODES) {
            if (normalized.endsWith("_" + code) || normalized.endsWith(code)) {
                return code; // Return the canonical uppercase code
            }
        }
        
        return null;
    }
    
    /**
     * Extract role from username pattern: {role}_{countyCode}
     * Examples: caseworker_CT1 -> caseworker, supervisor_CT2 -> supervisor
     * @param username Username to parse
     * @return Role prefix if found, null otherwise
     */
    public String extractRoleFromUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        String normalized = username.trim();
        
        // Find county code in username and extract role prefix
        for (String code : ALL_COUNTY_CODES) {
            if (normalized.endsWith("_" + code)) {
                return normalized.substring(0, normalized.length() - code.length() - 1);
            }
            if (normalized.endsWith(code)) {
                return normalized.substring(0, normalized.length() - code.length());
            }
        }
        
        return normalized; // Return full username if no county code found
    }
    
    /**
     * Get all county names for the configured county codes
     * @return List of county names [Orange, Sacramento, Riverside, Los Angeles, Alameda]
     */
    public List<String> getAllCountyNames() {
        return new ArrayList<>(COUNTY_NAME_TO_CODE.keySet());
    }
}

