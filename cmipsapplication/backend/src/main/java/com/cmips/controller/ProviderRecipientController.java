package com.cmips.controller;

import com.cmips.entity.ProviderRecipientRelationship;
import com.cmips.repository.ProviderRecipientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provider-recipient")
@CrossOrigin(origins = "*")
public class ProviderRecipientController {
    
    @Autowired
    private ProviderRecipientRepository providerRecipientRepository;
    
    /**
     * Get provider's assigned recipients
     */
    @GetMapping("/my-recipients")
    public ResponseEntity<List<ProviderRecipientRelationship>> getMyRecipients() {
        String providerId = getCurrentUserId();
        List<ProviderRecipientRelationship> recipients = 
            providerRecipientRepository.findByProviderIdAndStatus(providerId, "ACTIVE");
        return ResponseEntity.ok(recipients);
    }
    
    /**
     * Get recipient's assigned providers
     */
    @GetMapping("/my-providers")
    public ResponseEntity<List<ProviderRecipientRelationship>> getMyProviders() {
        String recipientId = getCurrentUserId();
        List<ProviderRecipientRelationship> providers = 
            providerRecipientRepository.findByRecipientIdAndStatus(recipientId, "ACTIVE");
        return ResponseEntity.ok(providers);
    }
    
    /**
     * Get all relationships (for case workers)
     */
    @GetMapping("/all")
    public ResponseEntity<List<ProviderRecipientRelationship>> getAllRelationships() {
        List<ProviderRecipientRelationship> relationships = providerRecipientRepository.findAll();
        return ResponseEntity.ok(relationships);
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}


