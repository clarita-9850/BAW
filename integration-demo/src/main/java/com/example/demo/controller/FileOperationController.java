package com.example.demo.controller;

import com.example.demo.dto.FileOperationRequest;
import com.example.demo.dto.FileOperationResponse;
import com.example.demo.dto.SchemaInfoResponse;
import com.example.demo.model.Employee;
import com.example.demo.service.FileOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for file operations using the Integration Hub Framework.
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileOperationController {

    @Autowired
    private FileOperationService fileOperationService;

    /**
     * Get schema information for a record type.
     */
    @GetMapping("/schema/{recordType}")
    public ResponseEntity<?> getSchema(@PathVariable String recordType) {
        try {
            Class<?> recordTypeClass = getRecordTypeClass(recordType);
            SchemaInfoResponse schema = fileOperationService.getSchemaInfo(recordTypeClass);
            return ResponseEntity.ok(schema);
        } catch (ClassNotFoundException e) {
            log.error("Record type not found: {}", recordType, e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Record type not found: " + recordType,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error getting schema for type: {}", recordType, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get schema",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Merge multiple files (with file upload support).
     */
    @PostMapping(value = "/merge", consumes = {"multipart/form-data"})
    public ResponseEntity<FileOperationResponse> mergeFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("recordType") String recordType,
            @RequestParam(value = "inputFormat", required = false) String inputFormat,
            @RequestParam(value = "format", required = false, defaultValue = "JSON") String outputFormat,
            @RequestParam(value = "outputFilePath", required = false) String outputFilePath,
            @RequestParam(value = "deduplicate", required = false) String deduplicateStr,
            @RequestParam(value = "keepFirst", required = false) String keepFirstStr,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "ASC") String sortOrder,
            @RequestParam(value = "filterExpressions", required = false) String filterExpressions,
            @RequestParam(value = "limit", required = false) Integer limit) {
        try {
            // Auto-detect input format from file extension if not provided
            if (inputFormat == null || inputFormat.isEmpty()) {
                if (files != null && files.length > 0 && !files[0].isEmpty()) {
                    String filename = files[0].getOriginalFilename();
                    inputFormat = detectFormatFromFilename(filename);
                }
                if (inputFormat == null) {
                    inputFormat = "CSV"; // Default
                }
            }
            
            log.info("Merge request received: {} files, recordType={}, inputFormat={}, outputFormat={}", 
                    files != null ? files.length : 0, recordType, inputFormat, outputFormat);
            
            // Save uploaded files and get their paths
            List<String> savedFilePaths = saveUploadedFiles(files);
            log.info("Saved {} files to: {}", savedFilePaths.size(), savedFilePaths);
            
            // Verify files exist before proceeding
            for (String filePath : savedFilePaths) {
                Path path = Paths.get(filePath);
                if (!Files.exists(path)) {
                    throw new RuntimeException("Saved file does not exist: " + filePath);
                }
                log.debug("Verified file exists: {} (size: {} bytes)", filePath, Files.size(path));
            }
            
            // Parse boolean parameters (FormData sends as strings)
            boolean deduplicate = "true".equalsIgnoreCase(deduplicateStr);
            boolean keepFirst = keepFirstStr == null || "true".equalsIgnoreCase(keepFirstStr);
            
            // Build request object - use inputFormat for reading, outputFormat for writing
            FileOperationRequest request = FileOperationRequest.builder()
                    .recordType(recordType)
                    .inputFilePaths(savedFilePaths)
                    .format(inputFormat) // Input format for reading files
                    .outputFormat(outputFormat) // Output format for writing
                    .outputFilePath(outputFilePath)
                    .mergeOptions(FileOperationRequest.MergeOptions.builder()
                            .deduplicate(deduplicate)
                            .keepFirst(keepFirst)
                            .sortField(sortField)
                            .sortOrder(sortOrder)
                            .filterExpressions(filterExpressions != null && !filterExpressions.trim().isEmpty() ?
                                    Arrays.stream(filterExpressions.split("\n"))
                                            .map(String::trim)
                                            .filter(s -> !s.isEmpty())
                                            .collect(java.util.stream.Collectors.toList()) : null)
                            .limit(limit)
                            .build())
                    .build();
            
            Class<?> recordTypeClass = getRecordTypeClass(recordType);
            FileOperationResponse response = fileOperationService.mergeFiles(request, recordTypeClass);

            // Clean up uploaded files after successful processing
            cleanupFiles(savedFilePaths);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error merging files", e);
            FileOperationResponse errorResponse = FileOperationResponse.builder()
                    .success(false)
                    .message("Merge operation failed: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Split a file into multiple partitions (with file upload support).
     */
    @PostMapping(value = "/split", consumes = {"multipart/form-data"})
    public ResponseEntity<FileOperationResponse> splitFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("recordType") String recordType,
            @RequestParam(value = "inputFormat", required = false) String inputFormat,
            @RequestParam(value = "format", defaultValue = "JSON") String outputFormat,
            @RequestParam(value = "outputFilePath", required = false) String outputFilePath,
            @RequestParam("splitType") String splitType,
            @RequestParam(value = "splitField", required = false) String splitField,
            @RequestParam(value = "splitCount", required = false) Integer splitCount) {
        try {
            // Auto-detect input format from file extension if not provided
            if (inputFormat == null || inputFormat.isEmpty()) {
                inputFormat = detectFormatFromFilename(file.getOriginalFilename());
            }

            log.info("Split request: recordType={}, inputFormat={}, outputFormat={}",
                    recordType, inputFormat, outputFormat);

            // Save uploaded file and get its path
            String savedFilePath = saveUploadedFile(file);
            
            // Build request object - use inputFormat for reading, outputFormat for writing
            FileOperationRequest request = FileOperationRequest.builder()
                    .recordType(recordType)
                    .inputFilePaths(List.of(savedFilePath))
                    .format(inputFormat)
                    .outputFormat(outputFormat)
                    .outputFilePath(outputFilePath)
                    .splitOptions(FileOperationRequest.SplitOptions.builder()
                            .splitType(splitType)
                            .splitField(splitField)
                            .splitCount(splitCount)
                            .build())
                    .build();

            Class<?> recordTypeClass = getRecordTypeClass(recordType);
            FileOperationResponse response = fileOperationService.splitFile(request, recordTypeClass);

            // Clean up uploaded file after processing
            cleanupFiles(List.of(savedFilePath));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error splitting file", e);
            FileOperationResponse errorResponse = FileOperationResponse.builder()
                    .success(false)
                    .message("Split operation failed: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Split a file by columns - creates multiple output files with different column subsets.
     * This is a vertical split (by columns) rather than horizontal split (by records).
     */
    @PostMapping(value = "/split-columns", consumes = {"multipart/form-data"})
    public ResponseEntity<FileOperationResponse> splitFileByColumns(
            @RequestParam("file") MultipartFile file,
            @RequestParam("splitDefinitions") String splitDefinitionsJson,
            @RequestParam(value = "recordType", defaultValue = "Employee") String recordType,
            @RequestParam(value = "inputFormat", required = false) String inputFormat,
            @RequestParam(value = "format", defaultValue = "CSV") String outputFormat) {
        try {
            // Auto-detect input format from file extension if not provided
            if (inputFormat == null || inputFormat.isEmpty()) {
                inputFormat = detectFormatFromFilename(file.getOriginalFilename());
            }

            log.info("Column split request: recordType={}, inputFormat={}, outputFormat={}, definitions={}",
                    recordType, inputFormat, outputFormat, splitDefinitionsJson);

            // Save uploaded file
            String savedFilePath = saveUploadedFile(file);

            // Parse split definitions from JSON
            // Format: [{"name": "personal", "columns": ["id", "name"]}, {"name": "salary", "columns": ["id", "department", "salary"]}]
            List<FileOperationRequest.ColumnSplitDefinition> splitDefinitions = parseSplitDefinitions(splitDefinitionsJson);

            // Build request
            FileOperationRequest request = FileOperationRequest.builder()
                    .recordType(recordType)
                    .inputFilePaths(List.of(savedFilePath))
                    .format(inputFormat)
                    .outputFormat(outputFormat)
                    .columnSplitOptions(FileOperationRequest.ColumnSplitOptions.builder()
                            .splitDefinitions(splitDefinitions)
                            .build())
                    .build();

            Class<?> recordTypeClass = getRecordTypeClass(recordType);
            FileOperationResponse response = fileOperationService.splitFileByColumns(request, recordTypeClass);

            // Clean up uploaded file
            cleanupFiles(List.of(savedFilePath));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error splitting file by columns", e);
            FileOperationResponse errorResponse = FileOperationResponse.builder()
                    .success(false)
                    .message("Column split failed: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Parse split definitions JSON string into objects using Jackson.
     */
    private List<FileOperationRequest.ColumnSplitDefinition> parseSplitDefinitions(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<FileOperationRequest.ColumnSplitDefinition> definitions = mapper.readValue(
                json,
                new TypeReference<List<FileOperationRequest.ColumnSplitDefinition>>() {}
        );
        log.info("Parsed {} split definitions: {}", definitions.size(), definitions);
        return definitions;
    }

    /**
     * Convert file format or record type (with file upload support).
     */
    @PostMapping(value = "/convert", consumes = {"multipart/form-data"})
    public ResponseEntity<FileOperationResponse> convertFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("recordType") String recordType,
            @RequestParam(value = "sourceFormat", required = false) String sourceFormat,
            @RequestParam(value = "targetFormat", defaultValue = "JSON") String targetFormat,
            @RequestParam(value = "targetRecordType", required = false) String targetRecordType,
            @RequestParam(value = "outputFilePath", required = false) String outputFilePath) {
        try {
            // Auto-detect source format from file extension if not provided
            if (sourceFormat == null || sourceFormat.isEmpty()) {
                sourceFormat = detectFormatFromFilename(file.getOriginalFilename());
            }

            log.info("Convert request: recordType={}, sourceFormat={}, targetFormat={}",
                    recordType, sourceFormat, targetFormat);

            // Save uploaded file and get its path
            String savedFilePath = saveUploadedFile(file);
            
            // Build request object
            FileOperationRequest request = FileOperationRequest.builder()
                    .recordType(recordType)
                    .inputFilePaths(List.of(savedFilePath))
                    .format(sourceFormat)
                    .outputFilePath(outputFilePath)
                    .convertOptions(FileOperationRequest.ConvertOptions.builder()
                            .targetRecordType(targetRecordType != null ? targetRecordType : recordType)
                            .targetFormat(targetFormat)
                            .build())
                    .build();
            
            Class<?> sourceType = getRecordTypeClass(recordType);
            Class<?> targetType = getRecordTypeClass(
                    targetRecordType != null ? targetRecordType : recordType
            );
            FileOperationResponse response = fileOperationService.convertFile(request, sourceType, targetType);
            
            // Clean up uploaded file after processing
            cleanupFiles(List.of(savedFilePath));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error converting file", e);
            FileOperationResponse errorResponse = FileOperationResponse.builder()
                    .success(false)
                    .message("Convert operation failed: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Get available record types.
     */
    @GetMapping("/record-types")
    public ResponseEntity<Map<String, String>> getRecordTypes() {
        Map<String, String> types = new HashMap<>();
        types.put("Employee", "com.example.demo.model.Employee");
        types.put("SimpleRecord", "com.example.demo.testmodel.SimpleRecord");
        types.put("CompositeKeyRecord", "com.example.demo.testmodel.CompositeKeyRecord");
        return ResponseEntity.ok(types);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "File Operation Service");
        return ResponseEntity.ok(response);
    }

    /**
     * Download a file by its path.
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("path") String filePath) {
        try {
            Path path = Paths.get(filePath);

            // Security check: ensure the file is within the data/output directory
            Path outputDir = Paths.get(System.getProperty("user.dir")).resolve("data/output").normalize();
            if (!path.normalize().startsWith(outputDir)) {
                log.warn("Attempted to download file outside output directory: {}", filePath);
                return ResponseEntity.badRequest().build();
            }

            if (!Files.exists(path)) {
                log.error("File not found for download: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(path.toUri());
            String filename = path.getFileName().toString();
            String contentType = determineContentType(filename);

            // Encode filename for Content-Disposition header
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");

            log.info("Downloading file: {} (size: {} bytes)", filename, Files.size(path));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading file: {}", filePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Determine content type based on file extension.
     */
    private String determineContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".json")) {
            return "application/json";
        } else if (lower.endsWith(".xml")) {
            return "application/xml";
        } else if (lower.endsWith(".csv")) {
            return "text/csv";
        } else if (lower.endsWith(".tsv")) {
            return "text/tab-separated-values";
        }
        return "application/octet-stream";
    }

    /**
     * Helper method to get record type class from string.
     */
    private Class<?> getRecordTypeClass(String recordType) throws ClassNotFoundException {
        if (recordType == null || recordType.isEmpty()) {
            return Employee.class; // Default
        }
        
        // Map simple names to full class names
        switch (recordType) {
            case "Employee":
                return Employee.class;
            case "SimpleRecord":
                return Class.forName("com.example.demo.testmodel.SimpleRecord");
            case "CompositeKeyRecord":
                return Class.forName("com.example.demo.testmodel.CompositeKeyRecord");
            case "AllTypesRecord":
                return Class.forName("com.example.demo.testmodel.AllTypesRecord");
            default:
                // Try to load as full class name
                return Class.forName(recordType);
        }
    }
    
    /**
     * Save uploaded file to temporary directory and return its path.
     */
    private String saveUploadedFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        
        // Create upload directory if it doesn't exist (use absolute path)
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        Path uploadDir = workingDir.resolve("./data/upload").normalize();
        Files.createDirectories(uploadDir);
        
        // Generate unique filename to avoid conflicts
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = "upload_" + System.currentTimeMillis() + "_" + 
                UUID.randomUUID().toString().substring(0, 8) + extension;
        
        Path filePath = uploadDir.resolve(uniqueFilename).normalize();
        
        log.info("Saving file: {} to: {}", originalFilename, filePath);
        log.info("Upload directory exists: {}, writable: {}", 
                Files.exists(uploadDir), Files.isWritable(uploadDir));
        
        // Use transferTo() method which is the recommended way to save MultipartFile
        file.transferTo(filePath.toFile());
        
        // Verify file was written immediately
        if (!Files.exists(filePath)) {
            log.error("File was NOT saved: {}", filePath);
            throw new RuntimeException("File was not saved: " + filePath);
        }
        
        long fileSize = Files.size(filePath);
        if (fileSize == 0) {
            log.error("Saved file is empty: {}", filePath);
            throw new RuntimeException("Saved file is empty: " + filePath);
        }
        
        log.info("Successfully saved uploaded file: {} -> {} (size: {} bytes, exists: {})", 
                originalFilename, filePath, fileSize, Files.exists(filePath));
        
        // Double-check file still exists before returning
        if (!Files.exists(filePath)) {
            log.error("File disappeared immediately after saving: {}", filePath);
            throw new RuntimeException("File disappeared after saving: " + filePath);
        }
        
        return filePath.toString(); // Return absolute path
    }
    
    /**
     * Save multiple uploaded files to temporary directory and return their paths.
     */
    private List<String> saveUploadedFiles(MultipartFile[] files) throws Exception {
        List<String> savedPaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                savedPaths.add(saveUploadedFile(file));
            }
        }
        return savedPaths;
    }
    
    /**
     * Clean up uploaded temporary files.
     */
    private void cleanupFiles(List<String> filePaths) {
        for (String filePath : filePaths) {
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    Files.delete(path);
                    log.debug("Cleaned up temporary file: {}", filePath);
                }
            } catch (Exception e) {
                log.warn("Failed to clean up file: {}", filePath, e);
            }
        }
    }
    
    /**
     * Detect file format from filename extension.
     */
    private String detectFormatFromFilename(String filename) {
        if (filename == null) {
            return "CSV";
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".csv")) {
            return "CSV";
        } else if (lower.endsWith(".json")) {
            return "JSON";
        } else if (lower.endsWith(".xml")) {
            return "XML";
        } else if (lower.endsWith(".tsv")) {
            return "TSV";
        } else if (lower.endsWith(".txt")) {
            // Default to CSV for .txt files
            return "CSV";
        }
        return "CSV"; // Default
    }
}


