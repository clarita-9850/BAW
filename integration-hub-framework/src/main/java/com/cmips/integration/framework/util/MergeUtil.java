package com.cmips.integration.framework.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for merging data from multiple sources.
 *
 * <p>This class provides static utility methods for combining data from
 * multiple lists into a single unified list.
 *
 * <p>Example usage:
 * <pre>
 * // Simple concatenation
 * List&lt;Payment&gt; merged = MergeUtil.merge(List.of(list1, list2, list3));
 *
 * // Merge with deduplication by key
 * List&lt;Payment&gt; unique = MergeUtil.mergeUnique(
 *     List.of(list1, list2),
 *     Payment::getId
 * );
 *
 * // Merge and sort
 * List&lt;Payment&gt; sorted = MergeUtil.mergeAndSort(
 *     List.of(list1, list2),
 *     Comparator.comparing(Payment::getDate)
 * );
 *
 * // Interleave (round-robin)
 * List&lt;Payment&gt; interleaved = MergeUtil.interleave(List.of(list1, list2));
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class MergeUtil {

    private MergeUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Merges multiple lists by concatenation.
     *
     * <p>All elements from the first list, followed by all elements from
     * the second list, and so on.
     *
     * @param <T> the element type
     * @param sources the lists to merge
     * @return the merged list
     */
    public static <T> List<T> merge(List<List<T>> sources) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }

        int totalSize = sources.stream()
                .filter(s -> s != null)
                .mapToInt(List::size)
                .sum();

        List<T> result = new ArrayList<>(totalSize);
        for (List<T> source : sources) {
            if (source != null) {
                result.addAll(source);
            }
        }
        return result;
    }

    /**
     * Merges multiple lists with deduplication by key.
     *
     * <p>When duplicate keys are found, the last occurrence wins.
     *
     * @param <T> the element type
     * @param <K> the key type
     * @param sources the lists to merge
     * @param keyExtractor function to extract the deduplication key
     * @return the merged list with duplicates removed
     */
    public static <T, K> List<T> mergeUnique(List<List<T>> sources, Function<T, K> keyExtractor) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }

        Map<K, T> unique = new LinkedHashMap<>();
        for (List<T> source : sources) {
            if (source != null) {
                for (T item : source) {
                    K key = keyExtractor.apply(item);
                    unique.put(key, item);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    /**
     * Merges multiple lists with deduplication, keeping first occurrence.
     *
     * @param <T> the element type
     * @param <K> the key type
     * @param sources the lists to merge
     * @param keyExtractor function to extract the deduplication key
     * @return the merged list with duplicates removed (first wins)
     */
    public static <T, K> List<T> mergeUniqueFirst(List<List<T>> sources, Function<T, K> keyExtractor) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }

        Map<K, T> unique = new LinkedHashMap<>();
        for (List<T> source : sources) {
            if (source != null) {
                for (T item : source) {
                    K key = keyExtractor.apply(item);
                    unique.putIfAbsent(key, item);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    /**
     * Merges and sorts multiple lists.
     *
     * @param <T> the element type
     * @param sources the lists to merge
     * @param comparator the comparator for sorting
     * @return the merged and sorted list
     */
    public static <T> List<T> mergeAndSort(List<List<T>> sources, Comparator<T> comparator) {
        List<T> merged = merge(sources);
        merged.sort(comparator);
        return merged;
    }

    /**
     * Interleaves elements from multiple lists in round-robin fashion.
     *
     * <p>Takes one element from each list in turn until all lists are exhausted.
     *
     * @param <T> the element type
     * @param sources the lists to interleave
     * @return the interleaved list
     */
    public static <T> List<T> interleave(List<List<T>> sources) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }

        int totalSize = sources.stream()
                .filter(s -> s != null)
                .mapToInt(List::size)
                .sum();

        List<T> result = new ArrayList<>(totalSize);
        List<Iterator<T>> iterators = sources.stream()
                .filter(s -> s != null && !s.isEmpty())
                .map(List::iterator)
                .toList();

        if (iterators.isEmpty()) {
            return result;
        }

        boolean hasMore = true;
        while (hasMore) {
            hasMore = false;
            for (Iterator<T> it : iterators) {
                if (it.hasNext()) {
                    result.add(it.next());
                    if (it.hasNext()) {
                        hasMore = true;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Merges two sorted lists into a single sorted list.
     *
     * <p>This is an efficient merge for pre-sorted lists (merge sort style).
     *
     * @param <T> the element type
     * @param list1 the first sorted list
     * @param list2 the second sorted list
     * @param comparator the comparator for ordering
     * @return the merged sorted list
     */
    public static <T> List<T> mergeSorted(List<T> list1, List<T> list2, Comparator<T> comparator) {
        if (list1 == null || list1.isEmpty()) {
            return list2 == null ? new ArrayList<>() : new ArrayList<>(list2);
        }
        if (list2 == null || list2.isEmpty()) {
            return new ArrayList<>(list1);
        }

        List<T> result = new ArrayList<>(list1.size() + list2.size());
        int i = 0, j = 0;

        while (i < list1.size() && j < list2.size()) {
            if (comparator.compare(list1.get(i), list2.get(j)) <= 0) {
                result.add(list1.get(i++));
            } else {
                result.add(list2.get(j++));
            }
        }

        while (i < list1.size()) {
            result.add(list1.get(i++));
        }
        while (j < list2.size()) {
            result.add(list2.get(j++));
        }

        return result;
    }

    /**
     * Partitions a list into chunks of the specified size.
     *
     * @param <T> the element type
     * @param list the list to partition
     * @param chunkSize the maximum chunk size
     * @return a list of chunks
     */
    public static <T> List<List<T>> partition(List<T> list, int chunkSize) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        }

        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(new ArrayList<>(list.subList(i, Math.min(i + chunkSize, list.size()))));
        }
        return chunks;
    }
}
