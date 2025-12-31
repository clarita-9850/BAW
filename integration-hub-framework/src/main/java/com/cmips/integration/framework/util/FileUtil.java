package com.cmips.integration.framework.util;

import com.cmips.integration.framework.support.ChecksumAlgorithm;
import com.cmips.integration.framework.support.FilePattern;
import com.cmips.integration.framework.support.FileWatcher;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;

/**
 * Utility class for file operations.
 *
 * <p>This class provides static utility methods for common file operations
 * such as reading, writing, parsing, and watching files.
 *
 * <p>Example usage:
 * <pre>
 * // Read file content
 * String content = FileUtil.readFile(Paths.get("data.txt"), UTF_8);
 *
 * // Read lines
 * List&lt;String&gt; lines = FileUtil.readLines(Paths.get("data.txt"));
 *
 * // Parse XML
 * PaymentBatch batch = FileUtil.parseXml(Paths.get("payments.xml"), PaymentBatch.class);
 *
 * // Parse CSV
 * List&lt;Map&lt;String, String&gt;&gt; records = FileUtil.parseCsv(Paths.get("data.csv"));
 *
 * // Calculate checksum
 * String checksum = FileUtil.calculateChecksum(path, ChecksumAlgorithm.SHA256);
 *
 * // Watch directory for new files
 * FileWatcher watcher = FileUtil.watchDirectory(Paths.get("/input"), FilePattern.glob("*.xml"));
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FileUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    private static final XmlMapper xmlMapper = new XmlMapper();

    private FileUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Reads the entire content of a file as a string.
     *
     * @param path the file path
     * @param charset the character encoding
     * @return the file content
     * @throws IOException if reading fails
     */
    public static String readFile(Path path, Charset charset) throws IOException {
        return Files.readString(path, charset);
    }

    /**
     * Reads the entire content of a file as a string using UTF-8.
     *
     * @param path the file path
     * @return the file content
     * @throws IOException if reading fails
     */
    public static String readFile(Path path) throws IOException {
        return readFile(path, StandardCharsets.UTF_8);
    }

    /**
     * Reads all lines from a file.
     *
     * @param path the file path
     * @return list of lines
     * @throws IOException if reading fails
     */
    public static List<String> readLines(Path path) throws IOException {
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Reads all lines from a file with specified charset.
     *
     * @param path the file path
     * @param charset the character encoding
     * @return list of lines
     * @throws IOException if reading fails
     */
    public static List<String> readLines(Path path, Charset charset) throws IOException {
        return Files.readAllLines(path, charset);
    }

    /**
     * Writes content to a file.
     *
     * @param path the file path
     * @param content the content to write
     * @throws IOException if writing fails
     */
    public static void writeFile(Path path, String content) throws IOException {
        writeFile(path, content, StandardCharsets.UTF_8);
    }

    /**
     * Writes content to a file with specified charset.
     *
     * @param path the file path
     * @param content the content to write
     * @param charset the character encoding
     * @throws IOException if writing fails
     */
    public static void writeFile(Path path, String content, Charset charset) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, charset);
    }

    /**
     * Parses an XML file into an object.
     *
     * @param <T> the target type
     * @param path the XML file path
     * @param type the target class
     * @return the parsed object
     * @throws IOException if parsing fails
     */
    public static <T> T parseXml(Path path, Class<T> type) throws IOException {
        return xmlMapper.readValue(path.toFile(), type);
    }

    /**
     * Parses an XML string into an object.
     *
     * @param <T> the target type
     * @param xml the XML string
     * @param type the target class
     * @return the parsed object
     * @throws IOException if parsing fails
     */
    public static <T> T parseXmlString(String xml, Class<T> type) throws IOException {
        return xmlMapper.readValue(xml, type);
    }

    /**
     * Parses a CSV file into a list of maps.
     *
     * <p>The first line is treated as the header row containing column names.
     *
     * @param path the CSV file path
     * @return list of records, where each record is a map of column name to value
     * @throws IOException if parsing fails
     */
    public static List<Map<String, String>> parseCsv(Path path) throws IOException {
        return parseCsv(path, ",");
    }

    /**
     * Parses a CSV file with custom delimiter.
     *
     * @param path the CSV file path
     * @param delimiter the field delimiter
     * @return list of records
     * @throws IOException if parsing fails
     */
    public static List<Map<String, String>> parseCsv(Path path, String delimiter) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return records;
            }

            String[] headers = headerLine.split(delimiter);
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(delimiter, -1);
                Map<String, String> record = new HashMap<>();

                for (int i = 0; i < headers.length && i < values.length; i++) {
                    record.put(headers[i], values[i].trim());
                }

                records.add(record);
            }
        }

        return records;
    }

    /**
     * Creates a FileWatcher for the specified directory and pattern.
     *
     * @param directory the directory to watch
     * @param pattern the file pattern to match
     * @return a new FileWatcher
     * @throws IOException if the watcher cannot be created
     */
    public static FileWatcher watchDirectory(Path directory, FilePattern pattern) throws IOException {
        return new FileWatcher(directory, pattern);
    }

    /**
     * Moves a file to a new location.
     *
     * @param source the source file
     * @param target the target location
     * @throws IOException if the move fails
     */
    public static void moveFile(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Moved file from {} to {}", source, target);
    }

    /**
     * Copies a file to a new location.
     *
     * @param source the source file
     * @param target the target location
     * @throws IOException if the copy fails
     */
    public static void copyFile(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Copied file from {} to {}", source, target);
    }

    /**
     * Calculates the checksum of a file.
     *
     * @param path the file path
     * @param algorithm the checksum algorithm
     * @return the checksum as a hex string
     * @throws IOException if reading fails
     */
    public static String calculateChecksum(Path path, ChecksumAlgorithm algorithm) throws IOException {
        if (algorithm == ChecksumAlgorithm.CRC32) {
            return calculateCrc32(path);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.getAlgorithmName());
            try (InputStream is = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Unsupported checksum algorithm: " + algorithm, e);
        }
    }

    private static String calculateCrc32(Path path) throws IOException {
        CRC32 crc = new CRC32();
        try (InputStream is = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
        }
        return Long.toHexString(crc.getValue());
    }

    /**
     * Lists files in a directory matching a pattern.
     *
     * @param directory the directory
     * @param globPattern the glob pattern
     * @return list of matching file paths
     * @throws IOException if listing fails
     */
    public static List<Path> listFiles(Path directory, String globPattern) throws IOException {
        FilePattern pattern = FilePattern.glob(globPattern);
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(pattern::matches)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Finds a single file matching a pattern in a directory.
     *
     * @param directory the directory
     * @param globPattern the glob pattern
     * @return the matching file path, or null if not found
     * @throws IOException if listing fails
     */
    public static Path findFile(Path directory, String globPattern) throws IOException {
        List<Path> files = listFiles(directory, globPattern);
        return files.isEmpty() ? null : files.get(0);
    }

    /**
     * Deletes a file if it exists.
     *
     * @param path the file path
     * @return {@code true} if the file was deleted
     * @throws IOException if deletion fails
     */
    public static boolean deleteIfExists(Path path) throws IOException {
        return Files.deleteIfExists(path);
    }

    /**
     * Creates a temporary file with the given prefix and suffix.
     *
     * @param prefix the file name prefix
     * @param suffix the file name suffix
     * @return the path to the temporary file
     * @throws IOException if creation fails
     */
    public static Path createTempFile(String prefix, String suffix) throws IOException {
        return Files.createTempFile(prefix, suffix);
    }

    /**
     * Gets the file extension.
     *
     * @param path the file path
     * @return the extension without the dot, or empty string if none
     */
    public static String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
    }

    /**
     * Gets the file name without extension.
     *
     * @param path the file path
     * @return the file name without extension
     */
    public static String getBaseName(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}
