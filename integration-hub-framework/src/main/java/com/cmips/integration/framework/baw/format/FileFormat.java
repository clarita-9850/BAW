package com.cmips.integration.framework.baw.format;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Represents a file format for reading and writing records.
 *
 * <p>Use the static factory methods to create format configurations:
 * <pre>
 * // CSV with defaults
 * FileFormat csv = FileFormat.csv();
 *
 * // CSV with options
 * FileFormat csv = FileFormat.csv()
 *     .delimiter(';')
 *     .quoteChar('"')
 *     .escapeChar('\\')
 *     .hasHeader(true);
 *
 * // Fixed-width
 * FileFormat fixed = FileFormat.fixedWidth();
 *
 * // XML
 * FileFormat xml = FileFormat.xml()
 *     .rootElement("payments")
 *     .recordElement("payment");
 *
 * // JSON
 * FileFormat json = FileFormat.json()
 *     .prettyPrint(true);
 *
 * // JSON Lines
 * FileFormat jsonl = FileFormat.jsonLines();
 * </pre>
 *
 * @author CMIPS Integration Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class FileFormat {

    private final FormatType type;
    private final Charset charset;
    private final String lineSeparator;
    private final String nullValue;

    // CSV/Delimited options
    private final char delimiter;
    private final char quoteChar;
    private final char escapeChar;
    private final boolean hasHeader;

    // XML options
    private final String rootElement;
    private final String recordElement;
    private final boolean xmlDeclaration;
    private final boolean prettyPrint;

    // JSON options
    private final boolean jsonArray;

    private FileFormat(Builder builder) {
        this.type = builder.type;
        this.charset = builder.charset;
        this.lineSeparator = builder.lineSeparator;
        this.nullValue = builder.nullValue;
        this.delimiter = builder.delimiter;
        this.quoteChar = builder.quoteChar;
        this.escapeChar = builder.escapeChar;
        this.hasHeader = builder.hasHeader;
        this.rootElement = builder.rootElement;
        this.recordElement = builder.recordElement;
        this.xmlDeclaration = builder.xmlDeclaration;
        this.prettyPrint = builder.prettyPrint;
        this.jsonArray = builder.jsonArray;
    }

    // Static factory methods

    public static Builder csv() {
        return new Builder(FormatType.CSV).delimiter(',');
    }

    public static Builder tsv() {
        return new Builder(FormatType.TSV).delimiter('\t');
    }

    public static Builder pipe() {
        return new Builder(FormatType.PIPE).delimiter('|');
    }

    public static Builder fixedWidth() {
        return new Builder(FormatType.FIXED_WIDTH);
    }

    public static Builder xml() {
        return new Builder(FormatType.XML);
    }

    public static Builder json() {
        return new Builder(FormatType.JSON).jsonArray(true);
    }

    public static Builder jsonLines() {
        return new Builder(FormatType.JSON_LINES).jsonArray(false);
    }

    // Getters

    public FormatType getType() {
        return type;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public String getNullValue() {
        return nullValue;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    public String getRootElement() {
        return rootElement;
    }

    public String getRecordElement() {
        return recordElement;
    }

    public boolean includeXmlDeclaration() {
        return xmlDeclaration;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public boolean isJsonArray() {
        return jsonArray;
    }

    public boolean isDelimited() {
        return type == FormatType.CSV || type == FormatType.TSV || type == FormatType.PIPE;
    }

    /**
     * Format type enumeration.
     */
    public enum FormatType {
        CSV, TSV, PIPE, FIXED_WIDTH, XML, JSON, JSON_LINES
    }

    /**
     * Builder for FileFormat.
     */
    public static class Builder {
        private final FormatType type;
        private Charset charset = StandardCharsets.UTF_8;
        private String lineSeparator = System.lineSeparator();
        private String nullValue = "";
        private char delimiter = ',';
        private char quoteChar = '"';
        private char escapeChar = '"';
        private boolean hasHeader = true;
        private String rootElement = "records";
        private String recordElement = "record";
        private boolean xmlDeclaration = true;
        private boolean prettyPrint = false;
        private boolean jsonArray = true;

        private Builder(FormatType type) {
            this.type = type;
        }

        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder lineSeparator(String lineSeparator) {
            this.lineSeparator = lineSeparator;
            return this;
        }

        public Builder nullValue(String nullValue) {
            this.nullValue = nullValue;
            return this;
        }

        public Builder delimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder quoteChar(char quoteChar) {
            this.quoteChar = quoteChar;
            return this;
        }

        public Builder escapeChar(char escapeChar) {
            this.escapeChar = escapeChar;
            return this;
        }

        public Builder hasHeader(boolean hasHeader) {
            this.hasHeader = hasHeader;
            return this;
        }

        public Builder rootElement(String rootElement) {
            this.rootElement = rootElement;
            return this;
        }

        public Builder recordElement(String recordElement) {
            this.recordElement = recordElement;
            return this;
        }

        public Builder xmlDeclaration(boolean xmlDeclaration) {
            this.xmlDeclaration = xmlDeclaration;
            return this;
        }

        public Builder prettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        public Builder jsonArray(boolean jsonArray) {
            this.jsonArray = jsonArray;
            return this;
        }

        public FileFormat build() {
            return new FileFormat(this);
        }
    }
}
