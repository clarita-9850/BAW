package com.cmips.test;

import com.cmips.entity.Timesheet;
import com.cmips.dto.TimesheetResponse;
import com.cmips.entity.TimesheetStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Test class to demonstrate field filtering functionality
 */
public class FieldFilteringTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 FIELD FILTERING TEST DEMONSTRATION");
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
        
        // Test different user roles
        testRole("CASE_WORKER", List.of("CASE_WORKER"), timesheet);
        testRole("RECIPIENT", List.of("RECIPIENT"), timesheet);
        testRole("PROVIDER", List.of("PROVIDER"), timesheet);
        testRole("UNKNOWN_ROLE", List.of("UNKNOWN_ROLE"), timesheet);
    }
    
    private static void testRole(String roleName, List<String> roles, Timesheet timesheet) {
        System.out.println("🔍 Testing Role: " + roleName);
        
        // Simulate field filtering logic
        TimesheetResponse filteredResponse = simulateFieldFiltering(timesheet, roles);
        
        System.out.println("✅ Filtered Response for " + roleName + ":");
        System.out.println("  Employee Name: " + filteredResponse.getEmployeeName());
        System.out.println("  Department: " + filteredResponse.getDepartment());
        System.out.println("  Location: " + filteredResponse.getLocation());
        System.out.println("  Regular Hours: " + filteredResponse.getRegularHours());
        System.out.println("  Overtime Hours: " + filteredResponse.getOvertimeHours());
        System.out.println("");
    }
    
    private static TimesheetResponse simulateFieldFiltering(Timesheet timesheet, List<String> roles) {
        TimesheetResponse response = new TimesheetResponse();
        
        // Always include basic fields
        response.setId(timesheet.getId());
        response.setUserId(timesheet.getUserId());
        response.setPayPeriodStart(timesheet.getPayPeriodStart());
        response.setPayPeriodEnd(timesheet.getPayPeriodEnd());
        response.setTotalHours(timesheet.getTotalHours());
        response.setStatus(timesheet.getStatus());
        response.setCreatedAt(timesheet.getCreatedAt());
        response.setUpdatedAt(timesheet.getUpdatedAt());
        
        // Apply role-based filtering
        if (roles.contains("CASE_WORKER")) {
            // Case Workers see ALL fields
            response.setEmployeeName(timesheet.getEmployeeName());
            response.setDepartment(timesheet.getDepartment());
            response.setLocation(timesheet.getLocation());
            response.setRegularHours(timesheet.getRegularHours());
            response.setOvertimeHours(timesheet.getOvertimeHours());
        } else if (roles.contains("RECIPIENT")) {
            // Recipients see LIMITED fields
            response.setEmployeeName(timesheet.getEmployeeName());
            response.setRegularHours(timesheet.getRegularHours());
            response.setOvertimeHours(timesheet.getOvertimeHours());
            // Department and Location are null (restricted)
            response.setDepartment(null);
            response.setLocation(null);
        } else if (roles.contains("PROVIDER")) {
            // Providers see BASIC fields only
            // All sensitive fields are null (restricted)
            response.setEmployeeName(null);
            response.setDepartment(null);
            response.setLocation(null);
            response.setRegularHours(null);
            response.setOvertimeHours(null);
        } else {
            // Unknown roles get basic access only
            response.setEmployeeName(null);
            response.setDepartment(null);
            response.setLocation(null);
            response.setRegularHours(null);
            response.setOvertimeHours(null);
        }
        
        return response;
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
