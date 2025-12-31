package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.*;
import com.cmips.entity.CaseEntity.CaseStatus;
import com.cmips.service.CaseManagementService;
import com.cmips.service.FieldLevelAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Case Management REST Controller
 * All endpoints are protected by configurable Keycloak permissions
 */
@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CaseManagementController {

    private static final Logger log = LoggerFactory.getLogger(CaseManagementController.class);

    private final CaseManagementService caseManagementService;
    private final FieldLevelAuthorizationService fieldAuthService;

    public CaseManagementController(CaseManagementService caseManagementService,
                                    FieldLevelAuthorizationService fieldAuthService) {
        this.caseManagementService = caseManagementService;
        this.fieldAuthService = fieldAuthService;
    }

    // ==================== CASE CRUD ====================

    @GetMapping
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<Map<String, Object>>> getAllCases(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String caseOwnerId,
            @RequestParam(required = false) String status) {

        List<CaseEntity> cases;
        if (countyCode != null) {
            cases = caseManagementService.getActiveCasesByCounty(countyCode);
        } else if (caseOwnerId != null) {
            cases = caseManagementService.getCasesForCaseworker(caseOwnerId);
        } else {
            CaseStatus caseStatus = status != null ? CaseStatus.valueOf(status) : null;
            cases = caseManagementService.searchCases(null, null, null, null, caseStatus);
        }

        // Apply field-level authorization
        List<Map<String, Object>> filteredCases = cases.stream()
                .map(c -> fieldAuthService.filterFieldsForRole(c, roles, "Case Resource"))
                .toList();

        return ResponseEntity.ok(filteredCases);
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getCaseById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {

        CaseEntity caseEntity = caseManagementService.searchCases(null, null, null, null, null)
                .stream().filter(c -> c.getId().equals(id)).findFirst()
                .orElseThrow(() -> new RuntimeException("Case not found"));

        Map<String, Object> filteredCase = fieldAuthService.filterFieldsForRole(caseEntity, roles, "Case Resource");
        return ResponseEntity.ok(filteredCase);
    }

    @PostMapping
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<CaseEntity> createCase(
            @RequestBody CreateCaseRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.createCase(
                request.getRecipientId(),
                request.getCaseOwnerId(),
                request.getCountyCode(),
                userId);

        return ResponseEntity.ok(caseEntity);
    }

    @PutMapping("/{id}/approve")
    @RequirePermission(resource = "Case Resource", scope = "approve")
    public ResponseEntity<CaseEntity> approveCase(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.approveCase(id, userId);
        return ResponseEntity.ok(caseEntity);
    }

    @PutMapping("/{id}/deny")
    @RequirePermission(resource = "Case Resource", scope = "deny")
    public ResponseEntity<CaseEntity> denyCase(
            @PathVariable Long id,
            @RequestBody DenialRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.denyCase(id, request.getReason(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    @PutMapping("/{id}/terminate")
    @RequirePermission(resource = "Case Resource", scope = "terminate")
    public ResponseEntity<CaseEntity> terminateCase(
            @PathVariable Long id,
            @RequestBody TerminationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.terminateCase(id, request.getReason(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    @PutMapping("/{id}/leave")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<CaseEntity> placeCaseOnLeave(
            @PathVariable Long id,
            @RequestBody LeaveRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.placeCaseOnLeave(id, request.getReason(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    @PutMapping("/{id}/withdraw")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<CaseEntity> withdrawApplication(
            @PathVariable Long id,
            @RequestBody WithdrawalRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.withdrawApplication(id, request.getReason(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    @PutMapping("/{id}/assign")
    @RequirePermission(resource = "Case Resource", scope = "assign")
    public ResponseEntity<CaseEntity> assignCase(
            @PathVariable Long id,
            @RequestBody AssignmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.assignCaseToCaseworker(id, request.getCaseOwnerId(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    // ==================== SEARCH ====================

    @GetMapping("/search")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<Map<String, Object>>> searchCases(
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) String cin,
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String caseOwnerId,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {

        CaseStatus caseStatus = status != null ? CaseStatus.valueOf(status) : null;
        List<CaseEntity> cases = caseManagementService.searchCases(caseNumber, cin, countyCode, caseOwnerId, caseStatus);

        List<Map<String, Object>> filteredCases = cases.stream()
                .map(c -> fieldAuthService.filterFieldsForRole(c, roles, "Case Resource"))
                .toList();

        return ResponseEntity.ok(filteredCases);
    }

    @GetMapping("/due-for-reassessment")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<CaseEntity>> getCasesDueForReassessment(
            @RequestParam(required = false) String date) {

        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now().plusDays(30);
        List<CaseEntity> cases = caseManagementService.getCasesDueForReassessment(targetDate);
        return ResponseEntity.ok(cases);
    }

    // ==================== CASE NOTES ====================

    @GetMapping("/{id}/notes")
    @RequirePermission(resource = "Case Notes Resource", scope = "view")
    public ResponseEntity<List<CaseNoteEntity>> getCaseNotes(@PathVariable Long id) {
        List<CaseNoteEntity> notes = caseManagementService.getCaseNotes(id);
        return ResponseEntity.ok(notes);
    }

    @PostMapping("/{id}/notes")
    @RequirePermission(resource = "Case Notes Resource", scope = "create")
    public ResponseEntity<CaseNoteEntity> addCaseNote(
            @PathVariable Long id,
            @RequestBody CaseNoteRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseNoteEntity note = caseManagementService.addCaseNote(
                id, request.getNoteType(), request.getSubject(), request.getContent(), userId);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/notes/{noteId}/append")
    @RequirePermission(resource = "Case Notes Resource", scope = "edit")
    public ResponseEntity<CaseNoteEntity> appendToNote(
            @PathVariable Long noteId,
            @RequestBody AppendNoteRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseNoteEntity note = caseManagementService.appendToNote(noteId, request.getContent(), userId);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/notes/{noteId}/cancel")
    @RequirePermission(resource = "Case Notes Resource", scope = "delete")
    public ResponseEntity<CaseNoteEntity> cancelNote(
            @PathVariable Long noteId,
            @RequestBody CancelNoteRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseNoteEntity note = caseManagementService.cancelNote(noteId, request.getReason(), userId);
        return ResponseEntity.ok(note);
    }

    // ==================== CASE CONTACTS ====================

    @GetMapping("/{id}/contacts")
    @RequirePermission(resource = "Case Contacts Resource", scope = "view")
    public ResponseEntity<List<CaseContactEntity>> getCaseContacts(@PathVariable Long id) {
        List<CaseContactEntity> contacts = caseManagementService.getActiveContacts(id);
        return ResponseEntity.ok(contacts);
    }

    @PostMapping("/{id}/contacts")
    @RequirePermission(resource = "Case Contacts Resource", scope = "create")
    public ResponseEntity<CaseContactEntity> addCaseContact(
            @PathVariable Long id,
            @RequestBody CaseContactEntity contact,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        contact.setCaseId(id);
        CaseContactEntity savedContact = caseManagementService.addCaseContact(contact, userId);
        return ResponseEntity.ok(savedContact);
    }

    @PutMapping("/contacts/{contactId}/inactivate")
    @RequirePermission(resource = "Case Contacts Resource", scope = "delete")
    public ResponseEntity<CaseContactEntity> inactivateContact(
            @PathVariable Long contactId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseContactEntity contact = caseManagementService.inactivateContact(contactId, userId);
        return ResponseEntity.ok(contact);
    }

    // ==================== INTER-COUNTY TRANSFER ====================

    @PostMapping("/{id}/transfer/initiate")
    @RequirePermission(resource = "Case Resource", scope = "transfer")
    public ResponseEntity<CaseEntity> initiateTransfer(
            @PathVariable Long id,
            @RequestBody TransferRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.initiateInterCountyTransfer(
                id, request.getReceivingCountyCode(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    @PostMapping("/{id}/transfer/complete")
    @RequirePermission(resource = "Case Resource", scope = "transfer")
    public ResponseEntity<CaseEntity> completeTransfer(
            @PathVariable Long id,
            @RequestBody CompleteTransferRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        CaseEntity caseEntity = caseManagementService.completeInterCountyTransfer(
                id, request.getNewCaseOwnerId(), userId);
        return ResponseEntity.ok(caseEntity);
    }

    // ==================== COMPANION CASES ====================

    @GetMapping("/{recipientId}/companion-cases")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<RecipientEntity>> findCompanionCases(@PathVariable Long recipientId) {
        List<RecipientEntity> companions = caseManagementService.findCompanionCases(recipientId);
        return ResponseEntity.ok(companions);
    }

    // ==================== STATISTICS ====================

    @GetMapping("/statistics/{countyCode}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<CaseManagementService.CaseStatistics> getCaseStatistics(
            @PathVariable String countyCode) {

        CaseManagementService.CaseStatistics stats = caseManagementService.getCaseStatistics(countyCode);
        return ResponseEntity.ok(stats);
    }

    // ==================== REQUEST DTOs ====================

    @lombok.Data
    public static class CreateCaseRequest {
        private Long recipientId;
        private String caseOwnerId;
        private String countyCode;
    }

    @lombok.Data
    public static class DenialRequest {
        private String reason;
    }

    @lombok.Data
    public static class TerminationRequest {
        private String reason;
    }

    @lombok.Data
    public static class LeaveRequest {
        private String reason;
    }

    @lombok.Data
    public static class WithdrawalRequest {
        private String reason;
    }

    @lombok.Data
    public static class AssignmentRequest {
        private String caseOwnerId;
    }

    @lombok.Data
    public static class CaseNoteRequest {
        private String noteType;
        private String subject;
        private String content;
    }

    @lombok.Data
    public static class AppendNoteRequest {
        private String content;
    }

    @lombok.Data
    public static class CancelNoteRequest {
        private String reason;
    }

    @lombok.Data
    public static class TransferRequest {
        private String receivingCountyCode;
    }

    @lombok.Data
    public static class CompleteTransferRequest {
        private String newCaseOwnerId;
    }
}
