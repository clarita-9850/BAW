package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.entity.ServiceEligibilityEntity.AssessmentType;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service Eligibility Service
 * Implements business rules from DSD Section 21
 */
@Service
public class ServiceEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(ServiceEligibilityService.class);

    private final ServiceEligibilityRepository serviceEligibilityRepository;
    private final CaseRepository caseRepository;
    private final HealthCareCertificationRepository healthCareCertificationRepository;
    private final TaskService taskService;

    public ServiceEligibilityService(ServiceEligibilityRepository serviceEligibilityRepository,
                                     CaseRepository caseRepository,
                                     HealthCareCertificationRepository healthCareCertificationRepository,
                                     TaskService taskService) {
        this.serviceEligibilityRepository = serviceEligibilityRepository;
        this.caseRepository = caseRepository;
        this.healthCareCertificationRepository = healthCareCertificationRepository;
        this.taskService = taskService;
    }

    // ==================== ASSESSMENT MANAGEMENT ====================

    /**
     * Create a new service eligibility assessment (per BR SE 03-06)
     */
    @Transactional
    public ServiceEligibilityEntity createAssessment(Long caseId, AssessmentType assessmentType, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        ServiceEligibilityEntity assessment = ServiceEligibilityEntity.builder()
                .caseId(caseId)
                .recipientId(caseEntity.getRecipientId())
                .assessmentType(assessmentType)
                .assessmentDate(LocalDate.now())
                .status("PENDING")
                .assessorId(userId)
                .createdBy(userId)
                .build();

        // Apply date rules based on assessment type
        applyAssessmentTypeDateRules(assessment, caseId);

        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Apply date field rules based on assessment type (per BR SE 03-06, 50)
     */
    private void applyAssessmentTypeDateRules(ServiceEligibilityEntity assessment, Long caseId) {
        Optional<ServiceEligibilityEntity> lastActiveOpt = serviceEligibilityRepository.findActiveEligibilityByCaseId(caseId);

        switch (assessment.getAssessmentType()) {
            case CHANGE:
                // Per BR SE 03 - Copy dates from last active evidence
                if (lastActiveOpt.isPresent()) {
                    ServiceEligibilityEntity lastActive = lastActiveOpt.get();
                    assessment.setAuthorizationEndDate(lastActive.getAuthorizationEndDate());
                    assessment.setReassessmentDueDate(lastActive.getReassessmentDueDate());
                    assessment.setHomeVisitDate(lastActive.getHomeVisitDate());
                    // Copy waiver and reduced hours info per BR SE 20
                    assessment.setWaiverProgram(lastActive.getWaiverProgram());
                    assessment.setReinstatedHours(lastActive.getReinstatedHours());
                    assessment.setSocialWorkerCertification(lastActive.getSocialWorkerCertification());
                    assessment.setVerifiedByCaseOwnerOrSupervisor(true);
                }
                break;

            case INITIAL:
                // Per BR SE 04, 18 - All date fields blank
                // Waiver program and reduced hours reset
                assessment.setWaiverProgram(null);
                assessment.setReinstatedHours(null);
                assessment.setSocialWorkerCertification(null);
                assessment.setVerifiedByCaseOwnerOrSupervisor(false);
                break;

            case REASSESSMENT:
            case INTER_COUNTY_TRANSFER:
            case TELEHEALTH:
                // Per BR SE 05, 06, 50 - All date fields blank
                // Reassessment due date set to 1 year from home visit date if not user entered
                if (lastActiveOpt.isPresent()) {
                    ServiceEligibilityEntity lastActive = lastActiveOpt.get();
                    assessment.setWaiverProgram(lastActive.getWaiverProgram());
                }
                break;
        }
    }

    /**
     * Update assessment with home visit and calculate dates (per BR SE 04-06)
     */
    @Transactional
    public ServiceEligibilityEntity updateHomeVisitDate(Long assessmentId, LocalDate homeVisitDate, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        assessment.setHomeVisitDate(homeVisitDate);

        // Per BR SE 04, 05, 06, 50 - Set reassessment due date to 1 year from home visit if not user entered
        if (assessment.getReassessmentDueDate() == null &&
            assessment.getAssessmentType() != AssessmentType.CHANGE) {
            assessment.setDefaultReassessmentDueDate();
        }

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Calculate total assessed need (per BR SE 01)
     */
    @Transactional
    public ServiceEligibilityEntity calculateTotalAssessedNeed(Long assessmentId, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        Double totalHours = assessment.calculateTotalAssessedNeed();
        assessment.setTotalAuthorizedHoursMonthly(totalHours);
        assessment.setTotalAuthorizedHoursWeekly(totalHours / 4.33); // Monthly to weekly conversion

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Update service hours and calculate HTG indicators (per BR SE 07, 08)
     */
    @Transactional
    public ServiceEligibilityEntity updateServiceHours(Long assessmentId, ServiceHoursUpdate update, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        // Update individual service hours
        if (update.getDomesticServicesHours() != null) {
            assessment.setDomesticServicesHours(update.getDomesticServicesHours());
        }
        if (update.getRelatedServicesHours() != null) {
            assessment.setRelatedServicesHours(update.getRelatedServicesHours());
        }
        if (update.getPersonalCareHours() != null) {
            assessment.setPersonalCareHours(update.getPersonalCareHours());
        }
        if (update.getParamedicalHours() != null) {
            assessment.setParamedicalHours(update.getParamedicalHours());
        }
        // ... additional service types

        // Calculate HTG indicators
        calculateHtgIndicators(assessment);

        // Recalculate total
        assessment.setTotalAuthorizedHoursMonthly(assessment.calculateTotalAssessedNeed());

        // Per BR SE 23 - Reset verified flag if net adjusted need changes
        if (Boolean.TRUE.equals(assessment.getVerifiedByCaseOwnerOrSupervisor())) {
            assessment.setVerifiedByCaseOwnerOrSupervisor(false);
        }

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Calculate HTG indicators (per BR SE 07, 08)
     */
    private void calculateHtgIndicators(ServiceEligibilityEntity assessment) {
        // HTG lookup would typically come from a reference table
        // + if exceeds, - if below, blank if within

        // Example implementation - would need actual HTG values from reference table
        if (assessment.getFunctionalRankDomestic() != null && assessment.getDomesticServicesHours() != null) {
            Double htgLimit = getHtgLimit("DOMESTIC", assessment.getFunctionalRankDomestic());
            if (htgLimit != null) {
                if (assessment.getDomesticServicesHours() > htgLimit) {
                    assessment.setHtgDomestic("+");
                } else if (assessment.getDomesticServicesHours() < htgLimit * 0.8) {
                    assessment.setHtgDomestic("-");
                } else {
                    assessment.setHtgDomestic(null);
                }
            }
        }
        // Similar logic for other service types...
    }

    private Double getHtgLimit(String serviceType, Integer functionalRank) {
        // This would lookup from a reference table
        // Placeholder implementation
        return switch (serviceType) {
            case "DOMESTIC" -> functionalRank * 5.0;
            case "PERSONAL" -> functionalRank * 8.0;
            default -> null;
        };
    }

    /**
     * Set advance pay rate (per BR SE 09)
     */
    @Transactional
    public ServiceEligibilityEntity setAdvancePayRate(Long assessmentId, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (Boolean.TRUE.equals(assessment.getAdvancePayIndicated()) && assessment.getAdvancePayRate() == null) {
            // Per BR SE 09 - Set to highest county IP rate
            assessment.setAdvancePayRate(assessment.getCountyIpRate());
        }

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Update Share of Cost (per BR SE 13-15)
     */
    @Transactional
    public ServiceEligibilityEntity updateShareOfCost(Long assessmentId, Double netIncome, Double countableIncome, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        assessment.setNetIncome(netIncome);
        assessment.setCountableIncome(countableIncome);

        // Calculate IHSS Share of Cost (simplified - actual calculation would be more complex)
        // Per BR SE 13, 14, 15 - Reset if income evidence changes
        if (countableIncome != null) {
            // Placeholder calculation
            assessment.setIhssShareOfCost(Math.max(0, countableIncome - 1000));
        }

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Update waiver program and reset verification (per BR SE 22)
     */
    @Transactional
    public ServiceEligibilityEntity updateWaiverProgram(Long assessmentId, String waiverProgram, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        String oldWaiverProgram = assessment.getWaiverProgram();
        assessment.setWaiverProgram(waiverProgram);

        // Per BR SE 22 - Reset verified flag if waiver program changed
        if (oldWaiverProgram != null && !oldWaiverProgram.equals(waiverProgram) &&
            Boolean.TRUE.equals(assessment.getVerifiedByCaseOwnerOrSupervisor())) {
            assessment.setVerifiedByCaseOwnerOrSupervisor(false);
        }

        assessment.setUpdatedBy(userId);
        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Submit assessment for approval
     */
    @Transactional
    public ServiceEligibilityEntity submitForApproval(Long assessmentId, String userId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        assessment.setStatus("PENDING_APPROVAL");
        assessment.setUpdatedBy(userId);

        // Create approval task
        Task task = Task.builder()
                .title("Approve Assessment for Case " + assessment.getCaseId())
                .description("Service eligibility assessment pending approval")
                .workQueue("ASSESSMENT_APPROVAL")
                .status(Task.TaskStatus.PENDING)
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(5).atStartOfDay())
                .build();
        taskService.createTask(task);

        return serviceEligibilityRepository.save(assessment);
    }

    /**
     * Approve assessment and update case authorization
     */
    @Transactional
    public ServiceEligibilityEntity approveAssessment(Long assessmentId, String approverId) {
        ServiceEligibilityEntity assessment = serviceEligibilityRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        // Supersede any previous active assessment
        Optional<ServiceEligibilityEntity> previousActive = serviceEligibilityRepository.findActiveEligibilityByCaseId(assessment.getCaseId());
        if (previousActive.isPresent() && !previousActive.get().getId().equals(assessmentId)) {
            ServiceEligibilityEntity prev = previousActive.get();
            prev.setStatus("SUPERSEDED");
            serviceEligibilityRepository.save(prev);
        }

        assessment.setStatus("ACTIVE");
        assessment.setApprovedById(approverId);
        assessment.setApprovalDate(LocalDate.now());

        // Update case with new authorization info
        CaseEntity caseEntity = caseRepository.findById(assessment.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found"));
        caseEntity.setAuthorizedHoursMonthly(assessment.getTotalAuthorizedHoursMonthly());
        caseEntity.setAuthorizedHoursWeekly(assessment.getTotalAuthorizedHoursWeekly());
        caseEntity.setAuthorizationStartDate(assessment.getAuthorizationStartDate());
        caseEntity.setAuthorizationEndDate(assessment.getAuthorizationEndDate());
        caseEntity.setReassessmentDueDate(assessment.getReassessmentDueDate());
        caseEntity.setLastAssessmentDate(assessment.getAssessmentDate());
        caseEntity.setAssessmentType(assessment.getAssessmentType().name());
        caseRepository.save(caseEntity);

        return serviceEligibilityRepository.save(assessment);
    }

    // ==================== HEALTH CARE CERTIFICATION ====================

    /**
     * Create health care certification (per BR SE 28-50)
     */
    @Transactional
    public HealthCareCertificationEntity createHealthCareCertification(Long caseId, String method, String formType,
                                                                         String printOption, String language, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        HealthCareCertificationEntity cert = HealthCareCertificationEntity.builder()
                .caseId(caseId)
                .recipientId(caseEntity.getRecipientId())
                .certificationMethod(method)
                .formType(formType)
                .printOption(printOption)
                .language(language)
                .printDate(LocalDate.now())
                .status("ACTIVE")
                .createdBy(userId)
                .build();

        // Per BR SE 28 - Calculate initial due date (45 days from print date)
        cert.calculateInitialDueDate();

        if ("SEND_ESP".equals(printOption)) {
            cert.setSentToEsp(true);
            cert.setElectronicFormDueDate(cert.getDueDate());
        }

        cert = healthCareCertificationRepository.save(cert);

        // Create task triggers
        createHealthCareCertificationTasks(cert);

        return cert;
    }

    /**
     * Create task triggers for health care certification (per BR SE 28)
     */
    private void createHealthCareCertificationTasks(HealthCareCertificationEntity cert) {
        // First task: 10 business days before due date
        LocalDate firstTaskDate = cert.getDueDate().minusDays(14); // ~10 business days
        Task firstTask = Task.builder()
                .title("Health Care Certification Due Soon - Case " + cert.getCaseId())
                .description("SOC 873 due in 10 business days")
                .workQueue("HEALTH_CARE_CERTIFICATION")
                .status(Task.TaskStatus.PENDING)
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(firstTaskDate.atStartOfDay())
                .build();
        taskService.createTask(firstTask);

        // Second task: 1 business day before due date
        LocalDate secondTaskDate = cert.getDueDate().minusDays(1);
        Task secondTask = Task.builder()
                .title("Health Care Certification Due Tomorrow - Case " + cert.getCaseId())
                .description("SOC 873 due tomorrow - take action if not received")
                .workQueue("HEALTH_CARE_CERTIFICATION")
                .status(Task.TaskStatus.PENDING)
                .priority(Task.TaskPriority.HIGH)
                .dueDate(secondTaskDate.atStartOfDay())
                .build();
        taskService.createTask(secondTask);
    }

    /**
     * Record documentation received (per BR SE 32)
     */
    @Transactional
    public HealthCareCertificationEntity recordDocumentationReceived(Long certId, String certificationType,
                                                                      LocalDate receivedDate, String userId) {
        HealthCareCertificationEntity cert = healthCareCertificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        cert.setCertificationType(certificationType);
        cert.setDocumentationReceivedDate(receivedDate);
        cert.complete();
        cert.setUpdatedBy(userId);

        return healthCareCertificationRepository.save(cert);
    }

    /**
     * Request good cause extension (per BR SE 31)
     */
    @Transactional
    public HealthCareCertificationEntity requestGoodCauseExtension(Long certId, LocalDate extensionDate, String userId) {
        HealthCareCertificationEntity cert = healthCareCertificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        // Extension must be requested before initial due date
        if (cert.getDueDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot request extension after due date has passed");
        }

        cert.setGoodCauseExtensionRequested(true);
        cert.setGoodCauseExtensionDate(extensionDate);
        cert.calculateGoodCauseExtensionDueDate();
        cert.setUpdatedBy(userId);

        return healthCareCertificationRepository.save(cert);
    }

    /**
     * Grant exception (per BR SE 30)
     */
    @Transactional
    public HealthCareCertificationEntity grantException(Long certId, String exceptionReason,
                                                         LocalDate exceptionDate, String userId) {
        HealthCareCertificationEntity cert = healthCareCertificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        cert.setExceptionGranted(true);
        cert.setExceptionReason(exceptionReason);
        cert.setExceptionGrantedDate(exceptionDate);
        cert.calculateDueDateFromException();
        cert.setUpdatedBy(userId);

        return healthCareCertificationRepository.save(cert);
    }

    /**
     * Inactivate health care certification (per BR SE 34)
     */
    @Transactional
    public HealthCareCertificationEntity inactivateCertification(Long certId, String userId) {
        HealthCareCertificationEntity cert = healthCareCertificationRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        // Check if case has been authorized - cannot inactivate after authorization
        CaseEntity caseEntity = caseRepository.findById(cert.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found"));

        if (caseEntity.getCaseStatus() == CaseEntity.CaseStatus.ELIGIBLE ||
            caseEntity.getCaseStatus() == CaseEntity.CaseStatus.DENIED) {
            throw new RuntimeException("Cannot inactivate certification after case authorization");
        }

        cert.inactivate(userId);
        return healthCareCertificationRepository.save(cert);
    }

    // ==================== QUERIES ====================

    /**
     * Get evidence history (per BR SE 25)
     */
    public List<ServiceEligibilityEntity> getEvidenceHistory(Long caseId) {
        return serviceEligibilityRepository.findEvidenceHistoryByCaseId(caseId);
    }

    /**
     * Get active eligibility for case
     */
    public Optional<ServiceEligibilityEntity> getActiveEligibility(Long caseId) {
        return serviceEligibilityRepository.findActiveEligibilityByCaseId(caseId);
    }

    /**
     * Get pending approvals
     */
    public List<ServiceEligibilityEntity> getPendingApprovals() {
        return serviceEligibilityRepository.findPendingApprovals();
    }

    // ==================== DTOs ====================

    public static class ServiceHoursUpdate {
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

        public ServiceHoursUpdate() {}

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
    }
}
