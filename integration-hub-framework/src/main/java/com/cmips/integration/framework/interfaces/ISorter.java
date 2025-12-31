package com.cmips.integration.framework.interfaces;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Interface for sorting data collections.
 *
 * <p>This interface defines the contract for sorting operations in the integration
 * framework. Sorters can be used to order data before output or for merging
 * sorted streams.
 *
 * <p>Example implementation:
 * <pre>
 * public class PaymentAmountSorter implements ISorter&lt;Payment&gt; {
 *
 *     &#64;Override
 *     public List&lt;Payment&gt; sort(List&lt;Payment&gt; data) {
 *         return data.stream()
 *             .sorted(Comparator.comparing(Payment::getAmount).reversed())
 *             .collect(Collectors.toList());
 *     }
 *
 *     &#64;Override
 *     public Comparator&lt;Payment&gt; getComparator() {
 *         return Comparator.comparing(Payment::getAmount).reversed();
 *     }
 * }
 * </pre>
 *
 * @param <T> the type of data to sort
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ISorter<T> {

    /**
     * Sorts the given data according to the implementation's ordering.
     *
     * <p>This method should return a new sorted list without modifying
     * the input list.
     *
     * @param data the data to sort
     * @return a new list containing the sorted data
     */
    List<T> sort(List<T> data);

    /**
     * Returns the comparator used for sorting.
     *
     * <p>This comparator defines the ordering criteria for the sorter.
     *
     * @return the comparator
     */
    Comparator<T> getComparator();

    /**
     * Sorts the data in reverse order.
     *
     * @param data the data to sort
     * @return a new list containing the data in reverse order
     */
    default List<T> sortReversed(List<T> data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> sorted = new ArrayList<>(data);
        sorted.sort(getComparator().reversed());
        return sorted;
    }

    /**
     * Checks if the data is already sorted according to this sorter's ordering.
     *
     * @param data the data to check
     * @return {@code true} if the data is sorted
     */
    default boolean isSorted(List<T> data) {
        if (data == null || data.size() <= 1) {
            return true;
        }
        Comparator<T> comparator = getComparator();
        for (int i = 0; i < data.size() - 1; i++) {
            if (comparator.compare(data.get(i), data.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the name of this sorter.
     *
     * @return the sorter name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns a description of the sort order.
     *
     * @return description of the ordering
     */
    default String getSortDescription() {
        return "Custom sort order";
    }

    /**
     * Creates a sorter that applies this sorter's ordering followed by
     * the given sorter's ordering as a tiebreaker.
     *
     * @param thenBy the secondary sorter
     * @return a combined sorter
     */
    default ISorter<T> thenSortBy(ISorter<T> thenBy) {
        Comparator<T> combined = this.getComparator().thenComparing(thenBy.getComparator());
        return new ISorter<>() {
            @Override
            public List<T> sort(List<T> data) {
                if (data == null || data.isEmpty()) {
                    return new ArrayList<>();
                }
                List<T> sorted = new ArrayList<>(data);
                sorted.sort(combined);
                return sorted;
            }

            @Override
            public Comparator<T> getComparator() {
                return combined;
            }

            @Override
            public String getSortDescription() {
                return ISorter.this.getSortDescription() + " then " + thenBy.getSortDescription();
            }
        };
    }

    /**
     * Returns the top N elements according to this sorter's ordering.
     *
     * @param data the data to process
     * @param n the number of elements to return
     * @return the top N elements
     */
    default List<T> topN(List<T> data, int n) {
        List<T> sorted = sort(data);
        return sorted.subList(0, Math.min(n, sorted.size()));
    }

    /**
     * Returns the bottom N elements according to this sorter's ordering.
     *
     * @param data the data to process
     * @param n the number of elements to return
     * @return the bottom N elements
     */
    default List<T> bottomN(List<T> data, int n) {
        List<T> sorted = sortReversed(data);
        return sorted.subList(0, Math.min(n, sorted.size()));
    }
}
