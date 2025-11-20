package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evv_records")
public class EVVRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String providerId;
    
    @Column(nullable = false)
    private String recipientId;
    
    @Column(nullable = false)
    private String serviceType; // PERSONAL_CARE, DOMESTIC, MEDICAL, PROTECTIVE
    
    @Column(nullable = false)
    private LocalDateTime checkInTime;
    
    @Column
    private LocalDateTime checkOutTime;
    
    @Column(nullable = false)
    private Double checkInLatitude;
    
    @Column(nullable = false)
    private Double checkInLongitude;
    
    @Column
    private Double checkOutLatitude;
    
    @Column
    private Double checkOutLongitude;
    
    @Column
    private String checkInAddress;
    
    @Column
    private String checkOutAddress;
    
    @Column
    private Double hoursWorked;
    
    @Column(nullable = false)
    private String status; // IN_PROGRESS, COMPLETED, VERIFIED, VIOLATION
    
    @Column
    private String violationType; // LOCATION_MISMATCH, TIME_ANOMALY, etc.
    
    @Column
    private String violationNotes;
    
    @Column
    private Long timesheetId;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "IN_PROGRESS";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
    
    public Double getCheckInLatitude() { return checkInLatitude; }
    public void setCheckInLatitude(Double checkInLatitude) { this.checkInLatitude = checkInLatitude; }
    
    public Double getCheckInLongitude() { return checkInLongitude; }
    public void setCheckInLongitude(Double checkInLongitude) { this.checkInLongitude = checkInLongitude; }
    
    public Double getCheckOutLatitude() { return checkOutLatitude; }
    public void setCheckOutLatitude(Double checkOutLatitude) { this.checkOutLatitude = checkOutLatitude; }
    
    public Double getCheckOutLongitude() { return checkOutLongitude; }
    public void setCheckOutLongitude(Double checkOutLongitude) { this.checkOutLongitude = checkOutLongitude; }
    
    public String getCheckInAddress() { return checkInAddress; }
    public void setCheckInAddress(String checkInAddress) { this.checkInAddress = checkInAddress; }
    
    public String getCheckOutAddress() { return checkOutAddress; }
    public void setCheckOutAddress(String checkOutAddress) { this.checkOutAddress = checkOutAddress; }
    
    public Double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }
    
    public String getViolationNotes() { return violationNotes; }
    public void setViolationNotes(String violationNotes) { this.violationNotes = violationNotes; }
    
    public Long getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


