package com.cmips.service;

import com.cmips.entity.WorkQueueSubscription;
import com.cmips.repository.WorkQueueSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkQueueSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(WorkQueueSubscriptionService.class);

    private final WorkQueueSubscriptionRepository subscriptionRepository;
    private final WorkQueueCatalogService catalogService;

    public WorkQueueSubscriptionService(WorkQueueSubscriptionRepository subscriptionRepository,
                                        WorkQueueCatalogService catalogService) {
        this.subscriptionRepository = subscriptionRepository;
        this.catalogService = catalogService;
    }

    /**
     * Subscribe a user to a work queue
     */
    @Transactional
    public WorkQueueSubscription subscribeUserToQueue(String username, String workQueue, String subscribedBy) {
        log.info("Subscribing user {} to queue {} by {}", username, workQueue, subscribedBy);

        // Prevent subscribing to supervisor-only queues (like ESCALATED)
        if (catalogService.isSupervisorOnly(workQueue)) {
            throw new IllegalArgumentException("Cannot subscribe to supervisor-only queue: " + workQueue);
        }

        // Check if subscription already exists
        Optional<WorkQueueSubscription> existing = subscriptionRepository
            .findByUsernameAndWorkQueue(username, workQueue);

        if (existing.isPresent()) {
            log.warn("User {} is already subscribed to queue {}", username, workQueue);
            return existing.get();
        }

        WorkQueueSubscription subscription = WorkQueueSubscription.builder()
            .username(username)
            .workQueue(workQueue)
            .subscribedBy(subscribedBy)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        return subscriptionRepository.save(subscription);
    }

    /**
     * Unsubscribe a user from a work queue
     */
    @Transactional
    public void unsubscribeUserFromQueue(String username, String workQueue) {
        log.info("Unsubscribing user {} from queue {}", username, workQueue);
        subscriptionRepository.deleteByUsernameAndWorkQueue(username, workQueue);
    }

    /**
     * Get all queues a user is subscribed to
     */
    public List<String> getUserQueues(String username) {
        log.info("Getting queues for user {}", username);
        return subscriptionRepository.findByUsername(username).stream()
            .map(WorkQueueSubscription::getWorkQueue)
            .toList();
    }

    /**
     * Get all users subscribed to a queue
     */
    public List<String> getQueueSubscribers(String workQueue) {
        log.info("Getting subscribers for queue {}", workQueue);
        return subscriptionRepository.findByWorkQueue(workQueue).stream()
            .map(WorkQueueSubscription::getUsername)
            .toList();
    }

    /**
     * Get all subscriptions for a queue (with full details)
     */
    public List<WorkQueueSubscription> getQueueSubscriptions(String workQueue) {
        log.info("Getting full subscription details for queue {}", workQueue);
        return subscriptionRepository.findByWorkQueue(workQueue);
    }

    /**
     * Get all subscriptions for a user (with full details)
     */
    public List<WorkQueueSubscription> getUserSubscriptions(String username) {
        log.info("Getting full subscription details for user {}", username);
        return subscriptionRepository.findByUsername(username);
    }

    /**
     * Check if user is subscribed to a queue
     */
    public boolean isUserSubscribed(String username, String workQueue) {
        return subscriptionRepository.findByUsernameAndWorkQueue(username, workQueue).isPresent();
    }
}
