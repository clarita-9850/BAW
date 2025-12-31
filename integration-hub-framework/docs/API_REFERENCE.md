# Integration Hub Framework - API Reference

## Table of Contents

1. [Core Interfaces](#core-interfaces)
2. [Annotations](#annotations)
3. [Model Classes](#model-classes)
4. [Utility Classes](#utility-classes)
5. [Support Classes](#support-classes)
6. [Base Classes](#base-classes)
7. [Framework Core](#framework-core)
8. [Configuration](#configuration)

---

## Core Interfaces

### IInputSource<T>

Interface for reading data from any source.

```java
package com.cmips.integration.framework.interfaces;

public interface IInputSource<T> {

    // Connection lifecycle
    void connect() throws ConnectionException;
    void close();
    boolean isConnected();

    // Data reading
    boolean hasData();
    List<T> read() throws ReadException;
    List<T> readBatch(int batchSize) throws ReadException;
    Optional<T> readOne() throws ReadException;

    // Acknowledgment
    void acknowledge();
    void rollback();

    // Metadata
    SourceMetadata getMetadata();
    String getName();
    boolean supportsBatchReading();
    long estimateCount();
}
```

### ITransformer<I, O>

Interface for data transformation operations.

```java
package com.cmips.integration.framework.interfaces;

public interface ITransformer<I, O> {

    // Transformation
    O transform(I input) throws TransformationException;
    List<O> transformAll(List<I> inputs) throws TransformationException;

    // Validation
    ValidationResult validate(I input);
    ValidationResult validateAll(List<I> inputs);

    // Lifecycle
    void initialize();
    void destroy();

    // Metadata
    TransformerMetadata getMetadata();
    String getName();
    boolean isStateless();

    // Composition
    <R> ITransformer<I, R> andThen(ITransformer<O, R> after);
}
```

### IOutputDestination<T>

Interface for sending data to a destination.

```java
package com.cmips.integration.framework.interfaces;

public interface IOutputDestination<T> {

    // Connection lifecycle
    void connect() throws ConnectionException;
    void close();
    boolean isConnected();

    // Data sending
    SendResult send(T data) throws SendException;
    SendResult sendBatch(List<T> batch) throws SendException;
    boolean verify(SendResult result);

    // Transaction support
    void commit();
    void rollback();
    boolean supportsTransactions();
    void beginTransaction();

    // Metadata
    String getName();
}
```

### IMerger<T>

Interface for merging data from multiple sources.

```java
package com.cmips.integration.framework.interfaces;

public interface IMerger<T> {

    List<T> merge(List<List<T>> sources);
    List<T> merge(List<List<T>> sources, MergeStrategy strategy);
    MultiSourceData<T> mergeWithMetadata(List<List<T>> sources);
}
```

### ISorter<T>

Interface for sorting data collections.

```java
package com.cmips.integration.framework.interfaces;

public interface ISorter<T> {

    List<T> sort(List<T> data);
    List<T> sort(List<T> data, Comparator<T> comparator);
    boolean isSorted(List<T> data);
    boolean isSorted(List<T> data, Comparator<T> comparator);
}
```

### IValidator<T>

Interface for data validation.

```java
package com.cmips.integration.framework.interfaces;

public interface IValidator<T> {

    ValidationResult validate(T data);
    ValidationResult validateAll(List<T> data);
    boolean isValid(T data);

    // Static factory
    static <T> IValidator<T> of(Predicate<T> predicate, String errorMessage);
}
```

---

## Annotations

### @InputSource

Marks a class as an input source component.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface InputSource {
    String name() default "";           // Component name
    String description() default "";    // Description
    int order() default 0;              // Processing order
    boolean enabled() default true;     // Enable/disable
}
```

### @Transformer

Marks a class as a transformer component.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Transformer {
    String name() default "";           // Component name
    String description() default "";    // Description
    Class<?> inputType() default Object.class;   // Input type
    Class<?> outputType() default Object.class;  // Output type
    boolean cache() default false;      // Enable caching
    boolean enabled() default true;     // Enable/disable
}
```

### @OutputDestination

Marks a class as an output destination component.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface OutputDestination {
    String name() default "";           // Component name
    String description() default "";    // Description
    int retry() default 0;              // Retry attempts
    boolean required() default true;    // Required for flow success
    boolean enabled() default true;     // Enable/disable
}
```

### @IntegrationFlow

Marks a class as a flow configuration.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface IntegrationFlow {
    String name() default "";           // Flow name
    String description() default "";    // Description
    String cron() default "";           // Cron schedule
    boolean enabled() default true;     // Enable/disable
}
```

### @Retry

Configures retry behavior.

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    int maxAttempts() default 3;        // Max retry attempts
    long initialDelay() default 1000;   // Initial delay (ms)
    long maxDelay() default 30000;      // Max delay (ms)
    double multiplier() default 2.0;    // Backoff multiplier
    BackoffStrategy backoff() default BackoffStrategy.EXPONENTIAL;
    Class<? extends Throwable>[] retryOn() default {Exception.class};
    Class<? extends Throwable>[] noRetryOn() default {};
}
```

### @CircuitBreaker

Configures circuit breaker behavior.

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CircuitBreaker {
    int failureThreshold() default 5;   // Failures to open
    long resetTimeout() default 30000;  // Reset timeout (ms)
    int successThreshold() default 3;   // Successes to close
    Class<? extends Throwable>[] tripOn() default {Exception.class};
}
```

---

## Model Classes

### FlowDefinition

Defines the structure of an integration flow.

```java
FlowDefinition flow = FlowDefinition.builder()
    .name("paymentProcessingFlow")
    .description("Processes payment files")
    .addInput("fileReader")
    .addInput("sftpReader")
    .addTransformer("validator")
    .addTransformer("enricher")
    .addOutput("apiClient")
    .addOutput("databaseWriter")
    .failOnError(true)
    .enabled(true)
    .property("batchSize", 100)
    .build();

// Accessors
String name = flow.getName();
List<String> inputs = flow.getInputs();
List<String> transformers = flow.getTransformers();
List<String> outputs = flow.getOutputs();
boolean failOnError = flow.isFailOnError();
boolean enabled = flow.isEnabled();
```

### SendResult

Represents the result of a send operation.

```java
// Static factories
SendResult success = SendResult.success("File uploaded");
SendResult successWithCount = SendResult.success("Uploaded", 150);
SendResult failure = SendResult.failure("Connection timeout");
SendResult partial = SendResult.partial("Uploaded 8 of 10 records");

// Builder
SendResult result = SendResult.builder()
    .success(true)
    .message("Batch uploaded")
    .recordsSent(100)
    .metadata("fileName", "payments.xml")
    .metadata("checksum", "abc123")
    .build();

// Accessors
boolean success = result.isSuccess();
boolean partial = result.isPartial();
boolean failure = result.isFailure();
String message = result.getMessage();
long recordsSent = result.getRecordsSent();
Optional<String> fileName = result.getMetadata("fileName", String.class);
```

### ValidationResult

Represents validation outcome.

```java
// Static factories
ValidationResult valid = ValidationResult.valid();
ValidationResult invalid = ValidationResult.invalid("Field X is required");
ValidationResult invalidMultiple = ValidationResult.invalid(List.of("Error 1", "Error 2"));
ValidationResult withWarnings = ValidationResult.valid(List.of("Warning 1"));

// Accessors
boolean isValid = result.isValid();
List<String> errors = result.getErrors();
List<String> warnings = result.getWarnings();
boolean hasWarnings = result.hasWarnings();
```

### MultiSourceData<T>

Container for data from multiple sources.

```java
MultiSourceData<Payment> data = new MultiSourceData<>();
data.addSource("fileReader", payments1);
data.addSource("sftpReader", payments2);

List<Payment> allData = data.getAllData();
List<Payment> fromFile = data.getSourceData("fileReader");
Set<String> sourceNames = data.getSourceNames();
int sourceCount = data.getSourceCount();
```

### SourceMetadata

Metadata about an input source.

```java
SourceMetadata metadata = SourceMetadata.builder()
    .name("PaymentFileReader")
    .description("Reads payment CSV files")
    .recordCount(1500)
    .lastModified(Instant.now())
    .attribute("encoding", "UTF-8")
    .attribute("delimiter", ",")
    .build();
```

### TransformerMetadata

Metadata about a transformer.

```java
TransformerMetadata metadata = TransformerMetadata.builder()
    .name("PaymentValidator")
    .description("Validates payment records")
    .inputType(RawPayment.class)
    .outputType(ValidatedPayment.class)
    .stateless(true)
    .build();
```

---

## Utility Classes

### FileUtil

File operations utility.

```java
// Reading
String content = FileUtil.readFile(path);
String content = FileUtil.readFile(path, charset);
List<String> lines = FileUtil.readLines(path);

// Writing
FileUtil.writeFile(path, content);
FileUtil.writeFile(path, content, charset);

// Parsing
List<Map<String, String>> csvData = FileUtil.parseCsv(path);
List<Map<String, String>> csvData = FileUtil.parseCsv(path, delimiter);
<T> T xmlObject = FileUtil.parseXml(path, MyClass.class);

// File operations
FileUtil.copyFile(source, target);
FileUtil.moveFile(source, target);
boolean deleted = FileUtil.deleteIfExists(path);

// Utilities
List<Path> files = FileUtil.listFiles(directory, "*.csv");
Path file = FileUtil.findFile(directory, "payment_*.xml");
String checksum = FileUtil.calculateChecksum(path, ChecksumAlgorithm.SHA256);
String extension = FileUtil.getExtension(path);
String baseName = FileUtil.getBaseName(path);
Path tempFile = FileUtil.createTempFile("prefix", ".tmp");

// File watching
FileWatcher watcher = FileUtil.watchDirectory(directory, FilePattern.glob("*.xml"));
```

### MergeUtil

Data merging utility.

```java
// Merge by concatenation
List<T> merged = MergeUtil.merge(List.of(list1, list2, list3));

// Merge with deduplication (last wins)
List<T> unique = MergeUtil.mergeUnique(List.of(list1, list2), Item::getId);

// Merge with deduplication (first wins)
List<T> unique = MergeUtil.mergeUniqueFirst(List.of(list1, list2), Item::getId);

// Merge and sort
List<T> sorted = MergeUtil.mergeAndSort(List.of(list1, list2), comparator);

// Merge two sorted lists efficiently
List<T> merged = MergeUtil.mergeSorted(list1, list2, comparator);

// Interleave (round-robin)
List<T> interleaved = MergeUtil.interleave(List.of(list1, list2));

// Partition into chunks
List<List<T>> chunks = MergeUtil.partition(list, chunkSize);
```

### SortUtil

Sorting utility.

```java
// Sort by field using reflection
List<T> sorted = SortUtil.sortByField(data, "fieldName");
List<T> sorted = SortUtil.sortByField(data, "fieldName", ascending);

// Sort by multiple fields
List<T> sorted = SortUtil.sortByFields(data, "field1", "field2");

// Sort with comparator
List<T> sorted = SortUtil.sort(data, comparator);

// Sort comparable elements
List<T> ascending = SortUtil.sortAscending(data);
List<T> descending = SortUtil.sortDescending(data);

// Top/bottom N elements
List<T> top = SortUtil.topN(data, 10, comparator);
List<T> bottom = SortUtil.bottomN(data, 10, comparator);

// Check if sorted
boolean sorted = SortUtil.isSorted(data, comparator);

// Reverse
List<T> reversed = SortUtil.reverse(data);
```

### TransformUtil

JSON/XML transformation utility.

```java
// JSON operations
String json = TransformUtil.toJson(object);
String prettyJson = TransformUtil.toJsonPretty(object);
<T> T object = TransformUtil.fromJson(json, MyClass.class);
<T> List<T> list = TransformUtil.fromJsonList(json, MyClass.class);

// XML operations
String xml = TransformUtil.toXml(object);
<T> T object = TransformUtil.fromXml(xml, MyClass.class);

// Field mapping
Map<String, Object> mapped = TransformUtil.mapFields(source, fieldMapping);

// Type conversion
<T> T converted = TransformUtil.convert(value, targetClass);
```

### SftpUtil

SFTP operations utility.

```java
// Create client
SftpClient client = SftpUtil.createClient(config);

// File operations
SftpUtil.upload(client, localPath, remoteDir, remoteFileName);
SftpUtil.download(client, remotePath, localPath);
List<RemoteFile> files = SftpUtil.listFiles(client, remoteDir, pattern);
boolean exists = SftpUtil.exists(client, remotePath);
SftpUtil.delete(client, remotePath);
SftpUtil.rename(client, oldPath, newPath);

// Upload with verification
UploadResult result = SftpUtil.uploadWithVerification(
    client, localPath, remoteDir, remoteFileName, ChecksumAlgorithm.SHA256);
```

### RestUtil

REST operations utility.

```java
// Create client
RestClient client = RestUtil.createClient(config);

// HTTP methods
<T> T response = RestUtil.get(client, "/api/resource", ResponseType.class);
<T> T response = RestUtil.post(client, "/api/resource", requestBody, ResponseType.class);
<T> T response = RestUtil.put(client, "/api/resource", requestBody, ResponseType.class);
RestUtil.delete(client, "/api/resource");

// OAuth2
String token = RestUtil.getOAuth2Token(oAuth2Config);
```

### ValidationUtil

Validation utility.

```java
// Schema validation
ValidationResult result = ValidationUtil.validateJson(json, schemaPath);
ValidationResult result = ValidationUtil.validateXml(xml, xsdPath);

// Field validation
ValidationResult result = ValidationUtil.validate(object, validationRules);
List<String> missing = ValidationUtil.checkRequiredFields(object, requiredFields);

// Common validators
boolean valid = ValidationUtil.isNotBlank(string);
boolean valid = ValidationUtil.isValidEmail(email);
boolean valid = ValidationUtil.isValidDate(date, pattern);
boolean valid = ValidationUtil.isInRange(number, min, max);
```

### DatabaseUtil

Database operations utility.

```java
// Query execution
List<Map<String, Object>> results = DatabaseUtil.executeQuery(
    dataSource, "SELECT * FROM payments WHERE status = :status",
    Map.of("status", "PENDING"));

// Single result
Optional<Map<String, Object>> result = DatabaseUtil.executeSingleQuery(
    dataSource, "SELECT * FROM payment WHERE id = :id", Map.of("id", 123));

// Batch operations
int[] counts = DatabaseUtil.executeBatch(
    dataSource, "INSERT INTO payments (id, amount) VALUES (:id, :amount)",
    batchParams);

// Stored procedures
Map<String, Object> output = DatabaseUtil.executeStoredProcedure(
    dataSource, "process_payment", inputParams, outputParams);

// Transaction support
DatabaseUtil.executeInTransaction(dataSource, connection -> {
    // multiple operations
});
```

---

## Support Classes

### SftpConfig

SFTP connection configuration.

```java
SftpConfig config = SftpConfig.builder()
    .host("sftp.example.com")
    .port(22)
    .username("user")
    .password("password")                    // or
    .privateKeyPath("/path/to/key")
    .privateKeyPassphrase("passphrase")
    .knownHostsPath("/path/to/known_hosts")
    .strictHostKeyChecking(false)
    .connectionTimeout(30000)
    .build();
```

### RestConfig

REST client configuration.

```java
RestConfig config = RestConfig.builder()
    .baseUrl("https://api.example.com")
    .connectionTimeout(10000)
    .readTimeout(30000)
    .followRedirects(true)
    // Authentication options
    .basicAuth("username", "password")       // or
    .bearerToken("token")                    // or
    .apiKey("X-API-Key", "key-value")        // or
    .oauth2(oauth2Config)
    // Headers
    .header("Accept", "application/json")
    .header("Content-Type", "application/json")
    .build();
```

### OAuth2Config

OAuth2 authentication configuration.

```java
OAuth2Config oauth2 = OAuth2Config.builder()
    .tokenUrl("https://auth.example.com/oauth/token")
    .clientId("client-id")
    .clientSecret("client-secret")
    .grantType("client_credentials")
    .scope("read write")
    .build();
```

### FilePattern

File matching pattern.

```java
// Glob pattern
FilePattern pattern = FilePattern.glob("*.csv");
FilePattern pattern = FilePattern.glob("payment_*.xml");

// Regex pattern
FilePattern pattern = FilePattern.regex("payment_\\d{8}\\.xml");

// Usage
boolean matches = pattern.matches(path);
List<Path> matched = files.stream().filter(pattern::matches).toList();
```

### FileWatcher

Directory monitoring.

```java
FileWatcher watcher = new FileWatcher(directory, FilePattern.glob("*.xml"));
watcher.start();

// Poll for new files
List<Path> newFiles = watcher.pollNewFiles();
List<Path> newFiles = watcher.pollNewFiles(Duration.ofSeconds(10));

// With callback
watcher.onNewFile(path -> processFile(path));

watcher.stop();
```

---

## Base Classes

### AbstractFileReader<T>

Base class for file-based input sources.

```java
@InputSource(name = "paymentReader")
public class PaymentFileReader extends AbstractFileReader<Payment> {

    public PaymentFileReader() {
        super("/data/input/*.csv");
    }

    @Override
    protected List<Payment> parseFile(Path file) throws ReadException {
        return FileUtil.parseCsv(file).stream()
            .map(this::mapToPayment)
            .toList();
    }

    @Override
    protected Path getArchiveDirectory() {
        return Path.of("/data/archive");
    }
}
```

### AbstractTransformer<I, O>

Base class for transformers.

```java
@Transformer(name = "paymentValidator")
public class PaymentValidator extends AbstractTransformer<RawPayment, ValidatedPayment> {

    @Override
    protected ValidatedPayment doTransform(RawPayment input) {
        return ValidatedPayment.from(input);
    }

    @Override
    protected ValidationResult doValidate(RawPayment input) {
        List<String> errors = new ArrayList<>();
        if (input.getAmount() == null) {
            errors.add("Amount is required");
        }
        return errors.isEmpty()
            ? ValidationResult.valid()
            : ValidationResult.invalid(errors);
    }
}
```

### AbstractSftpWriter<T>

Base class for SFTP output destinations.

```java
@OutputDestination(name = "sftpUploader")
public class PaymentSftpWriter extends AbstractSftpWriter<PaymentBatch> {

    public PaymentSftpWriter(SftpConfig config) {
        super(config, "/remote/output");
    }

    @Override
    protected byte[] formatData(PaymentBatch data) {
        return TransformUtil.toXml(data).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected String generateFileName(PaymentBatch data) {
        return "payments_" + data.getBatchId() + ".xml";
    }
}
```

### AbstractRestClient<T>

Base class for REST API output destinations.

```java
@OutputDestination(name = "paymentApi")
public class PaymentApiClient extends AbstractRestClient<Payment> {

    public PaymentApiClient(RestConfig config) {
        super(config);
    }

    @Override
    protected SendResult doSend(Payment payment) throws SendException {
        ApiResponse response = getClient().post(
            "/api/payments", payment, ApiResponse.class);

        return SendResult.builder()
            .success(response.isSuccess())
            .message(response.getMessage())
            .metadata("paymentId", response.getPaymentId())
            .build();
    }
}
```

---

## Framework Core

### IntegrationEngine

Main entry point for flow execution.

```java
@Autowired
private IntegrationEngine engine;

// Synchronous execution
FlowResult result = engine.executeFlow("paymentFlow");

// Asynchronous execution
Future<FlowResult> future = engine.executeFlowAsync("paymentFlow");
FlowResult result = future.get();

// Execute all enabled flows
Map<String, FlowResult> results = engine.executeAllFlows();

// Flow management
boolean running = engine.isFlowRunning("paymentFlow");
Collection<String> runningFlows = engine.getRunningFlows();
boolean cancelled = engine.cancelFlow("paymentFlow");

// Validation
List<String> errors = engine.validateFlow("paymentFlow");

// Statistics
Map<String, Object> stats = engine.getStatistics();

// Lifecycle
boolean ready = engine.isReady();
engine.shutdown();
```

### FlowResult

Result of flow execution.

```java
FlowResult result = engine.executeFlow("paymentFlow");

String flowName = result.getFlowName();
boolean success = result.isSuccess();
boolean partial = result.isPartial();
int recordCount = result.getRecordCount();
Duration duration = result.getDuration();
long durationMs = result.getDurationMs();
String message = result.getMessage();
```

### ErrorHandler

Centralized error handling.

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
String lastMessage = stats.getLastErrorMessage();

// Recent errors
List<ErrorRecord> recent = errorHandler.getRecentErrors();
List<ErrorRecord> flowErrors = errorHandler.getRecentErrors("paymentFlow");

// Clear statistics
errorHandler.clearStats();
errorHandler.clearStats("paymentFlow");
```

---

## Configuration

### IntegrationProperties

Configuration properties (application.yml):

```yaml
integration:
  enabled: true
  scan-packages:
    - com.example.flows
    - com.example.integrations

  retry:
    max-attempts: 3
    initial-delay: 1000
    max-delay: 30000
    multiplier: 2.0
    jitter: true

  circuit-breaker:
    enabled: true
    failure-threshold: 5
    reset-timeout: 30000
    success-threshold: 3

  async:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 100
    keep-alive-seconds: 60
    thread-name-prefix: integration-async-

  sftp:
    connection-timeout: 30000
    read-timeout: 60000
    strict-host-key-checking: false
    default-port: 22

  rest:
    connection-timeout: 10000
    read-timeout: 30000
    max-connections-per-route: 20
    max-total-connections: 100
    follow-redirects: true
    user-agent: IntegrationHub/1.0

  file:
    encoding: UTF-8
    buffer-size: 8192
    archive-directory: archive
    error-directory: error
    delete-after-process: false
    watch-poll-interval: 10s

  custom:
    key1: value1
    key2: value2
```

### Programmatic Configuration

```java
@Configuration
public class IntegrationConfig {

    @Bean
    public FlowDefinition paymentFlow() {
        return FlowDefinition.builder()
            .name("paymentFlow")
            .addInput("fileReader")
            .addTransformer("validator")
            .addOutput("apiClient")
            .build();
    }

    @Bean
    public SftpConfig sftpConfig() {
        return SftpConfig.builder()
            .host("sftp.example.com")
            .username("user")
            .password("password")
            .build();
    }
}
```
