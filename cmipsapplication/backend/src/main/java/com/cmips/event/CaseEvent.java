package com.cmips.event;

import java.time.Instant;

public class CaseEvent {
    private String caseId;
    private String owner;
    private String recipient;
    private String location;
    private Instant createdAt;

    public CaseEvent() {}

    public CaseEvent(String caseId, String owner, String recipient, String location, Instant createdAt) {
        this.caseId = caseId;
        this.owner = owner;
        this.recipient = recipient;
        this.location = location;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String caseId;
        private String owner;
        private String recipient;
        private String location;
        private Instant createdAt;

        public Builder caseId(String caseId) { this.caseId = caseId; return this; }
        public Builder owner(String owner) { this.owner = owner; return this; }
        public Builder recipient(String recipient) { this.recipient = recipient; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public CaseEvent build() {
            return new CaseEvent(caseId, owner, recipient, location, createdAt);
        }
    }
}
