package com.example.kafkaeventdrivenapp.repository;

import com.example.kafkaeventdrivenapp.entity.TimesheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimesheetRepository extends JpaRepository<TimesheetEntity, Long> {
    
    // Find by employee ID
    List<TimesheetEntity> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
    
    // Find by user ID
    List<TimesheetEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // Find by status
    List<TimesheetEntity> findByStatusOrderByCreatedAtDesc(String status);
    
    // Find by employee and status
    List<TimesheetEntity> findByEmployeeIdAndStatusOrderByCreatedAtDesc(String employeeId, String status);
    
    // Find timesheets requiring revision (older than 7 days)
    @Query("SELECT t FROM TimesheetEntity t WHERE t.status = 'REVISION_REQUESTED' AND t.updatedAt < :cutoffDate")
    List<TimesheetEntity> findTimesheetsRequiringRevision(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find timesheets pending review
    @Query("SELECT t FROM TimesheetEntity t WHERE t.status = 'SUBMITTED' ORDER BY t.createdAt ASC")
    List<TimesheetEntity> findTimesheetsPendingReview();
    
    // Count by status
    long countByStatus(String status);
    
    // Find by date range (overlapping pay periods)
    @Query("SELECT t FROM TimesheetEntity t WHERE t.payPeriodStart <= :endDate AND t.payPeriodEnd >= :startDate ORDER BY t.createdAt DESC")
    List<TimesheetEntity> findTimesheetsByDateRange(@Param("startDate") java.time.LocalDate startDate, 
                                                   @Param("endDate") java.time.LocalDate endDate);
    
    // Find by date range for pipeline extraction (overlapping pay periods) with safety limit
    @Query(value = "SELECT * FROM timesheets WHERE pay_period_start <= :endDate AND pay_period_end >= :startDate ORDER BY created_at DESC LIMIT 5000", nativeQuery = true)
    List<TimesheetEntity> findByDateRange(@Param("startDate") java.time.LocalDate startDate, 
                                         @Param("endDate") java.time.LocalDate endDate);
    
    // Location-based access control methods
    List<TimesheetEntity> findByLocationOrderByCreatedAtDesc(String location);
    
    List<TimesheetEntity> findByDepartmentOrderByCreatedAtDesc(String department);
    
    // Find by location and status
    List<TimesheetEntity> findByLocationAndStatusOrderByCreatedAtDesc(String location, String status);
    
    // Find by date range and location
    @Query("SELECT t FROM TimesheetEntity t WHERE t.payPeriodStart >= :startDate AND t.payPeriodEnd <= :endDate AND t.location = :location ORDER BY t.createdAt DESC")
    List<TimesheetEntity> findByDateRangeAndLocation(@Param("startDate") java.time.LocalDate startDate, 
                                                      @Param("endDate") java.time.LocalDate endDate,
                                                      @Param("location") String location);
    
    // Find by user ID (for user-specific access)
    @Query("SELECT t FROM TimesheetEntity t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    List<TimesheetEntity> findByUser(@Param("userId") String userId);
    
    // Find by user ID with pagination
    @Query(value = "SELECT * FROM timesheets WHERE user_id = :userId ORDER BY created_at DESC LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<TimesheetEntity> findByUserWithPagination(@Param("userId") String userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    // Find by user ID and date range (overlapping pay periods)
    @Query("SELECT t FROM TimesheetEntity t WHERE t.userId = :userId AND t.payPeriodStart <= :endDate AND t.payPeriodEnd >= :startDate ORDER BY t.createdAt DESC")
    List<TimesheetEntity> findByUserAndDateRange(@Param("userId") String userId,
                                                  @Param("startDate") java.time.LocalDate startDate, 
                                                  @Param("endDate") java.time.LocalDate endDate);
    
    // Find by user ID and date range with pagination
    @Query(value = "SELECT * FROM timesheets WHERE user_id = :userId AND pay_period_start <= :endDate AND pay_period_end >= :startDate ORDER BY created_at DESC LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<TimesheetEntity> findByUserAndDateRangeWithPagination(@Param("userId") String userId,
                                                                 @Param("startDate") java.time.LocalDate startDate, 
                                                                 @Param("endDate") java.time.LocalDate endDate,
                                                                 @Param("offset") int offset,
                                                                 @Param("pageSize") int pageSize);
    
    // Find most recent records with limit (for safety when no date range is specified)
    @Query(value = "SELECT * FROM timesheets ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<TimesheetEntity> findMostRecentWithLimit(@Param("limit") int limit);
    
    // Find most recent records with pagination
    @Query(value = "SELECT * FROM timesheets ORDER BY created_at DESC LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<TimesheetEntity> findMostRecentWithPagination(@Param("offset") int offset, @Param("pageSize") int pageSize);
    
    // Find by date range with explicit limit (for safety)
    @Query(value = "SELECT * FROM timesheets WHERE pay_period_start <= :endDate AND pay_period_end >= :startDate ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<TimesheetEntity> findByDateRangeWithLimit(@Param("startDate") java.time.LocalDate startDate, 
                                                   @Param("endDate") java.time.LocalDate endDate,
                                                   @Param("limit") int limit);
    
    // Find by date range with pagination
    @Query(value = "SELECT * FROM timesheets WHERE pay_period_start <= :endDate AND pay_period_end >= :startDate ORDER BY created_at DESC LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<TimesheetEntity> findByDateRangeWithPagination(@Param("startDate") java.time.LocalDate startDate, 
                                                         @Param("endDate") java.time.LocalDate endDate,
                                                         @Param("offset") int offset,
                                                         @Param("pageSize") int pageSize);
    
    // Count by date range (for pagination)
    @Query(value = "SELECT COUNT(*) FROM timesheets WHERE pay_period_start <= :endDate AND pay_period_end >= :startDate", nativeQuery = true)
    long countByDateRange(@Param("startDate") java.time.LocalDate startDate, 
                         @Param("endDate") java.time.LocalDate endDate);
    
    // Count most recent records (for pagination when no date range)
    @Query(value = "SELECT COUNT(*) FROM timesheets", nativeQuery = true)
    long countMostRecent();
    
    // Count by user ID (for pagination)
    @Query(value = "SELECT COUNT(*) FROM timesheets WHERE user_id = :userId", nativeQuery = true)
    long countByUser(@Param("userId") String userId);
    
    // Count by user ID and date range (for pagination)
    @Query(value = "SELECT COUNT(*) FROM timesheets WHERE user_id = :userId AND pay_period_start <= :endDate AND pay_period_end >= :startDate", nativeQuery = true)
    long countByUserAndDateRange(@Param("userId") String userId,
                                 @Param("startDate") java.time.LocalDate startDate, 
                                 @Param("endDate") java.time.LocalDate endDate);
    
    // Count by location (for pagination)
    @Query(value = "SELECT COUNT(*) FROM timesheets WHERE location = :location", nativeQuery = true)
    long countByLocation(@Param("location") String location);
    
    // Count by location and date range (for pagination)
    @Query(value = "SELECT COUNT(*) FROM timesheets WHERE location = :location AND pay_period_start <= :endDate AND pay_period_end >= :startDate", nativeQuery = true)
    long countByLocationAndDateRange(@Param("location") String location,
                                     @Param("startDate") java.time.LocalDate startDate, 
                                     @Param("endDate") java.time.LocalDate endDate);
    
    // Find by location with pagination
    @Query(value = "SELECT * FROM timesheets WHERE location = :location ORDER BY created_at DESC LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<TimesheetEntity> findByLocationWithPagination(@Param("location") String location, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    // Find by location and date range with pagination
    @Query(value = "SELECT * FROM timesheets WHERE location = :location AND pay_period_start <= :endDate AND pay_period_end >= :startDate ORDER BY created_at DESC LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<TimesheetEntity> findByLocationAndDateRangeWithPagination(@Param("location") String location,
                                                                    @Param("startDate") java.time.LocalDate startDate, 
                                                                    @Param("endDate") java.time.LocalDate endDate,
                                                                    @Param("offset") int offset,
                                                                    @Param("pageSize") int pageSize);
    
    // Analytics methods - Fixed to use proper optional parameter handling
    @Query(value = "SELECT COUNT(*) FROM timesheets " +
            "WHERE (:createdAfter IS NULL OR created_at >= :createdAfter) " +
            "AND (:createdBefore IS NULL OR created_at <= :createdBefore) " +
            "AND (:location IS NULL OR location = :location) " +
            "AND (:department IS NULL OR department = :department) " +
            "AND (:statusFilter IS NULL OR status = :statusFilter)", nativeQuery = true)
    long countCreatedAfterWithFilters(@Param("createdAfter") LocalDateTime createdAfter,
                                      @Param("createdBefore") LocalDateTime createdBefore,
                                      @Param("location") String location,
                                      @Param("department") String department,
                                      @Param("statusFilter") String statusFilter);
    
    @Query(value = "SELECT COUNT(*) FROM timesheets " +
            "WHERE status IN ('SUBMITTED', 'REVISION_REQUESTED') " +
            "AND (:location IS NULL OR location = :location) " +
            "AND (:department IS NULL OR department = :department) " +
            "AND (:statusFilter IS NULL OR status = :statusFilter) " +
            "AND (:createdAfter IS NULL OR created_at >= :createdAfter) " +
            "AND (:createdBefore IS NULL OR created_at <= :createdBefore)", nativeQuery = true)
    long countPendingApprovalsWithFilters(@Param("location") String location,
                                          @Param("department") String department,
                                          @Param("statusFilter") String statusFilter,
                                          @Param("createdAfter") LocalDateTime createdAfter,
                                          @Param("createdBefore") LocalDateTime createdBefore);
    
    @Query(value = "SELECT COALESCE(SUM(total_hours), 0) FROM timesheets " +
            "WHERE (:createdAfter IS NULL OR created_at >= :createdAfter) " +
            "AND (:createdBefore IS NULL OR created_at <= :createdBefore) " +
            "AND (:location IS NULL OR location = :location) " +
            "AND (:department IS NULL OR department = :department) " +
            "AND (:statusFilter IS NULL OR status = :statusFilter)", nativeQuery = true)
    Double sumTotalHoursAfterWithFilters(@Param("createdAfter") LocalDateTime createdAfter,
                                         @Param("createdBefore") LocalDateTime createdBefore,
                                         @Param("location") String location,
                                         @Param("department") String department,
                                         @Param("statusFilter") String statusFilter);
    
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (approved_at - submitted_at))/3600) FROM timesheets " +
            "WHERE status = 'APPROVED' " +
            "AND submitted_at IS NOT NULL " +
            "AND approved_at IS NOT NULL " +
            "AND (:location IS NULL OR location = :location) " +
            "AND (:department IS NULL OR department = :department) " +
            "AND (:createdAfter IS NULL OR created_at >= :createdAfter) " +
            "AND (:createdBefore IS NULL OR created_at <= :createdBefore)", nativeQuery = true)
    Double avgApprovalTimeHoursWithFilters(@Param("location") String location,
                                           @Param("department") String department,
                                           @Param("createdAfter") LocalDateTime createdAfter,
                                           @Param("createdBefore") LocalDateTime createdBefore);
    
    @Query(value = "SELECT COUNT(DISTINCT employee_id) FROM timesheets " +
            "WHERE employee_id IS NOT NULL " +
            "AND (:location IS NULL OR location = :location) " +
            "AND (:department IS NULL OR department = :department) " +
            "AND (:statusFilter IS NULL OR status = :statusFilter) " +
            "AND (:createdAfter IS NULL OR created_at >= :createdAfter) " +
            "AND (:createdBefore IS NULL OR created_at <= :createdBefore)", nativeQuery = true)
    long countDistinctEmployeesWithFilters(@Param("location") String location,
                                           @Param("department") String department,
                                           @Param("statusFilter") String statusFilter,
                                           @Param("createdAfter") LocalDateTime createdAfter,
                                           @Param("createdBefore") LocalDateTime createdBefore);
    
    // Distinct filter option queries
    @Query(value = "SELECT DISTINCT location FROM timesheets WHERE location IS NOT NULL ORDER BY location", nativeQuery = true)
    List<String> findDistinctLocations();
    
    @Query(value = "SELECT DISTINCT department FROM timesheets WHERE department IS NOT NULL ORDER BY department", nativeQuery = true)
    List<String> findDistinctDepartments();
    
    @Query(value = "SELECT DISTINCT status FROM timesheets WHERE status IS NOT NULL ORDER BY status", nativeQuery = true)
    List<String> findDistinctStatuses();
    
    // Demographic field distinct queries
    @Query(value = "SELECT DISTINCT provider_gender FROM timesheets WHERE provider_gender IS NOT NULL ORDER BY provider_gender", nativeQuery = true)
    List<String> findDistinctProviderGenders();
    
    @Query(value = "SELECT DISTINCT recipient_gender FROM timesheets WHERE recipient_gender IS NOT NULL ORDER BY recipient_gender", nativeQuery = true)
    List<String> findDistinctRecipientGenders();
    
    @Query(value = "SELECT DISTINCT provider_ethnicity FROM timesheets WHERE provider_ethnicity IS NOT NULL ORDER BY provider_ethnicity", nativeQuery = true)
    List<String> findDistinctProviderEthnicities();
    
    @Query(value = "SELECT DISTINCT recipient_ethnicity FROM timesheets WHERE recipient_ethnicity IS NOT NULL ORDER BY recipient_ethnicity", nativeQuery = true)
    List<String> findDistinctRecipientEthnicities();
    
    @Query(value = "SELECT DISTINCT provider_age_group FROM timesheets WHERE provider_age_group IS NOT NULL ORDER BY provider_age_group", nativeQuery = true)
    List<String> findDistinctProviderAgeGroups();
    
    @Query(value = "SELECT DISTINCT recipient_age_group FROM timesheets WHERE recipient_age_group IS NOT NULL ORDER BY recipient_age_group", nativeQuery = true)
    List<String> findDistinctRecipientAgeGroups();
    
    // Combined distinct queries for filters (union of provider and recipient)
    @Query(value = "SELECT DISTINCT provider_gender FROM timesheets WHERE provider_gender IS NOT NULL " +
                   "UNION SELECT DISTINCT recipient_gender FROM timesheets WHERE recipient_gender IS NOT NULL " +
                   "ORDER BY provider_gender", nativeQuery = true)
    List<String> findDistinctGenders();
    
    @Query(value = "SELECT DISTINCT provider_ethnicity FROM timesheets WHERE provider_ethnicity IS NOT NULL " +
                   "UNION SELECT DISTINCT recipient_ethnicity FROM timesheets WHERE recipient_ethnicity IS NOT NULL " +
                   "ORDER BY provider_ethnicity", nativeQuery = true)
    List<String> findDistinctEthnicities();
    
    @Query(value = "SELECT DISTINCT provider_age_group FROM timesheets WHERE provider_age_group IS NOT NULL " +
                   "UNION SELECT DISTINCT recipient_age_group FROM timesheets WHERE recipient_age_group IS NOT NULL " +
                   "ORDER BY provider_age_group", nativeQuery = true)
    List<String> findDistinctAgeGroups();
    
    // Demographic distribution queries
    @Query(value = "SELECT provider_gender, COUNT(*) FROM timesheets WHERE provider_gender IS NOT NULL GROUP BY provider_gender ORDER BY provider_gender", nativeQuery = true)
    List<Object[]> countByProviderGender();
    
    @Query(value = "SELECT recipient_gender, COUNT(*) FROM timesheets WHERE recipient_gender IS NOT NULL GROUP BY recipient_gender ORDER BY recipient_gender", nativeQuery = true)
    List<Object[]> countByRecipientGender();
    
    @Query(value = "SELECT provider_ethnicity, COUNT(*) FROM timesheets WHERE provider_ethnicity IS NOT NULL GROUP BY provider_ethnicity ORDER BY provider_ethnicity", nativeQuery = true)
    List<Object[]> countByProviderEthnicity();
    
    @Query(value = "SELECT recipient_ethnicity, COUNT(*) FROM timesheets WHERE recipient_ethnicity IS NOT NULL GROUP BY recipient_ethnicity ORDER BY recipient_ethnicity", nativeQuery = true)
    List<Object[]> countByRecipientEthnicity();
    
    @Query(value = "SELECT provider_age_group, COUNT(*) FROM timesheets WHERE provider_age_group IS NOT NULL GROUP BY provider_age_group ORDER BY provider_age_group", nativeQuery = true)
    List<Object[]> countByProviderAgeGroup();
    
    @Query(value = "SELECT recipient_age_group, COUNT(*) FROM timesheets WHERE recipient_age_group IS NOT NULL GROUP BY recipient_age_group ORDER BY recipient_age_group", nativeQuery = true)
    List<Object[]> countByRecipientAgeGroup();
}
