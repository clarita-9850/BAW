package com.cmips.repository;

import com.cmips.entity.ProviderAssignmentEntity;
import com.cmips.entity.ProviderAssignmentEntity.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProviderAssignmentRepository extends JpaRepository<ProviderAssignmentEntity, Long> {

    // Find by case
    List<ProviderAssignmentEntity> findByCaseId(Long caseId);

    // Find by provider
    List<ProviderAssignmentEntity> findByProviderId(Long providerId);

    // Find by recipient
    List<ProviderAssignmentEntity> findByRecipientId(Long recipientId);

    // Find active assignments by case
    List<ProviderAssignmentEntity> findByCaseIdAndStatus(Long caseId, AssignmentStatus status);

    // Find active assignments by provider
    List<ProviderAssignmentEntity> findByProviderIdAndStatus(Long providerId, AssignmentStatus status);

    // Find active assignments by recipient
    List<ProviderAssignmentEntity> findByRecipientIdAndStatus(Long recipientId, AssignmentStatus status);

    // Check if provider is assigned to case
    boolean existsByProviderIdAndCaseIdAndStatus(Long providerId, Long caseId, AssignmentStatus status);

    // Count active cases for provider (per BR PVM 71)
    @Query("SELECT COUNT(DISTINCT pa.caseId) FROM ProviderAssignmentEntity pa WHERE pa.providerId = :providerId AND pa.status = 'ACTIVE'")
    Integer countActiveCasesByProvider(@Param("providerId") Long providerId);

    // Find providers with multiple active recipients (for overtime exemption eligibility)
    @Query("SELECT pa.providerId FROM ProviderAssignmentEntity pa WHERE pa.status = 'ACTIVE' GROUP BY pa.providerId HAVING COUNT(DISTINCT pa.recipientId) >= 2")
    List<Long> findProvidersWithMultipleRecipients();

    // Find assignments by provider type
    List<ProviderAssignmentEntity> findByProviderType(String providerType);

    // Find backup providers for a case
    List<ProviderAssignmentEntity> findByCaseIdAndIsBackupProviderTrue(Long caseId);

    // Find assignments with recipient waiver
    List<ProviderAssignmentEntity> findByHasRecipientWaiverTrue();

    // Find assignments with expiring recipient waivers
    @Query("SELECT pa FROM ProviderAssignmentEntity pa WHERE pa.hasRecipientWaiver = true AND pa.recipientWaiverEndDate <= :date")
    List<ProviderAssignmentEntity> findAssignmentsWithExpiringRecipientWaiver(@Param("date") LocalDate date);

    // Find assignments by relationship (for funding source impact)
    List<ProviderAssignmentEntity> findByRelationshipToRecipient(String relationship);

    // Find spouse or parent assignments (impact funding source per BR PVM 13)
    @Query("SELECT pa FROM ProviderAssignmentEntity pa WHERE pa.relationshipToRecipient IN ('SPOUSE', 'PARENT_OF_MINOR') AND pa.status = 'ACTIVE'")
    List<ProviderAssignmentEntity> findFundingSourceImpactingAssignments();

    // Find assignments with travel time
    List<ProviderAssignmentEntity> findByHasTravelTimeAgreementTrue();

    // Calculate total assigned hours for a provider across all cases
    @Query("SELECT COALESCE(SUM(pa.assignedHours), 0) FROM ProviderAssignmentEntity pa WHERE pa.providerId = :providerId AND pa.status = 'ACTIVE'")
    Double getTotalAssignedHoursForProvider(@Param("providerId") Long providerId);

    // Calculate total authorized hours for a case across all providers
    @Query("SELECT COALESCE(SUM(pa.authorizedHoursMonthly), 0) FROM ProviderAssignmentEntity pa WHERE pa.caseId = :caseId AND pa.status = 'ACTIVE'")
    Double getTotalAuthorizedHoursForCase(@Param("caseId") Long caseId);

    // Find assignments needing initial notification (SOC 2271)
    @Query("SELECT pa FROM ProviderAssignmentEntity pa WHERE pa.initialNotificationSent = false OR pa.initialNotificationSent IS NULL")
    List<ProviderAssignmentEntity> findAssignmentsNeedingNotification();
}
