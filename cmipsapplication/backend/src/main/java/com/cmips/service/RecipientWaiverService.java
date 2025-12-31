package com.cmips.service;

import com.cmips.entity.RecipientWaiverEntity;
import com.cmips.entity.RecipientWaiverEntity.*;
import com.cmips.entity.PersonNoteEntity;
import com.cmips.repository.RecipientWaiverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for Recipient Waiver (SOC 2298) Processing
 * Implements DSD Section 23 - CORI Tier 2 Waiver Management
 *
 * Tier 1 convictions = Permanent disqualification (no waiver possible)
 * Tier 2 convictions = Waiver possible via SOC 2298 form
 *
 * Workflow:
 * 1. CORI check returns Tier 2 conviction
 * 2. Provider disclosure to recipient
 * 3. Recipient decision (SOC 2298 waiver request)
 * 4. County review
 * 5. Supervisor approval (if needed)
 * 6. Final decision
 */
@Service
public class RecipientWaiverService {

    private static final Logger log = LoggerFactory.getLogger(RecipientWaiverService.class);

    // Waiver expiration period (typically tied to provider enrollment)
    private static final int DEFAULT_WAIVER_YEARS = 2;

    private final RecipientWaiverRepository waiverRepository;
    private final PersonNoteService personNoteService;

    public RecipientWaiverService(RecipientWaiverRepository waiverRepository,
                                   PersonNoteService personNoteService) {
        this.waiverRepository = waiverRepository;
        this.personNoteService = personNoteService;
    }

    // ==================== CREATE WAIVER ====================

    /**
     * Initiate waiver process after CORI check reveals Tier 2 conviction
     */
    @Transactional
    public RecipientWaiverEntity initiateWaiver(Long recipientId,
                                                  Long providerId,
                                                  String coriId,
                                                  String countyCode,
                                                  String convictionDetails,
                                                  LocalDate convictionDate,
                                                  String userId) {
        log.info("Initiating waiver for recipient: {}, provider: {}", recipientId, providerId);

        // Check if waiver already exists for this pair
        if (waiverRepository.existsWaiver(recipientId, providerId)) {
            throw new IllegalStateException("Waiver already exists for this recipient-provider pair");
        }

        RecipientWaiverEntity waiver = RecipientWaiverEntity.builder()
                .recipientId(recipientId)
                .providerId(providerId)
                .coriId(coriId)
                .countyCode(countyCode)
                .convictionTier(ConvictionTier.TIER2)
                .convictionDetails(convictionDetails)
                .convictionDate(convictionDate)
                .status(WaiverStatus.PENDING_DISCLOSURE)
                .createdBy(userId)
                .build();

        RecipientWaiverEntity saved = waiverRepository.save(waiver);

        // Create note
        personNoteService.createRecipientNote(
                recipientId,
                PersonNoteEntity.NoteCategory.CORI_WAIVER,
                "CORI Waiver Initiated",
                "Tier 2 conviction identified. Waiver process initiated for provider ID: " + providerId,
                PersonNoteEntity.ContactMethod.SYSTEM_GENERATED,
                userId
        );

        log.info("Waiver initiated with ID: {}", saved.getId());
        return saved;
    }

    // ==================== DISCLOSURE PROCESS ====================

    /**
     * Record that provider has disclosed conviction to recipient
     */
    @Transactional
    public RecipientWaiverEntity recordDisclosure(String waiverId,
                                                    LocalDate disclosureDate,
                                                    String disclosureMethod,
                                                    String witnessName,
                                                    String userId) {
        RecipientWaiverEntity waiver = getWaiverById(waiverId);

        if (waiver.getStatus() != WaiverStatus.PENDING_DISCLOSURE) {
            throw new IllegalStateException("Waiver is not pending disclosure");
        }

        waiver.setDisclosureDate(disclosureDate);
        waiver.setDisclosureMethod(disclosureMethod);
        waiver.setDisclosedByWorkerId(userId);
        waiver.setRecipientAcknowledgedDisclosure(true);
        waiver.setAcknowledgmentDate(disclosureDate);
        waiver.setStatus(WaiverStatus.DISCLOSED);
        waiver.setUpdatedBy(userId);

        log.info("Disclosure recorded for waiver: {}", waiverId);
        return waiverRepository.save(waiver);
    }

    // ==================== RECIPIENT DECISION ====================

    /**
     * Record recipient's decision on waiver (SOC 2298)
     */
    @Transactional
    public RecipientWaiverEntity recordRecipientDecision(String waiverId,
                                                          boolean waiverRequested,
                                                          String recipientJustification,
                                                          String userId) {
        RecipientWaiverEntity waiver = getWaiverById(waiverId);

        if (waiver.getStatus() != WaiverStatus.DISCLOSED) {
            throw new IllegalStateException("Disclosure must be completed before recipient decision");
        }

        waiver.setRecipientDecision(waiverRequested);
        waiver.setRecipientDecisionDate(LocalDate.now());
        waiver.setRecipientJustification(recipientJustification);

        if (waiverRequested) {
            waiver.setStatus(WaiverStatus.WAIVER_REQUESTED);
            waiver.setWaiverRequestDate(LocalDate.now());

            personNoteService.createRecipientNote(
                    waiver.getRecipientId(),
                    PersonNoteEntity.NoteCategory.CORI_WAIVER,
                    "SOC 2298 Waiver Requested",
                    "Recipient has requested waiver for provider. Justification: " + recipientJustification,
                    PersonNoteEntity.ContactMethod.IN_PERSON,
                    userId
            );
        } else {
            // Recipient declined to sign waiver - provider cannot be hired
            waiver.setStatus(WaiverStatus.WAIVER_DECLINED);

            personNoteService.createRecipientNote(
                    waiver.getRecipientId(),
                    PersonNoteEntity.NoteCategory.CORI_WAIVER,
                    "SOC 2298 Waiver Declined",
                    "Recipient has declined to sign waiver for provider.",
                    PersonNoteEntity.ContactMethod.IN_PERSON,
                    userId
            );
        }

        waiver.setUpdatedBy(userId);
        log.info("Recipient decision recorded for waiver: {} - Requested: {}", waiverId, waiverRequested);
        return waiverRepository.save(waiver);
    }

    // ==================== SOC 2298 FORM ====================

    /**
     * Record SOC 2298 form signing
     */
    @Transactional
    public RecipientWaiverEntity recordSOC2298Signing(String waiverId,
                                                       LocalDate signedDate,
                                                       String witnessName,
                                                       String userId) {
        RecipientWaiverEntity waiver = getWaiverById(waiverId);

        if (waiver.getStatus() != WaiverStatus.WAIVER_REQUESTED) {
            throw new IllegalStateException("Waiver must be requested before SOC 2298 signing");
        }

        waiver.setSoc2298Signed(true);
        waiver.setSoc2298SignedDate(signedDate);
        waiver.setSoc2298WitnessName(witnessName);
        waiver.setStatus(WaiverStatus.SOC_2298_SIGNED);
        waiver.setUpdatedBy(userId);

        log.info("SOC 2298 form signed for waiver: {}", waiverId);
        return waiverRepository.save(waiver);
    }

    // ==================== COUNTY REVIEW ====================

    /**
     * Submit waiver for county review
     */
    @Transactional
    public RecipientWaiverEntity submitForCountyReview(String waiverId, String userId) {
        RecipientWaiverEntity waiver = getWaiverById(waiverId);

        if (waiver.getStatus() != WaiverStatus.SOC_2298_SIGNED) {
            throw new IllegalStateException("SOC 2298 must be signed before county review");
        }

        waiver.setStatus(WaiverStatus.COUNTY_REVIEW);
        waiver.setUpdatedBy(userId);

        log.info("Waiver submitted for county review: {}", waiverId);
        return waiverRepository.save(waiver);
    }

    /**
     * Assign county reviewer
     */
    @Transactional
    public RecipientWaiverEntity assignCountyReviewer(String waiverId,
                                                        String reviewerId,
                                                        String reviewerName,
                                                        String userId) {
        RecipientWaiverEntity waiver = getWaiverById(waiverId);

        waiver.setCountyReviewerId(reviewerId);
        waiver.setCountyReviewerName(reviewerName);
        waiver.setUpdatedBy(userId);

        log.info("County reviewer assigned to waiver: {} - Reviewer: {}", waiverId, reviewerName);
        return waiverRepository.save(waiver);
    }

    /**
     * Record county review decision
     */
    @Transactional
    public RecipientWaiverEntity recordCountyDecision(String waiverId,
                                                        CountyDecision decision,
                                                        String decisionReason,
                                                        boolean requiresSupervisorReview,
                                                        String userId) {
        RecipientWaiverEntity waiver = getWaiverById(waiverId);

        if (waiver.getStatus() != WaiverStatus.COUNTY_REVIEW) {
            throw new IllegalStateException("Waiver must be in county review status");
        }

        waiver.setCountyDecision(decision);
        waiver.setCountyReviewDate(LocalDate.now());
        waiver.setCountyDecisionReason(decisionReason);
        waiver.setUpdatedBy(userId);

        if (requiresSupervisorReview || decision == CountyDecision.CONDITIONAL) {
            waiver.setStatus(WaiverStatus.SUPERVISOR_REVIEW);
            waiver.setSupervisorApprovalRequired(true);
        } else {
            finalizeWaiver(waiver, decision, userId);
        }

        log.info("County decision recorded for waiver: {} - Decision: {}", waiverId, decision);
        return waiverRepository.save(waiver);
    }

    // ==================== SUPERVISOR REVIEW ====================

    /**
     * Assign supervisor for review
     */
    @Transactional
    public RecipientWaiverEntity assignSupervisor(String waiverId,
                                                    String supervisorId,
                                                    String supervisorName,
                                                    String userId) {
        RecipientWaiverEntity waiver = getWaiverById(waiverId);

        waiver.setSupervisorId(supervisorId);
        waiver.setSupervisorName(supervisorName);
        waiver.setUpdatedBy(userId);

        log.info("Supervisor assigned to waiver: {} - Supervisor: {}", waiverId, supervisorName);
        return waiverRepository.save(waiver);
    }

    /**
     * Record supervisor decision
     */
    @Transactional
    public RecipientWaiverEntity recordSupervisorDecision(String waiverId,
                                                            boolean approved,
                                                            String notes,
                                                            String userId) {
        RecipientWaiverEntity waiver = getWaiverById(waiverId);

        if (waiver.getStatus() != WaiverStatus.SUPERVISOR_REVIEW) {
            throw new IllegalStateException("Waiver must be in supervisor review status");
        }

        waiver.setSupervisorDecision(approved);
        waiver.setSupervisorDecisionDate(LocalDate.now());
        // Store supervisor notes in the main notes field
        waiver.setNotes((waiver.getNotes() != null ? waiver.getNotes() + "\n" : "") +
                "Supervisor notes: " + notes);
        waiver.setUpdatedBy(userId);

        CountyDecision finalDecision = approved ? CountyDecision.APPROVED : CountyDecision.DENIED;
        finalizeWaiver(waiver, finalDecision, userId);

        log.info("Supervisor decision recorded for waiver: {} - Approved: {}", waiverId, approved);
        return waiverRepository.save(waiver);
    }

    // ==================== FINALIZE WAIVER ====================

    /**
     * Finalize waiver based on decision
     */
    private void finalizeWaiver(RecipientWaiverEntity waiver, CountyDecision decision, String userId) {
        switch (decision) {
            case APPROVED:
                waiver.setStatus(WaiverStatus.APPROVED);
                waiver.setEffectiveDate(LocalDate.now());
                waiver.setExpirationDate(LocalDate.now().plusYears(DEFAULT_WAIVER_YEARS));

                personNoteService.createRecipientNote(
                        waiver.getRecipientId(),
                        PersonNoteEntity.NoteCategory.CORI_WAIVER,
                        "Waiver Approved",
                        "SOC 2298 waiver approved for provider ID: " + waiver.getProviderId() +
                                ". Effective: " + waiver.getEffectiveDate() +
                                ", Expires: " + waiver.getExpirationDate(),
                        PersonNoteEntity.ContactMethod.SYSTEM_GENERATED,
                        userId
                );
                break;

            case DENIED:
                waiver.setStatus(WaiverStatus.DENIED);

                personNoteService.createRecipientNote(
                        waiver.getRecipientId(),
                        PersonNoteEntity.NoteCategory.CORI_WAIVER,
                        "Waiver Denied",
                        "SOC 2298 waiver denied for provider ID: " + waiver.getProviderId() +
                                ". Reason: " + waiver.getCountyDecisionReason(),
                        PersonNoteEntity.ContactMethod.SYSTEM_GENERATED,
                        userId
                );
                break;

            case CONDITIONAL:
                waiver.setStatus(WaiverStatus.APPROVED);
                waiver.setEffectiveDate(LocalDate.now());
                // Shorter expiration for conditional approvals
                waiver.setExpirationDate(LocalDate.now().plusYears(1));

                personNoteService.createRecipientNote(
                        waiver.getRecipientId(),
                        PersonNoteEntity.NoteCategory.CORI_WAIVER,
                        "Waiver Conditionally Approved",
                        "SOC 2298 waiver conditionally approved for provider ID: " + waiver.getProviderId(),
                        PersonNoteEntity.ContactMethod.SYSTEM_GENERATED,
                        userId
                );
                break;

            case PENDING:
            case PENDING_MORE_INFO:
                // No action needed
                break;
        }
    }

    // ==================== REVOKE WAIVER ====================

    /**
     * Revoke an approved waiver
     */
    @Transactional
    public RecipientWaiverEntity revokeWaiver(String waiverId, String reason, String userId) {
        RecipientWaiverEntity waiver = getWaiverById(waiverId);

        if (waiver.getStatus() != WaiverStatus.APPROVED) {
            throw new IllegalStateException("Only approved waivers can be revoked");
        }

        waiver.setRevoked(true);
        waiver.setRevocationDate(LocalDate.now());
        waiver.setRevocationReason(reason);
        waiver.setRevokedBy(userId);
        waiver.setStatus(WaiverStatus.REVOKED);
        waiver.setUpdatedBy(userId);

        personNoteService.createRecipientNote(
                waiver.getRecipientId(),
                PersonNoteEntity.NoteCategory.CORI_WAIVER,
                "Waiver Revoked",
                "SOC 2298 waiver revoked for provider ID: " + waiver.getProviderId() +
                        ". Reason: " + reason,
                PersonNoteEntity.ContactMethod.SYSTEM_GENERATED,
                userId
        );

        log.info("Waiver revoked: {} - Reason: {}", waiverId, reason);
        return waiverRepository.save(waiver);
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get waiver by ID
     */
    public RecipientWaiverEntity getWaiverById(String waiverId) {
        return waiverRepository.findById(waiverId)
                .orElseThrow(() -> new RuntimeException("Waiver not found: " + waiverId));
    }

    /**
     * Get active waiver for recipient-provider pair
     */
    public Optional<RecipientWaiverEntity> getActiveWaiver(Long recipientId, Long providerId) {
        return waiverRepository.findActiveWaiver(recipientId, providerId);
    }

    /**
     * Check if provider has active waiver
     */
    public boolean hasActiveWaiver(Long providerId) {
        return waiverRepository.hasActiveWaiver(providerId);
    }

    /**
     * Get waivers by recipient
     */
    public List<RecipientWaiverEntity> getWaiversByRecipient(Long recipientId) {
        return waiverRepository.findByRecipientId(recipientId);
    }

    /**
     * Get waivers by provider
     */
    public List<RecipientWaiverEntity> getWaiversByProvider(Long providerId) {
        return waiverRepository.findByProviderId(providerId);
    }

    /**
     * Get pending waivers
     */
    public List<RecipientWaiverEntity> getPendingWaivers() {
        return waiverRepository.findPendingWaivers();
    }

    /**
     * Get pending waivers by county
     */
    public List<RecipientWaiverEntity> getPendingWaiversByCounty(String countyCode) {
        return waiverRepository.findPendingWaiversByCounty(countyCode);
    }

    /**
     * Get waivers pending county review
     */
    public List<RecipientWaiverEntity> getWaiversPendingCountyReview() {
        return waiverRepository.findPendingCountyReview();
    }

    /**
     * Get waivers pending county review by county
     */
    public List<RecipientWaiverEntity> getWaiversPendingCountyReviewByCounty(String countyCode) {
        return waiverRepository.findPendingCountyReviewByCounty(countyCode);
    }

    /**
     * Get waivers pending supervisor review
     */
    public List<RecipientWaiverEntity> getWaiversPendingSupervisorReview() {
        return waiverRepository.findPendingSupervisorReview();
    }

    /**
     * Get expiring waivers
     */
    public List<RecipientWaiverEntity> getExpiringWaivers(int daysUntilExpiration) {
        LocalDate expirationDate = LocalDate.now().plusDays(daysUntilExpiration);
        return waiverRepository.findExpiringWaivers(expirationDate);
    }

    /**
     * Search waivers
     */
    public List<RecipientWaiverEntity> searchWaivers(WaiverStatus status,
                                                       String countyCode,
                                                       Long recipientId,
                                                       Long providerId,
                                                       CountyDecision countyDecision) {
        return waiverRepository.searchWaivers(status, countyCode, recipientId, providerId, countyDecision);
    }

    /**
     * Count pending waivers by county
     */
    public Long countPendingByCounty(String countyCode) {
        return waiverRepository.countPendingByCounty(countyCode);
    }
}
