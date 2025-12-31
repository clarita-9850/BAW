package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.model.County;
import com.example.kafkaeventdrivenapp.model.UserAccess;
import com.example.kafkaeventdrivenapp.model.UserRole;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DistrictCountyService {

    private final CountyMappingService countyMappingService;
    private final Map<UserRole, UserAccess> userAccessMap = new EnumMap<>(UserRole.class);

    public DistrictCountyService(CountyMappingService countyMappingService) {
        this.countyMappingService = countyMappingService;
        initializeUserAccess();
    }

    private void initializeUserAccess() {
        List<String> allCounties = countyMappingService.getAllCounties();

        userAccessMap.put(UserRole.ADMIN, new UserAccess(
            "admin_default",
            UserRole.ADMIN.name(),
            null,
            allCounties,
            true
        ));

        userAccessMap.put(UserRole.SYSTEM_SCHEDULER, new UserAccess(
            "system_scheduler_default",
            UserRole.SYSTEM_SCHEDULER.name(),
            null,
            allCounties,
            true
        ));

        // SUPERVISOR does NOT have full access - each supervisor is limited to their assigned county from JWT token
        userAccessMap.put(UserRole.SUPERVISOR, new UserAccess(
            "supervisor_default",
            UserRole.SUPERVISOR.name(),
            null,
            Collections.emptyList(), // Will be determined from JWT token countyId
            false
        ));

        // Sample default assignments for other roles; these can be overridden at runtime.
        userAccessMap.put(UserRole.CASE_WORKER, new UserAccess(
            "case_worker_default",
            UserRole.CASE_WORKER.name(),
            "Alameda",
            List.of("Alameda"),
            false
        ));

        userAccessMap.put(UserRole.PROVIDER, new UserAccess(
            "provider_default",
            UserRole.PROVIDER.name(),
            "Los Angeles",
            List.of("Los Angeles"),
            false
        ));

        userAccessMap.put(UserRole.RECIPIENT, new UserAccess(
            "recipient_default",
            UserRole.RECIPIENT.name(),
            "San Diego",
            List.of("San Diego"),
            false
        ));
    }

    public List<County> getAllCounties() {
        List<County> result = new ArrayList<>();
        for (String county : countyMappingService.getAllCounties()) {
            result.add(new County(toCountyId(county), county + " County", "CA", true));
        }
        return result;
    }

    public UserAccess getUserAccess(String userRole) {
        UserRole role = UserRole.from(userRole);
        UserAccess access = userAccessMap.get(role);
        if (access == null) {
            return null;
        }
        return new UserAccess(
            access.getUserId(),
            access.getUserRole(),
            access.getAssignedCounty(),
            new ArrayList<>(access.getAccessibleCounties()),
            access.isHasFullAccess()
        );
    }

    public boolean canAccessCounty(String userRole, String countyName) {
        UserAccess access = userAccessMap.get(UserRole.from(userRole));
        if (access == null) {
            return false;
        }
        if (access.isHasFullAccess()) {
            return countyMappingService.isKnownCounty(countyName);
        }
        String canonical = countyMappingService.normalizeCountyName(countyName);
        return canonical != null && access.getAccessibleCounties().contains(canonical);
    }

    public List<String> getAccessibleCounties(String userRole) {
        return getAccessibleCounties(userRole, null);
    }
    
    public List<String> getAccessibleCounties(String userRole, String userCounty) {
        UserRole role = UserRole.from(userRole);
        UserAccess access = userAccessMap.get(role);
        if (access == null) {
            return Collections.emptyList();
        }
        
        // PRIORITY 1: If userCounty is provided from JWT token, return only that county
        // This applies to ALL roles, including ADMIN, to respect user's assigned county
        // SUPERVISOR must have county in JWT token (no full access)
        if (userCounty != null && !userCounty.trim().isEmpty()) {
            // Normalize the county name
            String normalizedCounty = countyMappingService.normalizeCountyName(userCounty);
            if (normalizedCounty != null) {
                System.out.println("✅ DistrictCountyService: User has county in JWT (" + userCounty + " -> " + normalizedCounty + "), returning single county for role: " + role);
                return List.of(normalizedCounty);
            }
            // If normalization fails, try to use the county as-is if it's in the list
            List<String> allCounties = countyMappingService.getAllCounties();
            if (allCounties.contains(userCounty)) {
                System.out.println("✅ DistrictCountyService: User has county in JWT (" + userCounty + "), returning single county for role: " + role);
                return List.of(userCounty);
            }
            System.out.println("⚠️ DistrictCountyService: County from JWT (" + userCounty + ") not found in known counties list");
        }
        
        // PRIORITY 2: For CASE_WORKER, PROVIDER, RECIPIENT, SUPERVISOR: if no county extracted, return empty (don't use defaults)
        // SUPERVISOR requires county from JWT token - no full access
        if (role == UserRole.CASE_WORKER || role == UserRole.PROVIDER || role == UserRole.RECIPIENT || role == UserRole.SUPERVISOR) {
            System.out.println("⚠️ DistrictCountyService: No county extracted for " + role + ", returning empty list");
            return Collections.emptyList();
        }
        
        // PRIORITY 3: ADMIN and SYSTEM_SCHEDULER have full access to all counties
        // (only if no county was specified in JWT token)
        if (access.isHasFullAccess()) {
            System.out.println("✅ DistrictCountyService: Role " + role + " has full access, returning all counties");
            return countyMappingService.getAllCounties();
        }
        
        // PRIORITY 4: Fallback to default assignment from userAccessMap (for other roles like DISTRICT_WORKER)
        System.out.println("✅ DistrictCountyService: Returning default accessible counties for role: " + role);
        return new ArrayList<>(access.getAccessibleCounties());
    }

    private String toCountyId(String countyName) {
        return countyName.toLowerCase(Locale.ROOT).replace(" ", "-");
    }
}
