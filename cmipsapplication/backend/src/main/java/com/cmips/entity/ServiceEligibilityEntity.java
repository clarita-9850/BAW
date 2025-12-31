package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service Eligibility Entity - Represents service assessments and eligibility
 * Based on DSD Section 21 - Service Eligibility User Stories and Business Rules
 */
@Entity
@Table(name = "service_eligibility")
public class ServiceEligibilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    // Assessment Type: INITIAL, CHANGE, REASSESSMENT, INTER_COUNTY_TRANSFER, TELEHEALTH
    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type")
    private AssessmentType assessmentType;

    // Assessment Dates
    @Column(name = "assessment_date")
    private LocalDate assessmentDate;

    @Column(name = "home_visit_date")
    private LocalDate homeVisitDate;

    @Column(name = "authorization_start_date")
    private LocalDate authorizationStartDate;

    @Column(name = "authorization_end_date")
    private LocalDate authorizationEndDate;

    @Column(name = "reassessment_due_date")
    private LocalDate reassessmentDueDate;

    // Status: PENDING, ACTIVE, INACTIVE, SUPERSEDED
    @Column(name = "status", length = 20)
    private String status;

    // Authorized Hours
    @Column(name = "total_authorized_hours_monthly")
    private Double totalAuthorizedHoursMonthly;

    @Column(name = "total_authorized_hours_weekly")
    private Double totalAuthorizedHoursWeekly;

    @Column(name = "auth_to_purchase_hours")
    private Double authToPurchaseHours;

    // Service Types Hours (Individual columns for each service)
    @Column(name = "domestic_services_hours")
    private Double domesticServicesHours;

    @Column(name = "related_services_hours")
    private Double relatedServicesHours;

    @Column(name = "personal_care_hours")
    private Double personalCareHours;

    @Column(name = "paramedical_hours")
    private Double paramedicalHours;

    @Column(name = "protective_supervision_hours")
    private Double protectiveSupervisionHours;

    @Column(name = "meal_preparation_hours")
    private Double mealPreparationHours;

    @Column(name = "meal_cleanup_hours")
    private Double mealCleanupHours;

    @Column(name = "laundry_hours")
    private Double laundryHours;

    @Column(name = "shopping_errands_hours")
    private Double shoppingErrandsHours;

    @Column(name = "ambulation_hours")
    private Double ambulationHours;

    @Column(name = "bathing_oral_hygiene_hours")
    private Double bathingOralHygieneHours;

    @Column(name = "grooming_hours")
    private Double groomingHours;

    @Column(name = "dressing_hours")
    private Double dressingHours;

    @Column(name = "bowel_bladder_care_hours")
    private Double bowelBladderCareHours;

    @Column(name = "transfer_repositioning_hours")
    private Double transferRepositioningHours;

    @Column(name = "feeding_hours")
    private Double feedingHours;

    @Column(name = "respiration_hours")
    private Double respirationHours;

    @Column(name = "skin_care_hours")
    private Double skinCareHours;

    // Functional Ranks (1-5 scale)
    @Column(name = "functional_rank_domestic")
    private Integer functionalRankDomestic;

    @Column(name = "functional_rank_related")
    private Integer functionalRankRelated;

    @Column(name = "functional_rank_personal")
    private Integer functionalRankPersonal;

    @Column(name = "functional_rank_paramedical")
    private Integer functionalRankParamedical;

    // HTG (Hourly Task Guideline) Indicators
    // +: Exceeds HTG, -: Below HTG, blank: Within HTG
    @Column(name = "htg_domestic", length = 5)
    private String htgDomestic;

    @Column(name = "htg_related", length = 5)
    private String htgRelated;

    @Column(name = "htg_personal", length = 5)
    private String htgPersonal;

    @Column(name = "htg_paramedical", length = 5)
    private String htgParamedical;

    // Adjustments (Proration)
    @Column(name = "adjustments_hours")
    private Double adjustmentsHours;

    @Column(name = "proration_factor")
    private Double prorationFactor;

    // Share of Cost Information
    @Column(name = "net_income")
    private Double netIncome;

    @Column(name = "countable_income")
    private Double countableIncome;

    @Column(name = "ihss_share_of_cost")
    private Double ihssShareOfCost;

    // Waiver Program
    @Column(name = "waiver_program", length = 50)
    private String waiverProgram;

    @Column(name = "recipient_declines_cfco")
    private Boolean recipientDeclinesCfco;

    // Reduced Hours
    @Column(name = "reinstated_hours")
    private Double reinstatedHours;

    @Column(name = "social_worker_certification")
    private String socialWorkerCertification;

    @Column(name = "verified_by_case_owner_or_supervisor")
    private Boolean verifiedByCaseOwnerOrSupervisor;

    // Advance Pay (per BR SE 09)
    @Column(name = "advance_pay_indicated")
    private Boolean advancePayIndicated;

    @Column(name = "advance_pay_rate")
    private Double advancePayRate;

    // County Pay Rates
    @Column(name = "county_ip_rate")
    private Double countyIpRate;

    @Column(name = "county_pay_rate_effective_date")
    private LocalDate countyPayRateEffectiveDate;

    // Assessor Information
    @Column(name = "assessor_id", length = 100)
    private String assessorId;

    @Column(name = "assessor_name", length = 200)
    private String assessorName;

    // Approval Information
    @Column(name = "approved_by_id", length = 100)
    private String approvedById;

    @Column(name = "approved_by_name", length = 200)
    private String approvedByName;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

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
    public ServiceEligibilityEntity() {
    }

    // Private constructor for builder
    private ServiceEligibilityEntity(Builder builder) {
        this.id = builder.id;
        this.caseId = builder.caseId;
        this.recipientId = builder.recipientId;
        this.assessmentType = builder.assessmentType;
        this.assessmentDate = builder.assessmentDate;
        this.homeVisitDate = builder.homeVisitDate;
        this.authorizationStartDate = builder.authorizationStartDate;
        this.authorizationEndDate = builder.authorizationEndDate;
        this.reassessmentDueDate = builder.reassessmentDueDate;
        this.status = builder.status;
        this.totalAuthorizedHoursMonthly = builder.totalAuthorizedHoursMonthly;
        this.totalAuthorizedHoursWeekly = builder.totalAuthorizedHoursWeekly;
        this.authToPurchaseHours = builder.authToPurchaseHours;
        this.domesticServicesHours = builder.domesticServicesHours;
        this.relatedServicesHours = builder.relatedServicesHours;
        this.personalCareHours = builder.personalCareHours;
        this.paramedicalHours = builder.paramedicalHours;
        this.protectiveSupervisionHours = builder.protectiveSupervisionHours;
        this.mealPreparationHours = builder.mealPreparationHours;
        this.mealCleanupHours = builder.mealCleanupHours;
        this.laundryHours = builder.laundryHours;
        this.shoppingErrandsHours = builder.shoppingErrandsHours;
        this.ambulationHours = builder.ambulationHours;
        this.bathingOralHygieneHours = builder.bathingOralHygieneHours;
        this.groomingHours = builder.groomingHours;
        this.dressingHours = builder.dressingHours;
        this.bowelBladderCareHours = builder.bowelBladderCareHours;
        this.transferRepositioningHours = builder.transferRepositioningHours;
        this.feedingHours = builder.feedingHours;
        this.respirationHours = builder.respirationHours;
        this.skinCareHours = builder.skinCareHours;
        this.functionalRankDomestic = builder.functionalRankDomestic;
        this.functionalRankRelated = builder.functionalRankRelated;
        this.functionalRankPersonal = builder.functionalRankPersonal;
        this.functionalRankParamedical = builder.functionalRankParamedical;
        this.htgDomestic = builder.htgDomestic;
        this.htgRelated = builder.htgRelated;
        this.htgPersonal = builder.htgPersonal;
        this.htgParamedical = builder.htgParamedical;
        this.adjustmentsHours = builder.adjustmentsHours;
        this.prorationFactor = builder.prorationFactor;
        this.netIncome = builder.netIncome;
        this.countableIncome = builder.countableIncome;
        this.ihssShareOfCost = builder.ihssShareOfCost;
        this.waiverProgram = builder.waiverProgram;
        this.recipientDeclinesCfco = builder.recipientDeclinesCfco;
        this.reinstatedHours = builder.reinstatedHours;
        this.socialWorkerCertification = builder.socialWorkerCertification;
        this.verifiedByCaseOwnerOrSupervisor = builder.verifiedByCaseOwnerOrSupervisor;
        this.advancePayIndicated = builder.advancePayIndicated;
        this.advancePayRate = builder.advancePayRate;
        this.countyIpRate = builder.countyIpRate;
        this.countyPayRateEffectiveDate = builder.countyPayRateEffectiveDate;
        this.assessorId = builder.assessorId;
        this.assessorName = builder.assessorName;
        this.approvedById = builder.approvedById;
        this.approvedByName = builder.approvedByName;
        this.approvalDate = builder.approvalDate;
        this.createdAt = builder.createdAt;
        this.createdBy = builder.createdBy;
        this.updatedAt = builder.updatedAt;
        this.updatedBy = builder.updatedBy;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Assessment Type Enum
    public enum AssessmentType {
        INITIAL,
        CHANGE,
        REASSESSMENT,
        INTER_COUNTY_TRANSFER,
        TELEHEALTH
    }

    /**
     * Calculate Total Assessed Need per BR SE 01
     */
    public Double calculateTotalAssessedNeed() {
        double total = 0.0;
        if (domesticServicesHours != null) total += domesticServicesHours;
        if (relatedServicesHours != null) total += relatedServicesHours;
        if (personalCareHours != null) total += personalCareHours;
        if (paramedicalHours != null) total += paramedicalHours;
        if (protectiveSupervisionHours != null) total += protectiveSupervisionHours;
        if (mealPreparationHours != null) total += mealPreparationHours;
        if (mealCleanupHours != null) total += mealCleanupHours;
        if (laundryHours != null) total += laundryHours;
        if (shoppingErrandsHours != null) total += shoppingErrandsHours;
        if (ambulationHours != null) total += ambulationHours;
        if (bathingOralHygieneHours != null) total += bathingOralHygieneHours;
        if (groomingHours != null) total += groomingHours;
        if (dressingHours != null) total += dressingHours;
        if (bowelBladderCareHours != null) total += bowelBladderCareHours;
        if (transferRepositioningHours != null) total += transferRepositioningHours;
        if (feedingHours != null) total += feedingHours;
        if (respirationHours != null) total += respirationHours;
        if (skinCareHours != null) total += skinCareHours;
        return total;
    }

    /**
     * Set Reassessment Due Date per BR SE 04, 05, 06, 50
     */
    public void setDefaultReassessmentDueDate() {
        if (homeVisitDate != null && reassessmentDueDate == null) {
            reassessmentDueDate = homeVisitDate.plusYears(1);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public AssessmentType getAssessmentType() { return assessmentType; }
    public void setAssessmentType(AssessmentType assessmentType) { this.assessmentType = assessmentType; }

    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }

    public LocalDate getHomeVisitDate() { return homeVisitDate; }
    public void setHomeVisitDate(LocalDate homeVisitDate) { this.homeVisitDate = homeVisitDate; }

    public LocalDate getAuthorizationStartDate() { return authorizationStartDate; }
    public void setAuthorizationStartDate(LocalDate authorizationStartDate) { this.authorizationStartDate = authorizationStartDate; }

    public LocalDate getAuthorizationEndDate() { return authorizationEndDate; }
    public void setAuthorizationEndDate(LocalDate authorizationEndDate) { this.authorizationEndDate = authorizationEndDate; }

    public LocalDate getReassessmentDueDate() { return reassessmentDueDate; }
    public void setReassessmentDueDate(LocalDate reassessmentDueDate) { this.reassessmentDueDate = reassessmentDueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotalAuthorizedHoursMonthly() { return totalAuthorizedHoursMonthly; }
    public void setTotalAuthorizedHoursMonthly(Double totalAuthorizedHoursMonthly) { this.totalAuthorizedHoursMonthly = totalAuthorizedHoursMonthly; }

    public Double getTotalAuthorizedHoursWeekly() { return totalAuthorizedHoursWeekly; }
    public void setTotalAuthorizedHoursWeekly(Double totalAuthorizedHoursWeekly) { this.totalAuthorizedHoursWeekly = totalAuthorizedHoursWeekly; }

    public Double getAuthToPurchaseHours() { return authToPurchaseHours; }
    public void setAuthToPurchaseHours(Double authToPurchaseHours) { this.authToPurchaseHours = authToPurchaseHours; }

    public Double getDomesticServicesHours() { return domesticServicesHours; }
    public void setDomesticServicesHours(Double domesticServicesHours) { this.domesticServicesHours = domesticServicesHours; }

    public Double getRelatedServicesHours() { return relatedServicesHours; }
    public void setRelatedServicesHours(Double relatedServicesHours) { this.relatedServicesHours = relatedServicesHours; }

    public Double getPersonalCareHours() { return personalCareHours; }
    public void setPersonalCareHours(Double personalCareHours) { this.personalCareHours = personalCareHours; }

    public Double getParamedicalHours() { return paramedicalHours; }
    public void setParamedicalHours(Double paramedicalHours) { this.paramedicalHours = paramedicalHours; }

    public Double getProtectiveSupervisionHours() { return protectiveSupervisionHours; }
    public void setProtectiveSupervisionHours(Double protectiveSupervisionHours) { this.protectiveSupervisionHours = protectiveSupervisionHours; }

    public Double getMealPreparationHours() { return mealPreparationHours; }
    public void setMealPreparationHours(Double mealPreparationHours) { this.mealPreparationHours = mealPreparationHours; }

    public Double getMealCleanupHours() { return mealCleanupHours; }
    public void setMealCleanupHours(Double mealCleanupHours) { this.mealCleanupHours = mealCleanupHours; }

    public Double getLaundryHours() { return laundryHours; }
    public void setLaundryHours(Double laundryHours) { this.laundryHours = laundryHours; }

    public Double getShoppingErrandsHours() { return shoppingErrandsHours; }
    public void setShoppingErrandsHours(Double shoppingErrandsHours) { this.shoppingErrandsHours = shoppingErrandsHours; }

    public Double getAmbulationHours() { return ambulationHours; }
    public void setAmbulationHours(Double ambulationHours) { this.ambulationHours = ambulationHours; }

    public Double getBathingOralHygieneHours() { return bathingOralHygieneHours; }
    public void setBathingOralHygieneHours(Double bathingOralHygieneHours) { this.bathingOralHygieneHours = bathingOralHygieneHours; }

    public Double getGroomingHours() { return groomingHours; }
    public void setGroomingHours(Double groomingHours) { this.groomingHours = groomingHours; }

    public Double getDressingHours() { return dressingHours; }
    public void setDressingHours(Double dressingHours) { this.dressingHours = dressingHours; }

    public Double getBowelBladderCareHours() { return bowelBladderCareHours; }
    public void setBowelBladderCareHours(Double bowelBladderCareHours) { this.bowelBladderCareHours = bowelBladderCareHours; }

    public Double getTransferRepositioningHours() { return transferRepositioningHours; }
    public void setTransferRepositioningHours(Double transferRepositioningHours) { this.transferRepositioningHours = transferRepositioningHours; }

    public Double getFeedingHours() { return feedingHours; }
    public void setFeedingHours(Double feedingHours) { this.feedingHours = feedingHours; }

    public Double getRespirationHours() { return respirationHours; }
    public void setRespirationHours(Double respirationHours) { this.respirationHours = respirationHours; }

    public Double getSkinCareHours() { return skinCareHours; }
    public void setSkinCareHours(Double skinCareHours) { this.skinCareHours = skinCareHours; }

    public Integer getFunctionalRankDomestic() { return functionalRankDomestic; }
    public void setFunctionalRankDomestic(Integer functionalRankDomestic) { this.functionalRankDomestic = functionalRankDomestic; }

    public Integer getFunctionalRankRelated() { return functionalRankRelated; }
    public void setFunctionalRankRelated(Integer functionalRankRelated) { this.functionalRankRelated = functionalRankRelated; }

    public Integer getFunctionalRankPersonal() { return functionalRankPersonal; }
    public void setFunctionalRankPersonal(Integer functionalRankPersonal) { this.functionalRankPersonal = functionalRankPersonal; }

    public Integer getFunctionalRankParamedical() { return functionalRankParamedical; }
    public void setFunctionalRankParamedical(Integer functionalRankParamedical) { this.functionalRankParamedical = functionalRankParamedical; }

    public String getHtgDomestic() { return htgDomestic; }
    public void setHtgDomestic(String htgDomestic) { this.htgDomestic = htgDomestic; }

    public String getHtgRelated() { return htgRelated; }
    public void setHtgRelated(String htgRelated) { this.htgRelated = htgRelated; }

    public String getHtgPersonal() { return htgPersonal; }
    public void setHtgPersonal(String htgPersonal) { this.htgPersonal = htgPersonal; }

    public String getHtgParamedical() { return htgParamedical; }
    public void setHtgParamedical(String htgParamedical) { this.htgParamedical = htgParamedical; }

    public Double getAdjustmentsHours() { return adjustmentsHours; }
    public void setAdjustmentsHours(Double adjustmentsHours) { this.adjustmentsHours = adjustmentsHours; }

    public Double getProrationFactor() { return prorationFactor; }
    public void setProrationFactor(Double prorationFactor) { this.prorationFactor = prorationFactor; }

    public Double getNetIncome() { return netIncome; }
    public void setNetIncome(Double netIncome) { this.netIncome = netIncome; }

    public Double getCountableIncome() { return countableIncome; }
    public void setCountableIncome(Double countableIncome) { this.countableIncome = countableIncome; }

    public Double getIhssShareOfCost() { return ihssShareOfCost; }
    public void setIhssShareOfCost(Double ihssShareOfCost) { this.ihssShareOfCost = ihssShareOfCost; }

    public String getWaiverProgram() { return waiverProgram; }
    public void setWaiverProgram(String waiverProgram) { this.waiverProgram = waiverProgram; }

    public Boolean getRecipientDeclinesCfco() { return recipientDeclinesCfco; }
    public void setRecipientDeclinesCfco(Boolean recipientDeclinesCfco) { this.recipientDeclinesCfco = recipientDeclinesCfco; }

    public Double getReinstatedHours() { return reinstatedHours; }
    public void setReinstatedHours(Double reinstatedHours) { this.reinstatedHours = reinstatedHours; }

    public String getSocialWorkerCertification() { return socialWorkerCertification; }
    public void setSocialWorkerCertification(String socialWorkerCertification) { this.socialWorkerCertification = socialWorkerCertification; }

    public Boolean getVerifiedByCaseOwnerOrSupervisor() { return verifiedByCaseOwnerOrSupervisor; }
    public void setVerifiedByCaseOwnerOrSupervisor(Boolean verifiedByCaseOwnerOrSupervisor) { this.verifiedByCaseOwnerOrSupervisor = verifiedByCaseOwnerOrSupervisor; }

    public Boolean getAdvancePayIndicated() { return advancePayIndicated; }
    public void setAdvancePayIndicated(Boolean advancePayIndicated) { this.advancePayIndicated = advancePayIndicated; }

    public Double getAdvancePayRate() { return advancePayRate; }
    public void setAdvancePayRate(Double advancePayRate) { this.advancePayRate = advancePayRate; }

    public Double getCountyIpRate() { return countyIpRate; }
    public void setCountyIpRate(Double countyIpRate) { this.countyIpRate = countyIpRate; }

    public LocalDate getCountyPayRateEffectiveDate() { return countyPayRateEffectiveDate; }
    public void setCountyPayRateEffectiveDate(LocalDate countyPayRateEffectiveDate) { this.countyPayRateEffectiveDate = countyPayRateEffectiveDate; }

    public String getAssessorId() { return assessorId; }
    public void setAssessorId(String assessorId) { this.assessorId = assessorId; }

    public String getAssessorName() { return assessorName; }
    public void setAssessorName(String assessorName) { this.assessorName = assessorName; }

    public String getApprovedById() { return approvedById; }
    public void setApprovedById(String approvedById) { this.approvedById = approvedById; }

    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long caseId;
        private Long recipientId;
        private AssessmentType assessmentType;
        private LocalDate assessmentDate;
        private LocalDate homeVisitDate;
        private LocalDate authorizationStartDate;
        private LocalDate authorizationEndDate;
        private LocalDate reassessmentDueDate;
        private String status;
        private Double totalAuthorizedHoursMonthly;
        private Double totalAuthorizedHoursWeekly;
        private Double authToPurchaseHours;
        private Double domesticServicesHours;
        private Double relatedServicesHours;
        private Double personalCareHours;
        private Double paramedicalHours;
        private Double protectiveSupervisionHours;
        private Double mealPreparationHours;
        private Double mealCleanupHours;
        private Double laundryHours;
        private Double shoppingErrandsHours;
        private Double ambulationHours;
        private Double bathingOralHygieneHours;
        private Double groomingHours;
        private Double dressingHours;
        private Double bowelBladderCareHours;
        private Double transferRepositioningHours;
        private Double feedingHours;
        private Double respirationHours;
        private Double skinCareHours;
        private Integer functionalRankDomestic;
        private Integer functionalRankRelated;
        private Integer functionalRankPersonal;
        private Integer functionalRankParamedical;
        private String htgDomestic;
        private String htgRelated;
        private String htgPersonal;
        private String htgParamedical;
        private Double adjustmentsHours;
        private Double prorationFactor;
        private Double netIncome;
        private Double countableIncome;
        private Double ihssShareOfCost;
        private String waiverProgram;
        private Boolean recipientDeclinesCfco;
        private Double reinstatedHours;
        private String socialWorkerCertification;
        private Boolean verifiedByCaseOwnerOrSupervisor;
        private Boolean advancePayIndicated;
        private Double advancePayRate;
        private Double countyIpRate;
        private LocalDate countyPayRateEffectiveDate;
        private String assessorId;
        private String assessorName;
        private String approvedById;
        private String approvedByName;
        private LocalDate approvalDate;
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder caseId(Long caseId) { this.caseId = caseId; return this; }
        public Builder recipientId(Long recipientId) { this.recipientId = recipientId; return this; }
        public Builder assessmentType(AssessmentType assessmentType) { this.assessmentType = assessmentType; return this; }
        public Builder assessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; return this; }
        public Builder homeVisitDate(LocalDate homeVisitDate) { this.homeVisitDate = homeVisitDate; return this; }
        public Builder authorizationStartDate(LocalDate authorizationStartDate) { this.authorizationStartDate = authorizationStartDate; return this; }
        public Builder authorizationEndDate(LocalDate authorizationEndDate) { this.authorizationEndDate = authorizationEndDate; return this; }
        public Builder reassessmentDueDate(LocalDate reassessmentDueDate) { this.reassessmentDueDate = reassessmentDueDate; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder totalAuthorizedHoursMonthly(Double totalAuthorizedHoursMonthly) { this.totalAuthorizedHoursMonthly = totalAuthorizedHoursMonthly; return this; }
        public Builder totalAuthorizedHoursWeekly(Double totalAuthorizedHoursWeekly) { this.totalAuthorizedHoursWeekly = totalAuthorizedHoursWeekly; return this; }
        public Builder authToPurchaseHours(Double authToPurchaseHours) { this.authToPurchaseHours = authToPurchaseHours; return this; }
        public Builder domesticServicesHours(Double domesticServicesHours) { this.domesticServicesHours = domesticServicesHours; return this; }
        public Builder relatedServicesHours(Double relatedServicesHours) { this.relatedServicesHours = relatedServicesHours; return this; }
        public Builder personalCareHours(Double personalCareHours) { this.personalCareHours = personalCareHours; return this; }
        public Builder paramedicalHours(Double paramedicalHours) { this.paramedicalHours = paramedicalHours; return this; }
        public Builder protectiveSupervisionHours(Double protectiveSupervisionHours) { this.protectiveSupervisionHours = protectiveSupervisionHours; return this; }
        public Builder mealPreparationHours(Double mealPreparationHours) { this.mealPreparationHours = mealPreparationHours; return this; }
        public Builder mealCleanupHours(Double mealCleanupHours) { this.mealCleanupHours = mealCleanupHours; return this; }
        public Builder laundryHours(Double laundryHours) { this.laundryHours = laundryHours; return this; }
        public Builder shoppingErrandsHours(Double shoppingErrandsHours) { this.shoppingErrandsHours = shoppingErrandsHours; return this; }
        public Builder ambulationHours(Double ambulationHours) { this.ambulationHours = ambulationHours; return this; }
        public Builder bathingOralHygieneHours(Double bathingOralHygieneHours) { this.bathingOralHygieneHours = bathingOralHygieneHours; return this; }
        public Builder groomingHours(Double groomingHours) { this.groomingHours = groomingHours; return this; }
        public Builder dressingHours(Double dressingHours) { this.dressingHours = dressingHours; return this; }
        public Builder bowelBladderCareHours(Double bowelBladderCareHours) { this.bowelBladderCareHours = bowelBladderCareHours; return this; }
        public Builder transferRepositioningHours(Double transferRepositioningHours) { this.transferRepositioningHours = transferRepositioningHours; return this; }
        public Builder feedingHours(Double feedingHours) { this.feedingHours = feedingHours; return this; }
        public Builder respirationHours(Double respirationHours) { this.respirationHours = respirationHours; return this; }
        public Builder skinCareHours(Double skinCareHours) { this.skinCareHours = skinCareHours; return this; }
        public Builder functionalRankDomestic(Integer functionalRankDomestic) { this.functionalRankDomestic = functionalRankDomestic; return this; }
        public Builder functionalRankRelated(Integer functionalRankRelated) { this.functionalRankRelated = functionalRankRelated; return this; }
        public Builder functionalRankPersonal(Integer functionalRankPersonal) { this.functionalRankPersonal = functionalRankPersonal; return this; }
        public Builder functionalRankParamedical(Integer functionalRankParamedical) { this.functionalRankParamedical = functionalRankParamedical; return this; }
        public Builder htgDomestic(String htgDomestic) { this.htgDomestic = htgDomestic; return this; }
        public Builder htgRelated(String htgRelated) { this.htgRelated = htgRelated; return this; }
        public Builder htgPersonal(String htgPersonal) { this.htgPersonal = htgPersonal; return this; }
        public Builder htgParamedical(String htgParamedical) { this.htgParamedical = htgParamedical; return this; }
        public Builder adjustmentsHours(Double adjustmentsHours) { this.adjustmentsHours = adjustmentsHours; return this; }
        public Builder prorationFactor(Double prorationFactor) { this.prorationFactor = prorationFactor; return this; }
        public Builder netIncome(Double netIncome) { this.netIncome = netIncome; return this; }
        public Builder countableIncome(Double countableIncome) { this.countableIncome = countableIncome; return this; }
        public Builder ihssShareOfCost(Double ihssShareOfCost) { this.ihssShareOfCost = ihssShareOfCost; return this; }
        public Builder waiverProgram(String waiverProgram) { this.waiverProgram = waiverProgram; return this; }
        public Builder recipientDeclinesCfco(Boolean recipientDeclinesCfco) { this.recipientDeclinesCfco = recipientDeclinesCfco; return this; }
        public Builder reinstatedHours(Double reinstatedHours) { this.reinstatedHours = reinstatedHours; return this; }
        public Builder socialWorkerCertification(String socialWorkerCertification) { this.socialWorkerCertification = socialWorkerCertification; return this; }
        public Builder verifiedByCaseOwnerOrSupervisor(Boolean verifiedByCaseOwnerOrSupervisor) { this.verifiedByCaseOwnerOrSupervisor = verifiedByCaseOwnerOrSupervisor; return this; }
        public Builder advancePayIndicated(Boolean advancePayIndicated) { this.advancePayIndicated = advancePayIndicated; return this; }
        public Builder advancePayRate(Double advancePayRate) { this.advancePayRate = advancePayRate; return this; }
        public Builder countyIpRate(Double countyIpRate) { this.countyIpRate = countyIpRate; return this; }
        public Builder countyPayRateEffectiveDate(LocalDate countyPayRateEffectiveDate) { this.countyPayRateEffectiveDate = countyPayRateEffectiveDate; return this; }
        public Builder assessorId(String assessorId) { this.assessorId = assessorId; return this; }
        public Builder assessorName(String assessorName) { this.assessorName = assessorName; return this; }
        public Builder approvedById(String approvedById) { this.approvedById = approvedById; return this; }
        public Builder approvedByName(String approvedByName) { this.approvedByName = approvedByName; return this; }
        public Builder approvalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }

        public ServiceEligibilityEntity build() {
            return new ServiceEligibilityEntity(this);
        }
    }
}
