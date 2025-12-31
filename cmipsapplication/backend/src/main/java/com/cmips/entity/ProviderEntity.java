package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provider Entity - Represents an IHSS Individual Provider (IP)
 * Based on DSD Section 23 - Provider Management User Stories and Business Rules
 */
@Entity
@Table(name = "providers")
public class ProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_number", unique = true, length = 20)
    private String providerNumber;

    // Basic Demographics (stored in UPPERCASE per BR PVM 20)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;

    // Identifiers
    @Column(name = "ssn", length = 11)
    private String ssn;

    // SSN Verification: NOT_YET_VERIFIED, VERIFIED, DECEASED, DUPLICATE_SSN, SUSPECT_SSN
    @Column(name = "ssn_verification_status", length = 50)
    private String ssnVerificationStatus;

    @Column(name = "taxpayer_id", length = 20)
    private String taxpayerId;

    // Contact Information
    @Column(name = "primary_phone", length = 20)
    private String primaryPhone;

    @Column(name = "secondary_phone", length = 20)
    private String secondaryPhone;

    @Column(name = "email", length = 200)
    private String email;

    // Address
    @Column(name = "street_address", length = 300)
    private String streetAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 2)
    private String state;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    // County (DOJ County per BR PVM 21, 27-29)
    @Column(name = "doj_county_code", length = 10)
    private String dojCountyCode;

    @Column(name = "doj_county_name", length = 100)
    private String dojCountyName;

    // Enrollment Status
    // PENDING, YES (Eligible), NO (Ineligible), PENDING_REINSTATEMENT
    @Column(name = "eligible", length = 30)
    private String eligible;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    // Ineligible Reasons per Business Rules
    // SUSPENDED_OR_INELIGIBLE, TIER_1_CONVICTION, TIER_2_CONVICTION, SUBSEQUENT_TIER_1_CONVICTION,
    // SUBSEQUENT_TIER_2_CONVICTION, DUPLICATE_SSN, SUSPECT_SSN, DEATH, INACTIVE_NO_PAYROLL_1_YEAR,
    // THIRD_OVERTIME_VIOLATION, FOURTH_OVERTIME_VIOLATION, SOC_846_NOT_COMPLETED, PROVIDER_ENROLLMENT_INELIGIBLE
    @Column(name = "ineligible_reason", length = 100)
    private String ineligibleReason;

    // Provider Status: ACTIVE, ON_LEAVE, TERMINATED
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_status")
    private ProviderStatus providerStatus;

    @Column(name = "leave_termination_effective_date")
    private LocalDate leaveTerminationEffectiveDate;

    @Column(name = "termination_reason", length = 500)
    private String terminationReason;

    // Enrollment Requirements (SOC Forms)
    @Column(name = "soc_426_completed")
    private Boolean soc426Completed; // Provider Enrollment Form

    @Column(name = "soc_426_date")
    private LocalDate soc426Date;

    @Column(name = "orientation_completed")
    private Boolean orientationCompleted;

    @Column(name = "orientation_date")
    private LocalDate orientationDate;

    @Column(name = "soc_846_completed")
    private Boolean soc846Completed; // Provider Enrollment Agreement

    @Column(name = "soc_846_date")
    private LocalDate soc846Date;

    @Column(name = "provider_agreement_signed")
    private Boolean providerAgreementSigned;

    @Column(name = "overtime_agreement_signed")
    private Boolean overtimeAgreementSigned;

    // Background Check (DOJ)
    @Column(name = "background_check_completed")
    private Boolean backgroundCheckCompleted;

    @Column(name = "background_check_date")
    private LocalDate backgroundCheckDate;

    @Column(name = "background_check_status", length = 50)
    private String backgroundCheckStatus;

    // Medi-Cal Suspended/Ineligible (per BR PVM 09)
    @Column(name = "medi_cal_suspended")
    private Boolean mediCalSuspended;

    @Column(name = "medi_cal_suspended_begin_date")
    private LocalDate mediCalSuspendedBeginDate;

    @Column(name = "medi_cal_suspended_end_date")
    private LocalDate mediCalSuspendedEndDate;

    // Public Authority (PA) Registration (per BR PVM 14)
    @Column(name = "pa_registered")
    private Boolean paRegistered;

    @Column(name = "pa_training_completed")
    private Boolean paTrainingCompleted;

    @Column(name = "pa_fingerprinting_completed")
    private Boolean paFingerprintingCompleted;

    // Original Hire Date (per business rules)
    @Column(name = "original_hire_date")
    private LocalDate originalHireDate;

    // Electronic Services
    @Column(name = "esp_registered")
    private Boolean espRegistered;

    @Column(name = "e_timesheet_status", length = 50)
    private String eTimesheetStatus; // ENROLLED, REQUEST_PENDING, NOT_ENROLLED

    // Overtime Violation Tracking
    @Column(name = "overtime_violation_count")
    private Integer overtimeViolationCount;

    @Column(name = "next_possible_violation_date")
    private LocalDate nextPossibleViolationDate;

    @Column(name = "training_completed_for_violation_2")
    private Boolean trainingCompletedForViolation2;

    @Column(name = "training_completion_date")
    private LocalDate trainingCompletionDate;

    // Overtime Exemption
    @Column(name = "has_overtime_exemption")
    private Boolean hasOvertimeExemption;

    @Column(name = "overtime_exemption_begin_date")
    private LocalDate overtimeExemptionBeginDate;

    @Column(name = "overtime_exemption_end_date")
    private LocalDate overtimeExemptionEndDate;

    // Sick Leave Eligibility (per User Story 5)
    @Column(name = "sick_leave_accrued_hours")
    private Double sickLeaveAccruedHours;

    @Column(name = "sick_leave_accrued_date")
    private LocalDate sickLeaveAccruedDate;

    @Column(name = "sick_leave_eligible_date")
    private LocalDate sickLeaveEligibleDate;

    @Column(name = "sick_leave_eligibility_period_start")
    private LocalDate sickLeaveEligibilityPeriodStart;

    @Column(name = "sick_leave_eligibility_period_end")
    private LocalDate sickLeaveEligibilityPeriodEnd;

    @Column(name = "total_service_hours_worked")
    private Double totalServiceHoursWorked;

    // Number of Active Cases (per BR PVM 71)
    @Column(name = "number_of_active_cases")
    private Integer numberOfActiveCases;

    // Weekly/Monthly Maximum Calculations
    @Column(name = "weekly_maximum_hours")
    private Double weeklyMaximumHours;

    @Column(name = "monthly_maximum_hours")
    private Double monthlyMaximumHours;

    // Death Information
    @Column(name = "deceased")
    private Boolean deceased;

    @Column(name = "date_of_death")
    private LocalDate dateOfDeath;

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
    public ProviderEntity() {
    }

    // All-args constructor
    public ProviderEntity(Long id, String providerNumber, String firstName, String lastName, String middleName,
                          LocalDate dateOfBirth, String gender, String ssn, String ssnVerificationStatus,
                          String taxpayerId, String primaryPhone, String secondaryPhone, String email,
                          String streetAddress, String city, String state, String zipCode, String dojCountyCode,
                          String dojCountyName, String eligible, LocalDate effectiveDate, String ineligibleReason,
                          ProviderStatus providerStatus, LocalDate leaveTerminationEffectiveDate, String terminationReason,
                          Boolean soc426Completed, LocalDate soc426Date, Boolean orientationCompleted,
                          LocalDate orientationDate, Boolean soc846Completed, LocalDate soc846Date,
                          Boolean providerAgreementSigned, Boolean overtimeAgreementSigned,
                          Boolean backgroundCheckCompleted, LocalDate backgroundCheckDate, String backgroundCheckStatus,
                          Boolean mediCalSuspended, LocalDate mediCalSuspendedBeginDate, LocalDate mediCalSuspendedEndDate,
                          Boolean paRegistered, Boolean paTrainingCompleted, Boolean paFingerprintingCompleted,
                          LocalDate originalHireDate, Boolean espRegistered, String eTimesheetStatus,
                          Integer overtimeViolationCount, LocalDate nextPossibleViolationDate,
                          Boolean trainingCompletedForViolation2, LocalDate trainingCompletionDate,
                          Boolean hasOvertimeExemption, LocalDate overtimeExemptionBeginDate,
                          LocalDate overtimeExemptionEndDate, Double sickLeaveAccruedHours, LocalDate sickLeaveAccruedDate,
                          LocalDate sickLeaveEligibleDate, LocalDate sickLeaveEligibilityPeriodStart,
                          LocalDate sickLeaveEligibilityPeriodEnd, Double totalServiceHoursWorked,
                          Integer numberOfActiveCases, Double weeklyMaximumHours, Double monthlyMaximumHours,
                          Boolean deceased, LocalDate dateOfDeath, LocalDateTime createdAt, String createdBy,
                          LocalDateTime updatedAt, String updatedBy) {
        this.id = id;
        this.providerNumber = providerNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.ssn = ssn;
        this.ssnVerificationStatus = ssnVerificationStatus;
        this.taxpayerId = taxpayerId;
        this.primaryPhone = primaryPhone;
        this.secondaryPhone = secondaryPhone;
        this.email = email;
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.dojCountyCode = dojCountyCode;
        this.dojCountyName = dojCountyName;
        this.eligible = eligible;
        this.effectiveDate = effectiveDate;
        this.ineligibleReason = ineligibleReason;
        this.providerStatus = providerStatus;
        this.leaveTerminationEffectiveDate = leaveTerminationEffectiveDate;
        this.terminationReason = terminationReason;
        this.soc426Completed = soc426Completed;
        this.soc426Date = soc426Date;
        this.orientationCompleted = orientationCompleted;
        this.orientationDate = orientationDate;
        this.soc846Completed = soc846Completed;
        this.soc846Date = soc846Date;
        this.providerAgreementSigned = providerAgreementSigned;
        this.overtimeAgreementSigned = overtimeAgreementSigned;
        this.backgroundCheckCompleted = backgroundCheckCompleted;
        this.backgroundCheckDate = backgroundCheckDate;
        this.backgroundCheckStatus = backgroundCheckStatus;
        this.mediCalSuspended = mediCalSuspended;
        this.mediCalSuspendedBeginDate = mediCalSuspendedBeginDate;
        this.mediCalSuspendedEndDate = mediCalSuspendedEndDate;
        this.paRegistered = paRegistered;
        this.paTrainingCompleted = paTrainingCompleted;
        this.paFingerprintingCompleted = paFingerprintingCompleted;
        this.originalHireDate = originalHireDate;
        this.espRegistered = espRegistered;
        this.eTimesheetStatus = eTimesheetStatus;
        this.overtimeViolationCount = overtimeViolationCount;
        this.nextPossibleViolationDate = nextPossibleViolationDate;
        this.trainingCompletedForViolation2 = trainingCompletedForViolation2;
        this.trainingCompletionDate = trainingCompletionDate;
        this.hasOvertimeExemption = hasOvertimeExemption;
        this.overtimeExemptionBeginDate = overtimeExemptionBeginDate;
        this.overtimeExemptionEndDate = overtimeExemptionEndDate;
        this.sickLeaveAccruedHours = sickLeaveAccruedHours;
        this.sickLeaveAccruedDate = sickLeaveAccruedDate;
        this.sickLeaveEligibleDate = sickLeaveEligibleDate;
        this.sickLeaveEligibilityPeriodStart = sickLeaveEligibilityPeriodStart;
        this.sickLeaveEligibilityPeriodEnd = sickLeaveEligibilityPeriodEnd;
        this.totalServiceHoursWorked = totalServiceHoursWorked;
        this.numberOfActiveCases = numberOfActiveCases;
        this.weeklyMaximumHours = weeklyMaximumHours;
        this.monthlyMaximumHours = monthlyMaximumHours;
        this.deceased = deceased;
        this.dateOfDeath = dateOfDeath;
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

    public String getProviderNumber() {
        return providerNumber;
    }

    public void setProviderNumber(String providerNumber) {
        this.providerNumber = providerNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getSsnVerificationStatus() {
        return ssnVerificationStatus;
    }

    public void setSsnVerificationStatus(String ssnVerificationStatus) {
        this.ssnVerificationStatus = ssnVerificationStatus;
    }

    public String getTaxpayerId() {
        return taxpayerId;
    }

    public void setTaxpayerId(String taxpayerId) {
        this.taxpayerId = taxpayerId;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public String getSecondaryPhone() {
        return secondaryPhone;
    }

    public void setSecondaryPhone(String secondaryPhone) {
        this.secondaryPhone = secondaryPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getDojCountyCode() {
        return dojCountyCode;
    }

    public void setDojCountyCode(String dojCountyCode) {
        this.dojCountyCode = dojCountyCode;
    }

    public String getDojCountyName() {
        return dojCountyName;
    }

    public void setDojCountyName(String dojCountyName) {
        this.dojCountyName = dojCountyName;
    }

    public String getEligible() {
        return eligible;
    }

    public void setEligible(String eligible) {
        this.eligible = eligible;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getIneligibleReason() {
        return ineligibleReason;
    }

    public void setIneligibleReason(String ineligibleReason) {
        this.ineligibleReason = ineligibleReason;
    }

    public ProviderStatus getProviderStatus() {
        return providerStatus;
    }

    public void setProviderStatus(ProviderStatus providerStatus) {
        this.providerStatus = providerStatus;
    }

    public LocalDate getLeaveTerminationEffectiveDate() {
        return leaveTerminationEffectiveDate;
    }

    public void setLeaveTerminationEffectiveDate(LocalDate leaveTerminationEffectiveDate) {
        this.leaveTerminationEffectiveDate = leaveTerminationEffectiveDate;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public Boolean getSoc426Completed() {
        return soc426Completed;
    }

    public void setSoc426Completed(Boolean soc426Completed) {
        this.soc426Completed = soc426Completed;
    }

    public LocalDate getSoc426Date() {
        return soc426Date;
    }

    public void setSoc426Date(LocalDate soc426Date) {
        this.soc426Date = soc426Date;
    }

    public Boolean getOrientationCompleted() {
        return orientationCompleted;
    }

    public void setOrientationCompleted(Boolean orientationCompleted) {
        this.orientationCompleted = orientationCompleted;
    }

    public LocalDate getOrientationDate() {
        return orientationDate;
    }

    public void setOrientationDate(LocalDate orientationDate) {
        this.orientationDate = orientationDate;
    }

    public Boolean getSoc846Completed() {
        return soc846Completed;
    }

    public void setSoc846Completed(Boolean soc846Completed) {
        this.soc846Completed = soc846Completed;
    }

    public LocalDate getSoc846Date() {
        return soc846Date;
    }

    public void setSoc846Date(LocalDate soc846Date) {
        this.soc846Date = soc846Date;
    }

    public Boolean getProviderAgreementSigned() {
        return providerAgreementSigned;
    }

    public void setProviderAgreementSigned(Boolean providerAgreementSigned) {
        this.providerAgreementSigned = providerAgreementSigned;
    }

    public Boolean getOvertimeAgreementSigned() {
        return overtimeAgreementSigned;
    }

    public void setOvertimeAgreementSigned(Boolean overtimeAgreementSigned) {
        this.overtimeAgreementSigned = overtimeAgreementSigned;
    }

    public Boolean getBackgroundCheckCompleted() {
        return backgroundCheckCompleted;
    }

    public void setBackgroundCheckCompleted(Boolean backgroundCheckCompleted) {
        this.backgroundCheckCompleted = backgroundCheckCompleted;
    }

    public LocalDate getBackgroundCheckDate() {
        return backgroundCheckDate;
    }

    public void setBackgroundCheckDate(LocalDate backgroundCheckDate) {
        this.backgroundCheckDate = backgroundCheckDate;
    }

    public String getBackgroundCheckStatus() {
        return backgroundCheckStatus;
    }

    public void setBackgroundCheckStatus(String backgroundCheckStatus) {
        this.backgroundCheckStatus = backgroundCheckStatus;
    }

    public Boolean getMediCalSuspended() {
        return mediCalSuspended;
    }

    public void setMediCalSuspended(Boolean mediCalSuspended) {
        this.mediCalSuspended = mediCalSuspended;
    }

    public LocalDate getMediCalSuspendedBeginDate() {
        return mediCalSuspendedBeginDate;
    }

    public void setMediCalSuspendedBeginDate(LocalDate mediCalSuspendedBeginDate) {
        this.mediCalSuspendedBeginDate = mediCalSuspendedBeginDate;
    }

    public LocalDate getMediCalSuspendedEndDate() {
        return mediCalSuspendedEndDate;
    }

    public void setMediCalSuspendedEndDate(LocalDate mediCalSuspendedEndDate) {
        this.mediCalSuspendedEndDate = mediCalSuspendedEndDate;
    }

    public Boolean getPaRegistered() {
        return paRegistered;
    }

    public void setPaRegistered(Boolean paRegistered) {
        this.paRegistered = paRegistered;
    }

    public Boolean getPaTrainingCompleted() {
        return paTrainingCompleted;
    }

    public void setPaTrainingCompleted(Boolean paTrainingCompleted) {
        this.paTrainingCompleted = paTrainingCompleted;
    }

    public Boolean getPaFingerprintingCompleted() {
        return paFingerprintingCompleted;
    }

    public void setPaFingerprintingCompleted(Boolean paFingerprintingCompleted) {
        this.paFingerprintingCompleted = paFingerprintingCompleted;
    }

    public LocalDate getOriginalHireDate() {
        return originalHireDate;
    }

    public void setOriginalHireDate(LocalDate originalHireDate) {
        this.originalHireDate = originalHireDate;
    }

    public Boolean getEspRegistered() {
        return espRegistered;
    }

    public void setEspRegistered(Boolean espRegistered) {
        this.espRegistered = espRegistered;
    }

    public String geteTimesheetStatus() {
        return eTimesheetStatus;
    }

    public void seteTimesheetStatus(String eTimesheetStatus) {
        this.eTimesheetStatus = eTimesheetStatus;
    }

    public Integer getOvertimeViolationCount() {
        return overtimeViolationCount;
    }

    public void setOvertimeViolationCount(Integer overtimeViolationCount) {
        this.overtimeViolationCount = overtimeViolationCount;
    }

    public LocalDate getNextPossibleViolationDate() {
        return nextPossibleViolationDate;
    }

    public void setNextPossibleViolationDate(LocalDate nextPossibleViolationDate) {
        this.nextPossibleViolationDate = nextPossibleViolationDate;
    }

    public Boolean getTrainingCompletedForViolation2() {
        return trainingCompletedForViolation2;
    }

    public void setTrainingCompletedForViolation2(Boolean trainingCompletedForViolation2) {
        this.trainingCompletedForViolation2 = trainingCompletedForViolation2;
    }

    public LocalDate getTrainingCompletionDate() {
        return trainingCompletionDate;
    }

    public void setTrainingCompletionDate(LocalDate trainingCompletionDate) {
        this.trainingCompletionDate = trainingCompletionDate;
    }

    public Boolean getHasOvertimeExemption() {
        return hasOvertimeExemption;
    }

    public void setHasOvertimeExemption(Boolean hasOvertimeExemption) {
        this.hasOvertimeExemption = hasOvertimeExemption;
    }

    public LocalDate getOvertimeExemptionBeginDate() {
        return overtimeExemptionBeginDate;
    }

    public void setOvertimeExemptionBeginDate(LocalDate overtimeExemptionBeginDate) {
        this.overtimeExemptionBeginDate = overtimeExemptionBeginDate;
    }

    public LocalDate getOvertimeExemptionEndDate() {
        return overtimeExemptionEndDate;
    }

    public void setOvertimeExemptionEndDate(LocalDate overtimeExemptionEndDate) {
        this.overtimeExemptionEndDate = overtimeExemptionEndDate;
    }

    public Double getSickLeaveAccruedHours() {
        return sickLeaveAccruedHours;
    }

    public void setSickLeaveAccruedHours(Double sickLeaveAccruedHours) {
        this.sickLeaveAccruedHours = sickLeaveAccruedHours;
    }

    public LocalDate getSickLeaveAccruedDate() {
        return sickLeaveAccruedDate;
    }

    public void setSickLeaveAccruedDate(LocalDate sickLeaveAccruedDate) {
        this.sickLeaveAccruedDate = sickLeaveAccruedDate;
    }

    public LocalDate getSickLeaveEligibleDate() {
        return sickLeaveEligibleDate;
    }

    public void setSickLeaveEligibleDate(LocalDate sickLeaveEligibleDate) {
        this.sickLeaveEligibleDate = sickLeaveEligibleDate;
    }

    public LocalDate getSickLeaveEligibilityPeriodStart() {
        return sickLeaveEligibilityPeriodStart;
    }

    public void setSickLeaveEligibilityPeriodStart(LocalDate sickLeaveEligibilityPeriodStart) {
        this.sickLeaveEligibilityPeriodStart = sickLeaveEligibilityPeriodStart;
    }

    public LocalDate getSickLeaveEligibilityPeriodEnd() {
        return sickLeaveEligibilityPeriodEnd;
    }

    public void setSickLeaveEligibilityPeriodEnd(LocalDate sickLeaveEligibilityPeriodEnd) {
        this.sickLeaveEligibilityPeriodEnd = sickLeaveEligibilityPeriodEnd;
    }

    public Double getTotalServiceHoursWorked() {
        return totalServiceHoursWorked;
    }

    public void setTotalServiceHoursWorked(Double totalServiceHoursWorked) {
        this.totalServiceHoursWorked = totalServiceHoursWorked;
    }

    public Integer getNumberOfActiveCases() {
        return numberOfActiveCases;
    }

    public void setNumberOfActiveCases(Integer numberOfActiveCases) {
        this.numberOfActiveCases = numberOfActiveCases;
    }

    public Double getWeeklyMaximumHours() {
        return weeklyMaximumHours;
    }

    public void setWeeklyMaximumHours(Double weeklyMaximumHours) {
        this.weeklyMaximumHours = weeklyMaximumHours;
    }

    public Double getMonthlyMaximumHours() {
        return monthlyMaximumHours;
    }

    public void setMonthlyMaximumHours(Double monthlyMaximumHours) {
        this.monthlyMaximumHours = monthlyMaximumHours;
    }

    public Boolean getDeceased() {
        return deceased;
    }

    public void setDeceased(Boolean deceased) {
        this.deceased = deceased;
    }

    public LocalDate getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(LocalDate dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
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
        // Convert names to uppercase per BR PVM 20
        if (firstName != null) firstName = firstName.toUpperCase();
        if (lastName != null) lastName = lastName.toUpperCase();
        if (middleName != null) middleName = middleName.toUpperCase();
        // Set initial SSN verification status per BR PVM 03
        if (ssnVerificationStatus == null) ssnVerificationStatus = "NOT_YET_VERIFIED";
        if (overtimeViolationCount == null) overtimeViolationCount = 0;
        if (numberOfActiveCases == null) numberOfActiveCases = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Convert names to uppercase per BR PVM 20
        if (firstName != null) firstName = firstName.toUpperCase();
        if (lastName != null) lastName = lastName.toUpperCase();
        if (middleName != null) middleName = middleName.toUpperCase();
    }

    // Provider Status Enum
    public enum ProviderStatus {
        ACTIVE,
        ON_LEAVE,
        TERMINATED
    }

    // Helper method to get full name
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (firstName != null) name.append(firstName);
        if (middleName != null) name.append(" ").append(middleName);
        if (lastName != null) name.append(" ").append(lastName);
        return name.toString().trim();
    }

    // Helper method to check if provider is eligible to serve
    public boolean isEligibleToServe() {
        return "YES".equalsIgnoreCase(eligible) && providerStatus == ProviderStatus.ACTIVE;
    }

    // Helper method to check if provider can be reinstated (within 30 days per BR PVM 25)
    public boolean canBeReinstated() {
        if (!"NO".equalsIgnoreCase(eligible)) return false;
        if ("THIRD_OVERTIME_VIOLATION".equals(ineligibleReason) ||
            "FOURTH_OVERTIME_VIOLATION".equals(ineligibleReason)) return false;
        if (effectiveDate == null) return false;
        return effectiveDate.plusDays(30).isAfter(LocalDate.now());
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String providerNumber;
        private String firstName;
        private String lastName;
        private String middleName;
        private LocalDate dateOfBirth;
        private String gender;
        private String ssn;
        private String ssnVerificationStatus;
        private String taxpayerId;
        private String primaryPhone;
        private String secondaryPhone;
        private String email;
        private String streetAddress;
        private String city;
        private String state;
        private String zipCode;
        private String dojCountyCode;
        private String dojCountyName;
        private String eligible;
        private LocalDate effectiveDate;
        private String ineligibleReason;
        private ProviderStatus providerStatus;
        private LocalDate leaveTerminationEffectiveDate;
        private String terminationReason;
        private Boolean soc426Completed;
        private LocalDate soc426Date;
        private Boolean orientationCompleted;
        private LocalDate orientationDate;
        private Boolean soc846Completed;
        private LocalDate soc846Date;
        private Boolean providerAgreementSigned;
        private Boolean overtimeAgreementSigned;
        private Boolean backgroundCheckCompleted;
        private LocalDate backgroundCheckDate;
        private String backgroundCheckStatus;
        private Boolean mediCalSuspended;
        private LocalDate mediCalSuspendedBeginDate;
        private LocalDate mediCalSuspendedEndDate;
        private Boolean paRegistered;
        private Boolean paTrainingCompleted;
        private Boolean paFingerprintingCompleted;
        private LocalDate originalHireDate;
        private Boolean espRegistered;
        private String eTimesheetStatus;
        private Integer overtimeViolationCount;
        private LocalDate nextPossibleViolationDate;
        private Boolean trainingCompletedForViolation2;
        private LocalDate trainingCompletionDate;
        private Boolean hasOvertimeExemption;
        private LocalDate overtimeExemptionBeginDate;
        private LocalDate overtimeExemptionEndDate;
        private Double sickLeaveAccruedHours;
        private LocalDate sickLeaveAccruedDate;
        private LocalDate sickLeaveEligibleDate;
        private LocalDate sickLeaveEligibilityPeriodStart;
        private LocalDate sickLeaveEligibilityPeriodEnd;
        private Double totalServiceHoursWorked;
        private Integer numberOfActiveCases;
        private Double weeklyMaximumHours;
        private Double monthlyMaximumHours;
        private Boolean deceased;
        private LocalDate dateOfDeath;
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder providerNumber(String providerNumber) {
            this.providerNumber = providerNumber;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder ssn(String ssn) {
            this.ssn = ssn;
            return this;
        }

        public Builder ssnVerificationStatus(String ssnVerificationStatus) {
            this.ssnVerificationStatus = ssnVerificationStatus;
            return this;
        }

        public Builder taxpayerId(String taxpayerId) {
            this.taxpayerId = taxpayerId;
            return this;
        }

        public Builder primaryPhone(String primaryPhone) {
            this.primaryPhone = primaryPhone;
            return this;
        }

        public Builder secondaryPhone(String secondaryPhone) {
            this.secondaryPhone = secondaryPhone;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder streetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        public Builder dojCountyCode(String dojCountyCode) {
            this.dojCountyCode = dojCountyCode;
            return this;
        }

        public Builder dojCountyName(String dojCountyName) {
            this.dojCountyName = dojCountyName;
            return this;
        }

        public Builder eligible(String eligible) {
            this.eligible = eligible;
            return this;
        }

        public Builder effectiveDate(LocalDate effectiveDate) {
            this.effectiveDate = effectiveDate;
            return this;
        }

        public Builder ineligibleReason(String ineligibleReason) {
            this.ineligibleReason = ineligibleReason;
            return this;
        }

        public Builder providerStatus(ProviderStatus providerStatus) {
            this.providerStatus = providerStatus;
            return this;
        }

        public Builder leaveTerminationEffectiveDate(LocalDate leaveTerminationEffectiveDate) {
            this.leaveTerminationEffectiveDate = leaveTerminationEffectiveDate;
            return this;
        }

        public Builder terminationReason(String terminationReason) {
            this.terminationReason = terminationReason;
            return this;
        }

        public Builder soc426Completed(Boolean soc426Completed) {
            this.soc426Completed = soc426Completed;
            return this;
        }

        public Builder soc426Date(LocalDate soc426Date) {
            this.soc426Date = soc426Date;
            return this;
        }

        public Builder orientationCompleted(Boolean orientationCompleted) {
            this.orientationCompleted = orientationCompleted;
            return this;
        }

        public Builder orientationDate(LocalDate orientationDate) {
            this.orientationDate = orientationDate;
            return this;
        }

        public Builder soc846Completed(Boolean soc846Completed) {
            this.soc846Completed = soc846Completed;
            return this;
        }

        public Builder soc846Date(LocalDate soc846Date) {
            this.soc846Date = soc846Date;
            return this;
        }

        public Builder providerAgreementSigned(Boolean providerAgreementSigned) {
            this.providerAgreementSigned = providerAgreementSigned;
            return this;
        }

        public Builder overtimeAgreementSigned(Boolean overtimeAgreementSigned) {
            this.overtimeAgreementSigned = overtimeAgreementSigned;
            return this;
        }

        public Builder backgroundCheckCompleted(Boolean backgroundCheckCompleted) {
            this.backgroundCheckCompleted = backgroundCheckCompleted;
            return this;
        }

        public Builder backgroundCheckDate(LocalDate backgroundCheckDate) {
            this.backgroundCheckDate = backgroundCheckDate;
            return this;
        }

        public Builder backgroundCheckStatus(String backgroundCheckStatus) {
            this.backgroundCheckStatus = backgroundCheckStatus;
            return this;
        }

        public Builder mediCalSuspended(Boolean mediCalSuspended) {
            this.mediCalSuspended = mediCalSuspended;
            return this;
        }

        public Builder mediCalSuspendedBeginDate(LocalDate mediCalSuspendedBeginDate) {
            this.mediCalSuspendedBeginDate = mediCalSuspendedBeginDate;
            return this;
        }

        public Builder mediCalSuspendedEndDate(LocalDate mediCalSuspendedEndDate) {
            this.mediCalSuspendedEndDate = mediCalSuspendedEndDate;
            return this;
        }

        public Builder paRegistered(Boolean paRegistered) {
            this.paRegistered = paRegistered;
            return this;
        }

        public Builder paTrainingCompleted(Boolean paTrainingCompleted) {
            this.paTrainingCompleted = paTrainingCompleted;
            return this;
        }

        public Builder paFingerprintingCompleted(Boolean paFingerprintingCompleted) {
            this.paFingerprintingCompleted = paFingerprintingCompleted;
            return this;
        }

        public Builder originalHireDate(LocalDate originalHireDate) {
            this.originalHireDate = originalHireDate;
            return this;
        }

        public Builder espRegistered(Boolean espRegistered) {
            this.espRegistered = espRegistered;
            return this;
        }

        public Builder eTimesheetStatus(String eTimesheetStatus) {
            this.eTimesheetStatus = eTimesheetStatus;
            return this;
        }

        public Builder overtimeViolationCount(Integer overtimeViolationCount) {
            this.overtimeViolationCount = overtimeViolationCount;
            return this;
        }

        public Builder nextPossibleViolationDate(LocalDate nextPossibleViolationDate) {
            this.nextPossibleViolationDate = nextPossibleViolationDate;
            return this;
        }

        public Builder trainingCompletedForViolation2(Boolean trainingCompletedForViolation2) {
            this.trainingCompletedForViolation2 = trainingCompletedForViolation2;
            return this;
        }

        public Builder trainingCompletionDate(LocalDate trainingCompletionDate) {
            this.trainingCompletionDate = trainingCompletionDate;
            return this;
        }

        public Builder hasOvertimeExemption(Boolean hasOvertimeExemption) {
            this.hasOvertimeExemption = hasOvertimeExemption;
            return this;
        }

        public Builder overtimeExemptionBeginDate(LocalDate overtimeExemptionBeginDate) {
            this.overtimeExemptionBeginDate = overtimeExemptionBeginDate;
            return this;
        }

        public Builder overtimeExemptionEndDate(LocalDate overtimeExemptionEndDate) {
            this.overtimeExemptionEndDate = overtimeExemptionEndDate;
            return this;
        }

        public Builder sickLeaveAccruedHours(Double sickLeaveAccruedHours) {
            this.sickLeaveAccruedHours = sickLeaveAccruedHours;
            return this;
        }

        public Builder sickLeaveAccruedDate(LocalDate sickLeaveAccruedDate) {
            this.sickLeaveAccruedDate = sickLeaveAccruedDate;
            return this;
        }

        public Builder sickLeaveEligibleDate(LocalDate sickLeaveEligibleDate) {
            this.sickLeaveEligibleDate = sickLeaveEligibleDate;
            return this;
        }

        public Builder sickLeaveEligibilityPeriodStart(LocalDate sickLeaveEligibilityPeriodStart) {
            this.sickLeaveEligibilityPeriodStart = sickLeaveEligibilityPeriodStart;
            return this;
        }

        public Builder sickLeaveEligibilityPeriodEnd(LocalDate sickLeaveEligibilityPeriodEnd) {
            this.sickLeaveEligibilityPeriodEnd = sickLeaveEligibilityPeriodEnd;
            return this;
        }

        public Builder totalServiceHoursWorked(Double totalServiceHoursWorked) {
            this.totalServiceHoursWorked = totalServiceHoursWorked;
            return this;
        }

        public Builder numberOfActiveCases(Integer numberOfActiveCases) {
            this.numberOfActiveCases = numberOfActiveCases;
            return this;
        }

        public Builder weeklyMaximumHours(Double weeklyMaximumHours) {
            this.weeklyMaximumHours = weeklyMaximumHours;
            return this;
        }

        public Builder monthlyMaximumHours(Double monthlyMaximumHours) {
            this.monthlyMaximumHours = monthlyMaximumHours;
            return this;
        }

        public Builder deceased(Boolean deceased) {
            this.deceased = deceased;
            return this;
        }

        public Builder dateOfDeath(LocalDate dateOfDeath) {
            this.dateOfDeath = dateOfDeath;
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

        public ProviderEntity build() {
            return new ProviderEntity(id, providerNumber, firstName, lastName, middleName, dateOfBirth, gender,
                    ssn, ssnVerificationStatus, taxpayerId, primaryPhone, secondaryPhone, email, streetAddress,
                    city, state, zipCode, dojCountyCode, dojCountyName, eligible, effectiveDate, ineligibleReason,
                    providerStatus, leaveTerminationEffectiveDate, terminationReason, soc426Completed, soc426Date,
                    orientationCompleted, orientationDate, soc846Completed, soc846Date, providerAgreementSigned,
                    overtimeAgreementSigned, backgroundCheckCompleted, backgroundCheckDate, backgroundCheckStatus,
                    mediCalSuspended, mediCalSuspendedBeginDate, mediCalSuspendedEndDate, paRegistered,
                    paTrainingCompleted, paFingerprintingCompleted, originalHireDate, espRegistered,
                    eTimesheetStatus, overtimeViolationCount, nextPossibleViolationDate,
                    trainingCompletedForViolation2, trainingCompletionDate, hasOvertimeExemption,
                    overtimeExemptionBeginDate, overtimeExemptionEndDate, sickLeaveAccruedHours,
                    sickLeaveAccruedDate, sickLeaveEligibleDate, sickLeaveEligibilityPeriodStart,
                    sickLeaveEligibilityPeriodEnd, totalServiceHoursWorked, numberOfActiveCases,
                    weeklyMaximumHours, monthlyMaximumHours, deceased, dateOfDeath, createdAt, createdBy,
                    updatedAt, updatedBy);
        }
    }
}
