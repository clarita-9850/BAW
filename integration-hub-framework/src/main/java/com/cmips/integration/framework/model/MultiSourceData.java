package com.cmips.integration.framework.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Container for data from multiple input sources.
 *
 * <p>This class provides a unified view of data collected from multiple
 * input sources. It maintains the separation of sources while allowing
 * aggregate operations across all data.
 *
 * <p>Example usage:
 * <pre>
 * // Create from multiple sources
 * List&lt;Payment&gt; filePayments = fileReader.read();
 * List&lt;Payment&gt; apiPayments = apiReader.read();
 * List&lt;Payment&gt; dbPayments = dbReader.read();
 *
 * MultiSourceData&lt;Payment&gt; allPayments = new MultiSourceData&lt;&gt;(
 *     List.of(filePayments, apiPayments, dbPayments)
 * );
 *
 * // Access all data
 * List&lt;Payment&gt; combined = allPayments.getAllData();
 *
 * // Access specific source
 * List&lt;Payment&gt; fromFile = allPayments.getSource(0);
 *
 * // Get total count
 * long total = allPayments.getTotalCount();
 *
 * // Stream all data
 * allPayments.stream().forEach(payment -&gt; process(payment));
 * </pre>
 *
 * @param <T> the type of data in the sources
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class MultiSourceData<T> implements Iterable<T> {

    /**
     * The list of data from each source.
     */
    private final List<List<T>> sources;

    /**
     * Source names for identification.
     */
    private final List<String> sourceNames;

    /**
     * Cached count of total items.
     */
    private final long totalCount;

    /**
     * Creates a new MultiSourceData with the given sources.
     *
     * @param sources the list of data sources
     */
    public MultiSourceData(List<List<T>> sources) {
        this(sources, null);
    }

    /**
     * Creates a new MultiSourceData with named sources.
     *
     * @param sources the list of data sources
     * @param sourceNames the names of each source
     */
    public MultiSourceData(List<List<T>> sources, List<String> sourceNames) {
        if (sources == null) {
            this.sources = Collections.emptyList();
            this.sourceNames = Collections.emptyList();
            this.totalCount = 0;
        } else {
            this.sources = sources.stream()
                    .map(s -> s == null ? Collections.<T>emptyList() : Collections.unmodifiableList(new ArrayList<>(s)))
                    .toList();
            this.sourceNames = sourceNames != null
                    ? Collections.unmodifiableList(new ArrayList<>(sourceNames))
                    : Collections.emptyList();
            this.totalCount = this.sources.stream().mapToLong(List::size).sum();
        }
    }

    /**
     * Creates an empty MultiSourceData.
     *
     * @param <T> the data type
     * @return an empty MultiSourceData
     */
    public static <T> MultiSourceData<T> empty() {
        return new MultiSourceData<>(Collections.emptyList());
    }

    /**
     * Creates a MultiSourceData from a single source.
     *
     * @param <T> the data type
     * @param source the single data source
     * @return a MultiSourceData containing the single source
     */
    public static <T> MultiSourceData<T> of(List<T> source) {
        return new MultiSourceData<>(List.of(source));
    }

    /**
     * Creates a MultiSourceData from two sources.
     *
     * @param <T> the data type
     * @param source1 the first data source
     * @param source2 the second data source
     * @return a MultiSourceData containing both sources
     */
    public static <T> MultiSourceData<T> of(List<T> source1, List<T> source2) {
        return new MultiSourceData<>(List.of(source1, source2));
    }

    /**
     * Returns all data combined into a single list.
     *
     * @return a new list containing all data from all sources
     */
    public List<T> getAllData() {
        List<T> combined = new ArrayList<>((int) totalCount);
        for (List<T> source : sources) {
            combined.addAll(source);
        }
        return combined;
    }

    /**
     * Returns data from a specific source by index.
     *
     * @param index the source index (0-based)
     * @return the data from that source
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public List<T> getSource(int index) {
        return sources.get(index);
    }

    /**
     * Returns data from a specific source by name.
     *
     * @param name the source name
     * @return an Optional containing the data if the source exists
     */
    public Optional<List<T>> getSourceByName(String name) {
        int index = sourceNames.indexOf(name);
        if (index >= 0 && index < sources.size()) {
            return Optional.of(sources.get(index));
        }
        return Optional.empty();
    }

    /**
     * Returns all sources as a list of lists.
     *
     * @return the list of sources
     */
    public List<List<T>> getSources() {
        return sources;
    }

    /**
     * Returns the source names.
     *
     * @return the list of source names
     */
    public List<String> getSourceNames() {
        return sourceNames;
    }

    /**
     * Returns the number of sources.
     *
     * @return the source count
     */
    public int getSourceCount() {
        return sources.size();
    }

    /**
     * Returns the total count of items across all sources.
     *
     * @return the total item count
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * Returns the count of items in a specific source.
     *
     * @param index the source index
     * @return the item count for that source
     */
    public int getSourceCount(int index) {
        return sources.get(index).size();
    }

    /**
     * Checks if there is any data in any source.
     *
     * @return {@code true} if there is at least one item
     */
    public boolean hasData() {
        return totalCount > 0;
    }

    /**
     * Checks if all sources are empty.
     *
     * @return {@code true} if all sources are empty
     */
    public boolean isEmpty() {
        return totalCount == 0;
    }

    /**
     * Returns a stream of all data.
     *
     * @return a stream of all items
     */
    public Stream<T> stream() {
        return sources.stream().flatMap(List::stream);
    }

    /**
     * Returns an iterator over all data.
     *
     * @return an iterator
     */
    @Override
    public Iterator<T> iterator() {
        return getAllData().iterator();
    }

    @Override
    public String toString() {
        return "MultiSourceData{" +
                "sourceCount=" + sources.size() +
                ", totalCount=" + totalCount +
                ", sourceNames=" + sourceNames +
                '}';
    }
}
