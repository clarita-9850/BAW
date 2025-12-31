package com.cmips.integration.framework.baw.repository;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Default implementation of MergeBuilder.
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
class DefaultMergeBuilder<T> implements MergeBuilder<T> {

    private final List<List<T>> sources;
    private final DefaultFileRepository<T> repository;
    private Comparator<T> comparator;
    private boolean descending = false;
    private Function<T, ?> deduplicateKey;
    private boolean keepLast = false;
    private Predicate<T> filter;
    private UnaryOperator<T> transformer;
    private Integer limit;

    DefaultMergeBuilder(List<List<T>> sources, DefaultFileRepository<T> repository) {
        this.sources = sources;
        this.repository = repository;
    }

    @Override
    public <U extends Comparable<? super U>> MergeBuilder<T> sortBy(Function<T, U> keyExtractor) {
        this.comparator = Comparator.comparing(keyExtractor);
        return this;
    }

    @Override
    public <U extends Comparable<? super U>> MergeBuilder<T> thenBy(Function<T, U> keyExtractor) {
        if (this.comparator == null) {
            return sortBy(keyExtractor);
        }
        this.comparator = this.comparator.thenComparing(keyExtractor);
        return this;
    }

    @Override
    public MergeBuilder<T> sortBy(Comparator<T> comparator) {
        this.comparator = comparator;
        return this;
    }

    @Override
    public MergeBuilder<T> ascending() {
        this.descending = false;
        return this;
    }

    @Override
    public MergeBuilder<T> descending() {
        this.descending = true;
        return this;
    }

    @Override
    public MergeBuilder<T> deduplicate() {
        this.deduplicateKey = repository.getIdExtractor();
        return this;
    }

    @Override
    public <K> MergeBuilder<T> deduplicate(Function<T, K> keyExtractor) {
        this.deduplicateKey = keyExtractor;
        return this;
    }

    @Override
    public MergeBuilder<T> deduplicateKeepLast() {
        this.keepLast = true;
        return this;
    }

    @Override
    public MergeBuilder<T> filter(Predicate<T> predicate) {
        this.filter = predicate;
        return this;
    }

    @Override
    public MergeBuilder<T> transform(UnaryOperator<T> transformer) {
        this.transformer = transformer;
        return this;
    }

    @Override
    public MergeBuilder<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public List<T> build() {
        return buildWithStats().getRecords();
    }

    @Override
    public MergeResult<T> buildWithStats() {
        // Count source records
        int sourceCount = sources.stream()
                .filter(Objects::nonNull)
                .mapToInt(List::size)
                .sum();

        // Merge all sources
        List<T> merged = new ArrayList<>();
        for (List<T> source : sources) {
            if (source != null) {
                merged.addAll(source);
            }
        }

        int filteredOut = 0;
        int duplicatesRemoved = 0;

        // Apply filter
        if (filter != null) {
            int beforeFilter = merged.size();
            merged = merged.stream()
                    .filter(filter)
                    .collect(Collectors.toList());
            filteredOut = beforeFilter - merged.size();
        }

        // Apply deduplication
        if (deduplicateKey != null) {
            int beforeDedup = merged.size();
            merged = deduplicateList(merged);
            duplicatesRemoved = beforeDedup - merged.size();
        }

        // Apply sorting
        if (comparator != null) {
            Comparator<T> finalComparator = descending ? comparator.reversed() : comparator;
            merged.sort(finalComparator);
        }

        // Apply transformation
        if (transformer != null) {
            merged = merged.stream()
                    .map(transformer)
                    .collect(Collectors.toList());
        }

        // Apply limit
        int limitApplied = 0;
        if (limit != null && merged.size() > limit) {
            limitApplied = merged.size() - limit;
            merged = merged.subList(0, limit);
        }

        return MergeResult.<T>builder()
                .records(merged)
                .sourceCount(sourceCount)
                .totalCount(merged.size())
                .duplicatesRemoved(duplicatesRemoved)
                .filteredOut(filteredOut)
                .limitApplied(limitApplied)
                .build();
    }

    private List<T> deduplicateList(List<T> list) {
        Map<Object, T> unique = new LinkedHashMap<>();

        if (keepLast) {
            // Last occurrence wins
            for (T item : list) {
                Object key = deduplicateKey.apply(item);
                unique.put(key, item);
            }
        } else {
            // First occurrence wins
            for (T item : list) {
                Object key = deduplicateKey.apply(item);
                unique.putIfAbsent(key, item);
            }
        }

        return new ArrayList<>(unique.values());
    }
}
