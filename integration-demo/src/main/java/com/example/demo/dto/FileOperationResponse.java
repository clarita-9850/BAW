package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for file operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileOperationResponse {
    
    private boolean success;
    private String message;
    private String outputFilePath;
    private OperationStats stats;
    private List<String> errors;
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationStats {
        private Integer inputRecordCount;
        private Integer outputRecordCount;
        private Integer duplicatesRemoved;
        private Integer filteredOut;
        private Integer partitionsCreated;
        private Map<String, Integer> partitionCounts;
        private Map<String, String> partitionFilePaths; // Maps partition key to file path
    }
}



