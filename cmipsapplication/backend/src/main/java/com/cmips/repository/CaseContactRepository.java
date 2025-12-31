package com.cmips.repository;

import com.cmips.entity.CaseContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseContactRepository extends JpaRepository<CaseContactEntity, Long> {

    // Find by case
    List<CaseContactEntity> findByCaseId(Long caseId);

    // Find active contacts by case
    @Query("SELECT cc FROM CaseContactEntity cc WHERE cc.caseId = :caseId AND cc.status = 'ACTIVE'")
    List<CaseContactEntity> findActiveContactsByCaseId(@Param("caseId") Long caseId);

    // Find by recipient
    List<CaseContactEntity> findByRecipientId(Long recipientId);

    // Find by contact type
    List<CaseContactEntity> findByCaseIdAndContactType(Long caseId, String contactType);

    // Find designees (Guardian/Conservator for payroll sync)
    @Query("SELECT cc FROM CaseContactEntity cc WHERE cc.caseId = :caseId AND cc.isDesignee = true AND cc.status = 'ACTIVE'")
    List<CaseContactEntity> findDesigneesByCaseId(@Param("caseId") Long caseId);

    // Find contacts needing payroll sync
    @Query("SELECT cc FROM CaseContactEntity cc WHERE cc.isDesignee = true AND (cc.syncedToPayroll = false OR cc.syncedToPayroll IS NULL)")
    List<CaseContactEntity> findContactsNeedingPayrollSync();

    // Find emergency contacts
    @Query("SELECT cc FROM CaseContactEntity cc WHERE cc.caseId = :caseId AND cc.contactType = 'EMERGENCY' AND cc.status = 'ACTIVE'")
    List<CaseContactEntity> findEmergencyContactsByCaseId(@Param("caseId") Long caseId);
}
