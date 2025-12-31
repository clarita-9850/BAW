package com.cmips.service;

import com.cmips.entity.ApplicationEntity;
import com.cmips.entity.ApplicationEntity.*;
import com.cmips.entity.ReferralEntity;
import com.cmips.entity.RecipientEntity;
import com.cmips.entity.CaseEntity;
import com.cmips.repository.ApplicationRepository;
import com.cmips.repository.ReferralRepository;
import com.cmips.repository.RecipientRepository;
import com.cmips.repository.CaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Application Processing
 * Implements DSD Section 20 - Application Processing with 45-day timeline
 */
@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);
    private static final int PROCESSING_DAYS = 45; // Federal requirement

    private final ApplicationRepository applicationRepository;
    private final ReferralRepository referralRepository;
    private final RecipientRepository recipientRepository;
    private final CaseRepository caseRepository;
    private final ReferralService referralService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ReferralRepository referralRepository,
                              RecipientRepository recipientRepository,
                              CaseRepository caseRepository,
                              ReferralService referralService) {
        this.applicationRepository = applicationRepository;
        this.referralRepository = referralRepository;
        this.recipientRepository = recipientRepository;
        this.caseRepository = caseRepository;
        this.referralService = referralService;
    }

    // ==================== CREATE APPLICATION ====================

    /**
     * Create a new application directly (without referral)
     */
    @Transactional
    public ApplicationEntity createApplication(ApplicationEntity application, Long recipientId, String userId) {
        log.info("Creating new application for recipient: {}", recipientId);

        // Set defaults
        application.setRecipientId(recipientId);
        application.setApplicationDate(LocalDate.now());
        application.setDeadlineDate(LocalDate.now().plusDays(PROCESSING_DAYS));
        application.setStatus(ApplicationStatus.PENDING);
        application.setCreatedBy(userId);

        // Update recipient to APPLICANT status
        updateRecipientToApplicant(recipientId, userId);

        ApplicationEntity saved = applicationRepository.save(application);
        log.info("Application created: {} with deadline: {}", saved.getApplicationNumber(), saved.getDeadlineDate());

        return saved;
    }

    /**
     * Create application from referral conversion
     */
    @Transactional
    public ApplicationEntity createFromReferral(String referralId, ApplicationEntity application, String userId) {
        log.info("Creating application from referral: {}", referralId);

        ReferralEntity referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new RuntimeException("Referral not found: " + referralId));

        // Create recipient from referral info if not exists
        Long recipientId = referral.getRecipientId();
        if (recipientId == null) {
            RecipientEntity recipient = createRecipientFromReferral(referral, userId);
            recipientId = recipient.getId();
        }

        // Set referral link
        application.setReferralId(referralId);

        // Create application
        ApplicationEntity saved = createApplication(application, recipientId, userId);

        // Update referral status
        referralService.convertToApplication(referralId, saved.getId(), recipientId, userId);

        return saved;
    }

    // ==================== CIN CLEARANCE ====================

    /**
     * Perform CIN clearance (simulated for MVP)
     */
    @Transactional
    public ApplicationEntity performCINClearance(String applicationId, String userId) {
        log.info("Performing CIN clearance for application: {}", applicationId);

        ApplicationEntity application = getApplicationById(applicationId);

        // Simulate SCI query
        application.setCinClearanceStatus(CINClearanceStatus.IN_PROGRESS);
        application.setCinClearanceDate(LocalDateTime.now());

        // Simulate finding/creating CIN
        String simulatedCin = generateSimulatedCIN();
        application.setCin(simulatedCin);
        application.setCinClearanceStatus(CINClearanceStatus.NEW_CIN_CREATED);
        application.setCinMatchType("NEW");
        application.setUpdatedBy(userId);

        // Update recipient with CIN
        if (application.getRecipientId() != null) {
            RecipientEntity recipient = recipientRepository.findById(application.getRecipientId()).orElse(null);
            if (recipient != null) {
                recipient.setCin(simulatedCin);
                recipientRepository.save(recipient);
            }
        }

        log.info("CIN clearance completed for application {}. CIN: {}", applicationId, simulatedCin);
        return applicationRepository.save(application);
    }

    /**
     * Handle CIN match selection (when possible matches found)
     */
    @Transactional
    public ApplicationEntity selectCINMatch(String applicationId, String selectedCin, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setCin(selectedCin);
        application.setCinClearanceStatus(CINClearanceStatus.CLEARED);
        application.setCinMatchType("EXACT");
        application.setUpdatedBy(userId);

        // Update recipient with CIN
        if (application.getRecipientId() != null) {
            RecipientEntity recipient = recipientRepository.findById(application.getRecipientId()).orElse(null);
            if (recipient != null) {
                recipient.setCin(selectedCin);
                recipientRepository.save(recipient);
            }
        }

        log.info("CIN match selected for application {}. CIN: {}", applicationId, selectedCin);
        return applicationRepository.save(application);
    }

    // ==================== MEDS VERIFICATION ====================

    /**
     * Perform MEDS verification (simulated for MVP)
     */
    @Transactional
    public ApplicationEntity performMEDSVerification(String applicationId, String userId) {
        log.info("Performing MEDS verification for application: {}", applicationId);

        ApplicationEntity application = getApplicationById(applicationId);

        // Simulate MEDS query
        application.setMedsVerificationDate(LocalDateTime.now());

        // Simulate active Medi-Cal (for MVP, always return active)
        application.setMediCalAidCode("1X");
        application.setMediCalStatus("ACTIVE");
        application.setMediCalEffectiveDate(LocalDate.now().minusMonths(6));
        application.setUpdatedBy(userId);

        log.info("MEDS verification completed for application {}. Status: {}", applicationId, application.getMediCalStatus());
        return applicationRepository.save(application);
    }

    // ==================== STATUS UPDATES ====================

    /**
     * Update application status
     */
    @Transactional
    public ApplicationEntity updateStatus(String applicationId, ApplicationStatus newStatus, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(newStatus);
        application.setUpdatedBy(userId);

        log.info("Application {} status changed from {} to {}", applicationId, oldStatus, newStatus);
        return applicationRepository.save(application);
    }

    /**
     * Schedule assessment
     */
    @Transactional
    public ApplicationEntity scheduleAssessment(String applicationId, LocalDate assessmentDate, String assessmentType, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setAssessmentScheduled(true);
        application.setAssessmentDate(assessmentDate);
        application.setAssessmentType(assessmentType);
        application.setStatus(ApplicationStatus.ASSESSMENT_SCHEDULED);
        application.setUpdatedBy(userId);

        log.info("Assessment scheduled for application {} on {}", applicationId, assessmentDate);
        return applicationRepository.save(application);
    }

    /**
     * Complete assessment
     */
    @Transactional
    public ApplicationEntity completeAssessment(String applicationId, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setAssessmentCompleted(true);
        application.setAssessmentCompletionDate(LocalDate.now());
        application.setStatus(ApplicationStatus.ASSESSMENT_COMPLETED);
        application.setUpdatedBy(userId);

        log.info("Assessment completed for application {}", applicationId);
        return applicationRepository.save(application);
    }

    // ==================== APPROVE/DENY ====================

    /**
     * Approve application and create case
     */
    @Transactional
    public ApplicationEntity approveApplication(String applicationId, String userId) {
        log.info("Approving application: {}", applicationId);

        ApplicationEntity application = getApplicationById(applicationId);

        // Validate prerequisites
        validateApprovalPrerequisites(application);

        // Update application
        application.setStatus(ApplicationStatus.APPROVED);
        application.setDecisionDate(LocalDate.now());
        application.setEffectiveDate(LocalDate.now());
        application.setUpdatedBy(userId);

        // Update recipient status
        if (application.getRecipientId() != null) {
            RecipientEntity recipient = recipientRepository.findById(application.getRecipientId()).orElse(null);
            if (recipient != null) {
                recipient.setPersonType(RecipientEntity.PersonType.RECIPIENT);
                recipientRepository.save(recipient);
            }
        }

        ApplicationEntity saved = applicationRepository.save(application);
        log.info("Application {} approved", applicationId);

        return saved;
    }

    /**
     * Deny application
     */
    @Transactional
    public ApplicationEntity denyApplication(String applicationId, DenialCode denialCode, String denialReason, String userId) {
        log.info("Denying application: {}", applicationId);

        ApplicationEntity application = getApplicationById(applicationId);

        application.setStatus(ApplicationStatus.DENIED);
        application.setDecisionDate(LocalDate.now());
        application.setDenialCode(denialCode);
        application.setDenialReason(denialReason);
        application.setUpdatedBy(userId);

        log.info("Application {} denied. Reason: {}", applicationId, denialCode);
        return applicationRepository.save(application);
    }

    /**
     * Withdraw application
     */
    @Transactional
    public ApplicationEntity withdrawApplication(String applicationId, String withdrawalReason, String userId) {
        log.info("Withdrawing application: {}", applicationId);

        ApplicationEntity application = getApplicationById(applicationId);

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setWithdrawalDate(LocalDate.now());
        application.setWithdrawalReason(withdrawalReason);
        application.setUpdatedBy(userId);

        log.info("Application {} withdrawn", applicationId);
        return applicationRepository.save(application);
    }

    // ==================== DEADLINE EXTENSION ====================

    /**
     * Extend 45-day deadline
     */
    @Transactional
    public ApplicationEntity extendDeadline(String applicationId, int additionalDays, String extensionReason, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        LocalDate currentDeadline = application.getExtendedDeadlineDate() != null ?
                application.getExtendedDeadlineDate() : application.getDeadlineDate();

        application.setExtensionDate(LocalDate.now());
        application.setExtensionReason(extensionReason);
        application.setExtendedDeadlineDate(currentDeadline.plusDays(additionalDays));
        application.setUpdatedBy(userId);

        log.info("Application {} deadline extended to {}", applicationId, application.getExtendedDeadlineDate());
        return applicationRepository.save(application);
    }

    // ==================== DOCUMENTATION ====================

    /**
     * Update SOC 873 received status
     */
    @Transactional
    public ApplicationEntity updateSoc873Status(String applicationId, boolean received, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setSoc873Received(received);
        if (received) {
            application.setSoc873Date(LocalDate.now());
        }
        application.setUpdatedBy(userId);

        checkDocumentationComplete(application);

        return applicationRepository.save(application);
    }

    /**
     * Update medical certification received status
     */
    @Transactional
    public ApplicationEntity updateMedicalCertification(String applicationId, boolean received, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setMedicalCertificationReceived(received);
        if (received) {
            application.setMedicalCertificationDate(LocalDate.now());
        }
        application.setUpdatedBy(userId);

        checkDocumentationComplete(application);

        return applicationRepository.save(application);
    }

    // ==================== LINK TO CASE ====================

    /**
     * Link application to created case
     */
    @Transactional
    public ApplicationEntity linkToCase(String applicationId, Long caseId, String caseNumber, String userId) {
        ApplicationEntity application = getApplicationById(applicationId);

        application.setCaseCreated(true);
        application.setCaseId(caseId);
        application.setCaseNumber(caseNumber);
        application.setCaseCreationDate(LocalDate.now());
        application.setUpdatedBy(userId);

        log.info("Application {} linked to case {}", applicationId, caseNumber);
        return applicationRepository.save(application);
    }

    // ==================== QUERY METHODS ====================

    public ApplicationEntity getApplicationById(String applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));
    }

    public ApplicationEntity getApplicationByNumber(String applicationNumber) {
        return applicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationNumber));
    }

    public List<ApplicationEntity> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<ApplicationEntity> getApplicationsByCounty(String countyCode) {
        return applicationRepository.findByCountyCode(countyCode);
    }

    public List<ApplicationEntity> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public List<ApplicationEntity> getPendingApplications() {
        return applicationRepository.findPendingApplications();
    }

    public List<ApplicationEntity> getPendingApplicationsByCounty(String countyCode) {
        return applicationRepository.findPendingApplicationsByCounty(countyCode);
    }

    public List<ApplicationEntity> getApplicationsByWorker(String workerId) {
        return applicationRepository.findByAssignedWorkerId(workerId);
    }

    public List<ApplicationEntity> getApproachingDeadline(int withinDays) {
        return applicationRepository.findApproachingDeadline(LocalDate.now().plusDays(withinDays));
    }

    public List<ApplicationEntity> getOverdueApplications() {
        return applicationRepository.findOverdueApplications();
    }

    public List<ApplicationEntity> getOverdueApplicationsByCounty(String countyCode) {
        return applicationRepository.findOverdueApplicationsByCounty(countyCode);
    }

    public List<ApplicationEntity> searchApplications(String applicationNumber,
                                                       ApplicationStatus status,
                                                       String countyCode,
                                                       ProgramType programType,
                                                       String assignedWorkerId,
                                                       String cin,
                                                       LocalDate startDate,
                                                       LocalDate endDate) {
        return applicationRepository.searchApplications(applicationNumber, status, countyCode, programType,
                assignedWorkerId, cin, startDate, endDate);
    }

    // ==================== STATISTICS ====================

    public Long countPendingByCounty(String countyCode) {
        return applicationRepository.countPendingByCounty(countyCode);
    }

    public Long countOverdueByCounty(String countyCode) {
        return applicationRepository.countOverdueByCounty(countyCode);
    }

    // ==================== HELPER METHODS ====================

    private void updateRecipientToApplicant(Long recipientId, String userId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found: " + recipientId));

        recipient.setPersonType(RecipientEntity.PersonType.APPLICANT);
        recipient.setUpdatedBy(userId);
        recipientRepository.save(recipient);
    }

    private RecipientEntity createRecipientFromReferral(ReferralEntity referral, String userId) {
        RecipientEntity recipient = new RecipientEntity();

        // Parse name from potential recipient name
        String name = referral.getPotentialRecipientName();
        if (name != null && name.contains(" ")) {
            String[] parts = name.split(" ", 2);
            recipient.setFirstName(parts[0].toUpperCase());
            recipient.setLastName(parts[1].toUpperCase());
        } else if (name != null) {
            recipient.setLastName(name.toUpperCase());
            recipient.setFirstName("UNKNOWN");
        }

        recipient.setDateOfBirth(referral.getPotentialRecipientDob());
        recipient.setSsn(referral.getPotentialRecipientSsn());
        recipient.setPrimaryPhone(referral.getPotentialRecipientPhone());
        recipient.setResidenceStreetName(referral.getStreetAddress());
        recipient.setResidenceCity(referral.getCity());
        recipient.setResidenceState(referral.getState());
        recipient.setResidenceZip(referral.getZipCode());
        recipient.setCountyCode(referral.getCountyCode());
        recipient.setCountyName(referral.getCountyName());
        recipient.setSpokenLanguage(referral.getPreferredLanguage());
        recipient.setPersonType(RecipientEntity.PersonType.APPLICANT);
        recipient.setCreatedBy(userId);

        return recipientRepository.save(recipient);
    }

    private String generateSimulatedCIN() {
        // Generate simulated CIN: 8 digits + 1 letter
        long digits = (long) (Math.random() * 100000000);
        char letter = (char) ('A' + (int) (Math.random() * 26));
        return String.format("%08d%c", digits, letter);
    }

    private void validateApprovalPrerequisites(ApplicationEntity application) {
        StringBuilder errors = new StringBuilder();

        if (!"ACTIVE".equals(application.getMediCalStatus())) {
            errors.append("Medi-Cal must be active. ");
        }
        if (application.getCin() == null || application.getCin().isEmpty()) {
            errors.append("CIN clearance required. ");
        }
        if (!Boolean.TRUE.equals(application.getAssessmentCompleted())) {
            errors.append("Assessment must be completed. ");
        }

        if (errors.length() > 0) {
            throw new IllegalStateException("Cannot approve application: " + errors);
        }
    }

    private void checkDocumentationComplete(ApplicationEntity application) {
        boolean complete = Boolean.TRUE.equals(application.getSoc873Received()) &&
                           Boolean.TRUE.equals(application.getMedicalCertificationReceived());
        application.setRequiredDocsComplete(complete);

        if (!complete) {
            StringBuilder missing = new StringBuilder();
            if (!Boolean.TRUE.equals(application.getSoc873Received())) {
                missing.append("SOC 873, ");
            }
            if (!Boolean.TRUE.equals(application.getMedicalCertificationReceived())) {
                missing.append("Medical Certification, ");
            }
            application.setMissingDocuments(missing.toString().replaceAll(", $", ""));
        } else {
            application.setMissingDocuments(null);
        }
    }
}
