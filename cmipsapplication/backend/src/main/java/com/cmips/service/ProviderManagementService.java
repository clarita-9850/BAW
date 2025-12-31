package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.entity.ProviderEntity.ProviderStatus;
import com.cmips.entity.ProviderAssignmentEntity.AssignmentStatus;
import com.cmips.entity.OvertimeViolationEntity.ViolationStatus;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Provider Management Service
 * Implements business rules from DSD Section 23
 */
@Service
public class ProviderManagementService {

    private static final Logger log = LoggerFactory.getLogger(ProviderManagementService.class);

    private final ProviderRepository providerRepository;
    private final ProviderCoriRepository providerCoriRepository;
    private final ProviderAssignmentRepository providerAssignmentRepository;
    private final OvertimeViolationRepository overtimeViolationRepository;
    private final CaseRepository caseRepository;
    private final TaskService taskService;

    public ProviderManagementService(
            ProviderRepository providerRepository,
            ProviderCoriRepository providerCoriRepository,
            ProviderAssignmentRepository providerAssignmentRepository,
            OvertimeViolationRepository overtimeViolationRepository,
            CaseRepository caseRepository,
            TaskService taskService) {
        this.providerRepository = providerRepository;
        this.providerCoriRepository = providerCoriRepository;
        this.providerAssignmentRepository = providerAssignmentRepository;
        this.overtimeViolationRepository = overtimeViolationRepository;
        this.caseRepository = caseRepository;
        this.taskService = taskService;
    }

    // ==================== PROVIDER ENROLLMENT ====================

    /**
     * Create a new provider (per BR PVM 01-03)
     */
    @Transactional
    public ProviderEntity createProvider(ProviderEntity provider, String userId) {
        // Generate provider number
        provider.setProviderNumber(generateProviderNumber());

        // Set initial status
        provider.setEligible("PENDING");
        provider.setProviderStatus(ProviderStatus.ACTIVE);

        // Per BR PVM 03 - Set SSN verification to Not Verified
        provider.setSsnVerificationStatus("NOT_YET_VERIFIED");

        provider.setCreatedBy(userId);

        // Names converted to uppercase in @PrePersist per BR PVM 20
        return providerRepository.save(provider);
    }

    /**
     * Approve provider enrollment
     */
    @Transactional
    public ProviderEntity approveEnrollment(Long providerId, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Validate enrollment requirements
        validateEnrollmentRequirements(provider);

        provider.setEligible("YES");
        provider.setEffectiveDate(LocalDate.now());
        provider.setIneligibleReason(null);
        provider.setOriginalHireDate(LocalDate.now());
        provider.setUpdatedBy(userId);

        // Per BR PVM 17 - Reset sick leave eligibility period
        if (provider.getSickLeaveEligibilityPeriodEnd() != null &&
            !provider.getSickLeaveEligibilityPeriodEnd().equals(LocalDate.of(9999, 12, 31))) {
            provider.setSickLeaveEligibilityPeriodEnd(LocalDate.of(9999, 12, 31));
        }

        return providerRepository.save(provider);
    }

    /**
     * Validate enrollment requirements (SOC 426, orientation, SOC 846, background check)
     */
    private void validateEnrollmentRequirements(ProviderEntity provider) {
        StringBuilder errors = new StringBuilder();

        if (!Boolean.TRUE.equals(provider.getSoc426Completed())) {
            errors.append("SOC 426 (Provider Enrollment Form) not completed. ");
        }
        if (!Boolean.TRUE.equals(provider.getOrientationCompleted())) {
            errors.append("Provider Orientation not completed. ");
        }
        if (!Boolean.TRUE.equals(provider.getSoc846Completed())) {
            errors.append("SOC 846 (Provider Enrollment Agreement) not completed. ");
        }
        // Per BR PVM 67-70 - Both Provider Agreement and Overtime Agreement required after FLSA
        if (!Boolean.TRUE.equals(provider.getProviderAgreementSigned())) {
            errors.append("Provider Agreement not signed. ");
        }
        if (!Boolean.TRUE.equals(provider.getOvertimeAgreementSigned())) {
            errors.append("Overtime Agreement not signed. ");
        }
        if (!Boolean.TRUE.equals(provider.getBackgroundCheckCompleted())) {
            errors.append("DOJ Background Check not completed. ");
        }

        // Check for CORI issues
        if (providerCoriRepository.hasActiveTier1Conviction(provider.getId())) {
            errors.append("Provider has active Tier 1 conviction. ");
        }
        if (providerCoriRepository.hasTier2WithoutWaiver(provider.getId())) {
            errors.append("Provider has Tier 2 conviction without waiver. ");
        }

        if (errors.length() > 0) {
            throw new RuntimeException("Enrollment requirements not met: " + errors.toString());
        }
    }

    /**
     * Set provider as ineligible (per BR PVM 15)
     */
    @Transactional
    public ProviderEntity setProviderIneligible(Long providerId, String ineligibleReason, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        LocalDate effectiveDate = LocalDate.now().plusDays(20);

        provider.setEligible("NO");
        provider.setIneligibleReason(ineligibleReason);
        provider.setEffectiveDate(effectiveDate);
        provider.setUpdatedBy(userId);

        // Terminate all active assignments
        terminateAllProviderAssignments(providerId, effectiveDate, "Provider Not Eligible to Work", userId);

        // End date workweek agreements (per BR PVM 73-76)
        // This would update WorkweekAgreement entities if we had them

        return providerRepository.save(provider);
    }

    /**
     * Reinstate provider (per BR PVM 23, 25)
     */
    @Transactional
    public ProviderEntity reinstateProvider(Long providerId, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Check if eligible for reinstatement (within 30 days per BR PVM 25)
        if (!provider.canBeReinstated()) {
            throw new RuntimeException("Provider cannot be reinstated - outside 30-day window or due to overtime violations");
        }

        provider.setEligible("PENDING_REINSTATEMENT");
        provider.setUpdatedBy(userId);

        return providerRepository.save(provider);
    }

    /**
     * Re-enroll provider (per BR PVM 24, 26)
     */
    @Transactional
    public ProviderEntity reEnrollProvider(Long providerId, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (!"NO".equals(provider.getEligible())) {
            throw new RuntimeException("Can only re-enroll ineligible providers");
        }

        // Clear enrollment information per BR PVM 24
        provider.setEligible("PENDING");
        provider.setIneligibleReason(null);
        provider.setSoc426Completed(false);
        provider.setSoc426Date(null);
        provider.setOrientationCompleted(false);
        provider.setOrientationDate(null);
        provider.setSoc846Completed(false);
        provider.setSoc846Date(null);
        provider.setProviderAgreementSigned(false);
        provider.setOvertimeAgreementSigned(false);
        provider.setBackgroundCheckCompleted(false);
        provider.setBackgroundCheckDate(null);
        provider.setUpdatedBy(userId);

        return providerRepository.save(provider);
    }

    // ==================== PROVIDER ASSIGNMENT ====================

    /**
     * Assign provider to case (per BR PVM 40-43)
     */
    @Transactional
    public ProviderAssignmentEntity assignProviderToCase(Long providerId, Long caseId, String providerType,
                                                          String relationship, Double assignedHours, String userId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (!provider.isEligibleToServe()) {
            throw new RuntimeException("Provider is not eligible to serve");
        }

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        ProviderAssignmentEntity assignment = ProviderAssignmentEntity.builder()
                .caseId(caseId)
                .providerId(providerId)
                .recipientId(caseEntity.getRecipientId())
                .providerType(providerType)
                .status(AssignmentStatus.ACTIVE)
                .beginDate(LocalDate.now())
                .relationshipToRecipient(relationship)
                .assignedHours(assignedHours)
                .assignedHoursFormIndicated(assignedHours != null)
                .impactsFundingSource(checkFundingSourceImpact(relationship))
                .createdBy(userId)
                .build();

        assignment = providerAssignmentRepository.save(assignment);

        // Update provider active case count
        updateProviderActiveCaseCount(providerId);

        // Per BR PVM 40 - Generate SOC 2271 notification
        assignment.setInitialNotificationSent(false);

        // Per BR PVM 13 - Update funding source if spouse or parent of minor
        if (assignment.getImpactsFundingSource()) {
            updateCaseFundingSource(caseId, relationship, userId);
        }

        log.info("Provider {} assigned to case {}", providerId, caseId);
        return assignment;
    }

    /**
     * Terminate provider from case (per BR PVM 44-47)
     */
    @Transactional
    public ProviderAssignmentEntity terminateProviderAssignment(Long assignmentId, String reason, String userId) {
        ProviderAssignmentEntity assignment = providerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setStatus(AssignmentStatus.TERMINATED);
        assignment.setLeaveTerminationEffectiveDate(LocalDate.now());
        assignment.setTerminationReason(reason);
        assignment.setUpdatedBy(userId);

        assignment = providerAssignmentRepository.save(assignment);

        // Update provider active case count
        updateProviderActiveCaseCount(assignment.getProviderId());

        return assignment;
    }

    /**
     * Place provider on leave for a case
     */
    @Transactional
    public ProviderAssignmentEntity placeProviderOnLeave(Long assignmentId, String reason, String userId) {
        ProviderAssignmentEntity assignment = providerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setStatus(AssignmentStatus.ON_LEAVE);
        assignment.setLeaveTerminationEffectiveDate(LocalDate.now());
        assignment.setTerminationReason(reason);
        assignment.setUpdatedBy(userId);

        return providerAssignmentRepository.save(assignment);
    }

    // ==================== CORI MANAGEMENT ====================

    /**
     * Get all CORI records for a provider
     */
    public List<ProviderCoriEntity> getProviderCoriRecords(Long providerId) {
        return providerCoriRepository.findByProviderId(providerId);
    }

    /**
     * Create CORI record (per BR PVM 31)
     */
    @Transactional
    public ProviderCoriEntity createCori(Long providerId, ProviderCoriEntity cori, String userId) {
        cori.setProviderId(providerId);
        cori.setCreatedBy(userId);
        cori = providerCoriRepository.save(cori);

        // Per BR PVM 31 - Set provider ineligible
        String ineligibleReason;
        Long coriCount = providerCoriRepository.countActiveCoriByProviderId(providerId);

        if ("TIER_1".equals(cori.getTier())) {
            ineligibleReason = coriCount > 1 ? "SUBSEQUENT_TIER_1_CONVICTION" : "TIER_1_CONVICTION";
        } else {
            ineligibleReason = coriCount > 1 ? "SUBSEQUENT_TIER_2_CONVICTION" : "TIER_2_CONVICTION";
        }

        setProviderIneligible(providerId, ineligibleReason, userId);

        return cori;
    }

    /**
     * Add general exception waiver to CORI (per BR PVM 33)
     */
    @Transactional
    public ProviderCoriEntity addGeneralException(Long coriId, LocalDate beginDate, LocalDate endDate, String notes, String userId) {
        ProviderCoriEntity cori = providerCoriRepository.findById(coriId)
                .orElseThrow(() -> new RuntimeException("CORI not found"));

        cori.setGeneralExceptionGranted(true);
        cori.setGeneralExceptionBeginDate(beginDate);
        cori.setGeneralExceptionEndDate(endDate);
        cori.setGeneralExceptionNotes(notes);
        cori.setUpdatedBy(userId);

        cori = providerCoriRepository.save(cori);

        // Per BR PVM 33 - Set provider eligible if all other requirements met
        ProviderEntity provider = providerRepository.findById(cori.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        if (!providerCoriRepository.hasActiveTier1Conviction(provider.getId()) &&
            !providerCoriRepository.hasTier2WithoutWaiver(provider.getId())) {
            // Check other enrollment requirements and set eligible if met
            try {
                validateEnrollmentRequirements(provider);
                provider.setEligible("YES");
                provider.setIneligibleReason(null);
                providerRepository.save(provider);
            } catch (Exception e) {
                // Other requirements not met, keep ineligible
                log.info("Provider {} has general exception but other requirements not met: {}", provider.getId(), e.getMessage());
            }
        }

        return cori;
    }

    /**
     * Add recipient waiver (per BR PVM 35)
     */
    @Transactional
    public ProviderAssignmentEntity addRecipientWaiver(Long assignmentId, Long coriId, LocalDate endDate, String userId) {
        ProviderAssignmentEntity assignment = providerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setHasRecipientWaiver(true);
        assignment.setRecipientWaiverCoriId(coriId);
        assignment.setRecipientWaiverBeginDate(LocalDate.now());
        assignment.setRecipientWaiverEndDate(endDate);
        assignment.setUpdatedBy(userId);

        return providerAssignmentRepository.save(assignment);
    }

    // ==================== OVERTIME VIOLATIONS ====================

    /**
     * Create overtime violation (per BR PVM 90+)
     */
    @Transactional
    public OvertimeViolationEntity createOvertimeViolation(Long providerId, String violationType,
                                                            Integer month, Integer year,
                                                            Double hoursClaimed, Double maximumAllowed, String userId) {
        // Check if violation already exists for this service month (one per month rule)
        if (overtimeViolationRepository.hasViolationForServiceMonth(providerId, month, year)) {
            throw new RuntimeException("Provider already has a violation for this service month");
        }

        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Check if provider has overtime exemption
        if (Boolean.TRUE.equals(provider.getHasOvertimeExemption())) {
            // Create inactive exemption violation
            OvertimeViolationEntity violation = OvertimeViolationEntity.builder()
                    .providerId(providerId)
                    .violationType(violationType)
                    .serviceMonth(month)
                    .serviceYear(year)
                    .violationDate(LocalDate.now())
                    .hoursClaimed(hoursClaimed)
                    .maximumAllowed(maximumAllowed)
                    .hoursExceeded(hoursClaimed - maximumAllowed)
                    .status(ViolationStatus.INACTIVE_EXEMPTION)
                    .createdBy(userId)
                    .build();
            return overtimeViolationRepository.save(violation);
        }

        // Determine violation number
        Integer activeViolationCount = overtimeViolationRepository.countActiveViolationsByProviderId(providerId);
        Integer violationNumber = activeViolationCount + 1;

        OvertimeViolationEntity violation = OvertimeViolationEntity.builder()
                .providerId(providerId)
                .violationNumber(violationNumber)
                .violationType(violationType)
                .serviceMonth(month)
                .serviceYear(year)
                .violationDate(LocalDate.now())
                .hoursClaimed(hoursClaimed)
                .maximumAllowed(maximumAllowed)
                .hoursExceeded(hoursClaimed - maximumAllowed)
                .status(ViolationStatus.PENDING_REVIEW)
                .createdBy(userId)
                .build();

        // If violation #2, set up training
        if (violationNumber == 2 && !Boolean.TRUE.equals(provider.getTrainingCompletedForViolation2())) {
            violation.setTrainingOffered(true);
            violation.setTrainingDueDate(LocalDate.now().plusDays(14));
        }

        violation = overtimeViolationRepository.save(violation);

        // Create task for county review (3 business days)
        createOvertimeViolationReviewTask(violation, userId);

        return violation;
    }

    /**
     * County review of violation (per BR PVM 90+)
     */
    @Transactional
    public OvertimeViolationEntity countyReviewViolation(Long violationId, String outcome, String comments, String reviewerId) {
        OvertimeViolationEntity violation = overtimeViolationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found"));

        violation.setCountyReviewDate(LocalDate.now());
        violation.setCountyReviewerId(reviewerId);
        violation.setCountyReviewOutcome(outcome);
        violation.setCountyReviewComments(comments);

        if ("UPHELD".equals(outcome)) {
            violation.setStatus(ViolationStatus.ACTIVE);
            // Process consequences based on violation number
            processViolationConsequences(violation, reviewerId);
        } else if ("OVERRIDE_REQUESTED".equals(outcome)) {
            // Create task for supervisor review
            createSupervisorReviewTask(violation, reviewerId);
        }

        return overtimeViolationRepository.save(violation);
    }

    /**
     * Supervisor review of violation override request
     */
    @Transactional
    public OvertimeViolationEntity supervisorReviewViolation(Long violationId, String outcome, String comments, String reviewerId) {
        OvertimeViolationEntity violation = overtimeViolationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found"));

        violation.setSupervisorReviewDate(LocalDate.now());
        violation.setSupervisorReviewerId(reviewerId);
        violation.setSupervisorReviewOutcome(outcome);
        violation.setSupervisorReviewComments(comments);

        if ("APPROVED".equals(outcome)) {
            violation.setStatus(ViolationStatus.INACTIVE);
        } else if ("REJECTED".equals(outcome)) {
            violation.setStatus(ViolationStatus.ACTIVE);
            processViolationConsequences(violation, reviewerId);
        }

        return overtimeViolationRepository.save(violation);
    }

    /**
     * Record training completion for Violation #2
     */
    @Transactional
    public OvertimeViolationEntity recordTrainingCompletion(Long violationId, String userId) {
        OvertimeViolationEntity violation = overtimeViolationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found"));

        if (violation.getViolationNumber() != 2) {
            throw new RuntimeException("Training only available for Violation #2");
        }

        violation.setTrainingCompleted(true);
        violation.setTrainingCompletionDate(LocalDate.now());
        violation.setTrainingCountyEntryDate(LocalDate.now());
        violation.setStatus(ViolationStatus.INACTIVE);

        // Update provider
        ProviderEntity provider = providerRepository.findById(violation.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        provider.setTrainingCompletedForViolation2(true);
        provider.setTrainingCompletionDate(LocalDate.now());
        providerRepository.save(provider);

        return overtimeViolationRepository.save(violation);
    }

    private void processViolationConsequences(OvertimeViolationEntity violation, String userId) {
        ProviderEntity provider = providerRepository.findById(violation.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        provider.setOvertimeViolationCount(violation.getViolationNumber());

        if (violation.getViolationNumber() >= 3) {
            // Violation #3: 90-day suspension, Violation #4: 365-day suspension
            int suspensionDays = violation.getSuspensionDays();
            LocalDate terminationDate = LocalDate.now().plusDays(20);
            LocalDate reinstatementDate = terminationDate.plusDays(suspensionDays);

            violation.setTerminationEffectiveDate(terminationDate);
            violation.setReinstatementDate(reinstatementDate);

            String ineligibleReason = violation.getViolationNumber() == 3 ?
                    "THIRD_OVERTIME_VIOLATION" : "FOURTH_OVERTIME_VIOLATION";

            provider.setEligible("NO");
            provider.setIneligibleReason(ineligibleReason);
            provider.setEffectiveDate(terminationDate);

            // Terminate all assignments
            terminateAllProviderAssignments(provider.getId(), terminationDate, "Provider Enrollment Ineligible", userId);
        }

        providerRepository.save(provider);
    }

    // ==================== SICK LEAVE ====================

    /**
     * Check and accrue sick leave (per User Story 5)
     */
    @Transactional
    public void checkAndAccrueSickLeave(Long providerId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Check if already accrued
        if (provider.getSickLeaveAccruedDate() != null) {
            return;
        }

        // Check if worked 100 hours
        if (provider.getTotalServiceHoursWorked() != null && provider.getTotalServiceHoursWorked() >= 100) {
            // Get fiscal year allocation
            int fiscalYear = LocalDate.now().getMonthValue() >= 7 ?
                    LocalDate.now().getYear() : LocalDate.now().getYear() - 1;

            double allocation;
            if (fiscalYear >= 2022) {
                allocation = 24.0; // 24 hours per fiscal year
            } else if (fiscalYear >= 2020) {
                allocation = 16.0;
            } else {
                allocation = 8.0;
            }

            provider.setSickLeaveAccruedHours(allocation);
            provider.setSickLeaveAccruedDate(LocalDate.now());
            providerRepository.save(provider);

            log.info("Provider {} accrued {} sick leave hours", providerId, allocation);
        }
    }

    // ==================== HELPER METHODS ====================

    private String generateProviderNumber() {
        return "P" + System.currentTimeMillis();
    }

    private void updateProviderActiveCaseCount(Long providerId) {
        Integer count = providerAssignmentRepository.countActiveCasesByProvider(providerId);
        ProviderEntity provider = providerRepository.findById(providerId).orElse(null);
        if (provider != null) {
            provider.setNumberOfActiveCases(count);
            providerRepository.save(provider);
        }
    }

    private void terminateAllProviderAssignments(Long providerId, LocalDate effectiveDate, String reason, String userId) {
        List<ProviderAssignmentEntity> assignments = providerAssignmentRepository
                .findByProviderIdAndStatus(providerId, AssignmentStatus.ACTIVE);

        for (ProviderAssignmentEntity assignment : assignments) {
            assignment.setStatus(AssignmentStatus.TERMINATED);
            assignment.setLeaveTerminationEffectiveDate(effectiveDate);
            assignment.setTerminationReason(reason);
            assignment.setUpdatedBy(userId);
            providerAssignmentRepository.save(assignment);
        }
    }

    private boolean checkFundingSourceImpact(String relationship) {
        return "SPOUSE".equals(relationship) || "PARENT_OF_MINOR".equals(relationship);
    }

    private void updateCaseFundingSource(Long caseId, String relationship, String userId) {
        CaseEntity caseEntity = caseRepository.findById(caseId).orElse(null);
        if (caseEntity != null) {
            // Per BR PVM 13 - Update to IPO if spouse or parent of minor
            if ("SPOUSE".equals(relationship) || "PARENT_OF_MINOR".equals(relationship)) {
                caseEntity.setFundingSource("IPO");
                caseEntity.setUpdatedBy(userId);
                caseRepository.save(caseEntity);
            }
        }
    }

    private void createOvertimeViolationReviewTask(OvertimeViolationEntity violation, String userId) {
        Task task = Task.builder()
                .title("Review Overtime Violation for Provider " + violation.getProviderId())
                .description("Overtime violation pending county review")
                .workQueue("COUNTY_OVERTIME_VIOLATION")
                .status(Task.TaskStatus.PENDING)
                .priority(Task.TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3).atStartOfDay())
                .build();

        Task savedTask = taskService.createTask(task);
        violation.setCountyReviewTaskId(savedTask.getId());
    }

    private void createSupervisorReviewTask(OvertimeViolationEntity violation, String userId) {
        Task task = Task.builder()
                .title("Supervisor Review for Provider Overtime Violation " + violation.getProviderId())
                .description("Override request pending supervisor review")
                .workQueue("SUPERVISOR_OVERTIME_VIOLATION")
                .status(Task.TaskStatus.PENDING)
                .priority(Task.TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(2).atStartOfDay())
                .build();

        Task savedTask = taskService.createTask(task);
        violation.setSupervisorReviewTaskId(savedTask.getId());
    }
}
