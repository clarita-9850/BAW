package com.example.demo.baw;

import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.repository.FileRepository;
import com.cmips.integration.framework.baw.split.SplitResult;
import com.cmips.integration.framework.baw.split.SplitRule;
import com.example.demo.testmodel.SimpleRecord;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BAW Framework split operations.
 * Tests SplitRule and SplitResult functionality.
 */
@DisplayName("Split Operations Tests")
class SplitOperationsTest {

    private static final Path TEST_DATA_DIR = Paths.get("src/test/resources/testdata");

    @TempDir
    Path tempDir;

    private FileRepository<SimpleRecord> repo;

    @BeforeEach
    void setUp() {
        repo = FileRepository.forType(SimpleRecord.class);
    }

    // ========== Split by Field Tests ==========

    @Nested
    @DisplayName("Split by Field")
    class SplitByFieldTests {

        @Test
        @DisplayName("Should split by string field")
        void testSplitByStringField() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Alice").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Bob").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("Alice").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(4L).name("Charlie").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            assertEquals(3, result.getPartitionCount());
            assertEquals(2, result.get("Alice").size());
            assertEquals(1, result.get("Bob").size());
            assertEquals(1, result.get("Charlie").size());
        }

        @Test
        @DisplayName("Should split by numeric field")
        void testSplitByNumericField() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(new BigDecimal("100")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(new BigDecimal("200")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(new BigDecimal("100")).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getAmount));

            assertEquals(2, result.getPartitionCount());
        }

        @Test
        @DisplayName("Should split by date field")
        void testSplitByDateField() {
            LocalDate date1 = LocalDate.of(2024, 1, 15);
            LocalDate date2 = LocalDate.of(2024, 2, 20);

            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(date1).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.ONE).date(date2).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(BigDecimal.ONE).date(date1).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getDate));

            assertEquals(2, result.getPartitionCount());
            assertEquals(2, result.get(date1.toString()).size());
            assertEquals(1, result.get(date2.toString()).size());
        }

        @Test
        @DisplayName("Should handle null field values in split")
        void testSplitWithNullValues() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name(null).amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            assertTrue(result.getPartitionCount() >= 1);
            assertEquals(2, result.get("A").size());
        }

        @Test
        @DisplayName("Should create single partition when all values are same")
        void testSplitSinglePartition() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Same").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Same").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("Same").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            assertEquals(1, result.getPartitionCount());
            assertEquals(3, result.get("Same").size());
        }
    }

    // ========== Split by Count Tests ==========

    @Nested
    @DisplayName("Split by Count")
    class SplitByCountTests {

        @Test
        @DisplayName("Should split into equal partitions")
        void testSplitByCountEqual() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(4L).name("D").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(5L).name("E").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(6L).name("F").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byCount(2));

            assertEquals(3, result.getPartitionCount());
            // Each partition should have 2 records
            for (String key : result.getPartitionKeys()) {
                assertEquals(2, result.get(key).size());
            }
        }

        @Test
        @DisplayName("Should handle uneven split")
        void testSplitByCountUneven() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(4L).name("D").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(5L).name("E").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byCount(2));

            // 5 records / 2 per partition = 3 partitions (2, 2, 1)
            assertEquals(3, result.getPartitionCount());

            int totalRecords = result.getPartitionKeys().stream()
                    .mapToInt(k -> result.get(k).size())
                    .sum();
            assertEquals(5, totalRecords);
        }

        @Test
        @DisplayName("Should handle count larger than records")
        void testSplitByCountLargerThanRecords() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byCount(10));

            // All records in one partition
            assertEquals(1, result.getPartitionCount());
            assertEquals(2, result.getPartitionKeys().stream()
                    .mapToInt(k -> result.get(k).size())
                    .sum());
        }

        @Test
        @DisplayName("Should handle single record per partition")
        void testSplitByCountSingle() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byCount(1));

            assertEquals(3, result.getPartitionCount());
            for (String key : result.getPartitionKeys()) {
                assertEquals(1, result.get(key).size());
            }
        }
    }

    // ========== Split by Predicate Tests ==========

    @Nested
    @DisplayName("Split by Predicate")
    class SplitByPredicateTests {

        @Test
        @DisplayName("Should split by single predicate")
        void testSplitBySinglePredicate() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(new BigDecimal("100")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(new BigDecimal("500")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(new BigDecimal("200")).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records,
                    SplitRule.byPredicate(r -> r.getAmount().compareTo(new BigDecimal("300")) > 0, "high_value", "standard"));

            assertEquals(2, result.getPartitionCount());
            assertEquals(1, result.get("high_value").size()); // Only B
            assertEquals(2, result.get("standard").size()); // A and C
        }

        @Test
        @DisplayName("Should split by multiple predicates")
        void testSplitByMultiplePredicates() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Alice").amount(new BigDecimal("100")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Bob").amount(new BigDecimal("500")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("Carol").amount(new BigDecimal("50")).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byPredicates(
                    Map.of(
                            "high", r -> r.getAmount().compareTo(new BigDecimal("400")) > 0,
                            "medium", r -> r.getAmount().compareTo(new BigDecimal("75")) > 0
                                    && r.getAmount().compareTo(new BigDecimal("400")) <= 0,
                            "low", r -> r.getAmount().compareTo(new BigDecimal("75")) <= 0
                    )
            ));

            assertEquals(3, result.getPartitionCount());
            assertEquals(1, result.get("high").size());
            assertEquals(1, result.get("medium").size());
            assertEquals(1, result.get("low").size());
        }

        @Test
        @DisplayName("Should handle predicate that matches nothing")
        void testSplitByPredicateNoMatch() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(new BigDecimal("100")).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records,
                    SplitRule.byPredicate(r -> r.getAmount().compareTo(new BigDecimal("1000")) > 0, "impossible", "other"));

            // All records go to "other" (falseLabel)
            assertTrue(result.getPartitionKeys().contains("other"));
            assertEquals(1, result.get("other").size());
        }
    }

    // ========== SplitResult API Tests ==========

    @Nested
    @DisplayName("SplitResult API")
    class SplitResultApiTests {

        @Test
        @DisplayName("Should get partition keys")
        void testGetPartitionKeys() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Alpha").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Beta").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("Gamma").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            Set<String> keys = result.getPartitionKeys();
            assertEquals(3, keys.size());
            assertTrue(keys.contains("Alpha"));
            assertTrue(keys.contains("Beta"));
            assertTrue(keys.contains("Gamma"));
        }

        @Test
        @DisplayName("Should get partition count")
        void testGetPartitionCount() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            assertEquals(2, result.getPartitionCount());
        }

        @Test
        @DisplayName("Should get partition by key")
        void testGetPartitionByKey() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Target").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Other").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            List<SimpleRecord> targetPartition = result.get("Target");
            assertNotNull(targetPartition);
            assertEquals(1, targetPartition.size());
            assertEquals(1L, targetPartition.get(0).getId());
        }

        @Test
        @DisplayName("Should return empty list for non-existent key")
        void testGetNonExistentKey() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Exists").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            assertTrue(result.get("DoesNotExist").isEmpty());
        }

        @Test
        @DisplayName("Should check if partition exists")
        void testHasPartition() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Present").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            assertTrue(result.hasPartition("Present"));
            assertFalse(result.hasPartition("Absent"));
        }

        @Test
        @DisplayName("Should get all partitions as map")
        void testGetPartitions() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("X").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Y").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            Map<String, List<SimpleRecord>> allPartitions = result.getPartitions();
            assertEquals(2, allPartitions.size());
            assertTrue(allPartitions.containsKey("X"));
            assertTrue(allPartitions.containsKey("Y"));
        }
    }

    // ========== Empty and Edge Cases Tests ==========

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty list")
        void testSplitEmptyList() {
            List<SimpleRecord> records = List.of();

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            assertEquals(0, result.getPartitionCount());
            assertTrue(result.getPartitionKeys().isEmpty());
        }

        @Test
        @DisplayName("Should handle single record")
        void testSplitSingleRecord() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Solo").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            assertEquals(1, result.getPartitionCount());
            assertEquals(1, result.get("Solo").size());
        }

        @Test
        @DisplayName("Should split from file")
        void testSplitFromFile() throws Exception {
            List<SimpleRecord> records = repo.read(
                    TEST_DATA_DIR.resolve("simple_records.csv"),
                    FileFormat.csv().build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            // Each person has unique name, so 5 partitions
            assertEquals(5, result.getPartitionCount());
        }

        @Test
        @DisplayName("Should preserve record order within partitions")
        void testPreserveOrderInPartitions() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Group").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Group").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("Group").amount(new BigDecimal("100")).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            List<SimpleRecord> group = result.get("Group");
            assertEquals(1L, group.get(0).getId());
            assertEquals(2L, group.get(1).getId());
            assertEquals(3L, group.get(2).getId());
        }
    }

    // ========== Integration with Other Operations Tests ==========

    @Nested
    @DisplayName("Integration with Other Operations")
    class IntegrationTests {

        @Test
        @DisplayName("Should split merged results")
        void testSplitAfterMerge() {
            List<SimpleRecord> list1 = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Engineering").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );
            List<SimpleRecord> list2 = Arrays.asList(
                    SimpleRecord.builder().id(2L).name("Sales").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("Engineering").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            List<SimpleRecord> merged = repo.merge(list1, list2).build();
            SplitResult<SimpleRecord> result = repo.split(merged, SplitRule.byField(SimpleRecord::getName));

            assertEquals(2, result.getPartitionCount());
            assertEquals(2, result.get("Engineering").size());
            assertEquals(1, result.get("Sales").size());
        }

        @Test
        @DisplayName("Should write split partitions to files")
        void testWriteSplitPartitions() throws Exception {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Dept A").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Dept B").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            SplitResult<SimpleRecord> result = repo.split(records, SplitRule.byField(SimpleRecord::getName));

            for (String key : result.getPartitionKeys()) {
                String safeKey = key.replaceAll("\\s+", "_").toLowerCase();
                Path outputPath = tempDir.resolve(safeKey + ".json");
                repo.write(result.get(key), outputPath, FileFormat.json().build());
                assertTrue(outputPath.toFile().exists());
            }
        }
    }
}
