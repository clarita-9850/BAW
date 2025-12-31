package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Locale;

/**
 * Simple Multi-Worker Controller
 * Demonstrates multiple workers per county and district without complex dependencies
 */
@RestController
@RequestMapping("/api/simple-multi-worker")
@CrossOrigin(origins = "*")
public class SimpleMultiWorkerController {

    public SimpleMultiWorkerController() {
        System.out.println("🔧 SimpleMultiWorkerController: Constructor called - initializing...");
        try {
            System.out.println("✅ SimpleMultiWorkerController: Constructor completed successfully");
        } catch (Exception e) {
            System.err.println("❌ SimpleMultiWorkerController: Constructor failed with error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get worker statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getWorkerStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("📊 Getting multi-worker statistics...");
            
            // Simulate multiple workers per county
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalWorkers", 50);
            stats.put("adminUsers", 2);
            stats.put("supervisors", 6);
            stats.put("caseWorkers", 12);
            stats.put("providers", 18);
            stats.put("recipients", 12);
            
            Map<String, Integer> countyDistribution = new HashMap<>();
            countyDistribution.put("Alameda", 5);
            countyDistribution.put("San Francisco", 5);
            countyDistribution.put("Santa Clara", 5);
            countyDistribution.put("Orange", 5);
            countyDistribution.put("Los Angeles", 5);
            countyDistribution.put("San Diego", 5);
            stats.put("countyDistribution", countyDistribution);
            
            response.put("status", "SUCCESS");
            response.put("message", "Multi-worker statistics retrieved successfully");
            response.put("statistics", stats);
            
            System.out.println("✅ Multi-worker statistics retrieved successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting multi-worker statistics: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get multi-worker statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get workers for a specific county
     */
    @GetMapping("/workers/county/{countyId}")
    public ResponseEntity<Map<String, Object>> getWorkersForCounty(@PathVariable String countyId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("👥 Getting workers for county: " + countyId);
            
            String normalizedCounty = normalizeCountyParam(countyId);
            
            if (normalizedCounty == null) {
                response.put("status", "NOT_FOUND");
                response.put("message", "County not found: " + countyId);
                return ResponseEntity.ok(response);
            }

            List<Map<String, Object>> workers = buildWorkersForCounty(normalizedCounty);
            
            response.put("status", "SUCCESS");
            response.put("message", "Retrieved workers for county " + countyId);
            response.put("countyId", countyId);
            response.put("totalWorkers", workers.size());
            response.put("workers", workers);
            
            System.out.println("✅ Retrieved " + workers.size() + " workers for county " + countyId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting workers for county: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get workers for county: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get role hierarchy
     */
    @GetMapping("/hierarchy")
    public ResponseEntity<Map<String, Object>> getRoleHierarchy() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🏗️ Getting role hierarchy...");
            
            Map<String, Object> hierarchy = new HashMap<>();
            
            // Central level
            List<Map<String, Object>> centralWorkers = new ArrayList<>();
            centralWorkers.add(createWorker("CW001", "Alice Johnson", UserRole.ADMIN, null));
            centralWorkers.add(createWorker("CW002", "Bob Smith", UserRole.ADMIN, null));
            hierarchy.put("central", centralWorkers);
            
            // Supervisor regions (formerly districts)
            Map<String, Object> supervisorRegions = new LinkedHashMap<>();
            supervisorRegions.put("Northern Region", buildWorkersForCounties(Arrays.asList("Alameda", "San Francisco", "Santa Clara")));
            supervisorRegions.put("Central Region", buildWorkersForCounties(Arrays.asList("Orange", "Los Angeles")));
            supervisorRegions.put("Southern Region", buildWorkersForCounties(Arrays.asList("Riverside", "San Diego")));
            hierarchy.put("supervisorRegions", supervisorRegions);
            
            // County level
            Map<String, Object> counties = new LinkedHashMap<>();
            List<String> canonicalCounties = Arrays.asList("Alameda", "San Francisco", "Santa Clara", "Orange", "Los Angeles", "San Diego");
            canonicalCounties.forEach(county -> counties.put(county, buildWorkersForCounty(county)));
            hierarchy.put("counties", counties);
            
            response.put("status", "SUCCESS");
            response.put("message", "Retrieved role hierarchy successfully");
            response.put("hierarchy", hierarchy);
            
            System.out.println("✅ Role hierarchy retrieved successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting role hierarchy: " + e.getMessage());
            e.printStackTrace();
            
            response.put("status", "ERROR");
            response.put("message", "Failed to get role hierarchy: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private List<Map<String, Object>> buildWorkersForCounties(List<String> counties) {
        List<Map<String, Object>> aggregated = new ArrayList<>();
        if (counties == null) {
            return aggregated;
        }
        counties.stream()
            .map(this::buildWorkersForCounty)
            .forEach(aggregated::addAll);
        return aggregated;
    }

    private List<Map<String, Object>> buildWorkersForCounty(String normalizedCounty) {
        List<Map<String, Object>> workers = new ArrayList<>();
        if (normalizedCounty == null) {
            return workers;
        }

        switch (normalizedCounty) {
            case "Alameda" -> {
                workers.add(createWorker("CW_A_001", "Alice Johnson", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("CW_A_002", "Bob Smith", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("PROV_A_001", "Carol Williams", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_A_002", "David Brown", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_A_003", "Eve Davis", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("REC_A_001", "Frank Miller", UserRole.RECIPIENT, normalizedCounty));
                workers.add(createWorker("REC_A_002", "Grace Wilson", UserRole.RECIPIENT, normalizedCounty));
            }
            case "San Francisco" -> {
                workers.add(createWorker("CW_B_001", "Henry Taylor", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("CW_B_002", "Ivy Anderson", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("PROV_B_001", "Jack Thomas", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_B_002", "Kevin Martinez", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_B_003", "Lisa Garcia", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("REC_B_001", "Mike Rodriguez", UserRole.RECIPIENT, normalizedCounty));
                workers.add(createWorker("REC_B_002", "Nancy Lee", UserRole.RECIPIENT, normalizedCounty));
            }
            case "Santa Clara" -> {
                workers.add(createWorker("CW_C_001", "Olivia White", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("CW_C_002", "Paul Harris", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("PROV_C_001", "Quinn Clark", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_C_002", "Rachel Lewis", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_C_003", "Sam Walker", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("REC_C_001", "Tina Hall", UserRole.RECIPIENT, normalizedCounty));
                workers.add(createWorker("REC_C_002", "Uma Young", UserRole.RECIPIENT, normalizedCounty));
            }
            case "Orange" -> {
                workers.add(createWorker("CW_D_001", "Victor King", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("CW_D_002", "Wendy Wright", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("PROV_D_001", "Xavier Lopez", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_D_002", "Yara Hill", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_D_003", "Zoe Scott", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("REC_D_001", "Aaron Green", UserRole.RECIPIENT, normalizedCounty));
                workers.add(createWorker("REC_D_002", "Betty Adams", UserRole.RECIPIENT, normalizedCounty));
            }
            case "Los Angeles" -> {
                workers.add(createWorker("CW_E_001", "Carl Baker", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("CW_E_002", "Diana Nelson", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("PROV_E_001", "Eva Carter", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_E_002", "Frank Mitchell", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_E_003", "Grace Perez", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("REC_E_001", "Henry Roberts", UserRole.RECIPIENT, normalizedCounty));
                workers.add(createWorker("REC_E_002", "Ivy Turner", UserRole.RECIPIENT, normalizedCounty));
            }
            case "San Diego" -> {
                workers.add(createWorker("CW_F_001", "Jack Phillips", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("CW_F_002", "Kevin Campbell", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("PROV_F_001", "Lisa Parker", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_F_002", "Mike Evans", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_F_003", "Nancy Edwards", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("REC_F_001", "Olivia Collins", UserRole.RECIPIENT, normalizedCounty));
                workers.add(createWorker("REC_F_002", "Paul Stewart", UserRole.RECIPIENT, normalizedCounty));
            }
            case "Riverside" -> {
                workers.add(createWorker("CW_G_001", "Devin Miles", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("CW_G_002", "Sophia Reed", UserRole.CASE_WORKER, normalizedCounty));
                workers.add(createWorker("PROV_G_001", "Ethan Ward", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("PROV_G_002", "Isabella Price", UserRole.PROVIDER, normalizedCounty));
                workers.add(createWorker("REC_G_001", "Logan Carter", UserRole.RECIPIENT, normalizedCounty));
                workers.add(createWorker("REC_G_002", "Maya Brooks", UserRole.RECIPIENT, normalizedCounty));
            }
            default -> workers.add(createWorker("CW_DEFAULT_001", "Default Case Worker", UserRole.CASE_WORKER, normalizedCounty));
        }

        return workers;
    }

    /**
     * Create a worker object
     */
    private Map<String, Object> createWorker(String roleId, String userName, UserRole roleType, String assignedLocation) {
        Map<String, Object> worker = new HashMap<>();
        worker.put("roleId", roleId);
        worker.put("userName", userName);
        worker.put("roleType", roleType.name());
        worker.put("assignedLocation", assignedLocation);
        worker.put("userEmail", userName.toLowerCase().replace(" ", ".") + "@" + 
                   (assignedLocation != null ? assignedLocation.toLowerCase() : "central") + ".gov");
        
        // Set access levels based on role type
        switch (roleType) {
            case ADMIN -> {
                worker.put("hasFullAccess", true);
                worker.put("hasFinancialAccess", true);
                worker.put("hasProjectAccess", true);
                worker.put("accessibleCounties", Arrays.asList(
                        "Alameda", "San Francisco", "Santa Clara", "Orange", "Los Angeles", "San Diego"));
            }
            case SUPERVISOR -> {
                worker.put("hasFullAccess", false);
                worker.put("hasFinancialAccess", true);
                worker.put("hasProjectAccess", true);
                if ("Northern Region".equals(assignedLocation)) {
                    worker.put("accessibleCounties", Arrays.asList("Alameda", "San Francisco", "Santa Clara"));
                } else if ("Central Region".equals(assignedLocation)) {
                    worker.put("accessibleCounties", Arrays.asList("Orange", "Los Angeles"));
                } else if ("Southern Region".equals(assignedLocation)) {
                    worker.put("accessibleCounties", Arrays.asList("Riverside", "San Diego"));
                } else if (assignedLocation != null) {
                    worker.put("accessibleCounties", Arrays.asList(assignedLocation));
                }
            }
            case CASE_WORKER -> {
                worker.put("hasFullAccess", false);
                worker.put("hasFinancialAccess", false);
                worker.put("hasProjectAccess", true);
                worker.put("accessibleCounties", Arrays.asList(assignedLocation));
            }
            case PROVIDER, RECIPIENT -> {
                worker.put("hasFullAccess", false);
                worker.put("hasFinancialAccess", false);
                worker.put("hasProjectAccess", false);
                worker.put("accessibleCounties", Arrays.asList(assignedLocation));
            }
            case SYSTEM_SCHEDULER -> {
                worker.put("hasFullAccess", false);
                worker.put("hasFinancialAccess", false);
                worker.put("hasProjectAccess", true);
                worker.put("accessibleCounties", Arrays.asList("Alameda"));
            }
        }
        
        return worker;
    }

    private String normalizeCountyParam(String countyId) {
        if (countyId == null) {
            return null;
        }
        String upper = countyId.trim().toUpperCase(Locale.ROOT);
        switch (upper) {
            case "CT5", "ALAMEDA", "ALAMEDA COUNTY":
                return "Alameda";
            case "CT1", "ORANGE", "ORANGE COUNTY":
                return "Orange";
            case "CT2", "SACRAMENTO", "SACRAMENTO COUNTY":
                return "Sacramento";
            case "CT3", "RIVERSIDE", "RIVERSIDE COUNTY":
                return "Riverside";
            case "CT4", "LOS ANGELES", "LOS ANGELES COUNTY":
                return "Los Angeles";
            default:
                return countyId.trim();
        }
    }
}
