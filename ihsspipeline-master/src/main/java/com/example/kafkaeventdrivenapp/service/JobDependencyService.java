package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.config.JobDependencyConfig;
import com.example.kafkaeventdrivenapp.config.JobProcessingProperties;
import com.example.kafkaeventdrivenapp.entity.ReportJobEntity;
import com.example.kafkaeventdrivenapp.model.BIReportRequest;
import com.example.kafkaeventdrivenapp.repository.ReportJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "job-dependencies.enabled", havingValue = "true", matchIfMissing = false)
public class JobDependencyService {
    
    @Autowired
    private JobDependencyConfig dependencyConfig;
    
    @Autowired
    private ReportJobRepository jobRepository;
    
    @Autowired
    private JobQueueService jobQueueService;

    @Autowired
    private JobProcessingProperties jobProcessingProperties;
    
    /**
     * Find and trigger dependent jobs for a completed parent job
     * 
     * @param parentJob The completed parent job
     * @param jwtToken JWT token for dependent job creation
     * @return List of job IDs for created dependent jobs
     */
    public List<String> triggerDependentJobs(ReportJobEntity parentJob, String jwtToken) {
        List<String> triggeredJobIds = new ArrayList<>();
        String parentJobId = parentJob.getJobId();
        
        System.out.println("   [STEP 1] Checking if dependency system is enabled...");
        if (!dependencyConfig.isEnabled()) {
            System.out.println("   ⚠️  Dependency system is disabled in configuration");
            return triggeredJobIds;
        }
        System.out.println("   ✅ Dependency system is enabled");
        
        String parentStatus = parentJob.getStatus();
        String parentReportType = parentJob.getReportType();
        String parentRole = parentJob.getUserRole();
        
        System.out.println("   [STEP 2] Analyzing parent job details:");
        System.out.println("      └─ Parent Job ID: " + parentJobId);
        System.out.println("      └─ Report Type: " + parentReportType);
        System.out.println("      └─ User Role: " + parentRole);
        System.out.println("      └─ Status: " + parentStatus);
        
        System.out.println("   [STEP 3] Searching dependency configurations...");
        // Find matching dependencies
        List<JobDependencyConfig.JobDependency> matchingDependencies = findMatchingDependencies(
            parentReportType, parentRole, parentStatus);
        
        if (matchingDependencies.isEmpty()) {
            System.out.println("   ℹ️  No matching dependencies found");
            System.out.println("      └─ No dependent jobs configured for: " + parentReportType + " / " + parentRole);
            return triggeredJobIds;
        }
        
        System.out.println("   ✅ Found " + matchingDependencies.size() + " matching dependency configuration(s)");
        for (int i = 0; i < matchingDependencies.size(); i++) {
            JobDependencyConfig.JobDependency dep = matchingDependencies.get(i);
            System.out.println("      " + (i + 1) + ". " + dep.getDependentReportType() + 
                            (dep.isMultipleDependency() ? " (multiple dependency)" : " (single dependency)"));
        }
        
        System.out.println("   [STEP 4] Processing each dependency...");
        // Process each matching dependency
        int dependencyIndex = 0;
        for (JobDependencyConfig.JobDependency dependency : matchingDependencies) {
            dependencyIndex++;
            System.out.println("   ┌─ Processing dependency #" + dependencyIndex + ": " + dependency.getDependentReportType());
            
            try {
                if (dependency.isMultipleDependency()) {
                    System.out.println("      └─ Type: Multiple dependency (requires ALL parent jobs to complete)");
                    System.out.println("      └─ Required parent report types: " + dependency.getParentReportTypes());
                    
                    // Check if all required parent jobs have completed successfully
                    System.out.println("      └─ [CHECK] Verifying all required parent jobs have completed...");
                    if (checkMultipleDependencies(dependency, parentRole)) {
                        System.out.println("      ✅ All required parent jobs completed successfully");
                        System.out.println("      └─ [ACTION] Creating dependent job...");
                        
                        BIReportRequest dependentRequest = createDependentJobRequest(dependency, parentJob);
                        String dependentJobId = jobQueueService.queueReportJob(dependentRequest, jwtToken, parentJob.getJobId());
                        triggeredJobIds.add(dependentJobId);
                        
                        System.out.println("      ✅ Dependent job created: " + dependentJobId);
                        System.out.println("         └─ Parent Job ID: " + parentJobId);
                        System.out.println("         └─ Report Type: " + dependentRequest.getReportType());
                        System.out.println("         └─ User Role: " + dependentRequest.getUserRole());
                    } else {
                        System.out.println("      ⏳ Not all required parent jobs have completed yet");
                        System.out.println("         └─ Waiting for remaining parent jobs to complete");
                        System.out.println("         └─ Dependent job will be triggered when all parents complete");
                    }
                } else {
                    System.out.println("      └─ Type: Single dependency (one-to-one)");
                    System.out.println("      └─ Parent report type: " + dependency.getParentReportType());
                    System.out.println("      └─ [ACTION] Creating dependent job...");
                    
                    // Single dependency - create dependent job
                    BIReportRequest dependentRequest = createDependentJobRequest(dependency, parentJob);
                    String dependentJobId = jobQueueService.queueReportJob(dependentRequest, jwtToken, parentJob.getJobId());
                    triggeredJobIds.add(dependentJobId);
                    
                    System.out.println("      ✅ Dependent job created: " + dependentJobId);
                    System.out.println("         └─ Parent Job ID: " + parentJobId);
                    System.out.println("         └─ Report Type: " + dependentRequest.getReportType());
                    System.out.println("         └─ User Role: " + dependentRequest.getUserRole());
                    System.out.println("         └─ Target System: " + dependentRequest.getTargetSystem());
                }
            } catch (Exception e) {
                System.err.println("      ❌ Error creating dependent job for dependency: " + dependency.getDependentReportType());
                System.err.println("         └─ Error: " + e.getMessage());
                e.printStackTrace();
                // Continue with other dependencies even if one fails
            }
            
            System.out.println("   └─ Dependency #" + dependencyIndex + " processing complete");
        }
        
        System.out.println("   [STEP 5] Summary:");
        System.out.println("      └─ Total dependencies processed: " + matchingDependencies.size());
        System.out.println("      └─ Dependent jobs created: " + triggeredJobIds.size());
        
        return triggeredJobIds;
    }
    
    /**
     * Find dependencies that match the parent job
     */
    private List<JobDependencyConfig.JobDependency> findMatchingDependencies(
            String parentReportType, String parentRole, String parentStatus) {
        
        return dependencyConfig.getDependencies().stream()
            .filter(dep -> {
                // Check if condition is met
                if (!dep.shouldTrigger(parentStatus)) {
                    return false;
                }
                
                // For single dependencies
                if (dep.isSingleDependency()) {
                    // Check report type match
                    if (!dep.getParentReportType().equals(parentReportType)) {
                        return false;
                    }
                    
                    // Check role filter if specified
                    if (dep.getParentRole() != null && !dep.getParentRole().equals(parentRole)) {
                        return false;
                    }
                    
                    return true;
                }
                
                // For multiple dependencies, we check them separately in checkMultipleDependencies
                // Here we just check if any of the parent report types match
                if (dep.isMultipleDependency()) {
                    return dep.getParentReportTypes().contains(parentReportType);
                }
                
                return false;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Check if all required parent jobs for a multiple dependency have completed successfully
     */
    private boolean checkMultipleDependencies(JobDependencyConfig.JobDependency dependency, String userRole) {
        if (!dependency.isMultipleDependency()) {
            return false;
        }
        
        List<String> requiredReportTypes = dependency.getParentReportTypes();
        System.out.println("         └─ Checking multiple dependency requirements:");
        System.out.println("            └─ Required report types: " + requiredReportTypes);
        System.out.println("            └─ User role: " + userRole);
        System.out.println("            └─ Required status: COMPLETED");
        
        // Find all completed jobs matching the required report types and role
        List<ReportJobEntity> completedJobs = jobRepository.findByReportTypeInAndUserRoleAndStatus(
            requiredReportTypes, userRole, "COMPLETED");
        
        System.out.println("            └─ Found " + completedJobs.size() + " completed job(s) matching criteria");
        
        // Check if we have at least one completed job for each required report type
        boolean allFound = true;
        for (String reportType : requiredReportTypes) {
            boolean found = completedJobs.stream()
                .anyMatch(job -> reportType.equals(job.getReportType()));
            
            if (found) {
                ReportJobEntity matchingJob = completedJobs.stream()
                    .filter(job -> reportType.equals(job.getReportType()))
                    .findFirst()
                    .orElse(null);
                System.out.println("            ✅ Found completed job for: " + reportType + 
                                 (matchingJob != null ? " (Job ID: " + matchingJob.getJobId() + ")" : ""));
            } else {
                System.out.println("            ❌ Missing completed job for: " + reportType);
                allFound = false;
            }
        }
        
        if (allFound) {
            System.out.println("            ✅ All required parent jobs have completed successfully");
        } else {
            System.out.println("            ⏳ Not all required parent jobs have completed yet");
        }
        
        return allFound;
    }
    
    /**
     * Create a BIReportRequest for a dependent job based on dependency configuration and parent job
     */
    private BIReportRequest createDependentJobRequest(JobDependencyConfig.JobDependency dependency, 
                                                      ReportJobEntity parentJob) {
        BIReportRequest request = new BIReportRequest();
        
        // Set dependent report type
        request.setReportType(dependency.getDependentReportType());
        
        // Set role (use dependent role if specified, otherwise use parent role)
        String dependentRole = dependency.getDependentRole() != null ? 
            dependency.getDependentRole() : parentJob.getUserRole();
        request.setUserRole(dependentRole);
        
        // Set target system (use dependent target system if specified, otherwise use parent's)
        String targetSystem = dependency.getDependentTargetSystem() != null ? 
            dependency.getDependentTargetSystem() : parentJob.getTargetSystem();
        request.setTargetSystem(targetSystem != null ? targetSystem : "SCHEDULED");
        
        // Set data format (use dependent format if specified, otherwise use parent's)
        String dataFormat = dependency.getDependentDataFormat() != null ? 
            dependency.getDependentDataFormat() : parentJob.getDataFormat();
        request.setDataFormat(dataFormat != null ? dataFormat : "JSON");
        
        // Set priority (use dependent priority if specified, otherwise use parent's)
        Integer priority = dependency.getDependentPriority() != null ? 
            dependency.getDependentPriority() : parentJob.getPriority();
        request.setPriority(priority != null ? priority : 5);
        
        // Set chunk size using processing properties (favor dependent override for faster runs)
        request.setChunkSize(jobProcessingProperties.getEffectiveDependentChunkSize(parentJob.getChunkSize()));
        
        // Try to extract date range from parent job's request data
        // For now, we'll use the same date range logic or leave it null
        // In a real scenario, you might want to parse the parent's requestData JSON
        // For simplicity, we'll leave dates as null and let the dependent job use defaults
        
        System.out.println("📋 JobDependencyService: Created dependent job request - " +
                          "type: " + request.getReportType() + 
                          ", role: " + request.getUserRole() + 
                          ", target: " + request.getTargetSystem());
        
        Map<String, Object> metadata = request.getAdditionalFilters() != null
            ? new HashMap<>(request.getAdditionalFilters())
            : new HashMap<>();
        metadata.put("parentJobId", parentJob.getJobId());
        metadata.put("parentReportType", parentJob.getReportType());
        metadata.put("parentRole", parentJob.getUserRole());
        request.setAdditionalFilters(metadata);

        return request;
    }
}


