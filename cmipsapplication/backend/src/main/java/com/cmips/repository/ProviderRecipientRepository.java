package com.cmips.repository;

import com.cmips.entity.ProviderRecipientRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderRecipientRepository extends JpaRepository<ProviderRecipientRelationship, Long> {
    
    List<ProviderRecipientRelationship> findByProviderId(String providerId);
    
    List<ProviderRecipientRelationship> findByRecipientId(String recipientId);
    
    List<ProviderRecipientRelationship> findByProviderIdAndStatus(String providerId, String status);
    
    List<ProviderRecipientRelationship> findByRecipientIdAndStatus(String recipientId, String status);
    
    List<ProviderRecipientRelationship> findByCounty(String county);
    
    List<ProviderRecipientRelationship> findByDistrict(String district);
}


