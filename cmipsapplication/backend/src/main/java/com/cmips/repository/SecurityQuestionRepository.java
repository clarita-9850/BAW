package com.cmips.repository;

import com.cmips.entity.SecurityQuestionEntity;
import com.cmips.entity.SecurityQuestionEntity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for SecurityQuestionEntity
 * Implements security question storage and retrieval for account recovery
 */
@Repository
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestionEntity, String> {

    // Find by user ID (Keycloak user ID)
    Optional<SecurityQuestionEntity> findByUserId(String userId);

    // Find by user type
    List<SecurityQuestionEntity> findByUserType(UserType userType);

    // Check if user has security questions set
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SecurityQuestionEntity s WHERE s.userId = :userId")
    boolean hasSecurityQuestions(@Param("userId") String userId);

    // Find locked accounts
    @Query("SELECT s FROM SecurityQuestionEntity s WHERE s.locked = true")
    List<SecurityQuestionEntity> findLockedAccounts();

    // Find accounts with high recovery attempts
    @Query("SELECT s FROM SecurityQuestionEntity s WHERE s.recoveryAttemptCount >= :threshold")
    List<SecurityQuestionEntity> findHighRecoveryAttempts(@Param("threshold") Integer threshold);

    // Delete by user ID
    void deleteByUserId(String userId);
}
