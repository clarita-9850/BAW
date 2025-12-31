package com.example.demo.reader;

import com.cmips.integration.framework.annotations.InputSource;
import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.ReadException;
import com.cmips.integration.framework.interfaces.IInputSource;
import com.cmips.integration.framework.util.FileUtil;
import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeList;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Input source that reads salary data from an XML file.
 * Demonstrates implementing IInputSource and using FileUtil.parseXml()
 */
@Slf4j
@InputSource(name = "salary-reader", description = "Reads salary data from XML file", order = 2)
public class SalaryFileReader implements IInputSource<Employee> {

    private final Path inputFile = Paths.get("./data/input/salaries.xml");
    private boolean connected = false;
    private List<Employee> data;

    @Override
    public void connect() throws ConnectionException {
        log.info("Salary Reader: Connecting to {}", inputFile);
        connected = true;
    }

    @Override
    public boolean hasData() {
        boolean exists = Files.exists(inputFile);
        log.info("Salary file exists: {}", exists);
        return exists;
    }

    @Override
    public List<Employee> read() throws ReadException {
        log.info("Reading salaries from: {}", inputFile);
        try {
            // USE FRAMEWORK UTILITY!
            EmployeeList empList = FileUtil.parseXml(inputFile, EmployeeList.class);
            data = empList.getEmployees();
            log.info("Read {} salary records", data.size());
            return data;
        } catch (IOException e) {
            throw new ReadException("Failed to read salaries file", e);
        }
    }

    @Override
    public void acknowledge() {
        log.info("Salary file processed successfully");
    }

    @Override
    public void close() {
        connected = false;
        data = null;
        log.info("Salary Reader closed");
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}
