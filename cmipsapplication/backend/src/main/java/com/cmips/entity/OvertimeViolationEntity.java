package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Overtime Violation Entity - Tracks provider overtime violations
 * Based on DSD Section 23 - BR PVM 90-190+
 */
@Entity
@Table(name = "overtime_violations")
public class OvertimeViolationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "violation_number")
    private Integer violationNumber;

    @Column(name = "violation_type", length = 100)
    private String violationType;

    @Column(name = "service_month")
    private Integer serviceMonth;

    @Column(name = "service_year")
    private Integer serviceYear;

    @Column(name = "violation_date")
    private LocalDate violationDate;

    @Column(name = "hours_claimed")
    private Double hoursClaimed;

    @Column(name = "maximum_allowed")
    private Double maximumAllowed;

    @Column(name = "hours_exceeded")
    private Double hoursExceeded;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ViolationStatus status;

    @Column(name = "county_review_date")
    private LocalDate countyReviewDate;

    @Column(name = "county_reviewer_id", length = 100)
    private String countyReviewerId;

    @Column(name = "county_review_outcome", length = 50)
    private String countyReviewOutcome;

    @Column(name = "county_review_comments", columnDefinition = "TEXT")
    private String countyReviewComments;

    @Column(name = "supervisor_review_date")
    private LocalDate supervisorReviewDate;

    @Column(name = "supervisor_reviewer_id", length = 100)
    private String supervisorReviewerId;

    @Column(name = "supervisor_review_outcome", length = 50)
    private String supervisorReviewOutcome;

    @Column(name = "supervisor_review_comments", columnDefinition = "TEXT")
    private String supervisorReviewComments;

    @Column(name = "county_dispute_filed")
    private Boolean countyDisputeFiled;

    @Column(name = "county_dispute_filed_date")
    private LocalDate countyDisputeFiledDate;

    @Column(name = "county_dispute_outcome", length = 50)
    private String countyDisputeOutcome;

    @Column(name = "county_dispute_resolution_date")
    private LocalDate countyDisputeResolutionDate;

    @Column(name = "county_dispute_comments", columnDefinition = "TEXT")
    private String countyDisputeComments;

    @Column(name = "cdss_review_requested")
    private Boolean cdssReviewRequested;

    @Column(name = "cdss_review_date")
    private LocalDate cdssReviewDate;

    @Column(name = "cdss_review_outcome", length = 50)
    private String cdssReviewOutcome;

    @Column(name = "cdss_review_comments", columnDefinition = "TEXT")
    private String cdssReviewComments;

    @Column(name = "county_review_letter_date")
    private LocalDate countyReviewLetterDate;

    @Column(name = "county_review_letter_sent")
    private Boolean countyReviewLetterSent;

    @Column(name = "training_offered")
    private Boolean trainingOffered;

    @Column(name = "training_due_date")
    private LocalDate trainingDueDate;

    @Column(name = "training_completed")
    private Boolean trainingCompleted;

    @Column(name = "training_completion_date")
    private LocalDate trainingCompletionDate;

    @Column(name = "training_county_entry_date")
    private LocalDate trainingCountyEntryDate;

    @Column(name = "termination_effective_date")
    private LocalDate terminationEffectiveDate;

    @Column(name = "reinstatement_date")
    private LocalDate reinstatementDate;

    @Column(name = "county_review_task_id")
    private Long countyReviewTaskId;

    @Column(name = "supervisor_review_task_id")
    private Long supervisorReviewTaskId;

    @Column(name = "county_dispute_task_id")
    private Long countyDisputeTaskId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public OvertimeViolationEntity() {}

    private OvertimeViolationEntity(Builder builder) {
        this.id = builder.id;
        this.providerId = builder.providerId;
        this.violationNumber = builder.violationNumber;
        this.violationType = builder.violationType;
        this.serviceMonth = builder.serviceMonth;
        this.serviceYear = builder.serviceYear;
        this.violationDate = builder.violationDate;
        this.hoursClaimed = builder.hoursClaimed;
        this.maximumAllowed = builder.maximumAllowed;
        this.hoursExceeded = builder.hoursExceeded;
        this.status = builder.status;
        this.countyReviewDate = builder.countyReviewDate;
        this.countyReviewerId = builder.countyReviewerId;
        this.countyReviewOutcome = builder.countyReviewOutcome;
        this.countyReviewComments = builder.countyReviewComments;
        this.supervisorReviewDate = builder.supervisorReviewDate;
        this.supervisorReviewerId = builder.supervisorReviewerId;
        this.supervisorReviewOutcome = builder.supervisorReviewOutcome;
        this.supervisorReviewComments = builder.supervisorReviewComments;
        this.countyDisputeFiled = builder.countyDisputeFiled;
        this.countyDisputeFiledDate = builder.countyDisputeFiledDate;
        this.countyDisputeOutcome = builder.countyDisputeOutcome;
        this.countyDisputeResolutionDate = builder.countyDisputeResolutionDate;
        this.countyDisputeComments = builder.countyDisputeComments;
        this.cdssReviewRequested = builder.cdssReviewRequested;
        this.cdssReviewDate = builder.cdssReviewDate;
        this.cdssReviewOutcome = builder.cdssReviewOutcome;
        this.cdssReviewComments = builder.cdssReviewComments;
        this.countyReviewLetterDate = builder.countyReviewLetterDate;
        this.countyReviewLetterSent = builder.countyReviewLetterSent;
        this.trainingOffered = builder.trainingOffered;
        this.trainingDueDate = builder.trainingDueDate;
        this.trainingCompleted = builder.trainingCompleted;
        this.trainingCompletionDate = builder.trainingCompletionDate;
        this.trainingCountyEntryDate = builder.trainingCountyEntryDate;
        this.terminationEffectiveDate = builder.terminationEffectiveDate;
        this.reinstatementDate = builder.reinstatementDate;
        this.countyReviewTaskId = builder.countyReviewTaskId;
        this.supervisorReviewTaskId = builder.supervisorReviewTaskId;
        this.countyDisputeTaskId = builder.countyDisputeTaskId;
        this.createdAt = builder.createdAt;
        this.createdBy = builder.createdBy;
        this.updatedAt = builder.updatedAt;
        this.updatedBy = builder.updatedBy;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = ViolationStatus.PENDING_REVIEW;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ViolationStatus {
        PENDING_REVIEW, ACTIVE, INACTIVE, INACTIVE_NO_VIOLATIONS_ONE_YEAR,
        INACTIVE_PROVIDER_ONE_YEAR_TERMINATION, INACTIVE_EXEMPTION
    }

    public int getSuspensionDays() {
        if (violationNumber == null) return 0;
        switch (violationNumber) {
            case 3: return 90;
            case 4: return 365;
            default: return 0;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Integer getViolationNumber() { return violationNumber; }
    public void setViolationNumber(Integer violationNumber) { this.violationNumber = violationNumber; }

    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }

    public Integer getServiceMonth() { return serviceMonth; }
    public void setServiceMonth(Integer serviceMonth) { this.serviceMonth = serviceMonth; }

    public Integer getServiceYear() { return serviceYear; }
    public void setServiceYear(Integer serviceYear) { this.serviceYear = serviceYear; }

    public LocalDate getViolationDate() { return violationDate; }
    public void setViolationDate(LocalDate violationDate) { this.violationDate = violationDate; }

    public Double getHoursClaimed() { return hoursClaimed; }
    public void setHoursClaimed(Double hoursClaimed) { this.hoursClaimed = hoursClaimed; }

    public Double getMaximumAllowed() { return maximumAllowed; }
    public void setMaximumAllowed(Double maximumAllowed) { this.maximumAllowed = maximumAllowed; }

    public Double getHoursExceeded() { return hoursExceeded; }
    public void setHoursExceeded(Double hoursExceeded) { this.hoursExceeded = hoursExceeded; }

    public ViolationStatus getStatus() { return status; }
    public void setStatus(ViolationStatus status) { this.status = status; }

    public LocalDate getCountyReviewDate() { return countyReviewDate; }
    public void setCountyReviewDate(LocalDate countyReviewDate) { this.countyReviewDate = countyReviewDate; }

    public String getCountyReviewerId() { return countyReviewerId; }
    public void setCountyReviewerId(String countyReviewerId) { this.countyReviewerId = countyReviewerId; }

    public String getCountyReviewOutcome() { return countyReviewOutcome; }
    public void setCountyReviewOutcome(String countyReviewOutcome) { this.countyReviewOutcome = countyReviewOutcome; }

    public String getCountyReviewComments() { return countyReviewComments; }
    public void setCountyReviewComments(String countyReviewComments) { this.countyReviewComments = countyReviewComments; }

    public LocalDate getSupervisorReviewDate() { return supervisorReviewDate; }
    public void setSupervisorReviewDate(LocalDate supervisorReviewDate) { this.supervisorReviewDate = supervisorReviewDate; }

    public String getSupervisorReviewerId() { return supervisorReviewerId; }
    public void setSupervisorReviewerId(String supervisorReviewerId) { this.supervisorReviewerId = supervisorReviewerId; }

    public String getSupervisorReviewOutcome() { return supervisorReviewOutcome; }
    public void setSupervisorReviewOutcome(String supervisorReviewOutcome) { this.supervisorReviewOutcome = supervisorReviewOutcome; }

    public String getSupervisorReviewComments() { return supervisorReviewComments; }
    public void setSupervisorReviewComments(String supervisorReviewComments) { this.supervisorReviewComments = supervisorReviewComments; }

    public Boolean getCountyDisputeFiled() { return countyDisputeFiled; }
    public void setCountyDisputeFiled(Boolean countyDisputeFiled) { this.countyDisputeFiled = countyDisputeFiled; }

    public LocalDate getCountyDisputeFiledDate() { return countyDisputeFiledDate; }
    public void setCountyDisputeFiledDate(LocalDate countyDisputeFiledDate) { this.countyDisputeFiledDate = countyDisputeFiledDate; }

    public String getCountyDisputeOutcome() { return countyDisputeOutcome; }
    public void setCountyDisputeOutcome(String countyDisputeOutcome) { this.countyDisputeOutcome = countyDisputeOutcome; }

    public LocalDate getCountyDisputeResolutionDate() { return countyDisputeResolutionDate; }
    public void setCountyDisputeResolutionDate(LocalDate countyDisputeResolutionDate) { this.countyDisputeResolutionDate = countyDisputeResolutionDate; }

    public String getCountyDisputeComments() { return countyDisputeComments; }
    public void setCountyDisputeComments(String countyDisputeComments) { this.countyDisputeComments = countyDisputeComments; }

    public Boolean getCdssReviewRequested() { return cdssReviewRequested; }
    public void setCdssReviewRequested(Boolean cdssReviewRequested) { this.cdssReviewRequested = cdssReviewRequested; }

    public LocalDate getCdssReviewDate() { return cdssReviewDate; }
    public void setCdssReviewDate(LocalDate cdssReviewDate) { this.cdssReviewDate = cdssReviewDate; }

    public String getCdssReviewOutcome() { return cdssReviewOutcome; }
    public void setCdssReviewOutcome(String cdssReviewOutcome) { this.cdssReviewOutcome = cdssReviewOutcome; }

    public String getCdssReviewComments() { return cdssReviewComments; }
    public void setCdssReviewComments(String cdssReviewComments) { this.cdssReviewComments = cdssReviewComments; }

    public LocalDate getCountyReviewLetterDate() { return countyReviewLetterDate; }
    public void setCountyReviewLetterDate(LocalDate countyReviewLetterDate) { this.countyReviewLetterDate = countyReviewLetterDate; }

    public Boolean getCountyReviewLetterSent() { return countyReviewLetterSent; }
    public void setCountyReviewLetterSent(Boolean countyReviewLetterSent) { this.countyReviewLetterSent = countyReviewLetterSent; }

    public Boolean getTrainingOffered() { return trainingOffered; }
    public void setTrainingOffered(Boolean trainingOffered) { this.trainingOffered = trainingOffered; }

    public LocalDate getTrainingDueDate() { return trainingDueDate; }
    public void setTrainingDueDate(LocalDate trainingDueDate) { this.trainingDueDate = trainingDueDate; }

    public Boolean getTrainingCompleted() { return trainingCompleted; }
    public void setTrainingCompleted(Boolean trainingCompleted) { this.trainingCompleted = trainingCompleted; }

    public LocalDate getTrainingCompletionDate() { return trainingCompletionDate; }
    public void setTrainingCompletionDate(LocalDate trainingCompletionDate) { this.trainingCompletionDate = trainingCompletionDate; }

    public LocalDate getTrainingCountyEntryDate() { return trainingCountyEntryDate; }
    public void setTrainingCountyEntryDate(LocalDate trainingCountyEntryDate) { this.trainingCountyEntryDate = trainingCountyEntryDate; }

    public LocalDate getTerminationEffectiveDate() { return terminationEffectiveDate; }
    public void setTerminationEffectiveDate(LocalDate terminationEffectiveDate) { this.terminationEffectiveDate = terminationEffectiveDate; }

    public LocalDate getReinstatementDate() { return reinstatementDate; }
    public void setReinstatementDate(LocalDate reinstatementDate) { this.reinstatementDate = reinstatementDate; }

    public Long getCountyReviewTaskId() { return countyReviewTaskId; }
    public void setCountyReviewTaskId(Long countyReviewTaskId) { this.countyReviewTaskId = countyReviewTaskId; }

    public Long getSupervisorReviewTaskId() { return supervisorReviewTaskId; }
    public void setSupervisorReviewTaskId(Long supervisorReviewTaskId) { this.supervisorReviewTaskId = supervisorReviewTaskId; }

    public Long getCountyDisputeTaskId() { return countyDisputeTaskId; }
    public void setCountyDisputeTaskId(Long countyDisputeTaskId) { this.countyDisputeTaskId = countyDisputeTaskId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long providerId;
        private Integer violationNumber;
        private String violationType;
        private Integer serviceMonth;
        private Integer serviceYear;
        private LocalDate violationDate;
        private Double hoursClaimed;
        private Double maximumAllowed;
        private Double hoursExceeded;
        private ViolationStatus status;
        private LocalDate countyReviewDate;
        private String countyReviewerId;
        private String countyReviewOutcome;
        private String countyReviewComments;
        private LocalDate supervisorReviewDate;
        private String supervisorReviewerId;
        private String supervisorReviewOutcome;
        private String supervisorReviewComments;
        private Boolean countyDisputeFiled;
        private LocalDate countyDisputeFiledDate;
        private String countyDisputeOutcome;
        private LocalDate countyDisputeResolutionDate;
        private String countyDisputeComments;
        private Boolean cdssReviewRequested;
        private LocalDate cdssReviewDate;
        private String cdssReviewOutcome;
        private String cdssReviewComments;
        private LocalDate countyReviewLetterDate;
        private Boolean countyReviewLetterSent;
        private Boolean trainingOffered;
        private LocalDate trainingDueDate;
        private Boolean trainingCompleted;
        private LocalDate trainingCompletionDate;
        private LocalDate trainingCountyEntryDate;
        private LocalDate terminationEffectiveDate;
        private LocalDate reinstatementDate;
        private Long countyReviewTaskId;
        private Long supervisorReviewTaskId;
        private Long countyDisputeTaskId;
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder providerId(Long providerId) { this.providerId = providerId; return this; }
        public Builder violationNumber(Integer violationNumber) { this.violationNumber = violationNumber; return this; }
        public Builder violationType(String violationType) { this.violationType = violationType; return this; }
        public Builder serviceMonth(Integer serviceMonth) { this.serviceMonth = serviceMonth; return this; }
        public Builder serviceYear(Integer serviceYear) { this.serviceYear = serviceYear; return this; }
        public Builder violationDate(LocalDate violationDate) { this.violationDate = violationDate; return this; }
        public Builder hoursClaimed(Double hoursClaimed) { this.hoursClaimed = hoursClaimed; return this; }
        public Builder maximumAllowed(Double maximumAllowed) { this.maximumAllowed = maximumAllowed; return this; }
        public Builder hoursExceeded(Double hoursExceeded) { this.hoursExceeded = hoursExceeded; return this; }
        public Builder status(ViolationStatus status) { this.status = status; return this; }
        public Builder countyReviewDate(LocalDate countyReviewDate) { this.countyReviewDate = countyReviewDate; return this; }
        public Builder countyReviewerId(String countyReviewerId) { this.countyReviewerId = countyReviewerId; return this; }
        public Builder countyReviewOutcome(String countyReviewOutcome) { this.countyReviewOutcome = countyReviewOutcome; return this; }
        public Builder countyReviewComments(String countyReviewComments) { this.countyReviewComments = countyReviewComments; return this; }
        public Builder supervisorReviewDate(LocalDate supervisorReviewDate) { this.supervisorReviewDate = supervisorReviewDate; return this; }
        public Builder supervisorReviewerId(String supervisorReviewerId) { this.supervisorReviewerId = supervisorReviewerId; return this; }
        public Builder supervisorReviewOutcome(String supervisorReviewOutcome) { this.supervisorReviewOutcome = supervisorReviewOutcome; return this; }
        public Builder supervisorReviewComments(String supervisorReviewComments) { this.supervisorReviewComments = supervisorReviewComments; return this; }
        public Builder countyDisputeFiled(Boolean countyDisputeFiled) { this.countyDisputeFiled = countyDisputeFiled; return this; }
        public Builder countyDisputeFiledDate(LocalDate countyDisputeFiledDate) { this.countyDisputeFiledDate = countyDisputeFiledDate; return this; }
        public Builder countyDisputeOutcome(String countyDisputeOutcome) { this.countyDisputeOutcome = countyDisputeOutcome; return this; }
        public Builder countyDisputeResolutionDate(LocalDate countyDisputeResolutionDate) { this.countyDisputeResolutionDate = countyDisputeResolutionDate; return this; }
        public Builder countyDisputeComments(String countyDisputeComments) { this.countyDisputeComments = countyDisputeComments; return this; }
        public Builder cdssReviewRequested(Boolean cdssReviewRequested) { this.cdssReviewRequested = cdssReviewRequested; return this; }
        public Builder cdssReviewDate(LocalDate cdssReviewDate) { this.cdssReviewDate = cdssReviewDate; return this; }
        public Builder cdssReviewOutcome(String cdssReviewOutcome) { this.cdssReviewOutcome = cdssReviewOutcome; return this; }
        public Builder cdssReviewComments(String cdssReviewComments) { this.cdssReviewComments = cdssReviewComments; return this; }
        public Builder countyReviewLetterDate(LocalDate countyReviewLetterDate) { this.countyReviewLetterDate = countyReviewLetterDate; return this; }
        public Builder countyReviewLetterSent(Boolean countyReviewLetterSent) { this.countyReviewLetterSent = countyReviewLetterSent; return this; }
        public Builder trainingOffered(Boolean trainingOffered) { this.trainingOffered = trainingOffered; return this; }
        public Builder trainingDueDate(LocalDate trainingDueDate) { this.trainingDueDate = trainingDueDate; return this; }
        public Builder trainingCompleted(Boolean trainingCompleted) { this.trainingCompleted = trainingCompleted; return this; }
        public Builder trainingCompletionDate(LocalDate trainingCompletionDate) { this.trainingCompletionDate = trainingCompletionDate; return this; }
        public Builder trainingCountyEntryDate(LocalDate trainingCountyEntryDate) { this.trainingCountyEntryDate = trainingCountyEntryDate; return this; }
        public Builder terminationEffectiveDate(LocalDate terminationEffectiveDate) { this.terminationEffectiveDate = terminationEffectiveDate; return this; }
        public Builder reinstatementDate(LocalDate reinstatementDate) { this.reinstatementDate = reinstatementDate; return this; }
        public Builder countyReviewTaskId(Long countyReviewTaskId) { this.countyReviewTaskId = countyReviewTaskId; return this; }
        public Builder supervisorReviewTaskId(Long supervisorReviewTaskId) { this.supervisorReviewTaskId = supervisorReviewTaskId; return this; }
        public Builder countyDisputeTaskId(Long countyDisputeTaskId) { this.countyDisputeTaskId = countyDisputeTaskId; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }

        public OvertimeViolationEntity build() { return new OvertimeViolationEntity(this); }
    }
}
