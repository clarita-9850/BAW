package com.cmips.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Application Entity - Formal request for IHSS services
 * Based on DSD Section 20 - Intake Application Processing
 *
 * An application is created when a person formally requests IHSS services.
 * Applications must be processed within 45 days per federal requirements.
 */
@Entity
@Table(name = "applications")
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Application Number (unique identifier)
    @Column(name = "application_number", unique = true, nullable = false, length = 50)
    private String applicationNumber;

    // Link to referral (if converted from referral)
    @Column(name = "referral_id")
    private String referralId;

    // Link to recipient
    @Column(name = "recipient_id")
    private Long recipientId;

    // Application Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    // Program Type
    @Enumerated(EnumType.STRING)
    @Column(name = "program_type")
    private ProgramType programType = ProgramType.IHSS;

    // ==================== KEY DATES ====================

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate; // Day 1 of 45-day clock

    @Column(name = "deadline_date", nullable = false)
    private LocalDate deadlineDate; // Day 45

    @Column(name = "decision_date")
    private LocalDate decisionDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate; // When services start

    // Extension
    @Column(name = "extension_date")
    private LocalDate extensionDate;

    @Column(name = "extension_reason", length = 500)
    private String extensionReason;

    @Column(name = "extended_deadline_date")
    private LocalDate extendedDeadlineDate;

    // ==================== CIN CLEARANCE ====================

    @Column(name = "cin", length = 20)
    private String cin; // Client Index Number (format: 12345678A)

    @Enumerated(EnumType.STRING)
    @Column(name = "cin_clearance_status")
    private CINClearanceStatus cinClearanceStatus;

    @Column(name = "cin_clearance_date")
    private LocalDateTime cinClearanceDate;

    @Column(name = "cin_match_type", length = 50)
    private String cinMatchType; // EXACT, POSSIBLE, NEW

    @Column(name = "cin_mismatch_details", length = 500)
    private String cinMismatchDetails;

    // ==================== MEDS INTEGRATION ====================

    @Column(name = "medi_cal_aid_code", length = 20)
    private String mediCalAidCode;

    @Column(name = "medi_cal_status", length = 50)
    private String mediCalStatus; // ACTIVE, INACTIVE, PENDING

    @Column(name = "medi_cal_effective_date")
    private LocalDate mediCalEffectiveDate;

    @Column(name = "medi_cal_end_date")
    private LocalDate mediCalEndDate;

    @Column(name = "share_of_cost_amount")
    private BigDecimal shareOfCostAmount;

    @Column(name = "meds_verification_date")
    private LocalDateTime medsVerificationDate;

    // ==================== ELIGIBILITY FACTORS ====================

    // Citizenship/Residency
    @Column(name = "citizenship_status", length = 50)
    private String citizenshipStatus;

    @Column(name = "california_resident")
    private Boolean californiaResident;

    @Column(name = "county_residence_verified")
    private Boolean countyResidenceVerified;

    // Age/Disability
    @Column(name = "age_at_application")
    private Integer ageAtApplication;

    @Column(name = "meets_age_requirement")
    private Boolean meetsAgeRequirement; // 65+ or disabled

    @Column(name = "disability_status", length = 100)
    private String disabilityStatus;

    // Living Arrangement
    @Column(name = "living_arrangement", length = 100)
    private String livingArrangement; // OWN_HOME, WITH_FAMILY, FACILITY, etc.

    @Column(name = "safe_living_environment")
    private Boolean safeLivingEnvironment;

    // ==================== FINANCIAL INFORMATION ====================

    @Column(name = "has_income")
    private Boolean hasIncome;

    @Column(name = "monthly_income")
    private BigDecimal monthlyIncome;

    @Column(name = "income_source", length = 200)
    private String incomeSource;

    @Column(name = "has_resources")
    private Boolean hasResources;

    @Column(name = "total_resources")
    private BigDecimal totalResources;

    // ==================== DECISION INFORMATION ====================

    @Column(name = "denial_reason", length = 500)
    private String denialReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "denial_code")
    private DenialCode denialCode;

    @Column(name = "withdrawal_reason", length = 500)
    private String withdrawalReason;

    @Column(name = "withdrawal_date")
    private LocalDate withdrawalDate;

    // ==================== CASE CREATION ====================

    @Column(name = "case_created")
    private Boolean caseCreated = false;

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "case_number", length = 50)
    private String caseNumber;

    @Column(name = "case_creation_date")
    private LocalDate caseCreationDate;

    // ==================== ASSIGNMENT ====================

    @Column(name = "assigned_worker_id", length = 100)
    private String assignedWorkerId;

    @Column(name = "assigned_worker_name", length = 200)
    private String assignedWorkerName;

    @Column(name = "supervisor_id", length = 100)
    private String supervisorId;

    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "county_name", length = 100)
    private String countyName;

    // ==================== ASSESSMENT SCHEDULING ====================

    @Column(name = "assessment_scheduled")
    private Boolean assessmentScheduled = false;

    @Column(name = "assessment_date")
    private LocalDate assessmentDate;

    @Column(name = "assessment_type", length = 50)
    private String assessmentType; // HOME_VISIT, PHONE, VIDEO

    @Column(name = "assessment_completed")
    private Boolean assessmentCompleted = false;

    @Column(name = "assessment_completion_date")
    private LocalDate assessmentCompletionDate;

    // ==================== DOCUMENTATION ====================

    @Column(name = "soc_873_received")
    private Boolean soc873Received = false; // Application form

    @Column(name = "soc_873_date")
    private LocalDate soc873Date;

    @Column(name = "medical_certification_received")
    private Boolean medicalCertificationReceived = false;

    @Column(name = "medical_certification_date")
    private LocalDate medicalCertificationDate;

    @Column(name = "required_docs_complete")
    private Boolean requiredDocsComplete = false;

    @Column(name = "missing_documents", length = 500)
    private String missingDocuments;

    // ==================== NOTES ====================

    @Column(name = "application_notes", columnDefinition = "TEXT")
    private String applicationNotes;

    @Column(name = "processing_notes", columnDefinition = "TEXT")
    private String processingNotes;

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

    public enum ApplicationStatus {
        PENDING,            // Initial status
        IN_PROGRESS,        // Being processed
        ASSESSMENT_SCHEDULED,
        ASSESSMENT_COMPLETED,
        PENDING_DOCUMENTATION,
        PENDING_MEDI_CAL,
        APPROVED,           // Application approved
        DENIED,             // Application denied
        WITHDRAWN,          // Applicant withdrew
        TRANSFERRED         // Transferred to another county
    }

    public enum ProgramType {
        IHSS,       // In-Home Supportive Services
        WPCS,       // Waiver Personal Care Services
        IHSS_WPCS,  // Both programs
        PCSP        // Personal Care Services Program
    }

    public enum CINClearanceStatus {
        NOT_STARTED,
        IN_PROGRESS,
        EXACT_MATCH,
        POSSIBLE_MATCHES,
        NEW_CIN_CREATED,
        MISMATCH_REVIEW,
        CLEARED,
        FAILED
    }

    public enum DenialCode {
        NOT_MEDI_CAL_ELIGIBLE,
        NOT_CA_RESIDENT,
        NOT_COUNTY_RESIDENT,
        DOES_NOT_MEET_DISABILITY,
        DOES_NOT_MEET_AGE,
        UNSAFE_LIVING_ENVIRONMENT,
        INSTITUTIONALIZED,
        DUPLICATE_APPLICATION,
        INCOMPLETE_APPLICATION,
        FAILED_TO_COOPERATE,
        OTHER
    }

    // ==================== LIFECYCLE HOOKS ====================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (applicationDate == null) {
            applicationDate = LocalDate.now();
        }
        // Set deadline to 45 days from application date
        if (deadlineDate == null) {
            deadlineDate = applicationDate.plusDays(45);
        }
        if (status == null) {
            status = ApplicationStatus.PENDING;
        }
        // Generate application number if not set
        if (applicationNumber == null) {
            applicationNumber = generateApplicationNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateApplicationNumber() {
        // Format: APP-YYYYMMDD-XXXXX (random 5 digits)
        return String.format("APP-%s-%05d",
                LocalDate.now().toString().replace("-", ""),
                (int) (Math.random() * 100000));
    }

    // ==================== HELPER METHODS ====================

    /**
     * Calculate days remaining until deadline
     */
    public long getDaysRemaining() {
        LocalDate effectiveDeadline = extendedDeadlineDate != null ? extendedDeadlineDate : deadlineDate;
        return ChronoUnit.DAYS.between(LocalDate.now(), effectiveDeadline);
    }

    /**
     * Check if application is overdue
     */
    public boolean isOverdue() {
        return getDaysRemaining() < 0 && status == ApplicationStatus.PENDING;
    }

    /**
     * Get timeline status for UI display
     */
    public String getTimelineStatus() {
        long daysRemaining = getDaysRemaining();
        if (daysRemaining < 0) return "OVERDUE";
        if (daysRemaining <= 7) return "CRITICAL";
        if (daysRemaining <= 15) return "WARNING";
        return "ON_TRACK";
    }

    /**
     * Check if all eligibility requirements are met
     */
    public boolean meetsEligibilityRequirements() {
        return Boolean.TRUE.equals(californiaResident) &&
               Boolean.TRUE.equals(countyResidenceVerified) &&
               Boolean.TRUE.equals(meetsAgeRequirement) &&
               "ACTIVE".equals(mediCalStatus);
    }

    // ==================== CONSTRUCTORS ====================

    public ApplicationEntity() {
    }

    public static ApplicationEntityBuilder builder() {
        return new ApplicationEntityBuilder();
    }

    // ==================== GETTERS AND SETTERS ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getReferralId() {
        return referralId;
    }

    public void setReferralId(String referralId) {
        this.referralId = referralId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public ProgramType getProgramType() {
        return programType;
    }

    public void setProgramType(ProgramType programType) {
        this.programType = programType;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDate getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(LocalDate deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExtensionDate() {
        return extensionDate;
    }

    public void setExtensionDate(LocalDate extensionDate) {
        this.extensionDate = extensionDate;
    }

    public String getExtensionReason() {
        return extensionReason;
    }

    public void setExtensionReason(String extensionReason) {
        this.extensionReason = extensionReason;
    }

    public LocalDate getExtendedDeadlineDate() {
        return extendedDeadlineDate;
    }

    public void setExtendedDeadlineDate(LocalDate extendedDeadlineDate) {
        this.extendedDeadlineDate = extendedDeadlineDate;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public CINClearanceStatus getCinClearanceStatus() {
        return cinClearanceStatus;
    }

    public void setCinClearanceStatus(CINClearanceStatus cinClearanceStatus) {
        this.cinClearanceStatus = cinClearanceStatus;
    }

    public LocalDateTime getCinClearanceDate() {
        return cinClearanceDate;
    }

    public void setCinClearanceDate(LocalDateTime cinClearanceDate) {
        this.cinClearanceDate = cinClearanceDate;
    }

    public String getCinMatchType() {
        return cinMatchType;
    }

    public void setCinMatchType(String cinMatchType) {
        this.cinMatchType = cinMatchType;
    }

    public String getCinMismatchDetails() {
        return cinMismatchDetails;
    }

    public void setCinMismatchDetails(String cinMismatchDetails) {
        this.cinMismatchDetails = cinMismatchDetails;
    }

    public String getMediCalAidCode() {
        return mediCalAidCode;
    }

    public void setMediCalAidCode(String mediCalAidCode) {
        this.mediCalAidCode = mediCalAidCode;
    }

    public String getMediCalStatus() {
        return mediCalStatus;
    }

    public void setMediCalStatus(String mediCalStatus) {
        this.mediCalStatus = mediCalStatus;
    }

    public LocalDate getMediCalEffectiveDate() {
        return mediCalEffectiveDate;
    }

    public void setMediCalEffectiveDate(LocalDate mediCalEffectiveDate) {
        this.mediCalEffectiveDate = mediCalEffectiveDate;
    }

    public LocalDate getMediCalEndDate() {
        return mediCalEndDate;
    }

    public void setMediCalEndDate(LocalDate mediCalEndDate) {
        this.mediCalEndDate = mediCalEndDate;
    }

    public BigDecimal getShareOfCostAmount() {
        return shareOfCostAmount;
    }

    public void setShareOfCostAmount(BigDecimal shareOfCostAmount) {
        this.shareOfCostAmount = shareOfCostAmount;
    }

    public LocalDateTime getMedsVerificationDate() {
        return medsVerificationDate;
    }

    public void setMedsVerificationDate(LocalDateTime medsVerificationDate) {
        this.medsVerificationDate = medsVerificationDate;
    }

    public String getCitizenshipStatus() {
        return citizenshipStatus;
    }

    public void setCitizenshipStatus(String citizenshipStatus) {
        this.citizenshipStatus = citizenshipStatus;
    }

    public Boolean getCaliforniaResident() {
        return californiaResident;
    }

    public void setCaliforniaResident(Boolean californiaResident) {
        this.californiaResident = californiaResident;
    }

    public Boolean getCountyResidenceVerified() {
        return countyResidenceVerified;
    }

    public void setCountyResidenceVerified(Boolean countyResidenceVerified) {
        this.countyResidenceVerified = countyResidenceVerified;
    }

    public Integer getAgeAtApplication() {
        return ageAtApplication;
    }

    public void setAgeAtApplication(Integer ageAtApplication) {
        this.ageAtApplication = ageAtApplication;
    }

    public Boolean getMeetsAgeRequirement() {
        return meetsAgeRequirement;
    }

    public void setMeetsAgeRequirement(Boolean meetsAgeRequirement) {
        this.meetsAgeRequirement = meetsAgeRequirement;
    }

    public String getDisabilityStatus() {
        return disabilityStatus;
    }

    public void setDisabilityStatus(String disabilityStatus) {
        this.disabilityStatus = disabilityStatus;
    }

    public String getLivingArrangement() {
        return livingArrangement;
    }

    public void setLivingArrangement(String livingArrangement) {
        this.livingArrangement = livingArrangement;
    }

    public Boolean getSafeLivingEnvironment() {
        return safeLivingEnvironment;
    }

    public void setSafeLivingEnvironment(Boolean safeLivingEnvironment) {
        this.safeLivingEnvironment = safeLivingEnvironment;
    }

    public Boolean getHasIncome() {
        return hasIncome;
    }

    public void setHasIncome(Boolean hasIncome) {
        this.hasIncome = hasIncome;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public String getIncomeSource() {
        return incomeSource;
    }

    public void setIncomeSource(String incomeSource) {
        this.incomeSource = incomeSource;
    }

    public Boolean getHasResources() {
        return hasResources;
    }

    public void setHasResources(Boolean hasResources) {
        this.hasResources = hasResources;
    }

    public BigDecimal getTotalResources() {
        return totalResources;
    }

    public void setTotalResources(BigDecimal totalResources) {
        this.totalResources = totalResources;
    }

    public String getDenialReason() {
        return denialReason;
    }

    public void setDenialReason(String denialReason) {
        this.denialReason = denialReason;
    }

    public DenialCode getDenialCode() {
        return denialCode;
    }

    public void setDenialCode(DenialCode denialCode) {
        this.denialCode = denialCode;
    }

    public String getWithdrawalReason() {
        return withdrawalReason;
    }

    public void setWithdrawalReason(String withdrawalReason) {
        this.withdrawalReason = withdrawalReason;
    }

    public LocalDate getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public Boolean getCaseCreated() {
        return caseCreated;
    }

    public void setCaseCreated(Boolean caseCreated) {
        this.caseCreated = caseCreated;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public LocalDate getCaseCreationDate() {
        return caseCreationDate;
    }

    public void setCaseCreationDate(LocalDate caseCreationDate) {
        this.caseCreationDate = caseCreationDate;
    }

    public String getAssignedWorkerId() {
        return assignedWorkerId;
    }

    public void setAssignedWorkerId(String assignedWorkerId) {
        this.assignedWorkerId = assignedWorkerId;
    }

    public String getAssignedWorkerName() {
        return assignedWorkerName;
    }

    public void setAssignedWorkerName(String assignedWorkerName) {
        this.assignedWorkerName = assignedWorkerName;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
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

    public Boolean getAssessmentScheduled() {
        return assessmentScheduled;
    }

    public void setAssessmentScheduled(Boolean assessmentScheduled) {
        this.assessmentScheduled = assessmentScheduled;
    }

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public Boolean getAssessmentCompleted() {
        return assessmentCompleted;
    }

    public void setAssessmentCompleted(Boolean assessmentCompleted) {
        this.assessmentCompleted = assessmentCompleted;
    }

    public LocalDate getAssessmentCompletionDate() {
        return assessmentCompletionDate;
    }

    public void setAssessmentCompletionDate(LocalDate assessmentCompletionDate) {
        this.assessmentCompletionDate = assessmentCompletionDate;
    }

    public Boolean getSoc873Received() {
        return soc873Received;
    }

    public void setSoc873Received(Boolean soc873Received) {
        this.soc873Received = soc873Received;
    }

    public LocalDate getSoc873Date() {
        return soc873Date;
    }

    public void setSoc873Date(LocalDate soc873Date) {
        this.soc873Date = soc873Date;
    }

    public Boolean getMedicalCertificationReceived() {
        return medicalCertificationReceived;
    }

    public void setMedicalCertificationReceived(Boolean medicalCertificationReceived) {
        this.medicalCertificationReceived = medicalCertificationReceived;
    }

    public LocalDate getMedicalCertificationDate() {
        return medicalCertificationDate;
    }

    public void setMedicalCertificationDate(LocalDate medicalCertificationDate) {
        this.medicalCertificationDate = medicalCertificationDate;
    }

    public Boolean getRequiredDocsComplete() {
        return requiredDocsComplete;
    }

    public void setRequiredDocsComplete(Boolean requiredDocsComplete) {
        this.requiredDocsComplete = requiredDocsComplete;
    }

    public String getMissingDocuments() {
        return missingDocuments;
    }

    public void setMissingDocuments(String missingDocuments) {
        this.missingDocuments = missingDocuments;
    }

    public String getApplicationNotes() {
        return applicationNotes;
    }

    public void setApplicationNotes(String applicationNotes) {
        this.applicationNotes = applicationNotes;
    }

    public String getProcessingNotes() {
        return processingNotes;
    }

    public void setProcessingNotes(String processingNotes) {
        this.processingNotes = processingNotes;
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

    // ==================== BUILDER ====================

    public static class ApplicationEntityBuilder {
        private final ApplicationEntity entity = new ApplicationEntity();

        public ApplicationEntityBuilder referralId(String referralId) {
            entity.setReferralId(referralId);
            return this;
        }

        public ApplicationEntityBuilder recipientId(Long recipientId) {
            entity.setRecipientId(recipientId);
            return this;
        }

        public ApplicationEntityBuilder status(ApplicationStatus status) {
            entity.setStatus(status);
            return this;
        }

        public ApplicationEntityBuilder programType(ProgramType programType) {
            entity.setProgramType(programType);
            return this;
        }

        public ApplicationEntityBuilder applicationDate(LocalDate applicationDate) {
            entity.setApplicationDate(applicationDate);
            return this;
        }

        public ApplicationEntityBuilder countyCode(String countyCode) {
            entity.setCountyCode(countyCode);
            return this;
        }

        public ApplicationEntityBuilder assignedWorkerId(String workerId) {
            entity.setAssignedWorkerId(workerId);
            return this;
        }

        public ApplicationEntityBuilder createdBy(String createdBy) {
            entity.setCreatedBy(createdBy);
            return this;
        }

        public ApplicationEntity build() {
            return entity;
        }
    }
}
