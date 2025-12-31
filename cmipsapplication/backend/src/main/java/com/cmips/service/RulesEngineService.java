package com.cmips.service;

import com.cmips.model.UserRole;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Service
public class RulesEngineService {

    private static final EnumMap<UserRole, AccessPattern> ROLE_ACCESS_PATTERNS =
            new EnumMap<>(UserRole.class);

    static {
        ROLE_ACCESS_PATTERNS.put(UserRole.ADMIN, new AccessPattern(
            "FULL_ACCESS",
            Set.of("Orange", "Sacramento", "Riverside", "Los Angeles", "Alameda"),
            Set.of("NO_FILTERS"),
            Set.of("NO_MASKING"),
            "UNMASKED_DATA",
            "HIGH_PRIORITY"
        ));

        ROLE_ACCESS_PATTERNS.put(UserRole.SYSTEM_SCHEDULER, new AccessPattern(
            "SYSTEM_ACCESS",
            Set.of("Orange", "Sacramento", "Riverside", "Los Angeles", "Alameda"),
            Set.of("SCHEDULED_JOBS"),
            Set.of("NO_MASKING"),
            "UNMASKED_DATA",
            "HIGH_PRIORITY"
        ));

        ROLE_ACCESS_PATTERNS.put(UserRole.SUPERVISOR, new AccessPattern(
            "DISTRICT_ACCESS",
            Set.of("Orange", "Sacramento", "Riverside", "Los Angeles", "Alameda"),
            Set.of("DISTRICT_FILTER"),
            Set.of("PROVIDER_EMAIL_MASK", "RECIPIENT_NAME_ANONYMIZE"),
            "PARTIALLY_MASKED_DATA",
            "MEDIUM_PRIORITY"
        ));

        ROLE_ACCESS_PATTERNS.put(UserRole.CASE_WORKER, new AccessPattern(
            "COUNTY_ACCESS",
            Set.of("SINGLE_COUNTY"),
            Set.of("COUNTY_FILTER"),
            Set.of("TOTAL_AMOUNT_AGGREGATE"),
            "AGGREGATED_DATA",
            "MEDIUM_PRIORITY"
        ));

        ROLE_ACCESS_PATTERNS.put(UserRole.PROVIDER, new AccessPattern(
            "OWN_RECORDS_ACCESS",
            Set.of("SINGLE_COUNTY"),
            Set.of("OWN_RECORDS_FILTER"),
            Set.of("RECIPIENT_NAME_ANONYMIZE"),
            "OWN_MASKED_DATA",
            "LOW_PRIORITY"
        ));

        ROLE_ACCESS_PATTERNS.put(UserRole.RECIPIENT, new AccessPattern(
            "OWN_RECORDS_ACCESS",
            Set.of("SINGLE_COUNTY"),
            Set.of("OWN_RECORDS_FILTER"),
            Set.of("PROVIDER_NAME_ANONYMIZE"),
            "OWN_MASKED_DATA",
            "LOW_PRIORITY"
        ));
    }

    public AccessPattern determineAccessPattern(String userRole) {
        if (userRole == null || userRole.trim().isEmpty()) {
            throw new IllegalArgumentException("User role cannot be null or empty");
        }

        UserRole role = UserRole.from(userRole);
        AccessPattern pattern = ROLE_ACCESS_PATTERNS.get(role);

        if (pattern == null) {
            throw new IllegalArgumentException("No access pattern found for role: " + role);
        }

        return pattern;
    }

    public static class AccessPattern {
        private final String accessType;
        private final Set<String> accessibleCounties;
        private final Set<String> filters;
        private final Set<String> maskingRules;
        private final String dataVisibilityLevel;
        private final String processingPriority;

        public AccessPattern(String accessType, Set<String> accessibleCounties, Set<String> filters,
                           Set<String> maskingRules, String dataVisibilityLevel, String processingPriority) {
            this.accessType = accessType;
            this.accessibleCounties = accessibleCounties;
            this.filters = filters;
            this.maskingRules = maskingRules;
            this.dataVisibilityLevel = dataVisibilityLevel;
            this.processingPriority = processingPriority;
        }

        public String getAccessType() { return accessType; }
        public Set<String> getAccessibleCounties() { return accessibleCounties; }
        public Set<String> getFilters() { return filters; }
        public Set<String> getMaskingRules() { return maskingRules; }
        public String getDataVisibilityLevel() { return dataVisibilityLevel; }
        public String getProcessingPriority() { return processingPriority; }
    }
}






