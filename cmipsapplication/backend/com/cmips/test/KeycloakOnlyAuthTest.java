package com.cmips.test;

import com.cmips.service.FieldLevelAuthorizationService;
import com.cmips.entity.Timesheet;
import com.cmips.dto.TimesheetResponse;
import com.cmips.entity.TimesheetStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;

/**
 * Test class to demonstrate Keycloak-only authorization
 * This shows how Spring Boot now ONLY queries Keycloak for field permissions
 */
public class KeycloakOnlyAuthTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 KEYCLOAK-ONLY AUTHORIZATION TEST");
        System.out.println("");
        
        // Create a sample timesheet
        Timesheet timesheet = createSampleTimesheet();
        
        System.out.println("📋 Sample Timesheet Data:");
        System.out.println("ID: " + timesheet.getId());
        System.out.println("Employee Name: " + timesheet.getEmployeeName());
        System.out.println("Department: " + timesheet.getDepartment());
        System.out.println("Location: " + timesheet.getLocation());
        System.out.println("Regular Hours: " + timesheet.getRegularHours());
        System.out.println("Overtime Hours: " + timesheet.getOvertimeHours());
        System.out.println("");
        
        // Test different Keycloak authorization scenarios
        testKeycloakAuthorization("CASE_WORKER", getAllFieldPermissions(), timesheet);
        testKeycloakAuthorization("RECIPIENT", getRecipientFieldPermissions(), timesheet);
        testKeycloakAuthorization("PROVIDER", getProviderFieldPermissions(), timesheet);
        testKeycloakAuthorization("UNKNOWN_ROLE", getNoFieldPermissions(), timesheet);
    }
    
    private static void testKeycloakAuthorization(String roleName, Set<String> keycloakPermissions, Timesheet timesheet) {
        System.out.println("🔍 Testing Keycloak Authorization for: " + roleName);
        System.out.println("📡 Keycloak granted permissions: " + keycloakPermissions);
        
        // Simulate field filtering based on Keycloak's authorization decision
        TimesheetResponse filteredResponse = applyKeycloakFieldFiltering(timesheet, keycloakPermissions);
        
        System.out.println("✅ Filtered Response based on Keycloak authorization:");
        System.out.println("  Employee Name: " + filteredResponse.getEmployeeName());
        System.out.println("  Department: " + filteredResponse.getDepartment());
        System.out.println("  Location: " + filteredResponse.getLocation());
        System.out.println("  Regular Hours: " + filteredResponse.getRegularHours());
        System.out.println("  Overtime Hours: " + filteredResponse.getOvertimeHours());
        System.out.println("");
    }
    
    /**
     * Simulates the new FieldLevelAuthorizationService logic
     * This method ONLY applies filtering based on Keycloak's authorization decision
     * NO hardcoded role-to-field mapping!
     */
    private static TimesheetResponse applyKeycloakFieldFiltering(Timesheet timesheet, Set<String> keycloakPermissions) {
        TimesheetResponse response = new TimesheetResponse();
        
        // Always include basic fields (these are never restricted)
        response.setId(timesheet.getId());
        response.setUserId(timesheet.getUserId());
        response.setPayPeriodStart(timesheet.getPayPeriodStart());
        response.setPayPeriodEnd(timesheet.getPayPeriodEnd());
        response.setTotalHours(timesheet.getTotalHours());
        response.setStatus(timesheet.getStatus());
        response.setCreatedAt(timesheet.getCreatedAt());
        response.setUpdatedAt(timesheet.getUpdatedAt());
        
        // Apply field-level filtering based ONLY on Keycloak's authorization decision
        // Each field is only included if Keycloak grants permission for it
        
        if (keycloakPermissions.contains("timesheet:employee_id")) {
            response.setEmployeeId(timesheet.getEmployeeId());
        }
        
        if (keycloakPermissions.contains("timesheet:employee_name")) {
            response.setEmployeeName(timesheet.getEmployeeName());
        }
        
        if (keycloakPermissions.contains("timesheet:department")) {
            response.setDepartment(timesheet.getDepartment());
        }
        
        if (keycloakPermissions.contains("timesheet:location")) {
            response.setLocation(timesheet.getLocation());
        }
        
        if (keycloakPermissions.contains("timesheet:regular_hours")) {
            response.setRegularHours(timesheet.getRegularHours());
        }
        
        if (keycloakPermissions.contains("timesheet:overtime_hours")) {
            response.setOvertimeHours(timesheet.getOvertimeHours());
        }
        
        if (keycloakPermissions.contains("timesheet:holiday_hours")) {
            response.setHolidayHours(timesheet.getHolidayHours());
        }
        
        if (keycloakPermissions.contains("timesheet:sick_hours")) {
            response.setSickHours(timesheet.getSickHours());
        }
        
        if (keycloakPermissions.contains("timesheet:vacation_hours")) {
            response.setVacationHours(timesheet.getVacationHours());
        }
        
        if (keycloakPermissions.contains("timesheet:comments")) {
            response.setComments(timesheet.getComments());
        }
        
        if (keycloakPermissions.contains("timesheet:supervisor_comments")) {
            response.setSupervisorComments(timesheet.getSupervisorComments());
        }
        
        if (keycloakPermissions.contains("timesheet:approval_info")) {
            response.setApprovedBy(timesheet.getApprovedBy());
            response.setApprovedAt(timesheet.getApprovedAt());
            response.setSubmittedBy(timesheet.getSubmittedBy());
            response.setSubmittedAt(timesheet.getSubmittedAt());
        }
        
        return response;
    }
    
    // Simulate different Keycloak authorization scenarios
    private static Set<String> getAllFieldPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add("timesheet:employee_id");
        permissions.add("timesheet:employee_name");
        permissions.add("timesheet:department");
        permissions.add("timesheet:location");
        permissions.add("timesheet:regular_hours");
        permissions.add("timesheet:overtime_hours");
        permissions.add("timesheet:holiday_hours");
        permissions.add("timesheet:sick_hours");
        permissions.add("timesheet:vacation_hours");
        permissions.add("timesheet:comments");
        permissions.add("timesheet:supervisor_comments");
        permissions.add("timesheet:approval_info");
        return permissions;
    }
    
    private static Set<String> getRecipientFieldPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add("timesheet:employee_name");
        permissions.add("timesheet:regular_hours");
        permissions.add("timesheet:overtime_hours");
        return permissions;
    }
    
    private static Set<String> getProviderFieldPermissions() {
        Set<String> permissions = new HashSet<>();
        // Providers get basic access only - no sensitive fields
        return permissions;
    }
    
    private static Set<String> getNoFieldPermissions() {
        return new HashSet<>();
    }
    
    private static Timesheet createSampleTimesheet() {
        Timesheet timesheet = new Timesheet();
        timesheet.setId(1L);
        timesheet.setUserId("user123");
        timesheet.setEmployeeName("John Doe");
        timesheet.setDepartment("IT Department");
        timesheet.setLocation("New York Office");
        timesheet.setRegularHours(new BigDecimal("40.0"));
        timesheet.setOvertimeHours(new BigDecimal("5.0"));
        timesheet.setTotalHours(new BigDecimal("45.0"));
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        timesheet.setPayPeriodStart(LocalDate.now().minusDays(14));
        timesheet.setPayPeriodEnd(LocalDate.now());
        timesheet.setCreatedAt(LocalDateTime.now().minusDays(1));
        timesheet.setUpdatedAt(LocalDateTime.now());
        return timesheet;
    }
}
