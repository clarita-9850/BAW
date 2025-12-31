package com.example.kafkaeventdrivenapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonSearchResponse {
    private boolean success;
    private String message;
    private List<PersonDTO> results;
    private int count;
    
    public PersonSearchResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.results = List.of();
        this.count = 0;
    }
    
    public PersonSearchResponse(boolean success, List<PersonDTO> results) {
        this.success = success;
        this.message = "Search completed successfully";
        this.results = results;
        this.count = results != null ? results.size() : 0;
    }
}

