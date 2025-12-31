# BAW Framework - Technical Specification

**Version**: 1.0.0  
**Status**: Draft  
**Last Updated**: 2024-12-24  

---

## 1. Executive Summary

BAW (Business Application Warehouse) Framework is a pure Java library that provides a declarative, annotation-driven approach to file processing operations. It enables type-safe file handling with operations for reading, writing, merging, splitting, converting, and transmitting files to external destinations.

The framework draws inspiration from JPA's programming model - using annotations to define schemas and repositories to perform operations - applied to the domain of file processing rather than database persistence.

---

## 2. Goals and Non-Goals

### 2.1 Goals

1. **Type Safety**: Prevent accidental merging of incompatible file types at compile time
2. **Format Agnosticism**: Same type definition works with CSV, Fixed-Width, XML, JSON
3. **Declarative Schema Definition**: Define file structure using annotations on POJOs
4. **Declarative Field Mapping**: Define cross-type conversions using annotations
5. **Fluent Operations API**: Chainable, readable API for merge, split, send operations
6. **Extensibility**: Allow custom formats, destinations, and validators via SPI
7. **Zero Framework Dependencies**: Pure Java library usable anywhere

### 2.2 Non-Goals

1. Database integration (this is not an ORM)
2. Real-time streaming (batch processing focus)
3. Distributed processing (single JVM)
4. GUI or visual tooling
5. Code generation at build time (runtime reflection only for v1)

---

## 3. Terminology

| Term | Definition |
|------|------------|
| **File Type** | A schema definition represented by an annotated POJO class |
| **File Format** | Serialization format (CSV, Fixed-Width, XML, JSON) |
| **Record** | A single instance of a File Type (one row/element) |
| **Repository** | Interface providing operations on a specific File Type |
| **Destination** | Target for file transmission (SFTP server, HTTP endpoint) |
| **Split Rule** | Logic for partitioning records into multiple groups |
| **Merge** | Combining multiple record sets into one with optional sorting/deduplication |
| **Conversion** | Transforming records from one File Type to another |

---

## 4. Functional Requirements

### 4.1 Schema Definition

#### FR-4.1.1: Type Annotation
The framework SHALL allow marking a POJO class as a File Type using an annotation that specifies:
- Unique type name (required)
- Description (optional)
- Version (optional)

#### FR-4.1.2: Column Annotation
The framework SHALL allow marking POJO fields as columns using an annotation that specifies:
- Column name (defaults to field name)
- Order/position in the record (required)
- Length for fixed-width formats (optional, -1 for variable)
- Padding character and alignment for fixed-width (optional)
- Nullability (optional, default true)
- Format pattern for dates/numbers (optional)
- Default value (optional)
- XML-specific options: attribute vs element, XPath for nesting (optional)

#### FR-4.1.3: Identity Annotation
The framework SHALL allow marking one or more fields as identity fields for deduplication purposes.

#### FR-4.1.4: Supported Field Types
The framework SHALL support these Java types for columns:
- String
- Integer, Long, Short, Byte (and primitives)
- BigDecimal, BigInteger
- Double, Float (and primitives)
- Boolean (and primitive)
- LocalDate, LocalDateTime, LocalTime
- Instant, ZonedDateTime
- Enum types

#### FR-4.1.5: Schema Validation
The framework SHALL validate schemas at repository creation time and report:
- Missing required annotations
- Invalid order values (duplicates, gaps)
- Invalid length values for fixed-width
- Unsupported field types
- Invalid format patterns

---

### 4.2 Repository Operations

#### FR-4.2.1: Repository Creation
The framework SHALL provide a factory method to create a repository for any valid File Type class.

#### FR-4.2.2: Read Operations
The repository SHALL support reading records from:
- File path
- Input stream
- Multiple file paths (combined into single list)

The repository SHALL support reading in these modes:
- Eager (all records into memory)
- Lazy/streaming (for large files)

#### FR-4.2.3: Write Operations
The repository SHALL support writing records to:
- File path
- Output stream
- Byte array (in-memory)

#### FR-4.2.4: Merge Operations
The repository SHALL provide a merge operation that:
- Combines multiple record lists into one
- Supports sorting by one or more fields
- Supports ascending and descending sort order
- Supports deduplication using identity fields
- Supports deduplication using custom key extractor
- Supports keeping first or last occurrence when deduplicating
- Supports filtering during merge
- Supports transformation during merge
- Returns merged records and optional statistics

#### FR-4.2.5: Split Operations
The repository SHALL provide a split operation that partitions records using rules:
- **By Field Value**: One partition per unique value of a field
- **By Count**: Fixed number of records per partition
- **By Size**: Approximate byte size per partition
- **By Predicate**: Two partitions based on true/false condition
- **By Multiple Predicates**: N partitions based on labeled conditions

Split rules SHALL be chainable (apply sequentially).

The framework SHALL detect and report contradictory split rules.

The split result SHALL provide:
- Access to partitions by key
- Record counts per partition
- Ability to write partitions to directory with naming strategy

#### FR-4.2.6: Convert Operations
The repository SHALL support converting records to a different File Type when:
- Target type has field mapping annotations referencing source type
- All required target fields have mappings or default values

Conversion SHALL support field-level transformers for value transformation.

#### FR-4.2.7: Query Operations
The repository SHALL support:
- Finding all records matching a predicate
- Finding first record matching a predicate
- Counting records matching a predicate

#### FR-4.2.8: Validation Operations
The repository SHALL validate records against:
- Nullability constraints
- Length constraints (for fixed-width)
- Format pattern constraints
- Custom validation rules

---

### 4.3 Field Mapping for Conversion

#### FR-4.3.1: Mapping Annotation
The framework SHALL allow annotating target type fields with mapping information:
- Source type class
- Source field name
- Optional transformer class

#### FR-4.3.2: Multiple Source Support
A single target field SHALL support mappings from multiple source types (for unified types).

#### FR-4.3.3: Transformer Interface
The framework SHALL provide a transformer interface for custom value conversion during mapping.

---

### 4.4 Destination System

#### FR-4.4.1: Destination Definition
The framework SHALL allow defining destinations using annotations on interfaces:
- Destination name (required)
- Protocol-specific configuration (SFTP, HTTP)

#### FR-4.4.2: SFTP Destination Configuration
SFTP destinations SHALL support:
- Host (with property placeholder support)
- Port (default 22)
- Remote path
- Credentials reference
- Connection timeout
- Directory auto-creation
- Temporary file suffix for atomic uploads

#### FR-4.4.3: HTTP Destination Configuration
HTTP destinations SHALL support:
- URL (with property placeholder support)
- HTTP method
- Content-Type header
- Additional headers
- Authentication reference
- Connection and read timeouts
- Multipart upload option

#### FR-4.4.4: Send Operations
The send operation SHALL support:
- Specifying output format
- Specifying destination (by class or instance)
- Specifying filename (static or generated)
- Adding metadata
- Success and failure callbacks
- Retry configuration
- Synchronous and asynchronous execution

#### FR-4.4.5: Send Result
Send operations SHALL return results containing:
- Success/failure status
- Record count sent
- Byte count sent
- Destination details
- Timestamp
- Error details (on failure)

---

### 4.5 File Format Support

#### FR-4.5.1: Built-in Formats
The framework SHALL support these formats out of the box:
- CSV (with configurable delimiter, quote character, escape character)
- TSV (tab-separated)
- Pipe-delimited
- Fixed-width positional
- XML
- JSON (array and JSON Lines)

#### FR-4.5.2: Format Options
Each format SHALL support configuration options:
- Character encoding (default UTF-8)
- Line separator
- Null value representation
- Format-specific options (header row, indentation, root element, etc.)

#### FR-4.5.3: Format Extensibility
The framework SHALL allow registering custom format parsers via SPI.

---

### 4.6 Type Safety

#### FR-4.6.1: Compile-Time Type Checking
The repository SHALL be parameterized by File Type, ensuring:
- Read operations return the correct type
- Write operations accept only the correct type
- Merge operations work only with same type
- Convert operations enforce type relationships via generics

#### FR-4.6.2: Runtime Type Validation
The framework SHALL validate at runtime:
- Conversion mappings exist for source→target type pair
- Destination is properly configured
- Schema matches file content (on read)

---

## 5. Non-Functional Requirements

### 5.1 Performance

#### NFR-5.1.1: Memory Efficiency
For files larger than available heap, the framework SHALL provide streaming read mode that processes records without loading all into memory.

#### NFR-5.1.2: Large File Support
The framework SHALL handle files with millions of records without failure (using streaming mode).

### 5.2 Reliability

#### NFR-5.2.1: Atomic Writes
For SFTP destinations, the framework SHALL use temporary files and rename for atomic uploads.

#### NFR-5.2.2: Retry Support
Send operations SHALL support configurable retry with backoff.

#### NFR-5.2.3: Error Recovery
On read errors, the framework SHALL report line number and content that caused the error.

### 5.3 Compatibility

#### NFR-5.3.1: Java Version
The framework SHALL support Java 11 and later.

#### NFR-5.3.2: No Required Dependencies
The core framework SHALL have no required runtime dependencies beyond the JDK.

#### NFR-5.3.3: Optional Dependencies
SFTP support MAY require JSch or Apache MINA SSHD as optional dependency.

### 5.4 Extensibility

#### NFR-5.4.1: SPI Pattern
The framework SHALL use Java ServiceLoader SPI for:
- Custom format parsers
- Custom destination handlers
- Custom validation rules
- Custom field transformers

---

## 6. Use Cases

### UC-1: Consolidate Multiple Program Files

**Actor**: Batch processing application  
**Precondition**: Multiple fixed-width files exist, one per program code  
**Flow**:
1. Application reads all program files using repository
2. Application merges records with sorting by ZIP code, then program code
3. Application deduplicates by payment ID
4. Application writes consolidated file in XML format
5. Application sends file to BPM via SFTP

**Postcondition**: Single consolidated file delivered to BPM

---

### UC-2: Split Return File by Destination

**Actor**: Batch processing application  
**Precondition**: Single pipe-delimited file containing mixed HRM and FIN records  
**Flow**:
1. Application reads combined return file
2. Application splits by source system field (HRM vs FIN)
3. Application writes each partition to separate file
4. Application sends HRM file to Payroll system
5. Application sends FIN file to Financial system

**Postcondition**: Each system receives only its relevant records

---

### UC-3: Convert Between File Types

**Actor**: Integration application  
**Precondition**: Records exist in HRM format, target system expects unified format  
**Flow**:
1. Application reads HRM records
2. Application converts to unified format using field mappings
3. Application validates converted records
4. Application writes in target format

**Postcondition**: Records available in target system's expected format

---

### UC-4: Validate and Report Errors

**Actor**: Data quality application  
**Precondition**: File received from external source  
**Flow**:
1. Application reads file
2. Application validates all records
3. Application collects validation errors with line numbers
4. Application separates valid and invalid records
5. Application processes valid records, reports invalid

**Postcondition**: Valid records processed, invalid records reported

---

## 7. API Contracts

### 7.1 Repository Interface

The repository provides these operation categories:

| Category | Operations |
|----------|------------|
| Read | read(path, format), read(stream, format), readAll(paths, format), readStream(path, format) |
| Write | write(records, path, format), write(records, stream, format), writeToBytes(records, format) |
| Merge | merge(records) → MergeBuilder |
| Split | split(records, rule) → SplitResult |
| Convert | convert(records, targetType) → List of target type |
| Query | findAll(records, predicate), findFirst(records, predicate), count(records, predicate) |
| Validate | validate(records) → ValidationResult |
| Metadata | getFileType(), getSchema() |

### 7.2 MergeBuilder Interface

Fluent builder providing:
- sortBy(field extractors...) - primary, secondary, etc.
- sortBy(comparator) - custom comparison
- ascending() / descending() - sort direction
- deduplicate() - using identity fields
- deduplicate(key extractor) - custom key
- deduplicateKeepLast() - keep last vs first
- filter(predicate) - filter during merge
- transform(function) - transform during merge
- build() → merged records
- buildWithStats() → MergeResult with statistics

### 7.3 SplitRule Interface

Factory methods:
- byField(extractor) - partition by field value
- byCount(n) - n records per partition
- bySize(bytes) - approximate bytes per partition
- byPredicate(predicate, trueLabel, falseLabel) - binary split
- byPredicates(map of label→predicate) - multi-way split

Combinators:
- andThen(rule) - chain rules
- validateCompatibility(rule) - check for conflicts

### 7.4 SendBuilder Interface

Fluent builder providing:
- as(format) - output format
- to(destination class or instance) - target
- withFilename(name or supplier) - filename
- withMetadata(key, value) - transmission metadata
- onSuccess(callback) - success handler
- onFailure(callback) - failure handler
- withRetry(config) - retry configuration
- execute() → SendResult (sync)
- executeAsync() → CompletableFuture of SendResult (async)

---

## 8. Error Handling

### 8.1 Exception Hierarchy

| Exception | When Thrown |
|-----------|-------------|
| SchemaValidationException | Invalid annotations on File Type class |
| FileParseException | Error parsing file content (includes line number) |
| FileWriteException | Error writing file |
| ConversionException | Missing or invalid field mapping for conversion |
| SplitRuleConflictException | Contradictory split rules combined |
| DestinationException | Error connecting to or sending to destination |
| ValidationException | Records fail validation constraints |

### 8.2 Error Information

All exceptions SHALL include:
- Descriptive message
- Cause (if applicable)
- Context information (file path, line number, field name, etc.)

---

## 9. Configuration

### 9.1 Global Configuration

Framework-level settings:
- Default character encoding
- Default date/time format patterns
- Default null value representation
- Whether to fail fast or collect all errors
- Logging verbosity

### 9.2 Property Placeholders

Annotation values supporting placeholders (e.g., `${sftp.host}`) SHALL be resolved via:
- System properties
- Environment variables
- Programmatic property source

### 9.3 Credentials Management

Credentials for destinations SHALL be provided via:
- Credentials provider interface (application implements)
- Reference by name in annotations
- Runtime configuration

Credentials SHALL NOT be stored in annotations directly.

---

## 10. Constraints and Limitations

### 10.1 File Size
- Eager read mode limited by available heap memory
- Streaming mode has no practical limit

### 10.2 Concurrent Access
- Repository instances are thread-safe for read operations
- Write operations to same file path are not synchronized (application responsibility)

### 10.3 Character Encoding
- UTF-8 is default and recommended
- Other encodings supported but may have edge cases with certain formats

### 10.4 XML Limitations
- Nested structures require XPath in annotations
- Maximum nesting depth: 10 levels
- Namespaces require explicit configuration

---

## 11. Security Considerations

### 11.1 Credential Handling
- No credentials in source code or annotations
- Credentials provider pattern for secure injection
- Support for key-based SSH authentication

### 11.2 Input Validation
- XML parsing uses secure defaults (DTD disabled, external entities disabled)
- Path traversal prevented in file operations
- Maximum record/field sizes enforced

### 11.3 Logging
- No sensitive data (credentials, PII) in log messages
- Configurable logging levels

---

## 12. Testing Requirements

### 12.1 Unit Test Coverage
- All annotation validations
- All format parsers (read and write)
- All split rule types and combinations
- Merge with all options
- Conversion with transformers
- Error handling paths

### 12.2 Integration Test Coverage
- SFTP send/receive (using embedded server)
- HTTP send (using mock server)
- Large file handling
- Character encoding variations

### 12.3 Test Data
- Sample files for each format
- Edge cases: empty files, single record, special characters
- Invalid files for error testing

---

## 13. Documentation Requirements

### 13.1 Javadoc
- All public classes and interfaces
- All public methods with parameters and return values
- Exception conditions

### 13.2 User Guide
- Getting started tutorial
- Annotation reference
- Format-specific guides
- Destination configuration
- Troubleshooting

### 13.3 Examples
- Complete working examples for common use cases
- Sample File Type definitions
- Sample applications

---

## Appendix A: Annotation Quick Reference

| Annotation | Target | Required Attributes | Purpose |
|------------|--------|---------------------|---------|
| @FileType | Class | name | Marks class as file schema |
| @FileColumn | Field | order | Marks field as column |
| @FileId | Field | (none) | Marks field for deduplication |
| @MapsFrom | Field | source, field | Defines conversion mapping |
| @Destination | Interface | name | Marks interface as destination |
| @Sftp | Interface | host, path | SFTP destination config |
| @HttpApi | Interface | url | HTTP destination config |
| @Validate | Field/Class | rule | Validation constraint |

---

## Appendix B: Format Support Matrix

| Feature | CSV | TSV | Pipe | Fixed-Width | XML | JSON |
|---------|-----|-----|------|-------------|-----|------|
| Header row | Yes | Yes | Yes | No | N/A | N/A |
| Quoted values | Yes | Yes | Yes | No | N/A | N/A |
| Nested structures | No | No | No | No | Yes | Yes |
| Attributes | No | No | No | No | Yes | No |
| Comments | No | No | No | No | Yes | No |
| Schema in file | Header | Header | Header | No | Optional | No |

---

## Appendix C: Glossary

| Term | Definition |
|------|------------|
| BAW | Business Application Warehouse |
| BPM | Bank Payment Module |
| CMIPS | Case Management Information and Payrolling System |
| EFT | Electronic Funds Transfer |
| FIN | Financial (Advantage FIN module) |
| HRM | Human Resource Management (Advantage HRM module) |
| POJO | Plain Old Java Object |
| SCO | State Controller's Office |
| SPI | Service Provider Interface |
| STO | State Treasurer's Office |
