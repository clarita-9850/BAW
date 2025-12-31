package com.cmips.repository;

import com.cmips.entity.ReferralEntity;
import com.cmips.entity.ReferralEntity.ReferralStatus;
import com.cmips.entity.ReferralEntity.ReferralSource;
import com.cmips.entity.ReferralEntity.ReferralPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ReferralEntity
 * Implements DSD Section 20 - Referral Management queries
 */
@Repository
public interface ReferralRepository extends JpaRepository<ReferralEntity, String> {

    // Find by recipient ID
    List<ReferralEntity> findByRecipientId(Long recipientId);

    // Find by status
    List<ReferralEntity> findByStatus(ReferralStatus status);

    // Find by county
    List<ReferralEntity> findByCountyCode(String countyCode);

    // Find by county and status
    List<ReferralEntity> findByCountyCodeAndStatus(String countyCode, ReferralStatus status);

    // Find by assigned worker
    List<ReferralEntity> findByAssignedWorkerId(String assignedWorkerId);

    // Find by assigned worker and status
    List<ReferralEntity> findByAssignedWorkerIdAndStatus(String assignedWorkerId, ReferralStatus status);

    // Find by source
    List<ReferralEntity> findBySource(ReferralSource source);

    // Find by priority
    List<ReferralEntity> findByPriority(ReferralPriority priority);

    // Find open referrals
    @Query("SELECT r FROM ReferralEntity r WHERE r.status = 'OPEN' OR r.status = 'PENDING' OR r.status = 'IN_PROGRESS'")
    List<ReferralEntity> findOpenReferrals();

    // Find open referrals by county
    @Query("SELECT r FROM ReferralEntity r WHERE r.countyCode = :countyCode AND (r.status = 'OPEN' OR r.status = 'PENDING' OR r.status = 'IN_PROGRESS')")
    List<ReferralEntity> findOpenReferralsByCounty(@Param("countyCode") String countyCode);

    // Search referrals by potential recipient information
    @Query("SELECT r FROM ReferralEntity r WHERE " +
           "(:name IS NULL OR UPPER(r.potentialRecipientName) LIKE UPPER(CONCAT('%', :name, '%'))) AND " +
           "(:phone IS NULL OR r.potentialRecipientPhone = :phone) AND " +
           "(:countyCode IS NULL OR r.countyCode = :countyCode)")
    List<ReferralEntity> searchByPotentialRecipient(
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("countyCode") String countyCode);

    // Find referrals by date range
    @Query("SELECT r FROM ReferralEntity r WHERE r.referralDate BETWEEN :startDate AND :endDate")
    List<ReferralEntity> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find referrals needing follow-up
    @Query("SELECT r FROM ReferralEntity r WHERE r.followUpDate <= :date AND (r.status = 'OPEN' OR r.status = 'PENDING')")
    List<ReferralEntity> findNeedingFollowUp(@Param("date") LocalDate date);

    // Find referrals needing follow-up by county
    @Query("SELECT r FROM ReferralEntity r WHERE r.countyCode = :countyCode AND r.followUpDate <= :date AND (r.status = 'OPEN' OR r.status = 'PENDING')")
    List<ReferralEntity> findNeedingFollowUpByCounty(
            @Param("countyCode") String countyCode,
            @Param("date") LocalDate date);

    // Find converted referrals
    @Query("SELECT r FROM ReferralEntity r WHERE r.convertedToApplication = true")
    List<ReferralEntity> findConvertedReferrals();

    // Find by external reference number
    Optional<ReferralEntity> findByExternalReferenceNumber(String externalReferenceNumber);

    // Find by referring agency
    List<ReferralEntity> findByReferringAgencyName(String referringAgencyName);

    // Count by status and county
    @Query("SELECT COUNT(r) FROM ReferralEntity r WHERE r.countyCode = :countyCode AND r.status = :status")
    Long countByCountyAndStatus(
            @Param("countyCode") String countyCode,
            @Param("status") ReferralStatus status);

    // Count open referrals by county
    @Query("SELECT COUNT(r) FROM ReferralEntity r WHERE r.countyCode = :countyCode AND (r.status = 'OPEN' OR r.status = 'PENDING' OR r.status = 'IN_PROGRESS')")
    Long countOpenByCounty(@Param("countyCode") String countyCode);

    // Find urgent/high priority referrals
    @Query("SELECT r FROM ReferralEntity r WHERE (r.priority = 'URGENT' OR r.priority = 'HIGH') AND (r.status = 'OPEN' OR r.status = 'PENDING')")
    List<ReferralEntity> findUrgentReferrals();

    // Find urgent referrals by county
    @Query("SELECT r FROM ReferralEntity r WHERE r.countyCode = :countyCode AND (r.priority = 'URGENT' OR r.priority = 'HIGH') AND (r.status = 'OPEN' OR r.status = 'PENDING')")
    List<ReferralEntity> findUrgentReferralsByCounty(@Param("countyCode") String countyCode);

    // Comprehensive search
    @Query("SELECT r FROM ReferralEntity r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:countyCode IS NULL OR r.countyCode = :countyCode) AND " +
           "(:source IS NULL OR r.source = :source) AND " +
           "(:priority IS NULL OR r.priority = :priority) AND " +
           "(:assignedWorkerId IS NULL OR r.assignedWorkerId = :assignedWorkerId) AND " +
           "(:startDate IS NULL OR r.referralDate >= :startDate) AND " +
           "(:endDate IS NULL OR r.referralDate <= :endDate)")
    List<ReferralEntity> searchReferrals(
            @Param("status") ReferralStatus status,
            @Param("countyCode") String countyCode,
            @Param("source") ReferralSource source,
            @Param("priority") ReferralPriority priority,
            @Param("assignedWorkerId") String assignedWorkerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
