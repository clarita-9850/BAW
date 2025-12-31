package com.example.kafkaeventdrivenapp.repository;

import com.example.kafkaeventdrivenapp.entity.CaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<CaseEntity, Long> {
    
    /**
     * Find all cases for a specific person
     */
    List<CaseEntity> findByPerson_PersonId(Long personId);
    
    /**
     * Find case by CMIPS case number
     */
    Optional<CaseEntity> findByCmipsCaseNumber(String cmipsCaseNumber);
    
    /**
     * Find case by legacy case number
     */
    Optional<CaseEntity> findByLegacyCaseNumber(String legacyCaseNumber);
    
    /**
     * Find all cases assigned to a specific caseworker
     */
    List<CaseEntity> findByAssignedCaseworkerId(String caseworkerId);
    
    /**
     * Find cases by status
     */
    List<CaseEntity> findByCaseStatus(String caseStatus);
    
    /**
     * Find active cases for a caseworker
     */
    @Query("SELECT c FROM CaseEntity c WHERE c.assignedCaseworkerId = :caseworkerId AND c.caseStatus = 'ACTIVE'")
    List<CaseEntity> findActiveCasesByCaseworker(@Param("caseworkerId") String caseworkerId);
    
    /**
     * Find cases by county code
     */
    List<CaseEntity> findByCountyCode(String countyCode);
    
    /**
     * Find cases by county code and status
     */
    List<CaseEntity> findByCountyCodeAndCaseStatus(String countyCode, String caseStatus);
    
    /**
     * Check if CMIPS case number exists
     */
    boolean existsByCmipsCaseNumber(String cmipsCaseNumber);
    
    /**
     * Check if legacy case number exists
     */
    boolean existsByLegacyCaseNumber(String legacyCaseNumber);
    
    /**
     * Count cases by status for a caseworker
     */
    @Query("SELECT COUNT(c) FROM CaseEntity c WHERE c.assignedCaseworkerId = :caseworkerId AND c.caseStatus = :status")
    long countByCaseworkerAndStatus(@Param("caseworkerId") String caseworkerId, @Param("status") String status);
}

