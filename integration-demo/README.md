# Integration Hub Framework - Simple Demo

This is a minimal Spring Boot application that demonstrates using the Integration Hub Framework.

## What This Does

1. Reads `employees.xml` (3 employees)
2. Reads `salaries.xml` (3 employees, 1 duplicate)
3. **Merges** using framework's `MergeUtil.mergeUnique()` (removes duplicate by ID)
4. **Sorts** by employee ID
5. **Transforms** to JSON using framework's `TransformUtil.toJson()`
6. **Writes** output file using framework's `FileUtil.writeFile()`

## Key Framework Features Demonstrated

### 1. Adding Framework Dependency

```xml
<dependency>
    <groupId>com.cmips</groupId>
    <artifactId>integration-hub-framework</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Implementing Interfaces

- `IInputSource<T>` - For readers (EmployeeFileReader, SalaryFileReader)
- `ITransformer<I, O>` - For transformers (EmployeeMerger, EmployeeSorter, XmlToJsonTransformer)
- `IOutputDestination<T>` - For writers (JsonFileWriter)

### 3. Using Annotations

- `@InputSource` - Mark as input source component
- `@Transformer` - Mark as transformer component
- `@OutputDestination` - Mark as output destination component

### 4. Using Framework Utilities

- `FileUtil.parseXml()` - Parse XML files
- `FileUtil.writeFile()` - Write files
- `MergeUtil.mergeUnique()` - Merge with deduplication
- `TransformUtil.toJson()` - Convert to JSON

## Running

```bash
# Build the project
mvn clean install

# Run the demo
mvn spring-boot:run
```

## Expected Output

```
15:30:00 INFO  - Creating directories...
15:30:00 INFO  - Created: ./data/input/employees.xml
15:30:00 INFO  - Created: ./data/input/salaries.xml
15:30:00 INFO  - Sample files ready!
15:30:00 INFO  -
15:30:00 INFO  - ========================================
15:30:00 INFO  -   SIMPLE INTEGRATION DEMO - STARTING
15:30:00 INFO  - ========================================
15:30:00 INFO  -
15:30:00 INFO  - -> Step 1: Reading employees file...
15:30:00 INFO  - Read 3 employees
15:30:00 INFO  -
15:30:00 INFO  - -> Step 2: Reading salaries file...
15:30:00 INFO  - Read 3 salary records
15:30:00 INFO  -
15:30:00 INFO  - -> Step 3: Merging data...
15:30:00 INFO  - Merged result: 5 employees
15:30:00 INFO  -
15:30:00 INFO  - -> Step 4: Sorting data...
15:30:00 INFO  - Sorted 5 employees
15:30:00 INFO  -
15:30:00 INFO  - -> Step 5: Converting to JSON...
15:30:00 INFO  - Generated JSON: 523 characters
15:30:00 INFO  -
15:30:00 INFO  - -> Step 6: Writing output file...
15:30:00 INFO  - Successfully wrote: ./data/output/employees_20241224_153000.json
15:30:00 INFO  -
15:30:00 INFO  - ========================================
15:30:00 INFO  -   DEMO COMPLETED SUCCESSFULLY!
15:30:00 INFO  -   Output: ./data/output/employees_...
15:30:00 INFO  - ========================================
```

## Output File

Check `./data/output/employees_YYYYMMDD_HHMMSS.json`:

```json
[
  {
    "id": 101,
    "name": "Alice Johnson",
    "department": "Engineering",
    "salary": 85000
  },
  {
    "id": 102,
    "name": "Bob Smith",
    "department": "Sales",
    "salary": 75000
  },
  {
    "id": 103,
    "name": "Carol Williams",
    "department": "HR",
    "salary": 65000
  },
  {
    "id": 104,
    "name": "David Brown",
    "department": "Engineering",
    "salary": 90000
  },
  {
    "id": 105,
    "name": "Eve Davis",
    "department": "Marketing",
    "salary": 70000
  }
]
```

## Project Structure

```
src/main/java/com/example/demo/
├── DemoApplication.java          # Main class
├── model/
│   ├── Employee.java             # Domain model
│   └── EmployeeList.java         # XML wrapper
├── reader/
│   ├── EmployeeFileReader.java   # Implements IInputSource
│   └── SalaryFileReader.java     # Implements IInputSource
├── processor/
│   ├── EmployeeMerger.java       # Implements ITransformer, uses MergeUtil
│   ├── EmployeeSorter.java       # Implements ITransformer
│   └── XmlToJsonTransformer.java # Implements ITransformer, uses TransformUtil
├── writer/
│   └── JsonFileWriter.java       # Implements IOutputDestination, uses FileUtil
└── flow/
    └── SimpleFlow.java           # Wires everything together
```

## Key Points

- **Framework is a JAR dependency** - Added via Maven
- **Implement interfaces** - IInputSource, ITransformer, IOutputDestination
- **Use annotations** - Framework discovers your components
- **Call utilities** - FileUtil, MergeUtil, TransformUtil
- **Spring autowires everything** - Dependencies injected automatically
