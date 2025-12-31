package com.cmips.model;

public class PersonSearchCriteria {
    private String firstName;
    private String lastName;
    private String ssn; // Format: XXX-XX-XXXX or partial (last 4 digits)
    private String dateOfBirth; // Format: YYYY-MM-DD
    private String searchType; // "NAME", "SSN", "DOB", "COMBINED"

    public PersonSearchCriteria() {}

    public PersonSearchCriteria(String firstName, String lastName, String ssn, String dateOfBirth, String searchType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.dateOfBirth = dateOfBirth;
        this.searchType = searchType;
    }

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

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }
}

