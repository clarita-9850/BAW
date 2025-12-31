package com.cmips.integration.framework.util;

import com.cmips.integration.framework.support.ChecksumAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileUtil.
 */
class FileUtilTest {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "line1\nline2\nline3");
    }

    @Test
    void readFile_shouldReadAllContent() throws IOException {
        String content = FileUtil.readFile(testFile);

        assertEquals("line1\nline2\nline3", content);
    }

    @Test
    void readLines_shouldReadAllLines() throws IOException {
        List<String> lines = FileUtil.readLines(testFile);

        assertEquals(3, lines.size());
        assertEquals("line1", lines.get(0));
        assertEquals("line2", lines.get(1));
        assertEquals("line3", lines.get(2));
    }

    @Test
    void parseCsv_shouldParseCorrectly() throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, "name,age,city\nJohn,30,NYC\nJane,25,LA");

        List<Map<String, String>> records = FileUtil.parseCsv(csvFile);

        assertEquals(2, records.size());
        assertEquals("John", records.get(0).get("name"));
        assertEquals("30", records.get(0).get("age"));
        assertEquals("NYC", records.get(0).get("city"));
        assertEquals("Jane", records.get(1).get("name"));
    }

    @Test
    void parseCsv_withCustomDelimiter_shouldParse() throws IOException {
        Path csvFile = tempDir.resolve("test.tsv");
        Files.writeString(csvFile, "name\tage\tcity\nJohn\t30\tNYC");

        List<Map<String, String>> records = FileUtil.parseCsv(csvFile, "\t");

        assertEquals(1, records.size());
        assertEquals("John", records.get(0).get("name"));
    }

    @Test
    void listFiles_shouldFindMatchingFiles() throws IOException {
        Files.createFile(tempDir.resolve("file1.dat"));
        Files.createFile(tempDir.resolve("file2.dat"));
        Files.createFile(tempDir.resolve("file3.csv"));

        List<Path> datFiles = FileUtil.listFiles(tempDir, "*.dat");

        assertEquals(2, datFiles.size());
        assertTrue(datFiles.stream().allMatch(p -> p.toString().endsWith(".dat")));
    }

    @Test
    void calculateChecksum_md5_shouldReturnValidChecksum() throws IOException {
        String checksum = FileUtil.calculateChecksum(testFile, ChecksumAlgorithm.MD5);

        assertNotNull(checksum);
        assertFalse(checksum.isEmpty());
        assertEquals(32, checksum.length()); // MD5 produces 32 hex characters
    }

    @Test
    void calculateChecksum_sha256_shouldReturnValidChecksum() throws IOException {
        String checksum = FileUtil.calculateChecksum(testFile, ChecksumAlgorithm.SHA256);

        assertNotNull(checksum);
        assertEquals(64, checksum.length()); // SHA-256 produces 64 hex characters
    }

    @Test
    void copyFile_shouldCopySuccessfully() throws IOException {
        Path dest = tempDir.resolve("copy.txt");

        FileUtil.copyFile(testFile, dest);

        assertTrue(Files.exists(dest));
        assertEquals(Files.readString(testFile), Files.readString(dest));
    }

    @Test
    void moveFile_shouldMoveSuccessfully() throws IOException {
        Path source = tempDir.resolve("source.txt");
        Files.writeString(source, "content");
        Path dest = tempDir.resolve("moved.txt");

        FileUtil.moveFile(source, dest);

        assertFalse(Files.exists(source));
        assertTrue(Files.exists(dest));
        assertEquals("content", Files.readString(dest));
    }

    @Test
    void deleteIfExists_shouldDeleteSuccessfully() throws IOException {
        Path toDelete = tempDir.resolve("todelete.txt");
        Files.writeString(toDelete, "delete me");

        boolean deleted = FileUtil.deleteIfExists(toDelete);

        assertTrue(deleted);
        assertFalse(Files.exists(toDelete));
    }

    @Test
    void deleteIfExists_shouldReturnFalseForNonExistent() throws IOException {
        Path nonExistent = tempDir.resolve("nonexistent.txt");

        boolean deleted = FileUtil.deleteIfExists(nonExistent);

        assertFalse(deleted);
    }

    @Test
    void getExtension_shouldExtractExtension() {
        assertEquals("txt", FileUtil.getExtension(testFile));
    }

    @Test
    void getExtension_shouldReturnEmptyForNoExtension() {
        Path noExtension = tempDir.resolve("noextension");
        assertEquals("", FileUtil.getExtension(noExtension));
    }

    @Test
    void getBaseName_shouldExtractBaseName() {
        assertEquals("test", FileUtil.getBaseName(testFile));
    }

    @Test
    void writeFile_shouldCreateAndWrite() throws IOException {
        Path newFile = tempDir.resolve("new.txt");

        FileUtil.writeFile(newFile, "new content");

        assertTrue(Files.exists(newFile));
        assertEquals("new content", Files.readString(newFile));
    }

    @Test
    void createTempFile_shouldCreateFile() throws IOException {
        Path tempFile = FileUtil.createTempFile("test", ".tmp");

        assertTrue(Files.exists(tempFile));
        assertTrue(tempFile.getFileName().toString().startsWith("test"));
        assertTrue(tempFile.getFileName().toString().endsWith(".tmp"));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void findFile_shouldFindMatchingFile() throws IOException {
        Files.createFile(tempDir.resolve("data_20231215.csv"));

        Path found = FileUtil.findFile(tempDir, "data_*.csv");

        assertNotNull(found);
        assertTrue(found.getFileName().toString().startsWith("data_"));
    }

    @Test
    void findFile_shouldReturnNullIfNotFound() throws IOException {
        Path found = FileUtil.findFile(tempDir, "nonexistent_*.xyz");

        assertNull(found);
    }
}
