package com.example.kafkaeventdrivenapp.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "job-dependencies")
@Data
public class JobDependencyConfig {
    
    private boolean enabled = true;
    private List<JobDependency> dependencies = new ArrayList<>();
    
    @PostConstruct
    public void validateConfiguration() {
        if (!enabled) {
            System.out.println("ℹ️ JobDependencyConfig: Job dependencies are disabled");
            return;
        }
        
        System.out.println("🔍 JobDependencyConfig: Validating " + dependencies.size() + " dependency configuration(s)...");
        
        for (JobDependency dep : dependencies) {
            // Validate that dependency has either single or multiple parent types
            if (!dep.isSingleDependency() && !dep.isMultipleDependency()) {
                System.err.println("⚠️ JobDependencyConfig: Invalid dependency - must specify either parent-report-type or parent-report-types");
                continue;
            }
            
            if (dep.isSingleDependency() && dep.isMultipleDependency()) {
                System.err.println("⚠️ JobDependencyConfig: Invalid dependency - cannot specify both parent-report-type and parent-report-types");
                continue;
            }
            
            if (dep.getDependentReportType() == null || dep.getDependentReportType().isEmpty()) {
                System.err.println("⚠️ JobDependencyConfig: Invalid dependency - dependent-report-type is required");
                continue;
            }
            
            System.out.println("✅ JobDependencyConfig: Valid dependency - " + 
                             (dep.isSingleDependency() ? dep.getParentReportType() : dep.getParentReportTypes()) + 
                             " -> " + dep.getDependentReportType());
        }
        
        System.out.println("✅ JobDependencyConfig: Configuration validation complete");
    }
    
    @Data
    public static class JobDependency {
        // Single parent report type (for one-to-one dependency)
        private String parentReportType;
        
        // Multiple parent report types (for multiple dependencies - ALL must succeed)
        private List<String> parentReportTypes;
        
        // Optional: Filter by parent job role
        private String parentRole;
        
        // Dependent job configuration
        private String dependentReportType;
        private String dependentRole; // Optional: override role
        private String dependentTargetSystem; // Optional: override target system
        private String dependentDataFormat; // Optional: override format
        private Integer dependentPriority; // Optional: override priority
        
        // Condition: ON_SUCCESS (only if parent succeeded) or ON_COMPLETION (always)
        private String condition = "ON_SUCCESS";
        
        // Helper method to check if this is a multiple dependency
        public boolean isMultipleDependency() {
            return parentReportTypes != null && !parentReportTypes.isEmpty();
        }
        
        // Helper method to check if this is a single dependency
        public boolean isSingleDependency() {
            return parentReportType != null && !parentReportType.isEmpty();
        }
        
        // Helper method to check if condition is met
        public boolean shouldTrigger(String parentStatus) {
            if ("ON_COMPLETION".equalsIgnoreCase(condition)) {
                return "COMPLETED".equals(parentStatus) || "FAILED".equals(parentStatus);
            } else {
                // ON_SUCCESS - only trigger if parent succeeded
                return "COMPLETED".equals(parentStatus);
            }
        }
    }
}

