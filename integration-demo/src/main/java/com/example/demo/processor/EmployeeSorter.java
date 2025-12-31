package com.example.demo.processor;

import com.cmips.integration.framework.annotations.Transformer;
import com.cmips.integration.framework.exception.TransformationException;
import com.cmips.integration.framework.interfaces.ITransformer;
import com.cmips.integration.framework.model.ValidationResult;
import com.example.demo.model.Employee;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transformer that sorts employees by ID.
 * Demonstrates a simple transformation that sorts data.
 */
@Slf4j
@Transformer(
    name = "employee-sorter",
    description = "Sorts employees by ID",
    inputType = List.class,
    outputType = List.class
)
public class EmployeeSorter implements ITransformer<List<Employee>, List<Employee>> {

    @Override
    public List<Employee> transform(List<Employee> employees) throws TransformationException {
        log.info("Sorting {} employees by ID", employees.size());

        List<Employee> sorted = employees.stream()
            .sorted(Comparator.comparing(Employee::getId))
            .collect(Collectors.toList());

        log.info("Sorted {} employees", sorted.size());
        return sorted;
    }

    @Override
    public ValidationResult validate(List<Employee> input) {
        if (input == null) {
            return ValidationResult.invalid("Input cannot be null");
        }
        return ValidationResult.valid();
    }
}
