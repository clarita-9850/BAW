package com.cmips.integration.framework.interfaces;

import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.SendException;
import com.cmips.integration.framework.model.SendResult;

import java.util.List;

/**
 * Interface for sending data to a destination.
 *
 * <p>This interface defines the contract for output destinations in the integration framework.
 * Implementations can send data to various destinations such as files, SFTP servers,
 * REST APIs, databases, message queues, etc.
 *
 * <p>The interface follows a typical write lifecycle:
 * <ol>
 *   <li>{@link #connect()} - Establish connection to the destination</li>
 *   <li>{@link #send(Object)} or {@link #sendBatch(List)} - Send the data</li>
 *   <li>{@link #verify(SendResult)} - Verify successful delivery (optional)</li>
 *   <li>{@link #close()} - Close the connection and release resources</li>
 * </ol>
 *
 * <p>Example implementation for an SFTP writer:
 * <pre>
 * &#64;OutputDestination(name = "sftpWriter", description = "Uploads files to SFTP server")
 * public class SftpPaymentWriter implements IOutputDestination&lt;PaymentFile&gt; {
 *
 *     private final SftpConfig config;
 *     private SftpClient client;
 *
 *     &#64;Override
 *     public void connect() throws ConnectionException {
 *         client = SftpUtil.createClient(config);
 *         try {
 *             client.connect();
 *         } catch (Exception e) {
 *             throw new ConnectionException("Failed to connect to SFTP", e);
 *         }
 *     }
 *
 *     &#64;Override
 *     public SendResult send(PaymentFile data) throws SendException {
 *         try {
 *             Path tempFile = createTempFile(data);
 *             client.upload(tempFile, config.getRemoteDir(), data.getFileName());
 *             return SendResult.success("File uploaded: " + data.getFileName());
 *         } catch (Exception e) {
 *             throw new SendException("Failed to upload file", e);
 *         }
 *     }
 *
 *     &#64;Override
 *     public boolean verify(SendResult result) {
 *         return result.isSuccess() &amp;&amp; client.exists(result.getMessage());
 *     }
 *
 *     &#64;Override
 *     public void close() {
 *         if (client != null) {
 *             client.disconnect();
 *         }
 *     }
 * }
 * </pre>
 *
 * @param <T> the type of data this destination accepts
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IOutputDestination<T> {

    /**
     * Establishes a connection to the destination.
     *
     * <p>This method should perform any necessary initialization, such as:
     * <ul>
     *   <li>Opening file handles or network connections</li>
     *   <li>Authenticating with remote services</li>
     *   <li>Creating necessary directories or resources</li>
     *   <li>Validating destination availability</li>
     * </ul>
     *
     * @throws ConnectionException if the connection cannot be established
     */
    void connect() throws ConnectionException;

    /**
     * Sends a single data item to the destination.
     *
     * @param data the data to send
     * @return a SendResult indicating the outcome
     * @throws SendException if sending fails
     */
    SendResult send(T data) throws SendException;

    /**
     * Sends a batch of data items to the destination.
     *
     * <p>The default implementation calls {@link #send(Object)} for each item.
     * Implementations may override this for more efficient batch processing.
     *
     * @param batch the list of data items to send
     * @return a SendResult summarizing the batch operation
     * @throws SendException if sending fails
     */
    default SendResult sendBatch(List<T> batch) throws SendException {
        if (batch == null || batch.isEmpty()) {
            return SendResult.success("No data to send");
        }

        int successCount = 0;
        int failCount = 0;
        StringBuilder messages = new StringBuilder();

        for (T item : batch) {
            try {
                SendResult result = send(item);
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failCount++;
                    messages.append(result.getMessage()).append("; ");
                }
            } catch (SendException e) {
                failCount++;
                messages.append(e.getMessage()).append("; ");
            }
        }

        if (failCount == 0) {
            return SendResult.success(String.format("Sent %d items successfully", successCount));
        } else if (successCount == 0) {
            throw new SendException(String.format("All %d items failed: %s", failCount, messages));
        } else {
            return SendResult.partial(
                    String.format("Sent %d/%d items. Failures: %s",
                            successCount, batch.size(), messages));
        }
    }

    /**
     * Verifies that the data was successfully sent.
     *
     * <p>This method can be used to perform additional verification after sending,
     * such as checking that a file exists on the remote server or that a database
     * record was created.
     *
     * @param result the result from the send operation
     * @return {@code true} if verification succeeds
     */
    default boolean verify(SendResult result) {
        return result != null && result.isSuccess();
    }

    /**
     * Commits the transaction or confirms the send operation.
     *
     * <p>This method is called after all data has been sent successfully.
     * It can be used to finalize transactions, flush buffers, or perform
     * post-send operations.
     */
    default void commit() {
        // Default implementation does nothing
    }

    /**
     * Rolls back any changes made during sending.
     *
     * <p>This method is called if an error occurs during or after sending.
     * It should undo any side effects of the send operation if possible.
     */
    default void rollback() {
        // Default implementation does nothing
    }

    /**
     * Closes the connection and releases any resources.
     *
     * <p>This method should be called when the destination is no longer needed.
     * It should release all resources such as file handles, network connections,
     * and memory buffers.
     */
    void close();

    /**
     * Checks if the destination is currently connected and ready to receive data.
     *
     * @return {@code true} if connected and ready
     */
    default boolean isConnected() {
        return true;
    }

    /**
     * Returns the name of this destination.
     *
     * @return the destination name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Indicates whether this destination supports transactional operations.
     *
     * @return {@code true} if transactions are supported
     */
    default boolean supportsTransactions() {
        return false;
    }

    /**
     * Begins a new transaction.
     *
     * <p>This method should be called before sending data if transactional
     * semantics are required.
     *
     * @throws UnsupportedOperationException if transactions are not supported
     */
    default void beginTransaction() {
        if (!supportsTransactions()) {
            throw new UnsupportedOperationException("Transactions not supported");
        }
    }
}
