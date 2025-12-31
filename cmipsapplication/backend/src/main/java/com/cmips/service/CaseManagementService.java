package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.entity.CaseEntity.CaseStatus;
import com.cmips.entity.RecipientEntity.PersonType;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Case Management Service
 * Implements business rules from DSD Section 20 and 21
 */
@Service
public class CaseManagementService {

    private static final Logger log = LoggerFactory.getLogger(CaseManagementService.class);

    private final CaseRepository caseRepository;
    private final RecipientRepository recipientRepository;
    private final ServiceEligibilityRepository serviceEligibilityRepository;
    private final ProviderAssignmentRepository providerAssignmentRepository;
    private final CaseNoteRepository caseNoteRepository;
    private final CaseContactRepository caseContactRepository;
    private final HealthCareCertificationRepository healthCareCertificationRepository;
    private final TaskService taskService;
    private final NotificationService notificationService;

    public CaseManagementService(
            CaseRepository caseRepository,
            RecipientRepository recipientRepository,
            ServiceEligibilityRepository serviceEligibilityRepository,
            ProviderAssignmentRepository providerAssignmentRepository,
            CaseNoteRepository caseNoteRepository,
            CaseContactRepository caseContactRepository,
            HealthCareCertificationRepository healthCareCertificationRepository,
            TaskService taskService,
            NotificationService notificationService) {
        this.caseRepository = caseRepository;
        this.recipientRepository = recipientRepository;
        this.serviceEligibilityRepository = serviceEligibilityRepository;
        this.providerAssignmentRepository = providerAssignmentRepository;
        this.caseNoteRepository = caseNoteRepository;
        this.caseContactRepository = caseContactRepository;
        this.healthCareCertificationRepository = healthCareCertificationRepository;
        this.taskService = taskService;
        this.notificationService = notificationService;
    }

    // ==================== CASE CREATION ====================

    /**
     * Create a new case from referral
     * Per BR OS 09, 13, 16 - handles SAWS/MEDS integration
     */
    @Transactional
    public CaseEntity createCase(Long recipientId, String caseOwnerId, String countyCode, String userId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // Update person type from Open-Referral to Applicant (per BR OS 19)
        if (recipient.getPersonType() == PersonType.OPEN_REFERRAL) {
            recipient.setPersonType(PersonType.APPLICANT);
            recipientRepository.save(recipient);
        }

        // Generate unique case number
        String caseNumber = generateCaseNumber(countyCode);

        CaseEntity caseEntity = CaseEntity.builder()
                .caseNumber(caseNumber)
                .recipientId(recipientId)
                .caseStatus(CaseStatus.PENDING)
                .caseType(CaseEntity.CaseType.IHSS)
                .countyCode(countyCode)
                .caseOwnerId(caseOwnerId)
                .cin(recipient.getCin())
                .applicationDate(LocalDate.now())
                .referralDate(recipient.getReferralDate())
                .createdBy(userId)
                .build();

        caseEntity = caseRepository.save(caseEntity);

        // Generate task notification for case assignment
        createCaseAssignmentTask(caseEntity, caseOwnerId);

        log.info("Created case {} for recipient {}", caseNumber, recipientId);
        return caseEntity;
    }

    /**
     * Create a referral (per BR OS 17, 28)
     */
    @Transactional
    public RecipientEntity createReferral(RecipientEntity recipient, String userId) {
        // Set person type to Open-Referral
        recipient.setPersonType(PersonType.OPEN_REFERRAL);
        recipient.setReferralDate(LocalDate.now());
        recipient.setCreatedBy(userId);

        // Names are converted to uppercase in @PrePersist
        return recipientRepository.save(recipient);
    }

    /**
     * Re-open closed referral (per BR OS 42)
     */
    @Transactional
    public RecipientEntity reopenReferral(Long recipientId, String referralSource, String countyCode, String userId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        if (recipient.getPersonType() != PersonType.CLOSED_REFERRAL) {
            throw new RuntimeException("Can only reopen closed referrals");
        }

        recipient.setPersonType(PersonType.OPEN_REFERRAL);
        recipient.setCountyCode(countyCode);
        recipient.setReferralDate(LocalDate.now());
        recipient.setReferralSource(referralSource);
        recipient.setReferralClosedDate(null);
        recipient.setReferralClosedReason(null);
        recipient.setUpdatedBy(userId);

        return recipientRepository.save(recipient);
    }

    /**
     * Close a referral
     */
    @Transactional
    public RecipientEntity closeReferral(Long recipientId, String reason, String userId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        recipient.setPersonType(PersonType.CLOSED_REFERRAL);
        recipient.setReferralClosedDate(LocalDate.now());
        recipient.setReferralClosedReason(reason);
        recipient.setUpdatedBy(userId);

        return recipientRepository.save(recipient);
    }

    // ==================== CASE STATUS MANAGEMENT ====================

    /**
     * Approve case - change status to ELIGIBLE
     * Person type changes to RECIPIENT
     */
    @Transactional
    public CaseEntity approveCase(Long caseId, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        caseEntity.setCaseStatus(CaseStatus.ELIGIBLE);
        caseEntity.setEligibilityDate(LocalDate.now());
        caseEntity.setUpdatedBy(userId);

        // Update recipient to RECIPIENT type
        RecipientEntity recipient = recipientRepository.findById(caseEntity.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        recipient.setPersonType(PersonType.RECIPIENT);
        recipientRepository.save(recipient);

        caseEntity = caseRepository.save(caseEntity);

        // Send MEDS IH34 notification would happen here
        log.info("Case {} approved, recipient {} is now ELIGIBLE", caseEntity.getCaseNumber(), recipient.getId());

        return caseEntity;
    }

    /**
     * Deny case (per BR OS 12)
     */
    @Transactional
    public CaseEntity denyCase(Long caseId, String denialReason, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        caseEntity.setCaseStatus(CaseStatus.DENIED);
        caseEntity.setDenialDate(LocalDate.now());
        caseEntity.setTerminationReason(denialReason);
        caseEntity.setUpdatedBy(userId);

        caseEntity = caseRepository.save(caseEntity);

        // Per BR OS 12 - Send MEDS IH34 – Update Application Data
        log.info("Case {} denied: {}", caseEntity.getCaseNumber(), denialReason);

        return caseEntity;
    }

    /**
     * Terminate case
     */
    @Transactional
    public CaseEntity terminateCase(Long caseId, String terminationReason, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        caseEntity.setCaseStatus(CaseStatus.TERMINATED);
        caseEntity.setTerminationDate(LocalDate.now());
        caseEntity.setTerminationReason(terminationReason);
        caseEntity.setUpdatedBy(userId);

        // Terminate all active provider assignments
        List<ProviderAssignmentEntity> assignments = providerAssignmentRepository
                .findByCaseIdAndStatus(caseId, ProviderAssignmentEntity.AssignmentStatus.ACTIVE);
        for (ProviderAssignmentEntity assignment : assignments) {
            assignment.setStatus(ProviderAssignmentEntity.AssignmentStatus.TERMINATED);
            assignment.setLeaveTerminationEffectiveDate(LocalDate.now());
            assignment.setTerminationReason("Case Terminated");
            providerAssignmentRepository.save(assignment);
        }

        caseEntity = caseRepository.save(caseEntity);
        log.info("Case {} terminated: {}", caseEntity.getCaseNumber(), terminationReason);

        return caseEntity;
    }

    /**
     * Place case on leave
     */
    @Transactional
    public CaseEntity placeCaseOnLeave(Long caseId, String reason, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        caseEntity.setCaseStatus(CaseStatus.ON_LEAVE);
        caseEntity.setUpdatedBy(userId);

        return caseRepository.save(caseEntity);
    }

    /**
     * Withdraw application
     */
    @Transactional
    public CaseEntity withdrawApplication(Long caseId, String reason, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (caseEntity.getCaseStatus() != CaseStatus.PENDING) {
            throw new RuntimeException("Can only withdraw pending applications");
        }

        caseEntity.setCaseStatus(CaseStatus.APPLICATION_WITHDRAWN);
        caseEntity.setTerminationReason(reason);
        caseEntity.setUpdatedBy(userId);

        return caseRepository.save(caseEntity);
    }

    // ==================== CASE SEARCH ====================

    /**
     * Search cases by multiple criteria
     */
    public List<CaseEntity> searchCases(String caseNumber, String cin, String countyCode,
                                        String caseOwnerId, CaseStatus status) {
        return caseRepository.searchCases(caseNumber, cin, countyCode, caseOwnerId, status);
    }

    /**
     * Get cases for a caseworker
     */
    public List<CaseEntity> getCasesForCaseworker(String caseOwnerId) {
        return caseRepository.findByCaseOwnerId(caseOwnerId);
    }

    /**
     * Get active cases by county
     */
    public List<CaseEntity> getActiveCasesByCounty(String countyCode) {
        return caseRepository.findActiveCasesByCounty(countyCode);
    }

    /**
     * Get cases due for reassessment
     */
    public List<CaseEntity> getCasesDueForReassessment(LocalDate date) {
        return caseRepository.findCasesDueForReassessment(date);
    }

    // ==================== CASE ASSIGNMENT ====================

    /**
     * Assign case to a caseworker
     */
    @Transactional
    public CaseEntity assignCaseToCaseworker(Long caseId, String newCaseOwnerId, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        String oldCaseOwnerId = caseEntity.getCaseOwnerId();
        caseEntity.setCaseOwnerId(newCaseOwnerId);
        caseEntity.setUpdatedBy(userId);

        caseEntity = caseRepository.save(caseEntity);

        // Create task for new case owner
        createCaseAssignmentTask(caseEntity, newCaseOwnerId);

        // Add case note about reassignment
        addCaseNote(caseId, "CASE_NOTE", "Case Reassignment",
                "Case reassigned from " + oldCaseOwnerId + " to " + newCaseOwnerId, userId);

        return caseEntity;
    }

    // ==================== CASE NOTES ====================

    /**
     * Add a note to a case
     */
    @Transactional
    public CaseNoteEntity addCaseNote(Long caseId, String noteType, String subject, String content, String userId) {
        CaseNoteEntity note = CaseNoteEntity.builder()
                .caseId(caseId)
                .noteType(noteType)
                .subject(subject)
                .content(content)
                .status("ACTIVE")
                .createdBy(userId)
                .build();

        return caseNoteRepository.save(note);
    }

    /**
     * Append to existing note (per User Story 5 - notes edited by appending)
     */
    @Transactional
    public CaseNoteEntity appendToNote(Long noteId, String additionalContent, String userId) {
        CaseNoteEntity note = caseNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.appendNote(additionalContent, userId);
        return caseNoteRepository.save(note);
    }

    /**
     * Cancel a note
     */
    @Transactional
    public CaseNoteEntity cancelNote(Long noteId, String reason, String userId) {
        CaseNoteEntity note = caseNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.cancel(userId, reason);
        return caseNoteRepository.save(note);
    }

    /**
     * Get notes for a case (chronological order, most recent first)
     */
    public List<CaseNoteEntity> getCaseNotes(Long caseId) {
        return caseNoteRepository.findByCaseIdOrderByCreatedAtDesc(caseId);
    }

    // ==================== CASE CONTACTS ====================

    /**
     * Add a contact to a case (per BR SE 44)
     */
    @Transactional
    public CaseContactEntity addCaseContact(CaseContactEntity contact, String userId) {
        contact.setCreatedBy(userId);
        // Start date and status set in @PrePersist per BR SE 44
        return caseContactRepository.save(contact);
    }

    /**
     * Inactivate a contact (per BR SE 45)
     */
    @Transactional
    public CaseContactEntity inactivateContact(Long contactId, String userId) {
        CaseContactEntity contact = caseContactRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        contact.inactivate();
        contact.setUpdatedBy(userId);
        return caseContactRepository.save(contact);
    }

    /**
     * Get active contacts for a case
     */
    public List<CaseContactEntity> getActiveContacts(Long caseId) {
        return caseContactRepository.findActiveContactsByCaseId(caseId);
    }

    // ==================== INTER-COUNTY TRANSFER ====================

    /**
     * Initiate inter-county transfer
     */
    @Transactional
    public CaseEntity initiateInterCountyTransfer(Long caseId, String receivingCountyCode, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        caseEntity.setTransferStatus("INITIATED");
        caseEntity.setSendingCountyCode(caseEntity.getCountyCode());
        caseEntity.setReceivingCountyCode(receivingCountyCode);
        caseEntity.setTransferDate(LocalDate.now());
        caseEntity.setUpdatedBy(userId);

        return caseRepository.save(caseEntity);
    }

    /**
     * Complete inter-county transfer
     */
    @Transactional
    public CaseEntity completeInterCountyTransfer(Long caseId, String newCaseOwnerId, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        caseEntity.setTransferStatus("COMPLETED");
        caseEntity.setCountyCode(caseEntity.getReceivingCountyCode());
        caseEntity.setCaseOwnerId(newCaseOwnerId);
        caseEntity.setUpdatedBy(userId);

        // Update recipient county
        RecipientEntity recipient = recipientRepository.findById(caseEntity.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        recipient.setCountyCode(caseEntity.getReceivingCountyCode());
        recipientRepository.save(recipient);

        return caseRepository.save(caseEntity);
    }

    // ==================== COMPANION CASES ====================

    /**
     * Find companion cases by matching address (per BR SE 26, 27)
     */
    public List<RecipientEntity> findCompanionCases(Long recipientId) {
        RecipientEntity recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        if (recipient.getResidenceStreetNumber() == null ||
            recipient.getResidenceStreetName() == null ||
            recipient.getResidenceCity() == null) {
            return List.of();
        }

        return recipientRepository.findCompanionCasesByAddress(
                recipient.getResidenceStreetNumber(),
                recipient.getResidenceStreetName(),
                recipient.getResidenceCity(),
                recipientId);
    }

    // ==================== HELPER METHODS ====================

    private String generateCaseNumber(String countyCode) {
        // Format: CC-YYYYMMDD-XXXXX where CC is county code, XXXXX is sequence
        String dateStr = LocalDate.now().toString().replace("-", "").substring(0, 8);
        String random = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return countyCode + "-" + dateStr + "-" + random;
    }

    private void createCaseAssignmentTask(CaseEntity caseEntity, String assigneeId) {
        Task task = Task.builder()
                .title("New Case Assignment: " + caseEntity.getCaseNumber())
                .description("You have been assigned case " + caseEntity.getCaseNumber())
                .assignedTo(assigneeId)
                .workQueue("CASE_MANAGEMENT")
                .status(Task.TaskStatus.PENDING)
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(5).atStartOfDay())
                .build();

        taskService.createTask(task);
    }

    // ==================== STATISTICS ====================

    /**
     * Get case statistics for a county
     */
    public CaseStatistics getCaseStatistics(String countyCode) {
        return CaseStatistics.builder()
                .pendingCount(caseRepository.countByCountyCodeAndStatus(countyCode, CaseStatus.PENDING))
                .eligibleCount(caseRepository.countByCountyCodeAndStatus(countyCode, CaseStatus.ELIGIBLE))
                .onLeaveCount(caseRepository.countByCountyCodeAndStatus(countyCode, CaseStatus.ON_LEAVE))
                .deniedCount(caseRepository.countByCountyCodeAndStatus(countyCode, CaseStatus.DENIED))
                .terminatedCount(caseRepository.countByCountyCodeAndStatus(countyCode, CaseStatus.TERMINATED))
                .build();
    }

    public static class CaseStatistics {
        private Long pendingCount;
        private Long eligibleCount;
        private Long onLeaveCount;
        private Long deniedCount;
        private Long terminatedCount;

        public CaseStatistics() {}

        public CaseStatistics(Long pendingCount, Long eligibleCount, Long onLeaveCount, Long deniedCount, Long terminatedCount) {
            this.pendingCount = pendingCount;
            this.eligibleCount = eligibleCount;
            this.onLeaveCount = onLeaveCount;
            this.deniedCount = deniedCount;
            this.terminatedCount = terminatedCount;
        }

        public Long getPendingCount() { return pendingCount; }
        public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }

        public Long getEligibleCount() { return eligibleCount; }
        public void setEligibleCount(Long eligibleCount) { this.eligibleCount = eligibleCount; }

        public Long getOnLeaveCount() { return onLeaveCount; }
        public void setOnLeaveCount(Long onLeaveCount) { this.onLeaveCount = onLeaveCount; }

        public Long getDeniedCount() { return deniedCount; }
        public void setDeniedCount(Long deniedCount) { this.deniedCount = deniedCount; }

        public Long getTerminatedCount() { return terminatedCount; }
        public void setTerminatedCount(Long terminatedCount) { this.terminatedCount = terminatedCount; }

        public static CaseStatisticsBuilder builder() { return new CaseStatisticsBuilder(); }

        public static class CaseStatisticsBuilder {
            private Long pendingCount;
            private Long eligibleCount;
            private Long onLeaveCount;
            private Long deniedCount;
            private Long terminatedCount;

            public CaseStatisticsBuilder pendingCount(Long pendingCount) { this.pendingCount = pendingCount; return this; }
            public CaseStatisticsBuilder eligibleCount(Long eligibleCount) { this.eligibleCount = eligibleCount; return this; }
            public CaseStatisticsBuilder onLeaveCount(Long onLeaveCount) { this.onLeaveCount = onLeaveCount; return this; }
            public CaseStatisticsBuilder deniedCount(Long deniedCount) { this.deniedCount = deniedCount; return this; }
            public CaseStatisticsBuilder terminatedCount(Long terminatedCount) { this.terminatedCount = terminatedCount; return this; }

            public CaseStatistics build() {
                return new CaseStatistics(pendingCount, eligibleCount, onLeaveCount, deniedCount, terminatedCount);
            }
        }
    }
}
