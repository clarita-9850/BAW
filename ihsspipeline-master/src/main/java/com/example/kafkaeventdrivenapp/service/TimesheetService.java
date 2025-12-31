package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.entity.TimesheetEntity;
import com.example.kafkaeventdrivenapp.model.Timesheet;
import com.example.kafkaeventdrivenapp.repository.TimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TimesheetService {
    
    @Autowired
    private TimesheetRepository timesheetRepository;
    
    /**
     * Save timesheet to database
     */
    public TimesheetEntity saveTimesheet(Timesheet timesheet) {
        System.out.println("💾 TimesheetService: Saving timesheet to database");
        
        // Convert Timesheet model to TimesheetEntity (new schema)
        TimesheetEntity entity = new TimesheetEntity();
        entity.setEmployeeId(timesheet.getProviderId() != null ? timesheet.getProviderId() : "EMP-UNKNOWN");
        entity.setEmployeeName("Employee " + (timesheet.getProviderId() != null ? timesheet.getProviderId() : "Unknown"));
        entity.setUserId(timesheet.getRecipientId() != null ? timesheet.getRecipientId() : "user-unknown");
        entity.setDepartment("Unknown");
        entity.setLocation("Unknown");
        entity.setPayPeriodStart(timesheet.getStartDate());
        entity.setPayPeriodEnd(timesheet.getEndDate());
        entity.setTotalHours(timesheet.getTotalHours() > 0 ? 
            java.math.BigDecimal.valueOf(timesheet.getTotalHours()) : null);
        entity.setStatus(timesheet.getStatus() != null ? timesheet.getStatus() : "DRAFT");
        entity.setComments(timesheet.getComments());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        TimesheetEntity savedEntity = timesheetRepository.save(entity);
        System.out.println("✅ TimesheetService: Timesheet " + savedEntity.getId() + " saved successfully");
        
        return savedEntity;
    }
    
    /**
     * Update timesheet status
     */
    public TimesheetEntity updateTimesheetStatus(Long timesheetId, String status) {
        System.out.println("🔄 TimesheetService: Updating status for timesheet " + timesheetId + " to " + status);
        
        Optional<TimesheetEntity> optionalEntity = timesheetRepository.findById(timesheetId);
        if (optionalEntity.isPresent()) {
            TimesheetEntity entity = optionalEntity.get();
            entity.setStatus(status);
            entity.setUpdatedAt(LocalDateTime.now());
            
            TimesheetEntity updatedEntity = timesheetRepository.save(entity);
            System.out.println("✅ TimesheetService: Status updated for timesheet " + timesheetId);
            return updatedEntity;
        } else {
            System.err.println("❌ TimesheetService: Timesheet " + timesheetId + " not found");
            throw new RuntimeException("Timesheet not found: " + timesheetId);
        }
    }
    
    /**
     * Update supervisor comments (replaces validation result in new schema)
     */
    public TimesheetEntity updateSupervisorComments(Long timesheetId, String comments, String supervisorId) {
        System.out.println("🔍 TimesheetService: Updating supervisor comments for timesheet " + timesheetId);
        
        Optional<TimesheetEntity> optionalEntity = timesheetRepository.findById(timesheetId);
        if (optionalEntity.isPresent()) {
            TimesheetEntity entity = optionalEntity.get();
            entity.setSupervisorComments(comments);
            entity.setApprovedBy(supervisorId);
            entity.setUpdatedAt(LocalDateTime.now());
            
            TimesheetEntity updatedEntity = timesheetRepository.save(entity);
            System.out.println("✅ TimesheetService: Supervisor comments updated for timesheet " + timesheetId);
            return updatedEntity;
        } else {
            System.err.println("❌ TimesheetService: Timesheet " + timesheetId + " not found");
            throw new RuntimeException("Timesheet not found: " + timesheetId);
        }
    }
    
    /**
     * Update rejection reason (using supervisor comments in new schema)
     */
    public TimesheetEntity updateRejectionReason(Long timesheetId, String rejectionReason) {
        System.out.println("❌ TimesheetService: Updating rejection reason for timesheet " + timesheetId);
        
        Optional<TimesheetEntity> optionalEntity = timesheetRepository.findById(timesheetId);
        if (optionalEntity.isPresent()) {
            TimesheetEntity entity = optionalEntity.get();
            entity.setSupervisorComments(rejectionReason);
            entity.setStatus("REJECTED");
            entity.setUpdatedAt(LocalDateTime.now());
            
            TimesheetEntity updatedEntity = timesheetRepository.save(entity);
            System.out.println("✅ TimesheetService: Rejection reason updated for timesheet " + timesheetId);
            return updatedEntity;
        } else {
            System.err.println("❌ TimesheetService: Timesheet " + timesheetId + " not found");
            throw new RuntimeException("Timesheet not found: " + timesheetId);
        }
    }
    
    /**
     * Submit timesheet (sets submitted_at and submitted_by)
     */
    public TimesheetEntity submitTimesheet(Long timesheetId, String submittedBy) {
        System.out.println("📤 TimesheetService: Submitting timesheet " + timesheetId);
        
        Optional<TimesheetEntity> optionalEntity = timesheetRepository.findById(timesheetId);
        if (optionalEntity.isPresent()) {
            TimesheetEntity entity = optionalEntity.get();
            entity.setStatus("SUBMITTED");
            entity.setSubmittedAt(LocalDateTime.now());
            entity.setSubmittedBy(submittedBy);
            entity.setUpdatedAt(LocalDateTime.now());
            
            TimesheetEntity updatedEntity = timesheetRepository.save(entity);
            System.out.println("✅ TimesheetService: Timesheet " + timesheetId + " submitted successfully");
            return updatedEntity;
        } else {
            System.err.println("❌ TimesheetService: Timesheet " + timesheetId + " not found");
            throw new RuntimeException("Timesheet not found: " + timesheetId);
        }
    }
    
    /**
     * Get timesheet by ID
     */
    public Optional<TimesheetEntity> getTimesheetById(Long timesheetId) {
        return timesheetRepository.findById(timesheetId);
    }
    
    /**
     * Get timesheets by employee ID
     */
    public List<TimesheetEntity> getTimesheetsByEmployee(String employeeId) {
        return timesheetRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }
    
    /**
     * Get timesheets by status
     */
    public List<TimesheetEntity> getTimesheetsByStatus(String status) {
        return timesheetRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    /**
     * Get timesheets pending review
     */
    public List<TimesheetEntity> getTimesheetsPendingReview() {
        return timesheetRepository.findTimesheetsPendingReview();
    }
}
