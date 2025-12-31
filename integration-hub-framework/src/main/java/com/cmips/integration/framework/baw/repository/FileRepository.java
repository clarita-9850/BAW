package com.cmips.integration.framework.baw.repository;

import com.cmips.integration.framework.baw.annotation.FileType;
import com.cmips.integration.framework.baw.exception.FileParseException;
import com.cmips.integration.framework.baw.exception.FileWriteException;
import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.split.SplitResult;
import com.cmips.integration.framework.baw.split.SplitRule;
import com.cmips.integration.framework.baw.validation.ValidationResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Repository interface for file type operations.
 *
 * <p>This is the main interface for working with file types. It provides
 * operations for reading, writing, merging, splitting, converting, and
 * validating records.
 *
 * <p>Example usage:
 * <pre>
 * // Create repository
 * FileRepository&lt;PaymentRecord&gt; repo = FileRepository.forType(PaymentRecord.class);
 *
 * // Read records
 * List&lt;PaymentRecord&gt; records = repo.read(path, FileFormat.csv());
 *
 * // Merge with sorting and deduplication
 * List&lt;PaymentRecord&gt; merged = repo.merge(records1, records2)
 *     .sortBy(PaymentRecord::getDate)
 *     .deduplicate()
 *     .build();
 *
 * // Split by field
 * SplitResult&lt;PaymentRecord&gt; split = repo.split(records, SplitRule.byField(PaymentRecord::getType));
 *
 * // Write to file
 * repo.write(merged, outputPath, FileFormat.xml());
 *
 * // Send to destination
 * repo.send(merged)
 *     .as(FileFormat.json())
 *     .to(BpmSftpDestination.class)
 *     .withFilename("payments_" + date + ".json")
 *     .execute();
 * </pre>
 *
 * @param <T> the file type (record class)
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FileRepository<T> {

    // ========== Factory Methods ==========

    /**
     * Creates a repository for the specified file type class.
     *
     * @param <T> the file type
     * @param type the file type class (must have @FileType annotation)
     * @return the repository
     */
    static <T> FileRepository<T> forType(Class<T> type) {
        return new DefaultFileRepository<>(type);
    }

    // ========== Read Operations ==========

    /**
     * Reads all records from a file.
     *
     * @param path the file path
     * @param format the file format
     * @return list of records
     * @throws FileParseException if reading fails
     */
    List<T> read(Path path, FileFormat format) throws FileParseException;

    /**
     * Reads all records from an input stream.
     *
     * @param stream the input stream
     * @param format the file format
     * @return list of records
     * @throws FileParseException if reading fails
     */
    List<T> read(InputStream stream, FileFormat format) throws FileParseException;

    /**
     * Reads and combines records from multiple files.
     *
     * @param paths the file paths
     * @param format the file format
     * @return combined list of records
     * @throws FileParseException if reading fails
     */
    List<T> readAll(List<Path> paths, FileFormat format) throws FileParseException;

    /**
     * Returns a lazy stream for reading large files.
     *
     * @param path the file path
     * @param format the file format
     * @return stream of records
     * @throws FileParseException if reading fails
     */
    Stream<T> readStream(Path path, FileFormat format) throws FileParseException;

    // ========== Write Operations ==========

    /**
     * Writes records to a file.
     *
     * @param records the records to write
     * @param path the file path
     * @param format the file format
     * @throws FileWriteException if writing fails
     */
    void write(List<T> records, Path path, FileFormat format) throws FileWriteException;

    /**
     * Writes records to an output stream.
     *
     * @param records the records to write
     * @param stream the output stream
     * @param format the file format
     * @throws FileWriteException if writing fails
     */
    void write(List<T> records, OutputStream stream, FileFormat format) throws FileWriteException;

    /**
     * Writes records to a byte array.
     *
     * @param records the records to write
     * @param format the file format
     * @return the content as bytes
     * @throws FileWriteException if writing fails
     */
    byte[] writeToBytes(List<T> records, FileFormat format) throws FileWriteException;

    // ========== Merge Operations ==========

    /**
     * Creates a merge builder for combining record lists.
     *
     * @param first the first list
     * @param second the second list
     * @return the merge builder
     */
    MergeBuilder<T> merge(List<T> first, List<T> second);

    /**
     * Creates a merge builder for multiple record lists.
     *
     * @param lists the lists to merge
     * @return the merge builder
     */
    @SuppressWarnings("unchecked")
    MergeBuilder<T> merge(List<T>... lists);

    // ========== Split Operations ==========

    /**
     * Splits records using a split rule.
     *
     * @param records the records to split
     * @param rule the split rule
     * @return the split result
     */
    SplitResult<T> split(List<T> records, SplitRule<T> rule);

    // ========== Convert Operations ==========

    /**
     * Converts records to a different file type.
     *
     * @param <R> the target type
     * @param records the source records
     * @param targetType the target file type class
     * @return the converted records
     */
    <R> List<R> convert(List<T> records, Class<R> targetType);

    // ========== Query Operations ==========

    /**
     * Finds all records matching a predicate.
     *
     * @param records the records to search
     * @param predicate the filter predicate
     * @return matching records
     */
    List<T> findAll(List<T> records, Predicate<T> predicate);

    /**
     * Finds the first record matching a predicate.
     *
     * @param records the records to search
     * @param predicate the filter predicate
     * @return the first matching record, or empty
     */
    Optional<T> findFirst(List<T> records, Predicate<T> predicate);

    /**
     * Counts records matching a predicate.
     *
     * @param records the records to count
     * @param predicate the filter predicate
     * @return the count
     */
    long count(List<T> records, Predicate<T> predicate);

    // ========== Validation Operations ==========

    /**
     * Validates records against schema constraints.
     *
     * @param records the records to validate
     * @return the validation result
     */
    ValidationResult<T> validate(List<T> records);

    // ========== Send Operations ==========

    /**
     * Creates a send builder for transmitting records to a destination.
     *
     * @param records the records to send
     * @return the send builder
     */
    SendBuilder<T> send(List<T> records);

    // ========== Metadata Operations ==========

    /**
     * Returns the file type annotation for this repository.
     *
     * @return the file type annotation
     */
    FileType getFileType();

    /**
     * Returns the schema information for this file type.
     *
     * @return the schema
     */
    Schema getSchema();
}
