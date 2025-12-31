package com.example.demo.flow;

import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.repository.FileRepository;
import com.cmips.integration.framework.baw.repository.MergeResult;
import com.cmips.integration.framework.baw.repository.Schema;
import com.cmips.integration.framework.baw.split.SplitResult;
import com.cmips.integration.framework.baw.split.SplitRule;
import com.example.demo.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BAW Framework style integration flow.
 *
 * Demonstrates the new JPA-inspired declarative API:
 * - FileRepository.forType() for type-safe operations
 * - FileFormat builders for format configuration
 * - MergeBuilder for fluent merge operations
 * - SplitRule for partitioning records
 *
 * This is a simpler, more declarative approach compared to the
 * traditional IInputSource/ITransformer/IOutputDestination pattern.
 */
@Slf4j
@Component
@Order(3) // Run after the other flows
public class BawStyleFlow implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        log.info("");
        log.info("========================================");
        log.info("  BAW FRAMEWORK DEMO - STARTING        ");
        log.info("========================================");
        log.info("");

        try {
            // Step 1: Create a repository for Employee type
            log.info("-> Step 1: Creating FileRepository for Employee...");
            FileRepository<Employee> repo = FileRepository.forType(Employee.class);

            // Show schema information
            Schema schema = repo.getSchema();
            log.info("Schema: {} (version {})", schema.getName(), schema.getVersion());
            log.info("Columns: {}", schema.getColumns().size());
            log.info("Identity columns: {}", schema.getIdColumns().size());

            // Step 2: Read files using the repository
            log.info("");
            log.info("-> Step 2: Reading files using FileRepository...");
            Path employeesPath = Paths.get("./data/input/employees.xml");
            Path salariesPath = Paths.get("./data/input/salaries.xml");

            // Create sample data for BAW demo (CSV format)
            createCsvSampleFiles();

            Path empCsvPath = Paths.get("./data/input/employees.csv");
            Path salCsvPath = Paths.get("./data/input/salaries.csv");

            List<Employee> employees = repo.read(empCsvPath, FileFormat.csv().build());
            log.info("Read {} employees from CSV", employees.size());

            List<Employee> salaries = repo.read(salCsvPath, FileFormat.csv().build());
            log.info("Read {} salary records from CSV", salaries.size());

            // Step 3: Merge with the fluent API
            log.info("");
            log.info("-> Step 3: Merging with fluent MergeBuilder...");
            MergeResult<Employee> mergeResult = repo.merge(employees, salaries)
                    .sortBy(Employee::getId)  // Sort by ID
                    .ascending()
                    .deduplicate()            // Remove duplicates by @FileId (id field)
                    .filter(e -> e.getSalary().compareTo(BigDecimal.ZERO) > 0)  // Only positive salaries
                    .buildWithStats();

            log.info("Merge statistics:");
            log.info("  - Source records: {}", mergeResult.getSourceCount());
            log.info("  - Result records: {}", mergeResult.getTotalCount());
            log.info("  - Duplicates removed: {}", mergeResult.getDuplicatesRemoved());
            log.info("  - Filtered out: {}", mergeResult.getFilteredOut());

            List<Employee> merged = mergeResult.getRecords();

            // Step 4: Split by department
            log.info("");
            log.info("-> Step 4: Splitting by department...");
            SplitResult<Employee> splitResult = repo.split(merged, SplitRule.byField(Employee::getDepartment));

            log.info("Split into {} partitions:", splitResult.getPartitionCount());
            for (String key : splitResult.getPartitionKeys()) {
                log.info("  - {}: {} employees", key, splitResult.get(key).size());
            }

            // Step 5: Write output in different formats
            log.info("");
            log.info("-> Step 5: Writing output files...");

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // Write as JSON
            Path jsonPath = Paths.get("./data/output/baw_employees_" + timestamp + ".json");
            repo.write(merged, jsonPath, FileFormat.json().prettyPrint(true).build());
            log.info("Wrote JSON: {}", jsonPath);

            // Write as CSV
            Path csvPath = Paths.get("./data/output/baw_employees_" + timestamp + ".csv");
            repo.write(merged, csvPath, FileFormat.csv().build());
            log.info("Wrote CSV: {}", csvPath);

            // Write department partitions
            for (String dept : splitResult.getPartitionKeys()) {
                Path deptPath = Paths.get("./data/output/baw_" + dept.toLowerCase() + "_" + timestamp + ".json");
                repo.write(splitResult.get(dept), deptPath, FileFormat.json().prettyPrint(true).build());
                log.info("Wrote department file: {}", deptPath);
            }

            // Step 6: Query operations
            log.info("");
            log.info("-> Step 6: Demonstrating query operations...");
            long engineeringCount = repo.count(merged, e -> "Engineering".equals(e.getDepartment()));
            log.info("Engineering employees: {}", engineeringCount);

            repo.findFirst(merged, e -> e.getSalary().compareTo(new BigDecimal("80000")) > 0)
                    .ifPresent(e -> log.info("First high earner (>80k): {} - ${}", e.getName(), e.getSalary()));

            log.info("");
            log.info("========================================");
            log.info("  BAW FRAMEWORK DEMO COMPLETED!        ");
            log.info("========================================");
            log.info("");

        } catch (Exception e) {
            log.error("BAW Demo failed!", e);
        }
    }

    private void createCsvSampleFiles() throws Exception {
        // Create employees.csv
        String employeesCsv = """
                id,name,department,salary
                101,Alice Johnson,Engineering,85000
                102,Bob Smith,Sales,75000
                103,Carol Williams,HR,65000
                """;

        Path empCsvPath = Paths.get("./data/input/employees.csv");
        java.nio.file.Files.writeString(empCsvPath, employeesCsv);

        // Create salaries.csv (with duplicate Bob Smith)
        String salariesCsv = """
                id,name,department,salary
                104,David Brown,Engineering,90000
                105,Eve Davis,Marketing,70000
                102,Bob Smith,Sales,75000
                """;

        Path salCsvPath = Paths.get("./data/input/salaries.csv");
        java.nio.file.Files.writeString(salCsvPath, salariesCsv);

        log.info("Created CSV sample files for BAW demo");
    }
}
