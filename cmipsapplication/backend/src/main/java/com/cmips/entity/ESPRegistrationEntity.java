package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ESP Registration Entity - Tracks self-service portal registration process
 * Based on DSD ESP Registration Documentation
 *
 * This entity tracks the 5-step registration process for recipients and providers
 * to register for the Electronic Services Portal (ESP).
 */
@Entity
@Table(name = "esp_registrations")
public class ESPRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // User Type
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    // Person reference
    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "provider_id")
    private Long providerId;

    // Keycloak user ID (after account creation)
    @Column(name = "keycloak_user_id", length = 100)
    private String keycloakUserId;

    // Registration Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationStatus status = RegistrationStatus.STARTED;

    // Current Step (1-5)
    @Column(name = "current_step")
    private Integer currentStep = 1;

    // ==================== STEP 1: IDENTITY VALIDATION ====================

    @Column(name = "identity_validated")
    private Boolean identityValidated = false;

    @Column(name = "identity_validation_date")
    private LocalDateTime identityValidationDate;

    // For Recipients
    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "cin_last4", length = 4)
    private String cinLast4;

    // For Providers
    @Column(name = "provider_number", length = 20)
    private String providerNumber;

    @Column(name = "ssn_last4", length = 4)
    private String ssnLast4;

    // Common validation fields
    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "validation_attempts")
    private Integer validationAttempts = 0;

    @Column(name = "validation_locked")
    private Boolean validationLocked = false;

    @Column(name = "validation_lock_until")
    private LocalDateTime validationLockUntil;

    // ==================== STEP 2: EMAIL VERIFICATION ====================

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "email_verification_code", length = 10)
    private String emailVerificationCode;

    @Column(name = "email_verification_sent_at")
    private LocalDateTime emailVerificationSentAt;

    @Column(name = "email_verification_expires_at")
    private LocalDateTime emailVerificationExpiresAt;

    @Column(name = "email_verification_attempts")
    private Integer emailVerificationAttempts = 0;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    // ==================== STEP 3: USERNAME CREATION ====================

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "username_created")
    private Boolean usernameCreated = false;

    @Column(name = "username_created_at")
    private LocalDateTime usernameCreatedAt;

    // ==================== STEP 4: PASSWORD CREATION ====================

    @Column(name = "password_created")
    private Boolean passwordCreated = false;

    @Column(name = "password_created_at")
    private LocalDateTime passwordCreatedAt;

    @Column(name = "password_strength", length = 20)
    private String passwordStrength; // WEAK, MEDIUM, STRONG

    // ==================== STEP 5: SECURITY QUESTIONS ====================

    @Column(name = "security_questions_set")
    private Boolean securityQuestionsSet = false;

    @Column(name = "security_questions_set_at")
    private LocalDateTime securityQuestionsSetAt;

    // ==================== REGISTRATION COMPLETION ====================

    @Column(name = "registration_started_at")
    private LocalDateTime registrationStartedAt;

    @Column(name = "registration_completed_at")
    private LocalDateTime registrationCompletedAt;

    @Column(name = "welcome_email_sent")
    private Boolean welcomeEmailSent = false;

    @Column(name = "welcome_email_sent_at")
    private LocalDateTime welcomeEmailSentAt;

    // ==================== ERROR TRACKING ====================

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "error_count")
    private Integer errorCount = 0;

    // ==================== SESSION TRACKING ====================

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // ==================== AUDIT FIELDS ====================

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== ENUMS ====================

    public enum UserType {
        RECIPIENT,
        PROVIDER
    }

    public enum RegistrationStatus {
        STARTED,                    // Registration initiated
        IDENTITY_VALIDATED,         // Step 1 complete
        EMAIL_VERIFIED,             // Step 2 complete
        USERNAME_CREATED,           // Step 3 complete
        PASSWORD_CREATED,           // Step 4 complete
        COMPLETED,                  // All steps complete
        FAILED,                     // Registration failed
        EXPIRED,                    // Session expired
        CANCELLED                   // User cancelled
    }

    // ==================== LIFECYCLE HOOKS ====================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        registrationStartedAt = LocalDateTime.now();
        if (status == null) {
            status = RegistrationStatus.STARTED;
        }
        if (currentStep == null) {
            currentStep = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Generate 6-digit verification code
     */
    public void generateEmailVerificationCode() {
        this.emailVerificationCode = String.format("%06d", (int) (Math.random() * 1000000));
        this.emailVerificationSentAt = LocalDateTime.now();
        this.emailVerificationExpiresAt = LocalDateTime.now().plusMinutes(15);
        this.emailVerificationAttempts = 0;
    }

    /**
     * Check if verification code is expired
     */
    public boolean isVerificationCodeExpired() {
        return emailVerificationExpiresAt != null &&
               LocalDateTime.now().isAfter(emailVerificationExpiresAt);
    }

    /**
     * Verify email code
     */
    public boolean verifyEmailCode(String code) {
        if (isVerificationCodeExpired()) return false;
        if (emailVerificationAttempts >= 3) return false;

        emailVerificationAttempts++;

        if (emailVerificationCode != null && emailVerificationCode.equals(code)) {
            emailVerified = true;
            emailVerifiedAt = LocalDateTime.now();
            currentStep = 3;
            status = RegistrationStatus.EMAIL_VERIFIED;
            return true;
        }
        return false;
    }

    /**
     * Progress to next step
     */
    public void progressToStep(int step) {
        this.currentStep = step;
        switch (step) {
            case 2:
                this.status = RegistrationStatus.IDENTITY_VALIDATED;
                this.identityValidated = true;
                this.identityValidationDate = LocalDateTime.now();
                break;
            case 3:
                this.status = RegistrationStatus.EMAIL_VERIFIED;
                break;
            case 4:
                this.status = RegistrationStatus.USERNAME_CREATED;
                this.usernameCreated = true;
                this.usernameCreatedAt = LocalDateTime.now();
                break;
            case 5:
                this.status = RegistrationStatus.PASSWORD_CREATED;
                this.passwordCreated = true;
                this.passwordCreatedAt = LocalDateTime.now();
                break;
        }
    }

    /**
     * Complete registration
     */
    public void completeRegistration(String keycloakUserId) {
        this.keycloakUserId = keycloakUserId;
        this.securityQuestionsSet = true;
        this.securityQuestionsSetAt = LocalDateTime.now();
        this.status = RegistrationStatus.COMPLETED;
        this.registrationCompletedAt = LocalDateTime.now();
    }

    /**
     * Get step name for display
     */
    public String getCurrentStepName() {
        switch (currentStep) {
            case 1: return "Identity Validation";
            case 2: return "Email Verification";
            case 3: return "Username Creation";
            case 4: return "Password Creation";
            case 5: return "Security Questions";
            default: return "Unknown";
        }
    }

    // ==================== CONSTRUCTORS ====================

    public ESPRegistrationEntity() {
    }

    public static ESPRegistrationEntityBuilder builder() {
        return new ESPRegistrationEntityBuilder();
    }

    // ==================== GETTERS AND SETTERS ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public void setKeycloakUserId(String keycloakUserId) {
        this.keycloakUserId = keycloakUserId;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public Boolean getIdentityValidated() {
        return identityValidated;
    }

    public void setIdentityValidated(Boolean identityValidated) {
        this.identityValidated = identityValidated;
    }

    public LocalDateTime getIdentityValidationDate() {
        return identityValidationDate;
    }

    public void setIdentityValidationDate(LocalDateTime identityValidationDate) {
        this.identityValidationDate = identityValidationDate;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getCinLast4() {
        return cinLast4;
    }

    public void setCinLast4(String cinLast4) {
        this.cinLast4 = cinLast4;
    }

    public String getProviderNumber() {
        return providerNumber;
    }

    public void setProviderNumber(String providerNumber) {
        this.providerNumber = providerNumber;
    }

    public String getSsnLast4() {
        return ssnLast4;
    }

    public void setSsnLast4(String ssnLast4) {
        this.ssnLast4 = ssnLast4;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getValidationAttempts() {
        return validationAttempts;
    }

    public void setValidationAttempts(Integer validationAttempts) {
        this.validationAttempts = validationAttempts;
    }

    public Boolean getValidationLocked() {
        return validationLocked;
    }

    public void setValidationLocked(Boolean validationLocked) {
        this.validationLocked = validationLocked;
    }

    public LocalDateTime getValidationLockUntil() {
        return validationLockUntil;
    }

    public void setValidationLockUntil(LocalDateTime validationLockUntil) {
        this.validationLockUntil = validationLockUntil;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getEmailVerificationCode() {
        return emailVerificationCode;
    }

    public void setEmailVerificationCode(String emailVerificationCode) {
        this.emailVerificationCode = emailVerificationCode;
    }

    public LocalDateTime getEmailVerificationSentAt() {
        return emailVerificationSentAt;
    }

    public void setEmailVerificationSentAt(LocalDateTime emailVerificationSentAt) {
        this.emailVerificationSentAt = emailVerificationSentAt;
    }

    public LocalDateTime getEmailVerificationExpiresAt() {
        return emailVerificationExpiresAt;
    }

    public void setEmailVerificationExpiresAt(LocalDateTime emailVerificationExpiresAt) {
        this.emailVerificationExpiresAt = emailVerificationExpiresAt;
    }

    public Integer getEmailVerificationAttempts() {
        return emailVerificationAttempts;
    }

    public void setEmailVerificationAttempts(Integer emailVerificationAttempts) {
        this.emailVerificationAttempts = emailVerificationAttempts;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getUsernameCreated() {
        return usernameCreated;
    }

    public void setUsernameCreated(Boolean usernameCreated) {
        this.usernameCreated = usernameCreated;
    }

    public LocalDateTime getUsernameCreatedAt() {
        return usernameCreatedAt;
    }

    public void setUsernameCreatedAt(LocalDateTime usernameCreatedAt) {
        this.usernameCreatedAt = usernameCreatedAt;
    }

    public Boolean getPasswordCreated() {
        return passwordCreated;
    }

    public void setPasswordCreated(Boolean passwordCreated) {
        this.passwordCreated = passwordCreated;
    }

    public LocalDateTime getPasswordCreatedAt() {
        return passwordCreatedAt;
    }

    public void setPasswordCreatedAt(LocalDateTime passwordCreatedAt) {
        this.passwordCreatedAt = passwordCreatedAt;
    }

    public String getPasswordStrength() {
        return passwordStrength;
    }

    public void setPasswordStrength(String passwordStrength) {
        this.passwordStrength = passwordStrength;
    }

    public Boolean getSecurityQuestionsSet() {
        return securityQuestionsSet;
    }

    public void setSecurityQuestionsSet(Boolean securityQuestionsSet) {
        this.securityQuestionsSet = securityQuestionsSet;
    }

    public LocalDateTime getSecurityQuestionsSetAt() {
        return securityQuestionsSetAt;
    }

    public void setSecurityQuestionsSetAt(LocalDateTime securityQuestionsSetAt) {
        this.securityQuestionsSetAt = securityQuestionsSetAt;
    }

    public LocalDateTime getRegistrationStartedAt() {
        return registrationStartedAt;
    }

    public void setRegistrationStartedAt(LocalDateTime registrationStartedAt) {
        this.registrationStartedAt = registrationStartedAt;
    }

    public LocalDateTime getRegistrationCompletedAt() {
        return registrationCompletedAt;
    }

    public void setRegistrationCompletedAt(LocalDateTime registrationCompletedAt) {
        this.registrationCompletedAt = registrationCompletedAt;
    }

    public Boolean getWelcomeEmailSent() {
        return welcomeEmailSent;
    }

    public void setWelcomeEmailSent(Boolean welcomeEmailSent) {
        this.welcomeEmailSent = welcomeEmailSent;
    }

    public LocalDateTime getWelcomeEmailSentAt() {
        return welcomeEmailSentAt;
    }

    public void setWelcomeEmailSentAt(LocalDateTime welcomeEmailSentAt) {
        this.welcomeEmailSentAt = welcomeEmailSentAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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

    // ==================== BUILDER ====================

    public static class ESPRegistrationEntityBuilder {
        private final ESPRegistrationEntity entity = new ESPRegistrationEntity();

        public ESPRegistrationEntityBuilder userType(UserType userType) {
            entity.setUserType(userType);
            return this;
        }

        public ESPRegistrationEntityBuilder recipientId(Long recipientId) {
            entity.setRecipientId(recipientId);
            return this;
        }

        public ESPRegistrationEntityBuilder providerId(Long providerId) {
            entity.setProviderId(providerId);
            return this;
        }

        public ESPRegistrationEntityBuilder caseNumber(String caseNumber) {
            entity.setCaseNumber(caseNumber);
            return this;
        }

        public ESPRegistrationEntityBuilder cinLast4(String cinLast4) {
            entity.setCinLast4(cinLast4);
            return this;
        }

        public ESPRegistrationEntityBuilder providerNumber(String providerNumber) {
            entity.setProviderNumber(providerNumber);
            return this;
        }

        public ESPRegistrationEntityBuilder ssnLast4(String ssnLast4) {
            entity.setSsnLast4(ssnLast4);
            return this;
        }

        public ESPRegistrationEntityBuilder email(String email) {
            entity.setEmail(email);
            return this;
        }

        public ESPRegistrationEntityBuilder firstName(String firstName) {
            entity.setFirstName(firstName);
            return this;
        }

        public ESPRegistrationEntityBuilder lastName(String lastName) {
            entity.setLastName(lastName);
            return this;
        }

        public ESPRegistrationEntityBuilder phone(String phone) {
            // Phone not stored in entity, ignore
            return this;
        }

        public ESPRegistrationEntityBuilder dateOfBirth(java.time.LocalDate dateOfBirth) {
            entity.setDateOfBirth(dateOfBirth != null ? dateOfBirth.toString() : null);
            return this;
        }

        public ESPRegistrationEntityBuilder status(RegistrationStatus status) {
            entity.setStatus(status);
            return this;
        }

        public ESPRegistrationEntityBuilder currentStep(Integer step) {
            entity.setCurrentStep(step);
            return this;
        }

        public ESPRegistrationEntityBuilder sessionId(String sessionId) {
            entity.setSessionId(sessionId);
            return this;
        }

        public ESPRegistrationEntityBuilder ipAddress(String ipAddress) {
            entity.setIpAddress(ipAddress);
            return this;
        }

        public ESPRegistrationEntityBuilder identityValidated(Boolean validated) {
            entity.setIdentityValidated(validated);
            return this;
        }

        public ESPRegistrationEntityBuilder identityValidatedAt(java.time.LocalDateTime time) {
            entity.setIdentityValidationDate(time);
            return this;
        }

        public ESPRegistrationEntityBuilder registrationStartedAt(java.time.LocalDateTime time) {
            entity.setRegistrationStartedAt(time);
            return this;
        }

        public ESPRegistrationEntity build() {
            return entity;
        }
    }

    // Additional helper methods needed by ESPRegistrationService
    public LocalDateTime getEmailVerificationCodeExpiry() {
        return emailVerificationExpiresAt;
    }

    public void incrementErrorCount() {
        this.errorCount = (this.errorCount == null ? 0 : this.errorCount) + 1;
    }

    // Complete registration without keycloak ID (set separately)
    public void completeRegistration() {
        this.securityQuestionsSet = true;
        this.securityQuestionsSetAt = LocalDateTime.now();
        this.status = RegistrationStatus.COMPLETED;
        this.registrationCompletedAt = LocalDateTime.now();
    }
}
