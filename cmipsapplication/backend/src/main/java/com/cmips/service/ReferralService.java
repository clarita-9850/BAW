package com.cmips.service;

import com.cmips.entity.ReferralEntity;
import com.cmips.entity.ReferralEntity.*;
import com.cmips.entity.RecipientEntity;
import com.cmips.entity.PersonNoteEntity;
import com.cmips.repository.ReferralRepository;
import com.cmips.repository.RecipientRepository;
import com.cmips.repository.PersonNoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Referral Management
 * Implements DSD Section 20 - Initial Contact & Referral Processing
 */
@Service
public class ReferralService {

    private static final Logger log = LoggerFactory.getLogger(ReferralService.class);

    private final ReferralRepository referralRepository;
    private final RecipientRepository recipientRepository;
    private final PersonNoteRepository personNoteRepository;

    public ReferralService(ReferralRepository referralRepository,
                          RecipientRepository recipientRepository,
                          PersonNoteRepository personNoteRepository) {
        this.referralRepository = referralRepository;
        this.recipientRepository = recipientRepository;
        this.personNoteRepository = personNoteRepository;
    }

    // ==================== CREATE REFERRAL ====================

    /**
     * Create a new referral
     */
    @Transactional
    public ReferralEntity createReferral(ReferralEntity referral, String userId) {
        log.info("Creating new referral for potential recipient: {}", referral.getPotentialRecipientName());

        // Set defaults
        referral.setStatus(ReferralStatus.OPEN);
        referral.setReferralDate(LocalDate.now());
        referral.setCreatedBy(userId);

        // Save referral
        ReferralEntity saved = referralRepository.save(referral);

        // Create initial contact note
        createInitialContactNote(saved, userId);

        log.info("Referral created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Create a referral from external agency
     */
    @Transactional
    public ReferralEntity createExternalReferral(ReferralEntity referral,
                                                  String agencyName,
                                                  String agencyContact,
                                                  String agencyPhone,
                                                  String externalRefNumber,
                                                  String userId) {
        referral.setSource(ReferralSource.OTHER_AGENCY);
        referral.setReferringAgencyName(agencyName);
        referral.setReferringAgencyContact(agencyContact);
        referral.setReferringAgencyPhone(agencyPhone);
        referral.setExternalReferenceNumber(externalRefNumber);

        return createReferral(referral, userId);
    }

    // ==================== UPDATE REFERRAL ====================

    /**
     * Update referral status
     */
    @Transactional
    public ReferralEntity updateStatus(String referralId, ReferralStatus newStatus, String userId) {
        ReferralEntity referral = getReferralById(referralId);

        ReferralStatus oldStatus = referral.getStatus();
        referral.setStatus(newStatus);
        referral.setUpdatedBy(userId);

        ReferralEntity saved = referralRepository.save(referral);

        // Log status change
        log.info("Referral {} status changed from {} to {}", referralId, oldStatus, newStatus);

        return saved;
    }

    /**
     * Assign referral to worker
     */
    @Transactional
    public ReferralEntity assignToWorker(String referralId, String workerId, String workerName, String userId) {
        ReferralEntity referral = getReferralById(referralId);

        referral.setAssignedWorkerId(workerId);
        referral.setAssignedWorkerName(workerName);
        referral.setUpdatedBy(userId);

        log.info("Referral {} assigned to worker {}", referralId, workerName);
        return referralRepository.save(referral);
    }

    /**
     * Set follow-up date
     */
    @Transactional
    public ReferralEntity setFollowUpDate(String referralId, LocalDate followUpDate, String userId) {
        ReferralEntity referral = getReferralById(referralId);

        referral.setFollowUpDate(followUpDate);
        referral.setStatus(ReferralStatus.PENDING);
        referral.setUpdatedBy(userId);

        log.info("Referral {} follow-up date set to {}", referralId, followUpDate);
        return referralRepository.save(referral);
    }

    /**
     * Update referral priority
     */
    @Transactional
    public ReferralEntity updatePriority(String referralId, ReferralPriority priority, String userId) {
        ReferralEntity referral = getReferralById(referralId);

        referral.setPriority(priority);
        referral.setUpdatedBy(userId);

        log.info("Referral {} priority changed to {}", referralId, priority);
        return referralRepository.save(referral);
    }

    // ==================== CLOSE REFERRAL ====================

    /**
     * Close referral without conversion
     */
    @Transactional
    public ReferralEntity closeReferral(String referralId, ReferralClosedReason reason, String reasonDetails, String userId) {
        ReferralEntity referral = getReferralById(referralId);

        // Validate referral can be closed
        if (referral.getStatus() == ReferralStatus.CLOSED || referral.getStatus() == ReferralStatus.CONVERTED) {
            throw new IllegalStateException("Referral is already closed or converted");
        }

        referral.setStatus(ReferralStatus.CLOSED);
        referral.setClosedDate(LocalDate.now());
        referral.setClosedReason(reason);
        referral.setClosedReasonDetails(reasonDetails);
        referral.setUpdatedBy(userId);

        log.info("Referral {} closed with reason: {}", referralId, reason);
        return referralRepository.save(referral);
    }

    /**
     * Reopen closed referral (within 30 days per DSD)
     */
    @Transactional
    public ReferralEntity reopenReferral(String referralId, String reopenReason, String userId) {
        ReferralEntity referral = getReferralById(referralId);

        // Validate referral can be reopened
        if (referral.getStatus() != ReferralStatus.CLOSED) {
            throw new IllegalStateException("Only closed referrals can be reopened");
        }

        // Check 30-day limit
        if (referral.getClosedDate() != null) {
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            if (referral.getClosedDate().isBefore(thirtyDaysAgo)) {
                throw new IllegalStateException("Referral cannot be reopened - closed more than 30 days ago");
            }
        }

        referral.setStatus(ReferralStatus.OPEN);
        referral.setClosedDate(null);
        referral.setClosedReason(null);
        referral.setClosedReasonDetails(null);
        referral.setUpdatedBy(userId);

        // Add note about reopening
        createReferralNote(referral, "Referral reopened. Reason: " + reopenReason, userId);

        log.info("Referral {} reopened", referralId);
        return referralRepository.save(referral);
    }

    // ==================== CONVERT TO APPLICATION ====================

    /**
     * Convert referral to application
     * This creates a recipient record and links to an application
     */
    @Transactional
    public ReferralEntity convertToApplication(String referralId, String applicationId, Long recipientId, String userId) {
        ReferralEntity referral = getReferralById(referralId);

        // Validate referral can be converted
        if (referral.getStatus() == ReferralStatus.CONVERTED || referral.getStatus() == ReferralStatus.CLOSED) {
            throw new IllegalStateException("Referral cannot be converted - already " + referral.getStatus());
        }

        referral.setStatus(ReferralStatus.CONVERTED);
        referral.setConvertedToApplication(true);
        referral.setApplicationId(applicationId);
        referral.setConversionDate(LocalDate.now());
        referral.setRecipientId(recipientId);
        referral.setClosedDate(LocalDate.now());
        referral.setClosedReason(ReferralClosedReason.CONVERTED_TO_APP);
        referral.setUpdatedBy(userId);

        log.info("Referral {} converted to application {}", referralId, applicationId);
        return referralRepository.save(referral);
    }

    // ==================== LINK TO RECIPIENT ====================

    /**
     * Link referral to existing recipient (if found during search)
     */
    @Transactional
    public ReferralEntity linkToRecipient(String referralId, Long recipientId, String userId) {
        ReferralEntity referral = getReferralById(referralId);

        referral.setRecipientId(recipientId);
        referral.setUpdatedBy(userId);

        log.info("Referral {} linked to recipient {}", referralId, recipientId);
        return referralRepository.save(referral);
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get referral by ID
     */
    public ReferralEntity getReferralById(String referralId) {
        return referralRepository.findById(referralId)
                .orElseThrow(() -> new RuntimeException("Referral not found: " + referralId));
    }

    /**
     * Get all referrals
     */
    public List<ReferralEntity> getAllReferrals() {
        return referralRepository.findAll();
    }

    /**
     * Get referrals by county
     */
    public List<ReferralEntity> getReferralsByCounty(String countyCode) {
        return referralRepository.findByCountyCode(countyCode);
    }

    /**
     * Get referrals by status
     */
    public List<ReferralEntity> getReferralsByStatus(ReferralStatus status) {
        return referralRepository.findByStatus(status);
    }

    /**
     * Get referrals by county and status
     */
    public List<ReferralEntity> getReferralsByCountyAndStatus(String countyCode, ReferralStatus status) {
        return referralRepository.findByCountyCodeAndStatus(countyCode, status);
    }

    /**
     * Get open referrals
     */
    public List<ReferralEntity> getOpenReferrals() {
        return referralRepository.findOpenReferrals();
    }

    /**
     * Get open referrals by county
     */
    public List<ReferralEntity> getOpenReferralsByCounty(String countyCode) {
        return referralRepository.findOpenReferralsByCounty(countyCode);
    }

    /**
     * Get referrals assigned to worker
     */
    public List<ReferralEntity> getReferralsByWorker(String workerId) {
        return referralRepository.findByAssignedWorkerId(workerId);
    }

    /**
     * Get referrals needing follow-up
     */
    public List<ReferralEntity> getReferralsNeedingFollowUp() {
        return referralRepository.findNeedingFollowUp(LocalDate.now());
    }

    /**
     * Get referrals needing follow-up by county
     */
    public List<ReferralEntity> getReferralsNeedingFollowUpByCounty(String countyCode) {
        return referralRepository.findNeedingFollowUpByCounty(countyCode, LocalDate.now());
    }

    /**
     * Get urgent referrals
     */
    public List<ReferralEntity> getUrgentReferrals() {
        return referralRepository.findUrgentReferrals();
    }

    /**
     * Get urgent referrals by county
     */
    public List<ReferralEntity> getUrgentReferralsByCounty(String countyCode) {
        return referralRepository.findUrgentReferralsByCounty(countyCode);
    }

    /**
     * Search referrals
     */
    public List<ReferralEntity> searchReferrals(ReferralStatus status,
                                                 String countyCode,
                                                 ReferralSource source,
                                                 ReferralPriority priority,
                                                 String assignedWorkerId,
                                                 LocalDate startDate,
                                                 LocalDate endDate) {
        return referralRepository.searchReferrals(status, countyCode, source, priority, assignedWorkerId, startDate, endDate);
    }

    // ==================== STATISTICS ====================

    /**
     * Count open referrals by county
     */
    public Long countOpenReferralsByCounty(String countyCode) {
        return referralRepository.countOpenByCounty(countyCode);
    }

    /**
     * Count referrals by county and status
     */
    public Long countByCountyAndStatus(String countyCode, ReferralStatus status) {
        return referralRepository.countByCountyAndStatus(countyCode, status);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create initial contact note for new referral
     */
    private void createInitialContactNote(ReferralEntity referral, String userId) {
        PersonNoteEntity note = PersonNoteEntity.builder()
                .referralId(referral.getId())
                .personType(PersonNoteEntity.PersonNoteType.REFERRAL)
                .noteDate(LocalDate.now())
                .contactMethod(PersonNoteEntity.ContactMethod.PHONE_CALL)
                .category(PersonNoteEntity.NoteCategory.INITIAL_CONTACT)
                .subject("Initial Contact - Referral Created")
                .content("New referral created. Source: " + referral.getSource() +
                        ". Potential recipient: " + referral.getPotentialRecipientName() +
                        ". Contact: " + referral.getContactFirstName() + " " + referral.getContactLastName())
                .createdBy(userId)
                .build();

        personNoteRepository.save(note);
    }

    /**
     * Create note for referral
     */
    private void createReferralNote(ReferralEntity referral, String content, String userId) {
        PersonNoteEntity note = PersonNoteEntity.builder()
                .referralId(referral.getId())
                .personType(PersonNoteEntity.PersonNoteType.REFERRAL)
                .noteDate(LocalDate.now())
                .category(PersonNoteEntity.NoteCategory.STATUS_UPDATE)
                .subject("Status Update")
                .content(content)
                .createdBy(userId)
                .build();

        personNoteRepository.save(note);
    }
}
