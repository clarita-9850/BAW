package com.cmips.entity;

public enum TimesheetStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    REVISION_REQUESTED("Revision Requested"),
    PROCESSED("Processed");
    
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

