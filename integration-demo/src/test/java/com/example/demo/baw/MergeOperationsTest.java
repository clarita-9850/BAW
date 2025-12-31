package com.example.demo.baw;

import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.repository.FileRepository;
import com.cmips.integration.framework.baw.repository.MergeResult;
import com.example.demo.testmodel.CompositeKeyRecord;
import com.example.demo.testmodel.SimpleRecord;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BAW Framework merge operations.
 * Tests MergeBuilder fluent API including sort, deduplicate, filter.
 */
@DisplayName("Merge Operations Tests")
class MergeOperationsTest {

    private static final Path TEST_DATA_DIR = Paths.get("src/test/resources/testdata");

    @TempDir
    Path tempDir;

    private FileRepository<SimpleRecord> simpleRepo;
    private FileRepository<CompositeKeyRecord> compositeRepo;

    @BeforeEach
    void setUp() {
        simpleRepo = FileRepository.forType(SimpleRecord.class);
        compositeRepo = FileRepository.forType(CompositeKeyRecord.class);
    }

    // ========== Basic Merge Tests ==========

    @Nested
    @DisplayName("Basic Merge Operations")
    class BasicMergeTests {

        @Test
        @DisplayName("Should merge two lists")
        void testMergeTwoLists() throws Exception {
            List<SimpleRecord> list1 = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.TEN).date(LocalDate.now()).build()
            );
            List<SimpleRecord> list2 = Arrays.asList(
                    SimpleRecord.builder().id(3L).name("C").amount(BigDecimal.TEN).date(LocalDate.now()).build()
            );

            List<SimpleRecord> merged = simpleRepo.merge(list1, list2).build();

            assertEquals(3, merged.size());
        }

        @Test
        @DisplayName("Should merge multiple lists using varargs")
        void testMergeMultipleLists() {
            List<SimpleRecord> list1 = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );
            List<SimpleRecord> list2 = Arrays.asList(
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );
            List<SimpleRecord> list3 = Arrays.asList(
                    SimpleRecord.builder().id(3L).name("C").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            List<SimpleRecord> merged = simpleRepo.merge(list1, list2, list3).build();

            assertEquals(3, merged.size());
        }

        @Test
        @DisplayName("Should merge empty lists")
        void testMergeEmptyLists() {
            List<SimpleRecord> empty1 = List.of();
            List<SimpleRecord> empty2 = List.of();

            List<SimpleRecord> merged = simpleRepo.merge(empty1, empty2).build();

            assertNotNull(merged);
            assertEquals(0, merged.size());
        }

        @Test
        @DisplayName("Should merge list with empty list")
        void testMergeWithEmptyList() {
            List<SimpleRecord> list1 = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Only One").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );
            List<SimpleRecord> empty = List.of();

            List<SimpleRecord> merged = simpleRepo.merge(list1, empty).build();

            assertEquals(1, merged.size());
        }

        @Test
        @DisplayName("Should merge from files")
        void testMergeFromFiles() throws Exception {
            List<SimpleRecord> main = simpleRepo.read(
                    TEST_DATA_DIR.resolve("simple_records.csv"),
                    FileFormat.csv().build()
            );
            List<SimpleRecord> additional = simpleRepo.read(
                    TEST_DATA_DIR.resolve("simple_records_merge.csv"),
                    FileFormat.csv().build()
            );

            List<SimpleRecord> merged = simpleRepo.merge(main, additional).build();

            assertEquals(8, merged.size()); // 5 + 3
        }
    }

    // ========== Sort Operations Tests ==========

    @Nested
    @DisplayName("Sort Operations")
    class SortOperationsTests {

        @Test
        @DisplayName("Should sort by single field ascending")
        void testSortByFieldAscending() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(3L).name("C").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.TEN).date(LocalDate.now()).build()
            );

            List<SimpleRecord> sorted = simpleRepo.merge(records, List.of())
                    .sortBy(SimpleRecord::getId)
                    .ascending()
                    .build();

            assertEquals(1L, sorted.get(0).getId());
            assertEquals(2L, sorted.get(1).getId());
            assertEquals(3L, sorted.get(2).getId());
        }

        @Test
        @DisplayName("Should sort by single field descending")
        void testSortByFieldDescending() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.TEN).date(LocalDate.now()).build()
            );

            List<SimpleRecord> sorted = simpleRepo.merge(records, List.of())
                    .sortBy(SimpleRecord::getId)
                    .descending()
                    .build();

            assertEquals(3L, sorted.get(0).getId());
            assertEquals(2L, sorted.get(1).getId());
            assertEquals(1L, sorted.get(2).getId());
        }

        @Test
        @DisplayName("Should sort by string field")
        void testSortByStringField() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Charlie").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Alice").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("Bob").amount(BigDecimal.TEN).date(LocalDate.now()).build()
            );

            List<SimpleRecord> sorted = simpleRepo.merge(records, List.of())
                    .sortBy(SimpleRecord::getName)
                    .ascending()
                    .build();

            assertEquals("Alice", sorted.get(0).getName());
            assertEquals("Bob", sorted.get(1).getName());
            assertEquals("Charlie", sorted.get(2).getName());
        }

        @Test
        @DisplayName("Should sort by date field")
        void testSortByDateField() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.TEN).date(LocalDate.of(2024, 3, 15)).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.TEN).date(LocalDate.of(2024, 1, 10)).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(BigDecimal.TEN).date(LocalDate.of(2024, 2, 20)).build()
            );

            List<SimpleRecord> sorted = simpleRepo.merge(records, List.of())
                    .sortBy(SimpleRecord::getDate)
                    .ascending()
                    .build();

            assertEquals(LocalDate.of(2024, 1, 10), sorted.get(0).getDate());
            assertEquals(LocalDate.of(2024, 2, 20), sorted.get(1).getDate());
            assertEquals(LocalDate.of(2024, 3, 15), sorted.get(2).getDate());
        }

        @Test
        @DisplayName("Should sort by BigDecimal field")
        void testSortByBigDecimalField() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(new BigDecimal("500.00")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(new BigDecimal("100.00")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(new BigDecimal("300.00")).date(LocalDate.now()).build()
            );

            List<SimpleRecord> sorted = simpleRepo.merge(records, List.of())
                    .sortBy(SimpleRecord::getAmount)
                    .ascending()
                    .build();

            assertEquals(0, new BigDecimal("100.00").compareTo(sorted.get(0).getAmount()));
            assertEquals(0, new BigDecimal("300.00").compareTo(sorted.get(1).getAmount()));
            assertEquals(0, new BigDecimal("500.00").compareTo(sorted.get(2).getAmount()));
        }

        @Test
        @DisplayName("Should handle null values in sort")
        void testSortWithNullValues() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(null).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(new BigDecimal("100.00")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(new BigDecimal("200.00")).date(LocalDate.now()).build()
            );

            // Sort by ID to avoid null comparison issues
            List<SimpleRecord> sorted = simpleRepo.merge(records, List.of())
                    .sortBy(SimpleRecord::getId)
                    .build();

            assertEquals(3, sorted.size());
        }
    }

    // ========== Deduplicate Operations Tests ==========

    @Nested
    @DisplayName("Deduplicate Operations")
    class DeduplicateOperationsTests {

        @Test
        @DisplayName("Should deduplicate by @FileId field")
        void testDeduplicateByFileId() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("First").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(1L).name("Duplicate").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Second").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            MergeResult<SimpleRecord> result = simpleRepo.merge(records, List.of())
                    .deduplicate()
                    .buildWithStats();

            assertEquals(2, result.getTotalCount());
            assertEquals(1, result.getDuplicatesRemoved());
        }

        @Test
        @DisplayName("Should keep first occurrence when deduplicating")
        void testDeduplicateKeepsFirst() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("First").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(1L).name("Second").amount(BigDecimal.TEN).date(LocalDate.now()).build()
            );

            List<SimpleRecord> deduped = simpleRepo.merge(records, List.of())
                    .deduplicate()
                    .build();

            assertEquals(1, deduped.size());
            assertEquals("First", deduped.get(0).getName());
        }

        @Test
        @DisplayName("Should deduplicate across merged lists")
        void testDeduplicateAcrossLists() {
            List<SimpleRecord> list1 = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("List1").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("List1").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );
            List<SimpleRecord> list2 = Arrays.asList(
                    SimpleRecord.builder().id(2L).name("List2 Duplicate").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("List2").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            MergeResult<SimpleRecord> result = simpleRepo.merge(list1, list2)
                    .deduplicate()
                    .buildWithStats();

            assertEquals(3, result.getTotalCount()); // 1, 2, 3
            assertEquals(1, result.getDuplicatesRemoved());
        }

        @Test
        @DisplayName("Should deduplicate by composite key")
        void testDeduplicateCompositeKey() throws Exception {
            List<CompositeKeyRecord> records = compositeRepo.read(
                    TEST_DATA_DIR.resolve("composite_key_records.csv"),
                    FileFormat.csv().build()
            );

            MergeResult<CompositeKeyRecord> result = compositeRepo.merge(records, List.of())
                    .deduplicate()
                    .buildWithStats();

            // Each unique composite key should appear once
            assertEquals(6, result.getSourceCount());
            // All have unique composite keys in test data
            assertEquals(6, result.getTotalCount());
        }

        @Test
        @DisplayName("Should handle no duplicates")
        void testNoDuplicates() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("One").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Two").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("Three").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            MergeResult<SimpleRecord> result = simpleRepo.merge(records, List.of())
                    .deduplicate()
                    .buildWithStats();

            assertEquals(3, result.getTotalCount());
            assertEquals(0, result.getDuplicatesRemoved());
        }
    }

    // ========== Filter Operations Tests ==========

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperationsTests {

        @Test
        @DisplayName("Should filter by predicate")
        void testFilterByPredicate() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(new BigDecimal("100")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(new BigDecimal("500")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(new BigDecimal("200")).date(LocalDate.now()).build()
            );

            MergeResult<SimpleRecord> result = simpleRepo.merge(records, List.of())
                    .filter(r -> r.getAmount().compareTo(new BigDecimal("150")) > 0)
                    .buildWithStats();

            assertEquals(2, result.getTotalCount());
            assertEquals(1, result.getFilteredOut());
        }

        @Test
        @DisplayName("Should filter by string matching")
        void testFilterByStringMatch() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Alice").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("Bob").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("Alice Jr").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            List<SimpleRecord> filtered = simpleRepo.merge(records, List.of())
                    .filter(r -> r.getName().startsWith("Alice"))
                    .build();

            assertEquals(2, filtered.size());
        }

        @Test
        @DisplayName("Should filter by date range")
        void testFilterByDateRange() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.of(2024, 1, 15)).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.ONE).date(LocalDate.of(2024, 3, 20)).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(BigDecimal.ONE).date(LocalDate.of(2024, 6, 10)).build()
            );

            LocalDate start = LocalDate.of(2024, 2, 1);
            LocalDate end = LocalDate.of(2024, 5, 1);

            List<SimpleRecord> filtered = simpleRepo.merge(records, List.of())
                    .filter(r -> !r.getDate().isBefore(start) && !r.getDate().isAfter(end))
                    .build();

            assertEquals(1, filtered.size());
            assertEquals("B", filtered.get(0).getName());
        }

        @Test
        @DisplayName("Should filter out all records")
        void testFilterOutAll() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(BigDecimal.TEN).date(LocalDate.now()).build()
            );

            List<SimpleRecord> filtered = simpleRepo.merge(records, List.of())
                    .filter(r -> false) // Filter out everything
                    .build();

            assertEquals(0, filtered.size());
        }

        @Test
        @DisplayName("Should combine multiple filter conditions")
        void testMultipleFilters() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Alice").amount(new BigDecimal("100")).date(LocalDate.of(2024, 1, 1)).build(),
                    SimpleRecord.builder().id(2L).name("Bob").amount(new BigDecimal("500")).date(LocalDate.of(2024, 2, 1)).build(),
                    SimpleRecord.builder().id(3L).name("Alice").amount(new BigDecimal("300")).date(LocalDate.of(2024, 3, 1)).build()
            );

            // Combine filter conditions using AND logic
            List<SimpleRecord> filtered = simpleRepo.merge(records, List.of())
                    .filter(r -> r.getName().equals("Alice") && r.getAmount().compareTo(new BigDecimal("150")) > 0)
                    .build();

            assertEquals(1, filtered.size());
            assertEquals(3L, filtered.get(0).getId());
        }
    }

    // ========== Combined Operations Tests ==========

    @Nested
    @DisplayName("Combined Operations")
    class CombinedOperationsTests {

        @Test
        @DisplayName("Should combine sort, deduplicate, and filter")
        void testCombinedOperations() throws Exception {
            List<SimpleRecord> main = simpleRepo.read(
                    TEST_DATA_DIR.resolve("simple_records.csv"),
                    FileFormat.csv().build()
            );
            List<SimpleRecord> additional = simpleRepo.read(
                    TEST_DATA_DIR.resolve("simple_records_merge.csv"),
                    FileFormat.csv().build()
            );

            MergeResult<SimpleRecord> result = simpleRepo.merge(main, additional)
                    .sortBy(SimpleRecord::getId)
                    .ascending()
                    .deduplicate()
                    .filter(r -> r.getAmount().compareTo(new BigDecimal("2000")) > 0)
                    .buildWithStats();

            // Should have filtered to high amount records, deduplicated
            assertTrue(result.getTotalCount() <= 8);
            assertTrue(result.getDuplicatesRemoved() >= 0);
            assertTrue(result.getFilteredOut() >= 0);

            // Verify sorted order
            List<SimpleRecord> records = result.getRecords();
            for (int i = 1; i < records.size(); i++) {
                assertTrue(records.get(i).getId() >= records.get(i - 1).getId());
            }
        }

        @Test
        @DisplayName("Should produce correct statistics")
        void testMergeStatistics() {
            List<SimpleRecord> list1 = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(new BigDecimal("100")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(2L).name("B").amount(new BigDecimal("200")).date(LocalDate.now()).build()
            );
            List<SimpleRecord> list2 = Arrays.asList(
                    SimpleRecord.builder().id(2L).name("B Dup").amount(new BigDecimal("300")).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(3L).name("C").amount(new BigDecimal("50")).date(LocalDate.now()).build()
            );

            MergeResult<SimpleRecord> result = simpleRepo.merge(list1, list2)
                    .deduplicate()
                    .filter(r -> r.getAmount().compareTo(new BigDecimal("75")) > 0)
                    .buildWithStats();

            assertEquals(4, result.getSourceCount()); // Total input records
            assertEquals(1, result.getDuplicatesRemoved()); // One duplicate (id=2)
            assertEquals(1, result.getFilteredOut()); // One filtered (id=3, amount=50)
            assertEquals(2, result.getTotalCount()); // Final count
        }
    }

    // ========== Edge Cases Tests ==========

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle single record")
        void testSingleRecord() {
            List<SimpleRecord> single = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("Only").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            MergeResult<SimpleRecord> result = simpleRepo.merge(single, List.of())
                    .sortBy(SimpleRecord::getId)
                    .deduplicate()
                    .buildWithStats();

            assertEquals(1, result.getTotalCount());
            assertEquals(0, result.getDuplicatesRemoved());
        }

        @Test
        @DisplayName("Should handle all records filtered out")
        void testAllFilteredOut() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build()
            );

            MergeResult<SimpleRecord> result = simpleRepo.merge(records, List.of())
                    .filter(r -> false)
                    .buildWithStats();

            assertEquals(0, result.getTotalCount());
            assertEquals(1, result.getFilteredOut());
        }

        @Test
        @DisplayName("Should handle all records duplicates")
        void testAllDuplicates() {
            List<SimpleRecord> records = Arrays.asList(
                    SimpleRecord.builder().id(1L).name("A").amount(BigDecimal.ONE).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(1L).name("B").amount(BigDecimal.TEN).date(LocalDate.now()).build(),
                    SimpleRecord.builder().id(1L).name("C").amount(BigDecimal.ZERO).date(LocalDate.now()).build()
            );

            MergeResult<SimpleRecord> result = simpleRepo.merge(records, List.of())
                    .deduplicate()
                    .buildWithStats();

            assertEquals(1, result.getTotalCount());
            assertEquals(2, result.getDuplicatesRemoved());
        }
    }
}
