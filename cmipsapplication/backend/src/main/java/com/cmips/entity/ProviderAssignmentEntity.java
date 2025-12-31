package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provider Assignment Entity - Links Providers to Recipient Cases
 * Based on DSD Section 23 - Provider Assignment User Stories
 */
@Entity
@Table(name = "provider_assignments")
public class ProviderAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "provider_type", length = 20)
    private String providerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssignmentStatus status;

    @Column(name = "begin_date")
    private LocalDate beginDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "leave_termination_effective_date")
    private LocalDate leaveTerminationEffectiveDate;

    @Column(name = "termination_reason", length = 500)
    private String terminationReason;

    @Column(name = "authorized_hours_monthly")
    private Double authorizedHoursMonthly;

    @Column(name = "authorized_hours_weekly")
    private Double authorizedHoursWeekly;

    @Column(name = "assigned_hours_form_indicated")
    private Boolean assignedHoursFormIndicated;

    @Column(name = "assigned_hours")
    private Double assignedHours;

    @Column(name = "pay_rate")
    private Double payRate;

    @Column(name = "pay_rate_effective_date")
    private LocalDate payRateEffectiveDate;

    @Column(name = "wpcs_hours")
    private Double wpcsHours;

    @Column(name = "is_backup_provider")
    private Boolean isBackupProvider;

    @Column(name = "backup_hours_used")
    private Double backupHoursUsed;

    @Column(name = "relationship_to_recipient", length = 50)
    private String relationshipToRecipient;

    @Column(name = "impacts_funding_source")
    private Boolean impactsFundingSource;

    @Column(name = "has_workweek_agreement")
    private Boolean hasWorkweekAgreement;

    @Column(name = "workweek_agreement_id")
    private Long workweekAgreementId;

    @Column(name = "has_travel_time_agreement")
    private Boolean hasTravelTimeAgreement;

    @Column(name = "travel_time_hours_weekly")
    private Double travelTimeHoursWeekly;

    @Column(name = "traveling_from_recipient_id")
    private Long travelingFromRecipientId;

    @Column(name = "is_electronic_timesheet")
    private Boolean isElectronicTimesheet;

    @Column(name = "evv_effective_date")
    private LocalDate evvEffectiveDate;

    @Column(name = "has_recipient_waiver")
    private Boolean hasRecipientWaiver;

    @Column(name = "recipient_waiver_begin_date")
    private LocalDate recipientWaiverBeginDate;

    @Column(name = "recipient_waiver_end_date")
    private LocalDate recipientWaiverEndDate;

    @Column(name = "recipient_waiver_cori_id")
    private Long recipientWaiverCoriId;

    @Column(name = "initial_notification_sent")
    private Boolean initialNotificationSent;

    @Column(name = "notification_sent_date")
    private LocalDate notificationSentDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public ProviderAssignmentEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = AssignmentStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDate getBeginDate() { return beginDate; }
    public void setBeginDate(LocalDate beginDate) { this.beginDate = beginDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getLeaveTerminationEffectiveDate() { return leaveTerminationEffectiveDate; }
    public void setLeaveTerminationEffectiveDate(LocalDate leaveTerminationEffectiveDate) { this.leaveTerminationEffectiveDate = leaveTerminationEffectiveDate; }

    public String getTerminationReason() { return terminationReason; }
    public void setTerminationReason(String terminationReason) { this.terminationReason = terminationReason; }

    public Double getAuthorizedHoursMonthly() { return authorizedHoursMonthly; }
    public void setAuthorizedHoursMonthly(Double authorizedHoursMonthly) { this.authorizedHoursMonthly = authorizedHoursMonthly; }

    public Double getAuthorizedHoursWeekly() { return authorizedHoursWeekly; }
    public void setAuthorizedHoursWeekly(Double authorizedHoursWeekly) { this.authorizedHoursWeekly = authorizedHoursWeekly; }

    public Boolean getAssignedHoursFormIndicated() { return assignedHoursFormIndicated; }
    public void setAssignedHoursFormIndicated(Boolean assignedHoursFormIndicated) { this.assignedHoursFormIndicated = assignedHoursFormIndicated; }

    public Double getAssignedHours() { return assignedHours; }
    public void setAssignedHours(Double assignedHours) { this.assignedHours = assignedHours; }

    public Double getPayRate() { return payRate; }
    public void setPayRate(Double payRate) { this.payRate = payRate; }

    public LocalDate getPayRateEffectiveDate() { return payRateEffectiveDate; }
    public void setPayRateEffectiveDate(LocalDate payRateEffectiveDate) { this.payRateEffectiveDate = payRateEffectiveDate; }

    public Double getWpcsHours() { return wpcsHours; }
    public void setWpcsHours(Double wpcsHours) { this.wpcsHours = wpcsHours; }

    public Boolean getIsBackupProvider() { return isBackupProvider; }
    public void setIsBackupProvider(Boolean isBackupProvider) { this.isBackupProvider = isBackupProvider; }

    public Double getBackupHoursUsed() { return backupHoursUsed; }
    public void setBackupHoursUsed(Double backupHoursUsed) { this.backupHoursUsed = backupHoursUsed; }

    public String getRelationshipToRecipient() { return relationshipToRecipient; }
    public void setRelationshipToRecipient(String relationshipToRecipient) { this.relationshipToRecipient = relationshipToRecipient; }

    public Boolean getImpactsFundingSource() { return impactsFundingSource; }
    public void setImpactsFundingSource(Boolean impactsFundingSource) { this.impactsFundingSource = impactsFundingSource; }

    public Boolean getHasWorkweekAgreement() { return hasWorkweekAgreement; }
    public void setHasWorkweekAgreement(Boolean hasWorkweekAgreement) { this.hasWorkweekAgreement = hasWorkweekAgreement; }

    public Long getWorkweekAgreementId() { return workweekAgreementId; }
    public void setWorkweekAgreementId(Long workweekAgreementId) { this.workweekAgreementId = workweekAgreementId; }

    public Boolean getHasTravelTimeAgreement() { return hasTravelTimeAgreement; }
    public void setHasTravelTimeAgreement(Boolean hasTravelTimeAgreement) { this.hasTravelTimeAgreement = hasTravelTimeAgreement; }

    public Double getTravelTimeHoursWeekly() { return travelTimeHoursWeekly; }
    public void setTravelTimeHoursWeekly(Double travelTimeHoursWeekly) { this.travelTimeHoursWeekly = travelTimeHoursWeekly; }

    public Long getTravelingFromRecipientId() { return travelingFromRecipientId; }
    public void setTravelingFromRecipientId(Long travelingFromRecipientId) { this.travelingFromRecipientId = travelingFromRecipientId; }

    public Boolean getIsElectronicTimesheet() { return isElectronicTimesheet; }
    public void setIsElectronicTimesheet(Boolean isElectronicTimesheet) { this.isElectronicTimesheet = isElectronicTimesheet; }

    public LocalDate getEvvEffectiveDate() { return evvEffectiveDate; }
    public void setEvvEffectiveDate(LocalDate evvEffectiveDate) { this.evvEffectiveDate = evvEffectiveDate; }

    public Boolean getHasRecipientWaiver() { return hasRecipientWaiver; }
    public void setHasRecipientWaiver(Boolean hasRecipientWaiver) { this.hasRecipientWaiver = hasRecipientWaiver; }

    public LocalDate getRecipientWaiverBeginDate() { return recipientWaiverBeginDate; }
    public void setRecipientWaiverBeginDate(LocalDate recipientWaiverBeginDate) { this.recipientWaiverBeginDate = recipientWaiverBeginDate; }

    public LocalDate getRecipientWaiverEndDate() { return recipientWaiverEndDate; }
    public void setRecipientWaiverEndDate(LocalDate recipientWaiverEndDate) { this.recipientWaiverEndDate = recipientWaiverEndDate; }

    public Long getRecipientWaiverCoriId() { return recipientWaiverCoriId; }
    public void setRecipientWaiverCoriId(Long recipientWaiverCoriId) { this.recipientWaiverCoriId = recipientWaiverCoriId; }

    public Boolean getInitialNotificationSent() { return initialNotificationSent; }
    public void setInitialNotificationSent(Boolean initialNotificationSent) { this.initialNotificationSent = initialNotificationSent; }

    public LocalDate getNotificationSentDate() { return notificationSentDate; }
    public void setNotificationSentDate(LocalDate notificationSentDate) { this.notificationSentDate = notificationSentDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // Assignment Status Enum
    public enum AssignmentStatus {
        ACTIVE,
        ON_LEAVE,
        TERMINATED
    }

    // Check if this provider assignment impacts funding source (spouse or parent of minor)
    public boolean checkFundingSourceImpact() {
        return "SPOUSE".equals(relationshipToRecipient) ||
               "PARENT_OF_MINOR".equals(relationshipToRecipient);
    }

    // Check if recipient waiver is active
    public boolean isRecipientWaiverActive() {
        if (!Boolean.TRUE.equals(hasRecipientWaiver)) return false;
        LocalDate today = LocalDate.now();
        if (recipientWaiverBeginDate != null && recipientWaiverBeginDate.isAfter(today)) return false;
        if (recipientWaiverEndDate != null && recipientWaiverEndDate.isBefore(today)) return false;
        return true;
    }

    // Builder
    public static ProviderAssignmentEntityBuilder builder() { return new ProviderAssignmentEntityBuilder(); }

    public static class ProviderAssignmentEntityBuilder {
        private Long id;
        private Long caseId;
        private Long providerId;
        private Long recipientId;
        private String providerType;
        private AssignmentStatus status;
        private LocalDate beginDate;
        private LocalDate endDate;
        private LocalDate leaveTerminationEffectiveDate;
        private String terminationReason;
        private Double authorizedHoursMonthly;
        private Double authorizedHoursWeekly;
        private Boolean assignedHoursFormIndicated;
        private Double assignedHours;
        private Double payRate;
        private LocalDate payRateEffectiveDate;
        private Double wpcsHours;
        private Boolean isBackupProvider;
        private Double backupHoursUsed;
        private String relationshipToRecipient;
        private Boolean impactsFundingSource;
        private Boolean hasWorkweekAgreement;
        private Long workweekAgreementId;
        private Boolean hasTravelTimeAgreement;
        private Double travelTimeHoursWeekly;
        private Long travelingFromRecipientId;
        private Boolean isElectronicTimesheet;
        private LocalDate evvEffectiveDate;
        private Boolean hasRecipientWaiver;
        private LocalDate recipientWaiverBeginDate;
        private LocalDate recipientWaiverEndDate;
        private Long recipientWaiverCoriId;
        private Boolean initialNotificationSent;
        private LocalDate notificationSentDate;
        private String createdBy;
        private String updatedBy;

        public ProviderAssignmentEntityBuilder id(Long id) { this.id = id; return this; }
        public ProviderAssignmentEntityBuilder caseId(Long caseId) { this.caseId = caseId; return this; }
        public ProviderAssignmentEntityBuilder providerId(Long providerId) { this.providerId = providerId; return this; }
        public ProviderAssignmentEntityBuilder recipientId(Long recipientId) { this.recipientId = recipientId; return this; }
        public ProviderAssignmentEntityBuilder providerType(String providerType) { this.providerType = providerType; return this; }
        public ProviderAssignmentEntityBuilder status(AssignmentStatus status) { this.status = status; return this; }
        public ProviderAssignmentEntityBuilder beginDate(LocalDate beginDate) { this.beginDate = beginDate; return this; }
        public ProviderAssignmentEntityBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public ProviderAssignmentEntityBuilder leaveTerminationEffectiveDate(LocalDate leaveTerminationEffectiveDate) { this.leaveTerminationEffectiveDate = leaveTerminationEffectiveDate; return this; }
        public ProviderAssignmentEntityBuilder terminationReason(String terminationReason) { this.terminationReason = terminationReason; return this; }
        public ProviderAssignmentEntityBuilder authorizedHoursMonthly(Double authorizedHoursMonthly) { this.authorizedHoursMonthly = authorizedHoursMonthly; return this; }
        public ProviderAssignmentEntityBuilder authorizedHoursWeekly(Double authorizedHoursWeekly) { this.authorizedHoursWeekly = authorizedHoursWeekly; return this; }
        public ProviderAssignmentEntityBuilder assignedHoursFormIndicated(Boolean assignedHoursFormIndicated) { this.assignedHoursFormIndicated = assignedHoursFormIndicated; return this; }
        public ProviderAssignmentEntityBuilder assignedHours(Double assignedHours) { this.assignedHours = assignedHours; return this; }
        public ProviderAssignmentEntityBuilder payRate(Double payRate) { this.payRate = payRate; return this; }
        public ProviderAssignmentEntityBuilder payRateEffectiveDate(LocalDate payRateEffectiveDate) { this.payRateEffectiveDate = payRateEffectiveDate; return this; }
        public ProviderAssignmentEntityBuilder wpcsHours(Double wpcsHours) { this.wpcsHours = wpcsHours; return this; }
        public ProviderAssignmentEntityBuilder isBackupProvider(Boolean isBackupProvider) { this.isBackupProvider = isBackupProvider; return this; }
        public ProviderAssignmentEntityBuilder backupHoursUsed(Double backupHoursUsed) { this.backupHoursUsed = backupHoursUsed; return this; }
        public ProviderAssignmentEntityBuilder relationshipToRecipient(String relationshipToRecipient) { this.relationshipToRecipient = relationshipToRecipient; return this; }
        public ProviderAssignmentEntityBuilder impactsFundingSource(Boolean impactsFundingSource) { this.impactsFundingSource = impactsFundingSource; return this; }
        public ProviderAssignmentEntityBuilder hasWorkweekAgreement(Boolean hasWorkweekAgreement) { this.hasWorkweekAgreement = hasWorkweekAgreement; return this; }
        public ProviderAssignmentEntityBuilder workweekAgreementId(Long workweekAgreementId) { this.workweekAgreementId = workweekAgreementId; return this; }
        public ProviderAssignmentEntityBuilder hasTravelTimeAgreement(Boolean hasTravelTimeAgreement) { this.hasTravelTimeAgreement = hasTravelTimeAgreement; return this; }
        public ProviderAssignmentEntityBuilder travelTimeHoursWeekly(Double travelTimeHoursWeekly) { this.travelTimeHoursWeekly = travelTimeHoursWeekly; return this; }
        public ProviderAssignmentEntityBuilder travelingFromRecipientId(Long travelingFromRecipientId) { this.travelingFromRecipientId = travelingFromRecipientId; return this; }
        public ProviderAssignmentEntityBuilder isElectronicTimesheet(Boolean isElectronicTimesheet) { this.isElectronicTimesheet = isElectronicTimesheet; return this; }
        public ProviderAssignmentEntityBuilder evvEffectiveDate(LocalDate evvEffectiveDate) { this.evvEffectiveDate = evvEffectiveDate; return this; }
        public ProviderAssignmentEntityBuilder hasRecipientWaiver(Boolean hasRecipientWaiver) { this.hasRecipientWaiver = hasRecipientWaiver; return this; }
        public ProviderAssignmentEntityBuilder recipientWaiverBeginDate(LocalDate recipientWaiverBeginDate) { this.recipientWaiverBeginDate = recipientWaiverBeginDate; return this; }
        public ProviderAssignmentEntityBuilder recipientWaiverEndDate(LocalDate recipientWaiverEndDate) { this.recipientWaiverEndDate = recipientWaiverEndDate; return this; }
        public ProviderAssignmentEntityBuilder recipientWaiverCoriId(Long recipientWaiverCoriId) { this.recipientWaiverCoriId = recipientWaiverCoriId; return this; }
        public ProviderAssignmentEntityBuilder initialNotificationSent(Boolean initialNotificationSent) { this.initialNotificationSent = initialNotificationSent; return this; }
        public ProviderAssignmentEntityBuilder notificationSentDate(LocalDate notificationSentDate) { this.notificationSentDate = notificationSentDate; return this; }
        public ProviderAssignmentEntityBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public ProviderAssignmentEntityBuilder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }

        public ProviderAssignmentEntity build() {
            ProviderAssignmentEntity entity = new ProviderAssignmentEntity();
            entity.id = this.id;
            entity.caseId = this.caseId;
            entity.providerId = this.providerId;
            entity.recipientId = this.recipientId;
            entity.providerType = this.providerType;
            entity.status = this.status;
            entity.beginDate = this.beginDate;
            entity.endDate = this.endDate;
            entity.leaveTerminationEffectiveDate = this.leaveTerminationEffectiveDate;
            entity.terminationReason = this.terminationReason;
            entity.authorizedHoursMonthly = this.authorizedHoursMonthly;
            entity.authorizedHoursWeekly = this.authorizedHoursWeekly;
            entity.assignedHoursFormIndicated = this.assignedHoursFormIndicated;
            entity.assignedHours = this.assignedHours;
            entity.payRate = this.payRate;
            entity.payRateEffectiveDate = this.payRateEffectiveDate;
            entity.wpcsHours = this.wpcsHours;
            entity.isBackupProvider = this.isBackupProvider;
            entity.backupHoursUsed = this.backupHoursUsed;
            entity.relationshipToRecipient = this.relationshipToRecipient;
            entity.impactsFundingSource = this.impactsFundingSource;
            entity.hasWorkweekAgreement = this.hasWorkweekAgreement;
            entity.workweekAgreementId = this.workweekAgreementId;
            entity.hasTravelTimeAgreement = this.hasTravelTimeAgreement;
            entity.travelTimeHoursWeekly = this.travelTimeHoursWeekly;
            entity.travelingFromRecipientId = this.travelingFromRecipientId;
            entity.isElectronicTimesheet = this.isElectronicTimesheet;
            entity.evvEffectiveDate = this.evvEffectiveDate;
            entity.hasRecipientWaiver = this.hasRecipientWaiver;
            entity.recipientWaiverBeginDate = this.recipientWaiverBeginDate;
            entity.recipientWaiverEndDate = this.recipientWaiverEndDate;
            entity.recipientWaiverCoriId = this.recipientWaiverCoriId;
            entity.initialNotificationSent = this.initialNotificationSent;
            entity.notificationSentDate = this.notificationSentDate;
            entity.createdBy = this.createdBy;
            entity.updatedBy = this.updatedBy;
            return entity;
        }
    }
}
