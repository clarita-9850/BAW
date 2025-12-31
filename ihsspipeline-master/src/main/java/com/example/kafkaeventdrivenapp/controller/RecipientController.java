package com.example.kafkaeventdrivenapp.controller;

import com.example.kafkaeventdrivenapp.model.Event;
import com.example.kafkaeventdrivenapp.model.TimesheetApproval;
import com.example.kafkaeventdrivenapp.model.TimesheetEventTypes;
import com.example.kafkaeventdrivenapp.service.EventService;
import com.example.kafkaeventdrivenapp.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/recipient")
@CrossOrigin(origins = "*")
public class RecipientController {

    @Autowired
    private EventService eventService;
    
    @Autowired
    private TimesheetService timesheetService;

    @PostMapping("/approve-timesheet")
    @Transactional
    public ResponseEntity<String> approveTimesheet(@RequestBody TimesheetApproval approval) {
        try {
            // Generate approval ID if not provided
            if (approval.getApprovalId() == null || approval.getApprovalId().isEmpty()) {
                approval.setApprovalId(UUID.randomUUID().toString());
            }
            approval.setStatus("APPROVED");
            approval.setApprovalDate(LocalDateTime.now());

            // Update database status (convert String ID to Long)
            try {
                Long id = Long.parseLong(approval.getTimesheetId());
                timesheetService.updateTimesheetStatus(id, "APPROVED");
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid timesheet ID format: " + approval.getTimesheetId());
            }
            System.out.println("✅ RecipientController: Timesheet " + approval.getTimesheetId() + " approved and status updated in database");

            Event event = new Event(TimesheetEventTypes.TIMESHEET_APPROVED, "RECIPIENT_SERVICE", approval);
            eventService.publishEvent("timesheet-approvals", event);
            
            return ResponseEntity.ok("Timesheet approved successfully: " + approval.getTimesheetId());
        } catch (Exception e) {
            System.err.println("❌ RecipientController: Failed to approve timesheet: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to approve timesheet: " + e.getMessage());
        }
    }

    @PostMapping("/reject-timesheet")
    @Transactional
    public ResponseEntity<String> rejectTimesheet(@RequestBody TimesheetApproval approval) {
        try {
            // Generate approval ID if not provided
            if (approval.getApprovalId() == null || approval.getApprovalId().isEmpty()) {
                approval.setApprovalId(UUID.randomUUID().toString());
            }
            approval.setStatus("REJECTED");
            approval.setApprovalDate(LocalDateTime.now());

            // Update database status (convert String ID to Long)
            try {
                Long id = Long.parseLong(approval.getTimesheetId());
                timesheetService.updateTimesheetStatus(id, "REJECTED");
                if (approval.getComments() != null && !approval.getComments().isEmpty()) {
                    timesheetService.updateRejectionReason(id, approval.getComments());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid timesheet ID format: " + approval.getTimesheetId());
            }
            System.out.println("❌ RecipientController: Timesheet " + approval.getTimesheetId() + " rejected and status updated in database");

            Event event = new Event(TimesheetEventTypes.TIMESHEET_REJECTED, "RECIPIENT_SERVICE", approval);
            eventService.publishEvent("timesheet-approvals", event);
            
            return ResponseEntity.ok("Timesheet rejected: " + approval.getTimesheetId());
        } catch (Exception e) {
            System.err.println("❌ RecipientController: Failed to reject timesheet: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to reject timesheet: " + e.getMessage());
        }
    }

    @PostMapping("/request-revision")
    @Transactional
    public ResponseEntity<String> requestRevision(@RequestBody TimesheetApproval approval) {
        try {
            // Generate approval ID if not provided
            if (approval.getApprovalId() == null || approval.getApprovalId().isEmpty()) {
                approval.setApprovalId(UUID.randomUUID().toString());
            }
            approval.setStatus("REVISION_REQUESTED");
            approval.setApprovalDate(LocalDateTime.now());

            // Update database status (convert String ID to Long)
            try {
                Long id = Long.parseLong(approval.getTimesheetId());
                timesheetService.updateTimesheetStatus(id, "REVISION_REQUESTED");
                if (approval.getComments() != null && !approval.getComments().isEmpty()) {
                    timesheetService.updateRejectionReason(id, approval.getComments());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid timesheet ID format: " + approval.getTimesheetId());
            }
            System.out.println("🔄 RecipientController: Timesheet " + approval.getTimesheetId() + " revision requested and status updated in database");

            Event event = new Event(TimesheetEventTypes.TIMESHEET_REVISION_REQUESTED, "RECIPIENT_SERVICE", approval);
            eventService.publishEvent("timesheet-approvals", event);
            
            return ResponseEntity.ok("Revision requested for timesheet: " + approval.getTimesheetId());
        } catch (Exception e) {
            System.err.println("❌ RecipientController: Failed to request revision: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to request revision: " + e.getMessage());
        }
    }

    @GetMapping("/pending-timesheets")
    public ResponseEntity<String> getPendingTimesheets() {
        // This would typically fetch from a database
        // For now, return a mock response
        return ResponseEntity.ok("No pending timesheets found");
    }

    @PostMapping("/send-reminder/{timesheetId}")
    public ResponseEntity<String> sendReminder(@PathVariable String timesheetId) {
        try {
            Event event = new Event(TimesheetEventTypes.TIMESHEET_REMINDER, "RECIPIENT_SERVICE", 
                    "Reminder for timesheet: " + timesheetId);
            eventService.publishEvent("timesheet-reminders", event);
            
            return ResponseEntity.ok("Reminder sent for timesheet: " + timesheetId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send reminder: " + e.getMessage());
        }
    }
}
