package com.cmips.integration.framework.baw.split;

import com.cmips.integration.framework.baw.format.FileFormat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Result of a split operation.
 *
 * <p>Example usage:
 * <pre>
 * SplitResult&lt;Payment&gt; result = repo.split(records, SplitRule.byField(Payment::getType));
 *
 * // Access partitions
 * List&lt;Payment&gt; hrmRecords = result.get("HRM");
 * List&lt;Payment&gt; finRecords = result.get("FIN");
 *
 * // Get partition keys
 * Set&lt;String&gt; keys = result.getPartitionKeys();
 *
 * // Get counts
 * Map&lt;String, Integer&gt; counts = result.getCounts();
 *
 * // Write partitions to directory
 * result.writeAll(outputDir, FileFormat.csv(), key -&gt; "payments_" + key + ".csv");
 * </pre>
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class SplitResult<T> {

    private final Map<String, List<T>> partitions;
    private final int totalCount;
    private final SplitRule<T> rule;

    private SplitResult(Map<String, List<T>> partitions, int totalCount, SplitRule<T> rule) {
        this.partitions = new LinkedHashMap<>(partitions);
        this.totalCount = totalCount;
        this.rule = rule;
    }

    /**
     * Creates a SplitResult by splitting records according to a rule.
     */
    public static <T> SplitResult<T> split(List<T> records, SplitRule<T> rule) {
        Map<String, List<T>> partitions = new LinkedHashMap<>();

        for (T record : records) {
            String key = rule.getPartitionKey(record);
            partitions.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }

        return new SplitResult<>(partitions, records.size(), rule);
    }

    /**
     * Gets records in a partition by key.
     *
     * @param key the partition key
     * @return the records in the partition, or empty list if not found
     */
    public List<T> get(String key) {
        return partitions.getOrDefault(key, Collections.emptyList());
    }

    /**
     * Gets all partition keys.
     *
     * @return set of partition keys
     */
    public Set<String> getPartitionKeys() {
        return Collections.unmodifiableSet(partitions.keySet());
    }

    /**
     * Gets the number of partitions.
     *
     * @return partition count
     */
    public int getPartitionCount() {
        return partitions.size();
    }

    /**
     * Gets record count for each partition.
     *
     * @return map of partition key to count
     */
    public Map<String, Integer> getCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Map.Entry<String, List<T>> entry : partitions.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    /**
     * Gets the total record count across all partitions.
     *
     * @return total count
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Returns true if a partition exists.
     *
     * @param key the partition key
     * @return true if partition exists
     */
    public boolean hasPartition(String key) {
        return partitions.containsKey(key);
    }

    /**
     * Returns true if partition is empty or doesn't exist.
     *
     * @param key the partition key
     * @return true if empty or missing
     */
    public boolean isEmpty(String key) {
        List<T> partition = partitions.get(key);
        return partition == null || partition.isEmpty();
    }

    /**
     * Gets all partitions as a map.
     *
     * @return unmodifiable map of partitions
     */
    public Map<String, List<T>> getPartitions() {
        return Collections.unmodifiableMap(partitions);
    }

    /**
     * Gets the split rule used to create this result.
     *
     * @return the split rule
     */
    public SplitRule<T> getRule() {
        return rule;
    }

    /**
     * Interface for writing partitions.
     */
    @FunctionalInterface
    public interface PartitionWriter<T> {
        void write(String key, List<T> records, Path path) throws Exception;
    }

    /**
     * Writes all partitions to a directory.
     *
     * @param directory the output directory
     * @param format the file format
     * @param filenameGenerator function to generate filename from partition key
     * @param writer the partition writer
     * @throws Exception if writing fails
     */
    public void writeAll(Path directory, FileFormat format,
                         Function<String, String> filenameGenerator,
                         PartitionWriter<T> writer) throws Exception {
        for (Map.Entry<String, List<T>> entry : partitions.entrySet()) {
            String filename = filenameGenerator.apply(entry.getKey());
            Path path = directory.resolve(filename);
            writer.write(entry.getKey(), entry.getValue(), path);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SplitResult{");
        sb.append("partitions=").append(partitions.size());
        sb.append(", totalRecords=").append(totalCount);
        sb.append(", counts=").append(getCounts());
        sb.append("}");
        return sb.toString();
    }
}
