package com.cmips.controller;

import com.cmips.entity.PersonEntity;
import com.cmips.model.PersonDTO;
import com.cmips.model.PersonSearchCriteria;
import com.cmips.model.PersonSearchResponse;
import com.cmips.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/person")
@CrossOrigin(origins = "*")
public class PersonController {
    
    @Autowired
    private PersonService personService;
    
    /**
     * Search persons by name or SSN
     * Requires CASE_WORKER role
     */
    @PostMapping("/search")
    public ResponseEntity<PersonSearchResponse> searchPersons(@RequestBody PersonSearchCriteria criteria) {
        try {
            System.out.println("🔍 PersonController: Search request received");
            
            // Validate search criteria
            if (!criteria.isValid()) {
                return ResponseEntity.badRequest()
                    .body(new PersonSearchResponse(false, "Invalid search criteria. Please provide name or SSN."));
            }
            
            PersonSearchResponse response = personService.searchPersons(criteria);
            
            // Apply SSN masking to results
            if (response.getResults() != null) {
                for (PersonDTO person : response.getResults()) {
                    // Mask SSN - show only last 4 digits
                    if (person.getSsn() != null && person.getSsn().length() >= 4) {
                        String masked = "***-**-" + person.getSsn().substring(person.getSsn().length() - 4);
                        person.setMaskedSsn(masked);
                        person.setSsn(null); // Remove full SSN from response
                    }
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ PersonController: Error searching persons: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(new PersonSearchResponse(false, "Error searching persons: " + e.getMessage()));
        }
    }
    
    /**
     * Get person by ID
     * Requires CASE_WORKER role
     */
    @GetMapping("/{id}")
    public ResponseEntity<PersonDTO> getPersonById(@PathVariable Long id) {
        try {
            PersonDTO person = personService.getPersonById(id);
            
            // Mask SSN in response
            if (person.getSsn() != null && person.getSsn().length() >= 4) {
                String masked = "***-**-" + person.getSsn().substring(person.getSsn().length() - 4);
                person.setMaskedSsn(masked);
                person.setSsn(null); // Remove full SSN from response
            }
            
            return ResponseEntity.ok(person);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ PersonController: Error getting person: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Create new person
     * Requires CASE_WORKER role
     */
    @PostMapping
    public ResponseEntity<PersonDTO> createPerson(@RequestBody PersonDTO personDTO) {
        try {
            String createdBy = getCurrentUsername();
            
            PersonEntity saved = personService.createPerson(personDTO, createdBy);
            
            // Convert to DTO and mask SSN
            PersonDTO response = personService.getPersonById(saved.getPersonId());
            if (response.getSsn() != null && response.getSsn().length() >= 4) {
                String masked = "***-**-" + response.getSsn().substring(response.getSsn().length() - 4);
                response.setMaskedSsn(masked);
                response.setSsn(null);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ PersonController: Error creating person: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update person
     * Requires CASE_WORKER role
     */
    @PutMapping("/{id}")
    public ResponseEntity<PersonDTO> updatePerson(@PathVariable Long id, @RequestBody PersonDTO personDTO) {
        try {
            personService.updatePerson(id, personDTO);
            PersonDTO updated = personService.getPersonById(id);
            
            // Mask SSN in response
            if (updated.getSsn() != null && updated.getSsn().length() >= 4) {
                String masked = "***-**-" + updated.getSsn().substring(updated.getSsn().length() - 4);
                updated.setMaskedSsn(masked);
                updated.setSsn(null);
            }
            
            return ResponseEntity.ok(updated);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ PersonController: Error updating person: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get current username from security context
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}






