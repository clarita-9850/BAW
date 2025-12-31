package com.cmips.integration.framework.baw.exception;

import java.nio.file.Path;

/**
 * Exception thrown when file parsing fails.
 *
 * <p>Includes context information such as:
 * <ul>
 *   <li>File path being parsed</li>
 *   <li>Line number where error occurred</li>
 *   <li>Content of the problematic line</li>
 *   <li>Column index if applicable</li>
 * </ul>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class FileParseException extends BawException {

    private final Path filePath;
    private final Integer lineNumber;
    private final String lineContent;
    private final Integer columnIndex;
    private final String fieldName;

    public FileParseException(String message) {
        super(message);
        this.filePath = null;
        this.lineNumber = null;
        this.lineContent = null;
        this.columnIndex = null;
        this.fieldName = null;
    }

    public FileParseException(String message, Throwable cause) {
        super(message, cause);
        this.filePath = null;
        this.lineNumber = null;
        this.lineContent = null;
        this.columnIndex = null;
        this.fieldName = null;
    }

    private FileParseException(Builder builder) {
        super(builder.buildMessage(), builder.cause);
        this.filePath = builder.filePath;
        this.lineNumber = builder.lineNumber;
        this.lineContent = builder.lineContent;
        this.columnIndex = builder.columnIndex;
        this.fieldName = builder.fieldName;
    }

    public Path getFilePath() {
        return filePath;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getLineContent() {
        return lineContent;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String message;
        private Throwable cause;
        private Path filePath;
        private Integer lineNumber;
        private String lineContent;
        private Integer columnIndex;
        private String fieldName;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder filePath(Path filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder lineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        public Builder lineContent(String lineContent) {
            this.lineContent = lineContent;
            return this;
        }

        public Builder columnIndex(int columnIndex) {
            this.columnIndex = columnIndex;
            return this;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        private String buildMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append("Parse error");

            if (filePath != null) {
                sb.append(" in ").append(filePath.getFileName());
            }
            if (lineNumber != null) {
                sb.append(" at line ").append(lineNumber);
            }
            if (columnIndex != null) {
                sb.append(", column ").append(columnIndex);
            }
            if (fieldName != null) {
                sb.append(" (field: ").append(fieldName).append(")");
            }
            if (message != null) {
                sb.append(": ").append(message);
            }
            if (lineContent != null) {
                sb.append("\n  Line content: ").append(truncate(lineContent, 100));
            }

            return sb.toString();
        }

        private String truncate(String s, int maxLen) {
            if (s == null) return null;
            if (s.length() <= maxLen) return s;
            return s.substring(0, maxLen) + "...";
        }

        public FileParseException build() {
            return new FileParseException(this);
        }
    }
}
