package com.cmips.repository;

import com.cmips.entity.RecipientWaiverEntity;
import com.cmips.entity.RecipientWaiverEntity.WaiverStatus;
import com.cmips.entity.RecipientWaiverEntity.CountyDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RecipientWaiverEntity
 * Implements DSD Section 23 - CORI Waiver queries
 */
@Repository
public interface RecipientWaiverRepository extends JpaRepository<RecipientWaiverEntity, String> {

    // Find by recipient ID
    List<RecipientWaiverEntity> findByRecipientId(Long recipientId);

    // Find by provider ID
    List<RecipientWaiverEntity> findByProviderId(Long providerId);

    // Find by recipient and provider
    Optional<RecipientWaiverEntity> findByRecipientIdAndProviderId(Long recipientId, Long providerId);

    // Find active waiver for recipient-provider pair
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.recipientId = :recipientId AND w.providerId = :providerId AND w.status = 'APPROVED' AND (w.revoked = false OR w.revoked IS NULL) AND (w.expirationDate IS NULL OR w.expirationDate >= CURRENT_DATE)")
    Optional<RecipientWaiverEntity> findActiveWaiver(
            @Param("recipientId") Long recipientId,
            @Param("providerId") Long providerId);

    // Find by CORI ID
    Optional<RecipientWaiverEntity> findByCoriId(String coriId);

    // Find by status
    List<RecipientWaiverEntity> findByStatus(WaiverStatus status);

    // Find by county
    List<RecipientWaiverEntity> findByCountyCode(String countyCode);

    // Find by county and status
    List<RecipientWaiverEntity> findByCountyCodeAndStatus(String countyCode, WaiverStatus status);

    // Find pending waivers (requiring action)
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.status = 'PENDING_DISCLOSURE' OR w.status = 'DISCLOSED' OR w.status = 'PENDING_DECISION' OR w.status = 'WAIVER_REQUESTED' OR w.status = 'SOC_2298_PENDING' OR w.status = 'SOC_2298_SIGNED' OR w.status = 'COUNTY_REVIEW' OR w.status = 'SUPERVISOR_REVIEW'")
    List<RecipientWaiverEntity> findPendingWaivers();

    // Find pending waivers by county
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.countyCode = :countyCode AND (w.status = 'PENDING_DISCLOSURE' OR w.status = 'DISCLOSED' OR w.status = 'PENDING_DECISION' OR w.status = 'WAIVER_REQUESTED' OR w.status = 'SOC_2298_PENDING' OR w.status = 'SOC_2298_SIGNED' OR w.status = 'COUNTY_REVIEW' OR w.status = 'SUPERVISOR_REVIEW')")
    List<RecipientWaiverEntity> findPendingWaiversByCounty(@Param("countyCode") String countyCode);

    // Find waivers pending county review
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.status = 'COUNTY_REVIEW'")
    List<RecipientWaiverEntity> findPendingCountyReview();

    // Find waivers pending county review by county
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.countyCode = :countyCode AND w.status = 'COUNTY_REVIEW'")
    List<RecipientWaiverEntity> findPendingCountyReviewByCounty(@Param("countyCode") String countyCode);

    // Find waivers pending supervisor review
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.status = 'SUPERVISOR_REVIEW'")
    List<RecipientWaiverEntity> findPendingSupervisorReview();

    // Find approved waivers
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.status = 'APPROVED' AND (w.revoked = false OR w.revoked IS NULL)")
    List<RecipientWaiverEntity> findApprovedWaivers();

    // Find approved waivers by county
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.countyCode = :countyCode AND w.status = 'APPROVED' AND (w.revoked = false OR w.revoked IS NULL)")
    List<RecipientWaiverEntity> findApprovedWaiversByCounty(@Param("countyCode") String countyCode);

    // Find expiring waivers
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.status = 'APPROVED' AND w.expirationDate <= :expirationDate AND (w.revoked = false OR w.revoked IS NULL)")
    List<RecipientWaiverEntity> findExpiringWaivers(@Param("expirationDate") LocalDate expirationDate);

    // Find revoked waivers
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.revoked = true")
    List<RecipientWaiverEntity> findRevokedWaivers();

    // Find waivers by county reviewer
    List<RecipientWaiverEntity> findByCountyReviewerId(String countyReviewerId);

    // Find waivers by supervisor
    List<RecipientWaiverEntity> findBySupervisorId(String supervisorId);

    // Check if provider has active waiver with any recipient
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM RecipientWaiverEntity w WHERE w.providerId = :providerId AND w.status = 'APPROVED' AND (w.revoked = false OR w.revoked IS NULL)")
    boolean hasActiveWaiver(@Param("providerId") Long providerId);

    // Check if specific recipient-provider waiver exists
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM RecipientWaiverEntity w WHERE w.recipientId = :recipientId AND w.providerId = :providerId")
    boolean existsWaiver(
            @Param("recipientId") Long recipientId,
            @Param("providerId") Long providerId);

    // Count by status and county
    @Query("SELECT COUNT(w) FROM RecipientWaiverEntity w WHERE w.countyCode = :countyCode AND w.status = :status")
    Long countByCountyAndStatus(
            @Param("countyCode") String countyCode,
            @Param("status") WaiverStatus status);

    // Count pending by county
    @Query("SELECT COUNT(w) FROM RecipientWaiverEntity w WHERE w.countyCode = :countyCode AND (w.status = 'PENDING_DISCLOSURE' OR w.status = 'DISCLOSED' OR w.status = 'PENDING_DECISION' OR w.status = 'WAIVER_REQUESTED' OR w.status = 'SOC_2298_PENDING' OR w.status = 'SOC_2298_SIGNED' OR w.status = 'COUNTY_REVIEW' OR w.status = 'SUPERVISOR_REVIEW')")
    Long countPendingByCounty(@Param("countyCode") String countyCode);

    // Find by date range (request date)
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE w.waiverRequestDate BETWEEN :startDate AND :endDate")
    List<RecipientWaiverEntity> findByRequestDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Comprehensive search
    @Query("SELECT w FROM RecipientWaiverEntity w WHERE " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:countyCode IS NULL OR w.countyCode = :countyCode) AND " +
           "(:recipientId IS NULL OR w.recipientId = :recipientId) AND " +
           "(:providerId IS NULL OR w.providerId = :providerId) AND " +
           "(:countyDecision IS NULL OR w.countyDecision = :countyDecision)")
    List<RecipientWaiverEntity> searchWaivers(
            @Param("status") WaiverStatus status,
            @Param("countyCode") String countyCode,
            @Param("recipientId") Long recipientId,
            @Param("providerId") Long providerId,
            @Param("countyDecision") CountyDecision countyDecision);
}
