package com.cmips.integration.framework.exception;

/**
 * Exception thrown when a read operation fails.
 *
 * <p>This exception is thrown by input sources when they fail to read data.
 * Common causes include:
 * <ul>
 *   <li>I/O errors during file reading</li>
 *   <li>Parse errors in data files</li>
 *   <li>Database query failures</li>
 *   <li>API response errors</li>
 *   <li>Data format mismatches</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * public List&lt;Payment&gt; read() throws ReadException {
 *     try {
 *         return Files.lines(filePath)
 *             .skip(1) // Skip header
 *             .map(this::parseLine)
 *             .collect(Collectors.toList());
 *     } catch (IOException e) {
 *         throw new ReadException("Failed to read payment file: " + filePath, e);
 *     } catch (ParseException e) {
 *         throw new ReadException("Failed to parse payment data at line " + e.getLine(), e);
 *     }
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class ReadException extends IntegrationException {

    private static final long serialVersionUID = 1L;

    /**
     * The source that failed to read.
     */
    private final String source;

    /**
     * The line number where the error occurred, if applicable.
     */
    private final long lineNumber;

    /**
     * The number of records successfully read before the failure.
     */
    private final long recordsRead;

    /**
     * Creates a new ReadException with the specified message.
     *
     * @param message the detail message
     */
    public ReadException(String message) {
        super(message);
        this.source = null;
        this.lineNumber = -1;
        this.recordsRead = 0;
    }

    /**
     * Creates a new ReadException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ReadException(String message, Throwable cause) {
        super(message, cause);
        this.source = null;
        this.lineNumber = -1;
        this.recordsRead = 0;
    }

    /**
     * Creates a new ReadException with source information.
     *
     * @param message the detail message
     * @param source the source that failed
     */
    public ReadException(String message, String source) {
        super(message);
        this.source = source;
        this.lineNumber = -1;
        this.recordsRead = 0;
    }

    /**
     * Creates a new ReadException with full context.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     * @param source the source that failed
     * @param lineNumber the line number where the error occurred
     * @param recordsRead the number of records read before failure
     */
    public ReadException(String message, Throwable cause, String source,
                          long lineNumber, long recordsRead) {
        super(message, cause);
        this.source = source;
        this.lineNumber = lineNumber;
        this.recordsRead = recordsRead;
    }

    /**
     * Returns the source that failed to read.
     *
     * @return the source, or {@code null} if not set
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the line number where the error occurred.
     *
     * @return the line number, or -1 if not applicable
     */
    public long getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the number of records successfully read before the failure.
     *
     * @return the number of records read
     */
    public long getRecordsRead() {
        return recordsRead;
    }

    @Override
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(super.getDetailedMessage());
        if (source != null) {
            sb.append(" [Source: ").append(source).append("]");
        }
        if (lineNumber >= 0) {
            sb.append(" [Line: ").append(lineNumber).append("]");
        }
        if (recordsRead > 0) {
            sb.append(" [Records read: ").append(recordsRead).append("]");
        }
        return sb.toString();
    }
}
