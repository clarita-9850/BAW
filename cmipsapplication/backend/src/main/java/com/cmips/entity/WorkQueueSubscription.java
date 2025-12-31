package com.cmips.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_queue_subscriptions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username", "work_queue"})
})
public class WorkQueueSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "work_queue", nullable = false)
    private String workQueue;

    @Column(name = "subscribed_by")
    private String subscribedBy; // Supervisor who added this subscription

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public WorkQueueSubscription() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWorkQueue() {
        return workQueue;
    }

    public void setWorkQueue(String workQueue) {
        this.workQueue = workQueue;
    }

    public String getSubscribedBy() {
        return subscribedBy;
    }

    public void setSubscribedBy(String subscribedBy) {
        this.subscribedBy = subscribedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Builder pattern
    public static WorkQueueSubscriptionBuilder builder() {
        return new WorkQueueSubscriptionBuilder();
    }

    public static class WorkQueueSubscriptionBuilder {
        private Long id;
        private String username;
        private String workQueue;
        private String subscribedBy;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        WorkQueueSubscriptionBuilder() {
        }

        public WorkQueueSubscriptionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public WorkQueueSubscriptionBuilder username(String username) {
            this.username = username;
            return this;
        }

        public WorkQueueSubscriptionBuilder workQueue(String workQueue) {
            this.workQueue = workQueue;
            return this;
        }

        public WorkQueueSubscriptionBuilder subscribedBy(String subscribedBy) {
            this.subscribedBy = subscribedBy;
            return this;
        }

        public WorkQueueSubscriptionBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public WorkQueueSubscriptionBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public WorkQueueSubscription build() {
            WorkQueueSubscription subscription = new WorkQueueSubscription();
            subscription.setId(id);
            subscription.setUsername(username);
            subscription.setWorkQueue(workQueue);
            subscription.setSubscribedBy(subscribedBy);
            subscription.setCreatedAt(createdAt);
            subscription.setUpdatedAt(updatedAt);
            return subscription;
        }
    }
}
