package com.cmips.controller;

import com.cmips.entity.PersonNoteEntity;
import com.cmips.entity.PersonNoteEntity.*;
import com.cmips.service.PersonNoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Person Notes / Contact History
 * Implements DSD requirement for chronological tracking of all contacts
 */
@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
public class PersonNoteController {

    private static final Logger log = LoggerFactory.getLogger(PersonNoteController.class);

    private final PersonNoteService noteService;

    public PersonNoteController(PersonNoteService noteService) {
        this.noteService = noteService;
    }

    // ==================== CREATE NOTES ====================

    /**
     * Create a note for a recipient
     */
    @PostMapping("/recipient/{recipientId}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> createRecipientNote(@PathVariable Long recipientId,
                                                  @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            NoteCategory category = NoteCategory.valueOf(request.get("category"));
            String subject = request.get("subject");
            String content = request.get("content");
            ContactMethod contactMethod = ContactMethod.valueOf(request.get("contactMethod"));

            PersonNoteEntity note = noteService.createRecipientNote(
                    recipientId, category, subject, content, contactMethod, userId);
            log.info("Created recipient note: {} by {}", note.getId(), userId);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("Error creating recipient note", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create a note for a provider
     */
    @PostMapping("/provider/{providerId}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> createProviderNote(@PathVariable Long providerId,
                                                 @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            NoteCategory category = NoteCategory.valueOf(request.get("category"));
            String subject = request.get("subject");
            String content = request.get("content");
            ContactMethod contactMethod = ContactMethod.valueOf(request.get("contactMethod"));

            PersonNoteEntity note = noteService.createProviderNote(
                    providerId, category, subject, content, contactMethod, userId);
            log.info("Created provider note: {} by {}", note.getId(), userId);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("Error creating provider note", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create a note for a referral
     */
    @PostMapping("/referral/{referralId}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> createReferralNote(@PathVariable String referralId,
                                                 @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            NoteCategory category = NoteCategory.valueOf(request.get("category"));
            String subject = request.get("subject");
            String content = request.get("content");
            ContactMethod contactMethod = ContactMethod.valueOf(request.get("contactMethod"));

            PersonNoteEntity note = noteService.createReferralNote(
                    referralId, category, subject, content, contactMethod, userId);
            log.info("Created referral note: {} by {}", note.getId(), userId);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("Error creating referral note", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create a note for a case
     */
    @PostMapping("/case/{caseId}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> createCaseNote(@PathVariable Long caseId,
                                             @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            NoteCategory category = NoteCategory.valueOf(request.get("category"));
            String subject = request.get("subject");
            String content = request.get("content");
            ContactMethod contactMethod = ContactMethod.valueOf(request.get("contactMethod"));

            PersonNoteEntity note = noteService.createCaseNote(
                    caseId, category, subject, content, contactMethod, userId);
            log.info("Created case note: {} by {}", note.getId(), userId);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("Error creating case note", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create a note with follow-up required
     */
    @PostMapping("/with-followup")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> createNoteWithFollowUp(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Long personId = request.get("personId") != null ?
                    ((Number) request.get("personId")).longValue() : null;
            PersonNoteType personType = PersonNoteType.valueOf((String) request.get("personType"));
            NoteCategory category = NoteCategory.valueOf((String) request.get("category"));
            String subject = (String) request.get("subject");
            String content = (String) request.get("content");
            LocalDate followUpDate = LocalDate.parse((String) request.get("followUpDate"));
            String followUpNotes = (String) request.get("followUpNotes");

            PersonNoteEntity note = noteService.createNoteWithFollowUp(
                    personId, personType, category, subject, content, followUpDate, followUpNotes, userId);
            log.info("Created note with follow-up: {} by {}", note.getId(), userId);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("Error creating note with follow-up", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create a confidential note
     */
    @PostMapping("/confidential")
    @PreAuthorize("hasAnyRole('supervisor', 'admin')")
    public ResponseEntity<?> createConfidentialNote(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Long personId = request.get("personId") != null ?
                    ((Number) request.get("personId")).longValue() : null;
            PersonNoteType personType = PersonNoteType.valueOf((String) request.get("personType"));
            String subject = (String) request.get("subject");
            String content = (String) request.get("content");

            PersonNoteEntity note = noteService.createConfidentialNote(
                    personId, personType, subject, content, userId);
            log.info("Created confidential note: {} by {}", note.getId(), userId);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("Error creating confidential note", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create a supervisor-only note
     */
    @PostMapping("/supervisor-only")
    @PreAuthorize("hasAnyRole('supervisor', 'admin')")
    public ResponseEntity<?> createSupervisorNote(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Long personId = request.get("personId") != null ?
                    ((Number) request.get("personId")).longValue() : null;
            PersonNoteType personType = PersonNoteType.valueOf((String) request.get("personType"));
            String subject = (String) request.get("subject");
            String content = (String) request.get("content");

            PersonNoteEntity note = noteService.createSupervisorNote(
                    personId, personType, subject, content, userId);
            log.info("Created supervisor note: {} by {}", note.getId(), userId);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("Error creating supervisor note", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== UPDATE NOTES ====================

    /**
     * Update note content (within 24-hour window)
     */
    @PutMapping("/{noteId}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> updateNote(@PathVariable String noteId,
                                         @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String newContent = request.get("content");

            PersonNoteEntity updated = noteService.updateNote(noteId, newContent, userId);
            log.info("Note {} updated by {}", noteId, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating note", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Complete follow-up
     */
    @PostMapping("/{noteId}/complete-followup")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> completeFollowUp(@PathVariable String noteId,
                                               @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String completionNotes = request.get("completionNotes");

            PersonNoteEntity updated = noteService.completeFollowUp(noteId, completionNotes, userId);
            log.info("Follow-up completed for note: {} by {}", noteId, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error completing follow-up", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Inactivate note
     */
    @PostMapping("/{noteId}/inactivate")
    @PreAuthorize("hasAnyRole('supervisor', 'admin')")
    public ResponseEntity<?> inactivateNote(@PathVariable String noteId,
                                             @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String reason = request.get("reason");

            PersonNoteEntity updated = noteService.inactivateNote(noteId, reason, userId);
            log.info("Note {} inactivated by {}", noteId, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error inactivating note", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== QUERY ====================

    /**
     * Get note by ID
     */
    @GetMapping("/{noteId}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getNoteById(@PathVariable String noteId) {
        try {
            PersonNoteEntity note = noteService.getNoteById(noteId);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("Error getting note", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get notes for a person
     */
    @GetMapping("/person/{personId}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getPersonNotes(@PathVariable Long personId,
                                             @RequestParam(required = false) Boolean activeOnly) {
        try {
            List<PersonNoteEntity> notes;
            if (Boolean.TRUE.equals(activeOnly)) {
                notes = noteService.getActivePersonNotes(personId);
            } else {
                notes = noteService.getPersonNotes(personId);
            }
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting person notes", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get notes for a referral
     */
    @GetMapping("/referral/{referralId}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getReferralNotes(@PathVariable String referralId) {
        try {
            List<PersonNoteEntity> notes = noteService.getReferralNotes(referralId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting referral notes", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get notes for a case
     */
    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getCaseNotes(@PathVariable Long caseId) {
        try {
            List<PersonNoteEntity> notes = noteService.getCaseNotes(caseId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting case notes", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get notes needing follow-up
     */
    @GetMapping("/needs-followup")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getNotesNeedingFollowUp(@RequestParam(required = false) Long personId) {
        try {
            List<PersonNoteEntity> notes;
            if (personId != null) {
                notes = noteService.getNotesNeedingFollowUpByPerson(personId);
            } else {
                notes = noteService.getNotesNeedingFollowUp();
            }
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting notes needing follow-up", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get notes by category
     */
    @GetMapping("/person/{personId}/category/{category}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getNotesByCategory(@PathVariable Long personId,
                                                 @PathVariable String category) {
        try {
            NoteCategory cat = NoteCategory.valueOf(category);
            List<PersonNoteEntity> notes = noteService.getNotesByCategory(personId, cat);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting notes by category", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get recent notes
     */
    @GetMapping("/person/{personId}/recent")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getRecentNotes(@PathVariable Long personId,
                                             @RequestParam(defaultValue = "30") int days) {
        try {
            List<PersonNoteEntity> notes = noteService.getRecentNotes(personId, days);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting recent notes", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search notes
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> searchNotes(@RequestParam Long personId,
                                          @RequestParam String searchTerm) {
        try {
            List<PersonNoteEntity> notes = noteService.searchNotes(personId, searchTerm);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error searching notes", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get important notes
     */
    @GetMapping("/person/{personId}/important")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getImportantNotes(@PathVariable Long personId) {
        try {
            List<PersonNoteEntity> notes = noteService.getImportantNotes(personId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting important notes", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get confidential notes (supervisor access required)
     */
    @GetMapping("/person/{personId}/confidential")
    @PreAuthorize("hasAnyRole('supervisor', 'admin')")
    public ResponseEntity<?> getConfidentialNotes(@PathVariable Long personId) {
        try {
            List<PersonNoteEntity> notes = noteService.getConfidentialNotes(personId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting confidential notes", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get supervisor-only notes (supervisor access required)
     */
    @GetMapping("/person/{personId}/supervisor-only")
    @PreAuthorize("hasAnyRole('supervisor', 'admin')")
    public ResponseEntity<?> getSupervisorOnlyNotes(@PathVariable Long personId) {
        try {
            List<PersonNoteEntity> notes = noteService.getSupervisorOnlyNotes(personId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("Error getting supervisor-only notes", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Count notes for a person
     */
    @GetMapping("/person/{personId}/count")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> countNotes(@PathVariable Long personId,
                                         @RequestParam(required = false) Boolean activeOnly) {
        try {
            Long count;
            if (Boolean.TRUE.equals(activeOnly)) {
                count = noteService.countActiveNotes(personId);
            } else {
                count = noteService.countNotes(personId);
            }
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("Error counting notes", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getName();
        }
        return "anonymous";
    }
}
