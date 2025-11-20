package com.cmips.service;

import com.cmips.dto.TimesheetCreateRequest;
import com.cmips.dto.TimesheetResponse;
import com.cmips.dto.TimesheetUpdateRequest;
import com.cmips.entity.Timesheet;
import com.cmips.entity.TimesheetStatus;
import com.cmips.repository.TimesheetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimesheetService {

    private static final Logger logger = LoggerFactory.getLogger(TimesheetService.class);
    
    @Autowired
    private TimesheetRepository timesheetRepository;
    
    /**
     * Create a new timesheet
     */
    public TimesheetResponse createTimesheet(String userId, TimesheetCreateRequest request) {
        // Check if timesheet already exists for this pay period
        Optional<Timesheet> existingTimesheet = timesheetRepository
            .findByUserIdAndPayPeriodStartAndPayPeriodEnd(userId, 
                request.getPayPeriodStart(), request.getPayPeriodEnd());
        
        if (existingTimesheet.isPresent()) {
            throw new IllegalArgumentException("Timesheet already exists for this pay period");
        }
        
        Timesheet timesheet = new Timesheet(
            userId,
            request.getEmployeeId(),
            request.getEmployeeName(),
            request.getDepartment(),
            request.getLocation(),
            request.getPayPeriodStart(),
            request.getPayPeriodEnd()
        );
        
        // Set hours
        timesheet.setRegularHours(request.getRegularHours());
        timesheet.setOvertimeHours(request.getOvertimeHours());
        timesheet.setHolidayHours(request.getHolidayHours());
        timesheet.setSickHours(request.getSickHours());
        timesheet.setVacationHours(request.getVacationHours());
        timesheet.setComments(request.getComments());
        
        // Calculate total hours
        timesheet.calculateTotalHours();
        
        // Set initial status
        timesheet.setStatus(TimesheetStatus.DRAFT);
        
        Timesheet savedTimesheet = timesheetRepository.save(timesheet);
        return new TimesheetResponse(savedTimesheet);
    }
    
    /**
     * Get timesheet by ID
     */
    @Transactional(readOnly = true)
    public Optional<TimesheetResponse> getTimesheetById(Long id) {
        return timesheetRepository.findById(id)
            .map(TimesheetResponse::new);
    }
    
    /**
     * Get timesheets by user ID
     */
    @Transactional(readOnly = true)
    public List<TimesheetResponse> getTimesheetsByUserId(String userId) {
        return timesheetRepository.findByUserIdOrderByPayPeriodStartDesc(userId)
            .stream()
            .map(TimesheetResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get timesheets by status
     */
    @Transactional(readOnly = true)
    public List<TimesheetResponse> getTimesheetsByStatus(TimesheetStatus status) {
        return timesheetRepository.findByStatusOrderByCreatedAtDesc(status)
            .stream()
            .map(TimesheetResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get timesheets by department
     */
    @Transactional(readOnly = true)
    public List<TimesheetResponse> getTimesheetsByDepartment(String department) {
        return timesheetRepository.findByDepartmentOrderByPayPeriodStartDesc(department)
            .stream()
            .map(TimesheetResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get timesheets by location
     */
    @Transactional(readOnly = true)
    public List<TimesheetResponse> getTimesheetsByLocation(String location) {
        return timesheetRepository.findByLocationOrderByPayPeriodStartDesc(location)
            .stream()
            .map(TimesheetResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all timesheets with pagination
     */
    @Transactional(readOnly = true)
    public Page<TimesheetResponse> getAllTimesheets(Pageable pageable) {
        return timesheetRepository.findAll(pageable)
            .map(TimesheetResponse::new);
    }
    
    /**
     * Update timesheet
     */
    public Optional<TimesheetResponse> updateTimesheet(Long id, TimesheetUpdateRequest request) {
        return timesheetRepository.findById(id)
            .map(timesheet -> {
                // Update hours if provided
                if (request.getRegularHours() != null) {
                    timesheet.setRegularHours(request.getRegularHours());
                }
                if (request.getOvertimeHours() != null) {
                    timesheet.setOvertimeHours(request.getOvertimeHours());
                }
                if (request.getHolidayHours() != null) {
                    timesheet.setHolidayHours(request.getHolidayHours());
                }
                if (request.getSickHours() != null) {
                    timesheet.setSickHours(request.getSickHours());
                }
                if (request.getVacationHours() != null) {
                    timesheet.setVacationHours(request.getVacationHours());
                }
                
                // Update comments if provided
                if (request.getComments() != null) {
                    timesheet.setComments(request.getComments());
                }
                
                // Update supervisor comments if provided
                if (request.getSupervisorComments() != null) {
                    timesheet.setSupervisorComments(request.getSupervisorComments());
                }
                
                // Update status if provided
                if (request.getStatus() != null) {
                    timesheet.setStatus(request.getStatus());
                }
                
                // Recalculate total hours
                timesheet.calculateTotalHours();
                
                Timesheet savedTimesheet = timesheetRepository.save(timesheet);
                return new TimesheetResponse(savedTimesheet);
            });
    }
    
    /**
     * Submit timesheet for approval
     */
    public Optional<TimesheetResponse> submitTimesheet(Long id, String userId) {
        return timesheetRepository.findById(id)
            .map(timesheet -> {
                if (timesheet.getStatus() != TimesheetStatus.DRAFT) {
                    throw new IllegalStateException("Only draft timesheets can be submitted");
                }
                
                timesheet.setStatus(TimesheetStatus.SUBMITTED);
                timesheet.setSubmittedBy(userId);
                timesheet.setSubmittedAt(LocalDateTime.now());
                
                Timesheet savedTimesheet = timesheetRepository.save(timesheet);
                return new TimesheetResponse(savedTimesheet);
            });
    }
    
    /**
     * Approve timesheet
     */
    public Optional<TimesheetResponse> approveTimesheet(Long id, String supervisorId) {
        return timesheetRepository.findById(id)
            .map(timesheet -> {
                // Auto-submit if still in DRAFT status
                if (timesheet.getStatus() == TimesheetStatus.DRAFT) {
                    timesheet.setStatus(TimesheetStatus.SUBMITTED);
                    timesheet.setSubmittedBy(timesheet.getUserId());
                    timesheet.setSubmittedAt(LocalDateTime.now());
                }
                
                if (timesheet.getStatus() != TimesheetStatus.SUBMITTED) {
                    throw new IllegalStateException("Only submitted timesheets can be approved");
                }
                
                timesheet.setStatus(TimesheetStatus.APPROVED);
                timesheet.setApprovedBy(supervisorId);
                timesheet.setApprovedAt(LocalDateTime.now());
                
                Timesheet savedTimesheet = timesheetRepository.save(timesheet);
                return new TimesheetResponse(savedTimesheet);
            });
    }
    
    /**
     * Reject timesheet
     */
    public Optional<TimesheetResponse> rejectTimesheet(Long id, String supervisorId, String comments) {
        return timesheetRepository.findById(id)
            .map(timesheet -> {
                // Auto-submit if still in DRAFT status
                if (timesheet.getStatus() == TimesheetStatus.DRAFT) {
                    timesheet.setStatus(TimesheetStatus.SUBMITTED);
                    timesheet.setSubmittedBy(timesheet.getUserId());
                    timesheet.setSubmittedAt(LocalDateTime.now());
                }
                
                if (timesheet.getStatus() != TimesheetStatus.SUBMITTED) {
                    throw new IllegalStateException("Only submitted timesheets can be rejected");
                }
                
                timesheet.setStatus(TimesheetStatus.REJECTED);
                timesheet.setApprovedBy(supervisorId);
                timesheet.setApprovedAt(LocalDateTime.now());
                timesheet.setSupervisorComments(comments);
                
                Timesheet savedTimesheet = timesheetRepository.save(timesheet);
                return new TimesheetResponse(savedTimesheet);
            });
    }
    
    /**
     * Request revision
     */
    public Optional<TimesheetResponse> requestRevision(Long id, String supervisorId, String comments) {
        return timesheetRepository.findById(id)
            .map(timesheet -> {
                if (timesheet.getStatus() != TimesheetStatus.SUBMITTED) {
                    throw new IllegalStateException("Only submitted timesheets can be requested for revision");
                }
                
                timesheet.setStatus(TimesheetStatus.REVISION_REQUESTED);
                timesheet.setApprovedBy(supervisorId);
                timesheet.setApprovedAt(LocalDateTime.now());
                timesheet.setSupervisorComments(comments);
                
                Timesheet savedTimesheet = timesheetRepository.save(timesheet);
                return new TimesheetResponse(savedTimesheet);
            });
    }
    
    /**
     * Delete timesheet
     */
    public boolean deleteTimesheet(Long id, String userId) {
        return timesheetRepository.findById(id)
            .map(timesheet -> {
                // Check if user can delete this timesheet
                // Providers can only delete their own timesheets
                // Case workers can delete any timesheet
                if (!timesheet.getUserId().equals(userId)) {
                    // For now, allow deletion if user has delete permission (handled by controller authorization)
                    // In the future, you might want to add role-based checks here
                    logger.warn("User {} attempting to delete timesheet {} owned by {}", userId, id, timesheet.getUserId());
                }
                
                // Only allow deletion of DRAFT timesheets
                if (timesheet.getStatus() != TimesheetStatus.DRAFT) {
                    throw new IllegalStateException("Only draft timesheets can be deleted");
                }
                
                timesheetRepository.deleteById(id);
                logger.info("Timesheet {} deleted by user {}", id, userId);
                return true;
            })
            .orElse(false);
    }
    
    /**
     * Get timesheets requiring approval
     */
    @Transactional(readOnly = true)
    public List<TimesheetResponse> getTimesheetsRequiringApproval() {
        return timesheetRepository.findTimesheetsRequiringApproval()
            .stream()
            .map(TimesheetResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get pending timesheets
     */
    @Transactional(readOnly = true)
    public List<TimesheetResponse> getPendingTimesheets() {
        return timesheetRepository.findPendingTimesheets()
            .stream()
            .map(TimesheetResponse::new)
            .collect(Collectors.toList());
    }
    
    // New method to get timesheets by user ID
    public Page<TimesheetResponse> getTimesheetsByUserId(String userId, Pageable pageable) {
        logger.info("Getting timesheets for user: {}", userId);
        return timesheetRepository.findByUserId(userId, pageable)
            .map(TimesheetResponse::new);
    }
    
    // New method to get submitted timesheets
    public Page<TimesheetResponse> getSubmittedTimesheets(Pageable pageable) {
        logger.info("Getting submitted timesheets");
        return timesheetRepository.findByStatus(TimesheetStatus.SUBMITTED, pageable)
            .map(TimesheetResponse::new);
    }
}
