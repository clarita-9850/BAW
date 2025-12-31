package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseCreationRequest {
    // Person Information (if creating new person)
    private PersonDTO personData;
    
    // Or use existing person ID
    private Long personId;
    
    // Case Information
    private String countyCode;
    private String caseNotes;
    
    // Disaster Preparedness Code (if not in person data)
    private String disasterPreparednessCode;
    
    // Case opened date (defaults to today if not provided)
    private LocalDate caseOpenedDate;
    
    // Helper method to check if creating new person
    public boolean isCreatingNewPerson() {
        return personData != null && personId == null;
    }
    
    // Helper method to check if using existing person
    public boolean isUsingExistingPerson() {
        return personId != null && personData == null;
    }
    
    // Helper method to validate request
    public boolean isValid() {
        if (isCreatingNewPerson()) {
            return personData != null && 
                   personData.getFirstName() != null && 
                   personData.getLastName() != null &&
                   personData.getSsn() != null &&
                   personData.getDateOfBirth() != null &&
                   countyCode != null;
        } else if (isUsingExistingPerson()) {
            return personId != null && countyCode != null;
        }
        return false;
    }
}

