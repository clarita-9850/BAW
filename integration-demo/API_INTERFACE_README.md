# Integration Hub Framework - Web Interface

A REST API and web interface for testing the Integration Hub Framework interactively.

## Features

- **Merge Files**: Combine multiple files with deduplication, sorting, and filtering
- **Split Files**: Partition files by field value or record count
- **Convert Format**: Convert files between CSV, JSON, XML formats
- **View Schema**: Inspect file type schemas and metadata

## Running the Application

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

- **Web Interface**: http://localhost:8081
- **API Base URL**: http://localhost:8081/api/files

## API Endpoints

### 1. Get Schema Information

```
GET /api/files/schema/{recordType}
```

**Parameters:**
- `recordType`: One of `Employee`, `SimpleRecord`, `CompositeKeyRecord`

**Response:**
```json
{
  "typeName": "employee-record",
  "description": "Employee record for HR system",
  "version": "1.0",
  "columns": [
    {
      "name": "id",
      "javaType": "Long",
      "order": 1,
      "nullable": false,
      "format": null,
      "length": null,
      "alignment": null
    }
  ],
  "identityFields": ["id"]
}
```

### 2. Merge Files

```
POST /api/files/merge
```

**Request Body:**
```json
{
  "recordType": "Employee",
  "inputFilePaths": [
    "./data/input/employees.csv",
    "./data/input/salaries.csv"
  ],
  "format": "JSON",
  "outputFilePath": "./data/output/merged.json",
  "mergeOptions": {
    "deduplicate": true,
    "keepFirst": true,
    "sortField": "id",
    "sortOrder": "ASC",
    "filterExpressions": ["department=Engineering"],
    "limit": 100
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "Files merged successfully",
  "outputFilePath": "./data/output/merged_20241224_120000.json",
  "stats": {
    "inputRecordCount": 6,
    "outputRecordCount": 5,
    "duplicatesRemoved": 1,
    "filteredOut": 0,
    "partitionsCreated": null,
    "partitionCounts": null
  },
  "errors": []
}
```

### 3. Split File

```
POST /api/files/split
```

**Request Body:**
```json
{
  "recordType": "Employee",
  "inputFilePaths": ["./data/input/employees.csv"],
  "format": "JSON",
  "outputFilePath": "./data/output/split",
  "splitOptions": {
    "splitType": "FIELD",
    "splitField": "department",
    "splitCount": null
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "File split successfully into 3 partitions",
  "outputFilePath": "./data/output",
  "stats": {
    "inputRecordCount": 5,
    "outputRecordCount": 5,
    "partitionsCreated": 3,
    "partitionCounts": {
      "Engineering": 2,
      "Sales": 1,
      "HR": 1,
      "Marketing": 1
    }
  },
  "errors": []
}
```

### 4. Convert File Format

```
POST /api/files/convert
```

**Request Body:**
```json
{
  "recordType": "Employee",
  "inputFilePaths": ["./data/input/employees.csv"],
  "format": "CSV",
  "outputFilePath": "./data/output/employees.json",
  "convertOptions": {
    "targetFormat": "JSON"
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "File converted successfully",
  "outputFilePath": "./data/output/employees.json",
  "stats": {
    "inputRecordCount": 5,
    "outputRecordCount": 5
  },
  "errors": []
}
```

### 5. Get Available Record Types

```
GET /api/files/record-types
```

**Response:**
```json
{
  "Employee": "com.example.demo.model.Employee",
  "SimpleRecord": "com.example.demo.testmodel.SimpleRecord",
  "CompositeKeyRecord": "com.example.demo.testmodel.CompositeKeyRecord"
}
```

### 6. Health Check

```
GET /api/files/health
```

**Response:**
```json
{
  "status": "UP",
  "service": "File Operation Service"
}
```

## Web Interface Usage

1. **Access the Interface**: Open http://localhost:8080 in your browser

2. **Merge Files Tab**:
   - Select record type (Employee, SimpleRecord)
   - Enter input file paths (one per line)
   - Choose output format (CSV, JSON, XML)
   - Configure merge options (deduplicate, sort, filter, limit)
   - Click "Merge Files"

3. **Split File Tab**:
   - Select record type
   - Enter input file path
   - Choose split type (By Field or By Count)
   - Configure split options
   - Click "Split File"

4. **Convert Format Tab**:
   - Select source record type
   - Enter input file path
   - Choose source and target formats
   - Click "Convert File"

5. **View Schema Tab**:
   - Select record type
   - Click "View Schema"
   - See schema details including columns, types, and constraints

## Example Usage

### Merge two CSV files with deduplication:

1. Ensure you have two CSV files:
   - `./data/input/employees.csv`
   - `./data/input/salaries.csv`

2. In the Merge Files tab:
   - Record Type: `Employee`
   - Input Files:
     ```
     ./data/input/employees.csv
     ./data/input/salaries.csv
     ```
   - Output Format: `JSON`
   - Check "Remove duplicates"
   - Sort By Field: `id`, Order: `Ascending`
   - Click "Merge Files"

3. Result file will be saved to `./data/output/merged_YYYYMMDD_HHMMSS.json`

### Split file by department:

1. In the Split File tab:
   - Record Type: `Employee`
   - Input File: `./data/input/employees.csv`
   - Output Format: `JSON`
   - Split Type: `By Field Value`
   - Split Field: `department`
   - Click "Split File"

2. Result files will be:
   - `./data/output/split_Engineering.json`
   - `./data/output/split_Sales.json`
   - `./data/output/split_HR.json`
   - etc.

## File Paths

All file paths are relative to the application working directory. The application creates these directories automatically:

- `./data/input/` - Place input files here
- `./data/output/` - Output files are written here

## Supported Formats

- **CSV**: Comma-separated values
- **JSON**: JavaScript Object Notation (with pretty printing)
- **XML**: Extensible Markup Language

## Supported Record Types

- **Employee**: Employee records with id, name, department, salary
- **SimpleRecord**: Simple test record (in test package)
- **CompositeKeyRecord**: Record with composite key (in test package)

## Error Handling

All API endpoints return a consistent error response format:

```json
{
  "success": false,
  "message": "Operation failed: error description",
  "errors": ["Detailed error message 1", "Detailed error message 2"],
  "outputFilePath": null,
  "stats": null
}
```

The web interface will display errors in red with detailed messages.

