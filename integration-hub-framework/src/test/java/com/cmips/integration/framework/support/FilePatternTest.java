package com.cmips.integration.framework.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FilePattern.
 */
class FilePatternTest {

    @TempDir
    Path tempDir;

    // ===========================================
    // Glob Pattern Tests
    // ===========================================

    @Test
    void glob_shouldMatchSimpleExtension() {
        FilePattern pattern = FilePattern.glob("*.xml");

        assertTrue(pattern.matches(Path.of("file.xml")));
        assertTrue(pattern.matches(Path.of("another.xml")));
        assertFalse(pattern.matches(Path.of("file.txt")));
        assertFalse(pattern.matches(Path.of("file.xmlx")));
    }

    @Test
    void glob_shouldMatchWithPrefix() {
        FilePattern pattern = FilePattern.glob("payment_*.csv");

        assertTrue(pattern.matches(Path.of("payment_20231215.csv")));
        assertTrue(pattern.matches(Path.of("payment_abc.csv")));
        assertFalse(pattern.matches(Path.of("invoice_20231215.csv")));
        assertFalse(pattern.matches(Path.of("payment_20231215.txt")));
    }

    @Test
    void glob_shouldMatchWithSuffix() {
        FilePattern pattern = FilePattern.glob("*_report.pdf");

        assertTrue(pattern.matches(Path.of("monthly_report.pdf")));
        assertTrue(pattern.matches(Path.of("daily_report.pdf")));
        assertFalse(pattern.matches(Path.of("monthly_report.doc")));
        assertFalse(pattern.matches(Path.of("report.pdf")));
    }

    @Test
    void glob_shouldMatchMultipleExtensions() {
        FilePattern pattern = FilePattern.glob("*.{xml,json}");

        assertTrue(pattern.matches(Path.of("data.xml")));
        assertTrue(pattern.matches(Path.of("data.json")));
        assertFalse(pattern.matches(Path.of("data.csv")));
    }

    @Test
    void glob_shouldMatchSingleCharacterWildcard() {
        FilePattern pattern = FilePattern.glob("file?.txt");

        assertTrue(pattern.matches(Path.of("file1.txt")));
        assertTrue(pattern.matches(Path.of("fileA.txt")));
        assertFalse(pattern.matches(Path.of("file12.txt")));
        assertFalse(pattern.matches(Path.of("file.txt")));
    }

    @Test
    void glob_shouldMatchExactFileName() {
        FilePattern pattern = FilePattern.glob("config.properties");

        assertTrue(pattern.matches(Path.of("config.properties")));
        assertFalse(pattern.matches(Path.of("config.xml")));
        assertFalse(pattern.matches(Path.of("myconfig.properties")));
    }

    @Test
    void glob_shouldMatchWithStringFileName() {
        FilePattern pattern = FilePattern.glob("*.csv");

        assertTrue(pattern.matches("data.csv"));
        assertTrue(pattern.matches("export.csv"));
        assertFalse(pattern.matches("data.txt"));
    }

    // ===========================================
    // Regex Pattern Tests
    // ===========================================

    @Test
    void regex_shouldMatchDatePattern() {
        FilePattern pattern = FilePattern.regex("payment_\\d{8}\\.xml");

        assertTrue(pattern.matches(Path.of("payment_20231215.xml")));
        assertTrue(pattern.matches(Path.of("payment_12345678.xml")));
        assertFalse(pattern.matches(Path.of("payment_abc.xml")));
        assertFalse(pattern.matches(Path.of("payment_2023121.xml"))); // 7 digits
        assertFalse(pattern.matches(Path.of("payment_20231215.csv")));
    }

    @Test
    void regex_shouldMatchComplexPattern() {
        FilePattern pattern = FilePattern.regex("invoice_[A-Z]{3}_\\d{4}\\.pdf");

        assertTrue(pattern.matches(Path.of("invoice_ABC_1234.pdf")));
        assertTrue(pattern.matches(Path.of("invoice_XYZ_9999.pdf")));
        assertFalse(pattern.matches(Path.of("invoice_abc_1234.pdf"))); // lowercase
        assertFalse(pattern.matches(Path.of("invoice_ABCD_1234.pdf"))); // 4 letters
        assertFalse(pattern.matches(Path.of("invoice_ABC_123.pdf"))); // 3 digits
    }

    @Test
    void regex_shouldMatchWithStringFileName() {
        FilePattern pattern = FilePattern.regex("data_\\d+\\.json");

        assertTrue(pattern.matches("data_123.json"));
        assertTrue(pattern.matches("data_999999.json"));
        assertFalse(pattern.matches("data_abc.json"));
    }

    @Test
    void regex_shouldMatchStartAndEndAnchors() {
        FilePattern pattern = FilePattern.regex("^report\\.csv$");

        assertTrue(pattern.matches(Path.of("report.csv")));
        assertFalse(pattern.matches(Path.of("myreport.csv")));
        assertFalse(pattern.matches(Path.of("report.csv.bak")));
    }

    // ===========================================
    // Any Pattern Tests
    // ===========================================

    @Test
    void any_shouldMatchAllFiles() {
        FilePattern pattern = FilePattern.any();

        assertTrue(pattern.matches(Path.of("file.txt")));
        assertTrue(pattern.matches(Path.of("image.png")));
        assertTrue(pattern.matches(Path.of("no_extension")));
        assertTrue(pattern.matches(Path.of(".hidden")));
    }

    // ===========================================
    // Null and Edge Case Tests
    // ===========================================

    @Test
    void matches_shouldReturnFalseForNullPath() {
        FilePattern pattern = FilePattern.glob("*.txt");

        assertFalse(pattern.matches((Path) null));
    }

    @Test
    void matches_shouldReturnFalseForNullString() {
        FilePattern pattern = FilePattern.glob("*.txt");

        assertFalse(pattern.matches((String) null));
    }

    @Test
    void matches_shouldReturnFalseForEmptyString() {
        FilePattern pattern = FilePattern.glob("*.txt");

        assertFalse(pattern.matches(""));
    }

    @Test
    void matches_shouldWorkWithFullPath() throws IOException {
        FilePattern pattern = FilePattern.glob("*.xml");
        Path file = tempDir.resolve("test.xml");
        Files.createFile(file);

        // Should match based on file name, not full path
        assertTrue(pattern.matches(file));
    }

    // ===========================================
    // Getter and Metadata Tests
    // ===========================================

    @Test
    void getPattern_shouldReturnPatternString() {
        FilePattern glob = FilePattern.glob("*.csv");
        FilePattern regex = FilePattern.regex("data_\\d+\\.txt");

        assertEquals("*.csv", glob.getPattern());
        assertEquals("data_\\d+\\.txt", regex.getPattern());
    }

    @Test
    void getType_shouldReturnCorrectType() {
        FilePattern glob = FilePattern.glob("*.csv");
        FilePattern regex = FilePattern.regex("data_\\d+\\.txt");

        assertEquals(FilePattern.PatternType.GLOB, glob.getType());
        assertEquals(FilePattern.PatternType.REGEX, regex.getType());
    }

    @Test
    void toString_shouldContainPatternInfo() {
        FilePattern glob = FilePattern.glob("*.csv");
        FilePattern regex = FilePattern.regex("data_\\d+\\.txt");

        String globStr = glob.toString();
        String regexStr = regex.toString();

        assertTrue(globStr.contains("GLOB"));
        assertTrue(globStr.contains("*.csv"));
        assertTrue(regexStr.contains("REGEX"));
        assertTrue(regexStr.contains("data_\\d+\\.txt"));
    }
}
