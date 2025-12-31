package com.cmips.repository;

import com.cmips.entity.ReportJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportJobRepository extends JpaRepository<ReportJobEntity, String> {
    
    // Find jobs by status
    List<ReportJobEntity> findByStatus(String status);
    
    // Find jobs by user role
    List<ReportJobEntity> findByUserRole(String userRole);
    
    // Find jobs by status and user role
    List<ReportJobEntity> findByStatusAndUserRole(String status, String userRole);
    
    // Find jobs by target system
    List<ReportJobEntity> findByTargetSystem(String targetSystem);
    
    // Find jobs created after a specific time
    List<ReportJobEntity> findByCreatedAtAfter(LocalDateTime dateTime);
    
    // Find jobs that can be retried (failed with retry count < max retries)
    @Query("SELECT j FROM ReportJobEntity j WHERE j.status = 'FAILED' AND j.retryCount < j.maxRetries")
    List<ReportJobEntity> findRetryableJobs();
    
    // Find jobs that are stuck (processing for too long)
    @Query("SELECT j FROM ReportJobEntity j WHERE j.status = 'PROCESSING' AND j.startedAt < :cutoffTime")
    List<ReportJobEntity> findStuckJobs(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find jobs by priority (for processing order)
    @Query("SELECT j FROM ReportJobEntity j WHERE j.status = 'QUEUED' ORDER BY j.priority DESC, j.createdAt ASC")
    List<ReportJobEntity> findQueuedJobsByPriority();
    
    // Find jobs for cleanup (completed/failed older than specified time)
    @Query("SELECT j FROM ReportJobEntity j WHERE j.status IN ('COMPLETED', 'FAILED') AND j.updatedAt < :cutoffTime")
    List<ReportJobEntity> findJobsForCleanup(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Count jobs by status
    long countByStatus(String status);
    
    // Count jobs by user role and status
    long countByUserRoleAndStatus(String userRole, String status);
    
    // Find job by ID with optional status check
    Optional<ReportJobEntity> findByJobId(String jobId);
    
    // Find jobs by date range
    @Query("SELECT j FROM ReportJobEntity j WHERE j.createdAt BETWEEN :startDate AND :endDate")
    List<ReportJobEntity> findJobsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    // Find jobs by report type
    List<ReportJobEntity> findByReportType(String reportType);
    
    // Find jobs by status and report type
    List<ReportJobEntity> findByStatusAndReportType(String status, String reportType);
    
    @Query(value = "SELECT * FROM report_jobs WHERE status = 'QUEUED' ORDER BY priority DESC, created_at ASC LIMIT :limit", nativeQuery = true)
    List<ReportJobEntity> findTopQueuedJobs(@Param("limit") int limit);
}

