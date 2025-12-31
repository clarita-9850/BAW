package com.cmips.controller;

import com.cmips.entity.ESPRegistrationEntity;
import com.cmips.service.ESPRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for ESP (Electronic Services Portal) Registration
 * Implements 5-Step self-service registration per DSD Section 20
 *
 * This is a PUBLIC endpoint - no authentication required for registration
 *
 * Steps:
 * 1. Identity Validation
 * 2. Email Verification
 * 3. Username Creation
 * 4. Password Creation
 * 5. Security Questions
 */
@RestController
@RequestMapping("/api/esp/register")
@CrossOrigin(origins = "*")
public class ESPRegistrationController {

    private static final Logger log = LoggerFactory.getLogger(ESPRegistrationController.class);

    private final ESPRegistrationService registrationService;

    public ESPRegistrationController(ESPRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    // ==================== STEP 1: IDENTITY VALIDATION ====================

    /**
     * Start registration for a Recipient
     * Validates identity and creates registration session
     */
    @PostMapping("/recipient/start")
    public ResponseEntity<?> startRecipientRegistration(@RequestBody Map<String, Object> request,
                                                         HttpServletRequest httpRequest) {
        try {
            String caseNumber = (String) request.get("caseNumber");
            String cinLast4 = (String) request.get("cinLast4");
            String lastName = (String) request.get("lastName");
            String dateOfBirth = (String) request.get("dateOfBirth");
            String ipAddress = getClientIpAddress(httpRequest);

            ESPRegistrationEntity registration = registrationService.startRecipientRegistration(
                    caseNumber, cinLast4, lastName, dateOfBirth, ipAddress);

            log.info("Recipient registration started for case: {}", caseNumber);

            // Return only safe fields
            return ResponseEntity.ok(Map.of(
                    "registrationId", registration.getId(),
                    "sessionId", registration.getSessionId(),
                    "currentStep", registration.getCurrentStep(),
                    "email", maskEmail(registration.getEmail()),
                    "message", "Identity validated. Please proceed to email verification."
            ));
        } catch (SecurityException e) {
            log.warn("Identity validation failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Registration state error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error starting recipient registration", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed. Please try again."));
        }
    }

    /**
     * Start registration for a Provider
     */
    @PostMapping("/provider/start")
    public ResponseEntity<?> startProviderRegistration(@RequestBody Map<String, Object> request,
                                                        HttpServletRequest httpRequest) {
        try {
            String providerNumber = (String) request.get("providerNumber");
            String lastName = (String) request.get("lastName");
            String dateOfBirth = (String) request.get("dateOfBirth");
            String ssn4 = (String) request.get("ssn4");
            String ipAddress = getClientIpAddress(httpRequest);

            ESPRegistrationEntity registration = registrationService.startProviderRegistration(
                    providerNumber, lastName, dateOfBirth, ssn4, ipAddress);

            log.info("Provider registration started for number: {}", providerNumber);

            return ResponseEntity.ok(Map.of(
                    "registrationId", registration.getId(),
                    "sessionId", registration.getSessionId(),
                    "currentStep", registration.getCurrentStep(),
                    "email", maskEmail(registration.getEmail()),
                    "message", "Identity validated. Please proceed to email verification."
            ));
        } catch (SecurityException e) {
            log.warn("Identity validation failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Registration state error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error starting provider registration", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed. Please try again."));
        }
    }

    // ==================== STEP 2: EMAIL VERIFICATION ====================

    /**
     * Send email verification code
     */
    @PostMapping("/{registrationId}/send-verification")
    public ResponseEntity<?> sendEmailVerification(@PathVariable String registrationId,
                                                    @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            ESPRegistrationEntity registration = registrationService.sendEmailVerification(registrationId, email);

            log.info("Verification code sent for registration: {}", registrationId);

            return ResponseEntity.ok(Map.of(
                    "registrationId", registrationId,
                    "currentStep", registration.getCurrentStep(),
                    "email", maskEmail(email),
                    "message", "Verification code sent to your email. Please check your inbox."
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error sending verification code", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Verify email code
     */
    @PostMapping("/{registrationId}/verify-email")
    public ResponseEntity<?> verifyEmailCode(@PathVariable String registrationId,
                                              @RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");

            ESPRegistrationEntity registration = registrationService.verifyEmailCode(registrationId, code);

            log.info("Email verified for registration: {}", registrationId);

            return ResponseEntity.ok(Map.of(
                    "registrationId", registrationId,
                    "currentStep", registration.getCurrentStep(),
                    "emailVerified", true,
                    "message", "Email verified. Please create your username."
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error verifying email code", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Resend verification code
     */
    @PostMapping("/{registrationId}/resend-verification")
    public ResponseEntity<?> resendVerification(@PathVariable String registrationId) {
        try {
            ESPRegistrationEntity registration = registrationService.resendEmailVerification(registrationId);

            return ResponseEntity.ok(Map.of(
                    "registrationId", registrationId,
                    "message", "New verification code sent to your email."
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error resending verification code", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== STEP 3: USERNAME CREATION ====================

    /**
     * Check username availability
     */
    @GetMapping("/username/check")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam String username) {
        try {
            boolean available = registrationService.isUsernameAvailable(username);
            return ResponseEntity.ok(Map.of(
                    "username", username,
                    "available", available
            ));
        } catch (Exception e) {
            log.error("Error checking username availability", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get username suggestions
     */
    @GetMapping("/username/suggest")
    public ResponseEntity<?> suggestUsernames(@RequestParam String firstName,
                                               @RequestParam String lastName) {
        try {
            List<String> suggestions = registrationService.suggestUsernames(firstName, lastName);
            return ResponseEntity.ok(Map.of("suggestions", suggestions));
        } catch (Exception e) {
            log.error("Error generating username suggestions", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Set username
     */
    @PostMapping("/{registrationId}/set-username")
    public ResponseEntity<?> setUsername(@PathVariable String registrationId,
                                          @RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");

            ESPRegistrationEntity registration = registrationService.setUsername(registrationId, username);

            log.info("Username set for registration: {}", registrationId);

            return ResponseEntity.ok(Map.of(
                    "registrationId", registrationId,
                    "currentStep", registration.getCurrentStep(),
                    "username", username,
                    "message", "Username created. Please set your password."
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error setting username", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== STEP 4: PASSWORD CREATION ====================

    /**
     * Set password
     */
    @PostMapping("/{registrationId}/set-password")
    public ResponseEntity<?> setPassword(@PathVariable String registrationId,
                                          @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");

            ESPRegistrationEntity registration = registrationService.setPassword(registrationId, password);

            log.info("Password set for registration: {}", registrationId);

            return ResponseEntity.ok(Map.of(
                    "registrationId", registrationId,
                    "currentStep", registration.getCurrentStep(),
                    "message", "Password created. Please set your security questions."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error setting password", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== STEP 5: SECURITY QUESTIONS ====================

    /**
     * Get available security questions
     */
    @GetMapping("/security-questions")
    public ResponseEntity<?> getSecurityQuestions() {
        try {
            String[] questions = registrationService.getAvailableSecurityQuestions();
            return ResponseEntity.ok(Map.of("questions", questions));
        } catch (Exception e) {
            log.error("Error getting security questions", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Set security questions and complete registration
     */
    @PostMapping("/{registrationId}/complete")
    public ResponseEntity<?> completeRegistration(@PathVariable String registrationId,
                                                   @RequestBody Map<String, String> request) {
        try {
            String question1 = request.get("question1");
            String answer1 = request.get("answer1");
            String question2 = request.get("question2");
            String answer2 = request.get("answer2");
            String question3 = request.get("question3");
            String answer3 = request.get("answer3");

            ESPRegistrationEntity registration = registrationService.setSecurityQuestionsAndComplete(
                    registrationId, question1, answer1, question2, answer2, question3, answer3);

            log.info("Registration completed: {} - Username: {}", registrationId, registration.getUsername());

            return ResponseEntity.ok(Map.of(
                    "registrationId", registrationId,
                    "username", registration.getUsername(),
                    "status", "COMPLETED",
                    "message", "Registration completed successfully. You can now log in with your username and password."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error completing registration", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to complete registration: " + e.getMessage()));
        }
    }

    // ==================== SESSION MANAGEMENT ====================

    /**
     * Get registration status by session ID
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getRegistrationBySession(@PathVariable String sessionId) {
        try {
            return registrationService.getBySessionId(sessionId)
                    .map(reg -> ResponseEntity.ok(Map.of(
                            "registrationId", reg.getId(),
                            "currentStep", reg.getCurrentStep(),
                            "status", reg.getStatus(),
                            "userType", reg.getUserType()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting registration by session", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cancel registration
     */
    @PostMapping("/{registrationId}/cancel")
    public ResponseEntity<?> cancelRegistration(@PathVariable String registrationId) {
        try {
            ESPRegistrationEntity cancelled = registrationService.cancelRegistration(registrationId);
            log.info("Registration cancelled: {}", registrationId);
            return ResponseEntity.ok(Map.of(
                    "registrationId", registrationId,
                    "status", "CANCELLED",
                    "message", "Registration cancelled."
            ));
        } catch (Exception e) {
            log.error("Error cancelling registration", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Mask email for display
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        String maskedLocal = local.length() > 2 ?
                local.substring(0, 2) + "***" :
                local.substring(0, 1) + "***";

        return maskedLocal + "@" + domain;
    }
}
