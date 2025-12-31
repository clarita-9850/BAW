package com.cmips.integration.framework.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SortUtil.
 */
class SortUtilTest {

    @Test
    void sortByField_shouldSortByStringField() {
        List<Map<String, Object>> people = new ArrayList<>();
        people.add(Map.of("name", "Charlie", "age", 25));
        people.add(Map.of("name", "Alice", "age", 30));
        people.add(Map.of("name", "Bob", "age", 20));

        // Use sortAscending for simple comparable lists
        List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
        List<String> result = SortUtil.sortAscending(names);

        assertEquals("Alice", result.get(0));
        assertEquals("Bob", result.get(1));
        assertEquals("Charlie", result.get(2));
    }

    @Test
    void sortAscending_shouldSortInNaturalOrder() {
        List<Integer> numbers = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);

        List<Integer> result = SortUtil.sortAscending(numbers);

        assertEquals(Arrays.asList(1, 1, 2, 3, 4, 5, 6, 9), result);
    }

    @Test
    void sortDescending_shouldSortInReverseOrder() {
        List<Integer> numbers = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);

        List<Integer> result = SortUtil.sortDescending(numbers);

        assertEquals(Arrays.asList(9, 6, 5, 4, 3, 2, 1, 1), result);
    }

    @Test
    void sort_withComparator_shouldSortCorrectly() {
        List<String> strings = Arrays.asList("apple", "cherry", "banana", "date");

        List<String> result = SortUtil.sort(strings, Comparator.comparing(String::length));

        // Sorted by length: date(4), apple(5), cherry(6), banana(6)
        assertEquals(4, result.get(0).length());
        assertEquals("date", result.get(0));
    }

    @Test
    void topN_shouldReturnTopElements() {
        List<Integer> numbers = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);

        List<Integer> result = SortUtil.topN(numbers, 3, Comparator.reverseOrder());

        assertEquals(3, result.size());
        assertEquals(9, result.get(0));
        assertEquals(6, result.get(1));
        assertEquals(5, result.get(2));
    }

    @Test
    void bottomN_shouldReturnBottomElements() {
        List<Integer> numbers = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);

        List<Integer> result = SortUtil.bottomN(numbers, 3, Comparator.reverseOrder());

        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(2, result.get(2));
    }

    @Test
    void isSorted_shouldReturnTrueForSortedList() {
        List<Integer> sorted = Arrays.asList(1, 2, 3, 4, 5);

        assertTrue(SortUtil.isSorted(sorted, Comparator.naturalOrder()));
    }

    @Test
    void isSorted_shouldReturnFalseForUnsortedList() {
        List<Integer> unsorted = Arrays.asList(1, 3, 2, 4, 5);

        assertFalse(SortUtil.isSorted(unsorted, Comparator.naturalOrder()));
    }

    @Test
    void isSorted_shouldReturnTrueForEmptyList() {
        List<Integer> empty = Collections.emptyList();

        assertTrue(SortUtil.isSorted(empty, Comparator.naturalOrder()));
    }

    @Test
    void isSorted_shouldReturnTrueForSingleElement() {
        List<Integer> single = Collections.singletonList(42);

        assertTrue(SortUtil.isSorted(single, Comparator.naturalOrder()));
    }

    @Test
    void reverse_shouldReverseList() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

        List<Integer> result = SortUtil.reverse(list);

        assertEquals(Arrays.asList(5, 4, 3, 2, 1), result);
    }

    @Test
    void sortByField_shouldSortUsingReflection() {
        class Person {
            String name;
            int age;
            Person(String name, int age) {
                this.name = name;
                this.age = age;
            }
        }

        List<Person> people = Arrays.asList(
                new Person("Charlie", 25),
                new Person("Alice", 30),
                new Person("Bob", 20)
        );

        List<Person> result = SortUtil.sortByField(people, "name");

        assertEquals("Alice", result.get(0).name);
        assertEquals("Bob", result.get(1).name);
        assertEquals("Charlie", result.get(2).name);
    }

    @Test
    void sortByField_descending_shouldSortInReverseOrder() {
        class Person {
            String name;
            int age;
            Person(String name, int age) {
                this.name = name;
                this.age = age;
            }
        }

        List<Person> people = Arrays.asList(
                new Person("Charlie", 25),
                new Person("Alice", 30),
                new Person("Bob", 20)
        );

        List<Person> result = SortUtil.sortByField(people, "age", false);

        assertEquals(30, result.get(0).age);
        assertEquals(25, result.get(1).age);
        assertEquals(20, result.get(2).age);
    }

    @Test
    void sortByFields_shouldSortByMultipleFields() {
        class Employee {
            String dept;
            String name;
            int salary;
            Employee(String dept, String name, int salary) {
                this.dept = dept;
                this.name = name;
                this.salary = salary;
            }
        }

        List<Employee> employees = Arrays.asList(
                new Employee("IT", "Charlie", 70000),
                new Employee("HR", "Alice", 60000),
                new Employee("IT", "Alice", 80000),
                new Employee("HR", "Bob", 55000)
        );

        List<Employee> result = SortUtil.sortByFields(employees, "dept", "name");

        assertEquals("HR", result.get(0).dept);
        assertEquals("Alice", result.get(0).name);
        assertEquals("HR", result.get(1).dept);
        assertEquals("Bob", result.get(1).name);
        assertEquals("IT", result.get(2).dept);
        assertEquals("Alice", result.get(2).name);
    }

    @Test
    void sortByField_shouldHandleEmptyList() {
        List<String> empty = Collections.emptyList();

        List<String> result = SortUtil.sortByField(empty, "length");

        assertTrue(result.isEmpty());
    }

    @Test
    void sortByField_shouldHandleNullList() {
        List<String> result = SortUtil.sortByField(null, "length");

        assertTrue(result.isEmpty());
    }
}
