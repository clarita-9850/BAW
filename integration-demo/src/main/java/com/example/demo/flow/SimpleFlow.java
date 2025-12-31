package com.example.demo.flow;

import com.cmips.integration.framework.model.SendResult;
import com.example.demo.model.Employee;
import com.example.demo.processor.EmployeeMerger;
import com.example.demo.processor.EmployeeSorter;
import com.example.demo.processor.XmlToJsonTransformer;
import com.example.demo.reader.EmployeeFileReader;
import com.example.demo.reader.SalaryFileReader;
import com.example.demo.writer.JsonFileWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Simple integration flow that demonstrates the framework.
 *
 * Flow steps:
 * 1. Read employees.xml (IInputSource)
 * 2. Read salaries.xml (IInputSource)
 * 3. Merge with deduplication (ITransformer + MergeUtil)
 * 4. Sort by ID (ITransformer)
 * 5. Convert to JSON (ITransformer + TransformUtil)
 * 6. Write output file (IOutputDestination + FileUtil)
 */
@Slf4j
@Component
@Order(2) // Run after the init CommandLineRunner
public class SimpleFlow implements CommandLineRunner {

    @Autowired
    private EmployeeFileReader employeeReader;

    @Autowired
    private SalaryFileReader salaryReader;

    @Autowired
    private EmployeeMerger merger;

    @Autowired
    private EmployeeSorter sorter;

    @Autowired
    private XmlToJsonTransformer jsonTransformer;

    @Autowired
    private JsonFileWriter jsonWriter;

    @Override
    public void run(String... args) throws Exception {
        log.info("");
        log.info("========================================");
        log.info("  SIMPLE INTEGRATION DEMO - STARTING   ");
        log.info("========================================");
        log.info("");

        try {
            // Step 1: Read employees file
            log.info("-> Step 1: Reading employees file...");
            employeeReader.connect();
            List<Employee> employees = null;
            if (employeeReader.hasData()) {
                employees = employeeReader.read();
            }
            employeeReader.acknowledge();
            employeeReader.close();

            // Step 2: Read salaries file
            log.info("");
            log.info("-> Step 2: Reading salaries file...");
            salaryReader.connect();
            List<Employee> salaries = null;
            if (salaryReader.hasData()) {
                salaries = salaryReader.read();
            }
            salaryReader.acknowledge();
            salaryReader.close();

            if (employees == null || salaries == null) {
                log.error("Files not found! Please ensure files exist in ./data/input/");
                return;
            }

            // Step 3: Merge (using framework utility)
            log.info("");
            log.info("-> Step 3: Merging data...");
            List<List<Employee>> sources = Arrays.asList(employees, salaries);
            List<Employee> merged = merger.transform(sources);

            // Step 4: Sort (using framework)
            log.info("");
            log.info("-> Step 4: Sorting data...");
            List<Employee> sorted = sorter.transform(merged);

            // Step 5: Transform to JSON (using framework utility)
            log.info("");
            log.info("-> Step 5: Converting to JSON...");
            String json = jsonTransformer.transform(sorted);

            // Step 6: Write output (using framework utility)
            log.info("");
            log.info("-> Step 6: Writing output file...");
            jsonWriter.connect();
            SendResult result = jsonWriter.send(json);
            jsonWriter.verify(result);
            jsonWriter.close();

            log.info("");
            log.info("========================================");
            log.info("  DEMO COMPLETED SUCCESSFULLY!         ");
            log.info("  Output: {}  ", result.getMetadata().get("path"));
            log.info("========================================");
            log.info("");

        } catch (Exception e) {
            log.error("Demo failed!", e);
        }
    }
}
