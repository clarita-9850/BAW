package com.cmips.event;

public class TaskEvent {
    private Long taskId;
    private String title;
    private String assignedTo;
    private String status;
    private String previousStatus;

    public TaskEvent() {}

    public TaskEvent(Long taskId, String title, String assignedTo, String status, String previousStatus) {
        this.taskId = taskId;
        this.title = title;
        this.assignedTo = assignedTo;
        this.status = status;
        this.previousStatus = previousStatus;
    }

    // Getters and Setters
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long taskId;
        private String title;
        private String assignedTo;
        private String status;
        private String previousStatus;

        public Builder taskId(Long taskId) { this.taskId = taskId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder assignedTo(String assignedTo) { this.assignedTo = assignedTo; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder previousStatus(String previousStatus) { this.previousStatus = previousStatus; return this; }

        public TaskEvent build() {
            return new TaskEvent(taskId, title, assignedTo, status, previousStatus);
        }
    }
}
