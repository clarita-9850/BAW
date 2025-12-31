package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Person Note Entity - Chronological record of contacts and events for a person
 * Based on DSD Section 20 - Person Notes documentation
 *
 * Person Notes track all interactions, conversations, and significant events
 * related to recipients, applicants, referrals, or providers.
 */
@Entity
@Table(name = "person_notes")
public class PersonNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Person reference (can be recipient, provider, or referral)
    @Column(name = "person_id")
    private Long personId;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type")
    private PersonNoteType personType;

    // For referrals (before person record exists)
    @Column(name = "referral_id")
    private String referralId;

    // For cases
    @Column(name = "case_id")
    private Long caseId;

    // Note Details
    @Column(name = "note_date", nullable = false)
    private LocalDate noteDate;

    @Column(name = "note_time")
    private LocalDateTime noteTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_method")
    private ContactMethod contactMethod;

    @Column(name = "contact_direction", length = 20)
    private String contactDirection; // INBOUND, OUTBOUND

    // Who was contacted
    @Column(name = "contacted_person", length = 200)
    private String contactedPerson; // Name of person contacted

    @Column(name = "contacted_relationship", length = 100)
    private String contactedRelationship; // Relationship to subject

    // Note Content
    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // Purpose/Category
    @Enumerated(EnumType.STRING)
    @Column(name = "note_category")
    private NoteCategory category;

    // Follow-up
    @Column(name = "follow_up_needed")
    private Boolean followUpNeeded = false;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "follow_up_notes", length = 500)
    private String followUpNotes;

    @Column(name = "follow_up_completed")
    private Boolean followUpCompleted = false;

    @Column(name = "follow_up_completed_date")
    private LocalDate followUpCompletedDate;

    // Resolution/Outcome
    @Column(name = "outcome", length = 500)
    private String outcome;

    // Priority/Importance
    @Enumerated(EnumType.STRING)
    @Column(name = "importance")
    private NoteImportance importance = NoteImportance.NORMAL;

    // Visibility
    @Column(name = "confidential")
    private Boolean confidential = false;

    @Column(name = "supervisor_only")
    private Boolean supervisorOnly = false;

    // Status
    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "inactivated_date")
    private LocalDateTime inactivatedDate;

    @Column(name = "inactivated_by", length = 100)
    private String inactivatedBy;

    @Column(name = "inactivation_reason", length = 500)
    private String inactivationReason;

    // Audit Fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_by_name", length = 200)
    private String createdByName;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Edit tracking (notes can only be edited within certain timeframe)
    @Column(name = "edit_count")
    private Integer editCount = 0;

    @Column(name = "last_edited_at")
    private LocalDateTime lastEditedAt;

    @Column(name = "editable_until")
    private LocalDateTime editableUntil; // 24 hours from creation

    // Enums
    public enum PersonNoteType {
        RECIPIENT,
        APPLICANT,
        REFERRAL,
        PROVIDER
    }

    public enum ContactMethod {
        PHONE_CALL,
        IN_PERSON,
        HOME_VISIT,
        OFFICE_VISIT,
        EMAIL,
        LETTER,
        FAX,
        TEXT_MESSAGE,
        VIDEO_CALL,
        VOICEMAIL,
        THIRD_PARTY,
        SYSTEM_GENERATED,
        SYSTEM,  // Alias for SYSTEM_GENERATED
        OTHER
    }

    public enum NoteCategory {
        INITIAL_CONTACT,
        FOLLOW_UP,
        APPLICATION_RELATED,
        ELIGIBILITY,
        ASSESSMENT,
        AUTHORIZATION,
        PROVIDER_RELATED,
        TIMESHEET,
        PAYMENT,
        COMPLAINT,
        INQUIRY,
        STATUS_UPDATE,
        CASE_ACTION,
        DOCUMENTATION,
        EMERGENCY,
        HOSPITALIZATION,
        ADDRESS_CHANGE,
        DEATH_NOTIFICATION,
        CORI_WAIVER,  // For CORI waiver-related notes (SOC 2298)
        OTHER
    }

    public enum NoteImportance {
        CRITICAL,
        HIGH,
        NORMAL,
        LOW
    }

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (noteDate == null) {
            noteDate = LocalDate.now();
        }
        if (noteTime == null) {
            noteTime = LocalDateTime.now();
        }
        // Notes are editable for 24 hours
        editableUntil = createdAt.plusHours(24);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastEditedAt = LocalDateTime.now();
        editCount = (editCount == null ? 0 : editCount) + 1;
    }

    // Constructors
    public PersonNoteEntity() {
    }

    // Builder pattern
    public static PersonNoteEntityBuilder builder() {
        return new PersonNoteEntityBuilder();
    }

    // Helper method to check if note is still editable
    public boolean isEditable() {
        if (editableUntil == null) return false;
        return LocalDateTime.now().isBefore(editableUntil);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public PersonNoteType getPersonType() {
        return personType;
    }

    public void setPersonType(PersonNoteType personType) {
        this.personType = personType;
    }

    public String getReferralId() {
        return referralId;
    }

    public void setReferralId(String referralId) {
        this.referralId = referralId;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public LocalDate getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(LocalDate noteDate) {
        this.noteDate = noteDate;
    }

    public LocalDateTime getNoteTime() {
        return noteTime;
    }

    public void setNoteTime(LocalDateTime noteTime) {
        this.noteTime = noteTime;
    }

    public ContactMethod getContactMethod() {
        return contactMethod;
    }

    public void setContactMethod(ContactMethod contactMethod) {
        this.contactMethod = contactMethod;
    }

    public String getContactDirection() {
        return contactDirection;
    }

    public void setContactDirection(String contactDirection) {
        this.contactDirection = contactDirection;
    }

    public String getContactedPerson() {
        return contactedPerson;
    }

    public void setContactedPerson(String contactedPerson) {
        this.contactedPerson = contactedPerson;
    }

    public String getContactedRelationship() {
        return contactedRelationship;
    }

    public void setContactedRelationship(String contactedRelationship) {
        this.contactedRelationship = contactedRelationship;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NoteCategory getCategory() {
        return category;
    }

    public void setCategory(NoteCategory category) {
        this.category = category;
    }

    public Boolean getFollowUpNeeded() {
        return followUpNeeded;
    }

    public void setFollowUpNeeded(Boolean followUpNeeded) {
        this.followUpNeeded = followUpNeeded;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDate followUpDate) {
        this.followUpDate = followUpDate;
    }

    public String getFollowUpNotes() {
        return followUpNotes;
    }

    public void setFollowUpNotes(String followUpNotes) {
        this.followUpNotes = followUpNotes;
    }

    public Boolean getFollowUpCompleted() {
        return followUpCompleted;
    }

    public void setFollowUpCompleted(Boolean followUpCompleted) {
        this.followUpCompleted = followUpCompleted;
    }

    public LocalDate getFollowUpCompletedDate() {
        return followUpCompletedDate;
    }

    public void setFollowUpCompletedDate(LocalDate followUpCompletedDate) {
        this.followUpCompletedDate = followUpCompletedDate;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public NoteImportance getImportance() {
        return importance;
    }

    public void setImportance(NoteImportance importance) {
        this.importance = importance;
    }

    public Boolean getConfidential() {
        return confidential;
    }

    public void setConfidential(Boolean confidential) {
        this.confidential = confidential;
    }

    public Boolean getSupervisorOnly() {
        return supervisorOnly;
    }

    public void setSupervisorOnly(Boolean supervisorOnly) {
        this.supervisorOnly = supervisorOnly;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getInactivatedDate() {
        return inactivatedDate;
    }

    public void setInactivatedDate(LocalDateTime inactivatedDate) {
        this.inactivatedDate = inactivatedDate;
    }

    public String getInactivatedBy() {
        return inactivatedBy;
    }

    public void setInactivatedBy(String inactivatedBy) {
        this.inactivatedBy = inactivatedBy;
    }

    public String getInactivationReason() {
        return inactivationReason;
    }

    public void setInactivationReason(String inactivationReason) {
        this.inactivationReason = inactivationReason;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getEditCount() {
        return editCount;
    }

    public void setEditCount(Integer editCount) {
        this.editCount = editCount;
    }

    public LocalDateTime getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(LocalDateTime lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    public LocalDateTime getEditableUntil() {
        return editableUntil;
    }

    public void setEditableUntil(LocalDateTime editableUntil) {
        this.editableUntil = editableUntil;
    }

    // Builder class
    public static class PersonNoteEntityBuilder {
        private final PersonNoteEntity entity = new PersonNoteEntity();

        public PersonNoteEntityBuilder personId(Long personId) {
            entity.setPersonId(personId);
            return this;
        }

        public PersonNoteEntityBuilder personType(PersonNoteType personType) {
            entity.setPersonType(personType);
            return this;
        }

        public PersonNoteEntityBuilder referralId(String referralId) {
            entity.setReferralId(referralId);
            return this;
        }

        public PersonNoteEntityBuilder caseId(Long caseId) {
            entity.setCaseId(caseId);
            return this;
        }

        public PersonNoteEntityBuilder noteDate(LocalDate noteDate) {
            entity.setNoteDate(noteDate);
            return this;
        }

        public PersonNoteEntityBuilder contactMethod(ContactMethod contactMethod) {
            entity.setContactMethod(contactMethod);
            return this;
        }

        public PersonNoteEntityBuilder subject(String subject) {
            entity.setSubject(subject);
            return this;
        }

        public PersonNoteEntityBuilder content(String content) {
            entity.setContent(content);
            return this;
        }

        public PersonNoteEntityBuilder category(NoteCategory category) {
            entity.setCategory(category);
            return this;
        }

        public PersonNoteEntityBuilder followUpNeeded(Boolean followUpNeeded) {
            entity.setFollowUpNeeded(followUpNeeded);
            return this;
        }

        public PersonNoteEntityBuilder followUpDate(LocalDate followUpDate) {
            entity.setFollowUpDate(followUpDate);
            return this;
        }

        public PersonNoteEntityBuilder importance(NoteImportance importance) {
            entity.setImportance(importance);
            return this;
        }

        public PersonNoteEntityBuilder createdBy(String createdBy) {
            entity.setCreatedBy(createdBy);
            return this;
        }

        public PersonNoteEntityBuilder createdByName(String createdByName) {
            entity.setCreatedByName(createdByName);
            return this;
        }

        public PersonNoteEntity build() {
            return entity;
        }
    }
}
