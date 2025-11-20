package com.cmips.controller;

import com.cmips.entity.EVVRecord;
import com.cmips.service.EVVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/evv")
@CrossOrigin(origins = "*")
public class EVVController {
    
    @Autowired
    private EVVService evvService;
    
    /**
     * EVV Check-In
     */
    @PostMapping("/check-in")
    public ResponseEntity<EVVRecord> checkIn(@RequestBody Map<String, Object> request) {
        try {
            String providerId = getCurrentUserId();
            String recipientId = (String) request.get("recipientId");
            String serviceType = (String) request.get("serviceType");
            Double latitude = Double.parseDouble(request.get("latitude").toString());
            Double longitude = Double.parseDouble(request.get("longitude").toString());
            
            EVVRecord evv = evvService.checkIn(providerId, recipientId, serviceType, latitude, longitude);
            return ResponseEntity.status(HttpStatus.CREATED).body(evv);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * EVV Check-Out
     */
    @PostMapping("/check-out/{evvId}")
    public ResponseEntity<EVVRecord> checkOut(@PathVariable Long evvId, @RequestBody Map<String, Object> request) {
        try {
            Double latitude = Double.parseDouble(request.get("latitude").toString());
            Double longitude = Double.parseDouble(request.get("longitude").toString());
            
            EVVRecord evv = evvService.checkOut(evvId, latitude, longitude);
            return ResponseEntity.ok(evv);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get provider's EVV records
     */
    @GetMapping("/my-records")
    public ResponseEntity<List<EVVRecord>> getMyEVVRecords() {
        String providerId = getCurrentUserId();
        List<EVVRecord> records = evvService.getProviderEVVRecords(providerId);
        return ResponseEntity.ok(records);
    }
    
    /**
     * Get active check-in
     */
    @GetMapping("/active-checkin")
    public ResponseEntity<EVVRecord> getActiveCheckIn(@RequestParam String recipientId) {
        String providerId = getCurrentUserId();
        Optional<EVVRecord> activeCheckIn = evvService.getActiveCheckIn(providerId, recipientId);
        return activeCheckIn.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get EVV records for a timesheet
     */
    @GetMapping("/timesheet/{timesheetId}")
    public ResponseEntity<List<EVVRecord>> getTimesheetEVVRecords(@PathVariable Long timesheetId) {
        List<EVVRecord> records = evvService.getTimesheetEVVRecords(timesheetId);
        return ResponseEntity.ok(records);
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}


