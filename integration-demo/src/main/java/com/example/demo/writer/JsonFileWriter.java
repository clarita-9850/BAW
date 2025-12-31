package com.example.demo.writer;

import com.cmips.integration.framework.annotations.OutputDestination;
import com.cmips.integration.framework.exception.ConnectionException;
import com.cmips.integration.framework.exception.SendException;
import com.cmips.integration.framework.interfaces.IOutputDestination;
import com.cmips.integration.framework.model.SendResult;
import com.cmips.integration.framework.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Output destination that writes JSON content to a file.
 * Demonstrates implementing IOutputDestination and using FileUtil.writeFile()
 */
@Slf4j
@OutputDestination(name = "json-writer", description = "Writes JSON to output file", required = true)
public class JsonFileWriter implements IOutputDestination<String> {

    private boolean connected = false;
    private Path lastWrittenFile;

    @Override
    public void connect() throws ConnectionException {
        log.info("JSON Writer: Ready to write");
        connected = true;
    }

    @Override
    public SendResult send(String jsonContent) throws SendException {
        // Generate filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("employees_%s.json", timestamp);
        Path outputFile = Paths.get("./data/output", filename);

        log.info("Writing JSON to: {}", outputFile);

        try {
            // USE FRAMEWORK UTILITY!
            FileUtil.writeFile(outputFile, jsonContent);
            lastWrittenFile = outputFile;

            log.info("Successfully wrote: {}", outputFile);

            return SendResult.builder()
                .success(true)
                .message("File written: " + filename)
                .metadata("filename", filename)
                .metadata("path", outputFile.toString())
                .metadata("size", jsonContent.length())
                .build();

        } catch (IOException e) {
            throw new SendException("Failed to write output file", e);
        }
    }

    @Override
    public boolean verify(SendResult result) {
        String path = result.getMetadata("path", String.class).orElse(null);
        if (path == null) {
            return false;
        }
        boolean exists = Files.exists(Paths.get(path));
        log.info("Verification: File exists = {}", exists);
        return exists;
    }

    @Override
    public void close() {
        connected = false;
        log.info("JSON Writer closed");
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}
