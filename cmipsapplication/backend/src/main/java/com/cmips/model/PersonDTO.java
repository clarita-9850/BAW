package com.cmips.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PersonDTO {
    private Long personId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String ssn; // May be masked
    private String maskedSsn; // Masked version (***-**-XXXX)
    private LocalDate dateOfBirth;
    private String gender;
    private String ethnicity;
    private String preferredSpokenLanguage;
    private String preferredWrittenLanguage;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String residenceAddressLine1;
    private String residenceAddressLine2;
    private String residenceCity;
    private String residenceState;
    private String residenceZip;
    private String mailingAddressLine1;
    private String mailingAddressLine2;
    private String mailingCity;
    private String mailingState;
    private String mailingZip;
    private Boolean mailingSameAsResidence;
    private String countyOfResidence;
    private String guardianConservatorName;
    private String guardianConservatorAddress;
    private String guardianConservatorPhone;
    private String disasterPreparednessCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    public PersonDTO() {}

    public PersonDTO(Long personId, String firstName, String middleName, String lastName, String suffix,
                     String ssn, String maskedSsn, LocalDate dateOfBirth, String gender, String ethnicity,
                     String preferredSpokenLanguage, String preferredWrittenLanguage, String primaryPhone,
                     String secondaryPhone, String email, String residenceAddressLine1, String residenceAddressLine2,
                     String residenceCity, String residenceState, String residenceZip, String mailingAddressLine1,
                     String mailingAddressLine2, String mailingCity, String mailingState, String mailingZip,
                     Boolean mailingSameAsResidence, String countyOfResidence, String guardianConservatorName,
                     String guardianConservatorAddress, String guardianConservatorPhone, String disasterPreparednessCode,
                     LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy) {
        this.personId = personId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.suffix = suffix;
        this.ssn = ssn;
        this.maskedSsn = maskedSsn;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.ethnicity = ethnicity;
        this.preferredSpokenLanguage = preferredSpokenLanguage;
        this.preferredWrittenLanguage = preferredWrittenLanguage;
        this.primaryPhone = primaryPhone;
        this.secondaryPhone = secondaryPhone;
        this.email = email;
        this.residenceAddressLine1 = residenceAddressLine1;
        this.residenceAddressLine2 = residenceAddressLine2;
        this.residenceCity = residenceCity;
        this.residenceState = residenceState;
        this.residenceZip = residenceZip;
        this.mailingAddressLine1 = mailingAddressLine1;
        this.mailingAddressLine2 = mailingAddressLine2;
        this.mailingCity = mailingCity;
        this.mailingState = mailingState;
        this.mailingZip = mailingZip;
        this.mailingSameAsResidence = mailingSameAsResidence;
        this.countyOfResidence = countyOfResidence;
        this.guardianConservatorName = guardianConservatorName;
        this.guardianConservatorAddress = guardianConservatorAddress;
        this.guardianConservatorPhone = guardianConservatorPhone;
        this.disasterPreparednessCode = disasterPreparednessCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    // Getters and Setters
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }

    public String getMaskedSsn() { return maskedSsn; }
    public void setMaskedSsn(String maskedSsn) { this.maskedSsn = maskedSsn; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEthnicity() { return ethnicity; }
    public void setEthnicity(String ethnicity) { this.ethnicity = ethnicity; }

    public String getPreferredSpokenLanguage() { return preferredSpokenLanguage; }
    public void setPreferredSpokenLanguage(String preferredSpokenLanguage) { this.preferredSpokenLanguage = preferredSpokenLanguage; }

    public String getPreferredWrittenLanguage() { return preferredWrittenLanguage; }
    public void setPreferredWrittenLanguage(String preferredWrittenLanguage) { this.preferredWrittenLanguage = preferredWrittenLanguage; }

    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }

    public String getSecondaryPhone() { return secondaryPhone; }
    public void setSecondaryPhone(String secondaryPhone) { this.secondaryPhone = secondaryPhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getResidenceAddressLine1() { return residenceAddressLine1; }
    public void setResidenceAddressLine1(String residenceAddressLine1) { this.residenceAddressLine1 = residenceAddressLine1; }

    public String getResidenceAddressLine2() { return residenceAddressLine2; }
    public void setResidenceAddressLine2(String residenceAddressLine2) { this.residenceAddressLine2 = residenceAddressLine2; }

    public String getResidenceCity() { return residenceCity; }
    public void setResidenceCity(String residenceCity) { this.residenceCity = residenceCity; }

    public String getResidenceState() { return residenceState; }
    public void setResidenceState(String residenceState) { this.residenceState = residenceState; }

    public String getResidenceZip() { return residenceZip; }
    public void setResidenceZip(String residenceZip) { this.residenceZip = residenceZip; }

    public String getMailingAddressLine1() { return mailingAddressLine1; }
    public void setMailingAddressLine1(String mailingAddressLine1) { this.mailingAddressLine1 = mailingAddressLine1; }

    public String getMailingAddressLine2() { return mailingAddressLine2; }
    public void setMailingAddressLine2(String mailingAddressLine2) { this.mailingAddressLine2 = mailingAddressLine2; }

    public String getMailingCity() { return mailingCity; }
    public void setMailingCity(String mailingCity) { this.mailingCity = mailingCity; }

    public String getMailingState() { return mailingState; }
    public void setMailingState(String mailingState) { this.mailingState = mailingState; }

    public String getMailingZip() { return mailingZip; }
    public void setMailingZip(String mailingZip) { this.mailingZip = mailingZip; }

    public Boolean getMailingSameAsResidence() { return mailingSameAsResidence; }
    public void setMailingSameAsResidence(Boolean mailingSameAsResidence) { this.mailingSameAsResidence = mailingSameAsResidence; }

    public String getCountyOfResidence() { return countyOfResidence; }
    public void setCountyOfResidence(String countyOfResidence) { this.countyOfResidence = countyOfResidence; }

    public String getGuardianConservatorName() { return guardianConservatorName; }
    public void setGuardianConservatorName(String guardianConservatorName) { this.guardianConservatorName = guardianConservatorName; }

    public String getGuardianConservatorAddress() { return guardianConservatorAddress; }
    public void setGuardianConservatorAddress(String guardianConservatorAddress) { this.guardianConservatorAddress = guardianConservatorAddress; }

    public String getGuardianConservatorPhone() { return guardianConservatorPhone; }
    public void setGuardianConservatorPhone(String guardianConservatorPhone) { this.guardianConservatorPhone = guardianConservatorPhone; }

    public String getDisasterPreparednessCode() { return disasterPreparednessCode; }
    public void setDisasterPreparednessCode(String disasterPreparednessCode) { this.disasterPreparednessCode = disasterPreparednessCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}

