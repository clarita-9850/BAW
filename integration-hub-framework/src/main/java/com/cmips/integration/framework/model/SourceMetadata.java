package com.cmips.integration.framework.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata about an input source.
 *
 * <p>This class provides descriptive information about an input source,
 * including its name, description, record count, and other attributes.
 *
 * <p>Example usage:
 * <pre>
 * SourceMetadata metadata = SourceMetadata.builder()
 *     .name("PaymentFileReader")
 *     .description("Reads payment files from /data/payments")
 *     .recordCount(1500)
 *     .lastModified(Instant.now())
 *     .attribute("fileName", "payments_20231215.csv")
 *     .attribute("fileSize", 245678L)
 *     .build();
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SourceMetadata {

    private final String name;
    private final String description;
    private final long recordCount;
    private final Instant lastModified;
    private final Map<String, Object> attributes;

    private SourceMetadata(String name, String description, long recordCount,
                           Instant lastModified, Map<String, Object> attributes) {
        this.name = name;
        this.description = description;
        this.recordCount = recordCount;
        this.lastModified = lastModified;
        this.attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getRecordCount() {
        return recordCount;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "SourceMetadata{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", recordCount=" + recordCount +
                ", lastModified=" + lastModified +
                '}';
    }

    public static class Builder {
        private String name = "";
        private String description = "";
        private long recordCount = -1;
        private Instant lastModified;
        private final Map<String, Object> attributes = new HashMap<>();

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder recordCount(long recordCount) {
            this.recordCount = recordCount;
            return this;
        }

        public Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public SourceMetadata build() {
            return new SourceMetadata(name, description, recordCount, lastModified, attributes);
        }
    }
}
