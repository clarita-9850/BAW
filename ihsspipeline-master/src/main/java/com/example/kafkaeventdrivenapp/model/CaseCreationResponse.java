package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseCreationResponse {
    private boolean success;
    private String message;
    private Long caseId;
    private String cmipsCaseNumber;
    private String legacyCaseNumber;
    private Long personId;
    private String caseStatus;
    private String assignedCaseworkerId;
    private LocalDate caseOpenedDate;
    private LocalDateTime createdAt;
    
    public CaseCreationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static CaseCreationResponse success(Long caseId, String cmipsCaseNumber, 
                                                String legacyCaseNumber, Long personId,
                                                String caseStatus, String assignedCaseworkerId,
                                                LocalDate caseOpenedDate, LocalDateTime createdAt) {
        CaseCreationResponse response = new CaseCreationResponse();
        response.setSuccess(true);
        response.setMessage("Case created successfully");
        response.setCaseId(caseId);
        response.setCmipsCaseNumber(cmipsCaseNumber);
        response.setLegacyCaseNumber(legacyCaseNumber);
        response.setPersonId(personId);
        response.setCaseStatus(caseStatus);
        response.setAssignedCaseworkerId(assignedCaseworkerId);
        response.setCaseOpenedDate(caseOpenedDate);
        response.setCreatedAt(createdAt);
        return response;
    }
    
    public static CaseCreationResponse failure(String message) {
        CaseCreationResponse response = new CaseCreationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}

