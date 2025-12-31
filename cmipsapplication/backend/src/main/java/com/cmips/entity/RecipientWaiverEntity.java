package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Recipient Waiver Entity - SOC 2298 waiver for Tier 2 CORI convictions
 * Based on DSD Section 23 - Provider Management CORI Business Rules
 *
 * When a provider has Tier 2 convictions (not permanently disqualifying),
 * the recipient can sign a waiver (SOC 2298) to hire that provider.
 */
@Entity
@Table(name = "recipient_waivers")
public class RecipientWaiverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Recipient who is signing the waiver
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    // Provider with Tier 2 conviction
    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "provider_name", length = 200)
    private String providerName;

    @Column(name = "provider_number", length = 20)
    private String providerNumber;

    // CORI reference
    @Column(name = "cori_id")
    private String coriId;

    // Waiver Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WaiverStatus status = WaiverStatus.PENDING_DISCLOSURE;

    // ==================== CONVICTION INFORMATION ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "conviction_tier")
    private ConvictionTier convictionTier = ConvictionTier.TIER2;

    @Column(name = "conviction_type", length = 200)
    private String convictionType;

    @Column(name = "conviction_date")
    private LocalDate convictionDate;

    @Column(name = "conviction_description", columnDefinition = "TEXT")
    private String convictionDescription;

    @Column(name = "sentence_served")
    private Boolean sentenceServed;

    @Column(name = "years_since_conviction")
    private Integer yearsSinceConviction;

    // ==================== DISCLOSURE PROCESS ====================

    @Column(name = "disclosure_date")
    private LocalDate disclosureDate;

    @Column(name = "disclosed_by_worker_id", length = 100)
    private String disclosedByWorkerId;

    @Column(name = "disclosed_by_worker_name", length = 200)
    private String disclosedByWorkerName;

    @Column(name = "disclosure_method", length = 50)
    private String disclosureMethod; // IN_PERSON, PHONE, VIDEO

    @Column(name = "recipient_acknowledged_disclosure")
    private Boolean recipientAcknowledgedDisclosure = false;

    @Column(name = "acknowledgment_date")
    private LocalDate acknowledgmentDate;

    // ==================== WAIVER DECISION ====================

    @Column(name = "waiver_request_date")
    private LocalDate waiverRequestDate;

    // Recipient's decision to proceed with waiver
    @Column(name = "recipient_decision")
    private Boolean recipientDecision; // true = wants to hire, false = does not

    @Column(name = "recipient_decision_date")
    private LocalDate recipientDecisionDate;

    @Column(name = "recipient_justification", columnDefinition = "TEXT")
    private String recipientJustification;

    // SOC 2298 Form
    @Column(name = "soc_2298_signed")
    private Boolean soc2298Signed = false;

    @Column(name = "soc_2298_signed_date")
    private LocalDate soc2298SignedDate;

    @Column(name = "soc_2298_witness_name", length = 200)
    private String soc2298WitnessName;

    // Electronic signature info
    @Column(name = "electronic_signature")
    private Boolean electronicSignature = false;

    @Column(name = "signature_ip_address", length = 50)
    private String signatureIpAddress;

    // ==================== COUNTY REVIEW ====================

    @Column(name = "county_review_required")
    private Boolean countyReviewRequired = true;

    @Column(name = "county_reviewer_id", length = 100)
    private String countyReviewerId;

    @Column(name = "county_reviewer_name", length = 200)
    private String countyReviewerName;

    @Column(name = "county_review_date")
    private LocalDate countyReviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "county_decision")
    private CountyDecision countyDecision;

    @Column(name = "county_decision_reason", length = 500)
    private String countyDecisionReason;

    // Supervisor approval (if required)
    @Column(name = "supervisor_approval_required")
    private Boolean supervisorApprovalRequired = false;

    @Column(name = "supervisor_id", length = 100)
    private String supervisorId;

    @Column(name = "supervisor_name", length = 200)
    private String supervisorName;

    @Column(name = "supervisor_decision")
    private Boolean supervisorDecision;

    @Column(name = "supervisor_decision_date")
    private LocalDate supervisorDecisionDate;

    // ==================== EFFECTIVE DATES ====================

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // Revocation
    @Column(name = "revoked")
    private Boolean revoked = false;

    @Column(name = "revocation_date")
    private LocalDate revocationDate;

    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

    @Column(name = "revoked_by", length = 100)
    private String revokedBy;

    // ==================== COUNTY ====================

    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "county_name", length = 100)
    private String countyName;

    // ==================== NOTES ====================

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ==================== AUDIT FIELDS ====================

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // ==================== ENUMS ====================

    public enum WaiverStatus {
        PENDING_DISCLOSURE,     // Waiting for disclosure to recipient
        DISCLOSED,              // Recipient has been informed
        PENDING_DECISION,       // Waiting for recipient decision
        WAIVER_REQUESTED,       // Recipient wants to proceed with waiver
        WAIVER_DECLINED,        // Recipient declined to hire
        SOC_2298_PENDING,       // Waiting for form signature
        SOC_2298_SIGNED,        // Form signed
        COUNTY_REVIEW,          // Under county review
        SUPERVISOR_REVIEW,      // Under supervisor review
        APPROVED,               // Waiver approved
        DENIED,                 // Waiver denied
        EXPIRED,                // Waiver expired
        REVOKED                 // Waiver revoked
    }

    public enum ConvictionTier {
        TIER1,  // Permanently disqualifying - waiver not possible
        TIER2,  // Disqualifying but waiver possible
        TIER_2  // Alias for compatibility
    }

    public enum CountyDecision {
        APPROVED,
        DENIED,
        PENDING_MORE_INFO,
        CONDITIONAL,
        PENDING
    }

    // ==================== LIFECYCLE HOOKS ====================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = WaiverStatus.PENDING_DISCLOSURE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Check if waiver is currently active
     */
    public boolean isActive() {
        if (status != WaiverStatus.APPROVED) return false;
        if (Boolean.TRUE.equals(revoked)) return false;
        if (expirationDate != null && LocalDate.now().isAfter(expirationDate)) return false;
        return true;
    }

    /**
     * Progress waiver to next status
     */
    public void progressStatus(WaiverStatus newStatus) {
        this.status = newStatus;
    }

    // ==================== CONSTRUCTORS ====================

    public RecipientWaiverEntity() {
    }

    public static RecipientWaiverEntityBuilder builder() {
        return new RecipientWaiverEntityBuilder();
    }

    // ==================== GETTERS AND SETTERS ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderNumber() {
        return providerNumber;
    }

    public void setProviderNumber(String providerNumber) {
        this.providerNumber = providerNumber;
    }

    public String getCoriId() {
        return coriId;
    }

    public void setCoriId(String coriId) {
        this.coriId = coriId;
    }

    public WaiverStatus getStatus() {
        return status;
    }

    public void setStatus(WaiverStatus status) {
        this.status = status;
    }

    public ConvictionTier getConvictionTier() {
        return convictionTier;
    }

    public void setConvictionTier(ConvictionTier convictionTier) {
        this.convictionTier = convictionTier;
    }

    public String getConvictionType() {
        return convictionType;
    }

    public void setConvictionType(String convictionType) {
        this.convictionType = convictionType;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public void setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
    }

    public String getConvictionDescription() {
        return convictionDescription;
    }

    public void setConvictionDescription(String convictionDescription) {
        this.convictionDescription = convictionDescription;
    }

    public Boolean getSentenceServed() {
        return sentenceServed;
    }

    public void setSentenceServed(Boolean sentenceServed) {
        this.sentenceServed = sentenceServed;
    }

    public Integer getYearsSinceConviction() {
        return yearsSinceConviction;
    }

    public void setYearsSinceConviction(Integer yearsSinceConviction) {
        this.yearsSinceConviction = yearsSinceConviction;
    }

    public LocalDate getDisclosureDate() {
        return disclosureDate;
    }

    public void setDisclosureDate(LocalDate disclosureDate) {
        this.disclosureDate = disclosureDate;
    }

    public String getDisclosedByWorkerId() {
        return disclosedByWorkerId;
    }

    public void setDisclosedByWorkerId(String disclosedByWorkerId) {
        this.disclosedByWorkerId = disclosedByWorkerId;
    }

    public String getDisclosedByWorkerName() {
        return disclosedByWorkerName;
    }

    public void setDisclosedByWorkerName(String disclosedByWorkerName) {
        this.disclosedByWorkerName = disclosedByWorkerName;
    }

    public String getDisclosureMethod() {
        return disclosureMethod;
    }

    public void setDisclosureMethod(String disclosureMethod) {
        this.disclosureMethod = disclosureMethod;
    }

    public Boolean getRecipientAcknowledgedDisclosure() {
        return recipientAcknowledgedDisclosure;
    }

    public void setRecipientAcknowledgedDisclosure(Boolean recipientAcknowledgedDisclosure) {
        this.recipientAcknowledgedDisclosure = recipientAcknowledgedDisclosure;
    }

    public LocalDate getAcknowledgmentDate() {
        return acknowledgmentDate;
    }

    public void setAcknowledgmentDate(LocalDate acknowledgmentDate) {
        this.acknowledgmentDate = acknowledgmentDate;
    }

    public LocalDate getWaiverRequestDate() {
        return waiverRequestDate;
    }

    public void setWaiverRequestDate(LocalDate waiverRequestDate) {
        this.waiverRequestDate = waiverRequestDate;
    }

    public Boolean getRecipientDecision() {
        return recipientDecision;
    }

    public void setRecipientDecision(Boolean recipientDecision) {
        this.recipientDecision = recipientDecision;
    }

    public LocalDate getRecipientDecisionDate() {
        return recipientDecisionDate;
    }

    public void setRecipientDecisionDate(LocalDate recipientDecisionDate) {
        this.recipientDecisionDate = recipientDecisionDate;
    }

    public String getRecipientJustification() {
        return recipientJustification;
    }

    public void setRecipientJustification(String recipientJustification) {
        this.recipientJustification = recipientJustification;
    }

    public Boolean getSoc2298Signed() {
        return soc2298Signed;
    }

    public void setSoc2298Signed(Boolean soc2298Signed) {
        this.soc2298Signed = soc2298Signed;
    }

    public LocalDate getSoc2298SignedDate() {
        return soc2298SignedDate;
    }

    public void setSoc2298SignedDate(LocalDate soc2298SignedDate) {
        this.soc2298SignedDate = soc2298SignedDate;
    }

    public String getSoc2298WitnessName() {
        return soc2298WitnessName;
    }

    public void setSoc2298WitnessName(String soc2298WitnessName) {
        this.soc2298WitnessName = soc2298WitnessName;
    }

    public Boolean getElectronicSignature() {
        return electronicSignature;
    }

    public void setElectronicSignature(Boolean electronicSignature) {
        this.electronicSignature = electronicSignature;
    }

    public String getSignatureIpAddress() {
        return signatureIpAddress;
    }

    public void setSignatureIpAddress(String signatureIpAddress) {
        this.signatureIpAddress = signatureIpAddress;
    }

    public Boolean getCountyReviewRequired() {
        return countyReviewRequired;
    }

    public void setCountyReviewRequired(Boolean countyReviewRequired) {
        this.countyReviewRequired = countyReviewRequired;
    }

    public String getCountyReviewerId() {
        return countyReviewerId;
    }

    public void setCountyReviewerId(String countyReviewerId) {
        this.countyReviewerId = countyReviewerId;
    }

    public String getCountyReviewerName() {
        return countyReviewerName;
    }

    public void setCountyReviewerName(String countyReviewerName) {
        this.countyReviewerName = countyReviewerName;
    }

    public LocalDate getCountyReviewDate() {
        return countyReviewDate;
    }

    public void setCountyReviewDate(LocalDate countyReviewDate) {
        this.countyReviewDate = countyReviewDate;
    }

    public CountyDecision getCountyDecision() {
        return countyDecision;
    }

    public void setCountyDecision(CountyDecision countyDecision) {
        this.countyDecision = countyDecision;
    }

    public String getCountyDecisionReason() {
        return countyDecisionReason;
    }

    public void setCountyDecisionReason(String countyDecisionReason) {
        this.countyDecisionReason = countyDecisionReason;
    }

    public Boolean getSupervisorApprovalRequired() {
        return supervisorApprovalRequired;
    }

    public void setSupervisorApprovalRequired(Boolean supervisorApprovalRequired) {
        this.supervisorApprovalRequired = supervisorApprovalRequired;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public Boolean getSupervisorDecision() {
        return supervisorDecision;
    }

    public void setSupervisorDecision(Boolean supervisorDecision) {
        this.supervisorDecision = supervisorDecision;
    }

    public LocalDate getSupervisorDecisionDate() {
        return supervisorDecisionDate;
    }

    public void setSupervisorDecisionDate(LocalDate supervisorDecisionDate) {
        this.supervisorDecisionDate = supervisorDecisionDate;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public LocalDate getRevocationDate() {
        return revocationDate;
    }

    public void setRevocationDate(LocalDate revocationDate) {
        this.revocationDate = revocationDate;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public String getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(String revokedBy) {
        this.revokedBy = revokedBy;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Builder
    public static class RecipientWaiverEntityBuilder {
        private final RecipientWaiverEntity entity = new RecipientWaiverEntity();

        public RecipientWaiverEntityBuilder recipientId(Long recipientId) {
            entity.setRecipientId(recipientId);
            return this;
        }

        public RecipientWaiverEntityBuilder providerId(Long providerId) {
            entity.setProviderId(providerId);
            return this;
        }

        public RecipientWaiverEntityBuilder coriId(String coriId) {
            entity.setCoriId(coriId);
            return this;
        }

        public RecipientWaiverEntityBuilder countyCode(String countyCode) {
            entity.setCountyCode(countyCode);
            return this;
        }

        public RecipientWaiverEntityBuilder createdBy(String createdBy) {
            entity.setCreatedBy(createdBy);
            return this;
        }

        public RecipientWaiverEntityBuilder convictionTier(ConvictionTier tier) {
            entity.setConvictionTier(tier);
            return this;
        }

        public RecipientWaiverEntityBuilder convictionDetails(String details) {
            entity.setConvictionDescription(details);
            return this;
        }

        public RecipientWaiverEntityBuilder convictionDate(LocalDate date) {
            entity.setConvictionDate(date);
            return this;
        }

        public RecipientWaiverEntityBuilder status(WaiverStatus status) {
            entity.setStatus(status);
            return this;
        }

        public RecipientWaiverEntity build() {
            return entity;
        }
    }

    // Additional setters for compatibility
    public void setRevokedDate(LocalDate date) {
        this.revocationDate = date;
    }

    public void setRevokedReason(String reason) {
        this.revocationReason = reason;
    }

    public String getConditions() {
        return this.notes; // Use notes field for conditions
    }

    public void setConditions(String conditions) {
        // Append to notes
        if (this.notes == null) {
            this.notes = "Conditions: " + conditions;
        } else {
            this.notes += "\nConditions: " + conditions;
        }
    }
}
