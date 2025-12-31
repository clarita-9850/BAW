package com.example.demo.processor;

import com.cmips.integration.framework.annotations.Transformer;
import com.cmips.integration.framework.exception.TransformationException;
import com.cmips.integration.framework.interfaces.ITransformer;
import com.cmips.integration.framework.model.ValidationResult;
import com.cmips.integration.framework.util.MergeUtil;
import com.example.demo.model.Employee;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Transformer that merges multiple lists of employees.
 * Demonstrates using MergeUtil.mergeUnique() to merge with deduplication.
 */
@Slf4j
@Transformer(
    name = "employee-merger",
    description = "Merges employee lists with deduplication",
    inputType = List.class,
    outputType = List.class
)
public class EmployeeMerger implements ITransformer<List<List<Employee>>, List<Employee>> {

    @Override
    public List<Employee> transform(List<List<Employee>> sources) throws TransformationException {
        log.info("Merging {} sources", sources.size());

        // USE FRAMEWORK UTILITY!
        // Merge and remove duplicates by employee ID
        List<Employee> merged = MergeUtil.mergeUnique(sources, Employee::getId);

        log.info("Merged result: {} employees", merged.size());
        return merged;
    }

    @Override
    public ValidationResult validate(List<List<Employee>> input) {
        if (input == null) {
            return ValidationResult.invalid("Input cannot be null");
        }
        if (input.isEmpty()) {
            return ValidationResult.invalid("Input list cannot be empty");
        }
        return ValidationResult.valid();
    }
}
