package com.cmips.integration.framework.base;

import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.SendException;
import com.cmips.integration.framework.interfaces.IOutputDestination;
import com.cmips.integration.framework.model.SendResult;
import com.cmips.integration.framework.support.SftpClient;
import com.cmips.integration.framework.support.SftpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Abstract base implementation of IOutputDestination for SFTP file uploads.
 *
 * <p>This class provides common SFTP upload infrastructure including connection
 * management and file handling. Subclasses only need to implement the data
 * formatting logic.
 *
 * <p>Example implementation:
 * <pre>
 * &#64;OutputDestination(name = "paymentSftpWriter", description = "Uploads payment files")
 * public class PaymentSftpWriter extends AbstractSftpWriter&lt;List&lt;Payment&gt;&gt; {
 *
 *     public PaymentSftpWriter(SftpConfig config, String remoteDir) {
 *         super(config, remoteDir);
 *     }
 *
 *     &#64;Override
 *     protected String formatData(List&lt;Payment&gt; payments) {
 *         StringBuilder sb = new StringBuilder();
 *         sb.append("id,amount,date\n");
 *         for (Payment p : payments) {
 *             sb.append(p.getId()).append(",")
 *               .append(p.getAmount()).append(",")
 *               .append(p.getDate()).append("\n");
 *         }
 *         return sb.toString();
 *     }
 *
 *     &#64;Override
 *     protected String generateFileName(List&lt;Payment&gt; payments) {
 *         return "payments_" + LocalDate.now() + ".csv";
 *     }
 * }
 * </pre>
 *
 * @param <T> the type of data this writer accepts
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AbstractSftpWriter<T> implements IOutputDestination<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final SftpConfig config;
    private final String remoteDir;
    private SftpClient client;
    private boolean connected;
    private long uploadCount;
    private long errorCount;

    /**
     * Creates a new SFTP writer with the given configuration.
     *
     * @param config the SFTP configuration
     * @param remoteDir the remote directory for uploads
     */
    protected AbstractSftpWriter(SftpConfig config, String remoteDir) {
        this.config = config;
        this.remoteDir = remoteDir;
        this.connected = false;
        this.uploadCount = 0;
        this.errorCount = 0;
    }

    @Override
    public void connect() throws ConnectionException {
        log.debug("Connecting to SFTP server: {}", config.getHost());
        client = new SftpClient(config);
        client.connect();
        connected = true;
        log.info("Connected to SFTP server: {}", config.getHost());
    }

    @Override
    public SendResult send(T data) throws SendException {
        if (!connected) {
            throw new SendException("Not connected. Call connect() first.");
        }

        Path tempFile = null;
        try {
            // Format the data
            String content = formatData(data);
            String fileName = generateFileName(data);

            // Write to temp file
            tempFile = Files.createTempFile("sftp_upload_", "_" + fileName);
            Files.writeString(tempFile, content);

            // Upload
            client.upload(tempFile, remoteDir, fileName);
            uploadCount++;

            String remotePath = remoteDir + "/" + fileName;
            log.info("Uploaded file to {}", remotePath);

            return SendResult.builder()
                    .success(true)
                    .message("Uploaded to " + remotePath)
                    .metadata("remotePath", remotePath)
                    .metadata("fileName", fileName)
                    .metadata("size", content.length())
                    .recordsSent(1)
                    .build();

        } catch (IOException e) {
            errorCount++;
            throw new SendException("Failed to prepare data for upload", e);
        } catch (SendException e) {
            errorCount++;
            throw e;
        } finally {
            // Clean up temp file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temp file: {}", tempFile);
                }
            }
        }
    }

    /**
     * Formats the data for upload.
     *
     * <p>Subclasses must implement this method to convert the data to a string
     * format suitable for the target file.
     *
     * @param data the data to format
     * @return the formatted content
     */
    protected abstract String formatData(T data);

    /**
     * Generates the file name for the upload.
     *
     * <p>Subclasses must implement this method to generate an appropriate
     * file name for each upload.
     *
     * @param data the data being uploaded
     * @return the file name
     */
    protected abstract String generateFileName(T data);

    @Override
    public boolean verify(SendResult result) {
        if (!connected || result == null || !result.isSuccess()) {
            return false;
        }

        String remotePath = result.getMetadata("remotePath", String.class).orElse(null);
        if (remotePath == null) {
            return false;
        }

        return client.exists(remotePath);
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            connected = false;
            log.info("Disconnected from SFTP server: {}", config.getHost());
        }
    }

    @Override
    public boolean isConnected() {
        return connected && client != null && client.isConnected();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Returns the SFTP configuration.
     *
     * @return the config
     */
    protected SftpConfig getConfig() {
        return config;
    }

    /**
     * Returns the remote directory.
     *
     * @return the remote directory
     */
    protected String getRemoteDir() {
        return remoteDir;
    }

    /**
     * Returns the SFTP client.
     *
     * @return the client, or null if not connected
     */
    protected SftpClient getClient() {
        return client;
    }

    /**
     * Returns the number of successful uploads.
     *
     * @return the upload count
     */
    public long getUploadCount() {
        return uploadCount;
    }

    /**
     * Returns the number of upload errors.
     *
     * @return the error count
     */
    public long getErrorCount() {
        return errorCount;
    }
}
