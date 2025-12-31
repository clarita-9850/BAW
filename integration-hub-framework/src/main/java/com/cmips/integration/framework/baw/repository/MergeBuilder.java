package com.cmips.integration.framework.baw.repository;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Fluent builder for merge operations.
 *
 * <p>Example usage:
 * <pre>
 * List&lt;PaymentRecord&gt; merged = repo.merge(list1, list2)
 *     .sortBy(PaymentRecord::getZipCode)
 *     .thenBy(PaymentRecord::getProgramCode)
 *     .ascending()
 *     .deduplicate()
 *     .filter(p -&gt; p.getAmount().compareTo(BigDecimal.ZERO) &gt; 0)
 *     .transform(p -&gt; p.toBuilder().status("MERGED").build())
 *     .build();
 *
 * // Or get statistics
 * MergeResult&lt;PaymentRecord&gt; result = repo.merge(list1, list2)
 *     .deduplicate()
 *     .buildWithStats();
 *
 * System.out.println("Total: " + result.getTotalCount());
 * System.out.println("Duplicates removed: " + result.getDuplicatesRemoved());
 * </pre>
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MergeBuilder<T> {

    /**
     * Sorts by a field (primary sort).
     *
     * @param <U> the field type
     * @param keyExtractor the field extractor
     * @return this builder
     */
    <U extends Comparable<? super U>> MergeBuilder<T> sortBy(Function<T, U> keyExtractor);

    /**
     * Adds secondary sort by another field.
     *
     * @param <U> the field type
     * @param keyExtractor the field extractor
     * @return this builder
     */
    <U extends Comparable<? super U>> MergeBuilder<T> thenBy(Function<T, U> keyExtractor);

    /**
     * Sorts using a custom comparator.
     *
     * @param comparator the comparator
     * @return this builder
     */
    MergeBuilder<T> sortBy(Comparator<T> comparator);

    /**
     * Sets ascending sort order (default).
     *
     * @return this builder
     */
    MergeBuilder<T> ascending();

    /**
     * Sets descending sort order.
     *
     * @return this builder
     */
    MergeBuilder<T> descending();

    /**
     * Deduplicates using identity fields (@FileId).
     *
     * @return this builder
     */
    MergeBuilder<T> deduplicate();

    /**
     * Deduplicates using a custom key extractor.
     *
     * @param <K> the key type
     * @param keyExtractor the key extractor function
     * @return this builder
     */
    <K> MergeBuilder<T> deduplicate(Function<T, K> keyExtractor);

    /**
     * When deduplicating, keeps the last occurrence instead of first.
     *
     * @return this builder
     */
    MergeBuilder<T> deduplicateKeepLast();

    /**
     * Filters records during merge.
     *
     * @param predicate the filter predicate
     * @return this builder
     */
    MergeBuilder<T> filter(Predicate<T> predicate);

    /**
     * Transforms records during merge.
     *
     * @param transformer the transformation function
     * @return this builder
     */
    MergeBuilder<T> transform(UnaryOperator<T> transformer);

    /**
     * Limits the number of records in the result.
     *
     * @param limit the maximum number of records
     * @return this builder
     */
    MergeBuilder<T> limit(int limit);

    /**
     * Builds and returns the merged record list.
     *
     * @return the merged records
     */
    List<T> build();

    /**
     * Builds and returns merge result with statistics.
     *
     * @return the merge result with stats
     */
    MergeResult<T> buildWithStats();
}
