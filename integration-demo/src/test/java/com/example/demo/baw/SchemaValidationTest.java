package com.example.demo.baw;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import com.cmips.integration.framework.baw.exception.SchemaValidationException;
import com.cmips.integration.framework.baw.repository.FileRepository;
import com.cmips.integration.framework.baw.repository.Schema;
import com.example.demo.testmodel.AllTypesRecord;
import com.example.demo.testmodel.CompositeKeyRecord;
import com.example.demo.testmodel.FixedWidthRecord;
import com.example.demo.testmodel.SimpleRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BAW Framework schema validation.
 * Tests @FileType, @FileColumn, and @FileId annotation processing.
 */
@DisplayName("Schema Validation Tests")
class SchemaValidationTest {

    // ========== @FileType Annotation Tests ==========

    @Nested
    @DisplayName("@FileType Annotation Tests")
    class FileTypeAnnotationTests {

        @Test
        @DisplayName("Valid @FileType annotation should create repository")
        void testValidFileTypeAnnotation() {
            FileRepository<SimpleRecord> repo = FileRepository.forType(SimpleRecord.class);
            assertNotNull(repo);
            assertNotNull(repo.getFileType());
            assertEquals("simple-record", repo.getFileType().name());
            assertEquals("Simple test record", repo.getFileType().description());
            assertEquals("1.0", repo.getFileType().version());
        }

        @Test
        @DisplayName("Missing @FileType annotation should throw SchemaValidationException")
        void testMissingFileTypeAnnotation() {
            assertThrows(SchemaValidationException.class, () -> {
                FileRepository.forType(NoFileTypeRecord.class);
            });
        }

        @Test
        @DisplayName("Empty name in @FileType should use class name")
        void testEmptyFileTypeName() {
            FileRepository<EmptyNameFileType> repo = FileRepository.forType(EmptyNameFileType.class);
            assertNotNull(repo);
            // Should not throw exception
        }

        @Test
        @DisplayName("@FileType with all attributes should be parsed correctly")
        void testFileTypeWithAllAttributes() {
            FileRepository<AllTypesRecord> repo = FileRepository.forType(AllTypesRecord.class);
            assertEquals("all-types-record", repo.getFileType().name());
            assertEquals("Record with all supported types", repo.getFileType().description());
            assertEquals("1.0", repo.getFileType().version());
        }
    }

    // ========== @FileColumn Annotation Tests ==========

    @Nested
    @DisplayName("@FileColumn Annotation Tests")
    class FileColumnAnnotationTests {

        @Test
        @DisplayName("Schema should contain all @FileColumn fields")
        void testSchemaContainsAllColumns() {
            FileRepository<SimpleRecord> repo = FileRepository.forType(SimpleRecord.class);
            Schema schema = repo.getSchema();

            assertNotNull(schema);
            assertEquals(4, schema.getColumns().size());

            List<String> columnNames = schema.getColumns().stream()
                    .map(Schema.ColumnSchema::getName)
                    .toList();
            assertTrue(columnNames.contains("id"));
            assertTrue(columnNames.contains("name"));
            assertTrue(columnNames.contains("amount"));
            assertTrue(columnNames.contains("date"));
        }

        @Test
        @DisplayName("Column order should be respected")
        void testColumnOrder() {
            FileRepository<SimpleRecord> repo = FileRepository.forType(SimpleRecord.class);
            Schema schema = repo.getSchema();

            List<Schema.ColumnSchema> columns = schema.getColumns();
            assertEquals(1, columns.get(0).getOrder());
            assertEquals(2, columns.get(1).getOrder());
            assertEquals(3, columns.get(2).getOrder());
            assertEquals(4, columns.get(3).getOrder());
        }

        @Test
        @DisplayName("Nullable attribute should be parsed correctly")
        void testNullableAttribute() {
            FileRepository<SimpleRecord> repo = FileRepository.forType(SimpleRecord.class);
            Schema schema = repo.getSchema();

            Schema.ColumnSchema idColumn = schema.getColumns().stream()
                    .filter(c -> "id".equals(c.getName()))
                    .findFirst()
                    .orElseThrow();
            assertFalse(idColumn.isNullable());

            Schema.ColumnSchema amountColumn = schema.getColumns().stream()
                    .filter(c -> "amount".equals(c.getName()))
                    .findFirst()
                    .orElseThrow();
            assertTrue(amountColumn.isNullable());
        }

        @Test
        @DisplayName("Format attribute should be parsed correctly")
        void testFormatAttribute() {
            FileRepository<SimpleRecord> repo = FileRepository.forType(SimpleRecord.class);
            Schema schema = repo.getSchema();

            Schema.ColumnSchema amountColumn = schema.getColumns().stream()
                    .filter(c -> "amount".equals(c.getName()))
                    .findFirst()
                    .orElseThrow();
            assertEquals("#,##0.00", amountColumn.getFormat());
        }

        @Test
        @DisplayName("Fixed-width length and padding should be parsed")
        void testFixedWidthAttributes() {
            FileRepository<FixedWidthRecord> repo = FileRepository.forType(FixedWidthRecord.class);
            Schema schema = repo.getSchema();

            Schema.ColumnSchema idColumn = schema.getColumns().stream()
                    .filter(c -> "id".equals(c.getName()))
                    .findFirst()
                    .orElseThrow();
            assertEquals(10, idColumn.getLength());

            Schema.ColumnSchema nameColumn = schema.getColumns().stream()
                    .filter(c -> "name".equals(c.getName()))
                    .findFirst()
                    .orElseThrow();
            assertEquals(30, nameColumn.getLength());
        }

        @Test
        @DisplayName("All supported data types should have columns")
        void testAllSupportedDataTypes() {
            FileRepository<AllTypesRecord> repo = FileRepository.forType(AllTypesRecord.class);
            Schema schema = repo.getSchema();

            assertEquals(11, schema.getColumns().size());

            List<String> columnNames = schema.getColumns().stream()
                    .map(Schema.ColumnSchema::getName)
                    .toList();

            assertTrue(columnNames.contains("stringField"));
            assertTrue(columnNames.contains("intField"));
            assertTrue(columnNames.contains("longField"));
            assertTrue(columnNames.contains("doubleField"));
            assertTrue(columnNames.contains("booleanField"));
            assertTrue(columnNames.contains("bigDecimalField"));
            assertTrue(columnNames.contains("localDateField"));
            assertTrue(columnNames.contains("localDateTimeField"));
            assertTrue(columnNames.contains("instantField"));
            assertTrue(columnNames.contains("statusField"));
        }
    }

    // ========== @FileId Annotation Tests ==========

    @Nested
    @DisplayName("@FileId Annotation Tests")
    class FileIdAnnotationTests {

        @Test
        @DisplayName("Single @FileId field should be identified")
        void testSingleFileId() {
            FileRepository<SimpleRecord> repo = FileRepository.forType(SimpleRecord.class);
            Schema schema = repo.getSchema();

            assertEquals(1, schema.getIdColumns().size());
            assertEquals("id", schema.getIdColumns().get(0).getName());
        }

        @Test
        @DisplayName("Composite @FileId fields should be identified with order")
        void testCompositeFileId() {
            FileRepository<CompositeKeyRecord> repo = FileRepository.forType(CompositeKeyRecord.class);
            Schema schema = repo.getSchema();

            assertEquals(3, schema.getIdColumns().size());

            List<String> idColumnNames = schema.getIdColumns().stream()
                    .map(Schema.ColumnSchema::getName)
                    .toList();
            assertEquals("regionCode", idColumnNames.get(0));
            assertEquals("accountNumber", idColumnNames.get(1));
            assertEquals("transactionDate", idColumnNames.get(2));
        }

        @Test
        @DisplayName("@FileId order attribute should be respected")
        void testFileIdOrder() {
            FileRepository<CompositeKeyRecord> repo = FileRepository.forType(CompositeKeyRecord.class);
            Schema schema = repo.getSchema();

            List<Schema.ColumnSchema> idColumns = schema.getIdColumns();
            // ID columns should be sorted by order
            assertEquals(1, idColumns.get(0).getOrder());
            assertEquals(2, idColumns.get(1).getOrder());
            assertEquals(3, idColumns.get(2).getOrder());
        }

        @Test
        @DisplayName("Record without @FileId should still work")
        void testNoFileId() {
            FileRepository<NoIdRecord> repo = FileRepository.forType(NoIdRecord.class);
            Schema schema = repo.getSchema();

            assertEquals(0, schema.getIdColumns().size());
        }
    }

    // ========== Schema Metadata Tests ==========

    @Nested
    @DisplayName("Schema Metadata Tests")
    class SchemaMetadataTests {

        @Test
        @DisplayName("Schema name should match @FileType name")
        void testSchemaName() {
            FileRepository<SimpleRecord> repo = FileRepository.forType(SimpleRecord.class);
            Schema schema = repo.getSchema();

            assertEquals("simple-record", schema.getName());
        }

        @Test
        @DisplayName("Schema version should match @FileType version")
        void testSchemaVersion() {
            FileRepository<SimpleRecord> repo = FileRepository.forType(SimpleRecord.class);
            Schema schema = repo.getSchema();

            assertEquals("1.0", schema.getVersion());
        }

        @Test
        @DisplayName("Schema should track field names correctly")
        void testFieldNames() {
            FileRepository<SimpleRecord> repo = FileRepository.forType(SimpleRecord.class);
            Schema schema = repo.getSchema();

            Schema.ColumnSchema idColumn = schema.getColumns().get(0);
            assertEquals("id", idColumn.getFieldName());
            assertEquals("id", idColumn.getName());
        }
    }

    // ========== Test Helper Classes ==========

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoFileTypeRecord {
        private Long id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FileType(name = "", description = "Empty name test")
    public static class EmptyNameFileType {
        @FileColumn(order = 1)
        private Long id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FileType(name = "no-id-record", description = "Record without @FileId")
    public static class NoIdRecord {
        @FileColumn(order = 1)
        private Long id;

        @FileColumn(order = 2)
        private String name;
    }
}
