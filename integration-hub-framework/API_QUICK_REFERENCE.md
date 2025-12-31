# Integration Hub Framework - Quick Reference

## Annotations

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@FileType(name, description, version)` | Class | Define a file type |
| `@FileColumn(order, name, length, format, nullable, ...)` | Field | Define a column |
| `@FileId(order)` | Field | Mark as identity field |
| `@MapsFrom(source, field, transformer)` | Field | Map from another type |
| `@Validate(notNull, min, max, pattern, ...)` | Field/Class | Validation rules |

## FileRepository<T>

```java
// Create
FileRepository<T> repo = FileRepository.forType(MyRecord.class);

// Read
List<T> records = repo.read(path, FileFormat.csv().build());
List<T> records = repo.read(inputStream, format);
Stream<T> stream = repo.readStream(path, format);  // Lazy

// Write
repo.write(records, path, FileFormat.json().build());
repo.write(records, outputStream, format);
byte[] bytes = repo.writeToBytes(records, format);

// Merge
List<T> merged = repo.merge(list1, list2)
    .sortBy(T::getField).ascending()
    .deduplicate()
    .filter(predicate)
    .build();

// Split
SplitResult<T> result = repo.split(records, SplitRule.byField(T::getField));
List<T> partition = result.get("key");

// Convert
List<Target> targets = repo.convert(sources, Target.class);

// Validate
ValidationResult<T> result = repo.validate(records);
result.throwIfInvalid();

// Query
List<T> found = repo.findAll(records, predicate);
Optional<T> first = repo.findFirst(records, predicate);
long count = repo.count(records, predicate);

// Send
SendResult result = repo.send(records)
    .as(format).to(Destination.class)
    .withFilename("file.csv")
    .execute();
```

## MergeBuilder<T>

```java
repo.merge(list1, list2, list3)
    .sortBy(T::getField1)           // Primary sort
    .thenBy(T::getField2)           // Secondary sort
    .ascending()                     // or .descending()
    .deduplicate()                   // By @FileId
    .deduplicate(T::getCustomKey)   // By custom key
    .filter(record -> condition)     // Filter records
    .transform(record -> modified)   // Transform records
    .limit(100)                      // Limit results
    .build();                        // Returns List<T>
    .buildWithStats();               // Returns MergeResult<T>
```

## SplitRule<T>

```java
// By field value
SplitRule.byField(T::getDepartment)

// By record count
SplitRule.byCount(100)  // 100 records per partition

// By file size
SplitRule.bySize(1024 * 1024)  // ~1MB per partition

// By predicate (boolean)
SplitRule.byPredicate(r -> r.getAmount() > 1000, "high", "low")

// By multiple predicates
SplitRule.byPredicates(Map.of(
    "urgent", r -> r.getPriority() == 1,
    "normal", r -> r.getPriority() == 2,
    "low", r -> r.getPriority() >= 3
))
```

## SplitResult<T>

```java
SplitResult<T> result = repo.split(records, rule);

result.get("key")           // List<T> for partition
result.getPartitionKeys()   // Set<String> of all keys
result.getPartitionCount()  // Number of partitions
result.hasPartition("key")  // Check if exists
result.getPartitions()      // Map<String, List<T>>
result.getCounts()          // Map<String, Integer>
```

## FileFormat

```java
FileFormat.csv()
    .delimiter(',')
    .quoteChar('"')
    .hasHeader(true)
    .charset(StandardCharsets.UTF_8)
    .build();

FileFormat.json()
    .prettyPrint(true)
    .build();

FileFormat.xml()
    .rootElement("records")
    .recordElement("record")
    .xmlDeclaration(true)
    .build();

FileFormat.fixedWidth().build();
FileFormat.tsv().build();
FileFormat.pipe().build();
FileFormat.jsonLines().build();
```

## Validation

```java
// Field-level
@Validate(notNull = true)
@Validate(notBlank = true)
@Validate(min = 0, max = 100)
@Validate(minLength = 1, maxLength = 50)
@Validate(pattern = "^[A-Z]+$")
@Validate(allowedValues = {"A", "B", "C"})

// Record-level (on class)
@Validate(validator = MyValidator.class)

// Custom validator
public class MyValidator implements RecordValidator<MyRecord> {
    @Override
    public ValidationError validate(MyRecord record) {
        if (invalid) return new ValidationError("message");
        return null;  // Valid
    }
}

// Check results
ValidationResult<T> result = repo.validate(records);
if (result.hasErrors()) {
    List<T> valid = result.getValidRecords();
    List<ValidationError> errors = result.getErrors();
}
```

## Destinations

```java
// SFTP
@Destination(name = "my-sftp")
@Sftp(host = "sftp.example.com", remotePath = "/uploads", credentials = "creds")
public interface MySftp {}

// HTTP API
@Destination(name = "my-api")
@HttpApi(url = "https://api.example.com/upload", method = POST)
public interface MyApi {}

// Send
repo.send(records)
    .as(FileFormat.csv().build())
    .to(MySftp.class)
    .withFilename("data.csv")
    .withRetry(RetryConfig.defaults())
    .onSuccess(r -> log.info("Sent"))
    .onFailure(r -> log.error(r.getErrorMessage()))
    .execute();
```

## Exceptions

| Exception | When Thrown |
|-----------|-------------|
| `SchemaValidationException` | Invalid @FileType schema |
| `FileParseException` | Error reading file |
| `FileWriteException` | Error writing file |
| `RecordValidationException` | Validation failures |
| `ConversionException` | Type conversion error |
| `DestinationException` | Send operation failed |
| `SplitRuleConflictException` | Incompatible split rules |

## Field Transformer

```java
public class MyTransformer implements FieldTransformer<String, Integer> {
    @Override
    public Integer transform(String source) {
        return Integer.parseInt(source);
    }
}

@MapsFrom(source = Source.class, field = "stringValue",
          transformer = MyTransformer.class)
private Integer intValue;
```

## Common Patterns

```java
// Read → Validate → Process → Write
List<T> records = repo.read(input, format);
ValidationResult<T> valid = repo.validate(records);
valid.throwIfInvalid();
List<T> processed = repo.merge(valid.getValidRecords(), List.of())
    .filter(condition)
    .sortBy(T::getKey)
    .build();
repo.write(processed, output, format);

// Split and write partitions
SplitResult<T> split = repo.split(records, SplitRule.byField(T::getType));
for (String key : split.getPartitionKeys()) {
    repo.write(split.get(key), dir.resolve(key + ".json"), jsonFormat);
}

// Merge multiple files
List<T> all = repo.readAll(List.of(file1, file2, file3), format);
List<T> deduped = repo.merge(all, List.of()).deduplicate().build();
```
