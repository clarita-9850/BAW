package com.example.externalvalidationapi.model;

import lombok.Data;

@Data
public class SsnValidationRequest {
    private String firstName;
    private String ssn;
}

