# Integration Hub Framework - Architecture Guide

## Overview

The Integration Hub Framework is a comprehensive Java Spring Boot library designed for enterprise batch processing and data integration. It provides a modular, extensible architecture that enables developers to build robust data pipelines with minimal boilerplate code.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Integration Hub Framework                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐         │
│  │  Input Sources  │───▶│  Transformers   │───▶│    Outputs      │         │
│  │  (IInputSource) │    │  (ITransformer) │    │(IOutputDest.)   │         │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘         │
│         ▲                       ▲                       ▲                   │
│         │                       │                       │                   │
│         └───────────────────────┴───────────────────────┘                   │
│                                 │                                            │
│                    ┌────────────┴────────────┐                              │
│                    │   Component Registry    │                              │
│                    │  (ComponentRegistry)    │                              │
│                    └────────────┬────────────┘                              │
│                                 │                                            │
│         ┌───────────────────────┼───────────────────────┐                   │
│         │                       │                       │                   │
│  ┌──────▼──────┐    ┌──────────▼──────────┐    ┌──────▼──────┐            │
│  │  Component  │    │   Flow Executor     │    │   Error     │            │
│  │   Scanner   │    │  (FlowExecutor)     │    │  Handler    │            │
│  └─────────────┘    └──────────┬──────────┘    └─────────────┘            │
│                                │                                            │
│                    ┌───────────▼───────────┐                               │
│                    │  Integration Engine   │                               │
│                    │ (IntegrationEngine)   │                               │
│                    └───────────────────────┘                               │
│                                                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                         Spring Boot Auto-Configuration                       │
│              (FrameworkAutoConfiguration, IntegrationProperties)            │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Interfaces Layer

The framework is built around a set of core interfaces that define the contract for integration components:

#### IInputSource<T>
Defines how data is read from various sources (files, SFTP, APIs, databases).

```java
public interface IInputSource<T> {
    void connect() throws ConnectionException;
    boolean hasData();
    List<T> read() throws ReadException;
    void acknowledge();
    void close();
    boolean isConnected();
}
```

#### ITransformer<I, O>
Defines data transformation operations between input and output types.

```java
public interface ITransformer<I, O> {
    O transform(I input) throws TransformationException;
    ValidationResult validate(I input);
    default <R> ITransformer<I, R> andThen(ITransformer<O, R> after);
}
```

#### IOutputDestination<T>
Defines how data is sent to destinations (files, SFTP, APIs, databases).

```java
public interface IOutputDestination<T> {
    void connect() throws ConnectionException;
    SendResult send(T data) throws SendException;
    boolean verify(SendResult result);
    void close();
}
```

#### IMerger<T>
Defines how data from multiple sources is merged.

```java
public interface IMerger<T> {
    List<T> merge(List<List<T>> sources);
    List<T> merge(List<List<T>> sources, MergeStrategy strategy);
}
```

#### ISorter<T>
Defines sorting operations on data collections.

```java
public interface ISorter<T> {
    List<T> sort(List<T> data, Comparator<T> comparator);
    List<T> sortByField(List<T> data, String fieldName);
}
```

#### IValidator<T>
Defines validation operations.

```java
public interface IValidator<T> {
    ValidationResult validate(T data);
    ValidationResult validateAll(List<T> data);
}
```

### 2. Core Framework

#### ComponentRegistry
Central registry that maintains references to all integration components. Components are stored in thread-safe concurrent maps.

```java
@Component
public class ComponentRegistry {
    private final Map<String, IInputSource<?>> inputSources;
    private final Map<String, ITransformer<?, ?>> transformers;
    private final Map<String, IOutputDestination<?>> outputs;
    private final Map<String, FlowDefinition> flowDefinitions;

    public void registerInputSource(String name, IInputSource<?> source);
    public void registerTransformer(String name, ITransformer<?, ?> transformer);
    public void registerOutputDestination(String name, IOutputDestination<?> destination);
    public void registerFlowDefinition(FlowDefinition definition);
}
```

#### ComponentScanner
Automatically discovers and registers components annotated with framework annotations at application startup.

```java
@Component
public class ComponentScanner implements ApplicationListener<ContextRefreshedEvent> {
    // Scans for @InputSource, @Transformer, @OutputDestination, @IntegrationFlow

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        scanInputSources();
        scanTransformers();
        scanOutputDestinations();
        scanFlowDefinitions();
    }
}
```

#### FlowExecutor
Executes integration flows by orchestrating the input → transform → output pipeline.

```java
@Component
public class FlowExecutor {
    public FlowResult executeFlow(String flowName);
    public FlowResult executeFlow(FlowDefinition definition);
    public List<String> validateFlow(String flowName);

    public static class FlowResult {
        private final boolean success;
        private final String message;
        private final int recordCount;
        private final long durationMs;
        private final Map<String, Object> metadata;
    }
}
```

#### IntegrationEngine
Main orchestration engine providing the public API for flow execution.

```java
@Component
public class IntegrationEngine {
    public FlowResult executeFlow(String flowName);
    public Future<FlowResult> executeFlowAsync(String flowName);
    public Map<String, FlowResult> executeAllFlows();
    public boolean cancelFlow(String flowName);
    public List<String> getRegisteredFlows();
}
```

#### ErrorHandler
Centralized error handling with statistics tracking and custom handler support.

```java
@Component
public class ErrorHandler {
    public void handle(String flowName, Throwable exception);
    public <T extends Throwable> void registerHandler(Class<T> type, BiConsumer<String, T> handler);
    public ErrorStats getStats(String flowName);
    public void clearStats(String flowName);

    public static class ErrorStats {
        private final int errorCount;
        private final List<ErrorRecord> recentErrors;
    }
}
```

### 3. Support Layer

#### SFTP Support
- `SftpConfig` - Configuration builder for SFTP connections
- `SftpClient` - JSch-based SFTP client wrapper
- `SftpUtil` - Static utility methods for SFTP operations

```java
SftpConfig config = SftpConfig.builder()
    .host("sftp.example.com")
    .port(22)
    .username("user")
    .password("secret")
    .build();

try (SftpClient client = new SftpClient(config)) {
    client.connect();
    client.download("/remote/file.csv", "/local/file.csv");
}
```

#### REST Support
- `RestConfig` - Configuration builder for REST clients
- `OAuth2Config` - OAuth2 authentication configuration
- `RestClient` - HTTP client with multiple auth methods
- `RestUtil` - Static utility methods for REST operations

```java
RestConfig config = RestConfig.builder()
    .baseUrl("https://api.example.com")
    .authType(AuthType.OAUTH2)
    .oauth2Config(OAuth2Config.builder()
        .tokenUrl("https://auth.example.com/token")
        .clientId("client-id")
        .clientSecret("client-secret")
        .build())
    .connectionTimeout(10000)
    .readTimeout(30000)
    .build();
```

#### File Support
- `FilePattern` - Glob and regex file matching
- `FileWatcher` - Directory monitoring using NIO WatchService
- `FileUtil` - File operations (read, write, parse, checksum)

```java
// Watch for new files
FileWatcher watcher = new FileWatcher(Paths.get("/data/inbox"), "*.csv");
watcher.start(path -> processFile(path));

// Parse CSV file
List<Map<String, String>> records = FileUtil.parseCsv(path);

// Calculate checksum
String checksum = FileUtil.calculateChecksum(path, ChecksumAlgorithm.SHA256);
```

### 4. Model Layer

#### FlowDefinition
Defines the structure of an integration flow:
- Input source names
- Transformer names (in execution order)
- Output destination names
- Configuration options (failOnError, enabled)

```java
FlowDefinition flow = FlowDefinition.builder()
    .name("paymentFlow")
    .description("Process payment files")
    .addInput("fileReader")
    .addTransformer("validator")
    .addTransformer("enricher")
    .addOutput("apiClient")
    .addOutput("archiveWriter")
    .failOnError(true)
    .enabled(true)
    .build();
```

#### SendResult
Immutable result object for send operations:
- Success/partial/failure status
- Message and metadata
- Record count and timestamp

```java
SendResult result = SendResult.builder()
    .success(true)
    .message("Sent successfully")
    .recordCount(100)
    .metadata("batchId", "12345")
    .build();
```

#### ValidationResult
Immutable validation result:
- Valid/invalid status
- Error messages
- Warning messages

```java
ValidationResult result = ValidationResult.builder()
    .valid(false)
    .addError("Amount is required")
    .addError("Date format is invalid")
    .addWarning("Currency defaulted to USD")
    .build();
```

## Data Flow

```
1. Application Startup
   └── ComponentScanner discovers annotated beans
       └── Components registered in ComponentRegistry

2. Flow Execution Request
   └── IntegrationEngine.executeFlow(flowName)
       └── FlowExecutor retrieves FlowDefinition
           └── Phase 1: Read from Input Sources
               └── IInputSource.connect() → read() → close()
           └── Phase 2: Apply Transformers
               └── ITransformer.transform() for each record
           └── Phase 3: Send to Output Destinations
               └── IOutputDestination.connect() → send() → close()
           └── Phase 4: Acknowledge Sources (on success)
               └── IInputSource.acknowledge()

3. Error Handling
   └── Exceptions caught by FlowExecutor
       └── ErrorHandler.handle() invoked
           └── Statistics updated
           └── Custom handlers invoked
```

## Sequence Diagram

```
┌────────┐    ┌──────────────┐    ┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│ Client │    │IntegrationEng│    │ FlowExecutor│    │  Registry    │    │  Components │
└───┬────┘    └──────┬───────┘    └──────┬──────┘    └──────┬───────┘    └──────┬──────┘
    │                │                    │                  │                   │
    │ executeFlow()  │                    │                  │                   │
    │───────────────▶│                    │                  │                   │
    │                │ executeFlow()      │                  │                   │
    │                │───────────────────▶│                  │                   │
    │                │                    │ getFlowDef()     │                   │
    │                │                    │─────────────────▶│                   │
    │                │                    │◀─────────────────│                   │
    │                │                    │                  │                   │
    │                │                    │ getInputSource() │                   │
    │                │                    │─────────────────▶│                   │
    │                │                    │◀─────────────────│                   │
    │                │                    │                  │                   │
    │                │                    │ connect()        │                   │
    │                │                    │─────────────────────────────────────▶│
    │                │                    │◀─────────────────────────────────────│
    │                │                    │ read()           │                   │
    │                │                    │─────────────────────────────────────▶│
    │                │                    │◀─────────────────────────────────────│
    │                │                    │                  │                   │
    │                │                    │ getTransformer() │                   │
    │                │                    │─────────────────▶│                   │
    │                │                    │◀─────────────────│                   │
    │                │                    │                  │                   │
    │                │                    │ transform()      │                   │
    │                │                    │─────────────────────────────────────▶│
    │                │                    │◀─────────────────────────────────────│
    │                │                    │                  │                   │
    │                │                    │ getOutputDest()  │                   │
    │                │                    │─────────────────▶│                   │
    │                │                    │◀─────────────────│                   │
    │                │                    │                  │                   │
    │                │                    │ send()           │                   │
    │                │                    │─────────────────────────────────────▶│
    │                │                    │◀─────────────────────────────────────│
    │                │                    │                  │                   │
    │                │ FlowResult         │                  │                   │
    │                │◀───────────────────│                  │                   │
    │ FlowResult     │                    │                  │                   │
    │◀───────────────│                    │                  │                   │
    │                │                    │                  │                   │
```

## Design Patterns

### 1. Template Method Pattern
Abstract base classes (AbstractFileReader, AbstractTransformer, etc.) provide the skeleton algorithm while allowing subclasses to override specific steps.

```java
public abstract class AbstractTransformer<I, O> implements ITransformer<I, O> {

    @Override
    public final O transform(I input) throws TransformationException {
        beforeTransform(input);
        O result = doTransform(input);  // Subclass implements this
        afterTransform(input, result);
        return result;
    }

    protected abstract O doTransform(I input) throws TransformationException;

    protected void beforeTransform(I input) { }
    protected void afterTransform(I input, O output) { }
}
```

### 2. Builder Pattern
Configuration classes use fluent builders for readable, type-safe construction:

```java
FlowDefinition.builder()
    .name("paymentFlow")
    .addInput("fileReader")
    .addTransformer("validator")
    .addOutput("apiClient")
    .build();
```

### 3. Strategy Pattern
Enums like MergeStrategy, BackoffStrategy, and CacheStrategy allow pluggable algorithms.

```java
public enum MergeStrategy {
    UNION,           // Include all records from all sources
    INTERSECTION,    // Only records that appear in all sources
    FIRST_WINS,      // Keep first occurrence of duplicates
    LAST_WINS        // Keep last occurrence of duplicates
}
```

### 4. Registry Pattern
ComponentRegistry provides a central lookup service for all components.

### 5. Observer Pattern
ComponentScanner listens to Spring context events for automatic component discovery.

### 6. Chain of Responsibility
Transformers can be chained using the `andThen()` method.

```java
ITransformer<A, D> chain = transformerAtoB
    .andThen(transformerBtoC)
    .andThen(transformerCtoD);
```

## Thread Safety

- ComponentRegistry uses ConcurrentHashMap for thread-safe component storage
- IntegrationEngine uses CachedThreadPool for async flow execution
- ErrorHandler uses atomic counters for statistics
- All model classes (SendResult, ValidationResult, FlowDefinition) are immutable

## Extension Points

1. **Custom Input Sources**: Implement IInputSource or extend AbstractFileReader
2. **Custom Transformers**: Implement ITransformer or extend AbstractTransformer
3. **Custom Outputs**: Implement IOutputDestination or extend AbstractRestClient/AbstractSftpWriter
4. **Custom Error Handlers**: Register handlers via ErrorHandler.registerHandler()
5. **Custom Merge Strategies**: Implement custom logic using MergeUtil methods

## Package Structure

```
com.cmips.integration.framework/
├── annotations/
│   ├── InputSource.java
│   ├── Transformer.java
│   ├── OutputDestination.java
│   ├── IntegrationFlow.java
│   ├── Retry.java
│   └── CircuitBreaker.java
├── base/
│   ├── AbstractFileReader.java
│   ├── AbstractTransformer.java
│   ├── AbstractSftpWriter.java
│   └── AbstractRestClient.java
├── config/
│   ├── IntegrationProperties.java
│   ├── FrameworkAutoConfiguration.java
│   └── ComponentScanConfiguration.java
├── core/
│   ├── ComponentRegistry.java
│   ├── ComponentScanner.java
│   ├── FlowExecutor.java
│   ├── IntegrationEngine.java
│   └── ErrorHandler.java
├── exception/
│   ├── IntegrationException.java
│   ├── ConnectionException.java
│   ├── ReadException.java
│   ├── TransformationException.java
│   └── SendException.java
├── interfaces/
│   ├── IInputSource.java
│   ├── ITransformer.java
│   ├── IOutputDestination.java
│   ├── IMerger.java
│   ├── ISorter.java
│   └── IValidator.java
├── model/
│   ├── FlowDefinition.java
│   ├── SendResult.java
│   ├── ValidationResult.java
│   ├── SourceMetadata.java
│   ├── TransformerMetadata.java
│   ├── DestinationMetadata.java
│   └── BatchResult.java
├── support/
│   ├── SftpConfig.java
│   ├── SftpClient.java
│   ├── RestConfig.java
│   ├── OAuth2Config.java
│   ├── RestClient.java
│   ├── FilePattern.java
│   ├── FileWatcher.java
│   ├── MergeStrategy.java
│   ├── SortOrder.java
│   ├── BackoffStrategy.java
│   ├── CacheStrategy.java
│   └── ChecksumAlgorithm.java
└── util/
    ├── FileUtil.java
    ├── MergeUtil.java
    ├── SortUtil.java
    ├── TransformUtil.java
    ├── SftpUtil.java
    ├── RestUtil.java
    ├── ValidationUtil.java
    └── DatabaseUtil.java
```

## Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `integration.enabled` | Enable/disable the framework | `true` |
| `integration.retry.max-attempts` | Maximum retry attempts | `3` |
| `integration.retry.initial-delay` | Initial retry delay (ms) | `1000` |
| `integration.retry.multiplier` | Backoff multiplier | `2.0` |
| `integration.circuit-breaker.enabled` | Enable circuit breaker | `true` |
| `integration.circuit-breaker.failure-threshold` | Failures before open | `5` |
| `integration.circuit-breaker.reset-timeout` | Reset timeout (ms) | `30000` |
| `integration.async.core-pool-size` | Core thread pool size | `10` |
| `integration.async.max-pool-size` | Max thread pool size | `50` |
| `integration.async.queue-capacity` | Task queue capacity | `100` |
| `integration.sftp.connection-timeout` | SFTP connect timeout (ms) | `30000` |
| `integration.rest.connection-timeout` | REST connect timeout (ms) | `10000` |
| `integration.rest.read-timeout` | REST read timeout (ms) | `30000` |
