package com.cmips.integration.framework.baw.format;

import com.cmips.integration.framework.baw.exception.FileParseException;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Interface for parsing files into records.
 *
 * <p>Implementations handle specific file formats (CSV, Fixed-Width, XML, JSON).
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface FormatParser<T> {

    /**
     * Parses all records from a file path.
     *
     * @param path the file path
     * @param format the format configuration
     * @return list of parsed records
     * @throws FileParseException if parsing fails
     */
    List<T> parse(Path path, FileFormat format) throws FileParseException;

    /**
     * Parses all records from an input stream.
     *
     * @param stream the input stream
     * @param format the format configuration
     * @return list of parsed records
     * @throws FileParseException if parsing fails
     */
    List<T> parse(InputStream stream, FileFormat format) throws FileParseException;

    /**
     * Returns a lazy stream of records for large files.
     *
     * @param path the file path
     * @param format the format configuration
     * @return stream of records
     * @throws FileParseException if parsing fails
     */
    Stream<T> parseStream(Path path, FileFormat format) throws FileParseException;

    /**
     * Returns the record type this parser handles.
     *
     * @return the record class
     */
    Class<T> getRecordType();
}
