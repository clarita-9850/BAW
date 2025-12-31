package com.cmips.integration.framework.exception;

/**
 * Exception thrown when a send operation fails.
 *
 * <p>This exception is thrown by output destinations when they fail to
 * deliver data. Common causes include:
 * <ul>
 *   <li>Network errors during transmission</li>
 *   <li>Remote server rejection</li>
 *   <li>File write failures</li>
 *   <li>Database insert/update failures</li>
 *   <li>API error responses</li>
 *   <li>Disk space exhaustion</li>
 *   <li>Permission denied</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * public SendResult send(ProcessedFile data) throws SendException {
 *     try {
 *         sftpClient.upload(data.getPath(), remoteDir, data.getFileName());
 *         return SendResult.success("Uploaded: " + data.getFileName());
 *     } catch (SftpException e) {
 *         if (e.getCode() == SftpException.NO_SUCH_FILE) {
 *             throw new SendException("Remote directory does not exist: " + remoteDir, e);
 *         }
 *         throw new SendException("Failed to upload file: " + data.getFileName(), e);
 *     }
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class SendException extends IntegrationException {

    private static final long serialVersionUID = 1L;

    /**
     * The destination that failed.
     */
    private final String destination;

    /**
     * The data that failed to send.
     */
    private final transient Object data;

    /**
     * The number of records successfully sent before the failure.
     */
    private final long recordsSent;

    /**
     * The total number of records that were attempted.
     */
    private final long totalRecords;

    /**
     * Creates a new SendException with the specified message.
     *
     * @param message the detail message
     */
    public SendException(String message) {
        super(message);
        this.destination = null;
        this.data = null;
        this.recordsSent = 0;
        this.totalRecords = 0;
    }

    /**
     * Creates a new SendException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public SendException(String message, Throwable cause) {
        super(message, cause);
        this.destination = null;
        this.data = null;
        this.recordsSent = 0;
        this.totalRecords = 0;
    }

    /**
     * Creates a new SendException with destination information.
     *
     * @param message the detail message
     * @param destination the destination that failed
     */
    public SendException(String message, String destination) {
        super(message);
        this.destination = destination;
        this.data = null;
        this.recordsSent = 0;
        this.totalRecords = 0;
    }

    /**
     * Creates a new SendException with full context.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     * @param destination the destination that failed
     * @param data the data that failed to send
     * @param recordsSent the number of records sent before failure
     * @param totalRecords the total number of records attempted
     */
    public SendException(String message, Throwable cause, String destination,
                          Object data, long recordsSent, long totalRecords) {
        super(message, cause);
        this.destination = destination;
        this.data = data;
        this.recordsSent = recordsSent;
        this.totalRecords = totalRecords;
    }

    /**
     * Returns the destination that failed.
     *
     * @return the destination, or {@code null} if not set
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Returns the data that failed to send.
     *
     * @return the data, or {@code null} if not set
     */
    public Object getData() {
        return data;
    }

    /**
     * Returns the number of records successfully sent before the failure.
     *
     * @return the number of records sent
     */
    public long getRecordsSent() {
        return recordsSent;
    }

    /**
     * Returns the total number of records that were attempted.
     *
     * @return the total records
     */
    public long getTotalRecords() {
        return totalRecords;
    }

    /**
     * Checks if partial data was sent before the failure.
     *
     * @return {@code true} if some data was sent
     */
    public boolean isPartialSend() {
        return recordsSent > 0 && recordsSent < totalRecords;
    }

    @Override
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(super.getDetailedMessage());
        if (destination != null) {
            sb.append(" [Destination: ").append(destination).append("]");
        }
        if (totalRecords > 0) {
            sb.append(" [Progress: ").append(recordsSent).append("/").append(totalRecords).append("]");
        }
        return sb.toString();
    }
}
