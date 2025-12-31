package com.cmips.event;

public class TimesheetEvent {
    private Long timesheetId;
    private String providerId;
    private String errorMessage;
    private String priority;

    public TimesheetEvent() {}

    public TimesheetEvent(Long timesheetId, String providerId, String errorMessage, String priority) {
        this.timesheetId = timesheetId;
        this.providerId = providerId;
        this.errorMessage = errorMessage;
        this.priority = priority;
    }

    // Getters and Setters
    public Long getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long timesheetId;
        private String providerId;
        private String errorMessage;
        private String priority;

        public Builder timesheetId(Long timesheetId) { this.timesheetId = timesheetId; return this; }
        public Builder providerId(String providerId) { this.providerId = providerId; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder priority(String priority) { this.priority = priority; return this; }

        public TimesheetEvent build() {
            return new TimesheetEvent(timesheetId, providerId, errorMessage, priority);
        }
    }
}
