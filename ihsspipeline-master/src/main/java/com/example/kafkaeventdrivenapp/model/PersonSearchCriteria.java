package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonSearchCriteria {
    private String firstName;
    private String lastName;
    private String ssn; // Format: XXX-XX-XXXX or partial (last 4 digits)
    private String dateOfBirth; // Format: YYYY-MM-DD
    private String searchType; // "NAME", "SSN", "DOB", "COMBINED"
    
    // Helper method to check if search is by name
    public boolean isNameSearch() {
        return "NAME".equals(searchType) || 
               (searchType == null && (firstName != null || lastName != null));
    }
    
    // Helper method to check if search is by SSN
    public boolean isSsnSearch() {
        return "SSN".equals(searchType) || (searchType == null && ssn != null && !ssn.isEmpty());
    }
    
    // Helper method to check if search criteria is valid
    public boolean isValid() {
        if (isNameSearch()) {
            return (firstName != null && !firstName.trim().isEmpty()) || 
                   (lastName != null && !lastName.trim().isEmpty());
        } else if (isSsnSearch()) {
            return ssn != null && !ssn.trim().isEmpty();
        }
        return false;
    }
}

