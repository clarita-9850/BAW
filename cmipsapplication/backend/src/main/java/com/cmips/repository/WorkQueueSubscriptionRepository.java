package com.cmips.repository;

import com.cmips.entity.WorkQueueSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkQueueSubscriptionRepository extends JpaRepository<WorkQueueSubscription, Long> {
    
    // Find all subscriptions for a user
    List<WorkQueueSubscription> findByUsername(String username);
    
    // Find all users subscribed to a queue
    List<WorkQueueSubscription> findByWorkQueue(String workQueue);
    
    // Check if a user is subscribed to a queue
    Optional<WorkQueueSubscription> findByUsernameAndWorkQueue(String username, String workQueue);
    
    // Delete a specific subscription
    void deleteByUsernameAndWorkQueue(String username, String workQueue);
    
    // Get all unique queues
    @Query("SELECT DISTINCT wqs.workQueue FROM WorkQueueSubscription wqs")
    List<String> findDistinctWorkQueue();
}

