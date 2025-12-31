package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Case Entity - Represents an IHSS case for a recipient
 * Based on DSD Section 20 - Recipient User Stories and Business Rules
 */
@Entity
@Table(name = "cases")
public class CaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", unique = true, nullable = false, length = 20)
    private String caseNumber;

    // Recipient Reference
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    // Case Status: PENDING, ELIGIBLE, PRESUMPTIVE_ELIGIBLE, ON_LEAVE, DENIED, TERMINATED, APPLICATION_WITHDRAWN
    @Enumerated(EnumType.STRING)
    @Column(name = "case_status", nullable = false)
    private CaseStatus caseStatus;

    // Case Type: IHSS, WPCS, IHSS_WPCS
    @Enumerated(EnumType.STRING)
    @Column(name = "case_type")
    private CaseType caseType;

    // County Information
    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "county_name", length = 100)
    private String countyName;

    // Case Owner (Caseworker)
    @Column(name = "case_owner_id")
    private String caseOwnerId;

    @Column(name = "case_owner_name", length = 200)
    private String caseOwnerName;

    @Column(name = "supervisor_id")
    private String supervisorId;

    // Client Index Number (CIN) - Key identifier across SAWS, MEDS, CMIPS
    @Column(name = "cin", length = 20)
    private String cin;

    // Medi-Cal Information
    @Column(name = "medi_cal_aid_code", length = 10)
    private String mediCalAidCode;

    @Column(name = "medi_cal_status")
    private String mediCalStatus;

    @Column(name = "medi_cal_eligibility_date")
    private LocalDate mediCalEligibilityDate;

    // Funding Source: PCSP, IPO, IPO3, IPO4, CFCO
    @Column(name = "funding_source", length = 20)
    private String fundingSource;

    // Application Dates
    @Column(name = "referral_date")
    private LocalDate referralDate;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "eligibility_date")
    private LocalDate eligibilityDate;

    @Column(name = "denial_date")
    private LocalDate denialDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "termination_reason", length = 500)
    private String terminationReason;

    // Authorization Information
    @Column(name = "authorization_start_date")
    private LocalDate authorizationStartDate;

    @Column(name = "authorization_end_date")
    private LocalDate authorizationEndDate;

    @Column(name = "authorized_hours_monthly")
    private Double authorizedHoursMonthly;

    @Column(name = "authorized_hours_weekly")
    private Double authorizedHoursWeekly;

    // Service Mode: IP (Individual Provider), CONTRACTED, ADVANCE_PAY
    @Column(name = "mode_of_service", length = 50)
    private String modeOfService;

    // Assessment Information
    @Column(name = "last_assessment_date")
    private LocalDate lastAssessmentDate;

    @Column(name = "reassessment_due_date")
    private LocalDate reassessmentDueDate;

    @Column(name = "home_visit_date")
    private LocalDate homeVisitDate;

    // Assessment Type: INITIAL, CHANGE, REASSESSMENT, INTER_COUNTY_TRANSFER, TELEHEALTH
    @Column(name = "assessment_type", length = 50)
    private String assessmentType;

    // Health Care Certification (SOC 873)
    @Column(name = "health_care_cert_status", length = 50)
    private String healthCareCertStatus;

    @Column(name = "health_care_cert_due_date")
    private LocalDate healthCareCertDueDate;

    @Column(name = "health_care_cert_received_date")
    private LocalDate healthCareCertReceivedDate;

    @Column(name = "health_care_cert_type", length = 100)
    private String healthCareCertType;

    @Column(name = "good_cause_extension_date")
    private LocalDate goodCauseExtensionDate;

    // Inter-County Transfer
    @Column(name = "transfer_status", length = 50)
    private String transferStatus;

    @Column(name = "sending_county_code", length = 10)
    private String sendingCountyCode;

    @Column(name = "receiving_county_code", length = 10)
    private String receivingCountyCode;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    // Share of Cost
    @Column(name = "share_of_cost_amount")
    private Double shareOfCostAmount;

    @Column(name = "countable_income")
    private Double countableIncome;

    @Column(name = "net_income")
    private Double netIncome;

    // Waiver Programs
    @Column(name = "waiver_program", length = 50)
    private String waiverProgram;

    @Column(name = "recipient_declines_cfco")
    private Boolean recipientDeclinesCfco;

    // Advance Pay
    @Column(name = "advance_pay_indicator")
    private Boolean advancePayIndicator;

    @Column(name = "advance_pay_rate")
    private Double advancePayRate;

    // County Use Notes
    @Column(name = "county_use_notes", columnDefinition = "TEXT")
    private String countyUseNotes;

    // Audit Fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public CaseEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (caseStatus == null) {
            caseStatus = CaseStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public CaseStatus getCaseStatus() { return caseStatus; }
    public void setCaseStatus(CaseStatus caseStatus) { this.caseStatus = caseStatus; }

    public CaseType getCaseType() { return caseType; }
    public void setCaseType(CaseType caseType) { this.caseType = caseType; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public String getCountyName() { return countyName; }
    public void setCountyName(String countyName) { this.countyName = countyName; }

    public String getCaseOwnerId() { return caseOwnerId; }
    public void setCaseOwnerId(String caseOwnerId) { this.caseOwnerId = caseOwnerId; }

    public String getCaseOwnerName() { return caseOwnerName; }
    public void setCaseOwnerName(String caseOwnerName) { this.caseOwnerName = caseOwnerName; }

    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getMediCalAidCode() { return mediCalAidCode; }
    public void setMediCalAidCode(String mediCalAidCode) { this.mediCalAidCode = mediCalAidCode; }

    public String getMediCalStatus() { return mediCalStatus; }
    public void setMediCalStatus(String mediCalStatus) { this.mediCalStatus = mediCalStatus; }

    public LocalDate getMediCalEligibilityDate() { return mediCalEligibilityDate; }
    public void setMediCalEligibilityDate(LocalDate mediCalEligibilityDate) { this.mediCalEligibilityDate = mediCalEligibilityDate; }

    public String getFundingSource() { return fundingSource; }
    public void setFundingSource(String fundingSource) { this.fundingSource = fundingSource; }

    public LocalDate getReferralDate() { return referralDate; }
    public void setReferralDate(LocalDate referralDate) { this.referralDate = referralDate; }

    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }

    public LocalDate getEligibilityDate() { return eligibilityDate; }
    public void setEligibilityDate(LocalDate eligibilityDate) { this.eligibilityDate = eligibilityDate; }

    public LocalDate getDenialDate() { return denialDate; }
    public void setDenialDate(LocalDate denialDate) { this.denialDate = denialDate; }

    public LocalDate getTerminationDate() { return terminationDate; }
    public void setTerminationDate(LocalDate terminationDate) { this.terminationDate = terminationDate; }

    public String getTerminationReason() { return terminationReason; }
    public void setTerminationReason(String terminationReason) { this.terminationReason = terminationReason; }

    public LocalDate getAuthorizationStartDate() { return authorizationStartDate; }
    public void setAuthorizationStartDate(LocalDate authorizationStartDate) { this.authorizationStartDate = authorizationStartDate; }

    public LocalDate getAuthorizationEndDate() { return authorizationEndDate; }
    public void setAuthorizationEndDate(LocalDate authorizationEndDate) { this.authorizationEndDate = authorizationEndDate; }

    public Double getAuthorizedHoursMonthly() { return authorizedHoursMonthly; }
    public void setAuthorizedHoursMonthly(Double authorizedHoursMonthly) { this.authorizedHoursMonthly = authorizedHoursMonthly; }

    public Double getAuthorizedHoursWeekly() { return authorizedHoursWeekly; }
    public void setAuthorizedHoursWeekly(Double authorizedHoursWeekly) { this.authorizedHoursWeekly = authorizedHoursWeekly; }

    public String getModeOfService() { return modeOfService; }
    public void setModeOfService(String modeOfService) { this.modeOfService = modeOfService; }

    public LocalDate getLastAssessmentDate() { return lastAssessmentDate; }
    public void setLastAssessmentDate(LocalDate lastAssessmentDate) { this.lastAssessmentDate = lastAssessmentDate; }

    public LocalDate getReassessmentDueDate() { return reassessmentDueDate; }
    public void setReassessmentDueDate(LocalDate reassessmentDueDate) { this.reassessmentDueDate = reassessmentDueDate; }

    public LocalDate getHomeVisitDate() { return homeVisitDate; }
    public void setHomeVisitDate(LocalDate homeVisitDate) { this.homeVisitDate = homeVisitDate; }

    public String getAssessmentType() { return assessmentType; }
    public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }

    public String getHealthCareCertStatus() { return healthCareCertStatus; }
    public void setHealthCareCertStatus(String healthCareCertStatus) { this.healthCareCertStatus = healthCareCertStatus; }

    public LocalDate getHealthCareCertDueDate() { return healthCareCertDueDate; }
    public void setHealthCareCertDueDate(LocalDate healthCareCertDueDate) { this.healthCareCertDueDate = healthCareCertDueDate; }

    public LocalDate getHealthCareCertReceivedDate() { return healthCareCertReceivedDate; }
    public void setHealthCareCertReceivedDate(LocalDate healthCareCertReceivedDate) { this.healthCareCertReceivedDate = healthCareCertReceivedDate; }

    public String getHealthCareCertType() { return healthCareCertType; }
    public void setHealthCareCertType(String healthCareCertType) { this.healthCareCertType = healthCareCertType; }

    public LocalDate getGoodCauseExtensionDate() { return goodCauseExtensionDate; }
    public void setGoodCauseExtensionDate(LocalDate goodCauseExtensionDate) { this.goodCauseExtensionDate = goodCauseExtensionDate; }

    public String getTransferStatus() { return transferStatus; }
    public void setTransferStatus(String transferStatus) { this.transferStatus = transferStatus; }

    public String getSendingCountyCode() { return sendingCountyCode; }
    public void setSendingCountyCode(String sendingCountyCode) { this.sendingCountyCode = sendingCountyCode; }

    public String getReceivingCountyCode() { return receivingCountyCode; }
    public void setReceivingCountyCode(String receivingCountyCode) { this.receivingCountyCode = receivingCountyCode; }

    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }

    public Double getShareOfCostAmount() { return shareOfCostAmount; }
    public void setShareOfCostAmount(Double shareOfCostAmount) { this.shareOfCostAmount = shareOfCostAmount; }

    public Double getCountableIncome() { return countableIncome; }
    public void setCountableIncome(Double countableIncome) { this.countableIncome = countableIncome; }

    public Double getNetIncome() { return netIncome; }
    public void setNetIncome(Double netIncome) { this.netIncome = netIncome; }

    public String getWaiverProgram() { return waiverProgram; }
    public void setWaiverProgram(String waiverProgram) { this.waiverProgram = waiverProgram; }

    public Boolean getRecipientDeclinesCfco() { return recipientDeclinesCfco; }
    public void setRecipientDeclinesCfco(Boolean recipientDeclinesCfco) { this.recipientDeclinesCfco = recipientDeclinesCfco; }

    public Boolean getAdvancePayIndicator() { return advancePayIndicator; }
    public void setAdvancePayIndicator(Boolean advancePayIndicator) { this.advancePayIndicator = advancePayIndicator; }

    public Double getAdvancePayRate() { return advancePayRate; }
    public void setAdvancePayRate(Double advancePayRate) { this.advancePayRate = advancePayRate; }

    public String getCountyUseNotes() { return countyUseNotes; }
    public void setCountyUseNotes(String countyUseNotes) { this.countyUseNotes = countyUseNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // Builder
    public static CaseEntityBuilder builder() { return new CaseEntityBuilder(); }

    public static class CaseEntityBuilder {
        private Long id;
        private String caseNumber;
        private Long recipientId;
        private CaseStatus caseStatus;
        private CaseType caseType;
        private String countyCode;
        private String countyName;
        private String caseOwnerId;
        private String caseOwnerName;
        private String supervisorId;
        private String cin;
        private String mediCalAidCode;
        private String mediCalStatus;
        private LocalDate mediCalEligibilityDate;
        private String fundingSource;
        private LocalDate referralDate;
        private LocalDate applicationDate;
        private LocalDate eligibilityDate;
        private LocalDate denialDate;
        private LocalDate terminationDate;
        private String terminationReason;
        private LocalDate authorizationStartDate;
        private LocalDate authorizationEndDate;
        private Double authorizedHoursMonthly;
        private Double authorizedHoursWeekly;
        private String modeOfService;
        private LocalDate lastAssessmentDate;
        private LocalDate reassessmentDueDate;
        private LocalDate homeVisitDate;
        private String assessmentType;
        private String healthCareCertStatus;
        private LocalDate healthCareCertDueDate;
        private LocalDate healthCareCertReceivedDate;
        private String healthCareCertType;
        private LocalDate goodCauseExtensionDate;
        private String transferStatus;
        private String sendingCountyCode;
        private String receivingCountyCode;
        private LocalDate transferDate;
        private Double shareOfCostAmount;
        private Double countableIncome;
        private Double netIncome;
        private String waiverProgram;
        private Boolean recipientDeclinesCfco;
        private Boolean advancePayIndicator;
        private Double advancePayRate;
        private String countyUseNotes;
        private String createdBy;
        private String updatedBy;

        public CaseEntityBuilder id(Long id) { this.id = id; return this; }
        public CaseEntityBuilder caseNumber(String caseNumber) { this.caseNumber = caseNumber; return this; }
        public CaseEntityBuilder recipientId(Long recipientId) { this.recipientId = recipientId; return this; }
        public CaseEntityBuilder caseStatus(CaseStatus caseStatus) { this.caseStatus = caseStatus; return this; }
        public CaseEntityBuilder caseType(CaseType caseType) { this.caseType = caseType; return this; }
        public CaseEntityBuilder countyCode(String countyCode) { this.countyCode = countyCode; return this; }
        public CaseEntityBuilder countyName(String countyName) { this.countyName = countyName; return this; }
        public CaseEntityBuilder caseOwnerId(String caseOwnerId) { this.caseOwnerId = caseOwnerId; return this; }
        public CaseEntityBuilder caseOwnerName(String caseOwnerName) { this.caseOwnerName = caseOwnerName; return this; }
        public CaseEntityBuilder supervisorId(String supervisorId) { this.supervisorId = supervisorId; return this; }
        public CaseEntityBuilder cin(String cin) { this.cin = cin; return this; }
        public CaseEntityBuilder mediCalAidCode(String mediCalAidCode) { this.mediCalAidCode = mediCalAidCode; return this; }
        public CaseEntityBuilder mediCalStatus(String mediCalStatus) { this.mediCalStatus = mediCalStatus; return this; }
        public CaseEntityBuilder mediCalEligibilityDate(LocalDate mediCalEligibilityDate) { this.mediCalEligibilityDate = mediCalEligibilityDate; return this; }
        public CaseEntityBuilder fundingSource(String fundingSource) { this.fundingSource = fundingSource; return this; }
        public CaseEntityBuilder referralDate(LocalDate referralDate) { this.referralDate = referralDate; return this; }
        public CaseEntityBuilder applicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; return this; }
        public CaseEntityBuilder eligibilityDate(LocalDate eligibilityDate) { this.eligibilityDate = eligibilityDate; return this; }
        public CaseEntityBuilder denialDate(LocalDate denialDate) { this.denialDate = denialDate; return this; }
        public CaseEntityBuilder terminationDate(LocalDate terminationDate) { this.terminationDate = terminationDate; return this; }
        public CaseEntityBuilder terminationReason(String terminationReason) { this.terminationReason = terminationReason; return this; }
        public CaseEntityBuilder authorizationStartDate(LocalDate authorizationStartDate) { this.authorizationStartDate = authorizationStartDate; return this; }
        public CaseEntityBuilder authorizationEndDate(LocalDate authorizationEndDate) { this.authorizationEndDate = authorizationEndDate; return this; }
        public CaseEntityBuilder authorizedHoursMonthly(Double authorizedHoursMonthly) { this.authorizedHoursMonthly = authorizedHoursMonthly; return this; }
        public CaseEntityBuilder authorizedHoursWeekly(Double authorizedHoursWeekly) { this.authorizedHoursWeekly = authorizedHoursWeekly; return this; }
        public CaseEntityBuilder modeOfService(String modeOfService) { this.modeOfService = modeOfService; return this; }
        public CaseEntityBuilder lastAssessmentDate(LocalDate lastAssessmentDate) { this.lastAssessmentDate = lastAssessmentDate; return this; }
        public CaseEntityBuilder reassessmentDueDate(LocalDate reassessmentDueDate) { this.reassessmentDueDate = reassessmentDueDate; return this; }
        public CaseEntityBuilder homeVisitDate(LocalDate homeVisitDate) { this.homeVisitDate = homeVisitDate; return this; }
        public CaseEntityBuilder assessmentType(String assessmentType) { this.assessmentType = assessmentType; return this; }
        public CaseEntityBuilder healthCareCertStatus(String healthCareCertStatus) { this.healthCareCertStatus = healthCareCertStatus; return this; }
        public CaseEntityBuilder healthCareCertDueDate(LocalDate healthCareCertDueDate) { this.healthCareCertDueDate = healthCareCertDueDate; return this; }
        public CaseEntityBuilder healthCareCertReceivedDate(LocalDate healthCareCertReceivedDate) { this.healthCareCertReceivedDate = healthCareCertReceivedDate; return this; }
        public CaseEntityBuilder healthCareCertType(String healthCareCertType) { this.healthCareCertType = healthCareCertType; return this; }
        public CaseEntityBuilder goodCauseExtensionDate(LocalDate goodCauseExtensionDate) { this.goodCauseExtensionDate = goodCauseExtensionDate; return this; }
        public CaseEntityBuilder transferStatus(String transferStatus) { this.transferStatus = transferStatus; return this; }
        public CaseEntityBuilder sendingCountyCode(String sendingCountyCode) { this.sendingCountyCode = sendingCountyCode; return this; }
        public CaseEntityBuilder receivingCountyCode(String receivingCountyCode) { this.receivingCountyCode = receivingCountyCode; return this; }
        public CaseEntityBuilder transferDate(LocalDate transferDate) { this.transferDate = transferDate; return this; }
        public CaseEntityBuilder shareOfCostAmount(Double shareOfCostAmount) { this.shareOfCostAmount = shareOfCostAmount; return this; }
        public CaseEntityBuilder countableIncome(Double countableIncome) { this.countableIncome = countableIncome; return this; }
        public CaseEntityBuilder netIncome(Double netIncome) { this.netIncome = netIncome; return this; }
        public CaseEntityBuilder waiverProgram(String waiverProgram) { this.waiverProgram = waiverProgram; return this; }
        public CaseEntityBuilder recipientDeclinesCfco(Boolean recipientDeclinesCfco) { this.recipientDeclinesCfco = recipientDeclinesCfco; return this; }
        public CaseEntityBuilder advancePayIndicator(Boolean advancePayIndicator) { this.advancePayIndicator = advancePayIndicator; return this; }
        public CaseEntityBuilder advancePayRate(Double advancePayRate) { this.advancePayRate = advancePayRate; return this; }
        public CaseEntityBuilder countyUseNotes(String countyUseNotes) { this.countyUseNotes = countyUseNotes; return this; }
        public CaseEntityBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public CaseEntityBuilder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }

        public CaseEntity build() {
            CaseEntity entity = new CaseEntity();
            entity.id = this.id;
            entity.caseNumber = this.caseNumber;
            entity.recipientId = this.recipientId;
            entity.caseStatus = this.caseStatus;
            entity.caseType = this.caseType;
            entity.countyCode = this.countyCode;
            entity.countyName = this.countyName;
            entity.caseOwnerId = this.caseOwnerId;
            entity.caseOwnerName = this.caseOwnerName;
            entity.supervisorId = this.supervisorId;
            entity.cin = this.cin;
            entity.mediCalAidCode = this.mediCalAidCode;
            entity.mediCalStatus = this.mediCalStatus;
            entity.mediCalEligibilityDate = this.mediCalEligibilityDate;
            entity.fundingSource = this.fundingSource;
            entity.referralDate = this.referralDate;
            entity.applicationDate = this.applicationDate;
            entity.eligibilityDate = this.eligibilityDate;
            entity.denialDate = this.denialDate;
            entity.terminationDate = this.terminationDate;
            entity.terminationReason = this.terminationReason;
            entity.authorizationStartDate = this.authorizationStartDate;
            entity.authorizationEndDate = this.authorizationEndDate;
            entity.authorizedHoursMonthly = this.authorizedHoursMonthly;
            entity.authorizedHoursWeekly = this.authorizedHoursWeekly;
            entity.modeOfService = this.modeOfService;
            entity.lastAssessmentDate = this.lastAssessmentDate;
            entity.reassessmentDueDate = this.reassessmentDueDate;
            entity.homeVisitDate = this.homeVisitDate;
            entity.assessmentType = this.assessmentType;
            entity.healthCareCertStatus = this.healthCareCertStatus;
            entity.healthCareCertDueDate = this.healthCareCertDueDate;
            entity.healthCareCertReceivedDate = this.healthCareCertReceivedDate;
            entity.healthCareCertType = this.healthCareCertType;
            entity.goodCauseExtensionDate = this.goodCauseExtensionDate;
            entity.transferStatus = this.transferStatus;
            entity.sendingCountyCode = this.sendingCountyCode;
            entity.receivingCountyCode = this.receivingCountyCode;
            entity.transferDate = this.transferDate;
            entity.shareOfCostAmount = this.shareOfCostAmount;
            entity.countableIncome = this.countableIncome;
            entity.netIncome = this.netIncome;
            entity.waiverProgram = this.waiverProgram;
            entity.recipientDeclinesCfco = this.recipientDeclinesCfco;
            entity.advancePayIndicator = this.advancePayIndicator;
            entity.advancePayRate = this.advancePayRate;
            entity.countyUseNotes = this.countyUseNotes;
            entity.createdBy = this.createdBy;
            entity.updatedBy = this.updatedBy;
            return entity;
        }
    }

    // Case Status Enum
    public enum CaseStatus {
        PENDING,
        ELIGIBLE,
        PRESUMPTIVE_ELIGIBLE,
        ON_LEAVE,
        DENIED,
        TERMINATED,
        APPLICATION_WITHDRAWN
    }

    // Case Type Enum
    public enum CaseType {
        IHSS,
        WPCS,
        IHSS_WPCS
    }
}
