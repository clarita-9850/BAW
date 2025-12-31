package com.cmips.service;

import com.cmips.entity.PersonEntity;
import com.cmips.model.PersonDTO;
import com.cmips.model.PersonSearchCriteria;
import com.cmips.model.PersonSearchResponse;
import com.cmips.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PersonService {
    
    @Autowired
    private PersonRepository personRepository;
    
    /**
     * Search persons by criteria (name or SSN)
     */
    public PersonSearchResponse searchPersons(PersonSearchCriteria criteria) {
        System.out.println("🔍 PersonService: Searching persons with criteria: " + criteria);
        
        if (!criteria.isValid()) {
            return new PersonSearchResponse(false, "Invalid search criteria. Please provide name or SSN.");
        }
        
        try {
            List<PersonEntity> results = new ArrayList<>();
            
            if (criteria.isNameSearch()) {
                // Search by name
                if (criteria.getFirstName() != null && !criteria.getFirstName().trim().isEmpty() &&
                    criteria.getLastName() != null && !criteria.getLastName().trim().isEmpty()) {
                    // Both first and last name provided
                    results = personRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
                        criteria.getFirstName().trim(), criteria.getLastName().trim());
                } else if (criteria.getFirstName() != null && !criteria.getFirstName().trim().isEmpty()) {
                    // Only first name
                    results = personRepository.findByFirstNameContainingIgnoreCase(criteria.getFirstName().trim());
                } else if (criteria.getLastName() != null && !criteria.getLastName().trim().isEmpty()) {
                    // Only last name
                    results = personRepository.findByLastNameContainingIgnoreCase(criteria.getLastName().trim());
                }
            } else if (criteria.isSsnSearch()) {
                // Search by SSN
                String ssn = criteria.getSsn().trim().replaceAll("[^0-9-]", "");
                
                // If full SSN format (XXX-XX-XXXX), search exact match
                if (ssn.matches("^\\d{3}-\\d{2}-\\d{4}$")) {
                    Optional<PersonEntity> person = personRepository.findBySsn(ssn);
                    if (person.isPresent()) {
                        results.add(person.get());
                    }
                } else if (ssn.length() == 4) {
                    // Last 4 digits only - search by ending
                    results = personRepository.findBySsnEndingWith(ssn);
                } else {
                    // Try to format and search
                    if (ssn.length() == 9) {
                        String formattedSsn = ssn.substring(0, 3) + "-" + ssn.substring(3, 5) + "-" + ssn.substring(5);
                        Optional<PersonEntity> person = personRepository.findBySsn(formattedSsn);
                        if (person.isPresent()) {
                            results.add(person.get());
                        }
                    }
                }
            }
            
            // Convert entities to DTOs with masking
            List<PersonDTO> personDTOs = results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            System.out.println("✅ PersonService: Found " + personDTOs.size() + " persons");
            return new PersonSearchResponse(true, personDTOs);
            
        } catch (Exception e) {
            System.err.println("❌ PersonService: Error searching persons: " + e.getMessage());
            e.printStackTrace();
            return new PersonSearchResponse(false, "Error searching persons: " + e.getMessage());
        }
    }
    
    /**
     * Create a new person record
     */
    public PersonEntity createPerson(PersonDTO personDTO, String createdBy) {
        System.out.println("➕ PersonService: Creating new person: " + personDTO.getFirstName() + " " + personDTO.getLastName());
        
        // Check if person with same SSN already exists
        if (personDTO.getSsn() != null && !personDTO.getSsn().trim().isEmpty()) {
            String ssn = formatSsn(personDTO.getSsn());
            if (personRepository.existsBySsn(ssn)) {
                throw new IllegalArgumentException("Person with SSN " + personDTO.getMaskedSsn() + " already exists");
            }
            personDTO.setSsn(ssn);
        }
        
        // Check for duplicate by name and DOB
        if (personDTO.getDateOfBirth() != null) {
            Optional<PersonEntity> existing = personRepository.findByFirstNameAndLastNameAndDateOfBirth(
                personDTO.getFirstName(), personDTO.getLastName(), personDTO.getDateOfBirth());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Person with same name and date of birth already exists");
            }
        }
        
        PersonEntity entity = convertToEntity(personDTO);
        entity.setCreatedBy(createdBy);
        
        PersonEntity saved = personRepository.save(entity);
        System.out.println("✅ PersonService: Person created with ID: " + saved.getPersonId());
        return saved;
    }
    
    /**
     * Get person by ID
     */
    public PersonDTO getPersonById(Long personId) {
        Optional<PersonEntity> person = personRepository.findById(personId);
        if (person.isPresent()) {
            return convertToDTO(person.get());
        }
        throw new IllegalArgumentException("Person not found with ID: " + personId);
    }
    
    /**
     * Update person
     */
    public PersonEntity updatePerson(Long personId, PersonDTO personDTO) {
        Optional<PersonEntity> existing = personRepository.findById(personId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Person not found with ID: " + personId);
        }
        
        PersonEntity entity = existing.get();
        updateEntityFromDTO(entity, personDTO);
        
        return personRepository.save(entity);
    }
    
    /**
     * Convert PersonEntity to PersonDTO with SSN masking
     */
    private PersonDTO convertToDTO(PersonEntity entity) {
        PersonDTO dto = new PersonDTO();
        dto.setPersonId(entity.getPersonId());
        dto.setFirstName(entity.getFirstName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setLastName(entity.getLastName());
        dto.setSuffix(entity.getSuffix());
        dto.setSsn(entity.getSsn()); // Full SSN (will be masked in controller if needed)
        dto.setMaskedSsn(entity.getMaskedSsn()); // Masked version
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setGender(entity.getGender());
        dto.setEthnicity(entity.getEthnicity());
        dto.setPreferredSpokenLanguage(entity.getPreferredSpokenLanguage());
        dto.setPreferredWrittenLanguage(entity.getPreferredWrittenLanguage());
        dto.setPrimaryPhone(entity.getPrimaryPhone());
        dto.setSecondaryPhone(entity.getSecondaryPhone());
        dto.setEmail(entity.getEmail());
        dto.setResidenceAddressLine1(entity.getResidenceAddressLine1());
        dto.setResidenceAddressLine2(entity.getResidenceAddressLine2());
        dto.setResidenceCity(entity.getResidenceCity());
        dto.setResidenceState(entity.getResidenceState());
        dto.setResidenceZip(entity.getResidenceZip());
        dto.setMailingAddressLine1(entity.getMailingAddressLine1());
        dto.setMailingAddressLine2(entity.getMailingAddressLine2());
        dto.setMailingCity(entity.getMailingCity());
        dto.setMailingState(entity.getMailingState());
        dto.setMailingZip(entity.getMailingZip());
        dto.setMailingSameAsResidence(entity.getMailingSameAsResidence());
        dto.setCountyOfResidence(entity.getCountyOfResidence());
        dto.setGuardianConservatorName(entity.getGuardianConservatorName());
        dto.setGuardianConservatorAddress(entity.getGuardianConservatorAddress());
        dto.setGuardianConservatorPhone(entity.getGuardianConservatorPhone());
        dto.setDisasterPreparednessCode(entity.getDisasterPreparednessCode());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }
    
    /**
     * Convert PersonDTO to PersonEntity
     */
    private PersonEntity convertToEntity(PersonDTO dto) {
        PersonEntity entity = new PersonEntity();
        entity.setFirstName(dto.getFirstName());
        entity.setMiddleName(dto.getMiddleName());
        entity.setLastName(dto.getLastName());
        entity.setSuffix(dto.getSuffix());
        entity.setSsn(formatSsn(dto.getSsn()));
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setGender(dto.getGender());
        entity.setEthnicity(dto.getEthnicity());
        entity.setPreferredSpokenLanguage(dto.getPreferredSpokenLanguage());
        entity.setPreferredWrittenLanguage(dto.getPreferredWrittenLanguage());
        entity.setPrimaryPhone(dto.getPrimaryPhone());
        entity.setSecondaryPhone(dto.getSecondaryPhone());
        entity.setEmail(dto.getEmail());
        entity.setResidenceAddressLine1(dto.getResidenceAddressLine1());
        entity.setResidenceAddressLine2(dto.getResidenceAddressLine2());
        entity.setResidenceCity(dto.getResidenceCity());
        entity.setResidenceState(dto.getResidenceState());
        entity.setResidenceZip(dto.getResidenceZip());
        entity.setMailingAddressLine1(dto.getMailingAddressLine1());
        entity.setMailingAddressLine2(dto.getMailingAddressLine2());
        entity.setMailingCity(dto.getMailingCity());
        entity.setMailingState(dto.getMailingState());
        entity.setMailingZip(dto.getMailingZip());
        entity.setMailingSameAsResidence(dto.getMailingSameAsResidence());
        entity.setCountyOfResidence(dto.getCountyOfResidence());
        entity.setGuardianConservatorName(dto.getGuardianConservatorName());
        entity.setGuardianConservatorAddress(dto.getGuardianConservatorAddress());
        entity.setGuardianConservatorPhone(dto.getGuardianConservatorPhone());
        entity.setDisasterPreparednessCode(dto.getDisasterPreparednessCode());
        return entity;
    }
    
    /**
     * Update entity from DTO
     */
    private void updateEntityFromDTO(PersonEntity entity, PersonDTO dto) {
        if (dto.getFirstName() != null) entity.setFirstName(dto.getFirstName());
        if (dto.getMiddleName() != null) entity.setMiddleName(dto.getMiddleName());
        if (dto.getLastName() != null) entity.setLastName(dto.getLastName());
        if (dto.getSuffix() != null) entity.setSuffix(dto.getSuffix());
        if (dto.getSsn() != null) entity.setSsn(formatSsn(dto.getSsn()));
        if (dto.getDateOfBirth() != null) entity.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) entity.setGender(dto.getGender());
        if (dto.getEthnicity() != null) entity.setEthnicity(dto.getEthnicity());
        if (dto.getPreferredSpokenLanguage() != null) entity.setPreferredSpokenLanguage(dto.getPreferredSpokenLanguage());
        if (dto.getPreferredWrittenLanguage() != null) entity.setPreferredWrittenLanguage(dto.getPreferredWrittenLanguage());
        if (dto.getPrimaryPhone() != null) entity.setPrimaryPhone(dto.getPrimaryPhone());
        if (dto.getSecondaryPhone() != null) entity.setSecondaryPhone(dto.getSecondaryPhone());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getResidenceAddressLine1() != null) entity.setResidenceAddressLine1(dto.getResidenceAddressLine1());
        if (dto.getResidenceAddressLine2() != null) entity.setResidenceAddressLine2(dto.getResidenceAddressLine2());
        if (dto.getResidenceCity() != null) entity.setResidenceCity(dto.getResidenceCity());
        if (dto.getResidenceState() != null) entity.setResidenceState(dto.getResidenceState());
        if (dto.getResidenceZip() != null) entity.setResidenceZip(dto.getResidenceZip());
        if (dto.getMailingAddressLine1() != null) entity.setMailingAddressLine1(dto.getMailingAddressLine1());
        if (dto.getMailingAddressLine2() != null) entity.setMailingAddressLine2(dto.getMailingAddressLine2());
        if (dto.getMailingCity() != null) entity.setMailingCity(dto.getMailingCity());
        if (dto.getMailingState() != null) entity.setMailingState(dto.getMailingState());
        if (dto.getMailingZip() != null) entity.setMailingZip(dto.getMailingZip());
        if (dto.getMailingSameAsResidence() != null) entity.setMailingSameAsResidence(dto.getMailingSameAsResidence());
        if (dto.getCountyOfResidence() != null) entity.setCountyOfResidence(dto.getCountyOfResidence());
        if (dto.getGuardianConservatorName() != null) entity.setGuardianConservatorName(dto.getGuardianConservatorName());
        if (dto.getGuardianConservatorAddress() != null) entity.setGuardianConservatorAddress(dto.getGuardianConservatorAddress());
        if (dto.getGuardianConservatorPhone() != null) entity.setGuardianConservatorPhone(dto.getGuardianConservatorPhone());
        if (dto.getDisasterPreparednessCode() != null) entity.setDisasterPreparednessCode(dto.getDisasterPreparednessCode());
    }
    
    /**
     * Format SSN to XXX-XX-XXXX format
     */
    private String formatSsn(String ssn) {
        if (ssn == null || ssn.trim().isEmpty()) {
            return null;
        }
        // Remove all non-digits
        String digits = ssn.replaceAll("[^0-9]", "");
        if (digits.length() != 9) {
            throw new IllegalArgumentException("SSN must contain exactly 9 digits");
        }
        return digits.substring(0, 3) + "-" + digits.substring(3, 5) + "-" + digits.substring(5);
    }
}






