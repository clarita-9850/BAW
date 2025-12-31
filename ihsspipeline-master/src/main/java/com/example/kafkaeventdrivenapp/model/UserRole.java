package com.example.kafkaeventdrivenapp.model;

/**
 * Canonical CMIPS roles enforced across both codebases.
 * NO FALLBACK - Role MUST be extracted from JWT token or request will fail.
 */
import java.util.Locale;

public enum UserRole {
    ADMIN(true, true),
    SUPERVISOR(true, false),
    CASE_WORKER(false, false),
    PROVIDER(false, false),
    RECIPIENT(false, false),
    SYSTEM_SCHEDULER(true, true);

    private final boolean internalStaff;
    private final boolean elevated;

    UserRole(boolean internalStaff, boolean elevated) {
        this.internalStaff = internalStaff;
        this.elevated = elevated;
    }

    public boolean isInternalStaff() {
        return internalStaff;
    }

    public boolean isElevated() {
        return elevated;
    }

    /**
     * Parse role from string value - NO FALLBACK, throws exception if invalid.
     * Role MUST be extracted from JWT token.
     * @throws IllegalArgumentException if value is null, blank, or not a valid role
     */
    public static UserRole from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required - cannot be null or empty. Role must be extracted from JWT token.");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        
        // Handle service account prefix for scheduled jobs
        if (normalized.startsWith("SERVICE-ACCOUNT-")) {
            return SYSTEM_SCHEDULER;
        }
        
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: '" + value + "'. Must be one of: ADMIN, SUPERVISOR, CASE_WORKER, PROVIDER, RECIPIENT, SYSTEM_SCHEDULER");
        }
    }
    
    /**
     * Safe parsing that returns null instead of throwing exception.
     * Use this for validation checks, not for actual role extraction.
     */
    public static UserRole fromOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("SERVICE-ACCOUNT-")) {
            return SYSTEM_SCHEDULER;
        }
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

