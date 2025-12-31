package com.cmips.integration.framework.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MergeUtil.
 */
class MergeUtilTest {

    @Test
    void merge_shouldConcatenateLists() {
        List<String> list1 = Arrays.asList("a", "b", "c");
        List<String> list2 = Arrays.asList("d", "e", "f");

        List<String> result = MergeUtil.merge(Arrays.asList(list1, list2));

        assertEquals(6, result.size());
        assertEquals(Arrays.asList("a", "b", "c", "d", "e", "f"), result);
    }

    @Test
    void merge_shouldHandleEmptyLists() {
        List<String> list1 = Arrays.asList("a", "b");
        List<String> list2 = Collections.emptyList();

        List<String> result = MergeUtil.merge(Arrays.asList(list1, list2));

        assertEquals(2, result.size());
        assertEquals(Arrays.asList("a", "b"), result);
    }

    @Test
    void merge_shouldHandleNullInput() {
        List<String> result = MergeUtil.merge(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void mergeUnique_shouldRemoveDuplicates() {
        record Person(String id, String name) {}

        List<Person> list1 = Arrays.asList(
                new Person("1", "John"),
                new Person("2", "Jane")
        );
        List<Person> list2 = Arrays.asList(
                new Person("2", "Jane Updated"),
                new Person("3", "Bob")
        );

        List<Person> result = MergeUtil.mergeUnique(Arrays.asList(list1, list2), Person::id);

        assertEquals(3, result.size());
        // Last wins, so Jane Updated should be present
        assertEquals("Jane Updated", result.stream().filter(p -> p.id().equals("2")).findFirst().get().name());
    }

    @Test
    void mergeUniqueFirst_shouldKeepFirstOccurrence() {
        record Person(String id, String name) {}

        List<Person> list1 = Arrays.asList(
                new Person("1", "John"),
                new Person("2", "Jane")
        );
        List<Person> list2 = Arrays.asList(
                new Person("2", "Jane Updated"),
                new Person("3", "Bob")
        );

        List<Person> result = MergeUtil.mergeUniqueFirst(Arrays.asList(list1, list2), Person::id);

        assertEquals(3, result.size());
        // First wins, so original Jane should be present
        assertEquals("Jane", result.stream().filter(p -> p.id().equals("2")).findFirst().get().name());
    }

    @Test
    void interleave_shouldInterleaveLists() {
        List<String> list1 = Arrays.asList("a", "b", "c");
        List<String> list2 = Arrays.asList("1", "2", "3");

        List<String> result = MergeUtil.interleave(Arrays.asList(list1, list2));

        assertEquals(6, result.size());
        assertEquals(Arrays.asList("a", "1", "b", "2", "c", "3"), result);
    }

    @Test
    void interleave_shouldHandleUnequalLengths() {
        List<String> list1 = Arrays.asList("a", "b", "c", "d");
        List<String> list2 = Arrays.asList("1", "2");

        List<String> result = MergeUtil.interleave(Arrays.asList(list1, list2));

        assertEquals(6, result.size());
        assertEquals(Arrays.asList("a", "1", "b", "2", "c", "d"), result);
    }

    @Test
    void mergeAndSort_shouldMergeAndSortByComparator() {
        List<Integer> list1 = Arrays.asList(3, 1, 4);
        List<Integer> list2 = Arrays.asList(2, 5, 0);

        List<Integer> result = MergeUtil.mergeAndSort(Arrays.asList(list1, list2), Comparator.naturalOrder());

        assertEquals(6, result.size());
        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5), result);
    }

    @Test
    void mergeSorted_shouldMergeTwoSortedLists() {
        List<Integer> list1 = Arrays.asList(1, 3, 5);
        List<Integer> list2 = Arrays.asList(2, 4, 6);

        List<Integer> result = MergeUtil.mergeSorted(list1, list2, Comparator.naturalOrder());

        assertEquals(6, result.size());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), result);
    }

    @Test
    void partition_shouldSplitIntoChunks() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<List<Integer>> result = MergeUtil.partition(list, 3);

        assertEquals(4, result.size());
        assertEquals(Arrays.asList(1, 2, 3), result.get(0));
        assertEquals(Arrays.asList(4, 5, 6), result.get(1));
        assertEquals(Arrays.asList(7, 8, 9), result.get(2));
        assertEquals(Arrays.asList(10), result.get(3));
    }

    @Test
    void partition_shouldHandleExactDivision() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);

        List<List<Integer>> result = MergeUtil.partition(list, 2);

        assertEquals(3, result.size());
        assertEquals(2, result.get(0).size());
        assertEquals(2, result.get(1).size());
        assertEquals(2, result.get(2).size());
    }

    @Test
    void partition_shouldThrowForInvalidChunkSize() {
        List<Integer> list = Arrays.asList(1, 2, 3);

        assertThrows(IllegalArgumentException.class, () -> MergeUtil.partition(list, 0));
        assertThrows(IllegalArgumentException.class, () -> MergeUtil.partition(list, -1));
    }

    @Test
    void partition_shouldHandleEmptyList() {
        List<Integer> list = Collections.emptyList();

        List<List<Integer>> result = MergeUtil.partition(list, 3);

        assertTrue(result.isEmpty());
    }

    @Test
    void merge_shouldHandleMultipleLists() {
        List<String> list1 = Arrays.asList("a", "b");
        List<String> list2 = Arrays.asList("c", "d");
        List<String> list3 = Arrays.asList("e", "f");

        List<String> result = MergeUtil.merge(Arrays.asList(list1, list2, list3));

        assertEquals(6, result.size());
        assertEquals(Arrays.asList("a", "b", "c", "d", "e", "f"), result);
    }

    @Test
    void interleave_shouldHandleEmptyInput() {
        List<String> result = MergeUtil.interleave(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void interleave_shouldHandleNullInput() {
        List<String> result = MergeUtil.interleave(null);

        assertTrue(result.isEmpty());
    }
}
