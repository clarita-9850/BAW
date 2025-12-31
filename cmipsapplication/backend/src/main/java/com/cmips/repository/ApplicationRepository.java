package com.cmips.repository;

import com.cmips.entity.ApplicationEntity;
import com.cmips.entity.ApplicationEntity.ApplicationStatus;
import com.cmips.entity.ApplicationEntity.ProgramType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ApplicationEntity
 * Implements DSD Section 20 - Application Processing queries
 */
@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, String> {

    // Find by application number
    Optional<ApplicationEntity> findByApplicationNumber(String applicationNumber);

    // Find by recipient ID
    List<ApplicationEntity> findByRecipientId(Long recipientId);

    // Find by referral ID
    Optional<ApplicationEntity> findByReferralId(String referralId);

    // Find by status
    List<ApplicationEntity> findByStatus(ApplicationStatus status);

    // Find by county
    List<ApplicationEntity> findByCountyCode(String countyCode);

    // Find by county and status
    List<ApplicationEntity> findByCountyCodeAndStatus(String countyCode, ApplicationStatus status);

    // Find by assigned worker
    List<ApplicationEntity> findByAssignedWorkerId(String assignedWorkerId);

    // Find by assigned worker and status
    List<ApplicationEntity> findByAssignedWorkerIdAndStatus(String assignedWorkerId, ApplicationStatus status);

    // Find pending applications
    @Query("SELECT a FROM ApplicationEntity a WHERE a.status = 'PENDING' OR a.status = 'IN_PROGRESS' OR a.status = 'PENDING_DOCUMENTATION' OR a.status = 'PENDING_MEDI_CAL'")
    List<ApplicationEntity> findPendingApplications();

    // Find pending applications by county
    @Query("SELECT a FROM ApplicationEntity a WHERE a.countyCode = :countyCode AND (a.status = 'PENDING' OR a.status = 'IN_PROGRESS' OR a.status = 'PENDING_DOCUMENTATION' OR a.status = 'PENDING_MEDI_CAL')")
    List<ApplicationEntity> findPendingApplicationsByCounty(@Param("countyCode") String countyCode);

    // ==================== 45-DAY TIMELINE QUERIES ====================

    // Find applications approaching deadline (within X days)
    @Query("SELECT a FROM ApplicationEntity a WHERE " +
           "COALESCE(a.extendedDeadlineDate, a.deadlineDate) <= :deadlineDate AND " +
           "(a.status = 'PENDING' OR a.status = 'IN_PROGRESS' OR a.status = 'PENDING_DOCUMENTATION' OR a.status = 'PENDING_MEDI_CAL')")
    List<ApplicationEntity> findApproachingDeadline(@Param("deadlineDate") LocalDate deadlineDate);

    // Find applications approaching deadline by county
    @Query("SELECT a FROM ApplicationEntity a WHERE a.countyCode = :countyCode AND " +
           "COALESCE(a.extendedDeadlineDate, a.deadlineDate) <= :deadlineDate AND " +
           "(a.status = 'PENDING' OR a.status = 'IN_PROGRESS' OR a.status = 'PENDING_DOCUMENTATION' OR a.status = 'PENDING_MEDI_CAL')")
    List<ApplicationEntity> findApproachingDeadlineByCounty(
            @Param("countyCode") String countyCode,
            @Param("deadlineDate") LocalDate deadlineDate);

    // Find overdue applications
    @Query("SELECT a FROM ApplicationEntity a WHERE " +
           "COALESCE(a.extendedDeadlineDate, a.deadlineDate) < CURRENT_DATE AND " +
           "(a.status = 'PENDING' OR a.status = 'IN_PROGRESS' OR a.status = 'PENDING_DOCUMENTATION' OR a.status = 'PENDING_MEDI_CAL')")
    List<ApplicationEntity> findOverdueApplications();

    // Find overdue applications by county
    @Query("SELECT a FROM ApplicationEntity a WHERE a.countyCode = :countyCode AND " +
           "COALESCE(a.extendedDeadlineDate, a.deadlineDate) < CURRENT_DATE AND " +
           "(a.status = 'PENDING' OR a.status = 'IN_PROGRESS' OR a.status = 'PENDING_DOCUMENTATION' OR a.status = 'PENDING_MEDI_CAL')")
    List<ApplicationEntity> findOverdueApplicationsByCounty(@Param("countyCode") String countyCode);

    // ==================== CIN/MEDS QUERIES ====================

    // Find by CIN
    Optional<ApplicationEntity> findByCin(String cin);

    // Find applications pending CIN clearance
    @Query("SELECT a FROM ApplicationEntity a WHERE (a.cinClearanceStatus IS NULL OR a.cinClearanceStatus = 'NOT_STARTED' OR a.cinClearanceStatus = 'IN_PROGRESS' OR a.cinClearanceStatus = 'POSSIBLE_MATCHES' OR a.cinClearanceStatus = 'MISMATCH_REVIEW')")
    List<ApplicationEntity> findPendingCINClearance();

    // Find applications pending Medi-Cal verification
    @Query("SELECT a FROM ApplicationEntity a WHERE (a.mediCalStatus IS NULL OR a.mediCalStatus = '' OR a.mediCalStatus = 'PENDING')")
    List<ApplicationEntity> findPendingMediCal();

    // Find applications without active Medi-Cal
    @Query("SELECT a FROM ApplicationEntity a WHERE a.mediCalStatus != 'ACTIVE' AND (a.status = 'PENDING' OR a.status = 'IN_PROGRESS')")
    List<ApplicationEntity> findWithoutActiveMediCal();

    // ==================== DATE RANGE QUERIES ====================

    // Find by application date range
    @Query("SELECT a FROM ApplicationEntity a WHERE a.applicationDate BETWEEN :startDate AND :endDate")
    List<ApplicationEntity> findByApplicationDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find by decision date range
    @Query("SELECT a FROM ApplicationEntity a WHERE a.decisionDate BETWEEN :startDate AND :endDate")
    List<ApplicationEntity> findByDecisionDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ==================== ASSESSMENT QUERIES ====================

    // Find applications with scheduled assessments
    @Query("SELECT a FROM ApplicationEntity a WHERE a.assessmentScheduled = true AND a.assessmentCompleted = false")
    List<ApplicationEntity> findWithScheduledAssessments();

    // Find applications with upcoming assessments
    @Query("SELECT a FROM ApplicationEntity a WHERE a.assessmentScheduled = true AND a.assessmentCompleted = false AND a.assessmentDate <= :date")
    List<ApplicationEntity> findWithUpcomingAssessments(@Param("date") LocalDate date);

    // ==================== DOCUMENTATION QUERIES ====================

    // Find applications with incomplete documentation
    @Query("SELECT a FROM ApplicationEntity a WHERE a.requiredDocsComplete = false AND (a.status = 'PENDING' OR a.status = 'PENDING_DOCUMENTATION')")
    List<ApplicationEntity> findWithIncompleteDocs();

    // Find applications missing SOC 873
    @Query("SELECT a FROM ApplicationEntity a WHERE a.soc873Received = false AND (a.status = 'PENDING' OR a.status = 'IN_PROGRESS')")
    List<ApplicationEntity> findMissingSoc873();

    // ==================== CASE CREATION QUERIES ====================

    // Find approved applications without cases created
    @Query("SELECT a FROM ApplicationEntity a WHERE a.status = 'APPROVED' AND (a.caseCreated = false OR a.caseCreated IS NULL)")
    List<ApplicationEntity> findApprovedWithoutCase();

    // Find by case ID
    Optional<ApplicationEntity> findByCaseId(Long caseId);

    // Find by case number
    Optional<ApplicationEntity> findByCaseNumber(String caseNumber);

    // ==================== STATISTICS QUERIES ====================

    // Count by status and county
    @Query("SELECT COUNT(a) FROM ApplicationEntity a WHERE a.countyCode = :countyCode AND a.status = :status")
    Long countByCountyAndStatus(
            @Param("countyCode") String countyCode,
            @Param("status") ApplicationStatus status);

    // Count pending by county
    @Query("SELECT COUNT(a) FROM ApplicationEntity a WHERE a.countyCode = :countyCode AND (a.status = 'PENDING' OR a.status = 'IN_PROGRESS' OR a.status = 'PENDING_DOCUMENTATION' OR a.status = 'PENDING_MEDI_CAL')")
    Long countPendingByCounty(@Param("countyCode") String countyCode);

    // Count overdue by county
    @Query("SELECT COUNT(a) FROM ApplicationEntity a WHERE a.countyCode = :countyCode AND " +
           "COALESCE(a.extendedDeadlineDate, a.deadlineDate) < CURRENT_DATE AND " +
           "(a.status = 'PENDING' OR a.status = 'IN_PROGRESS' OR a.status = 'PENDING_DOCUMENTATION' OR a.status = 'PENDING_MEDI_CAL')")
    Long countOverdueByCounty(@Param("countyCode") String countyCode);

    // ==================== COMPREHENSIVE SEARCH ====================

    @Query("SELECT a FROM ApplicationEntity a WHERE " +
           "(:applicationNumber IS NULL OR a.applicationNumber = :applicationNumber) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:countyCode IS NULL OR a.countyCode = :countyCode) AND " +
           "(:programType IS NULL OR a.programType = :programType) AND " +
           "(:assignedWorkerId IS NULL OR a.assignedWorkerId = :assignedWorkerId) AND " +
           "(:cin IS NULL OR a.cin = :cin) AND " +
           "(:startDate IS NULL OR a.applicationDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.applicationDate <= :endDate)")
    List<ApplicationEntity> searchApplications(
            @Param("applicationNumber") String applicationNumber,
            @Param("status") ApplicationStatus status,
            @Param("countyCode") String countyCode,
            @Param("programType") ProgramType programType,
            @Param("assignedWorkerId") String assignedWorkerId,
            @Param("cin") String cin,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
