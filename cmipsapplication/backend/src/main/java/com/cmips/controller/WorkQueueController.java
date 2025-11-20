package com.cmips.controller;

import com.cmips.entity.WorkQueueSubscription;
import com.cmips.service.WorkQueueCatalogService;
import com.cmips.service.TaskService;
import com.cmips.service.WorkQueueSubscriptionService;
import com.cmips.service.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-queues")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class WorkQueueController {
    
    private final WorkQueueCatalogService catalogService;
    private final TaskService taskService;
    private final WorkQueueSubscriptionService subscriptionService;
    private final KeycloakAdminService keycloakAdminService;
    
    /**
     * Get all predefined work queues
     * GET /api/work-queues/catalog
     */
    @GetMapping("/catalog")
    public ResponseEntity<List<WorkQueueCatalogService.WorkQueueInfo>> getQueueCatalog() {
        log.info("GET /api/work-queues/catalog - Request received");
        try {
            List<WorkQueueCatalogService.WorkQueueInfo> queues = catalogService.getAllQueues();
            log.info("Returning {} queues", queues.size());
            return ResponseEntity.ok(queues);
        } catch (Exception e) {
            log.error("Error getting queue catalog: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(List.of());
        }
    }
    
    /**
     * Get tasks for a specific queue
     * GET /api/work-queues/{queueName}/tasks
     */
    @GetMapping("/{queueName}/tasks")
    public ResponseEntity<List<com.cmips.entity.Task>> getQueueTasks(@PathVariable String queueName) {
        try {
            List<com.cmips.entity.Task> tasks = taskService.getQueueTasks(queueName);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            log.error("Error getting tasks for queue {}: {}", queueName, e.getMessage(), e);
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    /**
     * Get all queues with their task counts
     * GET /api/work-queues/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getQueuesSummary() {
        try {
            Map<String, Object> summary = new HashMap<>();
            List<WorkQueueCatalogService.WorkQueueInfo> queues = catalogService.getAllQueues();
            
            for (WorkQueueCatalogService.WorkQueueInfo queue : queues) {
                List<com.cmips.entity.Task> tasks = taskService.getQueueTasks(queue.getName());
                Map<String, Object> queueData = new HashMap<>();
                queueData.put("name", queue.getName());
                queueData.put("displayName", queue.getDisplayName());
                queueData.put("description", queue.getDescription());
                queueData.put("supervisorOnly", queue.isSupervisorOnly());
                queueData.put("taskCount", tasks.size());
                queueData.put("tasks", tasks);
                summary.put(queue.getName(), queueData);
            }
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting queues summary: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of());
        }
    }
    
    /**
     * Get all users (for supervisor to add to queues)
     * GET /api/work-queues/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            log.info("Getting all users for queue subscription");
            List<Map<String, Object>> users = keycloakAdminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    /**
     * Subscribe a user to a work queue
     * POST /api/work-queues/subscribe
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribeUserToQueue(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String workQueue = request.get("workQueue");
            String subscribedBy = request.get("subscribedBy"); // Supervisor username
            
            if (username == null || workQueue == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "username and workQueue are required"));
            }
            
            WorkQueueSubscription subscription = subscriptionService.subscribeUserToQueue(
                username, workQueue, subscribedBy
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User subscribed to queue successfully",
                "subscription", subscription
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error subscribing user to queue: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error subscribing user to queue: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to subscribe user to queue: " + e.getMessage()));
        }
    }
    
    /**
     * Unsubscribe a user from a work queue
     * DELETE /api/work-queues/unsubscribe
     */
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribeUserFromQueue(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String workQueue = request.get("workQueue");
            
            if (username == null || workQueue == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "username and workQueue are required"));
            }
            
            subscriptionService.unsubscribeUserFromQueue(username, workQueue);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User unsubscribed from queue successfully"
            ));
        } catch (Exception e) {
            log.error("Error unsubscribing user from queue: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to unsubscribe user from queue: " + e.getMessage()));
        }
    }
    
    /**
     * Get all subscribers for a queue
     * GET /api/work-queues/{queueName}/subscribers
     */
    @GetMapping("/{queueName}/subscribers")
    public ResponseEntity<List<WorkQueueSubscription>> getQueueSubscribers(@PathVariable String queueName) {
        try {
            List<WorkQueueSubscription> subscribers = subscriptionService.getQueueSubscriptions(queueName);
            return ResponseEntity.ok(subscribers);
        } catch (Exception e) {
            log.error("Error getting subscribers for queue {}: {}", queueName, e.getMessage(), e);
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    /**
     * Get full subscription details for a queue
     * GET /api/work-queues/queue/{workQueue}/details
     */
    @GetMapping("/queue/{workQueue}/details")
    public ResponseEntity<List<WorkQueueSubscription>> getQueueSubscriptionDetails(@PathVariable String workQueue) {
        log.info("GET /api/work-queues/queue/{}/details - Request received", workQueue);
        try {
            List<WorkQueueSubscription> subscriptions = subscriptionService.getQueueSubscriptions(workQueue);
            log.info("Returning {} subscriptions for queue {}", subscriptions.size(), workQueue);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            log.error("Error getting queue subscriptions for {}: {}", workQueue, e.getMessage(), e);
            return ResponseEntity.status(500).body(List.of());
        }
    }
}

