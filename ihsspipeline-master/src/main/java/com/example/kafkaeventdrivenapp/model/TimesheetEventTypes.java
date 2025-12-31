package com.example.kafkaeventdrivenapp.model;

public class TimesheetEventTypes {
    // Core timesheet events
    public static final String TIMESHEET_SUBMITTED = "TIMESHEET_SUBMITTED";
    public static final String TIMESHEET_APPROVED = "TIMESHEET_APPROVED";
    public static final String TIMESHEET_REJECTED = "TIMESHEET_REJECTED";
    public static final String TIMESHEET_REVISION_REQUESTED = "TIMESHEET_REVISION_REQUESTED";
    public static final String TIMESHEET_REVISED = "TIMESHEET_REVISED";
    public static final String TIMESHEET_REMINDER = "TIMESHEET_REMINDER";
    
    // Enhanced 5-stage pipeline events
    public static final String ROLE_VALIDATION_COMPLETED = "ROLE_VALIDATION_COMPLETED";
    public static final String RULES_ENGINE_TRIGGERED = "RULES_ENGINE_TRIGGERED";
    public static final String QUERY_BUILT = "QUERY_BUILT";
    public static final String DATA_FETCHED = "DATA_FETCHED";
    public static final String FIELD_MASKING_APPLIED = "FIELD_MASKING_APPLIED";
    public static final String DATA_EXTRACTION_COMPLETED = "DATA_EXTRACTION_COMPLETED";
    
    // Business Intelligence events
    public static final String BACKGROUND_JOB_QUEUED = "BACKGROUND_JOB_QUEUED";
    public static final String BACKGROUND_JOB_COMPLETED = "BACKGROUND_JOB_COMPLETED";
    public static final String REPORT_GENERATED = "REPORT_GENERATED";
}
