package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Health Care Certification Entity (SOC 873)
 * Based on DSD Section 21 - BR SE 28-50
 */
@Entity
@Table(name = "health_care_certifications")
public class HealthCareCertificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    // Method: USER_ENTERED (3rd Party), FORM_GENERATED (County)
    @Column(name = "certification_method", length = 50)
    private String certificationMethod;

    // Certification Type
    @Column(name = "certification_type", length = 100)
    private String certificationType;

    // SOC 873 Given to Applicant by Other Organization (3rd Party)
    @Column(name = "third_party_provided")
    private Boolean thirdPartyProvided;

    // Form Generated Information
    @Column(name = "form_type", length = 100)
    private String formType; // SOC_873_874, SOC_873_ENGLISH_ONLY, SOC_873L_874L, SOC_873L_ENGLISH_ONLY

    @Column(name = "print_option", length = 50)
    private String printOption; // NIGHTLY_BATCH, PRINT_NOW, GENERATE_LOCAL, SEND_ESP

    @Column(name = "print_date")
    private LocalDate printDate;

    @Column(name = "language", length = 50)
    private String language;

    // Due Date Management (per BR SE 28-31)
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "mailed_given_to_recipient_date")
    private LocalDate mailedGivenToRecipientDate;

    // Good Cause Extension (per BR SE 31)
    @Column(name = "good_cause_extension_requested")
    private Boolean goodCauseExtensionRequested;

    @Column(name = "good_cause_extension_date")
    private LocalDate goodCauseExtensionDate;

    @Column(name = "good_cause_extension_entered_date")
    private LocalDate goodCauseExtensionEnteredDate;

    @Column(name = "good_cause_extension_due_date")
    private LocalDate goodCauseExtensionDueDate;

    // Documentation Received (per BR SE 32)
    @Column(name = "documentation_received_date")
    private LocalDate documentationReceivedDate;

    @Column(name = "documentation_received_entered_date")
    private LocalDate documentationReceivedEnteredDate;

    // Exception Granted (per BR SE 30)
    @Column(name = "exception_granted")
    private Boolean exceptionGranted;

    @Column(name = "exception_granted_date")
    private LocalDate exceptionGrantedDate;

    @Column(name = "exception_granted_entered_date")
    private LocalDate exceptionGrantedEnteredDate;

    @Column(name = "exception_reason", length = 100)
    private String exceptionReason; // HOSPITAL_DISCHARGE, RISK_OUT_OF_HOME_PLACEMENT

    // Status: ACTIVE, COMPLETED, INACTIVATED
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "inactivated_date")
    private LocalDate inactivatedDate;

    @Column(name = "inactivated_by", length = 100)
    private String inactivatedBy;

    // Electronic Delivery (ESP)
    @Column(name = "sent_to_esp")
    private Boolean sentToEsp;

    @Column(name = "electronic_form_due_date")
    private LocalDate electronicFormDueDate;

    // Comments
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    // Audit Fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // No-args constructor
    public HealthCareCertificationEntity() {
    }

    // All-args constructor
    public HealthCareCertificationEntity(Long id, Long caseId, Long recipientId, String certificationMethod,
                                         String certificationType, Boolean thirdPartyProvided, String formType,
                                         String printOption, LocalDate printDate, String language, LocalDate dueDate,
                                         LocalDate mailedGivenToRecipientDate, Boolean goodCauseExtensionRequested,
                                         LocalDate goodCauseExtensionDate, LocalDate goodCauseExtensionEnteredDate,
                                         LocalDate goodCauseExtensionDueDate, LocalDate documentationReceivedDate,
                                         LocalDate documentationReceivedEnteredDate, Boolean exceptionGranted,
                                         LocalDate exceptionGrantedDate, LocalDate exceptionGrantedEnteredDate,
                                         String exceptionReason, String status, LocalDate inactivatedDate,
                                         String inactivatedBy, Boolean sentToEsp, LocalDate electronicFormDueDate,
                                         String comments, LocalDateTime createdAt, String createdBy,
                                         LocalDateTime updatedAt, String updatedBy) {
        this.id = id;
        this.caseId = caseId;
        this.recipientId = recipientId;
        this.certificationMethod = certificationMethod;
        this.certificationType = certificationType;
        this.thirdPartyProvided = thirdPartyProvided;
        this.formType = formType;
        this.printOption = printOption;
        this.printDate = printDate;
        this.language = language;
        this.dueDate = dueDate;
        this.mailedGivenToRecipientDate = mailedGivenToRecipientDate;
        this.goodCauseExtensionRequested = goodCauseExtensionRequested;
        this.goodCauseExtensionDate = goodCauseExtensionDate;
        this.goodCauseExtensionEnteredDate = goodCauseExtensionEnteredDate;
        this.goodCauseExtensionDueDate = goodCauseExtensionDueDate;
        this.documentationReceivedDate = documentationReceivedDate;
        this.documentationReceivedEnteredDate = documentationReceivedEnteredDate;
        this.exceptionGranted = exceptionGranted;
        this.exceptionGrantedDate = exceptionGrantedDate;
        this.exceptionGrantedEnteredDate = exceptionGrantedEnteredDate;
        this.exceptionReason = exceptionReason;
        this.status = status;
        this.inactivatedDate = inactivatedDate;
        this.inactivatedBy = inactivatedBy;
        this.sentToEsp = sentToEsp;
        this.electronicFormDueDate = electronicFormDueDate;
        this.comments = comments;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
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

    public String getCertificationMethod() {
        return certificationMethod;
    }

    public void setCertificationMethod(String certificationMethod) {
        this.certificationMethod = certificationMethod;
    }

    public String getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(String certificationType) {
        this.certificationType = certificationType;
    }

    public Boolean getThirdPartyProvided() {
        return thirdPartyProvided;
    }

    public void setThirdPartyProvided(Boolean thirdPartyProvided) {
        this.thirdPartyProvided = thirdPartyProvided;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public String getPrintOption() {
        return printOption;
    }

    public void setPrintOption(String printOption) {
        this.printOption = printOption;
    }

    public LocalDate getPrintDate() {
        return printDate;
    }

    public void setPrintDate(LocalDate printDate) {
        this.printDate = printDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getMailedGivenToRecipientDate() {
        return mailedGivenToRecipientDate;
    }

    public void setMailedGivenToRecipientDate(LocalDate mailedGivenToRecipientDate) {
        this.mailedGivenToRecipientDate = mailedGivenToRecipientDate;
    }

    public Boolean getGoodCauseExtensionRequested() {
        return goodCauseExtensionRequested;
    }

    public void setGoodCauseExtensionRequested(Boolean goodCauseExtensionRequested) {
        this.goodCauseExtensionRequested = goodCauseExtensionRequested;
    }

    public LocalDate getGoodCauseExtensionDate() {
        return goodCauseExtensionDate;
    }

    public void setGoodCauseExtensionDate(LocalDate goodCauseExtensionDate) {
        this.goodCauseExtensionDate = goodCauseExtensionDate;
    }

    public LocalDate getGoodCauseExtensionEnteredDate() {
        return goodCauseExtensionEnteredDate;
    }

    public void setGoodCauseExtensionEnteredDate(LocalDate goodCauseExtensionEnteredDate) {
        this.goodCauseExtensionEnteredDate = goodCauseExtensionEnteredDate;
    }

    public LocalDate getGoodCauseExtensionDueDate() {
        return goodCauseExtensionDueDate;
    }

    public void setGoodCauseExtensionDueDate(LocalDate goodCauseExtensionDueDate) {
        this.goodCauseExtensionDueDate = goodCauseExtensionDueDate;
    }

    public LocalDate getDocumentationReceivedDate() {
        return documentationReceivedDate;
    }

    public void setDocumentationReceivedDate(LocalDate documentationReceivedDate) {
        this.documentationReceivedDate = documentationReceivedDate;
    }

    public LocalDate getDocumentationReceivedEnteredDate() {
        return documentationReceivedEnteredDate;
    }

    public void setDocumentationReceivedEnteredDate(LocalDate documentationReceivedEnteredDate) {
        this.documentationReceivedEnteredDate = documentationReceivedEnteredDate;
    }

    public Boolean getExceptionGranted() {
        return exceptionGranted;
    }

    public void setExceptionGranted(Boolean exceptionGranted) {
        this.exceptionGranted = exceptionGranted;
    }

    public LocalDate getExceptionGrantedDate() {
        return exceptionGrantedDate;
    }

    public void setExceptionGrantedDate(LocalDate exceptionGrantedDate) {
        this.exceptionGrantedDate = exceptionGrantedDate;
    }

    public LocalDate getExceptionGrantedEnteredDate() {
        return exceptionGrantedEnteredDate;
    }

    public void setExceptionGrantedEnteredDate(LocalDate exceptionGrantedEnteredDate) {
        this.exceptionGrantedEnteredDate = exceptionGrantedEnteredDate;
    }

    public String getExceptionReason() {
        return exceptionReason;
    }

    public void setExceptionReason(String exceptionReason) {
        this.exceptionReason = exceptionReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getInactivatedDate() {
        return inactivatedDate;
    }

    public void setInactivatedDate(LocalDate inactivatedDate) {
        this.inactivatedDate = inactivatedDate;
    }

    public String getInactivatedBy() {
        return inactivatedBy;
    }

    public void setInactivatedBy(String inactivatedBy) {
        this.inactivatedBy = inactivatedBy;
    }

    public Boolean getSentToEsp() {
        return sentToEsp;
    }

    public void setSentToEsp(Boolean sentToEsp) {
        this.sentToEsp = sentToEsp;
    }

    public LocalDate getElectronicFormDueDate() {
        return electronicFormDueDate;
    }

    public void setElectronicFormDueDate(LocalDate electronicFormDueDate) {
        this.electronicFormDueDate = electronicFormDueDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    /**
     * Calculate Due Date per BR SE 28
     * Initial due date: 45 calendar days from form request/generation date
     */
    public void calculateInitialDueDate() {
        if (printDate != null) {
            this.dueDate = printDate.plusDays(45);
        }
    }

    /**
     * Recalculate Due Date per BR SE 29
     * When SOC 873 & 874 Mailed/Given To Recipient date entered
     */
    public void recalculateDueDateFromMailedDate() {
        if (mailedGivenToRecipientDate != null) {
            this.dueDate = mailedGivenToRecipientDate.plusDays(45);
        }
    }

    /**
     * Calculate Good Cause Extension Due Date per BR SE 31
     * Good Cause Extension Due Date = Due Date + 45 days
     */
    public void calculateGoodCauseExtensionDueDate() {
        if (dueDate != null && Boolean.TRUE.equals(goodCauseExtensionRequested)) {
            this.goodCauseExtensionEnteredDate = LocalDate.now();
            this.goodCauseExtensionDueDate = dueDate.plusDays(45);
        }
    }

    /**
     * Calculate Due Date from Exception Granted Date per BR SE 30
     */
    public void calculateDueDateFromException() {
        if (Boolean.TRUE.equals(exceptionGranted) && exceptionGrantedDate != null) {
            this.exceptionGrantedEnteredDate = LocalDate.now();
            this.dueDate = exceptionGrantedDate.plusDays(45);
        }
    }

    /**
     * Inactivate Health Care Certification per BR SE 34
     */
    public void inactivate(String userId) {
        this.status = "INACTIVATED";
        this.inactivatedDate = LocalDate.now();
        this.inactivatedBy = userId;
    }

    /**
     * Complete certification when documentation received per BR SE 32
     */
    public void complete() {
        if (documentationReceivedDate != null && certificationType != null) {
            this.status = "COMPLETED";
            this.documentationReceivedEnteredDate = LocalDate.now();
        }
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long caseId;
        private Long recipientId;
        private String certificationMethod;
        private String certificationType;
        private Boolean thirdPartyProvided;
        private String formType;
        private String printOption;
        private LocalDate printDate;
        private String language;
        private LocalDate dueDate;
        private LocalDate mailedGivenToRecipientDate;
        private Boolean goodCauseExtensionRequested;
        private LocalDate goodCauseExtensionDate;
        private LocalDate goodCauseExtensionEnteredDate;
        private LocalDate goodCauseExtensionDueDate;
        private LocalDate documentationReceivedDate;
        private LocalDate documentationReceivedEnteredDate;
        private Boolean exceptionGranted;
        private LocalDate exceptionGrantedDate;
        private LocalDate exceptionGrantedEnteredDate;
        private String exceptionReason;
        private String status;
        private LocalDate inactivatedDate;
        private String inactivatedBy;
        private Boolean sentToEsp;
        private LocalDate electronicFormDueDate;
        private String comments;
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder caseId(Long caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder recipientId(Long recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        public Builder certificationMethod(String certificationMethod) {
            this.certificationMethod = certificationMethod;
            return this;
        }

        public Builder certificationType(String certificationType) {
            this.certificationType = certificationType;
            return this;
        }

        public Builder thirdPartyProvided(Boolean thirdPartyProvided) {
            this.thirdPartyProvided = thirdPartyProvided;
            return this;
        }

        public Builder formType(String formType) {
            this.formType = formType;
            return this;
        }

        public Builder printOption(String printOption) {
            this.printOption = printOption;
            return this;
        }

        public Builder printDate(LocalDate printDate) {
            this.printDate = printDate;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder dueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder mailedGivenToRecipientDate(LocalDate mailedGivenToRecipientDate) {
            this.mailedGivenToRecipientDate = mailedGivenToRecipientDate;
            return this;
        }

        public Builder goodCauseExtensionRequested(Boolean goodCauseExtensionRequested) {
            this.goodCauseExtensionRequested = goodCauseExtensionRequested;
            return this;
        }

        public Builder goodCauseExtensionDate(LocalDate goodCauseExtensionDate) {
            this.goodCauseExtensionDate = goodCauseExtensionDate;
            return this;
        }

        public Builder goodCauseExtensionEnteredDate(LocalDate goodCauseExtensionEnteredDate) {
            this.goodCauseExtensionEnteredDate = goodCauseExtensionEnteredDate;
            return this;
        }

        public Builder goodCauseExtensionDueDate(LocalDate goodCauseExtensionDueDate) {
            this.goodCauseExtensionDueDate = goodCauseExtensionDueDate;
            return this;
        }

        public Builder documentationReceivedDate(LocalDate documentationReceivedDate) {
            this.documentationReceivedDate = documentationReceivedDate;
            return this;
        }

        public Builder documentationReceivedEnteredDate(LocalDate documentationReceivedEnteredDate) {
            this.documentationReceivedEnteredDate = documentationReceivedEnteredDate;
            return this;
        }

        public Builder exceptionGranted(Boolean exceptionGranted) {
            this.exceptionGranted = exceptionGranted;
            return this;
        }

        public Builder exceptionGrantedDate(LocalDate exceptionGrantedDate) {
            this.exceptionGrantedDate = exceptionGrantedDate;
            return this;
        }

        public Builder exceptionGrantedEnteredDate(LocalDate exceptionGrantedEnteredDate) {
            this.exceptionGrantedEnteredDate = exceptionGrantedEnteredDate;
            return this;
        }

        public Builder exceptionReason(String exceptionReason) {
            this.exceptionReason = exceptionReason;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder inactivatedDate(LocalDate inactivatedDate) {
            this.inactivatedDate = inactivatedDate;
            return this;
        }

        public Builder inactivatedBy(String inactivatedBy) {
            this.inactivatedBy = inactivatedBy;
            return this;
        }

        public Builder sentToEsp(Boolean sentToEsp) {
            this.sentToEsp = sentToEsp;
            return this;
        }

        public Builder electronicFormDueDate(LocalDate electronicFormDueDate) {
            this.electronicFormDueDate = electronicFormDueDate;
            return this;
        }

        public Builder comments(String comments) {
            this.comments = comments;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public HealthCareCertificationEntity build() {
            return new HealthCareCertificationEntity(id, caseId, recipientId, certificationMethod,
                    certificationType, thirdPartyProvided, formType, printOption, printDate, language,
                    dueDate, mailedGivenToRecipientDate, goodCauseExtensionRequested, goodCauseExtensionDate,
                    goodCauseExtensionEnteredDate, goodCauseExtensionDueDate, documentationReceivedDate,
                    documentationReceivedEnteredDate, exceptionGranted, exceptionGrantedDate,
                    exceptionGrantedEnteredDate, exceptionReason, status, inactivatedDate, inactivatedBy,
                    sentToEsp, electronicFormDueDate, comments, createdAt, createdBy, updatedAt, updatedBy);
        }
    }
}
