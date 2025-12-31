package com.cmips.repository;

import com.cmips.entity.HealthCareCertificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthCareCertificationRepository extends JpaRepository<HealthCareCertificationEntity, Long> {

    // Find by case
    List<HealthCareCertificationEntity> findByCaseId(Long caseId);

    // Find active certification by case
    @Query("SELECT hcc FROM HealthCareCertificationEntity hcc WHERE hcc.caseId = :caseId AND hcc.status = 'ACTIVE'")
    Optional<HealthCareCertificationEntity> findActiveByCaseId(@Param("caseId") Long caseId);

    // Find by recipient
    List<HealthCareCertificationEntity> findByRecipientId(Long recipientId);

    // Find certifications due soon (for task triggers per BR SE 28)
    @Query("SELECT hcc FROM HealthCareCertificationEntity hcc WHERE " +
           "hcc.status = 'ACTIVE' AND hcc.documentationReceivedDate IS NULL AND " +
           "(hcc.dueDate BETWEEN :startDate AND :endDate OR " +
           "(hcc.goodCauseExtensionDueDate IS NOT NULL AND hcc.goodCauseExtensionDueDate BETWEEN :startDate AND :endDate))")
    List<HealthCareCertificationEntity> findCertificationsDueSoon(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find overdue certifications
    @Query("SELECT hcc FROM HealthCareCertificationEntity hcc WHERE " +
           "hcc.status = 'ACTIVE' AND hcc.documentationReceivedDate IS NULL AND " +
           "((hcc.goodCauseExtensionDueDate IS NULL AND hcc.dueDate < :today) OR " +
           "(hcc.goodCauseExtensionDueDate IS NOT NULL AND hcc.goodCauseExtensionDueDate < :today))")
    List<HealthCareCertificationEntity> findOverdueCertifications(@Param("today") LocalDate today);

    // Find by certification method
    List<HealthCareCertificationEntity> findByCertificationMethod(String method);

    // Find pending certifications that need follow-up (10 business days before due)
    @Query("SELECT hcc FROM HealthCareCertificationEntity hcc WHERE " +
           "hcc.status = 'ACTIVE' AND hcc.documentationReceivedDate IS NULL AND " +
           "hcc.dueDate = :targetDate")
    List<HealthCareCertificationEntity> findCertificationsDueOn(@Param("targetDate") LocalDate targetDate);

    // Find certifications with exceptions
    @Query("SELECT hcc FROM HealthCareCertificationEntity hcc WHERE hcc.exceptionGranted = true AND hcc.status = 'ACTIVE'")
    List<HealthCareCertificationEntity> findCertificationsWithException();

    // Find certifications sent to ESP
    List<HealthCareCertificationEntity> findBySentToEspTrue();

    // Check if case has active certification
    @Query("SELECT COUNT(hcc) > 0 FROM HealthCareCertificationEntity hcc WHERE hcc.caseId = :caseId AND hcc.status = 'ACTIVE'")
    boolean hasActiveCertification(@Param("caseId") Long caseId);
}
