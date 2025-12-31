package com.cmips.integration.framework.support;

/**
 * Defines algorithms for calculating file checksums.
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ChecksumAlgorithm {

    /**
     * MD5 message digest algorithm.
     * Produces a 128-bit hash value.
     */
    MD5("MD5"),

    /**
     * SHA-1 secure hash algorithm.
     * Produces a 160-bit hash value.
     */
    SHA1("SHA-1"),

    /**
     * SHA-256 secure hash algorithm.
     * Produces a 256-bit hash value.
     */
    SHA256("SHA-256"),

    /**
     * SHA-512 secure hash algorithm.
     * Produces a 512-bit hash value.
     */
    SHA512("SHA-512"),

    /**
     * CRC32 cyclic redundancy check.
     * Fast but not cryptographically secure.
     */
    CRC32("CRC32");

    private final String algorithmName;

    ChecksumAlgorithm(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    /**
     * Returns the algorithm name as used by Java's MessageDigest.
     *
     * @return the algorithm name
     */
    public String getAlgorithmName() {
        return algorithmName;
    }
}
