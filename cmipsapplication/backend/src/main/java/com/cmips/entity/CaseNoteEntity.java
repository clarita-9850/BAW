package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Case Note Entity - Tracks notes on cases and persons
 * Based on DSD Section 20 - Person Notes Management
 */
@Entity
@Table(name = "case_notes")
public class CaseNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "provider_id")
    private Long providerId;

    // Note Type: CASE_NOTE, PERSON_NOTE, CONTACT_NOTE, ASSESSMENT_NOTE
    @Column(name = "note_type", length = 50)
    private String noteType;

    // Note Subject/Title
    @Column(name = "subject", length = 200)
    private String subject;

    // Note Content
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // Status: ACTIVE, CANCELLED
    @Column(name = "status", length = 20)
    private String status;

    // Notes are edited by appending, not modifying original
    @Column(name = "appended_content", columnDefinition = "TEXT")
    private String appendedContent;

    @Column(name = "appended_at")
    private LocalDateTime appendedAt;

    @Column(name = "appended_by", length = 100)
    private String appendedBy;

    // Cancellation Information
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    // Audit Fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Constructors
    public CaseNoteEntity() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
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

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppendedContent() {
        return appendedContent;
    }

    public void setAppendedContent(String appendedContent) {
        this.appendedContent = appendedContent;
    }

    public LocalDateTime getAppendedAt() {
        return appendedAt;
    }

    public void setAppendedAt(LocalDateTime appendedAt) {
        this.appendedAt = appendedAt;
    }

    public String getAppendedBy() {
        return appendedBy;
    }

    public void setAppendedBy(String appendedBy) {
        this.appendedBy = appendedBy;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Append content to note (instead of modifying original)
    public void appendNote(String additionalContent, String userId) {
        if (this.appendedContent == null) {
            this.appendedContent = additionalContent;
        } else {
            this.appendedContent = this.appendedContent + "\n\n---\n\n" + additionalContent;
        }
        this.appendedAt = LocalDateTime.now();
        this.appendedBy = userId;
    }

    // Cancel note
    public void cancel(String userId, String reason) {
        this.status = "CANCELLED";
        this.cancelledAt = LocalDateTime.now();
        this.cancelledBy = userId;
        this.cancellationReason = reason;
    }

    // Get full note content (original + appended)
    public String getFullContent() {
        if (appendedContent == null || appendedContent.isEmpty()) {
            return content;
        }
        return content + "\n\n---\nAppended on " + appendedAt + " by " + appendedBy + ":\n" + appendedContent;
    }

    // Builder pattern
    public static CaseNoteEntityBuilder builder() {
        return new CaseNoteEntityBuilder();
    }

    public static class CaseNoteEntityBuilder {
        private Long id;
        private Long caseId;
        private Long recipientId;
        private Long providerId;
        private String noteType;
        private String subject;
        private String content;
        private String status;
        private String appendedContent;
        private LocalDateTime appendedAt;
        private String appendedBy;
        private LocalDateTime cancelledAt;
        private String cancelledBy;
        private String cancellationReason;
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;

        CaseNoteEntityBuilder() {
        }

        public CaseNoteEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CaseNoteEntityBuilder caseId(Long caseId) {
            this.caseId = caseId;
            return this;
        }

        public CaseNoteEntityBuilder recipientId(Long recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        public CaseNoteEntityBuilder providerId(Long providerId) {
            this.providerId = providerId;
            return this;
        }

        public CaseNoteEntityBuilder noteType(String noteType) {
            this.noteType = noteType;
            return this;
        }

        public CaseNoteEntityBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public CaseNoteEntityBuilder content(String content) {
            this.content = content;
            return this;
        }

        public CaseNoteEntityBuilder status(String status) {
            this.status = status;
            return this;
        }

        public CaseNoteEntityBuilder appendedContent(String appendedContent) {
            this.appendedContent = appendedContent;
            return this;
        }

        public CaseNoteEntityBuilder appendedAt(LocalDateTime appendedAt) {
            this.appendedAt = appendedAt;
            return this;
        }

        public CaseNoteEntityBuilder appendedBy(String appendedBy) {
            this.appendedBy = appendedBy;
            return this;
        }

        public CaseNoteEntityBuilder cancelledAt(LocalDateTime cancelledAt) {
            this.cancelledAt = cancelledAt;
            return this;
        }

        public CaseNoteEntityBuilder cancelledBy(String cancelledBy) {
            this.cancelledBy = cancelledBy;
            return this;
        }

        public CaseNoteEntityBuilder cancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
            return this;
        }

        public CaseNoteEntityBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public CaseNoteEntityBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public CaseNoteEntityBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public CaseNoteEntityBuilder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public CaseNoteEntity build() {
            CaseNoteEntity entity = new CaseNoteEntity();
            entity.setId(id);
            entity.setCaseId(caseId);
            entity.setRecipientId(recipientId);
            entity.setProviderId(providerId);
            entity.setNoteType(noteType);
            entity.setSubject(subject);
            entity.setContent(content);
            entity.setStatus(status);
            entity.setAppendedContent(appendedContent);
            entity.setAppendedAt(appendedAt);
            entity.setAppendedBy(appendedBy);
            entity.setCancelledAt(cancelledAt);
            entity.setCancelledBy(cancelledBy);
            entity.setCancellationReason(cancellationReason);
            entity.setCreatedAt(createdAt);
            entity.setCreatedBy(createdBy);
            entity.setUpdatedAt(updatedAt);
            entity.setUpdatedBy(updatedBy);
            return entity;
        }
    }
}
