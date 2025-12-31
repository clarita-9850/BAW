package com.cmips.repository;

import com.cmips.entity.OvertimeViolationEntity;
import com.cmips.entity.OvertimeViolationEntity.ViolationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OvertimeViolationRepository extends JpaRepository<OvertimeViolationEntity, Long> {

    // Find by provider
    List<OvertimeViolationEntity> findByProviderId(Long providerId);

    // Find by provider and status
    List<OvertimeViolationEntity> findByProviderIdAndStatus(Long providerId, ViolationStatus status);

    // Find active violations by provider
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.providerId = :providerId AND ov.status = 'ACTIVE'")
    List<OvertimeViolationEntity> findActiveViolationsByProviderId(@Param("providerId") Long providerId);

    // Count active violations for provider
    @Query("SELECT COUNT(ov) FROM OvertimeViolationEntity ov WHERE ov.providerId = :providerId AND ov.status = 'ACTIVE'")
    Integer countActiveViolationsByProviderId(@Param("providerId") Long providerId);

    // Find pending review violations
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.status = 'PENDING_REVIEW'")
    List<OvertimeViolationEntity> findPendingReviewViolations();

    // Find violations by service month/year
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.providerId = :providerId " +
           "AND ov.serviceMonth = :month AND ov.serviceYear = :year")
    List<OvertimeViolationEntity> findByProviderAndServiceMonth(
            @Param("providerId") Long providerId,
            @Param("month") Integer month,
            @Param("year") Integer year);

    // Find violations with county dispute pending
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.countyDisputeFiled = true AND ov.countyDisputeOutcome IS NULL")
    List<OvertimeViolationEntity> findViolationsWithPendingCountyDispute();

    // Find violations with supervisor review pending
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.countyReviewOutcome = 'OVERRIDE_REQUESTED' AND ov.supervisorReviewOutcome IS NULL")
    List<OvertimeViolationEntity> findViolationsWithPendingSupervisorReview();

    // Find violations with CDSS review pending
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.cdssReviewRequested = true AND ov.cdssReviewOutcome IS NULL")
    List<OvertimeViolationEntity> findViolationsWithPendingCdssReview();

    // Find violations with training pending for Violation #2
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.violationNumber = 2 " +
           "AND ov.trainingOffered = true AND ov.trainingCompleted = false " +
           "AND ov.trainingDueDate >= :today")
    List<OvertimeViolationEntity> findViolationsWithPendingTraining(@Param("today") LocalDate today);

    // Find violations where training is overdue
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.violationNumber = 2 " +
           "AND ov.trainingOffered = true AND ov.trainingCompleted = false " +
           "AND ov.trainingDueDate < :today")
    List<OvertimeViolationEntity> findViolationsWithOverdueTraining(@Param("today") LocalDate today);

    // Find violations resulting in termination (3rd or 4th)
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.violationNumber IN (3, 4) AND ov.status = 'ACTIVE'")
    List<OvertimeViolationEntity> findTerminationViolations();

    // Find providers due for reinstatement after 90-day suspension
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.violationNumber = 3 " +
           "AND ov.status = 'ACTIVE' AND ov.terminationEffectiveDate <= :cutoffDate")
    List<OvertimeViolationEntity> findProvidersReadyForReinstatement(@Param("cutoffDate") LocalDate cutoffDate);

    // Find violations that should become inactive (1 year clean record)
    @Query("SELECT ov FROM OvertimeViolationEntity ov WHERE ov.providerId = :providerId " +
           "AND ov.status = 'ACTIVE' AND ov.violationDate <= :cutoffDate")
    List<OvertimeViolationEntity> findViolationsOlderThanOneYear(
            @Param("providerId") Long providerId,
            @Param("cutoffDate") LocalDate cutoffDate);

    // Find violation by type
    List<OvertimeViolationEntity> findByViolationType(String violationType);

    // Check if violation already exists for service month (one per month rule)
    @Query("SELECT COUNT(ov) > 0 FROM OvertimeViolationEntity ov WHERE ov.providerId = :providerId " +
           "AND ov.serviceMonth = :month AND ov.serviceYear = :year AND ov.status IN ('PENDING_REVIEW', 'ACTIVE')")
    boolean hasViolationForServiceMonth(
            @Param("providerId") Long providerId,
            @Param("month") Integer month,
            @Param("year") Integer year);
}
