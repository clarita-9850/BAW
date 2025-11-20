package com.cmips.service;

import com.cmips.entity.EVVRecord;
import com.cmips.repository.EVVRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class EVVService {
    
    @Autowired
    private EVVRepository evvRepository;
    
    public EVVRecord checkIn(String providerId, String recipientId, String serviceType, 
                            Double latitude, Double longitude) {
        // Check if there's an active check-in
        Optional<EVVRecord> activeCheckIn = evvRepository
            .findByProviderIdAndRecipientIdAndStatus(providerId, recipientId, "IN_PROGRESS");
        
        if (activeCheckIn.isPresent()) {
            throw new IllegalStateException("You already have an active check-in. Please check out first.");
        }
        
        EVVRecord evv = new EVVRecord();
        evv.setProviderId(providerId);
        evv.setRecipientId(recipientId);
        evv.setServiceType(serviceType);
        evv.setCheckInTime(LocalDateTime.now());
        evv.setCheckInLatitude(latitude);
        evv.setCheckInLongitude(longitude);
        evv.setCheckInAddress(formatLocation(latitude, longitude));
        evv.setStatus("IN_PROGRESS");
        
        return evvRepository.save(evv);
    }
    
    public EVVRecord checkOut(Long evvId, Double latitude, Double longitude) {
        EVVRecord evv = evvRepository.findById(evvId)
            .orElseThrow(() -> new RuntimeException("EVV record not found"));
        
        if (!"IN_PROGRESS".equals(evv.getStatus())) {
            throw new IllegalStateException("This EVV session is not active");
        }
        
        LocalDateTime now = LocalDateTime.now();
        evv.setCheckOutTime(now);
        evv.setCheckOutLatitude(latitude);
        evv.setCheckOutLongitude(longitude);
        evv.setCheckOutAddress(formatLocation(latitude, longitude));
        
        // Calculate hours worked
        long minutes = ChronoUnit.MINUTES.between(evv.getCheckInTime(), now);
        double hours = minutes / 60.0;
        evv.setHoursWorked(Math.round(hours * 100.0) / 100.0);
        
        // Validate location (within reasonable distance)
        double distance = calculateDistance(
            evv.getCheckInLatitude(), evv.getCheckInLongitude(),
            latitude, longitude
        );
        
        if (distance > 1.0) { // More than 1 km away
            evv.setStatus("VIOLATION");
            evv.setViolationType("LOCATION_MISMATCH");
            evv.setViolationNotes("Check-out location is " + String.format("%.2f", distance) + " km from check-in");
        } else {
            evv.setStatus("COMPLETED");
        }
        
        return evvRepository.save(evv);
    }
    
    public List<EVVRecord> getProviderEVVRecords(String providerId) {
        return evvRepository.findByProviderId(providerId);
    }
    
    public List<EVVRecord> getTimesheetEVVRecords(Long timesheetId) {
        return evvRepository.findByTimesheetId(timesheetId);
    }
    
    public Optional<EVVRecord> getActiveCheckIn(String providerId, String recipientId) {
        return evvRepository.findByProviderIdAndRecipientIdAndStatus(providerId, recipientId, "IN_PROGRESS");
    }
    
    private String formatLocation(Double latitude, Double longitude) {
        return String.format("%.6f, %.6f", latitude, longitude);
    }
    
    // Haversine formula to calculate distance between two points
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        
        return distance;
    }
}


