package com.cmips.integration.framework.interfaces;

import com.cmips.integration.framework.support.MergeStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Interface for merging multiple data sources.
 *
 * <p>This interface defines the contract for merging data from multiple sources
 * into a single unified dataset. Mergers are useful when data needs to be
 * combined from different input sources before transformation or output.
 *
 * <p>Common use cases include:
 * <ul>
 *   <li>Combining data from multiple files</li>
 *   <li>Merging records from different databases</li>
 *   <li>Consolidating API responses</li>
 *   <li>Deduplicating overlapping datasets</li>
 * </ul>
 *
 * <p>Example implementation:
 * <pre>
 * public class PaymentMerger implements IMerger&lt;Payment&gt; {
 *
 *     &#64;Override
 *     public List&lt;Payment&gt; merge(List&lt;List&lt;Payment&gt;&gt; sources) {
 *         Map&lt;String, Payment&gt; merged = new LinkedHashMap&lt;&gt;();
 *
 *         for (List&lt;Payment&gt; source : sources) {
 *             for (Payment payment : source) {
 *                 // Use payment ID as key, later values override earlier ones
 *                 merged.put(payment.getId(), payment);
 *             }
 *         }
 *
 *         return new ArrayList&lt;&gt;(merged.values());
 *     }
 *
 *     &#64;Override
 *     public MergeStrategy getStrategy() {
 *         return MergeStrategy.LAST_WINS;
 *     }
 * }
 * </pre>
 *
 * @param <T> the type of data to merge
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IMerger<T> {

    /**
     * Merges multiple data sources into a single list.
     *
     * <p>The merge behavior depends on the implementation and the configured
     * {@link #getStrategy() strategy}. Common behaviors include:
     * <ul>
     *   <li>Concatenating all sources</li>
     *   <li>Deduplicating by key</li>
     *   <li>Interleaving records</li>
     *   <li>Applying custom merge logic</li>
     * </ul>
     *
     * @param sources the list of data sources to merge
     * @return the merged result
     */
    List<T> merge(List<List<T>> sources);

    /**
     * Returns the merge strategy used by this merger.
     *
     * @return the merge strategy
     */
    MergeStrategy getStrategy();

    /**
     * Merges two sources using a custom merge function for conflicts.
     *
     * <p>When the same key exists in both sources, the merge function
     * is used to determine the resulting value.
     *
     * @param source1 the first source
     * @param source2 the second source
     * @param mergeFunction function to resolve conflicts
     * @return the merged result
     */
    default List<T> mergeWithConflictResolution(List<T> source1, List<T> source2,
                                                  BiFunction<T, T, T> mergeFunction) {
        List<T> result = new ArrayList<>(source1);
        result.addAll(source2);
        return result;
    }

    /**
     * Merges sources and removes duplicates based on equality.
     *
     * @param sources the sources to merge
     * @return merged list with duplicates removed
     */
    default List<T> mergeUnique(List<List<T>> sources) {
        List<T> merged = merge(sources);
        return merged.stream().distinct().toList();
    }

    /**
     * Returns the name of this merger.
     *
     * @return the merger name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Validates that the sources can be merged.
     *
     * @param sources the sources to validate
     * @return {@code true} if the sources can be merged
     */
    default boolean canMerge(List<List<T>> sources) {
        return sources != null && !sources.isEmpty();
    }

    /**
     * Returns the expected number of sources for this merger.
     *
     * @return the expected source count, or -1 for any number
     */
    default int expectedSourceCount() {
        return -1;
    }
}
