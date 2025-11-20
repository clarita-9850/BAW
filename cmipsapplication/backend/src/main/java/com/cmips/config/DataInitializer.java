package com.cmips.config;

import com.cmips.entity.ProviderRecipientRelationship;
import com.cmips.repository.ProviderRecipientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private ProviderRecipientRepository providerRecipientRepository;
    
    @Override
    public void run(String... args) throws Exception {
        initializeProviderRecipientRelationships();
    }
    
    private void initializeProviderRecipientRelationships() {
        if (providerRecipientRepository.count() > 0) {
            System.out.println("Provider-Recipient relationships already initialized");
            return;
        }
        
        System.out.println("Initializing Provider-Recipient relationships...");
        
        // Create relationship: provider1 → recipient1
        ProviderRecipientRelationship rel1 = new ProviderRecipientRelationship();
        rel1.setProviderId("f22dca91-4dc6-490d-b9e4-5a6b54211044"); // provider1's Keycloak UUID
        rel1.setProviderName("provider1");
        rel1.setRecipientId("recipient1-uuid");
        rel1.setRecipientName("recipient1");
        rel1.setCaseNumber("CASE-001");
        rel1.setAuthorizedHoursPerMonth(40);
        rel1.setStatus("ACTIVE");
        rel1.setRelationship("PRIMARY");
        rel1.setStartDate(LocalDate.now().minusMonths(6));
        rel1.setCounty("Sacramento");
        rel1.setDistrict("District 1");
        providerRecipientRepository.save(rel1);
        
        System.out.println("✅ Created Provider-Recipient relationship: provider1 → recipient1");
    }
}


