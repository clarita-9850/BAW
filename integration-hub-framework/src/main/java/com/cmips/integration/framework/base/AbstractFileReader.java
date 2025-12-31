package com.cmips.integration.framework.base;

import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.ReadException;
import com.cmips.integration.framework.interfaces.IInputSource;
import com.cmips.integration.framework.model.SourceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Abstract base implementation of IInputSource for reading files.
 *
 * <p>This class provides common file reading infrastructure. Subclasses only
 * need to implement the parsing logic for their specific file format.
 *
 * <p>Example implementation for CSV files:
 * <pre>
 * &#64;InputSource(name = "paymentCsvReader", description = "Reads payment CSV files")
 * public class PaymentCsvReader extends AbstractFileReader&lt;Payment&gt; {
 *
 *     public PaymentCsvReader(Path filePath) {
 *         super(filePath);
 *     }
 *
 *     &#64;Override
 *     protected List&lt;Payment&gt; parseContent(String content) throws ReadException {
 *         return Arrays.stream(content.split("\n"))
 *             .skip(1) // Skip header
 *             .map(this::parsePaymentLine)
 *             .collect(Collectors.toList());
 *     }
 *
 *     private Payment parsePaymentLine(String line) {
 *         String[] parts = line.split(",");
 *         return new Payment(parts[0], new BigDecimal(parts[1]), LocalDate.parse(parts[2]));
 *     }
 * }
 * </pre>
 *
 * @param <T> the type of data this reader produces
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AbstractFileReader<T> implements IInputSource<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Path filePath;
    private final Charset charset;
    private boolean connected;
    private String content;
    private List<T> data;
    private long fileSize;
    private Instant lastModified;

    /**
     * Creates a new file reader for the specified path.
     *
     * @param filePath the path to the file to read
     */
    protected AbstractFileReader(Path filePath) {
        this(filePath, StandardCharsets.UTF_8);
    }

    /**
     * Creates a new file reader with specified charset.
     *
     * @param filePath the path to the file to read
     * @param charset the character encoding
     */
    protected AbstractFileReader(Path filePath, Charset charset) {
        this.filePath = filePath;
        this.charset = charset;
        this.connected = false;
    }

    @Override
    public void connect() throws ConnectionException {
        log.debug("Connecting to file: {}", filePath);

        if (!Files.exists(filePath)) {
            throw new ConnectionException("File not found: " + filePath, filePath.toString());
        }

        if (!Files.isReadable(filePath)) {
            throw new ConnectionException("File is not readable: " + filePath, filePath.toString());
        }

        try {
            fileSize = Files.size(filePath);
            lastModified = Files.getLastModifiedTime(filePath).toInstant();
            connected = true;
            log.info("Connected to file: {} ({} bytes)", filePath, fileSize);
        } catch (IOException e) {
            throw new ConnectionException("Failed to access file: " + filePath, e,
                    filePath.toString(), -1);
        }
    }

    @Override
    public boolean hasData() {
        return connected && fileSize > 0;
    }

    @Override
    public List<T> read() throws ReadException {
        if (!connected) {
            throw new ReadException("Not connected. Call connect() first.", filePath.toString());
        }

        if (data != null) {
            return data;
        }

        try {
            log.debug("Reading file: {}", filePath);
            content = Files.readString(filePath, charset);
            data = parseContent(content);
            log.info("Read {} records from {}", data.size(), filePath);
            return data;
        } catch (IOException e) {
            throw new ReadException("Failed to read file: " + filePath, e,
                    filePath.toString(), -1, 0);
        }
    }

    /**
     * Parses the file content into a list of objects.
     *
     * <p>Subclasses must implement this method to provide format-specific parsing.
     *
     * @param content the file content as a string
     * @return the list of parsed objects
     * @throws ReadException if parsing fails
     */
    protected abstract List<T> parseContent(String content) throws ReadException;

    @Override
    public void acknowledge() {
        log.debug("Acknowledged processing of {}", filePath);
        // Default implementation does nothing
        // Subclasses can override to move file to archive, etc.
    }

    @Override
    public void close() {
        connected = false;
        content = null;
        data = null;
        log.debug("Closed file reader for {}", filePath);
    }

    @Override
    public SourceMetadata getMetadata() {
        return SourceMetadata.builder()
                .name(getClass().getSimpleName())
                .description("File reader for: " + filePath)
                .recordCount(data != null ? data.size() : -1)
                .lastModified(lastModified)
                .attribute("filePath", filePath.toString())
                .attribute("fileSize", fileSize)
                .attribute("charset", charset.name())
                .build();
    }

    @Override
    public long estimateCount() {
        if (data != null) {
            return data.size();
        }
        // Could implement line counting for CSV files, etc.
        return -1;
    }

    /**
     * Returns the file path.
     *
     * @return the file path
     */
    protected Path getFilePath() {
        return filePath;
    }

    /**
     * Returns the character encoding.
     *
     * @return the charset
     */
    protected Charset getCharset() {
        return charset;
    }

    /**
     * Returns the raw file content.
     *
     * @return the file content, or null if not yet read
     */
    protected String getContent() {
        return content;
    }

    /**
     * Returns the file size in bytes.
     *
     * @return the file size
     */
    protected long getFileSize() {
        return fileSize;
    }

    /**
     * Returns the file's last modification time.
     *
     * @return the last modified instant
     */
    protected Instant getLastModified() {
        return lastModified;
    }

    /**
     * Moves the processed file to an archive directory.
     *
     * @param archiveDir the archive directory
     * @throws IOException if the move fails
     */
    protected void archiveFile(Path archiveDir) throws IOException {
        Files.createDirectories(archiveDir);
        Path archivePath = archiveDir.resolve(filePath.getFileName());
        Files.move(filePath, archivePath);
        log.info("Archived file to {}", archivePath);
    }
}
