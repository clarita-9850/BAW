# Integration Hub Framework - API Documentation

**Version:** 1.0.0
**Package:** `com.cmips.integration.framework.baw`
**License:** Proprietary

---

## Table of Contents

1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Annotations](#annotations)
   - [@FileType](#filetype)
   - [@FileColumn](#filecolumn)
   - [@FileId](#fileid)
   - [@MapsFrom](#mapsfrom)
   - [@Validate](#validate)
4. [Core Interfaces](#core-interfaces)
   - [FileRepository](#filerepository)
   - [MergeBuilder](#mergebuilder)
   - [SplitRule & SplitResult](#splitrule--splitresult)
   - [SendBuilder](#sendbuilder)
5. [File Formats](#file-formats)
6. [Validation](#validation)
7. [Destinations](#destinations)
8. [Exceptions](#exceptions)
9. [Examples](#examples)

---

## Overview

The Integration Hub Framework (BAW - Batch And Workflow) is a JPA-inspired declarative file processing library for Java. It provides:

- **Annotation-based schema definition** - Define file types using familiar annotations
- **Type-safe file operations** - Read, write, merge, split, and convert files
- **Fluent API** - Chain operations with an intuitive builder pattern
- **Multi-format support** - CSV, JSON, XML, TSV, Fixed-Width, and more
- **Validation framework** - Declarative field and record validation
- **Destination management** - Send files via SFTP or HTTP API

---

## Quick Start

### Define a File Type

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "employee", description = "Employee records", version = "1.0")
public class Employee {

    @FileId
    @FileColumn(order = 1, name = "id", nullable = false)
    private Long id;

    @FileColumn(order = 2, name = "name", nullable = false)
    private String name;

    @FileColumn(order = 3, name = "salary", format = "#,##0.00")
    private BigDecimal salary;

    @FileColumn(order = 4, name = "hireDate", format = "yyyy-MM-dd")
    private LocalDate hireDate;
}
```

### Basic Operations

```java
// Create repository
FileRepository<Employee> repo = FileRepository.forType(Employee.class);

// Read from file
List<Employee> employees = repo.read(
    Path.of("employees.csv"),
    FileFormat.csv().build()
);

// Write to file
repo.write(employees, Path.of("output.json"), FileFormat.json().build());

// Merge and deduplicate
List<Employee> merged = repo.merge(list1, list2)
    .sortBy(Employee::getId)
    .ascending()
    .deduplicate()
    .build();

// Split by field
SplitResult<Employee> byDept = repo.split(
    employees,
    SplitRule.byField(Employee::getDepartment)
);
```

---

## Annotations

### @FileType

Marks a class as a File Type definition (similar to JPA's `@Entity`).

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileType {
    String name();                    // Required: Unique identifier
    String description() default "";  // Optional: Human-readable description
    String version() default "1.0";   // Optional: Schema version
}
```

**Example:**
```java
@FileType(
    name = "invoice",
    description = "Invoice records for billing",
    version = "2.0"
)
public class Invoice { ... }
```

---

### @FileColumn

Marks a field as a column in the file type definition.

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileColumn {
    String name() default "";                      // Column name (defaults to field name)
    int order();                                   // Position order (1-based), REQUIRED
    int length() default -1;                       // Length for fixed-width formats
    char padChar() default ' ';                    // Padding character for fixed-width
    Alignment alignment() default Alignment.LEFT;  // LEFT, RIGHT, CENTER
    boolean nullable() default true;               // Whether null values are allowed
    String format() default "";                    // Format pattern for dates/numbers
    String defaultValue() default "";              // Default value when null
    boolean xmlAttribute() default false;          // Treat as XML attribute
    String xpath() default "";                     // XPath for nested XML elements

    enum Alignment { LEFT, RIGHT, CENTER }
}
```

**Example - Basic:**
```java
@FileColumn(order = 1, name = "employee_id", nullable = false)
private Long id;

@FileColumn(order = 2, name = "full_name")
private String name;
```

**Example - Formatted:**
```java
@FileColumn(order = 3, name = "amount", format = "#,##0.00")
private BigDecimal amount;

@FileColumn(order = 4, name = "transaction_date", format = "yyyy-MM-dd")
private LocalDate date;
```

**Example - Fixed-Width:**
```java
@FileColumn(order = 1, name = "id", length = 10, alignment = Alignment.RIGHT, padChar = '0')
private Long id;  // Output: "0000000123"

@FileColumn(order = 2, name = "name", length = 30, alignment = Alignment.LEFT, padChar = ' ')
private String name;  // Output: "John Smith                    "
```

---

### @FileId

Marks a field as an identity field for deduplication. Supports composite keys.

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileId {
    int order() default 0;  // Order in composite key (0-based)
}
```

**Example - Single ID:**
```java
@FileId
@FileColumn(order = 1, name = "id")
private Long id;
```

**Example - Composite Key:**
```java
@FileId(order = 0)
@FileColumn(order = 1, name = "region")
private String regionCode;

@FileId(order = 1)
@FileColumn(order = 2, name = "account")
private String accountNumber;

@FileId(order = 2)
@FileColumn(order = 3, name = "date")
private LocalDate transactionDate;
```

---

### @MapsFrom

Defines field mapping from another File Type for conversion operations.

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MapsFrom.List.class)
public @interface MapsFrom {
    Class<?> source();                                    // Source File Type class
    String field();                                       // Field name in source
    Class<? extends FieldTransformer<?, ?>> transformer()
        default NoOpTransformer.class;                   // Value transformer
}
```

**FieldTransformer Interface:**
```java
@FunctionalInterface
public interface FieldTransformer<S, T> {
    T transform(S source);
}
```

**Example:**
```java
// Source type
@FileType(name = "employee-source")
public class EmployeeSource {
    @FileColumn(order = 1) private String firstName;
    @FileColumn(order = 2) private String lastName;
    @FileColumn(order = 3) private BigDecimal monthlySalary;
    @FileColumn(order = 4) private Boolean isActive;
}

// Target type with transformations
@FileType(name = "employee-target")
public class EmployeeTarget {

    @FileColumn(order = 1)
    @MapsFrom(source = EmployeeSource.class, field = "firstName",
              transformer = FullNameTransformer.class)
    private String fullName;

    @FileColumn(order = 2)
    @MapsFrom(source = EmployeeSource.class, field = "monthlySalary",
              transformer = AnnualSalaryTransformer.class)
    private BigDecimal annualSalary;

    @FileColumn(order = 3)
    @MapsFrom(source = EmployeeSource.class, field = "isActive",
              transformer = StatusTransformer.class)
    private String status;

    // Transformers
    public static class AnnualSalaryTransformer
            implements FieldTransformer<BigDecimal, BigDecimal> {
        @Override
        public BigDecimal transform(BigDecimal monthly) {
            return monthly != null ? monthly.multiply(new BigDecimal("12")) : null;
        }
    }

    public static class StatusTransformer
            implements FieldTransformer<Boolean, String> {
        @Override
        public String transform(Boolean active) {
            if (active == null) return "UNKNOWN";
            return active ? "ACTIVE" : "INACTIVE";
        }
    }
}

// Usage
FileRepository<EmployeeSource> sourceRepo = FileRepository.forType(EmployeeSource.class);
List<EmployeeSource> sources = sourceRepo.read(path, FileFormat.csv().build());

List<EmployeeTarget> targets = sourceRepo.convert(sources, EmployeeTarget.class);
```

---

### @Validate

Defines validation constraints for fields or records.

```java
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Validate.List.class)
public @interface Validate {
    String message() default "";                         // Error message
    boolean notNull() default false;                     // Must not be null
    boolean notBlank() default false;                    // Must not be blank/empty
    boolean notEmpty() default false;                    // Collection must not be empty
    double min() default Double.MIN_VALUE;               // Minimum numeric value
    double max() default Double.MAX_VALUE;               // Maximum numeric value
    int minLength() default 0;                           // Minimum string length
    int maxLength() default Integer.MAX_VALUE;           // Maximum string length
    String pattern() default "";                         // Regex pattern
    String[] allowedValues() default {};                 // Allowed values list
    Class<? extends RecordValidator<?>> validator()
        default NoOpValidator.class;                     // Custom validator
}
```

**RecordValidator Interface:**
```java
@FunctionalInterface
public interface RecordValidator<T> {
    ValidationError validate(T record);  // Returns null if valid
}
```

**Example - Field Validation:**
```java
@FileType(name = "order")
public class Order {

    @FileColumn(order = 1)
    @Validate(notNull = true, message = "Order ID is required")
    private Long orderId;

    @FileColumn(order = 2)
    @Validate(notBlank = true, minLength = 2, maxLength = 100)
    private String customerName;

    @FileColumn(order = 3)
    @Validate(pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
              message = "Invalid email format")
    private String email;

    @FileColumn(order = 4)
    @Validate(min = 0.01, max = 999999.99, message = "Amount must be between 0.01 and 999999.99")
    private BigDecimal amount;

    @FileColumn(order = 5)
    @Validate(allowedValues = {"PENDING", "APPROVED", "REJECTED", "SHIPPED"})
    private String status;
}
```

**Example - Record-Level Validation:**
```java
@FileType(name = "date-range")
@Validate(validator = DateRangeValidator.class,
          message = "End date must be after start date")
public class DateRange {

    @FileColumn(order = 1)
    private LocalDate startDate;

    @FileColumn(order = 2)
    private LocalDate endDate;

    public static class DateRangeValidator implements RecordValidator<DateRange> {
        @Override
        public ValidationError validate(DateRange record) {
            if (record.getStartDate() != null && record.getEndDate() != null) {
                if (record.getEndDate().isBefore(record.getStartDate())) {
                    return new ValidationError("End date must be after start date");
                }
            }
            return null;  // Valid
        }
    }
}
```

---

## Core Interfaces

### FileRepository

The main interface for all file operations. Use the factory method to create instances.

```java
public interface FileRepository<T> {

    // ==================== Factory Method ====================

    static <T> FileRepository<T> forType(Class<T> type);

    // ==================== Read Operations ====================

    /**
     * Read records from a file path.
     */
    List<T> read(Path path, FileFormat format) throws FileParseException;

    /**
     * Read records from an input stream.
     */
    List<T> read(InputStream stream, FileFormat format) throws FileParseException;

    /**
     * Read records from multiple files.
     */
    List<T> readAll(List<Path> paths, FileFormat format) throws FileParseException;

    /**
     * Read records as a lazy stream (for large files).
     */
    Stream<T> readStream(Path path, FileFormat format) throws FileParseException;

    // ==================== Write Operations ====================

    /**
     * Write records to a file path.
     */
    void write(List<T> records, Path path, FileFormat format) throws FileWriteException;

    /**
     * Write records to an output stream.
     */
    void write(List<T> records, OutputStream stream, FileFormat format)
        throws FileWriteException;

    /**
     * Write records to a byte array.
     */
    byte[] writeToBytes(List<T> records, FileFormat format) throws FileWriteException;

    // ==================== Merge Operations ====================

    /**
     * Start a merge operation with two lists.
     */
    MergeBuilder<T> merge(List<T> first, List<T> second);

    /**
     * Start a merge operation with multiple lists.
     */
    @SuppressWarnings("unchecked")
    MergeBuilder<T> merge(List<T>... lists);

    // ==================== Split Operations ====================

    /**
     * Split records into partitions based on a rule.
     */
    SplitResult<T> split(List<T> records, SplitRule<T> rule);

    // ==================== Convert Operations ====================

    /**
     * Convert records to another File Type using @MapsFrom mappings.
     */
    <R> List<R> convert(List<T> records, Class<R> targetType);

    // ==================== Query Operations ====================

    /**
     * Find all records matching a predicate.
     */
    List<T> findAll(List<T> records, Predicate<T> predicate);

    /**
     * Find the first record matching a predicate.
     */
    Optional<T> findFirst(List<T> records, Predicate<T> predicate);

    /**
     * Count records matching a predicate.
     */
    long count(List<T> records, Predicate<T> predicate);

    // ==================== Validation Operations ====================

    /**
     * Validate records against @Validate constraints.
     */
    ValidationResult<T> validate(List<T> records);

    // ==================== Send Operations ====================

    /**
     * Start a send operation to a destination.
     */
    SendBuilder<T> send(List<T> records);

    // ==================== Metadata Operations ====================

    /**
     * Get the @FileType annotation.
     */
    FileType getFileType();

    /**
     * Get the schema derived from annotations.
     */
    Schema getSchema();
}
```

---

### MergeBuilder

Fluent builder for merge operations with sorting, deduplication, and filtering.

```java
public interface MergeBuilder<T> {

    // ==================== Sorting ====================

    /**
     * Sort by a comparable field.
     */
    <U extends Comparable<? super U>> MergeBuilder<T> sortBy(Function<T, U> keyExtractor);

    /**
     * Add secondary sort by a comparable field.
     */
    <U extends Comparable<? super U>> MergeBuilder<T> thenBy(Function<T, U> keyExtractor);

    /**
     * Sort using a custom comparator.
     */
    MergeBuilder<T> sortBy(Comparator<T> comparator);

    /**
     * Set sort order to ascending (default).
     */
    MergeBuilder<T> ascending();

    /**
     * Set sort order to descending.
     */
    MergeBuilder<T> descending();

    // ==================== Deduplication ====================

    /**
     * Remove duplicates using @FileId fields.
     */
    MergeBuilder<T> deduplicate();

    /**
     * Remove duplicates using a custom key extractor.
     */
    <K> MergeBuilder<T> deduplicate(Function<T, K> keyExtractor);

    /**
     * Keep last occurrence instead of first when deduplicating.
     */
    MergeBuilder<T> deduplicateKeepLast();

    // ==================== Filtering & Transformation ====================

    /**
     * Filter records by a predicate.
     */
    MergeBuilder<T> filter(Predicate<T> predicate);

    /**
     * Transform each record.
     */
    MergeBuilder<T> transform(UnaryOperator<T> transformer);

    /**
     * Limit the number of results.
     */
    MergeBuilder<T> limit(int limit);

    // ==================== Build ====================

    /**
     * Execute and return the merged list.
     */
    List<T> build();

    /**
     * Execute and return results with statistics.
     */
    MergeResult<T> buildWithStats();
}
```

**MergeResult:**
```java
@Data
@Builder
public class MergeResult<T> {
    private final List<T> records;       // Merged records
    private final int sourceCount;       // Total before merge
    private final int totalCount;        // Total after merge
    private final int duplicatesRemoved; // Duplicates removed
    private final int filteredOut;       // Records filtered out
    private final int limitApplied;      // Records after limit

    public boolean hadDuplicates() {
        return duplicatesRemoved > 0;
    }
}
```

**Example:**
```java
FileRepository<Employee> repo = FileRepository.forType(Employee.class);

// Complex merge operation
MergeResult<Employee> result = repo.merge(mainList, updateList, archiveList)
    .sortBy(Employee::getDepartment)
    .thenBy(Employee::getName)
    .ascending()
    .deduplicate()
    .filter(e -> e.getSalary().compareTo(new BigDecimal("50000")) > 0)
    .filter(e -> e.getStatus().equals("ACTIVE"))
    .limit(1000)
    .buildWithStats();

System.out.println("Source records: " + result.getSourceCount());
System.out.println("Duplicates removed: " + result.getDuplicatesRemoved());
System.out.println("Filtered out: " + result.getFilteredOut());
System.out.println("Final count: " + result.getTotalCount());

List<Employee> employees = result.getRecords();
```

---

### SplitRule & SplitResult

Split records into partitions based on configurable rules.

**SplitRule:**
```java
public abstract class SplitRule<T> {

    // ==================== Abstract Method ====================

    public abstract String getPartitionKey(T record);

    // ==================== Factory Methods ====================

    /**
     * Split by a field value. Each unique value creates a partition.
     */
    static <T> SplitRule<T> byField(Function<T, ?> extractor);

    /**
     * Split into partitions of N records each.
     */
    static <T> SplitRule<T> byCount(int recordsPerPartition);

    /**
     * Split into partitions of approximately N bytes each.
     */
    static <T> SplitRule<T> bySize(long bytesPerPartition);

    /**
     * Split by a boolean predicate (two partitions).
     */
    static <T> SplitRule<T> byPredicate(
        Predicate<T> predicate,
        String trueLabel,
        String falseLabel
    );

    /**
     * Split by multiple named predicates.
     */
    static <T> SplitRule<T> byPredicates(Map<String, Predicate<T>> predicates);

    // ==================== Chaining ====================

    /**
     * Chain with another rule for hierarchical partitioning.
     */
    SplitRule<T> andThen(SplitRule<T> next);
}
```

**SplitResult:**
```java
public class SplitResult<T> {

    // ==================== Access Methods ====================

    /**
     * Get records for a specific partition key.
     * Returns empty list if key doesn't exist.
     */
    List<T> get(String key);

    /**
     * Get all partition keys.
     */
    Set<String> getPartitionKeys();

    /**
     * Get the number of partitions.
     */
    int getPartitionCount();

    /**
     * Get record counts for each partition.
     */
    Map<String, Integer> getCounts();

    /**
     * Get total record count across all partitions.
     */
    int getTotalCount();

    /**
     * Check if a partition exists.
     */
    boolean hasPartition(String key);

    /**
     * Check if a partition is empty.
     */
    boolean isEmpty(String key);

    /**
     * Get all partitions as an unmodifiable map.
     */
    Map<String, List<T>> getPartitions();

    /**
     * Get the rule used to create this result.
     */
    SplitRule<T> getRule();

    // ==================== Write Operations ====================

    /**
     * Write all partitions to files in a directory.
     */
    void writeAll(
        Path directory,
        FileFormat format,
        Function<String, String> filenameGenerator,
        PartitionWriter<T> writer
    ) throws Exception;

    @FunctionalInterface
    interface PartitionWriter<T> {
        void write(String key, List<T> records, Path path) throws Exception;
    }
}
```

**Examples:**

```java
FileRepository<Order> repo = FileRepository.forType(Order.class);
List<Order> orders = repo.read(path, FileFormat.csv().build());

// Split by field
SplitResult<Order> byRegion = repo.split(orders, SplitRule.byField(Order::getRegion));
List<Order> westCoast = byRegion.get("WEST");
List<Order> eastCoast = byRegion.get("EAST");

// Split by count (batch processing)
SplitResult<Order> batches = repo.split(orders, SplitRule.byCount(100));
for (String batchKey : batches.getPartitionKeys()) {
    processBatch(batches.get(batchKey));
}

// Split by predicate
SplitResult<Order> byValue = repo.split(orders,
    SplitRule.byPredicate(
        o -> o.getAmount().compareTo(new BigDecimal("10000")) > 0,
        "high_value",
        "standard"
    )
);

// Split by multiple predicates
SplitResult<Order> byPriority = repo.split(orders,
    SplitRule.byPredicates(Map.of(
        "urgent", o -> o.getPriority() == Priority.URGENT,
        "high", o -> o.getPriority() == Priority.HIGH,
        "normal", o -> o.getPriority() == Priority.NORMAL,
        "low", o -> o.getPriority() == Priority.LOW
    ))
);

// Write partitions to separate files
byRegion.writeAll(
    Path.of("output/by-region"),
    FileFormat.csv().build(),
    key -> "orders_" + key.toLowerCase() + ".csv",
    (key, records, path) -> repo.write(records, path, FileFormat.csv().build())
);
```

---

### SendBuilder

Fluent builder for sending files to destinations.

```java
public interface SendBuilder<T> {

    /**
     * Set the file format.
     */
    SendBuilder<T> as(FileFormat format);

    /**
     * Set destination by class (must have @Destination annotation).
     */
    SendBuilder<T> to(Class<?> destinationType);

    /**
     * Set destination by name.
     */
    SendBuilder<T> to(String destinationName);

    /**
     * Set the output filename.
     */
    SendBuilder<T> withFilename(String filename);

    /**
     * Set a dynamic filename.
     */
    SendBuilder<T> withFilename(Supplier<String> filenameSupplier);

    /**
     * Add metadata to the send operation.
     */
    SendBuilder<T> withMetadata(String key, Object value);

    /**
     * Set success callback.
     */
    SendBuilder<T> onSuccess(Consumer<SendResult> callback);

    /**
     * Set failure callback.
     */
    SendBuilder<T> onFailure(Consumer<SendResult> callback);

    /**
     * Configure retry behavior.
     */
    SendBuilder<T> withRetry(RetryConfig config);

    /**
     * Execute synchronously.
     */
    SendResult execute();

    /**
     * Execute asynchronously.
     */
    CompletableFuture<SendResult> executeAsync();
}
```

**SendResult:**
```java
@Data
@Builder
public class SendResult {
    private final boolean success;
    private final int recordCount;
    private final long byteCount;
    private final String destinationName;
    private final String destinationHost;
    private final String remotePath;
    private final String filename;
    private final Instant timestamp;
    private final String errorMessage;
    private final Throwable exception;
    private final int retryAttempts;
    private final Map<String, Object> metadata;

    public boolean isFailed() { return !success; }
    public <T> T getMetadata(String key) { ... }
}
```

**RetryConfig:**
```java
@Data
@Builder
public class RetryConfig {
    private int maxAttempts;           // Default: 3
    private long backoffMs;            // Default: 1000
    private double backoffMultiplier;  // Default: 2.0
    private long maxBackoffMs;         // Default: 30000
    private boolean retryOnConnectionError;  // Default: true
    private boolean retryOnTimeout;          // Default: true

    static RetryConfig defaults();
    static RetryConfig noRetry();
}
```

**Example:**
```java
FileRepository<Invoice> repo = FileRepository.forType(Invoice.class);

SendResult result = repo.send(invoices)
    .as(FileFormat.csv().build())
    .to(BillingSystem.class)
    .withFilename(() -> "invoices_" + LocalDate.now() + ".csv")
    .withMetadata("batchId", UUID.randomUUID().toString())
    .withRetry(RetryConfig.builder()
        .maxAttempts(5)
        .backoffMs(2000)
        .build())
    .onSuccess(r -> log.info("Sent {} records to {}", r.getRecordCount(), r.getDestinationName()))
    .onFailure(r -> log.error("Failed to send: {}", r.getErrorMessage()))
    .execute();

if (result.isSuccess()) {
    System.out.println("Sent " + result.getRecordCount() + " records");
    System.out.println("Remote path: " + result.getRemotePath());
}
```

---

## File Formats

Use `FileFormat` builders to configure file parsing and writing.

```java
public class FileFormat {

    // ==================== Factory Methods ====================

    static Builder csv();         // Comma-separated values
    static Builder tsv();         // Tab-separated values
    static Builder pipe();        // Pipe-delimited
    static Builder fixedWidth();  // Fixed-width format
    static Builder xml();         // XML format
    static Builder json();        // JSON array format
    static Builder jsonLines();   // JSON lines (one object per line)

    // ==================== Builder Methods ====================

    public static class Builder {
        Builder charset(Charset charset);              // Default: UTF-8
        Builder lineSeparator(String lineSeparator);   // Default: System line separator
        Builder nullValue(String nullValue);           // Default: "" (empty string)
        Builder delimiter(char delimiter);             // For delimited formats
        Builder quoteChar(char quoteChar);             // Default: "
        Builder escapeChar(char escapeChar);           // Default: \
        Builder hasHeader(boolean hasHeader);          // Default: true
        Builder rootElement(String rootElement);       // XML root element
        Builder recordElement(String recordElement);   // XML record element
        Builder xmlDeclaration(boolean include);       // Include <?xml...?>
        Builder prettyPrint(boolean prettyPrint);      // Pretty print output
        Builder jsonArray(boolean jsonArray);          // Wrap in JSON array
        FileFormat build();
    }
}
```

**Examples:**

```java
// CSV with custom settings
FileFormat csvFormat = FileFormat.csv()
    .delimiter(';')
    .quoteChar('\'')
    .hasHeader(true)
    .charset(StandardCharsets.ISO_8859_1)
    .nullValue("NULL")
    .build();

// JSON with pretty printing
FileFormat jsonFormat = FileFormat.json()
    .prettyPrint(true)
    .build();

// XML with custom elements
FileFormat xmlFormat = FileFormat.xml()
    .rootElement("employees")
    .recordElement("employee")
    .xmlDeclaration(true)
    .prettyPrint(true)
    .build();

// Fixed-width format
FileFormat fixedFormat = FileFormat.fixedWidth()
    .lineSeparator("\r\n")
    .charset(StandardCharsets.US_ASCII)
    .build();

// Tab-separated
FileFormat tsvFormat = FileFormat.tsv()
    .hasHeader(false)
    .build();
```

---

## Validation

### ValidationResult

Result of validating records against `@Validate` constraints.

```java
public class ValidationResult<T> {

    // ==================== Query Methods ====================

    boolean isValid();              // All records valid?
    boolean hasErrors();            // Any validation errors?
    List<T> getValidRecords();      // Records that passed
    List<T> getInvalidRecords();    // Records that failed
    List<ValidationError> getErrors();  // All validation errors
    int getTotalCount();
    int getValidCount();
    int getInvalidCount();
    int getErrorCount();

    // ==================== Exception ====================

    /**
     * Throw RecordValidationException if any records are invalid.
     */
    void throwIfInvalid();
}
```

**ValidationError:**
```java
@Data
@Builder
public class ValidationError {
    private String message;        // Error message
    private String fieldName;      // Field that failed (null for record-level)
    private Object invalidValue;   // The invalid value
    private Integer lineNumber;    // Line number in source file
    private String ruleName;       // Validation rule that failed
}
```

**Example:**
```java
FileRepository<Order> repo = FileRepository.forType(Order.class);
List<Order> orders = repo.read(path, FileFormat.csv().build());

ValidationResult<Order> result = repo.validate(orders);

if (result.hasErrors()) {
    System.out.println("Invalid records: " + result.getInvalidCount());

    for (ValidationError error : result.getErrors()) {
        System.out.printf("Line %d, Field '%s': %s (value: %s)%n",
            error.getLineNumber(),
            error.getFieldName(),
            error.getMessage(),
            error.getInvalidValue()
        );
    }

    // Process only valid records
    List<Order> validOrders = result.getValidRecords();
    processOrders(validOrders);

} else {
    processOrders(orders);
}

// Or throw exception if any invalid
result.throwIfInvalid();  // Throws RecordValidationException
```

---

## Destinations

### @Destination

Marks an interface as a destination definition.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Destination {
    String name();                   // Unique destination name (REQUIRED)
    String description() default "";
    String enabled() default "true"; // Can reference properties
}
```

### @Sftp

Configures SFTP destination.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sftp {
    String host();                          // SFTP hostname (REQUIRED)
    int port() default 22;
    String remotePath();                    // Remote directory (REQUIRED)
    String credentials();                   // Credentials reference (REQUIRED)
    int connectionTimeout() default 30000;
    boolean createDirectory() default false;
    String tempSuffix() default ".tmp";
    String knownHosts() default "";
}
```

### @HttpApi

Configures HTTP API destination.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpApi {
    String url();                               // URL endpoint (REQUIRED)
    HttpMethod method() default HttpMethod.POST;
    String contentType() default "application/json";
    String[] headers() default {};
    String authentication() default "";
    int connectionTimeout() default 30000;
    int readTimeout() default 60000;
    boolean multipart() default false;
    String multipartFieldName() default "file";

    enum HttpMethod { GET, POST, PUT, PATCH, DELETE }
}
```

**Example Destinations:**

```java
@Destination(name = "billing-sftp", description = "Billing system SFTP server")
@Sftp(
    host = "${billing.sftp.host}",
    port = 22,
    remotePath = "/incoming/invoices",
    credentials = "billing-credentials",
    createDirectory = true
)
public interface BillingSystem {}

@Destination(name = "reporting-api", description = "Reporting REST API")
@HttpApi(
    url = "${reporting.api.url}/upload",
    method = HttpMethod.POST,
    contentType = "application/json",
    authentication = "reporting-oauth",
    headers = {"X-API-Version: 2.0"}
)
public interface ReportingApi {}
```

**Credentials:**

```java
// Basic credentials
@Data
@Builder
public class Credentials {
    private String username;
    private String password;
}

// SSH credentials (password or key-based)
@Data
@Builder
public class SshCredentials {
    private String username;
    private String password;
    private String privateKey;       // Key content
    private String privateKeyPath;   // Key file path
    private String passphrase;       // Key passphrase

    boolean isKeyBased();
}

// OAuth2 credentials
@Data
@Builder
public class OAuth2Credentials {
    private String clientId;
    private String clientSecret;
    private String tokenUrl;
    private String scope;
    private String grantType;        // Default: "client_credentials"
    private String username;         // For password grant
    private String password;         // For password grant
}

// Credentials provider interface
public interface CredentialsProvider {
    Credentials getCredentials(String name);
    SshCredentials getSshCredentials(String name);
    OAuth2Credentials getOAuth2Credentials(String name);
}
```

---

## Exceptions

All framework exceptions extend `BawException` (unchecked).

| Exception | Description |
|-----------|-------------|
| `BawException` | Base exception for all framework errors |
| `FileParseException` | Error reading/parsing a file |
| `FileWriteException` | Error writing a file |
| `SchemaValidationException` | Invalid schema (missing annotations, etc.) |
| `RecordValidationException` | Records failed validation |
| `ConversionException` | Error converting between types |
| `DestinationException` | Error sending to destination |
| `SplitRuleConflictException` | Conflicting split rules |
| `CredentialsNotFoundException` | Credentials not found |

**FileParseException (detailed context):**
```java
public class FileParseException extends BawException {
    Path getFilePath();
    Integer getLineNumber();
    String getLineContent();
    Integer getColumnIndex();
    String getFieldName();

    // Builder for detailed errors
    static Builder builder() { ... }
}

// Example
throw FileParseException.builder()
    .message("Invalid date format")
    .filePath(path)
    .lineNumber(42)
    .lineContent("1,John,invalid-date")
    .columnIndex(2)
    .fieldName("hireDate")
    .build();
```

---

## Examples

### Complete Workflow Example

```java
@FileType(name = "transaction", description = "Financial transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @FileId
    @FileColumn(order = 1, name = "txn_id", nullable = false)
    @Validate(notNull = true)
    private String transactionId;

    @FileColumn(order = 2, name = "account")
    @Validate(notBlank = true, pattern = "^[A-Z]{2}\\d{8}$")
    private String accountNumber;

    @FileColumn(order = 3, name = "amount", format = "#,##0.00")
    @Validate(min = 0.01, max = 1000000)
    private BigDecimal amount;

    @FileColumn(order = 4, name = "type")
    @Validate(allowedValues = {"CREDIT", "DEBIT", "TRANSFER"})
    private String type;

    @FileColumn(order = 5, name = "date", format = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @FileColumn(order = 6, name = "status")
    private String status;
}

// Processing workflow
public class TransactionProcessor {

    private final FileRepository<Transaction> repo =
        FileRepository.forType(Transaction.class);

    public void process(Path inputDir, Path outputDir) throws Exception {

        // 1. Read all transaction files
        List<Path> files = Files.list(inputDir)
            .filter(p -> p.toString().endsWith(".csv"))
            .toList();

        List<Transaction> allTransactions = repo.readAll(files, FileFormat.csv().build());

        // 2. Validate
        ValidationResult<Transaction> validation = repo.validate(allTransactions);
        if (validation.hasErrors()) {
            // Write invalid records to error file
            repo.write(
                validation.getInvalidRecords(),
                outputDir.resolve("errors.csv"),
                FileFormat.csv().build()
            );
            log.warn("Found {} invalid records", validation.getInvalidCount());
        }

        // 3. Merge and deduplicate valid records
        List<Transaction> processed = repo.merge(validation.getValidRecords(), List.of())
            .sortBy(Transaction::getTransactionDate)
            .thenBy(Transaction::getTransactionId)
            .ascending()
            .deduplicate()
            .filter(t -> !"CANCELLED".equals(t.getStatus()))
            .build();

        // 4. Split by type
        SplitResult<Transaction> byType = repo.split(
            processed,
            SplitRule.byField(Transaction::getType)
        );

        // 5. Write each type to separate file
        for (String type : byType.getPartitionKeys()) {
            Path outputPath = outputDir.resolve(type.toLowerCase() + "_transactions.json");
            repo.write(byType.get(type), outputPath, FileFormat.json().prettyPrint(true).build());
        }

        // 6. Send high-value transactions to reporting
        List<Transaction> highValue = repo.findAll(
            processed,
            t -> t.getAmount().compareTo(new BigDecimal("10000")) > 0
        );

        if (!highValue.isEmpty()) {
            SendResult result = repo.send(highValue)
                .as(FileFormat.json().build())
                .to(ReportingApi.class)
                .withFilename("high_value_" + LocalDate.now() + ".json")
                .withRetry(RetryConfig.defaults())
                .execute();

            if (result.isFailed()) {
                throw new RuntimeException("Failed to send: " + result.getErrorMessage());
            }
        }

        log.info("Processed {} transactions, {} high-value sent to reporting",
            processed.size(), highValue.size());
    }
}
```

---

## Schema Information

Access schema metadata programmatically:

```java
FileRepository<Employee> repo = FileRepository.forType(Employee.class);

// Get file type info
FileType fileType = repo.getFileType();
System.out.println("Name: " + fileType.name());
System.out.println("Version: " + fileType.version());

// Get schema
Schema schema = repo.getSchema();
System.out.println("Columns: " + schema.getColumns().size());
System.out.println("ID Columns: " + schema.getIdColumns().size());

// Inspect columns
for (Schema.ColumnSchema column : schema.getColumns()) {
    System.out.printf("  %s (%s) - order=%d, nullable=%b%n",
        column.getName(),
        column.getJavaType().getSimpleName(),
        column.getOrder(),
        column.isNullable()
    );
}
```

---

## Best Practices

1. **Always use @FileId** - Enables proper deduplication
2. **Order columns explicitly** - Don't rely on field declaration order
3. **Use format patterns** - For dates and numbers
4. **Validate early** - Call `validate()` before processing
5. **Use buildWithStats()** - Track merge operation metrics
6. **Handle large files with streams** - Use `readStream()` for memory efficiency
7. **Configure retries** - Always use `withRetry()` for destinations
8. **Use meaningful partition keys** - For split operations

---

*Integration Hub Framework - API Documentation v1.0.0*
