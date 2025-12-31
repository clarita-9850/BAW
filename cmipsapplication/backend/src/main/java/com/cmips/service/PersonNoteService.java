package com.cmips.service;

import com.cmips.entity.PersonNoteEntity;
import com.cmips.entity.PersonNoteEntity.*;
import com.cmips.repository.PersonNoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Person Notes / Contact History
 * Implements chronological tracking of all contacts and events per DSD Section 20
 */
@Service
public class PersonNoteService {

    private static final Logger log = LoggerFactory.getLogger(PersonNoteService.class);

    // Edit window in hours (per DSD - 24 hours)
    private static final int EDIT_WINDOW_HOURS = 24;

    private final PersonNoteRepository personNoteRepository;

    public PersonNoteService(PersonNoteRepository personNoteRepository) {
        this.personNoteRepository = personNoteRepository;
    }

    // ==================== CREATE NOTES ====================

    /**
     * Create a note for a recipient
     */
    @Transactional
    public PersonNoteEntity createRecipientNote(Long recipientId,
                                                 NoteCategory category,
                                                 String subject,
                                                 String content,
                                                 ContactMethod contactMethod,
                                                 String userId) {
        PersonNoteEntity note = PersonNoteEntity.builder()
                .personId(recipientId)
                .personType(PersonNoteType.RECIPIENT)
                .category(category)
                .subject(subject)
                .content(content)
                .contactMethod(contactMethod)
                .noteDate(LocalDate.now())
                .createdBy(userId)
                .build();

        PersonNoteEntity saved = personNoteRepository.save(note);
        log.info("Created recipient note: {} for recipient: {}", saved.getId(), recipientId);
        return saved;
    }

    /**
     * Create a note for a provider
     */
    @Transactional
    public PersonNoteEntity createProviderNote(Long providerId,
                                                NoteCategory category,
                                                String subject,
                                                String content,
                                                ContactMethod contactMethod,
                                                String userId) {
        PersonNoteEntity note = PersonNoteEntity.builder()
                .personId(providerId)
                .personType(PersonNoteType.PROVIDER)
                .category(category)
                .subject(subject)
                .content(content)
                .contactMethod(contactMethod)
                .noteDate(LocalDate.now())
                .createdBy(userId)
                .build();

        PersonNoteEntity saved = personNoteRepository.save(note);
        log.info("Created provider note: {} for provider: {}", saved.getId(), providerId);
        return saved;
    }

    /**
     * Create a note for a referral
     */
    @Transactional
    public PersonNoteEntity createReferralNote(String referralId,
                                                NoteCategory category,
                                                String subject,
                                                String content,
                                                ContactMethod contactMethod,
                                                String userId) {
        PersonNoteEntity note = PersonNoteEntity.builder()
                .referralId(referralId)
                .personType(PersonNoteType.REFERRAL)
                .category(category)
                .subject(subject)
                .content(content)
                .contactMethod(contactMethod)
                .noteDate(LocalDate.now())
                .createdBy(userId)
                .build();

        PersonNoteEntity saved = personNoteRepository.save(note);
        log.info("Created referral note: {} for referral: {}", saved.getId(), referralId);
        return saved;
    }

    /**
     * Create a note for a case
     */
    @Transactional
    public PersonNoteEntity createCaseNote(Long caseId,
                                            NoteCategory category,
                                            String subject,
                                            String content,
                                            ContactMethod contactMethod,
                                            String userId) {
        PersonNoteEntity note = PersonNoteEntity.builder()
                .caseId(caseId)
                .personType(PersonNoteType.RECIPIENT) // Case notes are associated with recipient
                .category(category)
                .subject(subject)
                .content(content)
                .contactMethod(contactMethod)
                .noteDate(LocalDate.now())
                .createdBy(userId)
                .build();

        PersonNoteEntity saved = personNoteRepository.save(note);
        log.info("Created case note: {} for case: {}", saved.getId(), caseId);
        return saved;
    }

    // ==================== CREATE SPECIAL NOTES ====================

    /**
     * Create a confidential note (restricted access)
     */
    @Transactional
    public PersonNoteEntity createConfidentialNote(Long personId,
                                                    PersonNoteType personType,
                                                    String subject,
                                                    String content,
                                                    String userId) {
        PersonNoteEntity note = PersonNoteEntity.builder()
                .personId(personId)
                .personType(personType)
                .category(NoteCategory.CASE_ACTION)
                .subject(subject)
                .content(content)
                .noteDate(LocalDate.now())
                .importance(NoteImportance.HIGH)
                .createdBy(userId)
                .build();

        note.setConfidential(true);

        PersonNoteEntity saved = personNoteRepository.save(note);
        log.info("Created confidential note: {}", saved.getId());
        return saved;
    }

    /**
     * Create a supervisor-only note
     */
    @Transactional
    public PersonNoteEntity createSupervisorNote(Long personId,
                                                  PersonNoteType personType,
                                                  String subject,
                                                  String content,
                                                  String userId) {
        PersonNoteEntity note = PersonNoteEntity.builder()
                .personId(personId)
                .personType(personType)
                .category(NoteCategory.CASE_ACTION)
                .subject(subject)
                .content(content)
                .noteDate(LocalDate.now())
                .importance(NoteImportance.HIGH)
                .createdBy(userId)
                .build();

        note.setSupervisorOnly(true);

        PersonNoteEntity saved = personNoteRepository.save(note);
        log.info("Created supervisor note: {}", saved.getId());
        return saved;
    }

    /**
     * Create a note with follow-up required
     */
    @Transactional
    public PersonNoteEntity createNoteWithFollowUp(Long personId,
                                                    PersonNoteType personType,
                                                    NoteCategory category,
                                                    String subject,
                                                    String content,
                                                    LocalDate followUpDate,
                                                    String followUpNotes,
                                                    String userId) {
        PersonNoteEntity note = PersonNoteEntity.builder()
                .personId(personId)
                .personType(personType)
                .category(category)
                .subject(subject)
                .content(content)
                .noteDate(LocalDate.now())
                .followUpNeeded(true)
                .followUpDate(followUpDate)
                .createdBy(userId)
                .build();

        note.setFollowUpNotes(followUpNotes);

        PersonNoteEntity saved = personNoteRepository.save(note);
        log.info("Created note with follow-up: {} due {}", saved.getId(), followUpDate);
        return saved;
    }

    // ==================== UPDATE NOTES ====================

    /**
     * Update note content (only within 24-hour window)
     */
    @Transactional
    public PersonNoteEntity updateNote(String noteId, String newContent, String userId) {
        PersonNoteEntity note = getNoteById(noteId);

        // Check if within edit window
        if (!note.isEditable()) {
            throw new IllegalStateException("Note cannot be edited - 24-hour edit window has passed");
        }

        // Check if user is the creator
        if (!note.getCreatedBy().equals(userId)) {
            throw new SecurityException("Only the note creator can edit the note");
        }

        note.setContent(newContent);
        note.setUpdatedBy(userId);

        log.info("Note {} updated by {}", noteId, userId);
        return personNoteRepository.save(note);
    }

    /**
     * Mark follow-up as completed
     */
    @Transactional
    public PersonNoteEntity completeFollowUp(String noteId, String completionNotes, String userId) {
        PersonNoteEntity note = getNoteById(noteId);

        if (!Boolean.TRUE.equals(note.getFollowUpNeeded())) {
            throw new IllegalStateException("Note does not have a follow-up requirement");
        }

        note.setFollowUpCompleted(true);
        note.setFollowUpCompletedDate(LocalDate.now());

        // Add completion notes to the follow-up notes
        String existingNotes = note.getFollowUpNotes() != null ? note.getFollowUpNotes() + "\n" : "";
        note.setFollowUpNotes(existingNotes + "Completed: " + completionNotes);
        note.setUpdatedBy(userId);

        log.info("Follow-up completed for note: {}", noteId);
        return personNoteRepository.save(note);
    }

    /**
     * Inactivate a note (soft delete)
     */
    @Transactional
    public PersonNoteEntity inactivateNote(String noteId, String reason, String userId) {
        PersonNoteEntity note = getNoteById(noteId);

        note.setActive(false);
        note.setInactivatedDate(LocalDateTime.now());
        note.setInactivatedBy(userId);
        note.setInactivationReason(reason);

        log.info("Note {} inactivated by {}", noteId, userId);
        return personNoteRepository.save(note);
    }

    // ==================== QUERY METHODS ====================

    /**
     * Get note by ID
     */
    public PersonNoteEntity getNoteById(String noteId) {
        return personNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found: " + noteId));
    }

    /**
     * Get all notes for a person
     */
    public List<PersonNoteEntity> getPersonNotes(Long personId) {
        return personNoteRepository.findByPersonIdOrderByNoteDateDescNoteTimeDesc(personId);
    }

    /**
     * Get notes for a person by type
     */
    public List<PersonNoteEntity> getPersonNotesByType(Long personId, PersonNoteType personType) {
        return personNoteRepository.findByPersonIdAndPersonTypeOrderByNoteDateDescNoteTimeDesc(personId, personType);
    }

    /**
     * Get active (non-inactivated) notes for a person
     */
    public List<PersonNoteEntity> getActivePersonNotes(Long personId) {
        return personNoteRepository.findActiveByPersonId(personId);
    }

    /**
     * Get all notes for a referral
     */
    public List<PersonNoteEntity> getReferralNotes(String referralId) {
        return personNoteRepository.findByReferralIdOrderByNoteDateDescNoteTimeDesc(referralId);
    }

    /**
     * Get all notes for a case
     */
    public List<PersonNoteEntity> getCaseNotes(Long caseId) {
        return personNoteRepository.findByCaseIdOrderByNoteDateDescNoteTimeDesc(caseId);
    }

    /**
     * Get notes by category
     */
    public List<PersonNoteEntity> getNotesByCategory(Long personId, NoteCategory category) {
        return personNoteRepository.findByPersonIdAndCategory(personId, category);
    }

    /**
     * Get notes by contact method
     */
    public List<PersonNoteEntity> getNotesByContactMethod(Long personId, ContactMethod contactMethod) {
        return personNoteRepository.findByPersonIdAndContactMethod(personId, contactMethod);
    }

    /**
     * Get notes needing follow-up
     */
    public List<PersonNoteEntity> getNotesNeedingFollowUp() {
        return personNoteRepository.findNotesNeedingFollowUp();
    }

    /**
     * Get notes needing follow-up for a person
     */
    public List<PersonNoteEntity> getNotesNeedingFollowUpByPerson(Long personId) {
        return personNoteRepository.findNotesNeedingFollowUpByPerson(personId);
    }

    /**
     * Get notes needing follow-up by date
     */
    public List<PersonNoteEntity> getNotesNeedingFollowUpByDate(LocalDate date) {
        return personNoteRepository.findNotesNeedingFollowUpByDate(date);
    }

    /**
     * Get notes by date range
     */
    public List<PersonNoteEntity> getNotesByDateRange(Long personId, LocalDate startDate, LocalDate endDate) {
        return personNoteRepository.findByPersonIdAndDateRange(personId, startDate, endDate);
    }

    /**
     * Get notes created by a worker
     */
    public List<PersonNoteEntity> getNotesByCreator(String createdBy) {
        return personNoteRepository.findByCreatedBy(createdBy);
    }

    /**
     * Get important notes (CRITICAL or HIGH importance)
     */
    public List<PersonNoteEntity> getImportantNotes(Long personId) {
        return personNoteRepository.findImportantNotes(personId);
    }

    /**
     * Get confidential notes (requires elevated access)
     */
    public List<PersonNoteEntity> getConfidentialNotes(Long personId) {
        return personNoteRepository.findConfidentialNotes(personId);
    }

    /**
     * Get supervisor-only notes (requires supervisor access)
     */
    public List<PersonNoteEntity> getSupervisorOnlyNotes(Long personId) {
        return personNoteRepository.findSupervisorOnlyNotes(personId);
    }

    /**
     * Search notes by content
     */
    public List<PersonNoteEntity> searchNotes(Long personId, String searchTerm) {
        return personNoteRepository.searchNotes(personId, searchTerm);
    }

    /**
     * Get recent notes (within specified days)
     */
    public List<PersonNoteEntity> getRecentNotes(Long personId, int days) {
        LocalDate sinceDate = LocalDate.now().minusDays(days);
        return personNoteRepository.findRecentNotes(personId, sinceDate);
    }

    /**
     * Count notes for a person
     */
    public Long countNotes(Long personId) {
        return personNoteRepository.countByPersonId(personId);
    }

    /**
     * Count active notes for a person
     */
    public Long countActiveNotes(Long personId) {
        return personNoteRepository.countActiveByPersonId(personId);
    }
}
