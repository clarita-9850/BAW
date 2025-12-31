package com.cmips.integration.framework.support;

/**
 * Defines strategies for merging data from multiple sources.
 *
 * <p>The merge strategy determines how data from multiple sources
 * is combined into a single result set.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public enum MergeStrategy {

    /**
     * Concatenate all sources in order.
     * All records from source 1, then all from source 2, etc.
     */
    CONCATENATE,

    /**
     * Interleave records from each source.
     * Takes one record from each source in round-robin fashion.
     */
    INTERLEAVE,

    /**
     * When duplicate keys exist, keep the first occurrence.
     */
    FIRST_WINS,

    /**
     * When duplicate keys exist, keep the last occurrence.
     */
    LAST_WINS,

    /**
     * Remove all duplicates based on equality.
     */
    UNIQUE,

    /**
     * Custom merge logic defined by the merger implementation.
     */
    CUSTOM
}
