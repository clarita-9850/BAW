package com.cmips.repository;

import com.cmips.entity.Timesheet;
import com.cmips.entity.TimesheetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    
    // Find timesheets by user ID
    List<Timesheet> findByUserIdOrderByPayPeriodStartDesc(String userId);
    
    // Find timesheets by employee ID
    List<Timesheet> findByEmployeeIdOrderByPayPeriodStartDesc(String employeeId);
    
    // Find timesheets by status
    List<Timesheet> findByStatusOrderByCreatedAtDesc(TimesheetStatus status);
    
    // Find timesheets by department
    List<Timesheet> findByDepartmentOrderByPayPeriodStartDesc(String department);
    
    // Find timesheets by location
    List<Timesheet> findByLocationOrderByPayPeriodStartDesc(String location);
    
    // Find timesheets by pay period
    List<Timesheet> findByPayPeriodStartAndPayPeriodEnd(LocalDate startDate, LocalDate endDate);
    
    // Find timesheets by pay period range
    List<Timesheet> findByPayPeriodStartBetweenOrderByPayPeriodStartDesc(LocalDate startDate, LocalDate endDate);
    
    // Find timesheets by user and status
    List<Timesheet> findByUserIdAndStatusOrderByPayPeriodStartDesc(String userId, TimesheetStatus status);
    
    // Find timesheets by department and status
    List<Timesheet> findByDepartmentAndStatusOrderByCreatedAtDesc(String department, TimesheetStatus status);
    
    // Find timesheets by location and status
    List<Timesheet> findByLocationAndStatusOrderByCreatedAtDesc(String location, TimesheetStatus status);
    
    // Find timesheets by user and pay period
    Optional<Timesheet> findByUserIdAndPayPeriodStartAndPayPeriodEnd(String userId, LocalDate startDate, LocalDate endDate);
    
    // Find timesheets by employee and pay period
    Optional<Timesheet> findByEmployeeIdAndPayPeriodStartAndPayPeriodEnd(String employeeId, LocalDate startDate, LocalDate endDate);
    
    // Count timesheets by status
    long countByStatus(TimesheetStatus status);
    
    // Count timesheets by user
    long countByUserId(String userId);
    
    // Count timesheets by department
    long countByDepartment(String department);
    
    // Count timesheets by location
    long countByLocation(String location);
    
    // Find pending timesheets (submitted but not approved/rejected)
    @Query("SELECT t FROM Timesheet t WHERE t.status IN ('SUBMITTED', 'REVISION_REQUESTED') ORDER BY t.submittedAt ASC")
    List<Timesheet> findPendingTimesheets();
    
    // Find timesheets requiring approval
    @Query("SELECT t FROM Timesheet t WHERE t.status = 'SUBMITTED' ORDER BY t.submittedAt ASC")
    List<Timesheet> findTimesheetsRequiringApproval();
    
    // Find timesheets by supervisor
    @Query("SELECT t FROM Timesheet t WHERE t.approvedBy = :supervisorId ORDER BY t.approvedAt DESC")
    List<Timesheet> findByApprovedBy(@Param("supervisorId") String supervisorId);
    
    // Find timesheets submitted by user
    @Query("SELECT t FROM Timesheet t WHERE t.submittedBy = :userId ORDER BY t.submittedAt DESC")
    List<Timesheet> findBySubmittedBy(@Param("userId") String userId);
    
    // Find timesheets with total hours greater than threshold
    @Query("SELECT t FROM Timesheet t WHERE t.totalHours > :threshold ORDER BY t.totalHours DESC")
    List<Timesheet> findByTotalHoursGreaterThan(@Param("threshold") Double threshold);
    
    // Find timesheets by date range and status
    @Query("SELECT t FROM Timesheet t WHERE t.payPeriodStart >= :startDate AND t.payPeriodEnd <= :endDate AND t.status = :status ORDER BY t.payPeriodStart DESC")
    List<Timesheet> findByDateRangeAndStatus(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate, 
                                           @Param("status") TimesheetStatus status);
    // New method to find timesheets by user ID
    Page<Timesheet> findByUserId(String userId, Pageable pageable);
    
    // New method to find timesheets by status
    Page<Timesheet> findByStatus(TimesheetStatus status, Pageable pageable);
}
