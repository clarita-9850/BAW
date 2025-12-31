package com.cmips.integration.framework.model;

import java.time.Instant;
import java.util.Optional;

/**
 * Represents the result of a file upload operation.
 *
 * <p>This class provides detailed information about an upload operation,
 * including success status, remote path, file size, and verification details.
 *
 * <p>Example usage:
 * <pre>
 * UploadResult result = UploadResult.builder()
 *     .success(true)
 *     .localPath("/tmp/payment_batch.xml")
 *     .remotePath("/uploads/payments/payment_batch.xml")
 *     .fileSize(245678L)
 *     .checksum("abc123def456")
 *     .verified(true)
 *     .build();
 *
 * if (result.isSuccess() &amp;&amp; result.isVerified()) {
 *     log.info("File uploaded and verified: {}", result.getRemotePath());
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class UploadResult {

    private final boolean success;
    private final String localPath;
    private final String remotePath;
    private final long fileSize;
    private final String checksum;
    private final boolean verified;
    private final String errorMessage;
    private final Instant timestamp;
    private final long durationMs;

    private UploadResult(boolean success, String localPath, String remotePath,
                         long fileSize, String checksum, boolean verified,
                         String errorMessage, long durationMs) {
        this.success = success;
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.verified = verified;
        this.errorMessage = errorMessage;
        this.timestamp = Instant.now();
        this.durationMs = durationMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static UploadResult success(String localPath, String remotePath) {
        return builder()
                .success(true)
                .localPath(localPath)
                .remotePath(remotePath)
                .build();
    }

    public static UploadResult failure(String localPath, String errorMessage) {
        return builder()
                .success(false)
                .localPath(localPath)
                .errorMessage(errorMessage)
                .build();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Optional<String> getChecksum() {
        return Optional.ofNullable(checksum);
    }

    public boolean isVerified() {
        return verified;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public long getDurationMs() {
        return durationMs;
    }

    @Override
    public String toString() {
        if (success) {
            return "UploadResult{" +
                    "success=true, " +
                    "remotePath='" + remotePath + '\'' +
                    ", fileSize=" + fileSize +
                    ", verified=" + verified +
                    ", durationMs=" + durationMs +
                    '}';
        }
        return "UploadResult{" +
                "success=false, " +
                "localPath='" + localPath + '\'' +
                ", error='" + errorMessage + '\'' +
                '}';
    }

    public static class Builder {
        private boolean success = false;
        private String localPath;
        private String remotePath;
        private long fileSize = 0;
        private String checksum;
        private boolean verified = false;
        private String errorMessage;
        private long durationMs = 0;

        private Builder() {
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder localPath(String localPath) {
            this.localPath = localPath;
            return this;
        }

        public Builder remotePath(String remotePath) {
            this.remotePath = remotePath;
            return this;
        }

        public Builder fileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder verified(boolean verified) {
            this.verified = verified;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder durationMs(long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public UploadResult build() {
            return new UploadResult(success, localPath, remotePath, fileSize,
                    checksum, verified, errorMessage, durationMs);
        }
    }
}
