package com.example.kafkaeventdrivenapp.model;

public enum TimesheetStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    PENDING_REVIEW("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    REVISION_REQUIRED("Revision Required"),
    REVISED("Revised"),
    CANCELLED("Cancelled");

    private final String displayName;

    TimesheetStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
