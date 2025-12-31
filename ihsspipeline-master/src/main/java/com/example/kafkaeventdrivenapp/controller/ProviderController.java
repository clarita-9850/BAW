package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.Timesheet;
import com.example.kafkaeventdrivenapp.service.EventService;
import com.example.kafkaeventdrivenapp.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/provider")
@CrossOrigin(origins = "*")
public class ProviderController {

    @Autowired
    private EventService eventService;
    
    @Autowired
    private TimesheetService timesheetService;

    @PostMapping("/submit-timesheet")
    public ResponseEntity<String> submitTimesheet(@RequestBody Timesheet timesheet) {
        try {
            // Generate a unique ID for the timesheet if not provided
            if (timesheet.getTimesheetId() == null || timesheet.getTimesheetId().isEmpty()) {
                timesheet.setTimesheetId(UUID.randomUUID().toString());
            }
            timesheet.setStatus("SUBMITTED"); // Set initial status

            System.out.println("📝 ProviderController: Submitting timesheet " + timesheet.getTimesheetId() + 
                             " with " + timesheet.getTotalHours() + " hours");

            // Save timesheet using the service
            timesheetService.saveTimesheet(timesheet);
            
            // Publish event for the new enhanced workflow
            eventService.publishEvent("TIMESHEET_SUBMITTED", Map.of(
                "timesheetId", timesheet.getTimesheetId(),
                "providerId", timesheet.getProviderId(),
                "status", timesheet.getStatus(),
                "totalHours", timesheet.getTotalHours()
            ));
            
            return ResponseEntity.ok("Timesheet submitted successfully: " + timesheet.getTimesheetId() + 
                                   ". Status: " + timesheet.getStatus());
        } catch (Exception e) {
            System.err.println("❌ Error submitting timesheet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to submit timesheet: " + e.getMessage());
        }
    }

    @PostMapping("/revise-timesheet/{timesheetId}")
    public ResponseEntity<String> reviseTimesheet(@PathVariable String timesheetId, @RequestBody Timesheet timesheet) {
        try {
            timesheet.setTimesheetId(timesheetId);
            timesheet.setStatus("REVISED");

            System.out.println("🔄 ProviderController: Revising timesheet " + timesheetId + 
                             " with " + timesheet.getTotalHours() + " hours");

            // Update timesheet using the service (convert String ID to Long)
            try {
                Long id = Long.parseLong(timesheetId);
                timesheetService.updateTimesheetStatus(id, timesheet.getStatus());
            } catch (NumberFormatException e) {
                // If ID is not numeric, try to find by employee ID or create new
                throw new IllegalArgumentException("Invalid timesheet ID format: " + timesheetId);
            }
            
            // Publish event for the new enhanced workflow
            eventService.publishEvent("TIMESHEET_REVISED", Map.of(
                "timesheetId", timesheet.getTimesheetId(),
                "providerId", timesheet.getProviderId(),
                "status", timesheet.getStatus(),
                "totalHours", timesheet.getTotalHours()
            ));
            
            return ResponseEntity.ok("Timesheet revised successfully: " + timesheetId + 
                                   ". Status: " + timesheet.getStatus());
        } catch (Exception e) {
            System.err.println("❌ Error revising timesheet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to revise timesheet: " + e.getMessage());
        }
    }

    @GetMapping("/timesheet/{timesheetId}")
    public ResponseEntity<Timesheet> getTimesheet(@PathVariable String timesheetId) {
        // This would typically fetch from a database
        // For now, return a mock response
        Timesheet timesheet = new Timesheet();
        timesheet.setTimesheetId(timesheetId);
        timesheet.setProviderId("provider-123");
        timesheet.setStatus("SUBMITTED");
        return ResponseEntity.ok(timesheet);
    }
}
