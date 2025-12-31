package com.example.demo.processor;

import com.cmips.integration.framework.annotations.Transformer;
import com.cmips.integration.framework.exception.TransformationException;
import com.cmips.integration.framework.interfaces.ITransformer;
import com.cmips.integration.framework.model.ValidationResult;
import com.cmips.integration.framework.util.TransformUtil;
import com.example.demo.model.Employee;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Transformer that converts employee list to JSON.
 * Demonstrates using TransformUtil.toJson() for serialization.
 */
@Slf4j
@Transformer(
    name = "xml-to-json",
    description = "Converts employee list to JSON format",
    inputType = List.class,
    outputType = String.class
)
public class XmlToJsonTransformer implements ITransformer<List<Employee>, String> {

    @Override
    public String transform(List<Employee> employees) throws TransformationException {
        log.info("Converting {} employees to JSON", employees.size());

        // USE FRAMEWORK UTILITY!
        String json = TransformUtil.toJson(employees);

        log.info("Generated JSON: {} characters", json.length());
        return json;
    }

    @Override
    public ValidationResult validate(List<Employee> input) {
        if (input == null) {
            return ValidationResult.invalid("Input cannot be null");
        }
        if (input.isEmpty()) {
            return ValidationResult.invalid("Input list cannot be empty");
        }
        return ValidationResult.valid();
    }
}
