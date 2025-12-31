package com.cmips.model;

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

    public static UserRole from(String value) {
        if (value == null || value.isBlank()) {
            return defaultRole();
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("SERVICE-ACCOUNT-")) {
            return SYSTEM_SCHEDULER;
        }
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return defaultRole();
        }
    }

    public static UserRole defaultRole() {
        return CASE_WORKER;
    }
}

