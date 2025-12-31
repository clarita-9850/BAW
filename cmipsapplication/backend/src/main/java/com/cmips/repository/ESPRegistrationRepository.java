package com.cmips.repository;

import com.cmips.entity.ESPRegistrationEntity;
import com.cmips.entity.ESPRegistrationEntity.UserType;
import com.cmips.entity.ESPRegistrationEntity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ESPRegistrationEntity
 * Implements ESP Registration tracking queries
 */
@Repository
public interface ESPRegistrationRepository extends JpaRepository<ESPRegistrationEntity, String> {

    // Find by recipient ID
    List<ESPRegistrationEntity> findByRecipientId(Long recipientId);

    // Find by provider ID
    List<ESPRegistrationEntity> findByProviderId(Long providerId);

    // Find completed registration by recipient
    @Query("SELECT e FROM ESPRegistrationEntity e WHERE e.recipientId = :recipientId AND e.status = 'COMPLETED'")
    Optional<ESPRegistrationEntity> findCompletedByRecipientId(@Param("recipientId") Long recipientId);

    // Find completed registration by provider
    @Query("SELECT e FROM ESPRegistrationEntity e WHERE e.providerId = :providerId AND e.status = 'COMPLETED'")
    Optional<ESPRegistrationEntity> findCompletedByProviderId(@Param("providerId") Long providerId);

    // Find by Keycloak user ID
    Optional<ESPRegistrationEntity> findByKeycloakUserId(String keycloakUserId);

    // Find by username
    Optional<ESPRegistrationEntity> findByUsername(String username);

    // Find by email
    List<ESPRegistrationEntity> findByEmail(String email);

    // Find by status
    List<ESPRegistrationEntity> findByStatus(RegistrationStatus status);

    // Find by user type
    List<ESPRegistrationEntity> findByUserType(UserType userType);

    // Find by user type and status
    List<ESPRegistrationEntity> findByUserTypeAndStatus(UserType userType, RegistrationStatus status);

    // Find in-progress registrations
    @Query("SELECT e FROM ESPRegistrationEntity e WHERE e.status = 'STARTED' OR e.status = 'IDENTITY_VALIDATED' OR e.status = 'EMAIL_VERIFIED' OR e.status = 'USERNAME_CREATED' OR e.status = 'PASSWORD_CREATED'")
    List<ESPRegistrationEntity> findInProgressRegistrations();

    // Find by session ID
    Optional<ESPRegistrationEntity> findBySessionId(String sessionId);

    // Check if recipient already registered
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM ESPRegistrationEntity e WHERE e.recipientId = :recipientId AND e.status = 'COMPLETED'")
    boolean isRecipientRegistered(@Param("recipientId") Long recipientId);

    // Check if provider already registered
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM ESPRegistrationEntity e WHERE e.providerId = :providerId AND e.status = 'COMPLETED'")
    boolean isProviderRegistered(@Param("providerId") Long providerId);

    // Check if username is taken
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM ESPRegistrationEntity e WHERE e.username = :username")
    boolean isUsernameTaken(@Param("username") String username);

    // Find expired registrations (started but not completed within time limit)
    @Query("SELECT e FROM ESPRegistrationEntity e WHERE e.registrationStartedAt < :cutoffTime AND e.status != 'COMPLETED' AND e.status != 'FAILED' AND e.status != 'EXPIRED' AND e.status != 'CANCELLED'")
    List<ESPRegistrationEntity> findExpiredRegistrations(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find by date range
    @Query("SELECT e FROM ESPRegistrationEntity e WHERE e.registrationStartedAt BETWEEN :startDate AND :endDate")
    List<ESPRegistrationEntity> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find completed registrations by date range
    @Query("SELECT e FROM ESPRegistrationEntity e WHERE e.status = 'COMPLETED' AND e.registrationCompletedAt BETWEEN :startDate AND :endDate")
    List<ESPRegistrationEntity> findCompletedByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Count by status
    Long countByStatus(RegistrationStatus status);

    // Count by user type and status
    Long countByUserTypeAndStatus(UserType userType, RegistrationStatus status);

    // Find registrations at specific step
    List<ESPRegistrationEntity> findByCurrentStep(Integer currentStep);

    // Find registrations stuck at a step (for monitoring)
    @Query("SELECT e FROM ESPRegistrationEntity e WHERE e.currentStep = :step AND e.updatedAt < :cutoffTime AND e.status != 'COMPLETED' AND e.status != 'FAILED' AND e.status != 'EXPIRED'")
    List<ESPRegistrationEntity> findStuckAtStep(
            @Param("step") Integer step,
            @Param("cutoffTime") LocalDateTime cutoffTime);

    // Find by IP address (for fraud detection)
    List<ESPRegistrationEntity> findByIpAddress(String ipAddress);

    // Count registrations from same IP in time period (for rate limiting)
    @Query("SELECT COUNT(e) FROM ESPRegistrationEntity e WHERE e.ipAddress = :ipAddress AND e.registrationStartedAt > :sinceTime")
    Long countByIpAddressSince(
            @Param("ipAddress") String ipAddress,
            @Param("sinceTime") LocalDateTime sinceTime);

    // Find registrations with errors
    @Query("SELECT e FROM ESPRegistrationEntity e WHERE e.errorCount > 0 ORDER BY e.errorCount DESC")
    List<ESPRegistrationEntity> findWithErrors();

    // Statistics: Count completed by user type
    @Query("SELECT e.userType, COUNT(e) FROM ESPRegistrationEntity e WHERE e.status = 'COMPLETED' GROUP BY e.userType")
    List<Object[]> countCompletedByUserType();

    // Statistics: Average step where abandoned
    @Query("SELECT AVG(e.currentStep) FROM ESPRegistrationEntity e WHERE e.status = 'EXPIRED' OR e.status = 'CANCELLED'")
    Double averageAbandonmentStep();
}
