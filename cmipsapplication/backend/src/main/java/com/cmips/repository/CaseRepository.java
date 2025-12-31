package com.cmips.repository;

import com.cmips.entity.CaseEntity;
import com.cmips.entity.CaseEntity.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<CaseEntity, Long> {

    Optional<CaseEntity> findByCaseNumber(String caseNumber);

    List<CaseEntity> findByRecipientId(Long recipientId);

    List<CaseEntity> findByCaseOwnerId(String caseOwnerId);

    List<CaseEntity> findBySupervisorId(String supervisorId);

    List<CaseEntity> findByCountyCode(String countyCode);

    List<CaseEntity> findByCaseStatus(CaseStatus caseStatus);

    List<CaseEntity> findByCountyCodeAndCaseStatus(String countyCode, CaseStatus caseStatus);

    // Find cases by CIN
    Optional<CaseEntity> findByCin(String cin);

    // Find cases due for reassessment
    @Query("SELECT c FROM CaseEntity c WHERE c.reassessmentDueDate <= :date AND c.caseStatus IN ('ELIGIBLE', 'PRESUMPTIVE_ELIGIBLE')")
    List<CaseEntity> findCasesDueForReassessment(@Param("date") LocalDate date);

    // Find cases with pending health care certification
    @Query("SELECT c FROM CaseEntity c WHERE c.healthCareCertStatus = 'PENDING' AND c.healthCareCertDueDate <= :date")
    List<CaseEntity> findCasesWithPendingHealthCareCertification(@Param("date") LocalDate date);

    // Find cases by case owner and status
    List<CaseEntity> findByCaseOwnerIdAndCaseStatus(String caseOwnerId, CaseStatus caseStatus);

    // Find active cases by county
    @Query("SELECT c FROM CaseEntity c WHERE c.countyCode = :countyCode AND c.caseStatus IN ('ELIGIBLE', 'PRESUMPTIVE_ELIGIBLE', 'PENDING')")
    List<CaseEntity> findActiveCasesByCounty(@Param("countyCode") String countyCode);

    // Search cases by multiple criteria
    @Query("SELECT c FROM CaseEntity c WHERE " +
           "(:caseNumber IS NULL OR c.caseNumber LIKE %:caseNumber%) AND " +
           "(:cin IS NULL OR c.cin = :cin) AND " +
           "(:countyCode IS NULL OR c.countyCode = :countyCode) AND " +
           "(:caseOwnerId IS NULL OR c.caseOwnerId = :caseOwnerId) AND " +
           "(:caseStatus IS NULL OR c.caseStatus = :caseStatus)")
    List<CaseEntity> searchCases(
            @Param("caseNumber") String caseNumber,
            @Param("cin") String cin,
            @Param("countyCode") String countyCode,
            @Param("caseOwnerId") String caseOwnerId,
            @Param("caseStatus") CaseStatus caseStatus);

    // Count cases by status for a county
    @Query("SELECT COUNT(c) FROM CaseEntity c WHERE c.countyCode = :countyCode AND c.caseStatus = :status")
    Long countByCountyCodeAndStatus(@Param("countyCode") String countyCode, @Param("status") CaseStatus status);

    // Find inter-county transfer cases
    @Query("SELECT c FROM CaseEntity c WHERE c.transferStatus IS NOT NULL AND c.sendingCountyCode = :countyCode")
    List<CaseEntity> findOutgoingTransfersByCounty(@Param("countyCode") String countyCode);

    @Query("SELECT c FROM CaseEntity c WHERE c.transferStatus IS NOT NULL AND c.receivingCountyCode = :countyCode")
    List<CaseEntity> findIncomingTransfersByCounty(@Param("countyCode") String countyCode);
}
