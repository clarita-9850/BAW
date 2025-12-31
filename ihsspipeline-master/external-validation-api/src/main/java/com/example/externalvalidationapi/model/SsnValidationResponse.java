package com.example.externalvalidationapi.model;

import lombok.Data;

@Data
public class SsnValidationResponse {
    private boolean valid;
    private String status;
    private String message;
    private String matchedFirstName;
    private String matchedSsn;
}

