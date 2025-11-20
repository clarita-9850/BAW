package com.cmips.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service for managing predefined work queues
 */
@Service
@Slf4j
public class WorkQueueCatalogService {
    
    // Predefined work queues
    public static final String PROVIDER_MANAGEMENT = "PROVIDER_MANAGEMENT";
    public static final String PAYROLL_APPROVAL = "PAYROLL_APPROVAL";
    public static final String CASE_MANAGEMENT = "CASE_MANAGEMENT";
    public static final String TIMESHEET_REVIEW = "TIMESHEET_REVIEW";
    public static final String ESCALATED = "ESCALATED"; // Supervisor-only queue
    
    /**
     * Get all predefined work queues
     */
    public List<WorkQueueInfo> getAllQueues() {
        return Arrays.asList(
            new WorkQueueInfo(PROVIDER_MANAGEMENT, "Provider Management", "Tasks related to provider onboarding, updates, and management", false),
            new WorkQueueInfo(PAYROLL_APPROVAL, "Payroll Approval", "Tasks related to payroll processing and approval", false),
            new WorkQueueInfo(CASE_MANAGEMENT, "Case Management", "Tasks related to case management and assignments", false),
            new WorkQueueInfo(TIMESHEET_REVIEW, "Timesheet Review", "Tasks related to timesheet review and approval", false),
            new WorkQueueInfo(ESCALATED, "Escalated Tasks", "Tasks that have been escalated and require supervisor attention", true) // Supervisor-only
        );
    }
    
    /**
     * Get queue info by name
     */
    public WorkQueueInfo getQueueInfo(String queueName) {
        return getAllQueues().stream()
            .filter(q -> q.getName().equals(queueName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Check if a queue is supervisor-only
     */
    public boolean isSupervisorOnly(String queueName) {
        WorkQueueInfo info = getQueueInfo(queueName);
        return info != null && info.isSupervisorOnly();
    }
    
    /**
     * Work Queue Information
     */
    public static class WorkQueueInfo {
        private String name;
        private String displayName;
        private String description;
        private boolean supervisorOnly;
        
        public WorkQueueInfo(String name, String displayName, String description, boolean supervisorOnly) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
            this.supervisorOnly = supervisorOnly;
        }
        
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public boolean isSupervisorOnly() { return supervisorOnly; }
    }
}

