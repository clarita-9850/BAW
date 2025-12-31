package com.cmips.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "persons")
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long personId;

    // Personal Information
    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "suffix", length = 20)
    private String suffix;

    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", message = "SSN must be in format XXX-XX-XXXX")
    @Column(name = "ssn", length = 11, unique = true)
    private String ssn; // Format: XXX-XX-XXXX

    @NotNull(message = "Date of birth is required")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 50)
    private String gender;

    @Column(name = "ethnicity", length = 100)
    private String ethnicity;

    // Contact Information
    @Column(name = "preferred_spoken_language", length = 50)
    private String preferredSpokenLanguage;

    @Column(name = "preferred_written_language", length = 50)
    private String preferredWrittenLanguage;

    @Column(name = "primary_phone", length = 20)
    private String primaryPhone;

    @Column(name = "secondary_phone", length = 20)
    private String secondaryPhone;

    @Email(message = "Email should be valid")
    @Column(name = "email", length = 255)
    private String email;

    // Residence Address
    @NotBlank(message = "Residence address line 1 is required")
    @Column(name = "residence_address_line1", nullable = false, length = 255)
    private String residenceAddressLine1;

    @Column(name = "residence_address_line2", length = 255)
    private String residenceAddressLine2;

    @NotBlank(message = "Residence city is required")
    @Column(name = "residence_city", nullable = false, length = 100)
    private String residenceCity;

    @NotBlank(message = "Residence state is required")
    @Column(name = "residence_state", nullable = false, length = 2)
    private String residenceState = "CA";

    @NotBlank(message = "Residence ZIP code is required")
    @Column(name = "residence_zip", nullable = false, length = 10)
    private String residenceZip;

    // Mailing Address
    @Column(name = "mailing_address_line1", length = 255)
    private String mailingAddressLine1;

    @Column(name = "mailing_address_line2", length = 255)
    private String mailingAddressLine2;

    @Column(name = "mailing_city", length = 100)
    private String mailingCity;

    @Column(name = "mailing_state", length = 2)
    private String mailingState;

    @Column(name = "mailing_zip", length = 10)
    private String mailingZip;

    @Column(name = "mailing_same_as_residence")
    private Boolean mailingSameAsResidence = true;

    // Location Information
    @NotBlank(message = "County of residence is required")
    @Column(name = "county_of_residence", nullable = false, length = 100)
    private String countyOfResidence;

    // Guardian/Conservator Information
    @Column(name = "guardian_conservator_name", length = 255)
    private String guardianConservatorName;

    @Column(name = "guardian_conservator_address", length = 500)
    private String guardianConservatorAddress;

    @Column(name = "guardian_conservator_phone", length = 20)
    private String guardianConservatorPhone;

    // Disaster Preparedness
    @Pattern(regexp = "^[A-Z]{3}$", message = "Disaster preparedness code must be 3 uppercase letters")
    @Column(name = "disaster_preparedness_code", length = 3)
    private String disasterPreparednessCode; // 3-letter code

    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    // Constructors
    public PersonEntity() {
    }

    // Getters and Setters
    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
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

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getPreferredSpokenLanguage() {
        return preferredSpokenLanguage;
    }

    public void setPreferredSpokenLanguage(String preferredSpokenLanguage) {
        this.preferredSpokenLanguage = preferredSpokenLanguage;
    }

    public String getPreferredWrittenLanguage() {
        return preferredWrittenLanguage;
    }

    public void setPreferredWrittenLanguage(String preferredWrittenLanguage) {
        this.preferredWrittenLanguage = preferredWrittenLanguage;
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

    public String getResidenceAddressLine1() {
        return residenceAddressLine1;
    }

    public void setResidenceAddressLine1(String residenceAddressLine1) {
        this.residenceAddressLine1 = residenceAddressLine1;
    }

    public String getResidenceAddressLine2() {
        return residenceAddressLine2;
    }

    public void setResidenceAddressLine2(String residenceAddressLine2) {
        this.residenceAddressLine2 = residenceAddressLine2;
    }

    public String getResidenceCity() {
        return residenceCity;
    }

    public void setResidenceCity(String residenceCity) {
        this.residenceCity = residenceCity;
    }

    public String getResidenceState() {
        return residenceState;
    }

    public void setResidenceState(String residenceState) {
        this.residenceState = residenceState;
    }

    public String getResidenceZip() {
        return residenceZip;
    }

    public void setResidenceZip(String residenceZip) {
        this.residenceZip = residenceZip;
    }

    public String getMailingAddressLine1() {
        return mailingAddressLine1;
    }

    public void setMailingAddressLine1(String mailingAddressLine1) {
        this.mailingAddressLine1 = mailingAddressLine1;
    }

    public String getMailingAddressLine2() {
        return mailingAddressLine2;
    }

    public void setMailingAddressLine2(String mailingAddressLine2) {
        this.mailingAddressLine2 = mailingAddressLine2;
    }

    public String getMailingCity() {
        return mailingCity;
    }

    public void setMailingCity(String mailingCity) {
        this.mailingCity = mailingCity;
    }

    public String getMailingState() {
        return mailingState;
    }

    public void setMailingState(String mailingState) {
        this.mailingState = mailingState;
    }

    public String getMailingZip() {
        return mailingZip;
    }

    public void setMailingZip(String mailingZip) {
        this.mailingZip = mailingZip;
    }

    public Boolean getMailingSameAsResidence() {
        return mailingSameAsResidence;
    }

    public void setMailingSameAsResidence(Boolean mailingSameAsResidence) {
        this.mailingSameAsResidence = mailingSameAsResidence;
    }

    public String getCountyOfResidence() {
        return countyOfResidence;
    }

    public void setCountyOfResidence(String countyOfResidence) {
        this.countyOfResidence = countyOfResidence;
    }

    public String getGuardianConservatorName() {
        return guardianConservatorName;
    }

    public void setGuardianConservatorName(String guardianConservatorName) {
        this.guardianConservatorName = guardianConservatorName;
    }

    public String getGuardianConservatorAddress() {
        return guardianConservatorAddress;
    }

    public void setGuardianConservatorAddress(String guardianConservatorAddress) {
        this.guardianConservatorAddress = guardianConservatorAddress;
    }

    public String getGuardianConservatorPhone() {
        return guardianConservatorPhone;
    }

    public void setGuardianConservatorPhone(String guardianConservatorPhone) {
        this.guardianConservatorPhone = guardianConservatorPhone;
    }

    public String getDisasterPreparednessCode() {
        return disasterPreparednessCode;
    }

    public void setDisasterPreparednessCode(String disasterPreparednessCode) {
        this.disasterPreparednessCode = disasterPreparednessCode;
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

    // Helper method to get full name
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) fullName.append(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(middleName);
        }
        if (lastName != null) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(lastName);
        }
        if (suffix != null && !suffix.isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(suffix);
        }
        return fullName.toString();
    }

    // Helper method to mask SSN (show only last 4 digits)
    public String getMaskedSsn() {
        if (ssn == null || ssn.length() < 4) {
            return "***-**-****";
        }
        return "***-**-" + ssn.substring(ssn.length() - 4);
    }
}

