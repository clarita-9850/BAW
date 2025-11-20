package com.cmips.repository;

import com.cmips.entity.EVVRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EVVRepository extends JpaRepository<EVVRecord, Long> {
    
    List<EVVRecord> findByProviderId(String providerId);
    
    List<EVVRecord> findByRecipientId(String recipientId);
    
    List<EVVRecord> findByTimesheetId(Long timesheetId);
    
    List<EVVRecord> findByStatus(String status);
    
    Optional<EVVRecord> findByProviderIdAndRecipientIdAndStatus(String providerId, String recipientId, String status);
    
    List<EVVRecord> findByCheckInTimeBetween(LocalDateTime start, LocalDateTime end);
    
    List<EVVRecord> findByProviderIdAndCheckInTimeBetween(String providerId, LocalDateTime start, LocalDateTime end);
}


