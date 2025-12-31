package com.cmips.integration.framework.support;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.regex.Pattern;

/**
 * Defines a pattern for matching file names.
 *
 * <p>Supports both glob patterns and regular expressions.
 *
 * <p>Example usage:
 * <pre>
 * // Glob pattern
 * FilePattern glob = FilePattern.glob("*.xml");
 *
 * // Regex pattern
 * FilePattern regex = FilePattern.regex("payment_\\d{8}\\.xml");
 *
 * // Check if file matches
 * if (pattern.matches(path)) {
 *     processFile(path);
 * }
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FilePattern {

    private final String pattern;
    private final PatternType type;
    private final PathMatcher pathMatcher;
    private final Pattern regexPattern;

    private FilePattern(String pattern, PatternType type) {
        this.pattern = pattern;
        this.type = type;

        if (type == PatternType.GLOB) {
            this.pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            this.regexPattern = null;
        } else {
            this.pathMatcher = null;
            this.regexPattern = Pattern.compile(pattern);
        }
    }

    /**
     * Creates a glob pattern.
     *
     * @param pattern the glob pattern (e.g., "*.xml", "payment_*.csv")
     * @return a FilePattern
     */
    public static FilePattern glob(String pattern) {
        return new FilePattern(pattern, PatternType.GLOB);
    }

    /**
     * Creates a regex pattern.
     *
     * @param pattern the regex pattern
     * @return a FilePattern
     */
    public static FilePattern regex(String pattern) {
        return new FilePattern(pattern, PatternType.REGEX);
    }

    /**
     * Creates a pattern that matches any file.
     *
     * @return a FilePattern matching all files
     */
    public static FilePattern any() {
        return glob("*");
    }

    /**
     * Checks if the given path matches this pattern.
     *
     * @param path the path to check
     * @return {@code true} if the path matches
     */
    public boolean matches(Path path) {
        if (path == null) {
            return false;
        }

        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }

        if (type == PatternType.GLOB) {
            return pathMatcher.matches(fileName);
        } else {
            return regexPattern.matcher(fileName.toString()).matches();
        }
    }

    /**
     * Checks if the given file name matches this pattern.
     *
     * @param fileName the file name to check
     * @return {@code true} if the name matches
     */
    public boolean matches(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        if (type == PatternType.GLOB) {
            return pathMatcher.matches(Path.of(fileName));
        } else {
            return regexPattern.matcher(fileName).matches();
        }
    }

    /**
     * Returns the pattern string.
     *
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns the pattern type.
     *
     * @return the type
     */
    public PatternType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "FilePattern{" + type + ": " + pattern + '}';
    }

    /**
     * The type of pattern.
     */
    public enum PatternType {
        GLOB,
        REGEX
    }
}
