# Integration Hub Framework

A comprehensive Java Spring Boot framework library for enterprise batch processing and data integration.

## Overview

The Integration Hub Framework provides a robust foundation for building data integration solutions that need to:

- Read data from multiple sources (files, SFTP, databases, REST APIs)
- Transform and validate data through configurable pipelines
- Send data to multiple destinations with error handling and retries
- Merge, sort, and process data from multiple sources
- Handle errors gracefully with circuit breakers and retry mechanisms

## Features

- **Component-Based Architecture**: Define input sources, transformers, and output destinations as Spring beans
- **Annotation-Driven Configuration**: Use annotations to mark and configure integration components
- **Automatic Component Discovery**: Components are automatically discovered and registered at startup
- **Flow Orchestration**: Define and execute integration flows with multiple inputs, transformers, and outputs
- **Error Handling**: Centralized error handling with circuit breaker and retry support
- **SFTP Support**: Built-in SFTP client for file transfers
- **REST API Support**: HTTP client with OAuth2, Basic Auth, and API Key authentication
- **File Processing**: Utilities for reading, parsing, and processing various file formats
- **Data Merging**: Merge data from multiple sources with various strategies
- **Async Execution**: Execute flows asynchronously with configurable thread pools

## Requirements

- Java 17 or higher
- Spring Boot 3.2.x
- Maven 3.6+

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.cmips</groupId>
    <artifactId>integration-hub-framework</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### 1. Create an Input Source

```java
@InputSource(name = "paymentFileReader", description = "Reads payment files")
public class PaymentFileReader extends AbstractFileReader<Payment> {

    public PaymentFileReader() {
        super("/data/payments/*.csv");
    }

    @Override
    protected List<Payment> parseFile(Path file) throws ReadException {
        return FileUtil.parseCsv(file.toString(), true).stream()
            .map(this::mapToPayment)
            .toList();
    }

    private Payment mapToPayment(Map<String, String> row) {
        return new Payment(
            row.get("id"),
            new BigDecimal(row.get("amount")),
            row.get("currency")
        );
    }
}
```

### 2. Create a Transformer

```java
@Transformer(name = "paymentValidator", description = "Validates payments")
public class PaymentValidator extends AbstractTransformer<Payment, Payment> {

    @Override
    protected Payment doTransform(Payment payment) throws TransformationException {
        // Add validation timestamp
        payment.setValidatedAt(Instant.now());
        return payment;
    }

    @Override
    protected ValidationResult doValidate(Payment payment) {
        List<String> errors = new ArrayList<>();

        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Amount must be positive");
        }

        return errors.isEmpty()
            ? ValidationResult.valid()
            : ValidationResult.invalid(errors);
    }
}
```

### 3. Create an Output Destination

```java
@OutputDestination(name = "paymentApiClient", description = "Sends payments to API")
public class PaymentApiClient extends AbstractRestClient<Payment> {

    public PaymentApiClient(RestConfig config) {
        super(config);
    }

    @Override
    protected SendResult doSend(Payment payment) throws SendException {
        PaymentResponse response = getClient().post(
            "/api/payments",
            payment,
            PaymentResponse.class
        );

        return SendResult.builder()
            .success(response.isSuccess())
            .message(response.getMessage())
            .metadata("paymentId", response.getPaymentId())
            .build();
    }
}
```

### 4. Define a Flow

```java
@Configuration
public class PaymentFlowConfig {

    @Bean
    public FlowDefinition paymentProcessingFlow() {
        return FlowDefinition.builder()
            .name("paymentProcessingFlow")
            .description("Processes payment files and sends to API")
            .input("paymentFileReader")
            .transformer("paymentValidator")
            .output("paymentApiClient")
            .enabled(true)
            .failOnError(true)
            .build();
    }
}
```

### 5. Execute the Flow

```java
@Service
public class PaymentService {

    @Autowired
    private IntegrationEngine engine;

    public void processPayments() {
        FlowExecutor.FlowResult result = engine.executeFlow("paymentProcessingFlow");

        if (result.isSuccess()) {
            log.info("Processed {} payments in {}ms",
                result.getRecordCount(), result.getDurationMs());
        } else {
            log.error("Flow failed: {}", result.getMessage());
        }
    }
}
```

## Configuration

Configure the framework in your `application.yml`:

```yaml
integration:
  enabled: true

  retry:
    max-attempts: 3
    initial-delay: 1000
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
```

## Core Components

### Interfaces

| Interface | Description |
|-----------|-------------|
| `IInputSource<T>` | Reads data from a source |
| `ITransformer<I, O>` | Transforms data from one type to another |
| `IOutputDestination<T>` | Sends data to a destination |
| `IMerger<T>` | Merges data from multiple sources |
| `ISorter<T>` | Sorts data collections |
| `IValidator<T>` | Validates data |

### Annotations

| Annotation | Description |
|------------|-------------|
| `@InputSource` | Marks a class as an input source component |
| `@Transformer` | Marks a class as a transformer component |
| `@OutputDestination` | Marks a class as an output destination component |
| `@IntegrationFlow` | Marks a class as a flow configuration |
| `@Retry` | Configures retry behavior |
| `@CircuitBreaker` | Configures circuit breaker behavior |

### Utilities

| Utility | Description |
|---------|-------------|
| `FileUtil` | File reading, parsing, and manipulation |
| `MergeUtil` | Data merging and combining |
| `SortUtil` | Data sorting and ordering |
| `TransformUtil` | JSON/XML transformation |
| `SftpUtil` | SFTP operations |
| `RestUtil` | REST API operations |
| `ValidationUtil` | Data validation |
| `DatabaseUtil` | Database operations |

## Base Classes

The framework provides abstract base classes for common implementations:

- `AbstractFileReader<T>` - Base class for file-based input sources
- `AbstractTransformer<I, O>` - Base class for transformers
- `AbstractSftpWriter<T>` - Base class for SFTP output destinations
- `AbstractRestClient<T>` - Base class for REST API output destinations

## Error Handling

The framework provides centralized error handling through the `ErrorHandler` class:

```java
@Autowired
private ErrorHandler errorHandler;

// Register custom handlers
errorHandler.registerHandler(ConnectionException.class, (flowName, ex) -> {
    alertService.sendConnectionAlert(flowName, ex);
});

// Get error statistics
ErrorHandler.ErrorStats stats = errorHandler.getStats("paymentFlow");
log.info("Total errors: {}", stats.getErrorCount());
```

## Flow Execution

### Synchronous Execution

```java
FlowExecutor.FlowResult result = engine.executeFlow("myFlow");
```

### Asynchronous Execution

```java
Future<FlowExecutor.FlowResult> future = engine.executeFlowAsync("myFlow");
// ... do other work ...
FlowExecutor.FlowResult result = future.get();
```

### Execute All Flows

```java
Map<String, FlowExecutor.FlowResult> results = engine.executeAllFlows();
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/cmips/integration-hub-framework.git
cd integration-hub-framework

# Build with Maven
mvn clean install

# Run tests
mvn test
```

## Project Structure

```
integration-hub-framework/
├── src/
│   ├── main/
│   │   ├── java/com/cmips/integration/framework/
│   │   │   ├── annotations/     # Framework annotations
│   │   │   ├── base/            # Abstract base classes
│   │   │   ├── config/          # Auto-configuration
│   │   │   ├── core/            # Core framework components
│   │   │   ├── exception/       # Exception hierarchy
│   │   │   ├── interfaces/      # Core interfaces
│   │   │   ├── model/           # Data models
│   │   │   ├── support/         # Support classes
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── META-INF/spring/ # Auto-configuration
│   │       └── application-framework.yml
│   └── test/
│       └── java/com/cmips/integration/framework/
│           ├── core/            # Core tests
│           └── util/            # Utility tests
├── pom.xml
└── README.md
```

## License

This project is licensed under the Apache License 2.0.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Support

For questions and support, please open an issue on GitHub.
