package com.cmips.integration.framework.baw.format;

import com.cmips.integration.framework.baw.exception.FileWriteException;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for writing records to files.
 *
 * <p>Implementations handle specific file formats (CSV, Fixed-Width, XML, JSON).
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FormatWriter<T> {

    /**
     * Writes records to a file path.
     *
     * @param records the records to write
     * @param path the file path
     * @param format the format configuration
     * @throws FileWriteException if writing fails
     */
    void write(List<T> records, Path path, FileFormat format) throws FileWriteException;

    /**
     * Writes records to an output stream.
     *
     * @param records the records to write
     * @param stream the output stream
     * @param format the format configuration
     * @throws FileWriteException if writing fails
     */
    void write(List<T> records, OutputStream stream, FileFormat format) throws FileWriteException;

    /**
     * Writes records to a byte array (in-memory).
     *
     * @param records the records to write
     * @param format the format configuration
     * @return the formatted content as bytes
     * @throws FileWriteException if writing fails
     */
    byte[] writeToBytes(List<T> records, FileFormat format) throws FileWriteException;

    /**
     * Returns the record type this writer handles.
     *
     * @return the record class
     */
    Class<T> getRecordType();
}
