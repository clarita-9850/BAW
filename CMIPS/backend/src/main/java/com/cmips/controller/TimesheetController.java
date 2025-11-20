package com.cmips.controller;

import com.cmips.dto.TimesheetRequest;
import com.cmips.entity.Timesheet;
import com.cmips.entity.User;
import com.cmips.repository.TimesheetRepository;
import com.cmips.repository.UserRepository;
import com.cmips.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/timesheets")
@CrossOrigin(origins = "*")
public class TimesheetController {
    
    @Autowired
    private TimesheetRepository timesheetRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Submit a new timesheet
     * POST /api/timesheets
     */
    @PostMapping
    public ResponseEntity<Timesheet> submitTimesheet(@Valid @RequestBody TimesheetRequest request, 
                                                   HttpServletRequest httpRequest) {
        try {
            // Get user from JWT token
            String jwtToken = (String) httpRequest.getAttribute("jwtToken");
            User user = authService.validateJwtToken(jwtToken);
            
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Create timesheet
            Timesheet timesheet = new Timesheet();
            timesheet.setUserId(user.getId());
            timesheet.setDate(request.getDate());
            timesheet.setHours(request.getHours());
            timesheet.setDescription(request.getDescription());
            timesheet.setComments(request.getComments());
            timesheet.setStatus("SUBMITTED");
            timesheet.setRevisionCount(0);
            
            Timesheet savedTimesheet = timesheetRepository.save(timesheet);
            return ResponseEntity.ok(savedTimesheet);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all timesheets for the current user
     * GET /api/timesheets
     */
    @GetMapping
    public ResponseEntity<List<Timesheet>> getTimesheets(HttpServletRequest httpRequest) {
        try {
            // Get user from JWT token
            String jwtToken = (String) httpRequest.getAttribute("jwtToken");
            User user = authService.validateJwtToken(jwtToken);
            
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Timesheet> timesheets = timesheetRepository.findByUserIdOrderByDateDesc(user.getId());
            return ResponseEntity.ok(timesheets);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get timesheet by ID
     * GET /api/timesheets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Timesheet> getTimesheet(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // Get user from JWT token
            String jwtToken = (String) httpRequest.getAttribute("jwtToken");
            User user = authService.validateJwtToken(jwtToken);
            
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Optional<Timesheet> timesheet = timesheetRepository.findById(id);
            if (timesheet.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user owns this timesheet or has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!timesheet.get().getUserId().equals(user.getId()) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(timesheet.get());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update timesheet
     * PUT /api/timesheets/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Timesheet> updateTimesheet(@PathVariable Long id, 
                                                   @Valid @RequestBody TimesheetRequest request,
                                                   HttpServletRequest httpRequest) {
        try {
            // Get user from JWT token
            String jwtToken = (String) httpRequest.getAttribute("jwtToken");
            User user = authService.validateJwtToken(jwtToken);
            
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Optional<Timesheet> existingTimesheet = timesheetRepository.findById(id);
            if (existingTimesheet.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Timesheet timesheet = existingTimesheet.get();
            
            // Check if user owns this timesheet or has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!timesheet.getUserId().equals(user.getId()) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Update timesheet
            timesheet.setDate(request.getDate());
            timesheet.setHours(request.getHours());
            timesheet.setDescription(request.getDescription());
            timesheet.setComments(request.getComments());
            timesheet.setStatus("SUBMITTED"); // Reset status when updated
            
            Timesheet updatedTimesheet = timesheetRepository.save(timesheet);
            return ResponseEntity.ok(updatedTimesheet);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Delete timesheet
     * DELETE /api/timesheets/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimesheet(@PathVariable Long id, HttpServletRequest httpRequest) {
        try {
            // Get user from JWT token
            String jwtToken = (String) httpRequest.getAttribute("jwtToken");
            User user = authService.validateJwtToken(jwtToken);
            
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Optional<Timesheet> timesheet = timesheetRepository.findById(id);
            if (timesheet.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user owns this timesheet or has admin privileges
            String role = (String) httpRequest.getAttribute("userRole");
            if (!timesheet.get().getUserId().equals(user.getId()) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            timesheetRepository.deleteById(id);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get timesheets by status (for managers/auditors)
     * GET /api/timesheets/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Timesheet>> getTimesheetsByStatus(@PathVariable String status, 
                                                               HttpServletRequest httpRequest) {
        try {
            // Get user from JWT token
            String jwtToken = (String) httpRequest.getAttribute("jwtToken");
            User user = authService.validateJwtToken(jwtToken);
            
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Check if user has permission to view timesheets by status
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role) && !"MANAGER".equals(role) && !"AUDITOR".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<Timesheet> timesheets = timesheetRepository.findByStatusOrderByCreatedAt(status);
            return ResponseEntity.ok(timesheets);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get pending timesheets (for managers/auditors)
     * GET /api/timesheets/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Timesheet>> getPendingTimesheets(HttpServletRequest httpRequest) {
        try {
            // Get user from JWT token
            String jwtToken = (String) httpRequest.getAttribute("jwtToken");
            User user = authService.validateJwtToken(jwtToken);
            
            if (user == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Check if user has permission to view pending timesheets
            String role = (String) httpRequest.getAttribute("userRole");
            if (!"ADMIN".equals(role) && !"MANAGER".equals(role) && !"AUDITOR".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            List<Timesheet> timesheets = timesheetRepository.findPendingTimesheets();
            return ResponseEntity.ok(timesheets);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
