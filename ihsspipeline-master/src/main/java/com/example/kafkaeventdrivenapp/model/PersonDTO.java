package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}

