package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Security Question Entity - Stores user security questions for account recovery
 * Based on DSD ESP Registration - Step 5 Security Questions
 *
 * Users must set 3 security questions during ESP registration.
 * These are used for password recovery and account verification.
 */
@Entity
@Table(name = "security_questions")
public class SecurityQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // User reference (Keycloak user ID)
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    // User type
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;

    // Question 1
    @Column(name = "question1", nullable = false, length = 500)
    private String question1;

    @Column(name = "answer1_hash", nullable = false, length = 255)
    private String answer1Hash; // BCrypt hashed answer

    // Question 2
    @Column(name = "question2", nullable = false, length = 500)
    private String question2;

    @Column(name = "answer2_hash", nullable = false, length = 255)
    private String answer2Hash;

    // Question 3
    @Column(name = "question3", nullable = false, length = 500)
    private String question3;

    @Column(name = "answer3_hash", nullable = false, length = 255)
    private String answer3Hash;

    // Recovery tracking
    @Column(name = "last_used_for_recovery")
    private LocalDateTime lastUsedForRecovery;

    @Column(name = "recovery_attempt_count")
    private Integer recoveryAttemptCount = 0;

    @Column(name = "last_failed_attempt")
    private LocalDateTime lastFailedAttempt;

    @Column(name = "locked")
    private Boolean locked = false;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    // Audit Fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum UserType {
        RECIPIENT,
        PROVIDER
    }

    // Available security questions
    public static final String[] AVAILABLE_QUESTIONS = {
        "What is your mother's maiden name?",
        "What was the name of your first pet?",
        "What city were you born in?",
        "What is your favorite movie?",
        "What was the name of your elementary school?",
        "What is your favorite book?",
        "What was the make of your first car?",
        "What is your favorite sports team?",
        "What street did you grow up on?",
        "What was your childhood nickname?",
        "What is your oldest sibling's middle name?",
        "What was the name of your first employer?",
        "What is your favorite food?",
        "What was your favorite subject in school?",
        "What is the name of your favorite childhood friend?"
    };

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public SecurityQuestionEntity() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getQuestion1() {
        return question1;
    }

    public void setQuestion1(String question1) {
        this.question1 = question1;
    }

    public String getAnswer1Hash() {
        return answer1Hash;
    }

    public void setAnswer1Hash(String answer1Hash) {
        this.answer1Hash = answer1Hash;
    }

    public String getQuestion2() {
        return question2;
    }

    public void setQuestion2(String question2) {
        this.question2 = question2;
    }

    public String getAnswer2Hash() {
        return answer2Hash;
    }

    public void setAnswer2Hash(String answer2Hash) {
        this.answer2Hash = answer2Hash;
    }

    public String getQuestion3() {
        return question3;
    }

    public void setQuestion3(String question3) {
        this.question3 = question3;
    }

    public String getAnswer3Hash() {
        return answer3Hash;
    }

    public void setAnswer3Hash(String answer3Hash) {
        this.answer3Hash = answer3Hash;
    }

    public LocalDateTime getLastUsedForRecovery() {
        return lastUsedForRecovery;
    }

    public void setLastUsedForRecovery(LocalDateTime lastUsedForRecovery) {
        this.lastUsedForRecovery = lastUsedForRecovery;
    }

    public Integer getRecoveryAttemptCount() {
        return recoveryAttemptCount;
    }

    public void setRecoveryAttemptCount(Integer recoveryAttemptCount) {
        this.recoveryAttemptCount = recoveryAttemptCount;
    }

    public LocalDateTime getLastFailedAttempt() {
        return lastFailedAttempt;
    }

    public void setLastFailedAttempt(LocalDateTime lastFailedAttempt) {
        this.lastFailedAttempt = lastFailedAttempt;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
