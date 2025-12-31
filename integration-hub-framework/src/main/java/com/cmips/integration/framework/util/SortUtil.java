package com.cmips.integration.framework.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for sorting data collections.
 *
 * <p>This class provides static utility methods for sorting lists by
 * various criteria including field names, multiple fields, and custom comparators.
 *
 * <p>Example usage:
 * <pre>
 * // Sort by single field
 * List&lt;Payment&gt; sorted = SortUtil.sortByField(payments, "amount");
 *
 * // Sort by multiple fields
 * List&lt;Payment&gt; multiSorted = SortUtil.sortByFields(payments, "date", "amount");
 *
 * // Sort with custom comparator
 * List&lt;Payment&gt; custom = SortUtil.sort(payments, Comparator.comparing(Payment::getStatus));
 *
 * // Sort comparable elements
 * List&lt;String&gt; ascending = SortUtil.sortAscending(names);
 * List&lt;String&gt; descending = SortUtil.sortDescending(names);
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SortUtil {

    private SortUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Sorts a list by a single field name using reflection.
     *
     * @param <T> the element type
     * @param data the list to sort
     * @param fieldName the field to sort by
     * @return a new sorted list
     */
    public static <T> List<T> sortByField(List<T> data, String fieldName) {
        return sortByField(data, fieldName, true);
    }

    /**
     * Sorts a list by a single field name with direction.
     *
     * @param <T> the element type
     * @param data the list to sort
     * @param fieldName the field to sort by
     * @param ascending {@code true} for ascending order
     * @return a new sorted list
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> sortByField(List<T> data, String fieldName, boolean ascending) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> result = new ArrayList<>(data);
        Comparator<T> comparator = (o1, o2) -> {
            try {
                Comparable<Object> v1 = (Comparable<Object>) getFieldValue(o1, fieldName);
                Comparable<Object> v2 = (Comparable<Object>) getFieldValue(o2, fieldName);

                if (v1 == null && v2 == null) return 0;
                if (v1 == null) return ascending ? -1 : 1;
                if (v2 == null) return ascending ? 1 : -1;

                return v1.compareTo(v2);
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot compare field: " + fieldName, e);
            }
        };

        if (!ascending) {
            comparator = comparator.reversed();
        }

        result.sort(comparator);
        return result;
    }

    /**
     * Sorts a list by multiple fields.
     *
     * <p>Fields are applied in order, with earlier fields having higher priority.
     *
     * @param <T> the element type
     * @param data the list to sort
     * @param fieldNames the fields to sort by (in priority order)
     * @return a new sorted list
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> sortByFields(List<T> data, String... fieldNames) {
        if (data == null || data.isEmpty() || fieldNames == null || fieldNames.length == 0) {
            return data == null ? new ArrayList<>() : new ArrayList<>(data);
        }

        List<T> result = new ArrayList<>(data);

        Comparator<T> comparator = null;
        for (String fieldName : fieldNames) {
            Comparator<T> fieldComparator = (o1, o2) -> {
                try {
                    Comparable<Object> v1 = (Comparable<Object>) getFieldValue(o1, fieldName);
                    Comparable<Object> v2 = (Comparable<Object>) getFieldValue(o2, fieldName);

                    if (v1 == null && v2 == null) return 0;
                    if (v1 == null) return -1;
                    if (v2 == null) return 1;

                    return v1.compareTo(v2);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Cannot compare field: " + fieldName, e);
                }
            };

            if (comparator == null) {
                comparator = fieldComparator;
            } else {
                comparator = comparator.thenComparing(fieldComparator);
            }
        }

        result.sort(comparator);
        return result;
    }

    /**
     * Sorts a list using a custom comparator.
     *
     * @param <T> the element type
     * @param data the list to sort
     * @param comparator the comparator
     * @return a new sorted list
     */
    public static <T> List<T> sort(List<T> data, Comparator<T> comparator) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>(data);
        result.sort(comparator);
        return result;
    }

    /**
     * Sorts a list of comparable elements in ascending order.
     *
     * @param <T> the element type (must be Comparable)
     * @param data the list to sort
     * @return a new sorted list
     */
    public static <T extends Comparable<? super T>> List<T> sortAscending(List<T> data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>(data);
        result.sort(Comparator.naturalOrder());
        return result;
    }

    /**
     * Sorts a list of comparable elements in descending order.
     *
     * @param <T> the element type (must be Comparable)
     * @param data the list to sort
     * @return a new sorted list
     */
    public static <T extends Comparable<? super T>> List<T> sortDescending(List<T> data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>(data);
        result.sort(Comparator.reverseOrder());
        return result;
    }

    /**
     * Reverses the order of elements in a list.
     *
     * @param <T> the element type
     * @param data the list to reverse
     * @return a new reversed list
     */
    public static <T> List<T> reverse(List<T> data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>(data.size());
        for (int i = data.size() - 1; i >= 0; i--) {
            result.add(data.get(i));
        }
        return result;
    }

    /**
     * Gets the top N elements from a list according to a comparator.
     *
     * @param <T> the element type
     * @param data the list
     * @param n the number of elements to return
     * @param comparator the comparator
     * @return the top N elements
     */
    public static <T> List<T> topN(List<T> data, int n, Comparator<T> comparator) {
        if (data == null || data.isEmpty() || n <= 0) {
            return new ArrayList<>();
        }
        List<T> sorted = sort(data, comparator);
        return new ArrayList<>(sorted.subList(0, Math.min(n, sorted.size())));
    }

    /**
     * Gets the bottom N elements from a list according to a comparator.
     *
     * @param <T> the element type
     * @param data the list
     * @param n the number of elements to return
     * @param comparator the comparator
     * @return the bottom N elements
     */
    public static <T> List<T> bottomN(List<T> data, int n, Comparator<T> comparator) {
        return topN(data, n, comparator.reversed());
    }

    /**
     * Checks if a list is sorted according to a comparator.
     *
     * @param <T> the element type
     * @param data the list to check
     * @param comparator the comparator
     * @return {@code true} if the list is sorted
     */
    public static <T> boolean isSorted(List<T> data, Comparator<T> comparator) {
        if (data == null || data.size() <= 1) {
            return true;
        }
        for (int i = 0; i < data.size() - 1; i++) {
            if (comparator.compare(data.get(i), data.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    private static Object getFieldValue(Object obj, String fieldName) throws Exception {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field not found: " + fieldName);
    }
}
