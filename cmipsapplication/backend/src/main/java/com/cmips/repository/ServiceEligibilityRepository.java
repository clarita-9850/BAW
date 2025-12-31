package com.cmips.repository;

import com.cmips.entity.ServiceEligibilityEntity;
import com.cmips.entity.ServiceEligibilityEntity.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceEligibilityRepository extends JpaRepository<ServiceEligibilityEntity, Long> {

    // Find by case
    List<ServiceEligibilityEntity> findByCaseId(Long caseId);

    // Find by recipient
    List<ServiceEligibilityEntity> findByRecipientId(Long recipientId);

    // Find active eligibility for case
    @Query("SELECT se FROM ServiceEligibilityEntity se WHERE se.caseId = :caseId AND se.status = 'ACTIVE'")
    Optional<ServiceEligibilityEntity> findActiveEligibilityByCaseId(@Param("caseId") Long caseId);

    // Find by case and status
    List<ServiceEligibilityEntity> findByCaseIdAndStatus(Long caseId, String status);

    // Find by assessment type
    List<ServiceEligibilityEntity> findByAssessmentType(AssessmentType assessmentType);

    // Evidence History Search (per BR SE 25) - sorted by most recent Auth Start Date
    @Query("SELECT se FROM ServiceEligibilityEntity se WHERE se.caseId = :caseId ORDER BY se.authorizationStartDate DESC, se.createdAt DESC")
    List<ServiceEligibilityEntity> findEvidenceHistoryByCaseId(@Param("caseId") Long caseId);

    // Find eligibilities due for reassessment
    @Query("SELECT se FROM ServiceEligibilityEntity se WHERE se.reassessmentDueDate <= :date AND se.status = 'ACTIVE'")
    List<ServiceEligibilityEntity> findEligibilitiesDueForReassessment(@Param("date") LocalDate date);

    // Find eligibilities with authorization ending soon
    @Query("SELECT se FROM ServiceEligibilityEntity se WHERE se.authorizationEndDate BETWEEN :startDate AND :endDate AND se.status = 'ACTIVE'")
    List<ServiceEligibilityEntity> findEligibilitiesWithAuthorizationEndingSoon(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find most recent eligibility for case
    @Query("SELECT se FROM ServiceEligibilityEntity se WHERE se.caseId = :caseId ORDER BY se.authorizationStartDate DESC LIMIT 1")
    Optional<ServiceEligibilityEntity> findMostRecentByCaseId(@Param("caseId") Long caseId);

    // Find eligibilities exceeding HTG
    @Query("SELECT se FROM ServiceEligibilityEntity se WHERE " +
           "(se.htgDomestic = '+' OR se.htgRelated = '+' OR se.htgPersonal = '+' OR se.htgParamedical = '+') " +
           "AND se.status = 'ACTIVE'")
    List<ServiceEligibilityEntity> findEligibilitiesExceedingHtg();

    // Find eligibilities by assessor
    List<ServiceEligibilityEntity> findByAssessorId(String assessorId);

    // Find pending approvals
    @Query("SELECT se FROM ServiceEligibilityEntity se WHERE se.status = 'PENDING' AND se.approvalDate IS NULL")
    List<ServiceEligibilityEntity> findPendingApprovals();

    // Count by assessment type for reporting
    @Query("SELECT se.assessmentType, COUNT(se) FROM ServiceEligibilityEntity se WHERE se.caseId IN " +
           "(SELECT c.id FROM CaseEntity c WHERE c.countyCode = :countyCode) GROUP BY se.assessmentType")
    List<Object[]> countByAssessmentTypeForCounty(@Param("countyCode") String countyCode);
}
