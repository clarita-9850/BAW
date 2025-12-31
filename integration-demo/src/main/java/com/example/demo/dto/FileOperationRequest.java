package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for file operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileOperationRequest {
    
    private List<String> inputFilePaths;
    private String outputFilePath;
    private String format; // CSV, JSON, XML, TSV, PIPE, FIXED_WIDTH (input format)
    private String outputFormat; // CSV, JSON, XML, TSV, PIPE, FIXED_WIDTH (output format, optional)
    private String recordType; // Employee, SimpleRecord, etc.
    
    // Merge options
    private MergeOptions mergeOptions;

    // Split options (record-based)
    private SplitOptions splitOptions;

    // Column split options (column-based)
    private ColumnSplitOptions columnSplitOptions;

    // Convert options
    private ConvertOptions convertOptions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MergeOptions {
        private boolean deduplicate;
        private boolean keepFirst; // true = keep first, false = keep last
        private String sortField;
        private String sortOrder; // ASC, DESC
        private List<String> filterExpressions; // Simple field=value filters
        private Integer limit;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitOptions {
        private String splitType; // FIELD, COUNT, PREDICATE
        private String splitField; // For FIELD type
        private Integer splitCount; // For COUNT type
        private String predicate; // For PREDICATE type
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConvertOptions {
        private String targetRecordType;
        private String targetFormat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnSplitOptions {
        private List<ColumnSplitDefinition> splitDefinitions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnSplitDefinition {
        private String name;           // Name for this partition (e.g., "personal_info", "salary_data")
        private List<String> columns;  // List of column names to include
    }
}


