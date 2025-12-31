package com.cmips.integration.framework.baw.repository;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Result of a merge operation with statistics.
 *
 * @param <T> the record type
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
public class MergeResult<T> {

    /**
     * The merged records.
     */
    private final List<T> records;

    /**
     * Total records from all sources before merge.
     */
    private final int sourceCount;

    /**
     * Total records after merge.
     */
    private final int totalCount;

    /**
     * Number of duplicates removed.
     */
    private final int duplicatesRemoved;

    /**
     * Number of records filtered out.
     */
    private final int filteredOut;

    /**
     * Number of records after limit applied.
     */
    private final int limitApplied;

    /**
     * Returns the merged record list.
     */
    public List<T> getRecords() {
        return records;
    }

    /**
     * Returns true if any duplicates were removed.
     */
    public boolean hadDuplicates() {
        return duplicatesRemoved > 0;
    }
}
