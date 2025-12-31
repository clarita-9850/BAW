# Integration Hub Framework - Complete Guide

**Version:** 1.0.0
**Package:** `com.cmips.integration.framework`
**Java Version:** 17+
**Spring Boot Version:** 3.2.x

---

## Table of Contents

1. [Overview](#1-overview)
2. [Framework Architecture](#2-framework-architecture)
3. [Core Features](#3-core-features)
4. [The BAW (Batch And Workflow) Module](#4-the-baw-batch-and-workflow-module)
5. [Integration Flow Engine](#5-integration-flow-engine)
6. [Utility Classes](#6-utility-classes)
7. [Configuration](#7-configuration)
8. [Usage in CMIPS Application](#8-usage-in-cmips-application)
9. [Quick Reference](#9-quick-reference)

---

## 1. Overview

The Integration Hub Framework is a comprehensive Java Spring Boot library designed for enterprise batch processing and data integration. It provides two complementary programming models:

### 1.1 Integration Flow Engine
A flow-based processing model for orchestrating data pipelines with:
- Input sources (files, SFTP, REST APIs, databases)
- Transformers (validation, enrichment, conversion)
- Output destinations (files, SFTP, REST APIs, databases)
- Automatic component discovery and registration
- Error handling with circuit breaker and retry support

### 1.2 BAW (Batch And Workflow) Module
A JPA-inspired declarative file processing library with:
- Annotation-based schema definition for file types
- Type-safe file operations (read, write, merge, split, convert)
- Multi-format support (CSV, JSON, XML, Fixed-Width, etc.)
- Declarative validation framework
- SFTP and HTTP API destinations

---

## 2. Framework Architecture

```
+-----------------------------------------------------------------------------+
|                         Integration Hub Framework                            |
+-----------------------------------------------------------------------------+
|                                                                              |
|   +------------------+      +------------------+      +------------------+   |
|   |   Input Sources  | ---> |   Transformers   | ---> |     Outputs      |   |
|   |  (IInputSource)  |      |  (ITransformer)  |      | (IOutputDest.)   |   |
|   +------------------+      +------------------+      +------------------+   |
|            ^                        ^                         ^              |
|            |                        |                         |              |
|            +------------------------+-------------------------+              |
|                                     |                                        |
|                    +----------------+----------------+                       |
|                    |       Component Registry        |                       |
|                    +----------------+----------------+                       |
|                                     |                                        |
|      +--------------+---------------+--------------+---------------+         |
|      |              |               |              |               |         |
|  +---+---+    +-----+-----+   +-----+-----+   +----+----+    +-----+-----+   |
|  |Scanner|    |  Flow     |   |  Error    |   |  BAW    |    |  Utility  |   |
|  |       |    | Executor  |   | Handler   |   | Module  |    |  Classes  |   |
|  +-------+    +-----------+   +-----------+   +---------+    +-----------+   |
|                                     |                                        |
|                    +----------------+----------------+                       |
|                    |      Integration Engine         |                       |
|                    +---------------------------------+                       |
|                                                                              |
+-----------------------------------------------------------------------------+
|                    Spring Boot Auto-Configuration                            |
+-----------------------------------------------------------------------------+
```

### Package Structure

```
com.cmips.integration.framework/
├── annotations/           # @InputSource, @Transformer, @OutputDestination, etc.
├── base/                  # Abstract base classes (AbstractFileReader, etc.)
├── baw/                   # BAW Module (JPA-style file processing)
│   ├── annotation/        #   @FileType, @FileColumn, @FileId, @Validate, etc.
│   ├── destination/       #   @Destination, @Sftp, @HttpApi, Credentials
│   ├── format/            #   FileFormat builders (CSV, JSON, XML, etc.)
│   ├── repository/        #   FileRepository, MergeBuilder, SendBuilder
│   ├── split/             #   SplitRule, SplitResult
│   └── validation/        #   ValidationResult, ValidationError
├── config/                # Auto-configuration and properties
├── core/                  # ComponentRegistry, FlowExecutor, IntegrationEngine
├── exception/             # IntegrationException hierarchy
├── interfaces/            # IInputSource, ITransformer, IOutputDestination, etc.
├── model/                 # FlowDefinition, SendResult, ValidationResult
├── support/               # SftpConfig, RestConfig, FilePattern, FileWatcher
└── util/                  # FileUtil, MergeUtil, SortUtil, TransformUtil, etc.
```

---

## 3. Core Features

### 3.1 Component-Based Architecture

Define integration components using annotations:

```java
// Input Source - reads data from files
@InputSource(name = "paymentFileReader", description = "Reads payment CSV files")
public class PaymentFileReader extends AbstractFileReader<Payment> {
    // ...
}

// Transformer - validates and transforms data
@Transformer(name = "paymentValidator", description = "Validates payments")
public class PaymentValidator extends AbstractTransformer<Payment, Payment> {
    // ...
}

// Output Destination - sends data to REST API
@OutputDestination(name = "paymentApiClient", description = "Sends to API")
public class PaymentApiClient extends AbstractRestClient<Payment> {
    // ...
}
```

### 3.2 Automatic Component Discovery

Components are automatically discovered at startup by `ComponentScanner` and registered in `ComponentRegistry`.

### 3.3 Flow Orchestration

Define and execute data processing flows:

```java
FlowDefinition flow = FlowDefinition.builder()
    .name("paymentProcessingFlow")
    .description("Processes payment files")
    .addInput("paymentFileReader")
    .addTransformer("paymentValidator")
    .addTransformer("paymentEnricher")
    .addOutput("paymentApiClient")
    .addOutput("archiveWriter")
    .failOnError(true)
    .enabled(true)
    .build();

// Execute the flow
FlowResult result = engine.executeFlow("paymentProcessingFlow");
```

### 3.4 Error Handling

Centralized error handling with circuit breaker and retry support:

```java
// Annotation-based retry
@Retry(maxAttempts = 3, initialDelay = 1000, multiplier = 2.0)
@CircuitBreaker(failureThreshold = 5, resetTimeout = 30000)
public class PaymentApiClient extends AbstractRestClient<Payment> {
    // ...
}

// Programmatic error handling
errorHandler.registerHandler(ConnectionException.class, (flowName, ex) -> {
    alertService.sendConnectionAlert(flowName, ex.getMessage());
});
```

### 3.5 Async Execution

Execute flows asynchronously with configurable thread pools:

```java
Future<FlowResult> future = engine.executeFlowAsync("paymentFlow");
// ... do other work ...
FlowResult result = future.get();
```

---

## 4. The BAW (Batch And Workflow) Module

The BAW module provides a JPA-inspired programming model for file processing operations.

### 4.1 Schema Definition with Annotations

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FileType(name = "payment", description = "Payment records", version = "1.0")
public class Payment {

    @FileId
    @FileColumn(order = 1, name = "payment_id", nullable = false)
    @Validate(notNull = true)
    private String paymentId;

    @FileColumn(order = 2, name = "account_number")
    @Validate(notBlank = true, pattern = "^[A-Z]{2}\\d{8}$")
    private String accountNumber;

    @FileColumn(order = 3, name = "amount", format = "#,##0.00")
    @Validate(min = 0.01, max = 1000000)
    private BigDecimal amount;

    @FileColumn(order = 4, name = "payment_date", format = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @FileColumn(order = 5, name = "status")
    @Validate(allowedValues = {"PENDING", "APPROVED", "REJECTED"})
    private String status;
}
```

### 4.2 FileRepository Operations

```java
// Create repository for a file type
FileRepository<Payment> repo = FileRepository.forType(Payment.class);

// READ operations
List<Payment> payments = repo.read(path, FileFormat.csv().build());
Stream<Payment> stream = repo.readStream(path, format);  // Lazy loading for large files

// WRITE operations
repo.write(payments, path, FileFormat.json().prettyPrint(true).build());
byte[] bytes = repo.writeToBytes(payments, format);

// MERGE operations - combine multiple lists with sorting and deduplication
List<Payment> merged = repo.merge(list1, list2, list3)
    .sortBy(Payment::getPaymentDate)
    .thenBy(Payment::getPaymentId)
    .descending()
    .deduplicate()  // Uses @FileId for deduplication
    .filter(p -> p.getStatus().equals("APPROVED"))
    .limit(1000)
    .build();

// SPLIT operations - partition records by field or predicate
SplitResult<Payment> byStatus = repo.split(payments, SplitRule.byField(Payment::getStatus));
List<Payment> approved = byStatus.get("APPROVED");
List<Payment> pending = byStatus.get("PENDING");

// VALIDATE operations
ValidationResult<Payment> result = repo.validate(payments);
if (result.hasErrors()) {
    List<Payment> invalidRecords = result.getInvalidRecords();
    List<ValidationError> errors = result.getErrors();
}

// CONVERT operations - transform between file types
List<ExternalPayment> external = repo.convert(payments, ExternalPayment.class);

// QUERY operations
List<Payment> highValue = repo.findAll(payments, p -> p.getAmount().compareTo(new BigDecimal("10000")) > 0);
Optional<Payment> first = repo.findFirst(payments, p -> p.getPaymentId().equals("PAY001"));
```

### 4.3 File Format Support

| Format | Factory Method | Description |
|--------|----------------|-------------|
| CSV | `FileFormat.csv()` | Comma-separated values |
| TSV | `FileFormat.tsv()` | Tab-separated values |
| Pipe | `FileFormat.pipe()` | Pipe-delimited |
| Fixed-Width | `FileFormat.fixedWidth()` | Positional fixed-width |
| JSON | `FileFormat.json()` | JSON array |
| JSON Lines | `FileFormat.jsonLines()` | One JSON object per line |
| XML | `FileFormat.xml()` | XML format |

```java
// CSV with custom options
FileFormat csvFormat = FileFormat.csv()
    .delimiter(';')
    .quoteChar('\'')
    .hasHeader(true)
    .charset(StandardCharsets.ISO_8859_1)
    .build();

// XML with custom elements
FileFormat xmlFormat = FileFormat.xml()
    .rootElement("payments")
    .recordElement("payment")
    .xmlDeclaration(true)
    .prettyPrint(true)
    .build();

// Fixed-width format
@FileColumn(order = 1, name = "id", length = 10, alignment = Alignment.RIGHT, padChar = '0')
private Long id;  // Output: "0000000123"
```

### 4.4 Destination System

Define SFTP and HTTP API destinations:

```java
// SFTP Destination
@Destination(name = "billing-sftp", description = "Billing SFTP server")
@Sftp(
    host = "${billing.sftp.host}",
    port = 22,
    remotePath = "/incoming/payments",
    credentials = "billing-credentials",
    createDirectory = true
)
public interface BillingSystem {}

// HTTP API Destination
@Destination(name = "payment-api", description = "Payment REST API")
@HttpApi(
    url = "${payment.api.url}/upload",
    method = HttpMethod.POST,
    contentType = "application/json",
    authentication = "payment-oauth"
)
public interface PaymentApi {}

// Send records to a destination
SendResult result = repo.send(payments)
    .as(FileFormat.json().build())
    .to(BillingSystem.class)
    .withFilename(() -> "payments_" + LocalDate.now() + ".json")
    .withMetadata("batchId", UUID.randomUUID().toString())
    .withRetry(RetryConfig.builder()
        .maxAttempts(5)
        .backoffMs(2000)
        .build())
    .onSuccess(r -> log.info("Sent {} records", r.getRecordCount()))
    .onFailure(r -> log.error("Failed: {}", r.getErrorMessage()))
    .execute();
```

### 4.5 Field Mapping for Type Conversion

```java
// Source type
@FileType(name = "internal-payment")
public class InternalPayment {
    @FileColumn(order = 1) private String internalId;
    @FileColumn(order = 2) private BigDecimal monthlySalary;
    @FileColumn(order = 3) private Boolean isActive;
}

// Target type with mappings
@FileType(name = "external-payment")
public class ExternalPayment {

    @FileColumn(order = 1)
    @MapsFrom(source = InternalPayment.class, field = "internalId")
    private String externalId;

    @FileColumn(order = 2)
    @MapsFrom(source = InternalPayment.class, field = "monthlySalary",
              transformer = AnnualSalaryTransformer.class)
    private BigDecimal annualSalary;

    @FileColumn(order = 3)
    @MapsFrom(source = InternalPayment.class, field = "isActive",
              transformer = StatusTransformer.class)
    private String status;

    // Custom transformer
    public static class AnnualSalaryTransformer implements FieldTransformer<BigDecimal, BigDecimal> {
        @Override
        public BigDecimal transform(BigDecimal monthly) {
            return monthly != null ? monthly.multiply(new BigDecimal("12")) : null;
        }
    }
}
```

### 4.6 Validation Framework

```java
@FileType(name = "order")
@Validate(validator = OrderDateValidator.class, message = "End date must be after start date")
public class Order {

    @FileColumn(order = 1)
    @Validate(notNull = true, message = "Order ID is required")
    private Long orderId;

    @FileColumn(order = 2)
    @Validate(notBlank = true, minLength = 2, maxLength = 100)
    private String customerName;

    @FileColumn(order = 3)
    @Validate(pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private String email;

    @FileColumn(order = 4)
    @Validate(min = 0.01, max = 999999.99)
    private BigDecimal amount;

    @FileColumn(order = 5)
    @Validate(allowedValues = {"PENDING", "APPROVED", "SHIPPED"})
    private String status;

    // Record-level validator
    public static class OrderDateValidator implements RecordValidator<Order> {
        @Override
        public ValidationError validate(Order record) {
            if (record.getEndDate() != null && record.getStartDate() != null) {
                if (record.getEndDate().isBefore(record.getStartDate())) {
                    return new ValidationError("End date must be after start date");
                }
            }
            return null;
        }
    }
}
```

---

## 5. Integration Flow Engine

### 5.1 Core Interfaces

| Interface | Purpose |
|-----------|---------|
| `IInputSource<T>` | Read data from files, SFTP, APIs, databases |
| `ITransformer<I, O>` | Transform, validate, and enrich data |
| `IOutputDestination<T>` | Send data to files, SFTP, APIs, databases |
| `IMerger<T>` | Merge data from multiple sources |
| `ISorter<T>` | Sort data collections |
| `IValidator<T>` | Validate data |

### 5.2 IntegrationEngine

```java
@Autowired
private IntegrationEngine engine;

// Synchronous execution
FlowResult result = engine.executeFlow("paymentFlow");

// Asynchronous execution
Future<FlowResult> future = engine.executeFlowAsync("paymentFlow");

// Execute all enabled flows
Map<String, FlowResult> results = engine.executeAllFlows();

// Flow management
boolean running = engine.isFlowRunning("paymentFlow");
Collection<String> runningFlows = engine.getRunningFlows();
boolean cancelled = engine.cancelFlow("paymentFlow");

// Validation
List<String> errors = engine.validateFlow("paymentFlow");
```

### 5.3 FlowResult

```java
FlowResult result = engine.executeFlow("paymentFlow");

String flowName = result.getFlowName();
boolean success = result.isSuccess();
boolean partial = result.isPartial();
int recordCount = result.getRecordCount();
Duration duration = result.getDuration();
String message = result.getMessage();
```

### 5.4 ErrorHandler

```java
@Autowired
private ErrorHandler errorHandler;

// Register custom handlers
errorHandler.registerHandler(ConnectionException.class, (flowName, ex) -> {
    alertService.sendConnectionAlert(flowName, ex.getMessage());
});

// Get statistics
ErrorStats stats = errorHandler.getStats("paymentFlow");
long errorCount = stats.getErrorCount();
Map<String, Long> byType = stats.getErrorsByType();
Instant lastError = stats.getLastErrorTime();
```

---

## 6. Utility Classes

### 6.1 FileUtil

```java
// Reading and writing
String content = FileUtil.readFile(path);
List<String> lines = FileUtil.readLines(path);
FileUtil.writeFile(path, content);

// CSV/XML parsing
List<Map<String, String>> csvData = FileUtil.parseCsv(path);
List<Map<String, String>> csvData = FileUtil.parseCsv(path, hasHeader);
<T> T xmlObject = FileUtil.parseXml(path, MyClass.class);

// File operations
FileUtil.copyFile(source, target);
FileUtil.moveFile(source, target);
List<Path> files = FileUtil.listFiles(directory, "*.csv");
String checksum = FileUtil.calculateChecksum(path, ChecksumAlgorithm.SHA256);
```

### 6.2 MergeUtil

```java
// Merge multiple lists
List<T> merged = MergeUtil.merge(List.of(list1, list2, list3));

// Merge with deduplication
List<T> unique = MergeUtil.mergeUnique(List.of(list1, list2), Item::getId);
List<T> uniqueFirst = MergeUtil.mergeUniqueFirst(List.of(list1, list2), Item::getId);

// Merge and sort
List<T> sorted = MergeUtil.mergeAndSort(List.of(list1, list2), comparator);

// Merge two sorted lists efficiently
List<T> merged = MergeUtil.mergeSorted(list1, list2, comparator);

// Partition into chunks
List<List<T>> chunks = MergeUtil.partition(list, chunkSize);
```

### 6.3 SortUtil

```java
// Sort by field name using reflection
List<T> sorted = SortUtil.sortByField(data, "fieldName");
List<T> sorted = SortUtil.sortByFields(data, "field1", "field2");

// Sort with comparator
List<T> sorted = SortUtil.sort(data, comparator);
List<T> ascending = SortUtil.sortAscending(data);
List<T> descending = SortUtil.sortDescending(data);

// Top/bottom N elements
List<T> top = SortUtil.topN(data, 10, comparator);
List<T> bottom = SortUtil.bottomN(data, 10, comparator);
```

### 6.4 TransformUtil

```java
// JSON operations
String json = TransformUtil.toJson(object);
String prettyJson = TransformUtil.toJsonPretty(object);
<T> T object = TransformUtil.fromJson(json, MyClass.class);
<T> List<T> list = TransformUtil.fromJsonList(json, MyClass.class);

// XML operations
String xml = TransformUtil.toXml(object);
<T> T object = TransformUtil.fromXml(xml, MyClass.class);
```

### 6.5 SftpUtil

```java
// Create client and perform operations
SftpClient client = SftpUtil.createClient(config);
SftpUtil.upload(client, localPath, remoteDir, remoteFileName);
SftpUtil.download(client, remotePath, localPath);
List<RemoteFile> files = SftpUtil.listFiles(client, remoteDir, pattern);
boolean exists = SftpUtil.exists(client, remotePath);
```

### 6.6 RestUtil

```java
// Create client and perform HTTP operations
RestClient client = RestUtil.createClient(config);
<T> T response = RestUtil.get(client, "/api/resource", ResponseType.class);
<T> T response = RestUtil.post(client, "/api/resource", body, ResponseType.class);
<T> T response = RestUtil.put(client, "/api/resource", body, ResponseType.class);
RestUtil.delete(client, "/api/resource");

// OAuth2
String token = RestUtil.getOAuth2Token(oAuth2Config);
```

### 6.7 ValidationUtil

```java
// Schema validation
ValidationResult result = ValidationUtil.validateJson(json, schemaPath);
ValidationResult result = ValidationUtil.validateXml(xml, xsdPath);

// Common validators
boolean valid = ValidationUtil.isNotBlank(string);
boolean valid = ValidationUtil.isValidEmail(email);
boolean valid = ValidationUtil.isValidDate(date, pattern);
boolean valid = ValidationUtil.isInRange(number, min, max);
```

### 6.8 DatabaseUtil

```java
// Query execution
List<Map<String, Object>> results = DatabaseUtil.executeQuery(
    dataSource, "SELECT * FROM payments WHERE status = :status",
    Map.of("status", "PENDING"));

// Batch operations
int[] counts = DatabaseUtil.executeBatch(
    dataSource, "INSERT INTO payments (id, amount) VALUES (:id, :amount)",
    batchParams);

// Transaction support
DatabaseUtil.executeInTransaction(dataSource, connection -> {
    // multiple operations
});
```

---

## 7. Configuration

### 7.1 Application Properties

```yaml
integration:
  enabled: true
  scan-packages:
    - com.cmips.integration

  retry:
    max-attempts: 3
    initial-delay: 1000
    max-delay: 30000
    multiplier: 2.0

  circuit-breaker:
    enabled: true
    failure-threshold: 5
    reset-timeout: 30000

  async:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 100

  sftp:
    connection-timeout: 30000
    strict-host-key-checking: false

  rest:
    connection-timeout: 10000
    read-timeout: 30000

  file:
    encoding: UTF-8
    buffer-size: 8192
    archive-directory: archive
    error-directory: error
```

### 7.2 Maven Dependency

```xml
<dependency>
    <groupId>com.cmips</groupId>
    <artifactId>integration-hub-framework</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 8. Usage in CMIPS Application

The Integration Hub Framework is specifically designed to support the CMIPS (Case Management Information and Payrolling System) application. Here are key integration points:

### 8.1 Timesheet Processing

```java
@FileType(name = "timesheet", description = "IHSS Provider Timesheet")
public class TimesheetRecord {

    @FileId
    @FileColumn(order = 1, name = "timesheet_id")
    private Long timesheetId;

    @FileColumn(order = 2, name = "provider_id")
    @Validate(notNull = true)
    private Long providerId;

    @FileColumn(order = 3, name = "recipient_id")
    @Validate(notNull = true)
    private Long recipientId;

    @FileColumn(order = 4, name = "hours_worked", format = "#0.00")
    @Validate(min = 0, max = 283)
    private BigDecimal hoursWorked;

    @FileColumn(order = 5, name = "work_week", format = "yyyy-MM-dd")
    private LocalDate workWeek;

    @FileColumn(order = 6, name = "status")
    @Validate(allowedValues = {"DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "PAID"})
    private String status;
}

// Process timesheet files
@Service
public class TimesheetBatchProcessor {

    private final FileRepository<TimesheetRecord> repo =
        FileRepository.forType(TimesheetRecord.class);

    public void processTimesheets(Path inputDir) {
        // Read all timesheet files
        List<Path> files = FileUtil.listFiles(inputDir, "timesheet_*.csv");
        List<TimesheetRecord> allTimesheets = repo.readAll(files, FileFormat.csv().build());

        // Validate
        ValidationResult<TimesheetRecord> validation = repo.validate(allTimesheets);
        if (validation.hasErrors()) {
            handleInvalidTimesheets(validation.getInvalidRecords(), validation.getErrors());
        }

        // Merge and deduplicate
        List<TimesheetRecord> processed = repo.merge(validation.getValidRecords(), List.of())
            .sortBy(TimesheetRecord::getWorkWeek)
            .thenBy(TimesheetRecord::getProviderId)
            .deduplicate()
            .build();

        // Split by status for different processing
        SplitResult<TimesheetRecord> byStatus = repo.split(processed,
            SplitRule.byField(TimesheetRecord::getStatus));

        // Process approved timesheets for payment
        List<TimesheetRecord> approved = byStatus.get("APPROVED");
        processForPayment(approved);
    }
}
```

### 8.2 Case Management File Exchange

```java
@FileType(name = "case-transfer", description = "Case Transfer Record")
public class CaseTransferRecord {

    @FileId
    @FileColumn(order = 1, name = "case_number")
    private String caseNumber;

    @FileColumn(order = 2, name = "county_code")
    @Validate(pattern = "^[A-Z]{3}$")
    private String countyCode;

    @FileColumn(order = 3, name = "recipient_name")
    private String recipientName;

    @FileColumn(order = 4, name = "transfer_type")
    @Validate(allowedValues = {"INTER_COUNTY", "INTRA_COUNTY", "STATE_TRANSFER"})
    private String transferType;

    @FileColumn(order = 5, name = "effective_date", format = "yyyy-MM-dd")
    private LocalDate effectiveDate;
}

// Define destination for state reporting
@Destination(name = "state-reporting", description = "State Controller's Office SFTP")
@Sftp(
    host = "${state.sftp.host}",
    port = 22,
    remotePath = "/incoming/cmips",
    credentials = "state-credentials",
    createDirectory = true
)
public interface StateReportingSystem {}

// Send case transfer reports
SendResult result = caseRepo.send(transferRecords)
    .as(FileFormat.fixedWidth().build())
    .to(StateReportingSystem.class)
    .withFilename(() -> "CMIPS_TRANSFER_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".dat")
    .withRetry(RetryConfig.defaults())
    .execute();
```

### 8.3 Provider/Recipient Data Integration

```java
@InputSource(name = "providerDataReader", description = "Reads provider data from external system")
public class ProviderDataReader extends AbstractFileReader<ProviderRecord> {

    public ProviderDataReader() {
        super("/data/incoming/providers/*.xml");
    }

    @Override
    protected List<ProviderRecord> parseFile(Path file) throws ReadException {
        return FileUtil.parseXmlList(file.toString(), ProviderRecord.class);
    }
}

@Transformer(name = "providerValidator", description = "Validates provider records")
public class ProviderValidator extends AbstractTransformer<ProviderRecord, ProviderRecord> {

    @Override
    protected ProviderRecord doTransform(ProviderRecord input) {
        // Enrich with calculated fields
        input.setActiveIndicator(calculateActiveStatus(input));
        return input;
    }

    @Override
    protected ValidationResult doValidate(ProviderRecord input) {
        List<String> errors = new ArrayList<>();
        if (input.getSsn() == null || !isValidSsn(input.getSsn())) {
            errors.add("Invalid SSN format");
        }
        if (input.getProviderId() == null) {
            errors.add("Provider ID is required");
        }
        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }
}

@OutputDestination(name = "providerDatabase", description = "Saves to CMIPS database")
public class ProviderDatabaseWriter implements IOutputDestination<ProviderRecord> {

    @Autowired
    private ProviderRepository providerRepository;

    @Override
    public SendResult send(ProviderRecord data) throws SendException {
        providerRepository.save(data.toEntity());
        return SendResult.success("Provider saved");
    }
}

// Configure the flow
@Bean
public FlowDefinition providerIntegrationFlow() {
    return FlowDefinition.builder()
        .name("providerIntegrationFlow")
        .description("Imports provider data from external system")
        .addInput("providerDataReader")
        .addTransformer("providerValidator")
        .addOutput("providerDatabase")
        .addOutput("providerAuditLog")
        .failOnError(true)
        .build();
}
```

### 8.4 Payment File Generation

```java
@FileType(name = "eft-payment", description = "EFT Payment File for Bank")
public class EftPaymentRecord {

    @FileId
    @FileColumn(order = 1, name = "transaction_id", length = 15, alignment = Alignment.RIGHT, padChar = '0')
    private Long transactionId;

    @FileColumn(order = 2, name = "routing_number", length = 9)
    @Validate(pattern = "^\\d{9}$")
    private String routingNumber;

    @FileColumn(order = 3, name = "account_number", length = 17)
    private String accountNumber;

    @FileColumn(order = 4, name = "amount", length = 12, format = "#############", alignment = Alignment.RIGHT, padChar = '0')
    @Validate(min = 0.01)
    private BigDecimal amount;

    @FileColumn(order = 5, name = "payee_name", length = 30)
    private String payeeName;

    @FileColumn(order = 6, name = "payment_date", length = 8, format = "yyyyMMdd")
    private LocalDate paymentDate;
}

@Destination(name = "bank-sftp", description = "Bank EFT SFTP Server")
@Sftp(
    host = "${bank.sftp.host}",
    port = 22,
    remotePath = "/eft/incoming",
    credentials = "bank-credentials",
    tempSuffix = ".tmp"
)
public interface BankEftSystem {}

// Generate and send EFT file
public void generateEftFile(List<Payment> approvedPayments) {
    // Convert to EFT format
    FileRepository<Payment> paymentRepo = FileRepository.forType(Payment.class);
    List<EftPaymentRecord> eftRecords = paymentRepo.convert(approvedPayments, EftPaymentRecord.class);

    // Write in fixed-width format and send to bank
    FileRepository<EftPaymentRecord> eftRepo = FileRepository.forType(EftPaymentRecord.class);
    SendResult result = eftRepo.send(eftRecords)
        .as(FileFormat.fixedWidth().lineSeparator("\r\n").build())
        .to(BankEftSystem.class)
        .withFilename(() -> "CMIPS_EFT_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".txt")
        .withMetadata("recordCount", eftRecords.size())
        .withMetadata("totalAmount", calculateTotal(eftRecords))
        .execute();

    if (result.isSuccess()) {
        log.info("EFT file sent successfully: {} records, ${}",
            result.getRecordCount(), result.getMetadata("totalAmount"));
    }
}
```

### 8.5 Work Queue Event Processing

```java
// Using the Integration Flow Engine for event-driven processing
@InputSource(name = "taskEventReader", description = "Reads task events from Kafka")
public class TaskEventReader implements IInputSource<TaskEvent> {

    @Autowired
    private KafkaConsumer<String, TaskEvent> kafkaConsumer;

    @Override
    public List<TaskEvent> read() throws ReadException {
        ConsumerRecords<String, TaskEvent> records = kafkaConsumer.poll(Duration.ofSeconds(10));
        return StreamSupport.stream(records.spliterator(), false)
            .map(ConsumerRecord::value)
            .collect(Collectors.toList());
    }

    @Override
    public void acknowledge() {
        kafkaConsumer.commitSync();
    }
}

@Transformer(name = "taskEventProcessor", description = "Processes task events")
public class TaskEventProcessor extends AbstractTransformer<TaskEvent, Task> {

    @Autowired
    private TaskService taskService;

    @Override
    protected Task doTransform(TaskEvent event) {
        return switch (event.getEventType()) {
            case "CREATED" -> taskService.createTask(event);
            case "UPDATED" -> taskService.updateTask(event);
            case "COMPLETED" -> taskService.completeTask(event);
            default -> throw new TransformationException("Unknown event type: " + event.getEventType());
        };
    }
}
```

---

## 9. Quick Reference

### 9.1 Annotations Summary

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@FileType` | Class | Define a file type schema |
| `@FileColumn` | Field | Define a column in the file |
| `@FileId` | Field | Mark identity field for deduplication |
| `@Validate` | Field/Class | Validation constraints |
| `@MapsFrom` | Field | Map from another file type |
| `@Destination` | Interface | Define output destination |
| `@Sftp` | Interface | SFTP destination config |
| `@HttpApi` | Interface | HTTP API destination config |
| `@InputSource` | Class | Mark as input source component |
| `@Transformer` | Class | Mark as transformer component |
| `@OutputDestination` | Class | Mark as output destination component |
| `@IntegrationFlow` | Class | Mark as flow configuration |
| `@Retry` | Class/Method | Configure retry behavior |
| `@CircuitBreaker` | Class/Method | Configure circuit breaker |

### 9.2 Common Patterns

```java
// Pattern 1: Read -> Validate -> Process -> Write
List<T> records = repo.read(input, format);
ValidationResult<T> valid = repo.validate(records);
valid.throwIfInvalid();
List<T> processed = repo.merge(valid.getValidRecords(), List.of())
    .filter(condition)
    .sortBy(T::getKey)
    .build();
repo.write(processed, output, format);

// Pattern 2: Split and process partitions
SplitResult<T> split = repo.split(records, SplitRule.byField(T::getType));
for (String key : split.getPartitionKeys()) {
    processPartition(key, split.get(key));
}

// Pattern 3: Merge from multiple sources and deduplicate
List<T> all = repo.readAll(List.of(file1, file2, file3), format);
List<T> deduped = repo.merge(all, List.of())
    .deduplicate()
    .sortBy(T::getTimestamp)
    .descending()
    .build();

// Pattern 4: Convert and send
List<Target> converted = sourceRepo.convert(sources, Target.class);
targetRepo.send(converted)
    .as(FileFormat.json().build())
    .to(ExternalApi.class)
    .withRetry(RetryConfig.defaults())
    .execute();
```

### 9.3 Exception Hierarchy

| Exception | When Thrown |
|-----------|-------------|
| `BawException` | Base exception for BAW module |
| `SchemaValidationException` | Invalid @FileType schema |
| `FileParseException` | Error reading/parsing file |
| `FileWriteException` | Error writing file |
| `RecordValidationException` | Records failed validation |
| `ConversionException` | Type conversion error |
| `DestinationException` | Send operation failed |
| `SplitRuleConflictException` | Incompatible split rules |
| `IntegrationException` | Base for flow engine exceptions |
| `ConnectionException` | Connection failure |
| `ReadException` | Read operation failure |
| `TransformationException` | Transformation failure |
| `SendException` | Send operation failure |

---

## Summary

The Integration Hub Framework provides CMIPS with:

1. **Declarative File Processing** - Define file schemas with annotations, type-safe operations
2. **Multi-Format Support** - CSV, JSON, XML, Fixed-Width for various state/county interfaces
3. **Flow Orchestration** - Automated data pipelines for batch processing
4. **Robust Error Handling** - Retry, circuit breaker, and centralized error management
5. **External System Integration** - SFTP and HTTP API destinations for state agencies and banks
6. **Validation Framework** - Field and record-level validation with detailed error reporting
7. **Data Merging and Splitting** - Complex data consolidation and partitioning operations
8. **Async Processing** - Non-blocking execution for high-throughput scenarios

This framework reduces boilerplate code, ensures type safety, and provides a consistent programming model for all integration needs in the CMIPS application.
