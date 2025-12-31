package com.cmips.integration.framework.interfaces;

import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.ReadException;
import com.cmips.integration.framework.model.SourceMetadata;

import java.util.List;
import java.util.Optional;

/**
 * Interface for reading data from any source.
 *
 * <p>This interface defines the contract for input sources in the integration framework.
 * Implementations can read from various sources such as files, SFTP servers, REST APIs,
 * databases, message queues, etc.
 *
 * <p>The interface follows a typical read lifecycle:
 * <ol>
 *   <li>{@link #connect()} - Establish connection to the source</li>
 *   <li>{@link #hasData()} - Check if data is available</li>
 *   <li>{@link #read()} or {@link #readBatch(int)} - Read the data</li>
 *   <li>{@link #acknowledge()} - Acknowledge successful processing (optional)</li>
 *   <li>{@link #close()} - Close the connection and release resources</li>
 * </ol>
 *
 * <p>Example implementation for a file reader:
 * <pre>
 * &#64;InputSource(name = "paymentFileReader", description = "Reads payment files from disk")
 * public class PaymentFileReader implements IInputSource&lt;Payment&gt; {
 *
 *     private Path filePath;
 *     private List&lt;Payment&gt; payments;
 *     private boolean connected = false;
 *
 *     &#64;Override
 *     public void connect() throws ConnectionException {
 *         if (!Files.exists(filePath)) {
 *             throw new ConnectionException("File not found: " + filePath);
 *         }
 *         connected = true;
 *     }
 *
 *     &#64;Override
 *     public boolean hasData() {
 *         return connected &amp;&amp; payments != null &amp;&amp; !payments.isEmpty();
 *     }
 *
 *     &#64;Override
 *     public List&lt;Payment&gt; read() throws ReadException {
 *         try {
 *             payments = parsePaymentFile(filePath);
 *             return payments;
 *         } catch (IOException e) {
 *             throw new ReadException("Failed to read payment file", e);
 *         }
 *     }
 *
 *     &#64;Override
 *     public void close() {
 *         connected = false;
 *         payments = null;
 *     }
 * }
 * </pre>
 *
 * @param <T> the type of data this source produces
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IInputSource<T> {

    /**
     * Establishes a connection to the data source.
     *
     * <p>This method should perform any necessary initialization, such as:
     * <ul>
     *   <li>Opening file handles</li>
     *   <li>Establishing network connections</li>
     *   <li>Authenticating with remote services</li>
     *   <li>Validating source availability</li>
     * </ul>
     *
     * @throws ConnectionException if the connection cannot be established
     */
    void connect() throws ConnectionException;

    /**
     * Checks if data is available to be read from this source.
     *
     * <p>This method should be called after {@link #connect()} to verify
     * that there is data available for processing.
     *
     * @return {@code true} if data is available, {@code false} otherwise
     */
    boolean hasData();

    /**
     * Reads all available data from the source.
     *
     * <p>This method reads and returns all data from the source.
     * For large datasets, consider using {@link #readBatch(int)} instead.
     *
     * @return a list containing all data read from the source
     * @throws ReadException if an error occurs while reading
     */
    List<T> read() throws ReadException;

    /**
     * Reads a batch of data from the source.
     *
     * <p>This method is useful for processing large datasets in chunks
     * to manage memory usage and enable incremental processing.
     *
     * @param batchSize the maximum number of records to read
     * @return a list containing up to {@code batchSize} records
     * @throws ReadException if an error occurs while reading
     */
    default List<T> readBatch(int batchSize) throws ReadException {
        List<T> allData = read();
        if (allData.size() <= batchSize) {
            return allData;
        }
        return allData.subList(0, batchSize);
    }

    /**
     * Reads a single record from the source.
     *
     * @return an Optional containing the next record, or empty if no more data
     * @throws ReadException if an error occurs while reading
     */
    default Optional<T> readOne() throws ReadException {
        List<T> batch = readBatch(1);
        return batch.isEmpty() ? Optional.empty() : Optional.of(batch.get(0));
    }

    /**
     * Acknowledges successful processing of the data.
     *
     * <p>This method can be used to:
     * <ul>
     *   <li>Move processed files to an archive directory</li>
     *   <li>Commit message queue offsets</li>
     *   <li>Update database markers</li>
     *   <li>Delete temporary resources</li>
     * </ul>
     *
     * <p>The default implementation is a no-op.
     */
    default void acknowledge() {
        // Default implementation does nothing
    }

    /**
     * Rolls back any changes made during reading.
     *
     * <p>This method can be used to undo any side effects of reading
     * in case of downstream processing failures.
     *
     * <p>The default implementation is a no-op.
     */
    default void rollback() {
        // Default implementation does nothing
    }

    /**
     * Closes the connection and releases any resources.
     *
     * <p>This method should be called when the source is no longer needed.
     * It should release all resources such as file handles, network connections,
     * and memory buffers.
     */
    void close();

    /**
     * Returns metadata about this input source.
     *
     * <p>Metadata can include information such as:
     * <ul>
     *   <li>Source name and description</li>
     *   <li>Record count</li>
     *   <li>Last modified timestamp</li>
     *   <li>Source-specific attributes</li>
     * </ul>
     *
     * @return metadata about this source
     */
    default SourceMetadata getMetadata() {
        return SourceMetadata.builder()
                .name(this.getClass().getSimpleName())
                .description("No description available")
                .build();
    }

    /**
     * Returns the name of this input source.
     *
     * @return the source name
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Returns whether this source is currently connected.
     *
     * @return {@code true} if connected
     */
    boolean isConnected();

    /**
     * Checks if this source supports batch reading.
     *
     * @return {@code true} if batch reading is supported
     */
    default boolean supportsBatchReading() {
        return true;
    }

    /**
     * Returns the estimated number of records in this source.
     *
     * @return the estimated count, or -1 if unknown
     */
    default long estimateCount() {
        return -1;
    }
}
