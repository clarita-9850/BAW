package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for schema information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaInfoResponse {
    
    private String typeName;
    private String description;
    private String version;
    private List<ColumnInfo> columns;
    private List<String> identityFields;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnInfo {
        private String name;
        private String javaType;
        private Integer order;
        private boolean nullable;
        private String format;
        private Integer length;
        private String alignment;
    }
}



