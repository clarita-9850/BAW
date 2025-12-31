package com.cmips.integration.framework.baw.split;

import com.cmips.integration.framework.baw.exception.SplitRuleConflictException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Defines rules for splitting records into partitions.
 *
 * <p>Example usage:
 * <pre>
 * // Split by field value (one partition per unique value)
 * SplitRule&lt;Payment&gt; byType = SplitRule.byField(Payment::getType);
 *
 * // Split by count (fixed records per partition)
 * SplitRule&lt;Payment&gt; byCount = SplitRule.byCount(1000);
 *
 * // Split by predicate (binary split)
 * SplitRule&lt;Payment&gt; byAmount = SplitRule.byPredicate(
 *     p -&gt; p.getAmount().compareTo(BigDecimal.valueOf(10000)) &gt; 0,
 *     "high_value",
 *     "standard"
 * );
 *
 * // Split by multiple predicates
 * Map&lt;String, Predicate&lt;Payment&gt;&gt; predicates = new LinkedHashMap&lt;&gt;();
 * predicates.put("hrm", p -&gt; "HRM".equals(p.getSource()));
 * predicates.put("fin", p -&gt; "FIN".equals(p.getSource()));
 * predicates.put("other", p -&gt; true); // catch-all
 * SplitRule&lt;Payment&gt; bySource = SplitRule.byPredicates(predicates);
 *
 * // Chain rules
 * SplitRule&lt;Payment&gt; combined = byType.andThen(byCount);
 * </pre>
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class SplitRule<T> {

    private final String description;

    protected SplitRule(String description) {
        this.description = description;
    }

    /**
     * Returns the partition key for a record.
     *
     * @param record the record
     * @return the partition key
     */
    public abstract String getPartitionKey(T record);

    /**
     * Returns the rule description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Chains this rule with another rule.
     * The resulting key is a combination of both rule keys.
     *
     * @param next the next rule to apply
     * @return the combined rule
     */
    public SplitRule<T> andThen(SplitRule<T> next) {
        validateCompatibility(next);
        SplitRule<T> first = this;
        return new SplitRule<T>("(" + first.description + ") then (" + next.description + ")") {
            @Override
            public String getPartitionKey(T record) {
                return first.getPartitionKey(record) + "/" + next.getPartitionKey(record);
            }
        };
    }

    /**
     * Validates that this rule is compatible with another rule.
     *
     * @param other the other rule
     * @throws SplitRuleConflictException if rules conflict
     */
    public void validateCompatibility(SplitRule<T> other) throws SplitRuleConflictException {
        // By default, rules are compatible
        // Subclasses can override for specific conflict detection
    }

    // ========== Factory Methods ==========

    /**
     * Creates a rule that partitions by field value.
     * Each unique field value becomes a partition.
     *
     * @param <T> the record type
     * @param extractor the field extractor
     * @return the split rule
     */
    public static <T> SplitRule<T> byField(Function<T, ?> extractor) {
        return new SplitRule<T>("byField") {
            @Override
            public String getPartitionKey(T record) {
                Object value = extractor.apply(record);
                return value != null ? value.toString() : "null";
            }
        };
    }

    /**
     * Creates a rule that partitions by record count.
     * Creates partitions of approximately n records each.
     *
     * @param <T> the record type
     * @param recordsPerPartition records per partition
     * @return the split rule
     */
    public static <T> SplitRule<T> byCount(int recordsPerPartition) {
        return new CountBasedSplitRule<>(recordsPerPartition);
    }

    /**
     * Creates a rule that partitions by approximate byte size.
     *
     * @param <T> the record type
     * @param bytesPerPartition approximate bytes per partition
     * @return the split rule
     */
    public static <T> SplitRule<T> bySize(long bytesPerPartition) {
        return new SizeBasedSplitRule<>(bytesPerPartition);
    }

    /**
     * Creates a binary split rule based on a predicate.
     *
     * @param <T> the record type
     * @param predicate the test predicate
     * @param trueLabel the label for matching records
     * @param falseLabel the label for non-matching records
     * @return the split rule
     */
    public static <T> SplitRule<T> byPredicate(Predicate<T> predicate, String trueLabel, String falseLabel) {
        return new SplitRule<T>("byPredicate(" + trueLabel + "/" + falseLabel + ")") {
            @Override
            public String getPartitionKey(T record) {
                return predicate.test(record) ? trueLabel : falseLabel;
            }
        };
    }

    /**
     * Creates a multi-way split rule based on labeled predicates.
     * Predicates are evaluated in order; first match wins.
     *
     * @param <T> the record type
     * @param predicates map of label to predicate (LinkedHashMap recommended)
     * @return the split rule
     */
    public static <T> SplitRule<T> byPredicates(Map<String, Predicate<T>> predicates) {
        // Make a copy to ensure order preservation
        Map<String, Predicate<T>> orderedPredicates = new LinkedHashMap<>(predicates);

        return new SplitRule<T>("byPredicates(" + String.join(",", orderedPredicates.keySet()) + ")") {
            @Override
            public String getPartitionKey(T record) {
                for (Map.Entry<String, Predicate<T>> entry : orderedPredicates.entrySet()) {
                    if (entry.getValue().test(record)) {
                        return entry.getKey();
                    }
                }
                return "unmatched";
            }
        };
    }

    /**
     * Count-based split rule implementation.
     */
    private static class CountBasedSplitRule<T> extends SplitRule<T> {
        private final int recordsPerPartition;
        private int counter = 0;
        private int partitionIndex = 0;

        CountBasedSplitRule(int recordsPerPartition) {
            super("byCount(" + recordsPerPartition + ")");
            this.recordsPerPartition = recordsPerPartition;
        }

        @Override
        public synchronized String getPartitionKey(T record) {
            String key = "partition_" + partitionIndex;
            counter++;
            if (counter >= recordsPerPartition) {
                counter = 0;
                partitionIndex++;
            }
            return key;
        }

        @Override
        public void validateCompatibility(SplitRule<T> other) {
            if (other instanceof CountBasedSplitRule) {
                throw new SplitRuleConflictException(
                        getDescription(),
                        other.getDescription(),
                        "Cannot chain multiple count-based rules"
                );
            }
        }
    }

    /**
     * Size-based split rule implementation.
     */
    private static class SizeBasedSplitRule<T> extends SplitRule<T> {
        private final long bytesPerPartition;
        private long currentSize = 0;
        private int partitionIndex = 0;

        SizeBasedSplitRule(long bytesPerPartition) {
            super("bySize(" + bytesPerPartition + ")");
            this.bytesPerPartition = bytesPerPartition;
        }

        @Override
        public synchronized String getPartitionKey(T record) {
            // Estimate size (rough approximation)
            long recordSize = record.toString().length() * 2L; // Rough estimate
            currentSize += recordSize;

            String key = "partition_" + partitionIndex;

            if (currentSize >= bytesPerPartition) {
                currentSize = 0;
                partitionIndex++;
            }

            return key;
        }

        @Override
        public void validateCompatibility(SplitRule<T> other) {
            if (other instanceof SizeBasedSplitRule) {
                throw new SplitRuleConflictException(
                        getDescription(),
                        other.getDescription(),
                        "Cannot chain multiple size-based rules"
                );
            }
        }
    }
}
