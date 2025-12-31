package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Recipient Entity - Represents an IHSS recipient/applicant
 * Based on DSD Section 20 - Recipient User Stories and Business Rules
 */
@Entity
@Table(name = "recipients")
public class RecipientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Person Type: OPEN_REFERRAL, CLOSED_REFERRAL, APPLICANT, RECIPIENT
    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false)
    private PersonType personType;

    // Basic Demographics (stored in UPPERCASE per BR OS 28-30)
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

    @Column(name = "ethnicity", length = 50)
    private String ethnicity;

    // Identifiers
    @Column(name = "ssn", length = 11)
    private String ssn;

    @Column(name = "ssn_type", length = 50)
    private String ssnType; // VERIFIED, NOT_YET_VERIFIED, DUPLICATE_SSN, SUSPECT_SSN

    @Column(name = "blank_ssn_reason", length = 100)
    private String blankSsnReason; // APPLIED_FOR_SSN

    @Column(name = "cin", length = 20)
    private String cin; // Client Index Number

    @Column(name = "medi_cal_pseudo")
    private Boolean mediCalPseudo;

    @Column(name = "taxpayer_id", length = 20)
    private String taxpayerId;

    // Contact Information
    @Column(name = "primary_phone", length = 20)
    private String primaryPhone;

    @Column(name = "secondary_phone", length = 20)
    private String secondaryPhone;

    @Column(name = "email", length = 200)
    private String email;

    // Residence Address
    @Column(name = "residence_street_number", length = 20)
    private String residenceStreetNumber;

    @Column(name = "residence_street_name", length = 200)
    private String residenceStreetName;

    @Column(name = "residence_unit_type", length = 20)
    private String residenceUnitType;

    @Column(name = "residence_unit_number", length = 20)
    private String residenceUnitNumber;

    @Column(name = "residence_city", length = 100)
    private String residenceCity;

    @Column(name = "residence_state", length = 2)
    private String residenceState;

    @Column(name = "residence_zip", length = 10)
    private String residenceZip;

    @Column(name = "residence_county", length = 100)
    private String residenceCounty;

    // Mailing Address (if different)
    @Column(name = "mailing_street_number", length = 20)
    private String mailingStreetNumber;

    @Column(name = "mailing_street_name", length = 200)
    private String mailingStreetName;

    @Column(name = "mailing_unit_type", length = 20)
    private String mailingUnitType;

    @Column(name = "mailing_unit_number", length = 20)
    private String mailingUnitNumber;

    @Column(name = "mailing_city", length = 100)
    private String mailingCity;

    @Column(name = "mailing_state", length = 2)
    private String mailingState;

    @Column(name = "mailing_zip", length = 10)
    private String mailingZip;

    // Language Preferences (per BR OS 49-60)
    @Column(name = "spoken_language", length = 50)
    private String spokenLanguage;

    @Column(name = "written_language", length = 50)
    private String writtenLanguage;

    // Accessibility Options
    @Column(name = "blind_visually_impaired")
    private Boolean blindVisuallyImpaired;

    @Column(name = "noa_option", length = 50)
    private String noaOption; // LARGE_FONT, AUDIO_CD, DATA_CD, STANDARD

    @Column(name = "noa_language", length = 50)
    private String noaLanguage;

    @Column(name = "ihss_forms_option", length = 50)
    private String ihssFormsOption;

    @Column(name = "ihss_forms_language", length = 50)
    private String ihssFormsLanguage;

    @Column(name = "timesheet_accommodation", length = 50)
    private String timesheetAccommodation; // LARGE_FONT_TIMESHEET

    // Electronic Services
    @Column(name = "esp_registered")
    private Boolean espRegistered; // IHSS Electronic Service Portal

    @Column(name = "e_timesheet_option", length = 50)
    private String eTimesheetOption; // IHSS_WEBSITE, PAPER

    // Referral Information
    @Column(name = "referral_source", length = 200)
    private String referralSource;

    @Column(name = "referral_date")
    private LocalDate referralDate;

    @Column(name = "referral_closed_date")
    private LocalDate referralClosedDate;

    @Column(name = "referral_closed_reason", length = 500)
    private String referralClosedReason;

    // Emergency Contact / Disaster Preparedness
    @Column(name = "emergency_contact_name", length = 200)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relationship", length = 100)
    private String emergencyContactRelationship;

    // County Assignment
    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "county_name", length = 100)
    private String countyName;

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

    // Constructors
    public RecipientEntity() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonType getPersonType() {
        return personType;
    }

    public void setPersonType(PersonType personType) {
        this.personType = personType;
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

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getSsnType() {
        return ssnType;
    }

    public void setSsnType(String ssnType) {
        this.ssnType = ssnType;
    }

    public String getBlankSsnReason() {
        return blankSsnReason;
    }

    public void setBlankSsnReason(String blankSsnReason) {
        this.blankSsnReason = blankSsnReason;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public Boolean getMediCalPseudo() {
        return mediCalPseudo;
    }

    public void setMediCalPseudo(Boolean mediCalPseudo) {
        this.mediCalPseudo = mediCalPseudo;
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

    public String getResidenceStreetNumber() {
        return residenceStreetNumber;
    }

    public void setResidenceStreetNumber(String residenceStreetNumber) {
        this.residenceStreetNumber = residenceStreetNumber;
    }

    public String getResidenceStreetName() {
        return residenceStreetName;
    }

    public void setResidenceStreetName(String residenceStreetName) {
        this.residenceStreetName = residenceStreetName;
    }

    public String getResidenceUnitType() {
        return residenceUnitType;
    }

    public void setResidenceUnitType(String residenceUnitType) {
        this.residenceUnitType = residenceUnitType;
    }

    public String getResidenceUnitNumber() {
        return residenceUnitNumber;
    }

    public void setResidenceUnitNumber(String residenceUnitNumber) {
        this.residenceUnitNumber = residenceUnitNumber;
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

    public String getResidenceCounty() {
        return residenceCounty;
    }

    public void setResidenceCounty(String residenceCounty) {
        this.residenceCounty = residenceCounty;
    }

    public String getMailingStreetNumber() {
        return mailingStreetNumber;
    }

    public void setMailingStreetNumber(String mailingStreetNumber) {
        this.mailingStreetNumber = mailingStreetNumber;
    }

    public String getMailingStreetName() {
        return mailingStreetName;
    }

    public void setMailingStreetName(String mailingStreetName) {
        this.mailingStreetName = mailingStreetName;
    }

    public String getMailingUnitType() {
        return mailingUnitType;
    }

    public void setMailingUnitType(String mailingUnitType) {
        this.mailingUnitType = mailingUnitType;
    }

    public String getMailingUnitNumber() {
        return mailingUnitNumber;
    }

    public void setMailingUnitNumber(String mailingUnitNumber) {
        this.mailingUnitNumber = mailingUnitNumber;
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

    public String getSpokenLanguage() {
        return spokenLanguage;
    }

    public void setSpokenLanguage(String spokenLanguage) {
        this.spokenLanguage = spokenLanguage;
    }

    public String getWrittenLanguage() {
        return writtenLanguage;
    }

    public void setWrittenLanguage(String writtenLanguage) {
        this.writtenLanguage = writtenLanguage;
    }

    public Boolean getBlindVisuallyImpaired() {
        return blindVisuallyImpaired;
    }

    public void setBlindVisuallyImpaired(Boolean blindVisuallyImpaired) {
        this.blindVisuallyImpaired = blindVisuallyImpaired;
    }

    public String getNoaOption() {
        return noaOption;
    }

    public void setNoaOption(String noaOption) {
        this.noaOption = noaOption;
    }

    public String getNoaLanguage() {
        return noaLanguage;
    }

    public void setNoaLanguage(String noaLanguage) {
        this.noaLanguage = noaLanguage;
    }

    public String getIhssFormsOption() {
        return ihssFormsOption;
    }

    public void setIhssFormsOption(String ihssFormsOption) {
        this.ihssFormsOption = ihssFormsOption;
    }

    public String getIhssFormsLanguage() {
        return ihssFormsLanguage;
    }

    public void setIhssFormsLanguage(String ihssFormsLanguage) {
        this.ihssFormsLanguage = ihssFormsLanguage;
    }

    public String getTimesheetAccommodation() {
        return timesheetAccommodation;
    }

    public void setTimesheetAccommodation(String timesheetAccommodation) {
        this.timesheetAccommodation = timesheetAccommodation;
    }

    public Boolean getEspRegistered() {
        return espRegistered;
    }

    public void setEspRegistered(Boolean espRegistered) {
        this.espRegistered = espRegistered;
    }

    public String geteTimesheetOption() {
        return eTimesheetOption;
    }

    public void seteTimesheetOption(String eTimesheetOption) {
        this.eTimesheetOption = eTimesheetOption;
    }

    public String getReferralSource() {
        return referralSource;
    }

    public void setReferralSource(String referralSource) {
        this.referralSource = referralSource;
    }

    public LocalDate getReferralDate() {
        return referralDate;
    }

    public void setReferralDate(LocalDate referralDate) {
        this.referralDate = referralDate;
    }

    public LocalDate getReferralClosedDate() {
        return referralClosedDate;
    }

    public void setReferralClosedDate(LocalDate referralClosedDate) {
        this.referralClosedDate = referralClosedDate;
    }

    public String getReferralClosedReason() {
        return referralClosedReason;
    }

    public void setReferralClosedReason(String referralClosedReason) {
        this.referralClosedReason = referralClosedReason;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getEmergencyContactRelationship() {
        return emergencyContactRelationship;
    }

    public void setEmergencyContactRelationship(String emergencyContactRelationship) {
        this.emergencyContactRelationship = emergencyContactRelationship;
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
        // Convert names to uppercase per business rules
        if (firstName != null) firstName = firstName.toUpperCase();
        if (lastName != null) lastName = lastName.toUpperCase();
        if (middleName != null) middleName = middleName.toUpperCase();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Convert names to uppercase per business rules
        if (firstName != null) firstName = firstName.toUpperCase();
        if (lastName != null) lastName = lastName.toUpperCase();
        if (middleName != null) middleName = middleName.toUpperCase();
    }

    // Person Type Enum
    public enum PersonType {
        OPEN_REFERRAL,
        CLOSED_REFERRAL,
        APPLICANT,
        RECIPIENT
    }

    // Helper method to get full name
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (firstName != null) name.append(firstName);
        if (middleName != null) name.append(" ").append(middleName);
        if (lastName != null) name.append(" ").append(lastName);
        return name.toString().trim();
    }

    // Helper method to get full residence address
    public String getFullResidenceAddress() {
        StringBuilder address = new StringBuilder();
        if (residenceStreetNumber != null) address.append(residenceStreetNumber).append(" ");
        if (residenceStreetName != null) address.append(residenceStreetName);
        if (residenceUnitType != null && residenceUnitNumber != null) {
            address.append(" ").append(residenceUnitType).append(" ").append(residenceUnitNumber);
        }
        if (residenceCity != null) address.append(", ").append(residenceCity);
        if (residenceState != null) address.append(", ").append(residenceState);
        if (residenceZip != null) address.append(" ").append(residenceZip);
        return address.toString().trim();
    }

    // Builder pattern
    public static RecipientEntityBuilder builder() {
        return new RecipientEntityBuilder();
    }

    public static class RecipientEntityBuilder {
        private Long id;
        private PersonType personType;
        private String firstName;
        private String lastName;
        private String middleName;
        private LocalDate dateOfBirth;
        private String gender;
        private String ethnicity;
        private String ssn;
        private String ssnType;
        private String blankSsnReason;
        private String cin;
        private Boolean mediCalPseudo;
        private String taxpayerId;
        private String primaryPhone;
        private String secondaryPhone;
        private String email;
        private String residenceStreetNumber;
        private String residenceStreetName;
        private String residenceUnitType;
        private String residenceUnitNumber;
        private String residenceCity;
        private String residenceState;
        private String residenceZip;
        private String residenceCounty;
        private String mailingStreetNumber;
        private String mailingStreetName;
        private String mailingUnitType;
        private String mailingUnitNumber;
        private String mailingCity;
        private String mailingState;
        private String mailingZip;
        private String spokenLanguage;
        private String writtenLanguage;
        private Boolean blindVisuallyImpaired;
        private String noaOption;
        private String noaLanguage;
        private String ihssFormsOption;
        private String ihssFormsLanguage;
        private String timesheetAccommodation;
        private Boolean espRegistered;
        private String eTimesheetOption;
        private String referralSource;
        private LocalDate referralDate;
        private LocalDate referralClosedDate;
        private String referralClosedReason;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String emergencyContactRelationship;
        private String countyCode;
        private String countyName;
        private Boolean deceased;
        private LocalDate dateOfDeath;
        private LocalDateTime createdAt;
        private String createdBy;
        private LocalDateTime updatedAt;
        private String updatedBy;

        RecipientEntityBuilder() {
        }

        public RecipientEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RecipientEntityBuilder personType(PersonType personType) {
            this.personType = personType;
            return this;
        }

        public RecipientEntityBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public RecipientEntityBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public RecipientEntityBuilder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public RecipientEntityBuilder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public RecipientEntityBuilder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public RecipientEntityBuilder ethnicity(String ethnicity) {
            this.ethnicity = ethnicity;
            return this;
        }

        public RecipientEntityBuilder ssn(String ssn) {
            this.ssn = ssn;
            return this;
        }

        public RecipientEntityBuilder ssnType(String ssnType) {
            this.ssnType = ssnType;
            return this;
        }

        public RecipientEntityBuilder blankSsnReason(String blankSsnReason) {
            this.blankSsnReason = blankSsnReason;
            return this;
        }

        public RecipientEntityBuilder cin(String cin) {
            this.cin = cin;
            return this;
        }

        public RecipientEntityBuilder mediCalPseudo(Boolean mediCalPseudo) {
            this.mediCalPseudo = mediCalPseudo;
            return this;
        }

        public RecipientEntityBuilder taxpayerId(String taxpayerId) {
            this.taxpayerId = taxpayerId;
            return this;
        }

        public RecipientEntityBuilder primaryPhone(String primaryPhone) {
            this.primaryPhone = primaryPhone;
            return this;
        }

        public RecipientEntityBuilder secondaryPhone(String secondaryPhone) {
            this.secondaryPhone = secondaryPhone;
            return this;
        }

        public RecipientEntityBuilder email(String email) {
            this.email = email;
            return this;
        }

        public RecipientEntityBuilder residenceStreetNumber(String residenceStreetNumber) {
            this.residenceStreetNumber = residenceStreetNumber;
            return this;
        }

        public RecipientEntityBuilder residenceStreetName(String residenceStreetName) {
            this.residenceStreetName = residenceStreetName;
            return this;
        }

        public RecipientEntityBuilder residenceUnitType(String residenceUnitType) {
            this.residenceUnitType = residenceUnitType;
            return this;
        }

        public RecipientEntityBuilder residenceUnitNumber(String residenceUnitNumber) {
            this.residenceUnitNumber = residenceUnitNumber;
            return this;
        }

        public RecipientEntityBuilder residenceCity(String residenceCity) {
            this.residenceCity = residenceCity;
            return this;
        }

        public RecipientEntityBuilder residenceState(String residenceState) {
            this.residenceState = residenceState;
            return this;
        }

        public RecipientEntityBuilder residenceZip(String residenceZip) {
            this.residenceZip = residenceZip;
            return this;
        }

        public RecipientEntityBuilder residenceCounty(String residenceCounty) {
            this.residenceCounty = residenceCounty;
            return this;
        }

        public RecipientEntityBuilder mailingStreetNumber(String mailingStreetNumber) {
            this.mailingStreetNumber = mailingStreetNumber;
            return this;
        }

        public RecipientEntityBuilder mailingStreetName(String mailingStreetName) {
            this.mailingStreetName = mailingStreetName;
            return this;
        }

        public RecipientEntityBuilder mailingUnitType(String mailingUnitType) {
            this.mailingUnitType = mailingUnitType;
            return this;
        }

        public RecipientEntityBuilder mailingUnitNumber(String mailingUnitNumber) {
            this.mailingUnitNumber = mailingUnitNumber;
            return this;
        }

        public RecipientEntityBuilder mailingCity(String mailingCity) {
            this.mailingCity = mailingCity;
            return this;
        }

        public RecipientEntityBuilder mailingState(String mailingState) {
            this.mailingState = mailingState;
            return this;
        }

        public RecipientEntityBuilder mailingZip(String mailingZip) {
            this.mailingZip = mailingZip;
            return this;
        }

        public RecipientEntityBuilder spokenLanguage(String spokenLanguage) {
            this.spokenLanguage = spokenLanguage;
            return this;
        }

        public RecipientEntityBuilder writtenLanguage(String writtenLanguage) {
            this.writtenLanguage = writtenLanguage;
            return this;
        }

        public RecipientEntityBuilder blindVisuallyImpaired(Boolean blindVisuallyImpaired) {
            this.blindVisuallyImpaired = blindVisuallyImpaired;
            return this;
        }

        public RecipientEntityBuilder noaOption(String noaOption) {
            this.noaOption = noaOption;
            return this;
        }

        public RecipientEntityBuilder noaLanguage(String noaLanguage) {
            this.noaLanguage = noaLanguage;
            return this;
        }

        public RecipientEntityBuilder ihssFormsOption(String ihssFormsOption) {
            this.ihssFormsOption = ihssFormsOption;
            return this;
        }

        public RecipientEntityBuilder ihssFormsLanguage(String ihssFormsLanguage) {
            this.ihssFormsLanguage = ihssFormsLanguage;
            return this;
        }

        public RecipientEntityBuilder timesheetAccommodation(String timesheetAccommodation) {
            this.timesheetAccommodation = timesheetAccommodation;
            return this;
        }

        public RecipientEntityBuilder espRegistered(Boolean espRegistered) {
            this.espRegistered = espRegistered;
            return this;
        }

        public RecipientEntityBuilder eTimesheetOption(String eTimesheetOption) {
            this.eTimesheetOption = eTimesheetOption;
            return this;
        }

        public RecipientEntityBuilder referralSource(String referralSource) {
            this.referralSource = referralSource;
            return this;
        }

        public RecipientEntityBuilder referralDate(LocalDate referralDate) {
            this.referralDate = referralDate;
            return this;
        }

        public RecipientEntityBuilder referralClosedDate(LocalDate referralClosedDate) {
            this.referralClosedDate = referralClosedDate;
            return this;
        }

        public RecipientEntityBuilder referralClosedReason(String referralClosedReason) {
            this.referralClosedReason = referralClosedReason;
            return this;
        }

        public RecipientEntityBuilder emergencyContactName(String emergencyContactName) {
            this.emergencyContactName = emergencyContactName;
            return this;
        }

        public RecipientEntityBuilder emergencyContactPhone(String emergencyContactPhone) {
            this.emergencyContactPhone = emergencyContactPhone;
            return this;
        }

        public RecipientEntityBuilder emergencyContactRelationship(String emergencyContactRelationship) {
            this.emergencyContactRelationship = emergencyContactRelationship;
            return this;
        }

        public RecipientEntityBuilder countyCode(String countyCode) {
            this.countyCode = countyCode;
            return this;
        }

        public RecipientEntityBuilder countyName(String countyName) {
            this.countyName = countyName;
            return this;
        }

        public RecipientEntityBuilder deceased(Boolean deceased) {
            this.deceased = deceased;
            return this;
        }

        public RecipientEntityBuilder dateOfDeath(LocalDate dateOfDeath) {
            this.dateOfDeath = dateOfDeath;
            return this;
        }

        public RecipientEntityBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RecipientEntityBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public RecipientEntityBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public RecipientEntityBuilder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public RecipientEntity build() {
            RecipientEntity entity = new RecipientEntity();
            entity.setId(id);
            entity.setPersonType(personType);
            entity.setFirstName(firstName);
            entity.setLastName(lastName);
            entity.setMiddleName(middleName);
            entity.setDateOfBirth(dateOfBirth);
            entity.setGender(gender);
            entity.setEthnicity(ethnicity);
            entity.setSsn(ssn);
            entity.setSsnType(ssnType);
            entity.setBlankSsnReason(blankSsnReason);
            entity.setCin(cin);
            entity.setMediCalPseudo(mediCalPseudo);
            entity.setTaxpayerId(taxpayerId);
            entity.setPrimaryPhone(primaryPhone);
            entity.setSecondaryPhone(secondaryPhone);
            entity.setEmail(email);
            entity.setResidenceStreetNumber(residenceStreetNumber);
            entity.setResidenceStreetName(residenceStreetName);
            entity.setResidenceUnitType(residenceUnitType);
            entity.setResidenceUnitNumber(residenceUnitNumber);
            entity.setResidenceCity(residenceCity);
            entity.setResidenceState(residenceState);
            entity.setResidenceZip(residenceZip);
            entity.setResidenceCounty(residenceCounty);
            entity.setMailingStreetNumber(mailingStreetNumber);
            entity.setMailingStreetName(mailingStreetName);
            entity.setMailingUnitType(mailingUnitType);
            entity.setMailingUnitNumber(mailingUnitNumber);
            entity.setMailingCity(mailingCity);
            entity.setMailingState(mailingState);
            entity.setMailingZip(mailingZip);
            entity.setSpokenLanguage(spokenLanguage);
            entity.setWrittenLanguage(writtenLanguage);
            entity.setBlindVisuallyImpaired(blindVisuallyImpaired);
            entity.setNoaOption(noaOption);
            entity.setNoaLanguage(noaLanguage);
            entity.setIhssFormsOption(ihssFormsOption);
            entity.setIhssFormsLanguage(ihssFormsLanguage);
            entity.setTimesheetAccommodation(timesheetAccommodation);
            entity.setEspRegistered(espRegistered);
            entity.seteTimesheetOption(eTimesheetOption);
            entity.setReferralSource(referralSource);
            entity.setReferralDate(referralDate);
            entity.setReferralClosedDate(referralClosedDate);
            entity.setReferralClosedReason(referralClosedReason);
            entity.setEmergencyContactName(emergencyContactName);
            entity.setEmergencyContactPhone(emergencyContactPhone);
            entity.setEmergencyContactRelationship(emergencyContactRelationship);
            entity.setCountyCode(countyCode);
            entity.setCountyName(countyName);
            entity.setDeceased(deceased);
            entity.setDateOfDeath(dateOfDeath);
            entity.setCreatedAt(createdAt);
            entity.setCreatedBy(createdBy);
            entity.setUpdatedAt(updatedAt);
            entity.setUpdatedBy(updatedBy);
            return entity;
        }
    }
}
