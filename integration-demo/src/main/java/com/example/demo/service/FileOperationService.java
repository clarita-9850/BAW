package com.example.demo.service;

import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.repository.FileRepository;
import com.cmips.integration.framework.baw.repository.MergeBuilder;
import com.cmips.integration.framework.baw.repository.MergeResult;
import com.cmips.integration.framework.baw.repository.Schema;
import com.cmips.integration.framework.baw.split.SplitResult;
import com.cmips.integration.framework.baw.split.SplitRule;
import com.cmips.integration.framework.baw.util.ColumnProjectionUtil;
import com.example.demo.dto.FileOperationRequest;
import com.example.demo.dto.FileOperationResponse;
import com.example.demo.dto.SchemaInfoResponse;
import com.example.demo.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service for file operations using the Integration Hub Framework.
 */
@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class FileOperationService {

    /**
     * Merge multiple files with options.
     */
    public FileOperationResponse mergeFiles(FileOperationRequest request, Class<?> recordType) {
        try {
            FileRepository repo = FileRepository.forType(recordType);
            FileFormat format = parseFileFormat(request.getFormat());
            
            // Read all input files
            List<List> allRecords = new ArrayList<>();
            int totalInputRecords = 0;
            
            for (String inputPath : request.getInputFilePaths()) {
                Path path = resolvePath(inputPath);
                log.info("Attempting to read file: {} (absolute path: {}, exists: {})", inputPath, path, Files.exists(path));
                if (!Files.exists(path)) {
                    log.error("File does not exist at resolved path: {} (original: {})", path, inputPath);
                    throw new RuntimeException("File does not exist: " + path);
                }
                log.info("File exists, size: {} bytes. Reading...", Files.size(path));
                List records = repo.read(path, format);
                log.info("Successfully read {} records from {}", records.size(), path);
                allRecords.add(records);
                totalInputRecords += records.size();
            }
            
            // Build merge operation - merge sequentially
            MergeBuilder mergeBuilder;
            if (allRecords.size() == 1) {
                mergeBuilder = repo.merge(allRecords.get(0), Collections.emptyList());
            } else if (allRecords.size() == 2) {
                mergeBuilder = repo.merge(allRecords.get(0), allRecords.get(1));
            } else {
                // Merge first two, then merge result with remaining lists
                mergeBuilder = repo.merge(allRecords.get(0), allRecords.get(1));
                for (int i = 2; i < allRecords.size(); i++) {
                    List currentMerged = mergeBuilder.build();
                    mergeBuilder = repo.merge(currentMerged, allRecords.get(i));
                }
            }
            
            // Apply merge options
            FileOperationRequest.MergeOptions mergeOpts = request.getMergeOptions();
            if (mergeOpts != null) {
                // Sorting
                if (mergeOpts.getSortField() != null) {
                    Function keyExtractor = getKeyExtractor(recordType, mergeOpts.getSortField());
                    mergeBuilder = mergeBuilder.sortBy(keyExtractor);
                    if ("DESC".equalsIgnoreCase(mergeOpts.getSortOrder())) {
                        mergeBuilder = mergeBuilder.descending();
                    } else {
                        mergeBuilder = mergeBuilder.ascending();
                    }
                }
                
                // Deduplication
                if (mergeOpts.isDeduplicate()) {
                    if (mergeOpts.isKeepFirst()) {
                        mergeBuilder = mergeBuilder.deduplicate();
                    } else {
                        mergeBuilder = mergeBuilder.deduplicateKeepLast();
                    }
                }
                
                // Filter (simple field=value for now)
                if (mergeOpts.getFilterExpressions() != null && !mergeOpts.getFilterExpressions().isEmpty()) {
                    for (String filterExpr : mergeOpts.getFilterExpressions()) {
                        Predicate predicate = parseFilterExpression(recordType, filterExpr);
                        mergeBuilder = mergeBuilder.filter(predicate);
                    }
                }
                
                // Limit
                if (mergeOpts.getLimit() != null && mergeOpts.getLimit() > 0) {
                    mergeBuilder = mergeBuilder.limit(mergeOpts.getLimit());
                }
            }
            
            // Execute merge
            MergeResult result = mergeBuilder.buildWithStats();
            List mergedRecords = result.getRecords();
            
            // Write output - use outputFormat if available, otherwise use input format
            String outputFormat = request.getOutputFormat() != null ? request.getOutputFormat() : request.getFormat();
            String outputPath = request.getOutputFilePath();
            if (outputPath == null) {
                outputPath = generateOutputPath("merged", outputFormat);
            }
            writeFileRaw(mergedRecords, outputPath, outputFormat, recordType);
            
            // Build response
            FileOperationResponse.OperationStats stats = FileOperationResponse.OperationStats.builder()
                    .inputRecordCount(totalInputRecords)
                    .outputRecordCount(mergedRecords.size())
                    .duplicatesRemoved(result.getDuplicatesRemoved())
                    .filteredOut(result.getFilteredOut())
                    .build();
            
            return FileOperationResponse.builder()
                    .success(true)
                    .message("Files merged successfully")
                    .outputFilePath(outputPath)
                    .stats(stats)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error merging files", e);
            return FileOperationResponse.builder()
                    .success(false)
                    .message("Merge failed: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
        }
    }

    /**
     * Split file with options.
     */
    public FileOperationResponse splitFile(FileOperationRequest request, Class<?> recordType) {
        try {
            FileRepository repo = FileRepository.forType(recordType);
            FileFormat format = parseFileFormat(request.getFormat());
            
            // Read input file
            Path inputPath = resolvePath(request.getInputFilePaths().get(0));
            List records = repo.read(inputPath, format);
            
            // Build split rule
            SplitRule splitRule = null;
            FileOperationRequest.SplitOptions splitOpts = request.getSplitOptions();
            
            if (splitOpts != null) {
                switch (splitOpts.getSplitType().toUpperCase()) {
                    case "FIELD":
                        Function fieldExtractor = getKeyExtractor(recordType, splitOpts.getSplitField());
                        splitRule = SplitRule.byField(fieldExtractor);
                        break;
                    case "COUNT":
                        splitRule = SplitRule.byCount(splitOpts.getSplitCount());
                        break;
                    case "PREDICATE":
                        throw new UnsupportedOperationException("Predicate splitting not yet implemented via API");
                    default:
                        throw new IllegalArgumentException("Unknown split type: " + splitOpts.getSplitType());
                }
            } else {
                throw new IllegalArgumentException("Split options are required");
            }
            
            // Execute split
            SplitResult splitResult = repo.split(records, splitRule);
            
            // Write partitions - use outputFormat if available, otherwise use input format
            String outputFormat = request.getOutputFormat() != null ? request.getOutputFormat() : request.getFormat();
            String baseOutputPath = request.getOutputFilePath();
            if (baseOutputPath == null) {
                baseOutputPath = generateOutputPath("split", outputFormat);
            }

            Path resolvedBasePath = resolvePath(baseOutputPath);
            Path basePath = resolvedBasePath.getParent();
            String baseFileName = resolvedBasePath.getFileName().toString();
            String nameWithoutExt = baseFileName.contains(".") ?
                    baseFileName.substring(0, baseFileName.lastIndexOf('.')) : baseFileName;
            String ext = baseFileName.contains(".") ?
                    baseFileName.substring(baseFileName.lastIndexOf('.')) : "." + outputFormat.toLowerCase();

            Map<String, Integer> partitionCounts = new HashMap<>();
            Map<String, String> partitionFilePaths = new HashMap<>();
            Set<String> partitionKeys = splitResult.getPartitionKeys();
            for (Object keyObj : partitionKeys) {
                String key = keyObj.toString();
                String partitionPath = basePath.resolve(nameWithoutExt + "_" + key + ext).toString();
                List partitionRecords = splitResult.get(key);
                writeFileRaw(partitionRecords, partitionPath, outputFormat, recordType);
                partitionCounts.put(key, partitionRecords.size());
                partitionFilePaths.put(key, partitionPath);
            }

            // Build response
            FileOperationResponse.OperationStats stats = FileOperationResponse.OperationStats.builder()
                    .inputRecordCount(records.size())
                    .outputRecordCount(records.size())
                    .partitionsCreated(splitResult.getPartitionCount())
                    .partitionCounts(partitionCounts)
                    .partitionFilePaths(partitionFilePaths)
                    .build();
            
            return FileOperationResponse.builder()
                    .success(true)
                    .message("File split successfully into " + splitResult.getPartitionCount() + " partitions")
                    .outputFilePath(basePath.toString())
                    .stats(stats)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error splitting file", e);
            return FileOperationResponse.builder()
                    .success(false)
                    .message("Split failed: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
        }
    }

    /**
     * Split file by columns - creates multiple output files with different column subsets.
     * Each split definition specifies which columns to include in that output file.
     * Uses the framework's ColumnProjectionUtil for proper column projection.
     */
    @SuppressWarnings("unchecked")
    public FileOperationResponse splitFileByColumns(FileOperationRequest request, Class<?> recordType) {
        try {
            FileOperationRequest.ColumnSplitOptions splitOpts = request.getColumnSplitOptions();
            if (splitOpts == null || splitOpts.getSplitDefinitions() == null || splitOpts.getSplitDefinitions().isEmpty()) {
                throw new IllegalArgumentException("Column split definitions are required");
            }

            // Create repository and read input file using framework
            FileRepository repo = FileRepository.forType(recordType);
            FileFormat inputFormat = parseFileFormat(request.getFormat());
            Path inputPath = resolvePath(request.getInputFilePaths().get(0));
            
            log.info("Reading file with framework: path={}, format={}, recordType={}", 
                    inputPath, request.getFormat(), recordType.getSimpleName());
            
            List records = repo.read(inputPath, inputFormat);
            
            if (records.isEmpty()) {
                throw new IllegalArgumentException("Input file is empty or has no data rows");
            }
            
            log.info("Read {} records from input file", records.size());
            
            // Get schema to validate column names
            Schema schema = repo.getSchema();
            log.info("Schema columns: {}", schema.getColumns().stream()
                    .map(Schema.ColumnSchema::getName)
                    .collect(Collectors.toList()));

            // Process each split definition using ColumnProjectionUtil
            String outputFormat = request.getOutputFormat() != null ? request.getOutputFormat() : "CSV";
            FileFormat outputFileFormat = parseFileFormat(outputFormat);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            Map<String, Integer> partitionCounts = new HashMap<>();
            Map<String, String> partitionFilePaths = new HashMap<>();
            List<String> partitionNames = new ArrayList<>();

            for (FileOperationRequest.ColumnSplitDefinition splitDef : splitOpts.getSplitDefinitions()) {
                String partitionName = splitDef.getName() != null ? splitDef.getName() :
                        "part_" + (partitionNames.size() + 1);
                List<String> columnsToInclude = splitDef.getColumns();

                if (columnsToInclude == null || columnsToInclude.isEmpty()) {
                    log.warn("Skipping split definition with no columns: {}", partitionName);
                    continue;
                }

                // Validate columns exist in schema (framework utility will also validate, but better error message here)
                List<String> validColumns = new ArrayList<>();
                for (String col : columnsToInclude) {
                    Schema.ColumnSchema colSchema = schema.getColumn(col.trim());
                    if (colSchema != null) {
                        validColumns.add(colSchema.getName()); // Use canonical name from schema
                    } else {
                        log.warn("Column '{}' not found in schema for type {}. Available columns: {}", 
                                col, recordType.getSimpleName(),
                                schema.getColumns().stream()
                                    .map(Schema.ColumnSchema::getName)
                                    .collect(Collectors.joining(", ")));
                    }
                }

                if (validColumns.isEmpty()) {
                    log.warn("No valid columns found for split: {}", partitionName);
                    continue;
                }

                // Generate output file path
                String ext = getExtensionForFormat(outputFormat);
                String outputFileName = "split_" + timestamp + "_" + partitionName + "." + ext;
                Path outputDir = resolvePath("./data/output");
                Files.createDirectories(outputDir);
                Path outputPath = outputDir.resolve(outputFileName);

                // Use framework's ColumnProjectionUtil to write with selected columns
                log.info("Writing partition '{}' with columns {} to {}", partitionName, validColumns, outputPath);
                ColumnProjectionUtil.writeWithColumns(repo, records, outputPath, outputFileFormat, validColumns);

                partitionNames.add(partitionName);
                partitionCounts.put(partitionName, records.size());
                partitionFilePaths.put(partitionName, outputPath.toString());

                log.info("Created partition '{}' with columns {} -> {}",
                        partitionName, validColumns, outputPath);
            }

            if (partitionNames.isEmpty()) {
                throw new IllegalArgumentException("No valid partitions could be created. Please check column names match the schema.");
            }

            // Build response
            FileOperationResponse.OperationStats stats = FileOperationResponse.OperationStats.builder()
                    .inputRecordCount(records.size())
                    .outputRecordCount(records.size())
                    .partitionsCreated(partitionNames.size())
                    .partitionCounts(partitionCounts)
                    .partitionFilePaths(partitionFilePaths)
                    .build();

            return FileOperationResponse.builder()
                    .success(true)
                    .message("File split by columns into " + partitionNames.size() + " partitions: " +
                            String.join(", ", partitionNames))
                    .outputFilePath(resolvePath("./data/output").toString())
                    .stats(stats)
                    .build();

        } catch (Exception e) {
            log.error("Error splitting file by columns", e);
            return FileOperationResponse.builder()
                    .success(false)
                    .message("Column split failed: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get file extension for output format.
     */
    private String getExtensionForFormat(String format) {
        if (format == null) return "csv";
        switch (format.toUpperCase()) {
            case "JSON": return "json";
            case "XML": return "xml";
            default: return "csv";
        }
    }

    /**
     * Convert file format.
     */
    public FileOperationResponse convertFile(FileOperationRequest request, Class<?> sourceType, Class<?> targetType) {
        try {
            FileRepository sourceRepo = FileRepository.forType(sourceType);
            FileFormat sourceFormat = parseFileFormat(request.getFormat());
            
            // Read source file
            Path inputPath = resolvePath(request.getInputFilePaths().get(0));
            List sourceRecords = sourceRepo.read(inputPath, sourceFormat);
            
            // Convert to target type
            List targetRecords = sourceRepo.convert(sourceRecords, targetType);
            
            // Write output
            FileOperationRequest.ConvertOptions convertOpts = request.getConvertOptions();
            String outputFormat = convertOpts != null && convertOpts.getTargetFormat() != null ?
                    convertOpts.getTargetFormat() : request.getFormat();
            String outputPath = request.getOutputFilePath();
            if (outputPath == null) {
                outputPath = generateOutputPath("converted", outputFormat);
            }
            
            FileRepository targetRepo = FileRepository.forType(targetType);
            Path path = resolvePath(outputPath);
            FileFormat fileFormat = parseFileFormat(outputFormat);
            targetRepo.write(targetRecords, path, fileFormat);
            
            // Build response
            FileOperationResponse.OperationStats stats = FileOperationResponse.OperationStats.builder()
                    .inputRecordCount(sourceRecords.size())
                    .outputRecordCount(targetRecords.size())
                    .build();
            
            return FileOperationResponse.builder()
                    .success(true)
                    .message("File converted successfully")
                    .outputFilePath(outputPath)
                    .stats(stats)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error converting file", e);
            return FileOperationResponse.builder()
                    .success(false)
                    .message("Conversion failed: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
        }
    }

    /**
     * Get schema information for a record type.
     */
    public SchemaInfoResponse getSchemaInfo(Class<?> recordType) {
        try {
            FileRepository repo = FileRepository.forType(recordType);
            Schema schema = repo.getSchema();
            
            List<SchemaInfoResponse.ColumnInfo> columns = schema.getColumns().stream()
                    .map(col -> {
                        try {
                            Field field = recordType.getDeclaredField(col.getFieldName());
                            return SchemaInfoResponse.ColumnInfo.builder()
                                    .name(col.getName())
                                    .javaType(field.getType().getSimpleName())
                                    .order(col.getOrder())
                                    .nullable(col.isNullable())
                                    .format(col.getFormat())
                                    .length(col.getLength())
                                    .alignment(null)
                                    .build();
                        } catch (NoSuchFieldException e) {
                            return SchemaInfoResponse.ColumnInfo.builder()
                                    .name(col.getName())
                                    .javaType(col.getJavaType() != null ? col.getJavaType().getSimpleName() : "Unknown")
                                    .order(col.getOrder())
                                    .nullable(col.isNullable())
                                    .format(col.getFormat())
                                    .length(col.getLength())
                                    .alignment(null)
                                    .build();
                        }
                    })
                    .collect(Collectors.toList());
            
            List<String> identityFields = schema.getIdColumns().stream()
                    .map(Schema.ColumnSchema::getName)
                    .collect(Collectors.toList());
            
            return SchemaInfoResponse.builder()
                    .typeName(schema.getName())
                    .description(schema.getDescription())
                    .version(schema.getVersion())
                    .columns(columns)
                    .identityFields(identityFields)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting schema info", e);
            throw new RuntimeException("Failed to get schema info: " + e.getMessage(), e);
        }
    }

    // Helper methods
    
    private FileFormat parseFileFormat(String format) {
        if (format == null) {
            return FileFormat.csv().build();
        }
        
        switch (format.toUpperCase()) {
            case "CSV":
                return FileFormat.csv().build();
            case "JSON":
                return FileFormat.json().prettyPrint(true).build();
            case "XML":
                return FileFormat.xml().build();
            case "TSV":
                return FileFormat.tsv().build();
            case "PIPE":
                return FileFormat.pipe().build();
            default:
                return FileFormat.csv().build();
        }
    }
    
    /**
     * Resolve file path - handles both absolute and relative paths.
     * Relative paths are resolved relative to the application working directory.
     */
    private Path resolvePath(String filePath) {
        Path path = Paths.get(filePath);
        // If it's already absolute, return as-is
        if (path.isAbsolute()) {
            return path;
        }
        // For relative paths, resolve from the application's working directory
        // This ensures paths like "./data/input/file.csv" work correctly
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        return workingDir.resolve(path).normalize();
    }
    
    private String generateOutputPath(String prefix, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String ext = format != null ? format.toLowerCase() : "csv";
        String relativePath = "./data/output/" + prefix + "_" + timestamp + "." + ext;
        // Resolve to absolute path for output
        Path resolved = resolvePath(relativePath);
        return resolved.toString();
    }
    
    private Function getKeyExtractor(Class<?> recordType, String fieldName) {
        try {
            Field field = recordType.getDeclaredField(fieldName);
            field.setAccessible(true);
            return obj -> {
                try {
                    return field.get(obj);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Field not found: " + fieldName, e);
        }
    }
    
    private Predicate parseFilterExpression(Class<?> recordType, String filterExpr) {
        // Simple parsing: field=value
        String[] parts = filterExpr.split("=", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid filter expression: " + filterExpr);
        }
        
        String fieldName = parts[0].trim();
        String value = parts[1].trim();
        
        try {
            Field field = recordType.getDeclaredField(fieldName);
            field.setAccessible(true);
            return obj -> {
                try {
                    Object fieldValue = field.get(obj);
                    if (fieldValue == null) {
                        return "null".equals(value);
                    }
                    return fieldValue.toString().equals(value);
                } catch (IllegalAccessException e) {
                    return false;
                }
            };
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Field not found: " + fieldName, e);
        }
    }
    
    private void writeFileRaw(List records, String outputPath, String format, Class<?> recordType) {
        FileRepository repo = FileRepository.forType(recordType);
        Path path = resolvePath(outputPath);
        FileFormat fileFormat = parseFileFormat(format);
        repo.write(records, path, fileFormat);
    }
}
