package com.cmips.controller;

import com.cmips.entity.ReferralEntity;
import com.cmips.entity.ReferralEntity.*;
import com.cmips.service.ReferralService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Referral Management
 * Implements DSD Section 20 - Initial Contact & Referral Processing
 */
@RestController
@RequestMapping("/api/referrals")
@CrossOrigin(origins = "*")
public class ReferralController {

    private static final Logger log = LoggerFactory.getLogger(ReferralController.class);

    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    // ==================== CREATE ====================

    /**
     * Create a new referral
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> createReferral(@RequestBody ReferralEntity referral) {
        try {
            String userId = getCurrentUserId();
            ReferralEntity created = referralService.createReferral(referral, userId);
            log.info("Referral created: {} by user: {}", created.getId(), userId);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating referral", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create an external agency referral
     */
    @PostMapping("/external")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> createExternalReferral(@RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();

            // Build referral entity from request
            ReferralEntity referral = buildReferralFromRequest(request);

            String agencyName = (String) request.get("agencyName");
            String agencyContact = (String) request.get("agencyContact");
            String agencyPhone = (String) request.get("agencyPhone");
            String externalRefNumber = (String) request.get("externalReferenceNumber");

            ReferralEntity created = referralService.createExternalReferral(
                    referral, agencyName, agencyContact, agencyPhone, externalRefNumber, userId);

            log.info("External referral created: {} from agency: {}", created.getId(), agencyName);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Error creating external referral", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== UPDATE ====================

    /**
     * Update referral status
     */
    @PatchMapping("/{referralId}/status")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> updateStatus(@PathVariable String referralId,
                                           @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            ReferralStatus newStatus = ReferralStatus.valueOf(request.get("status"));

            ReferralEntity updated = referralService.updateStatus(referralId, newStatus, userId);
            log.info("Referral {} status updated to {} by {}", referralId, newStatus, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating referral status", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Assign referral to worker
     */
    @PatchMapping("/{referralId}/assign")
    @PreAuthorize("hasAnyRole('supervisor', 'admin')")
    public ResponseEntity<?> assignToWorker(@PathVariable String referralId,
                                             @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String workerId = request.get("workerId");
            String workerName = request.get("workerName");

            ReferralEntity updated = referralService.assignToWorker(referralId, workerId, workerName, userId);
            log.info("Referral {} assigned to {} by {}", referralId, workerName, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error assigning referral", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Set follow-up date
     */
    @PatchMapping("/{referralId}/follow-up")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> setFollowUp(@PathVariable String referralId,
                                          @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            LocalDate followUpDate = LocalDate.parse(request.get("followUpDate"));

            ReferralEntity updated = referralService.setFollowUpDate(referralId, followUpDate, userId);
            log.info("Referral {} follow-up set to {} by {}", referralId, followUpDate, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error setting follow-up date", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update referral priority
     */
    @PatchMapping("/{referralId}/priority")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> updatePriority(@PathVariable String referralId,
                                             @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            ReferralPriority priority = ReferralPriority.valueOf(request.get("priority"));

            ReferralEntity updated = referralService.updatePriority(referralId, priority, userId);
            log.info("Referral {} priority updated to {} by {}", referralId, priority, userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating referral priority", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CLOSE / REOPEN ====================

    /**
     * Close referral
     */
    @PostMapping("/{referralId}/close")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> closeReferral(@PathVariable String referralId,
                                            @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            ReferralClosedReason reason = ReferralClosedReason.valueOf(request.get("reason"));
            String reasonDetails = request.get("reasonDetails");

            ReferralEntity closed = referralService.closeReferral(referralId, reason, reasonDetails, userId);
            log.info("Referral {} closed with reason {} by {}", referralId, reason, userId);
            return ResponseEntity.ok(closed);
        } catch (Exception e) {
            log.error("Error closing referral", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reopen closed referral (within 30 days)
     */
    @PostMapping("/{referralId}/reopen")
    @PreAuthorize("hasAnyRole('supervisor', 'admin')")
    public ResponseEntity<?> reopenReferral(@PathVariable String referralId,
                                             @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String reopenReason = request.get("reason");

            ReferralEntity reopened = referralService.reopenReferral(referralId, reopenReason, userId);
            log.info("Referral {} reopened by {}", referralId, userId);
            return ResponseEntity.ok(reopened);
        } catch (Exception e) {
            log.error("Error reopening referral", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== CONVERT TO APPLICATION ====================

    /**
     * Convert referral to application
     */
    @PostMapping("/{referralId}/convert")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> convertToApplication(@PathVariable String referralId,
                                                   @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            String applicationId = (String) request.get("applicationId");
            Long recipientId = ((Number) request.get("recipientId")).longValue();

            ReferralEntity converted = referralService.convertToApplication(
                    referralId, applicationId, recipientId, userId);
            log.info("Referral {} converted to application {} by {}", referralId, applicationId, userId);
            return ResponseEntity.ok(converted);
        } catch (Exception e) {
            log.error("Error converting referral", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Link referral to existing recipient
     */
    @PostMapping("/{referralId}/link-recipient")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> linkToRecipient(@PathVariable String referralId,
                                              @RequestBody Map<String, Object> request) {
        try {
            String userId = getCurrentUserId();
            Long recipientId = ((Number) request.get("recipientId")).longValue();

            ReferralEntity linked = referralService.linkToRecipient(referralId, recipientId, userId);
            log.info("Referral {} linked to recipient {} by {}", referralId, recipientId, userId);
            return ResponseEntity.ok(linked);
        } catch (Exception e) {
            log.error("Error linking referral to recipient", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== QUERY ====================

    /**
     * Get referral by ID
     */
    @GetMapping("/{referralId}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getReferral(@PathVariable String referralId) {
        try {
            ReferralEntity referral = referralService.getReferralById(referralId);
            return ResponseEntity.ok(referral);
        } catch (Exception e) {
            log.error("Error getting referral", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all referrals (with optional filters)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getReferrals(
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workerId) {
        try {
            List<ReferralEntity> referrals;

            if (countyCode != null && status != null) {
                referrals = referralService.getReferralsByCountyAndStatus(
                        countyCode, ReferralStatus.valueOf(status));
            } else if (countyCode != null) {
                referrals = referralService.getReferralsByCounty(countyCode);
            } else if (status != null) {
                referrals = referralService.getReferralsByStatus(ReferralStatus.valueOf(status));
            } else if (workerId != null) {
                referrals = referralService.getReferralsByWorker(workerId);
            } else {
                referrals = referralService.getAllReferrals();
            }

            return ResponseEntity.ok(referrals);
        } catch (Exception e) {
            log.error("Error getting referrals", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get open referrals
     */
    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getOpenReferrals(@RequestParam(required = false) String countyCode) {
        try {
            List<ReferralEntity> referrals;
            if (countyCode != null) {
                referrals = referralService.getOpenReferralsByCounty(countyCode);
            } else {
                referrals = referralService.getOpenReferrals();
            }
            return ResponseEntity.ok(referrals);
        } catch (Exception e) {
            log.error("Error getting open referrals", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get urgent referrals
     */
    @GetMapping("/urgent")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getUrgentReferrals(@RequestParam(required = false) String countyCode) {
        try {
            List<ReferralEntity> referrals;
            if (countyCode != null) {
                referrals = referralService.getUrgentReferralsByCounty(countyCode);
            } else {
                referrals = referralService.getUrgentReferrals();
            }
            return ResponseEntity.ok(referrals);
        } catch (Exception e) {
            log.error("Error getting urgent referrals", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get referrals needing follow-up
     */
    @GetMapping("/follow-up")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getReferralsNeedingFollowUp(@RequestParam(required = false) String countyCode) {
        try {
            List<ReferralEntity> referrals;
            if (countyCode != null) {
                referrals = referralService.getReferralsNeedingFollowUpByCounty(countyCode);
            } else {
                referrals = referralService.getReferralsNeedingFollowUp();
            }
            return ResponseEntity.ok(referrals);
        } catch (Exception e) {
            log.error("Error getting referrals needing follow-up", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Search referrals with multiple criteria
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> searchReferrals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assignedWorkerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<ReferralEntity> referrals = referralService.searchReferrals(
                    status != null ? ReferralStatus.valueOf(status) : null,
                    countyCode,
                    source != null ? ReferralSource.valueOf(source) : null,
                    priority != null ? ReferralPriority.valueOf(priority) : null,
                    assignedWorkerId,
                    startDate != null ? LocalDate.parse(startDate) : null,
                    endDate != null ? LocalDate.parse(endDate) : null
            );
            return ResponseEntity.ok(referrals);
        } catch (Exception e) {
            log.error("Error searching referrals", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== STATISTICS ====================

    /**
     * Get referral statistics by county
     */
    @GetMapping("/stats/{countyCode}")
    @PreAuthorize("hasAnyRole('caseworker', 'supervisor', 'admin')")
    public ResponseEntity<?> getReferralStats(@PathVariable String countyCode) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("countyCode", countyCode);
            stats.put("openCount", referralService.countOpenReferralsByCounty(countyCode));
            stats.put("pendingCount", referralService.countByCountyAndStatus(countyCode, ReferralStatus.PENDING));
            stats.put("inProgressCount", referralService.countByCountyAndStatus(countyCode, ReferralStatus.IN_PROGRESS));
            stats.put("convertedCount", referralService.countByCountyAndStatus(countyCode, ReferralStatus.CONVERTED));
            stats.put("closedCount", referralService.countByCountyAndStatus(countyCode, ReferralStatus.CLOSED));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting referral stats", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getName();
        }
        return "anonymous";
    }

    private ReferralEntity buildReferralFromRequest(Map<String, Object> request) {
        return ReferralEntity.builder()
                .countyCode((String) request.get("countyCode"))
                .source(request.get("source") != null ?
                        ReferralSource.valueOf((String) request.get("source")) : ReferralSource.PHONE)
                .priority(request.get("priority") != null ?
                        ReferralPriority.valueOf((String) request.get("priority")) : ReferralPriority.NORMAL)
                .potentialRecipientName((String) request.get("potentialRecipientName"))
                .potentialRecipientPhone((String) request.get("potentialRecipientPhone"))
                .potentialRecipientAddress((String) request.get("potentialRecipientAddress"))
                .contactFirstName((String) request.get("contactFirstName"))
                .contactLastName((String) request.get("contactLastName"))
                .contactPhone((String) request.get("contactPhone"))
                .contactEmail((String) request.get("contactEmail"))
                .contactRelationship((String) request.get("contactRelationship"))
                .reasonForReferral((String) request.get("reasonForReferral"))
                .servicesNeeded((String) request.get("servicesNeeded"))
                .urgencyDescription((String) request.get("urgencyDescription"))
                .notes((String) request.get("notes"))
                .build();
    }
}
