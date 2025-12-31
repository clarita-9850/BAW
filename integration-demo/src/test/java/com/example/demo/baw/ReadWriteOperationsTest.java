package com.example.demo.baw;

import com.cmips.integration.framework.baw.exception.FileParseException;
import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.repository.FileRepository;
import com.example.demo.testmodel.AllTypesRecord;
import com.example.demo.testmodel.FixedWidthRecord;
import com.example.demo.testmodel.SimpleRecord;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BAW Framework read/write operations.
 * Note: Many tests are disabled because the framework's CSV parser
 * doesn't yet support LocalDate type conversion.
 */
@DisplayName("Read/Write Operations Tests")
class ReadWriteOperationsTest {

    private static final Path TEST_DATA_DIR = Paths.get("src/test/resources/testdata");

    @TempDir
    Path tempDir;

    private FileRepository<SimpleRecord> simpleRepo;

    @BeforeEach
    void setUp() {
        simpleRepo = FileRepository.forType(SimpleRecord.class);
    }

    // ========== JSON Read Tests (these work because Jackson handles dates) ==========

    @Nested
    @DisplayName("JSON Read Operations")
    class JsonReadTests {

        @Test
        @DisplayName("Should read JSON array file")
        void testReadJsonArray() throws Exception {
            List<SimpleRecord> records = simpleRepo.read(
                    TEST_DATA_DIR.resolve("simple_records.json"),
                    FileFormat.json().build()
            );

            assertNotNull(records);
            assertEquals(5, records.size());

            SimpleRecord first = records.get(0);
            assertEquals(1L, first.getId());
            assertEquals("Alice Johnson", first.getName());
        }

        @Test
        @DisplayName("Should read JSON with pretty printing")
        void testReadPrettyJson() throws Exception {
            String prettyJson = """
                    [
                      {
                        "id": 1,
                        "name": "Pretty Print Test",
                        "amount": 500.00,
                        "date": "2024-06-01"
                      }
                    ]
                    """;
            Path jsonFile = tempDir.resolve("pretty.json");
            Files.writeString(jsonFile, prettyJson);

            List<SimpleRecord> records = simpleRepo.read(jsonFile, FileFormat.json().build());

            assertEquals(1, records.size());
            assertEquals("Pretty Print Test", records.get(0).getName());
        }

        @Test
        @DisplayName("Should handle empty JSON array")
        void testReadEmptyJsonArray() throws Exception {
            Path emptyJson = tempDir.resolve("empty.json");
            Files.writeString(emptyJson, "[]");

            List<SimpleRecord> records = simpleRepo.read(emptyJson, FileFormat.json().build());

            assertNotNull(records);
            assertEquals(0, records.size());
        }
    }

    // ========== JSON Write Tests ==========

    @Nested
    @DisplayName("JSON Write Operations")
    class JsonWriteTests {

        @Test
        @DisplayName("Should write JSON array")
        void testWriteJsonArray() throws Exception {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("JSON Test").amount(new BigDecimal("300.00")).date(LocalDate.of(2024, 3, 15)).build()
            );

            Path outputFile = tempDir.resolve("output.json");
            simpleRepo.write(records, outputFile, FileFormat.json().build());

            assertTrue(Files.exists(outputFile));
            String content = Files.readString(outputFile);
            assertTrue(content.contains("\"id\""));
            assertTrue(content.contains("\"name\""));
            assertTrue(content.contains("JSON Test"));
        }

        @Test
        @DisplayName("Should write pretty-printed JSON")
        void testWritePrettyJson() throws Exception {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Pretty").amount(BigDecimal.TEN).date(LocalDate.now()).build()
            );

            Path outputFile = tempDir.resolve("pretty_output.json");
            simpleRepo.write(records, outputFile, FileFormat.json().prettyPrint(true).build());

            String content = Files.readString(outputFile);
            assertTrue(content.contains("\n")); // Pretty print adds newlines
        }

        @Test
        @DisplayName("Should write empty JSON array")
        void testWriteEmptyJsonArray() throws Exception {
            List<SimpleRecord> records = List.of();

            Path outputFile = tempDir.resolve("empty.json");
            simpleRepo.write(records, outputFile, FileFormat.json().build());

            String content = Files.readString(outputFile);
            assertTrue(content.contains("[]") || content.trim().equals("[ ]"));
        }
    }

    // ========== Round-Trip Tests ==========

    @Nested
    @DisplayName("Round-Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("JSON round-trip should preserve data")
        void testJsonRoundTrip() throws Exception {
            List<SimpleRecord> original = Arrays.asList(
                    SimpleRecord.builder().id(2L).name("JSON Round").amount(new BigDecimal("555.55")).date(LocalDate.of(2024, 6, 15)).build()
            );

            Path file = tempDir.resolve("roundtrip.json");
            simpleRepo.write(original, file, FileFormat.json().build());
            List<SimpleRecord> loaded = simpleRepo.read(file, FileFormat.json().build());

            assertEquals(1, loaded.size());
            assertEquals(original.get(0).getId(), loaded.get(0).getId());
            assertEquals(original.get(0).getName(), loaded.get(0).getName());
        }
    }

    // ========== Stream Operations Tests ==========

    @Nested
    @DisplayName("Stream Operations")
    class StreamOperationsTests {

        @Test
        @DisplayName("Should write to OutputStream")
        void testWriteToOutputStream() throws Exception {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Output Stream").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            simpleRepo.write(records, outputStream, FileFormat.json().build());

            String content = outputStream.toString();
            assertTrue(content.contains("Output Stream"));
        }

        @Test
        @DisplayName("Should write to bytes")
        void testWriteToBytes() throws Exception {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Bytes Test").amount(BigDecimal.ZERO).date(LocalDate.now()).build()
            );

            byte[] bytes = simpleRepo.writeToBytes(records, FileFormat.json().build());

            assertNotNull(bytes);
            assertTrue(bytes.length > 0);
            String content = new String(bytes);
            assertTrue(content.contains("Bytes Test"));
        }
    }

    // ========== Error Handling Tests ==========

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw FileParseException for non-existent file")
        void testNonExistentFile() {
            Path nonExistent = Paths.get("/non/existent/path.csv");

            assertThrows(FileParseException.class, () -> {
                simpleRepo.read(nonExistent, FileFormat.csv().build());
            });
        }
    }

    // ========== CSV Tests (Disabled due to LocalDate parsing limitation) ==========

    @Nested
    @DisplayName("CSV Read Operations")
    class CsvReadTests {

        @Test
        @DisplayName("Should read CSV file with header")
        void testReadCsvWithHeader() throws Exception {
            List<SimpleRecord> records = simpleRepo.read(
                    TEST_DATA_DIR.resolve("simple_records.csv"),
                    FileFormat.csv().build()
            );

            assertNotNull(records);
            assertEquals(5, records.size());
        }

        @Test
        @DisplayName("Should handle empty CSV file")
        void testReadEmptyCsv() throws Exception {
            List<SimpleRecord> records = simpleRepo.read(
                    TEST_DATA_DIR.resolve("empty_file.csv"),
                    FileFormat.csv().build()
            );

            assertNotNull(records);
            assertEquals(0, records.size());
        }
    }
}
