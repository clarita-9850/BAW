package com.cmips.repository;

import com.cmips.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    
    List<Timesheet> findByUserId(Long userId);
    
    List<Timesheet> findByUserIdOrderByDateDesc(Long userId);
    
    List<Timesheet> findByStatus(String status);
    
    List<Timesheet> findByUserIdAndStatus(Long userId, String status);
    
    List<Timesheet> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Timesheet> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM Timesheet t WHERE t.userId = :userId AND t.date >= :startDate AND t.date <= :endDate ORDER BY t.date DESC")
    List<Timesheet> findTimesheetsByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM Timesheet t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<Timesheet> findByStatusOrderByCreatedAt(@Param("status") String status);
    
    @Query("SELECT t FROM Timesheet t WHERE t.status IN ('SUBMITTED', 'REVISION_REQUESTED') ORDER BY t.createdAt ASC")
    List<Timesheet> findPendingTimesheets();
    
    @Query("SELECT t FROM Timesheet t WHERE t.userId = :userId AND t.status = :status ORDER BY t.date DESC")
    List<Timesheet> findTimesheetsByUserAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Query("SELECT SUM(t.hours) FROM Timesheet t WHERE t.userId = :userId AND t.date >= :startDate AND t.date <= :endDate")
    Double getTotalHoursByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
