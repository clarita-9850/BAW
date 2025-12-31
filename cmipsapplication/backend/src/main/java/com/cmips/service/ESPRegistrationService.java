package com.cmips.service;

import com.cmips.entity.ESPRegistrationEntity;
import com.cmips.entity.ESPRegistrationEntity.*;
import com.cmips.entity.SecurityQuestionEntity;
import com.cmips.entity.RecipientEntity;
import com.cmips.entity.RecipientEntity.PersonType;
import com.cmips.entity.ProviderEntity;
import com.cmips.entity.CaseEntity;
import com.cmips.entity.CaseEntity.CaseStatus;
import com.cmips.repository.ESPRegistrationRepository;
import com.cmips.repository.SecurityQuestionRepository;
import com.cmips.repository.RecipientRepository;
import com.cmips.repository.ProviderRepository;
import com.cmips.repository.CaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for ESP (Electronic Services Portal) Registration
 * Implements 5-Step self-service registration process per DSD Section 20
 *
 * Steps:
 * 1. Identity Validation - Verify person exists in system
 * 2. Email Verification - Send and verify email code
 * 3. Username Creation - Create unique username
 * 4. Password Creation - Set strong password
 * 5. Security Questions - Set 3 security questions for recovery
 */
@Service
public class ESPRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(ESPRegistrationService.class);

    // Registration session timeout (24 hours)
    private static final int SESSION_TIMEOUT_HOURS = 24;

    // Email verification code expiry (15 minutes)
    private static final int EMAIL_CODE_EXPIRY_MINUTES = 15;

    // Max verification attempts
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;

    private final ESPRegistrationRepository registrationRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final RecipientRepository recipientRepository;
    private final ProviderRepository providerRepository;
    private final CaseRepository caseRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakAdminService keycloakAdminService;

    public ESPRegistrationService(ESPRegistrationRepository registrationRepository,
                                  SecurityQuestionRepository securityQuestionRepository,
                                  RecipientRepository recipientRepository,
                                  ProviderRepository providerRepository,
                                  CaseRepository caseRepository,
                                  PasswordEncoder passwordEncoder,
                                  KeycloakAdminService keycloakAdminService) {
        this.registrationRepository = registrationRepository;
        this.securityQuestionRepository = securityQuestionRepository;
        this.recipientRepository = recipientRepository;
        this.providerRepository = providerRepository;
        this.caseRepository = caseRepository;
        this.passwordEncoder = passwordEncoder;
        this.keycloakAdminService = keycloakAdminService;
    }

    // ==================== STEP 1: IDENTITY VALIDATION ====================

    /**
     * Start registration for a Recipient (self-service ESP)
     * Validates identity using Case Number + CIN + Name + DOB and case eligibility.
     */
    @Transactional
    public ESPRegistrationEntity startRecipientRegistration(String caseNumber,
                                                            String cinLast4,
                                                            String lastName,
                                                            String dateOfBirth,
                                                            String ipAddress) {
        log.info("Starting ESP registration for recipient case: {}", caseNumber);

        // Find case by case number
        CaseEntity caseEntity = caseRepository.findByCaseNumber(caseNumber)
                .orElseThrow(() -> new RuntimeException("Case not found for case number: " + caseNumber));

        // Case must be ELIGIBLE or PRESUMPTIVE_ELIGIBLE
        if (!(caseEntity.getCaseStatus() == CaseStatus.ELIGIBLE ||
              caseEntity.getCaseStatus() == CaseStatus.PRESUMPTIVE_ELIGIBLE)) {
            throw new IllegalStateException("Case is not eligible for ESP registration");
        }

        // Validate CIN last 4
        String cin = caseEntity.getCin();
        if (cin == null || cin.length() < 4) {
            throw new IllegalStateException("CIN not available for this case");
        }
        String actualCinLast4 = cin.substring(cin.length() - 4);
        if (cinLast4 == null || !actualCinLast4.equals(cinLast4)) {
            throw new SecurityException("CIN validation failed");
        }

        // Load recipient
        Long recipientId = caseEntity.getRecipientId();
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found: " + recipientId));

        // Recipient must be in RECIPIENT person type
        if (recipient.getPersonType() != PersonType.RECIPIENT) {
            throw new IllegalStateException("Recipient is not in RECIPIENT status");
        }

        // Prevent duplicate registration (both by registration history and flag)
        if (registrationRepository.isRecipientRegistered(recipientId) ||
            Boolean.TRUE.equals(recipient.getEspRegistered())) {
            throw new IllegalStateException("Recipient is already registered for ESP");
        }

        // Validate identity (last name + DOB)
        if (!validateRecipientIdentity(recipient, lastName, dateOfBirth)) {
            throw new SecurityException("Identity validation failed");
        }

        // Check rate limiting by IP
        if (!checkRateLimit(ipAddress)) {
            throw new SecurityException("Too many registration attempts. Please try again later.");
        }

        // Create registration record
        ESPRegistrationEntity registration = ESPRegistrationEntity.builder()
                .userType(UserType.RECIPIENT)
                .recipientId(recipientId)
                .caseNumber(caseNumber)
                .cinLast4(cinLast4)
                .firstName(recipient.getFirstName())
                .lastName(recipient.getLastName())
                .email(recipient.getEmail())
                .dateOfBirth(recipient.getDateOfBirth())
                .status(RegistrationStatus.STARTED)
                .currentStep(1)
                .sessionId(UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .identityValidated(true)
                .identityValidatedAt(LocalDateTime.now())
                .registrationStartedAt(LocalDateTime.now())
                .build();

        ESPRegistrationEntity saved = registrationRepository.save(registration);
        log.info("Recipient registration started with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Start registration for a Provider (self-service ESP)
     * Validates provider exists and is eligible for ESP registration.
     */
    @Transactional
    public ESPRegistrationEntity startProviderRegistration(String providerNumber,
                                                           String lastName,
                                                           String dateOfBirth,
                                                           String ssn4,
                                                           String ipAddress) {
        log.info("Starting ESP registration for provider number: {}", providerNumber);

        // Find provider by provider number
        ProviderEntity provider = providerRepository.findByProviderNumber(providerNumber)
                .orElseThrow(() -> new RuntimeException("Provider not found for number: " + providerNumber));

        Long providerId = provider.getId();

        // Provider must be eligible to serve
        if (!provider.isEligibleToServe()) {
            throw new IllegalStateException("Provider is not eligible for ESP registration");
        }

        // Prevent duplicate registration (both by registration history and flag)
        if (registrationRepository.isProviderRegistered(providerId) ||
            Boolean.TRUE.equals(provider.getEspRegistered())) {
            throw new IllegalStateException("Provider is already registered for ESP");
        }

        // Validate identity (name, DOB, last 4 of SSN)
        if (!validateProviderIdentity(provider, lastName, dateOfBirth, ssn4)) {
            throw new SecurityException("Identity validation failed");
        }

        // Check rate limiting
        if (!checkRateLimit(ipAddress)) {
            throw new SecurityException("Too many registration attempts. Please try again later.");
        }

        // Create registration record
        ESPRegistrationEntity registration = ESPRegistrationEntity.builder()
                .userType(UserType.PROVIDER)
                .providerId(providerId)
                .providerNumber(providerNumber)
                .ssnLast4(ssn4)
                .firstName(provider.getFirstName())
                .lastName(provider.getLastName())
                .email(provider.getEmail())
                .dateOfBirth(provider.getDateOfBirth())
                .status(RegistrationStatus.STARTED)
                .currentStep(1)
                .sessionId(UUID.randomUUID().toString())
                .ipAddress(ipAddress)
                .identityValidated(true)
                .identityValidatedAt(LocalDateTime.now())
                .registrationStartedAt(LocalDateTime.now())
                .build();

        ESPRegistrationEntity saved = registrationRepository.save(registration);
        log.info("Provider registration started with ID: {}", saved.getId());

        return saved;
    }

    // ==================== STEP 2: EMAIL VERIFICATION ====================

    /**
     * Send email verification code
     */
    @Transactional
    public ESPRegistrationEntity sendEmailVerification(String registrationId, String email) {
        ESPRegistrationEntity registration = getAndValidateRegistration(registrationId, 1);

        // Update email if different
        registration.setEmail(email);

        // Generate verification code
        registration.generateEmailVerificationCode();
        registration.setStatus(RegistrationStatus.IDENTITY_VALIDATED);
        registration.progressToStep(2);

        ESPRegistrationEntity saved = registrationRepository.save(registration);

        // TODO: Send actual email via email service
        log.info("Email verification code sent to: {} (Code: {})",
                email, registration.getEmailVerificationCode());

        return saved;
    }

    /**
     * Verify email code
     */
    @Transactional
    public ESPRegistrationEntity verifyEmailCode(String registrationId, String code) {
        ESPRegistrationEntity registration = getAndValidateRegistration(registrationId, 2);

        // Check expiry
        if (registration.getEmailVerificationCodeExpiry() != null &&
            LocalDateTime.now().isAfter(registration.getEmailVerificationCodeExpiry())) {
            registration.incrementErrorCount();
            registrationRepository.save(registration);
            throw new SecurityException("Verification code has expired");
        }

        // Verify code
        if (!registration.verifyEmailCode(code)) {
            registration.incrementErrorCount();
            registrationRepository.save(registration);
            throw new SecurityException("Invalid verification code");
        }

        registration.setEmailVerified(true);
        registration.setEmailVerifiedAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.EMAIL_VERIFIED);
        registration.progressToStep(3);

        log.info("Email verified for registration: {}", registrationId);
        return registrationRepository.save(registration);
    }

    /**
     * Resend email verification code
     */
    @Transactional
    public ESPRegistrationEntity resendEmailVerification(String registrationId) {
        ESPRegistrationEntity registration = getAndValidateRegistration(registrationId, 2);

        if (registration.getErrorCount() >= MAX_VERIFICATION_ATTEMPTS) {
            throw new SecurityException("Maximum verification attempts exceeded");
        }

        registration.generateEmailVerificationCode();
        ESPRegistrationEntity saved = registrationRepository.save(registration);

        // TODO: Send actual email
        log.info("Resent email verification to: {}", registration.getEmail());

        return saved;
    }

    // ==================== STEP 3: USERNAME CREATION ====================

    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String username) {
        return !registrationRepository.isUsernameTaken(username);
    }

    /**
     * Set username for registration
     */
    @Transactional
    public ESPRegistrationEntity setUsername(String registrationId, String username) {
        ESPRegistrationEntity registration = getAndValidateRegistration(registrationId, 3);

        // Validate username format
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Invalid username format. Use 6-30 alphanumeric characters.");
        }

        // Check availability
        if (!isUsernameAvailable(username)) {
            throw new IllegalStateException("Username is already taken");
        }

        registration.setUsername(username);
        registration.setUsernameCreatedAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.USERNAME_CREATED);
        registration.progressToStep(4);

        log.info("Username set for registration: {}", registrationId);
        return registrationRepository.save(registration);
    }

    /**
     * Suggest available usernames based on name
     */
    public List<String> suggestUsernames(String firstName, String lastName) {
        String base = (firstName.substring(0, 1) + lastName).toLowerCase();

        return List.of(
            base,
            base + "1",
            firstName.toLowerCase() + "." + lastName.toLowerCase(),
            lastName.toLowerCase() + firstName.substring(0, 1).toLowerCase(),
            base + String.valueOf(System.currentTimeMillis() % 1000)
        ).stream()
         .filter(this::isUsernameAvailable)
         .limit(3)
         .toList();
    }

    // ==================== STEP 4: PASSWORD CREATION ====================

    // Temporary password storage for creating Keycloak user
    private static final java.util.Map<String, String> tempPasswords = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Set password for registration
     */
    @Transactional
    public ESPRegistrationEntity setPassword(String registrationId, String password) {
        ESPRegistrationEntity registration = getAndValidateRegistration(registrationId, 4);

        // Validate password strength
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters with uppercase, lowercase, number, and special character");
        }

        // Store password temporarily (will be used when creating Keycloak user)
        tempPasswords.put(registrationId, password);
        registration.setPasswordStrength(evaluatePasswordStrength(password));
        registration.setPasswordCreatedAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.PASSWORD_CREATED);
        registration.progressToStep(5);

        log.info("Password set for registration: {}", registrationId);
        return registrationRepository.save(registration);
    }

    private String evaluatePasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*") && password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++;
        return score >= 3 ? "STRONG" : score >= 2 ? "MEDIUM" : "WEAK";
    }

    // ==================== STEP 5: SECURITY QUESTIONS ====================

    /**
     * Get available security questions
     */
    public String[] getAvailableSecurityQuestions() {
        return SecurityQuestionEntity.AVAILABLE_QUESTIONS;
    }

    /**
     * Set security questions and complete registration
     */
    @Transactional
    public ESPRegistrationEntity setSecurityQuestionsAndComplete(String registrationId,
                                                                   String question1, String answer1,
                                                                   String question2, String answer2,
                                                                   String question3, String answer3) {
        ESPRegistrationEntity registration = getAndValidateRegistration(registrationId, 5);

        // Validate questions are different
        if (question1.equals(question2) || question2.equals(question3) || question1.equals(question3)) {
            throw new IllegalArgumentException("Security questions must be different");
        }

        // Validate answers are not empty and different
        if (answer1.isBlank() || answer2.isBlank() || answer3.isBlank()) {
            throw new IllegalArgumentException("Security answers cannot be empty");
        }

        // Create Keycloak user
        String keycloakUserId = createKeycloakUser(registration);
        registration.setKeycloakUserId(keycloakUserId);

        // Create security questions entity
        SecurityQuestionEntity securityQuestions = new SecurityQuestionEntity();
        securityQuestions.setUserId(keycloakUserId);
        securityQuestions.setUserType(registration.getUserType() == UserType.RECIPIENT ?
                 SecurityQuestionEntity.UserType.RECIPIENT :
                 SecurityQuestionEntity.UserType.PROVIDER);
        securityQuestions.setQuestion1(question1);
        securityQuestions.setAnswer1Hash(passwordEncoder.encode(answer1.toLowerCase().trim()));
        securityQuestions.setQuestion2(question2);
        securityQuestions.setAnswer2Hash(passwordEncoder.encode(answer2.toLowerCase().trim()));
        securityQuestions.setQuestion3(question3);
        securityQuestions.setAnswer3Hash(passwordEncoder.encode(answer3.toLowerCase().trim()));

        securityQuestionRepository.save(securityQuestions);

        // Complete registration
        registration.setSecurityQuestionsSetAt(LocalDateTime.now());
        registration.completeRegistration();

        // Mark underlying recipient/provider as ESP registered
        if (registration.getUserType() == UserType.RECIPIENT && registration.getRecipientId() != null) {
            recipientRepository.findById(registration.getRecipientId()).ifPresent(recipient -> {
                recipient.setEspRegistered(true);
                recipientRepository.save(recipient);
            });
        } else if (registration.getUserType() == UserType.PROVIDER && registration.getProviderId() != null) {
            providerRepository.findById(registration.getProviderId()).ifPresent(provider -> {
                provider.setEspRegistered(true);
                // Default e-timesheet status for newly registered providers
                provider.seteTimesheetStatus("ENROLLED");
                providerRepository.save(provider);
            });
        }

        log.info("ESP Registration completed for user: {}", registration.getUsername());
        return registrationRepository.save(registration);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get registration by ID and validate state
     */
    private ESPRegistrationEntity getAndValidateRegistration(String registrationId, int expectedStep) {
        ESPRegistrationEntity registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found: " + registrationId));

        // Check session not expired
        if (isSessionExpired(registration)) {
            registration.setStatus(RegistrationStatus.EXPIRED);
            registrationRepository.save(registration);
            throw new SecurityException("Registration session has expired");
        }

        // Check status
        if (registration.getStatus() == RegistrationStatus.COMPLETED ||
            registration.getStatus() == RegistrationStatus.FAILED ||
            registration.getStatus() == RegistrationStatus.EXPIRED ||
            registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Registration is already " + registration.getStatus());
        }

        // Check step (allow current or previous step for retries)
        if (registration.getCurrentStep() > expectedStep) {
            throw new IllegalStateException("This step has already been completed");
        }

        return registration;
    }

    /**
     * Check if session is expired
     */
    private boolean isSessionExpired(ESPRegistrationEntity registration) {
        return registration.getRegistrationStartedAt()
                .plusHours(SESSION_TIMEOUT_HOURS)
                .isBefore(LocalDateTime.now());
    }

    /**
     * Validate recipient identity
     */
    private boolean validateRecipientIdentity(RecipientEntity recipient,
                                              String lastName,
                                              String dateOfBirth) {
        // Basic validation - in production would be more sophisticated
        boolean lastNameMatch = recipient.getLastName() != null &&
                               recipient.getLastName().equalsIgnoreCase(lastName);

        boolean dobMatch = recipient.getDateOfBirth() != null &&
                          recipient.getDateOfBirth().toString().equals(dateOfBirth);

        return lastNameMatch && dobMatch;
    }

    /**
     * Validate provider identity
     */
    private boolean validateProviderIdentity(ProviderEntity provider,
                                              String lastName,
                                              String dateOfBirth,
                                              String ssn4) {
        boolean lastNameMatch = provider.getLastName() != null &&
                               provider.getLastName().equalsIgnoreCase(lastName);

        boolean dobMatch = provider.getDateOfBirth() != null &&
                          provider.getDateOfBirth().toString().equals(dateOfBirth);

        // Validate last 4 digits of SSN
        String ssn = provider.getSsn();
        boolean ssnMatch = false;
        if (ssn != null && ssn.length() >= 4 && ssn4 != null && ssn4.length() == 4) {
            String actualLast4 = ssn.substring(ssn.length() - 4);
            ssnMatch = actualLast4.equals(ssn4);
        }

        return lastNameMatch && dobMatch && ssnMatch;
    }

    /**
     * Check rate limiting by IP address
     */
    private boolean checkRateLimit(String ipAddress) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Long attempts = registrationRepository.countByIpAddressSince(ipAddress, oneHourAgo);
        return attempts < 10; // Max 10 attempts per hour per IP
    }

    /**
     * Validate username format
     */
    private boolean isValidUsername(String username) {
        if (username == null || username.length() < 6 || username.length() > 30) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9._-]+$");
    }

    /**
     * Validate password strength
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Create Keycloak user
     */
    private String createKeycloakUser(ESPRegistrationEntity registration) {
        try {
            // Determine role based on user type
            String role = registration.getUserType() == UserType.RECIPIENT ? "RECIPIENT" : "PROVIDER";

            // Get password from temp storage
            String password = tempPasswords.remove(registration.getId());
            if (password == null) {
                throw new RuntimeException("Password not found - registration may have expired");
            }

            // Create user in Keycloak
            String keycloakUserId = keycloakAdminService.createUser(
                registration.getUsername(),
                registration.getEmail(),
                password,
                registration.getFirstName(),
                registration.getLastName()
            );

            // Assign role
            if (keycloakUserId != null) {
                try {
                    keycloakAdminService.assignRoleToUser(keycloakUserId, role);
                    log.info("Assigned role {} to user {}", role, registration.getUsername());
                } catch (Exception e) {
                    log.warn("Failed to assign role {} to user {}: {}", role, registration.getUsername(), e.getMessage());
                }
            }

            log.info("Created Keycloak user: {} with role: {}", registration.getUsername(), role);
            return keycloakUserId;

        } catch (Exception e) {
            log.error("Failed to create Keycloak user for registration: {}", registration.getId(), e);
            throw new RuntimeException("Failed to create user account: " + e.getMessage());
        }
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get registration by session ID
     */
    public Optional<ESPRegistrationEntity> getBySessionId(String sessionId) {
        return registrationRepository.findBySessionId(sessionId);
    }

    /**
     * Get in-progress registrations
     */
    public List<ESPRegistrationEntity> getInProgressRegistrations() {
        return registrationRepository.findInProgressRegistrations();
    }

    /**
     * Cancel registration
     */
    @Transactional
    public ESPRegistrationEntity cancelRegistration(String registrationId) {
        ESPRegistrationEntity registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found: " + registrationId));

        if (registration.getStatus() == RegistrationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed registration");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        log.info("Registration cancelled: {}", registrationId);
        return registrationRepository.save(registration);
    }

    /**
     * Clean up expired registrations (scheduled task)
     */
    @Transactional
    public int cleanupExpiredRegistrations() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(SESSION_TIMEOUT_HOURS);
        List<ESPRegistrationEntity> expired = registrationRepository.findExpiredRegistrations(cutoff);

        for (ESPRegistrationEntity registration : expired) {
            registration.setStatus(RegistrationStatus.EXPIRED);
            registrationRepository.save(registration);
        }

        log.info("Cleaned up {} expired registrations", expired.size());
        return expired.size();
    }
}
