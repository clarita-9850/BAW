package com.cmips.integration.framework.support;

/**
 * Defines strategies for caching transformation results.
 *
 * <p>The cache strategy determines if and how transformation results are cached.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public enum CacheStrategy {

    /**
     * No caching. Every transformation is performed fresh.
     */
    NONE,

    /**
     * Cache results based on input object identity (reference).
     */
    IDENTITY,

    /**
     * Cache results based on input object equality (equals/hashCode).
     */
    EQUALITY,

    /**
     * Cache results with a time-to-live.
     * Cached entries expire after a configured duration.
     */
    TTL,

    /**
     * Least recently used cache with a maximum size.
     */
    LRU,

    /**
     * Weak reference cache.
     * Entries can be garbage collected when memory is needed.
     */
    WEAK
}
