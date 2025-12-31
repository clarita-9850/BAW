package com.example.kafkaeventdrivenapp.service;

import com.example.kafkaeventdrivenapp.entity.CaseEntity;
import com.example.kafkaeventdrivenapp.entity.PersonEntity;
import com.example.kafkaeventdrivenapp.model.CaseCreationRequest;
import com.example.kafkaeventdrivenapp.model.CaseCreationResponse;
import com.example.kafkaeventdrivenapp.model.PersonDTO;
import com.example.kafkaeventdrivenapp.repository.CaseRepository;
import com.example.kafkaeventdrivenapp.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class CaseService {
    
    @Autowired
    private CaseRepository caseRepository;
    
    @Autowired
    private PersonRepository personRepository;
    
    @Autowired
    private PersonService personService;
    
    @Autowired
    private CountyCodeMappingService countyCodeMappingService;
    
    private static final Random random = new Random();
    
    /**
     * Create a new case for a recipient
     */
    public CaseCreationResponse createCase(CaseCreationRequest request, String caseworkerId) {
        System.out.println("➕ CaseService: Creating new case for caseworker: " + caseworkerId);
        
        if (!request.isValid()) {
            return CaseCreationResponse.failure("Invalid case creation request. Please provide person data or person ID, and county code.");
        }
        
        try {
            PersonEntity person;
            
            // Create or get person
            if (request.isCreatingNewPerson()) {
                // Create new person
                PersonDTO personDTO = request.getPersonData();
                person = personService.createPerson(personDTO, caseworkerId);
            } else if (request.isUsingExistingPerson()) {
                // Use existing person
                Optional<PersonEntity> existingPerson = personRepository.findById(request.getPersonId());
                if (existingPerson.isEmpty()) {
                    return CaseCreationResponse.failure("Person not found with ID: " + request.getPersonId());
                }
                person = existingPerson.get();
            } else {
                return CaseCreationResponse.failure("Either person data or person ID must be provided");
            }
            
            // Generate case numbers
            String cmipsCaseNumber = generateCmipsCaseNumber();
            String countyCode = request.getCountyCode() != null ? request.getCountyCode() : person.getCountyOfResidence();
            String legacyCaseNumber = generateLegacyCaseNumber(countyCode);
            
            // Create case entity
            CaseEntity caseEntity = new CaseEntity();
            caseEntity.setPerson(person);
            caseEntity.setCmipsCaseNumber(cmipsCaseNumber);
            caseEntity.setLegacyCaseNumber(legacyCaseNumber);
            caseEntity.setCaseStatus("PENDING");
            caseEntity.setCountyCode(countyCode);
            caseEntity.setAssignedCaseworkerId(caseworkerId);
            caseEntity.setCaseOpenedDate(request.getCaseOpenedDate() != null ? request.getCaseOpenedDate() : LocalDate.now());
            caseEntity.setCaseNotes(request.getCaseNotes());
            caseEntity.setCreatedBy(caseworkerId);
            
            // Set disaster preparedness code if provided
            if (request.getDisasterPreparednessCode() != null && !request.getDisasterPreparednessCode().isEmpty()) {
                person.setDisasterPreparednessCode(request.getDisasterPreparednessCode());
                personRepository.save(person);
            }
            
            CaseEntity savedCase = caseRepository.save(caseEntity);
            System.out.println("✅ CaseService: Case created with ID: " + savedCase.getCaseId() + ", CMIPS Number: " + cmipsCaseNumber);
            
            return CaseCreationResponse.success(
                savedCase.getCaseId(),
                savedCase.getCmipsCaseNumber(),
                savedCase.getLegacyCaseNumber(),
                person.getPersonId(),
                savedCase.getCaseStatus(),
                savedCase.getAssignedCaseworkerId(),
                savedCase.getCaseOpenedDate(),
                savedCase.getCreatedAt()
            );
            
        } catch (IllegalArgumentException e) {
            System.err.println("❌ CaseService: Validation error: " + e.getMessage());
            return CaseCreationResponse.failure(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ CaseService: Error creating case: " + e.getMessage());
            e.printStackTrace();
            return CaseCreationResponse.failure("Error creating case: " + e.getMessage());
        }
    }
    
    /**
     * Generate a unique 7-digit CMIPS case number
     */
    public String generateCmipsCaseNumber() {
        String caseNumber;
        int attempts = 0;
        int maxAttempts = 100;
        
        do {
            // Generate 7-digit number (1000000 to 9999999)
            int number = 1000000 + random.nextInt(9000000);
            caseNumber = String.format("%07d", number);
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Unable to generate unique CMIPS case number after " + maxAttempts + " attempts");
            }
        } while (caseRepository.existsByCmipsCaseNumber(caseNumber));
        
        System.out.println("🔢 CaseService: Generated CMIPS case number: " + caseNumber);
        return caseNumber;
    }
    
    /**
     * Generate a unique 10-digit legacy case number
     * Format: 2-digit county code + 8-digit case number
     */
    public String generateLegacyCaseNumber(String countyCode) {
        // Get county code (2 digits)
        String countyCodeDigits = getCountyCodeDigits(countyCode);
        
        String caseNumber;
        int attempts = 0;
        int maxAttempts = 100;
        
        do {
            // Generate 8-digit number (10000000 to 99999999)
            int number = 10000000 + random.nextInt(90000000);
            caseNumber = countyCodeDigits + String.format("%08d", number);
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Unable to generate unique legacy case number after " + maxAttempts + " attempts");
            }
        } while (caseRepository.existsByLegacyCaseNumber(caseNumber));
        
        System.out.println("🔢 CaseService: Generated legacy case number: " + caseNumber);
        return caseNumber;
    }
    
    /**
     * Get 2-digit county code from county name or county code
     * Maps common California counties to their numeric codes
     * Also handles county codes (CT1-CT5) by mapping them to county names first
     */
    private String getCountyCodeDigits(String countyNameOrCode) {
        if (countyNameOrCode == null) {
            return "00"; // Default
        }
        
        // Check if input is a county code (CT1-CT5)
        if (countyCodeMappingService.isValidCountyCode(countyNameOrCode)) {
            String countyName = countyCodeMappingService.getCountyName(countyNameOrCode);
            if (countyName != null) {
                countyNameOrCode = countyName; // Use mapped county name
            }
        }
        
        // Map county names to 2-digit codes (simplified mapping)
        Map<String, String> countyCodes = Map.of(
            "Alameda", "01",
            "Los Angeles", "19",
            "Orange", "59",
            "Riverside", "65",
            "Sacramento", "67",
            "San Diego", "73",
            "San Francisco", "75",
            "Santa Clara", "85"
        );
        
        String code = countyCodes.get(countyNameOrCode);
        if (code != null) {
            return code;
        }
        
        // If not found, generate a code based on county name hash
        int hash = Math.abs(countyNameOrCode.hashCode());
        return String.format("%02d", hash % 100);
    }
    
    /**
     * Get case by ID
     */
    public CaseEntity getCaseById(Long caseId) {
        Optional<CaseEntity> caseEntity = caseRepository.findById(caseId);
        if (caseEntity.isEmpty()) {
            throw new IllegalArgumentException("Case not found with ID: " + caseId);
        }
        return caseEntity.get();
    }
    
    /**
     * Get cases for a person
     */
    public List<CaseEntity> getCasesByPersonId(Long personId) {
        return caseRepository.findByPerson_PersonId(personId);
    }
    
    /**
     * Get cases assigned to a caseworker
     */
    public List<CaseEntity> getCasesByCaseworker(String caseworkerId) {
        return caseRepository.findByAssignedCaseworkerId(caseworkerId);
    }
    
    /**
     * Get active cases for a caseworker
     */
    public List<CaseEntity> getActiveCasesByCaseworker(String caseworkerId) {
        return caseRepository.findActiveCasesByCaseworker(caseworkerId);
    }
    
    /**
     * Activate a case
     */
    public CaseEntity activateCase(Long caseId) {
        CaseEntity caseEntity = getCaseById(caseId);
        caseEntity.activateCase();
        return caseRepository.save(caseEntity);
    }
    
    /**
     * Close a case
     */
    public CaseEntity closeCase(Long caseId) {
        CaseEntity caseEntity = getCaseById(caseId);
        caseEntity.closeCase();
        return caseRepository.save(caseEntity);
    }
}

