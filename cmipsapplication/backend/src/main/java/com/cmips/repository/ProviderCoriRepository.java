package com.cmips.repository;

import com.cmips.entity.ProviderCoriEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderCoriRepository extends JpaRepository<ProviderCoriEntity, Long> {

    // Find by provider
    List<ProviderCoriEntity> findByProviderId(Long providerId);

    // Find active CORI records by provider
    @Query("SELECT pc FROM ProviderCoriEntity pc WHERE pc.providerId = :providerId AND pc.status = 'ACTIVE'")
    List<ProviderCoriEntity> findActiveCoriByProviderId(@Param("providerId") Long providerId);

    // Find by conviction date (per BR PVM 31)
    Optional<ProviderCoriEntity> findByProviderIdAndConvictionDate(Long providerId, LocalDate convictionDate);

    // Find Tier 1 convictions
    @Query("SELECT pc FROM ProviderCoriEntity pc WHERE pc.providerId = :providerId AND pc.tier = 'TIER_1' AND pc.status = 'ACTIVE'")
    List<ProviderCoriEntity> findActiveTier1ByProviderId(@Param("providerId") Long providerId);

    // Find Tier 2 convictions
    @Query("SELECT pc FROM ProviderCoriEntity pc WHERE pc.providerId = :providerId AND pc.tier = 'TIER_2' AND pc.status = 'ACTIVE'")
    List<ProviderCoriEntity> findActiveTier2ByProviderId(@Param("providerId") Long providerId);

    // Find CORI with active general exception
    @Query("SELECT pc FROM ProviderCoriEntity pc WHERE pc.providerId = :providerId " +
           "AND pc.generalExceptionGranted = true " +
           "AND (pc.generalExceptionEndDate IS NULL OR pc.generalExceptionEndDate >= :today)")
    List<ProviderCoriEntity> findCoriWithActiveGeneralException(
            @Param("providerId") Long providerId,
            @Param("today") LocalDate today);

    // Find CORI with expiring general exception
    @Query("SELECT pc FROM ProviderCoriEntity pc WHERE " +
           "pc.generalExceptionGranted = true AND " +
           "pc.generalExceptionEndDate BETWEEN :startDate AND :endDate")
    List<ProviderCoriEntity> findCoriWithExpiringGeneralException(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Count active CORI records for provider
    @Query("SELECT COUNT(pc) FROM ProviderCoriEntity pc WHERE pc.providerId = :providerId AND pc.status = 'ACTIVE'")
    Long countActiveCoriByProviderId(@Param("providerId") Long providerId);

    // Check if provider has any Tier 1 conviction
    @Query("SELECT COUNT(pc) > 0 FROM ProviderCoriEntity pc WHERE pc.providerId = :providerId AND pc.tier = 'TIER_1' AND pc.status = 'ACTIVE'")
    boolean hasActiveTier1Conviction(@Param("providerId") Long providerId);

    // Check if provider has Tier 2 conviction without waiver
    @Query("SELECT COUNT(pc) > 0 FROM ProviderCoriEntity pc WHERE pc.providerId = :providerId " +
           "AND pc.tier = 'TIER_2' AND pc.status = 'ACTIVE' " +
           "AND (pc.generalExceptionGranted = false OR pc.generalExceptionGranted IS NULL)")
    boolean hasTier2WithoutWaiver(@Param("providerId") Long providerId);
}
