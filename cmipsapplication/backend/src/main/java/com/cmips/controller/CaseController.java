package com.cmips.controller;

import com.cmips.event.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.cmips.entity.Notification;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CaseController {

    private static final Logger log = LoggerFactory.getLogger(CaseController.class);

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;
    private final com.cmips.service.NotificationService notificationService;

    public CaseController(KafkaTemplate<String, BaseEvent> kafkaTemplate,
                          com.cmips.service.NotificationService notificationService) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationService = notificationService;
    }
    
    @PostMapping("/address-change")
    public ResponseEntity<Map<String, String>> submitAddressChange(@RequestBody Map<String, Object> request) {
        try {
            String currentUserId = getCurrentUserId();
            
            // Extract request data
            String recipientId = (String) request.get("recipientId");
            String recipientName = (String) request.get("recipientName");
            String providerId = (String) request.get("providerId");
            String caseId = (String) request.get("caseId");
            @SuppressWarnings("unchecked")
            Map<String, String> newAddress = (Map<String, String>) request.get("newAddress");
            
            // Determine if address is outside California
            String state = newAddress.get("state");
            boolean isOutsideCA = !"CA".equals(state);
            
            // Assign to a real case worker username used by the system
            String caseOwner = "caseworker1";
            
            // Build payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("caseId", caseId);
            payload.put("recipientId", recipientId);
            payload.put("recipientName", recipientName);
            payload.put("providerId", providerId);
            payload.put("owner", caseOwner);
            payload.put("newAddress", newAddress);
            payload.put("state", state);
            payload.put("isOutsideCA", isOutsideCA);
            
            // Create and publish event
            BaseEvent event = BaseEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("case.address.changed")
                .timestamp(Instant.now())
                .userId(currentUserId)
                .source("provider-portal")
                .payload(payload)
                .metadata(BaseEvent.Metadata.builder()
                    .correlationId(UUID.randomUUID().toString())
                    .version("1.0")
                    .build())
                .build();
            
            // Publish to Kafka
            kafkaTemplate.send("cmips-case-events", event);
            log.info("Published address change event for case: {}, state: {}", caseId, state);
            
            // Create notification to recipient about provider address change
            // Send to recipient's username (e.g., "recipient1")
            String street = newAddress.getOrDefault("line1", newAddress.getOrDefault("street", ""));
            String city = newAddress.getOrDefault("city", "");
            String zip = newAddress.getOrDefault("zip", newAddress.getOrDefault("zipCode", ""));
            String fullAddress = String.format("%s, %s, %s %s", street, city, state, zip).trim();
            
            // Determine recipient userId - prefer recipientName, fallback to recipientId, or default to "recipient1"
            String recipientUserId = "recipient1"; // Default
            if (recipientName != null && !recipientName.isBlank()) {
                recipientUserId = recipientName;
            } else if (recipientId != null && !String.valueOf(recipientId).isBlank()) {
                recipientUserId = String.valueOf(recipientId);
            }
            
            log.info("Creating notification for recipient: {}, provider: {}, address: {}", 
                recipientUserId, providerId, fullAddress);
            
            Notification notification = Notification.builder()
                .userId(recipientUserId)
                .message(String.format("Your care giver %s has changed their address to %s", providerId, fullAddress))
                .notificationType(Notification.NotificationType.INFO)
                .actionLink("/recipient/dashboard")
                .relatedEntityType("CASE")
                .readStatus(false)
                .createdAt(java.time.LocalDateTime.now())
                .build();
            notificationService.createNotification(notification);
            log.info("Notification created successfully for user: {}", recipientUserId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Address change submitted successfully");
            response.put("caseId", caseId);
            response.put("willCreateTask", String.valueOf(isOutsideCA));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error submitting address change", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to submit address change: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getName();
        }
        return "anonymous";
    }
}


