package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main application class for the Integration Hub Framework demo.
 *
 * This demo shows how to:
 * 1. Add the framework as a Maven dependency
 * 2. Implement IInputSource, ITransformer, and IOutputDestination interfaces
 * 3. Use framework annotations (@InputSource, @Transformer, @OutputDestination)
 * 4. Use framework utilities (FileUtil, MergeUtil, TransformUtil)
 */
@Slf4j
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    @Order(1)
    public CommandLineRunner init() {
        return args -> {
            log.info("Creating directories...");
            Files.createDirectories(Paths.get("./data/input"));
            Files.createDirectories(Paths.get("./data/output"));
            Files.createDirectories(Paths.get("./data/upload"));

            // Create sample files only if not running as web server
            if (args.length == 0 || !args[0].equals("--web")) {
                createSampleFiles();
            }
            
            log.info("");
            log.info("========================================");
            log.info("  Integration Hub Framework Demo       ");
            log.info("  Web Interface: http://localhost:8081");
            log.info("  API Base: http://localhost:8081/api/files");
            log.info("========================================");
            log.info("");
        };
    }

    private void createSampleFiles() {
        try {
            // Create employees.xml
            String employeesXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <employees>
                    <employee>
                        <id>101</id>
                        <name>Alice Johnson</name>
                        <department>Engineering</department>
                        <salary>85000</salary>
                    </employee>
                    <employee>
                        <id>102</id>
                        <name>Bob Smith</name>
                        <department>Sales</department>
                        <salary>75000</salary>
                    </employee>
                    <employee>
                        <id>103</id>
                        <name>Carol Williams</name>
                        <department>HR</department>
                        <salary>65000</salary>
                    </employee>
                </employees>
                """;

            Path empFile = Paths.get("./data/input/employees.xml");
            if (!Files.exists(empFile)) {
                Files.writeString(empFile, employeesXml);
                log.info("Created: {}", empFile);
            }

            // Create salaries.xml (with one duplicate - Bob Smith ID 102)
            String salariesXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <employees>
                    <employee>
                        <id>104</id>
                        <name>David Brown</name>
                        <department>Engineering</department>
                        <salary>90000</salary>
                    </employee>
                    <employee>
                        <id>105</id>
                        <name>Eve Davis</name>
                        <department>Marketing</department>
                        <salary>70000</salary>
                    </employee>
                    <employee>
                        <id>102</id>
                        <name>Bob Smith</name>
                        <department>Sales</department>
                        <salary>75000</salary>
                    </employee>
                </employees>
                """;

            Path salFile = Paths.get("./data/input/salaries.xml");
            if (!Files.exists(salFile)) {
                Files.writeString(salFile, salariesXml);
                log.info("Created: {}", salFile);
            }

            log.info("Sample files ready!");

        } catch (Exception e) {
            log.error("Failed to create sample files", e);
        }
    }
}
