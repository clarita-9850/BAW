package com.cmips.model;

import java.util.List;

public class PersonSearchResponse {
    private boolean success;
    private String message;
    private List<PersonDTO> results;
    private int count;

    public PersonSearchResponse() {}

    public PersonSearchResponse(boolean success, String message, List<PersonDTO> results, int count) {
        this.success = success;
        this.message = message;
        this.results = results;
        this.count = count;
    }

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

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<PersonDTO> getResults() { return results; }
    public void setResults(List<PersonDTO> results) { this.results = results; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}

