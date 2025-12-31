# BAW Framework Test Application - Specification

**Version**: 1.0.0  
**Status**: Draft  
**Last Updated**: 2024-12-24  

---

## 1. Purpose

This Spring Boot application exists solely to test the BAW Framework library comprehensively. It provides:
- A test harness for all framework features
- Comprehensive test cases covering all requirements
- Sample file types and test data
- Integration test infrastructure (embedded SFTP, mock HTTP)

---

## 2. Test Scope

### 2.1 Framework Features to Test

| Feature Category | Features |
|------------------|----------|
| Schema Definition | @FileType, @FileColumn, @FileId annotations |
| Column Options | order, length, padding, nullable, format, default value, XML options |
| Repository Creation | Factory method, schema validation |
| Read Operations | Single file, multiple files, stream, different formats |
| Write Operations | To file, to stream, to bytes |
| Merge Operations | Sorting, deduplication, filtering, transformation |
| Split Operations | By field, by count, by size, by predicate, chained rules |
| Convert Operations | Field mapping, transformers |
| Send Operations | SFTP destination, HTTP destination, async, retry |
| Validation | Nullability, length, format, custom rules |
| Error Handling | All exception types, error details |
| Format Support | CSV, TSV, Pipe, Fixed-Width, XML, JSON |

---

## 3. Test Categories

### 3.1 Unit Tests
Test individual components in isolation with mocks.

### 3.2 Integration Tests
Test components working together with real I/O (embedded servers, temp files).

### 3.3 End-to-End Tests
Test complete workflows simulating real usage patterns.

---

## 4. Test Cases - Schema Definition

### 4.1 @FileType Annotation

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| FT-001 | Valid @FileType with name only | Schema created successfully |
| FT-002 | Valid @FileType with name, description, version | Schema includes all metadata |
| FT-003 | Missing @FileType on class | SchemaValidationException thrown |
| FT-004 | Empty name in @FileType | SchemaValidationException thrown |
| FT-005 | Duplicate type names in same registry | SchemaValidationException thrown |

### 4.2 @FileColumn Annotation

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| FC-001 | Valid @FileColumn with order only | Column created with defaults |
| FC-002 | Valid @FileColumn with all options | Column includes all settings |
| FC-003 | Missing order attribute | SchemaValidationException thrown |
| FC-004 | Duplicate order values | SchemaValidationException thrown |
| FC-005 | Negative order value | SchemaValidationException thrown |
| FC-006 | Gap in order sequence (1, 2, 5) | Schema created (gaps allowed) |
| FC-007 | Zero-length for fixed-width | SchemaValidationException thrown |
| FC-008 | Negative length for fixed-width | SchemaValidationException thrown |
| FC-009 | Invalid format pattern for date | SchemaValidationException thrown |
| FC-010 | Invalid format pattern for number | SchemaValidationException thrown |
| FC-011 | Column on unsupported field type | SchemaValidationException thrown |
| FC-012 | Column with xmlPath on non-XML use | xmlPath ignored, no error |
| FC-013 | Column with xmlAttribute=true | XML attribute mapping set |

### 4.3 @FileId Annotation

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| FI-001 | Single @FileId field | Field marked as identity |
| FI-002 | Multiple @FileId fields (composite) | All fields form composite key |
| FI-003 | @FileId with order specified | Composite key ordered correctly |
| FI-004 | No @FileId in type | No identity (dedup by all fields or error) |
| FI-005 | @FileId on field without @FileColumn | SchemaValidationException thrown |

### 4.4 Supported Field Types

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| ST-001 | String field | Supported |
| ST-002 | int/Integer field | Supported |
| ST-003 | long/Long field | Supported |
| ST-004 | short/Short field | Supported |
| ST-005 | byte/Byte field | Supported |
| ST-006 | double/Double field | Supported |
| ST-007 | float/Float field | Supported |
| ST-008 | boolean/Boolean field | Supported |
| ST-009 | BigDecimal field | Supported |
| ST-010 | BigInteger field | Supported |
| ST-011 | LocalDate field | Supported |
| ST-012 | LocalDateTime field | Supported |
| ST-013 | LocalTime field | Supported |
| ST-014 | Instant field | Supported |
| ST-015 | ZonedDateTime field | Supported |
| ST-016 | Enum field | Supported |
| ST-017 | List field | SchemaValidationException (unsupported) |
| ST-018 | Map field | SchemaValidationException (unsupported) |
| ST-019 | Custom object field | SchemaValidationException (unsupported) |

---

## 5. Test Cases - Repository Operations

### 5.1 Repository Creation

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| RC-001 | Create repository for valid type | Repository instance returned |
| RC-002 | Create repository for invalid type | SchemaValidationException thrown |
| RC-003 | Create repository twice for same type | Same or equal instance returned |
| RC-004 | Get schema from repository | Schema matches annotations |
| RC-005 | Get file type class from repository | Correct class returned |

### 5.2 Read Operations - CSV Format

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| RD-CSV-001 | Read valid CSV with header | Records parsed correctly |
| RD-CSV-002 | Read CSV without header | Records parsed by position |
| RD-CSV-003 | Read CSV with quoted values | Quotes handled correctly |
| RD-CSV-004 | Read CSV with escaped quotes | Escapes handled correctly |
| RD-CSV-005 | Read CSV with embedded commas | Commas in quotes preserved |
| RD-CSV-006 | Read CSV with embedded newlines | Newlines in quotes preserved |
| RD-CSV-007 | Read CSV with empty values | Null or default applied |
| RD-CSV-008 | Read CSV with extra columns | Extra columns ignored |
| RD-CSV-009 | Read CSV with missing columns | FileParseException or default |
| RD-CSV-010 | Read empty CSV file | Empty list returned |
| RD-CSV-011 | Read CSV with header only | Empty list returned |
| RD-CSV-012 | Read CSV with invalid number | FileParseException with line number |
| RD-CSV-013 | Read CSV with invalid date | FileParseException with line number |
| RD-CSV-014 | Read CSV with different encoding | Characters preserved |
| RD-CSV-015 | Read CSV from Path | Works correctly |
| RD-CSV-016 | Read CSV from InputStream | Works correctly |
| RD-CSV-017 | Read multiple CSV files | All records combined |

### 5.3 Read Operations - Fixed-Width Format

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| RD-FW-001 | Read valid fixed-width file | Records parsed correctly |
| RD-FW-002 | Read with left-padded values | Padding trimmed |
| RD-FW-003 | Read with right-padded values | Padding trimmed |
| RD-FW-004 | Read with center-padded values | Padding trimmed |
| RD-FW-005 | Read with custom pad character | Custom padding trimmed |
| RD-FW-006 | Read line shorter than expected | FileParseException |
| RD-FW-007 | Read line longer than expected | Extra characters ignored or error |
| RD-FW-008 | Read with empty file | Empty list returned |
| RD-FW-009 | Read with invalid number in position | FileParseException with line number |
| RD-FW-010 | Read numeric field with spaces | Spaces trimmed, parsed correctly |

### 5.4 Read Operations - Pipe Delimited Format

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| RD-PIPE-001 | Read valid pipe-delimited file | Records parsed correctly |
| RD-PIPE-002 | Read with embedded pipes in quotes | Handled correctly |
| RD-PIPE-003 | Read with empty values | Null or default applied |
| RD-PIPE-004 | Read with trailing delimiter | Empty last field |

### 5.5 Read Operations - XML Format

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| RD-XML-001 | Read valid XML file | Records parsed correctly |
| RD-XML-002 | Read XML with attributes | Attributes mapped to fields |
| RD-XML-003 | Read XML with nested elements | XPath mapping works |
| RD-XML-004 | Read XML with namespace | Namespace handled correctly |
| RD-XML-005 | Read XML with missing optional element | Null or default applied |
| RD-XML-006 | Read XML with missing required element | FileParseException |
| RD-XML-007 | Read malformed XML | FileParseException |
| RD-XML-008 | Read XML with CDATA | CDATA content extracted |
| RD-XML-009 | Read empty XML (root only) | Empty list returned |

### 5.6 Read Operations - JSON Format

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| RD-JSON-001 | Read valid JSON array | Records parsed correctly |
| RD-JSON-002 | Read JSON Lines format | Records parsed correctly |
| RD-JSON-003 | Read JSON with null values | Null fields set |
| RD-JSON-004 | Read JSON with missing fields | Null or default applied |
| RD-JSON-005 | Read JSON with extra fields | Extra fields ignored |
| RD-JSON-006 | Read malformed JSON | FileParseException |
| RD-JSON-007 | Read empty JSON array | Empty list returned |

### 5.7 Read Operations - Streaming

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| RD-STR-001 | Stream read CSV file | Records available as stream |
| RD-STR-002 | Stream read large file | Memory stays bounded |
| RD-STR-003 | Stream with filter | Only matching records processed |
| RD-STR-004 | Stream with limit | Only N records read |
| RD-STR-005 | Stream closed properly | Resources released |
| RD-STR-006 | Stream error mid-file | Exception propagated |

### 5.8 Write Operations

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| WR-001 | Write records to CSV file | Valid CSV created |
| WR-002 | Write records to fixed-width file | Valid fixed-width created |
| WR-003 | Write records to pipe-delimited file | Valid pipe file created |
| WR-004 | Write records to XML file | Valid XML created |
| WR-005 | Write records to JSON file | Valid JSON created |
| WR-006 | Write records to JSON Lines file | Valid JSONL created |
| WR-007 | Write empty list | Empty file or header only |
| WR-008 | Write to OutputStream | Data written correctly |
| WR-009 | Write to bytes | Byte array returned |
| WR-010 | Write with null field values | Null representation used |
| WR-011 | Write with special characters | Characters escaped properly |
| WR-012 | Write fixed-width respects length | Values truncated or padded |
| WR-013 | Write date with format pattern | Pattern applied |
| WR-014 | Write number with format pattern | Pattern applied |
| WR-015 | Round-trip read/write CSV | Data preserved exactly |
| WR-016 | Round-trip read/write fixed-width | Data preserved exactly |
| WR-017 | Round-trip read/write XML | Data preserved exactly |
| WR-018 | Round-trip read/write JSON | Data preserved exactly |

---

## 6. Test Cases - Merge Operations

### 6.1 Basic Merge

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| MG-001 | Merge single list | Same records returned |
| MG-002 | Merge empty list | Empty list returned |
| MG-003 | Merge without any options | Records combined in order |

### 6.2 Sorting

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| MG-SORT-001 | Sort by single field ascending | Correctly sorted |
| MG-SORT-002 | Sort by single field descending | Correctly sorted |
| MG-SORT-003 | Sort by two fields | Primary then secondary sort |
| MG-SORT-004 | Sort by three fields | All levels applied |
| MG-SORT-005 | Sort with null values (nulls first) | Nulls at start |
| MG-SORT-006 | Sort with null values (nulls last) | Nulls at end |
| MG-SORT-007 | Sort by numeric field | Numeric order (not string) |
| MG-SORT-008 | Sort by date field | Chronological order |
| MG-SORT-009 | Sort by enum field | Enum ordinal order |
| MG-SORT-010 | Sort with custom comparator | Comparator applied |
| MG-SORT-011 | Sort empty list | Empty list returned |
| MG-SORT-012 | Sort single record | Same record returned |

### 6.3 Deduplication

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| MG-DED-001 | Deduplicate using @FileId | Duplicates removed |
| MG-DED-002 | Deduplicate keeps first occurrence | First kept |
| MG-DED-003 | Deduplicate keeps last occurrence | Last kept |
| MG-DED-004 | Deduplicate with custom key extractor | Custom key used |
| MG-DED-005 | Deduplicate composite key | Both fields checked |
| MG-DED-006 | Deduplicate no duplicates | All records kept |
| MG-DED-007 | Deduplicate all duplicates | One record kept |
| MG-DED-008 | Deduplicate with null key values | Nulls handled consistently |
| MG-DED-009 | Deduplicate empty list | Empty list returned |
| MG-DED-010 | Deduplicate type without @FileId | Error or all fields used |

### 6.4 Filtering

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| MG-FIL-001 | Filter matching some records | Matching records returned |
| MG-FIL-002 | Filter matching no records | Empty list returned |
| MG-FIL-003 | Filter matching all records | All records returned |
| MG-FIL-004 | Filter with complex predicate | Predicate evaluated correctly |
| MG-FIL-005 | Filter combined with sort | Filter then sort applied |
| MG-FIL-006 | Filter combined with deduplicate | Filter then deduplicate applied |

### 6.5 Transformation

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| MG-TRN-001 | Transform modifying field value | Field changed |
| MG-TRN-002 | Transform setting computed value | Computed value set |
| MG-TRN-003 | Transform combined with sort | Transform then sort |
| MG-TRN-004 | Transform returning same instance | Works correctly |
| MG-TRN-005 | Transform returning new instance | New instances in result |

### 6.6 Combined Operations

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| MG-CMB-001 | Sort + deduplicate | Both applied in order |
| MG-CMB-002 | Filter + sort + deduplicate | All applied in order |
| MG-CMB-003 | Transform + sort + deduplicate | All applied in order |
| MG-CMB-004 | All options combined | All applied correctly |

### 6.7 Statistics

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| MG-STAT-001 | buildWithStats returns statistics | MergeResult has stats |
| MG-STAT-002 | Stats show total input count | Correct count |
| MG-STAT-003 | Stats show duplicates removed | Correct count |
| MG-STAT-004 | Stats show filtered out count | Correct count |
| MG-STAT-005 | Stats show final count | Correct count |

### 6.8 Merge from Files

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| MG-FILE-001 | Merge from multiple files | All files read and merged |
| MG-FILE-002 | Merge from empty file list | Empty result |
| MG-FILE-003 | Merge from single file | File read correctly |
| MG-FILE-004 | Merge files + sort | Read then sort |

---

## 7. Test Cases - Split Operations

### 7.1 Split by Field

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SP-FLD-001 | Split by string field | Partition per unique value |
| SP-FLD-002 | Split by enum field | Partition per enum value |
| SP-FLD-003 | Split by numeric field | Partition per unique number |
| SP-FLD-004 | Split by field with nulls | Null partition created |
| SP-FLD-005 | Split single unique value | One partition with all records |
| SP-FLD-006 | Split all unique values | One partition per record |
| SP-FLD-007 | Split empty list | Empty result |
| SP-FLD-008 | Split with custom partition naming | Custom names applied |

### 7.2 Split by Count

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SP-CNT-001 | Split 100 records by 10 | 10 partitions of 10 |
| SP-CNT-002 | Split 95 records by 10 | 9 partitions of 10, 1 of 5 |
| SP-CNT-003 | Split 5 records by 10 | 1 partition of 5 |
| SP-CNT-004 | Split 0 records by 10 | Empty result |
| SP-CNT-005 | Split with count of 1 | One partition per record |
| SP-CNT-006 | Split with count equal to total | One partition |

### 7.3 Split by Size

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SP-SZ-001 | Split by byte size limit | Partitions under limit |
| SP-SZ-002 | Split single large record | One record per partition |
| SP-SZ-003 | Split small records | Multiple per partition |
| SP-SZ-004 | Split empty list | Empty result |

### 7.4 Split by Predicate

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SP-PRD-001 | Split by boolean predicate | Two partitions |
| SP-PRD-002 | Split all match true | One partition (true label) |
| SP-PRD-003 | Split all match false | One partition (false label) |
| SP-PRD-004 | Split empty list | Empty result |
| SP-PRD-005 | Split with labeled partitions | Labels applied correctly |

### 7.5 Split by Multiple Predicates

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SP-MPR-001 | Split by 3 predicates | 3 partitions |
| SP-MPR-002 | Record matches first predicate | Goes to first partition |
| SP-MPR-003 | Record matches none | Error or catch-all |
| SP-MPR-004 | Record matches multiple | Goes to first match |
| SP-MPR-005 | Predicates with catch-all last | Catch-all gets remainder |

### 7.6 Split Rule Chaining

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SP-CHN-001 | Chain field + count rules | Both applied |
| SP-CHN-002 | Chain count + field rules | Both applied |
| SP-CHN-003 | Chain three rules | All applied |

### 7.7 Split Rule Conflicts

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SP-CFT-001 | Conflicting count rules | SplitRuleConflictException |
| SP-CFT-002 | Conflicting size rules | SplitRuleConflictException |
| SP-CFT-003 | Compatible rules validated | No exception |

### 7.8 Split Result Operations

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SP-RES-001 | Get partitions map | Map returned |
| SP-RES-002 | Get specific partition | List returned |
| SP-RES-003 | Get partition keys | Set of keys returned |
| SP-RES-004 | Get total record count | Sum of all partitions |
| SP-RES-005 | Get partition record count | Correct count |
| SP-RES-006 | Get non-existent partition | Empty list or null |
| SP-RES-007 | Write partitions to directory | Files created |
| SP-RES-008 | Write with naming strategy | Names applied |
| SP-RES-009 | Write with prefix and extension | Prefix and extension applied |
| SP-RES-010 | Transform partitions | Transformation applied |

---

## 8. Test Cases - Convert Operations

### 8.1 Basic Conversion

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| CV-001 | Convert with all fields mapped | All fields populated |
| CV-002 | Convert with partial mapping | Mapped fields populated |
| CV-003 | Convert empty list | Empty list returned |
| CV-004 | Convert single record | Single record returned |

### 8.2 Field Mapping

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| CV-MAP-001 | Direct field name mapping | Value copied |
| CV-MAP-002 | Different field names mapped | Value copied to target name |
| CV-MAP-003 | Field mapped from multiple sources | Correct source used |
| CV-MAP-004 | Target field with no mapping | Null or default |
| CV-MAP-005 | Source field is null | Null copied |
| CV-MAP-006 | Type conversion string to number | Converted correctly |
| CV-MAP-007 | Type conversion number to string | Converted correctly |
| CV-MAP-008 | Type conversion date formats | Converted correctly |

### 8.3 Transformers

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| CV-TRN-001 | Transformer modifies value | Modified value set |
| CV-TRN-002 | Transformer changes type | Type changed |
| CV-TRN-003 | Transformer returns null | Null set |
| CV-TRN-004 | Transformer throws exception | ConversionException |
| CV-TRN-005 | No-op transformer | Value unchanged |

### 8.4 Conversion Errors

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| CV-ERR-001 | No mapping exists for source type | ConversionException |
| CV-ERR-002 | Required target field not mapped | ConversionException |
| CV-ERR-003 | Incompatible type conversion | ConversionException |

---

## 9. Test Cases - Send Operations

### 9.1 SFTP Destination

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SND-SFTP-001 | Send file to SFTP server | File uploaded |
| SND-SFTP-002 | Send with temp suffix | Temp file renamed |
| SND-SFTP-003 | Send to non-existent directory | Error or create directory |
| SND-SFTP-004 | Send with create directory option | Directory created |
| SND-SFTP-005 | Send with connection timeout | Timeout respected |
| SND-SFTP-006 | Send to unreachable host | DestinationException |
| SND-SFTP-007 | Send with invalid credentials | DestinationException |
| SND-SFTP-008 | Send empty file | Empty file uploaded |
| SND-SFTP-009 | Send large file | File uploaded completely |
| SND-SFTP-010 | Test connection | Returns true/false |

### 9.2 HTTP Destination

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SND-HTTP-001 | Send file via POST | File sent, response received |
| SND-HTTP-002 | Send file via PUT | File sent |
| SND-HTTP-003 | Send with custom headers | Headers included |
| SND-HTTP-004 | Send as multipart | Multipart request sent |
| SND-HTTP-005 | Send with content type | Content-Type header set |
| SND-HTTP-006 | Send to unavailable server | DestinationException |
| SND-HTTP-007 | Send with connection timeout | Timeout respected |
| SND-HTTP-008 | Send with read timeout | Timeout respected |
| SND-HTTP-009 | Server returns error status | DestinationException |
| SND-HTTP-010 | Server returns success | SendResult success |

### 9.3 Send Builder Options

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SND-BLD-001 | Specify output format | Format used |
| SND-BLD-002 | Specify destination by class | Destination resolved |
| SND-BLD-003 | Specify destination instance | Instance used |
| SND-BLD-004 | Specify static filename | Filename used |
| SND-BLD-005 | Specify filename generator | Generated name used |
| SND-BLD-006 | Add metadata | Metadata included |
| SND-BLD-007 | Success callback invoked | Callback called with result |
| SND-BLD-008 | Failure callback invoked | Callback called with failure |

### 9.4 Async and Retry

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SND-ASY-001 | Execute async returns future | CompletableFuture returned |
| SND-ASY-002 | Async success completes future | Future completes with result |
| SND-ASY-003 | Async failure completes exceptionally | Exception in future |
| SND-RTY-001 | Retry on transient failure | Retry attempted |
| SND-RTY-002 | Retry respects max attempts | Stops after max |
| SND-RTY-003 | Retry respects backoff | Delays between retries |
| SND-RTY-004 | Retry success on second attempt | Success returned |
| SND-RTY-005 | Retry exhausted | Final failure returned |

### 9.5 Send Result

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| SND-RES-001 | Result contains success status | Correct status |
| SND-RES-002 | Result contains record count | Correct count |
| SND-RES-003 | Result contains byte count | Correct size |
| SND-RES-004 | Result contains timestamp | Timestamp present |
| SND-RES-005 | Result contains destination info | Destination info present |
| SND-RES-006 | Failure result contains error details | Error details present |

---

## 10. Test Cases - Validation

### 10.1 Nullability Validation

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| VL-NUL-001 | Nullable field with null value | Valid |
| VL-NUL-002 | Non-nullable field with null value | Validation error |
| VL-NUL-003 | Non-nullable field with value | Valid |
| VL-NUL-004 | Non-nullable field with empty string | Valid or invalid (configurable) |

### 10.2 Length Validation

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| VL-LEN-001 | String within length limit | Valid |
| VL-LEN-002 | String exceeds length limit | Validation error |
| VL-LEN-003 | String exactly at length limit | Valid |
| VL-LEN-004 | Empty string with length constraint | Valid |

### 10.3 Format Validation

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| VL-FMT-001 | Date matches format pattern | Valid |
| VL-FMT-002 | Date doesn't match format | Validation error |
| VL-FMT-003 | Number matches format | Valid |
| VL-FMT-004 | Number doesn't match format | Validation error |

### 10.4 Validation Result

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| VL-RES-001 | All records valid | isValid() returns true |
| VL-RES-002 | Some records invalid | isValid() returns false |
| VL-RES-003 | Errors contain line numbers | Line numbers present |
| VL-RES-004 | Errors contain field names | Field names present |
| VL-RES-005 | Errors contain messages | Messages present |
| VL-RES-006 | Get valid records | Only valid returned |
| VL-RES-007 | Get invalid records | Only invalid returned |

---

## 11. Test Cases - Error Handling

### 11.1 Schema Validation Exceptions

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| EH-SCH-001 | Exception contains type name | Type name in message |
| EH-SCH-002 | Exception contains field name | Field name in message |
| EH-SCH-003 | Exception contains violation description | Description present |

### 11.2 File Parse Exceptions

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| EH-PRS-001 | Exception contains file path | Path in message |
| EH-PRS-002 | Exception contains line number | Line number present |
| EH-PRS-003 | Exception contains column/field | Column info present |
| EH-PRS-004 | Exception contains raw value | Value in message |
| EH-PRS-005 | Exception has cause | Original exception as cause |

### 11.3 Conversion Exceptions

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| EH-CNV-001 | Exception contains source type | Source type in message |
| EH-CNV-002 | Exception contains target type | Target type in message |
| EH-CNV-003 | Exception contains field name | Field name present |

### 11.4 Destination Exceptions

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| EH-DST-001 | Exception contains destination name | Name in message |
| EH-DST-002 | Exception contains host/URL | Destination info present |
| EH-DST-003 | Exception has cause | Original exception as cause |

---

## 12. Test Cases - Format-Specific Edge Cases

### 12.1 CSV Edge Cases

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| FMT-CSV-001 | Value contains delimiter | Correctly quoted/escaped |
| FMT-CSV-002 | Value contains quote character | Correctly escaped |
| FMT-CSV-003 | Value contains newline | Correctly quoted |
| FMT-CSV-004 | Value is entirely whitespace | Preserved or trimmed |
| FMT-CSV-005 | Header name doesn't match field | Error or mapping by position |
| FMT-CSV-006 | BOM at start of file | BOM handled |

### 12.2 Fixed-Width Edge Cases

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| FMT-FW-001 | Value longer than length | Truncated |
| FMT-FW-002 | Numeric with leading zeros | Zeros preserved |
| FMT-FW-003 | Negative number alignment | Correctly aligned |
| FMT-FW-004 | Date field formatting | Format applied |
| FMT-FW-005 | Empty line in file | Skipped or error |
| FMT-FW-006 | Trailing whitespace on line | Handled correctly |

### 12.3 XML Edge Cases

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| FMT-XML-001 | Value contains < or > | Properly escaped |
| FMT-XML-002 | Value contains & | Properly escaped |
| FMT-XML-003 | Value contains quotes | Properly escaped |
| FMT-XML-004 | Empty element vs missing element | Both handled |
| FMT-XML-005 | Deeply nested XPath | Correctly traversed |
| FMT-XML-006 | Multiple namespaces | Correctly resolved |

### 12.4 JSON Edge Cases

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| FMT-JSON-001 | Unicode characters | Preserved correctly |
| FMT-JSON-002 | Numeric precision (BigDecimal) | Precision preserved |
| FMT-JSON-003 | Boolean as string "true" | Type coercion handled |
| FMT-JSON-004 | Null vs missing field | Both handled |
| FMT-JSON-005 | Empty string vs null | Distinguished |

---

## 13. Test Cases - Performance/Stress

### 13.1 Large File Handling

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| PF-001 | Read 1 million record CSV | Completes without OOM |
| PF-002 | Read 1 million record fixed-width | Completes without OOM |
| PF-003 | Merge 500K + 500K records | Completes in reasonable time |
| PF-004 | Sort 1 million records | Completes in reasonable time |
| PF-005 | Stream read large file | Memory bounded |
| PF-006 | Write 1 million records | Completes without OOM |

### 13.2 Concurrent Operations

| Test ID | Test Case | Expected Result |
|---------|-----------|-----------------|
| PF-CONC-001 | Multiple reads same repository | Thread-safe |
| PF-CONC-002 | Read and write simultaneously | Thread-safe |
| PF-CONC-003 | Multiple async sends | All complete correctly |

---

## 14. Test Infrastructure

### 14.1 Embedded Servers

| Component | Purpose |
|-----------|---------|
| Embedded SFTP Server | Test SFTP send/receive operations |
| Mock HTTP Server | Test HTTP API destinations |

### 14.2 Test File Types

The test application defines these sample file types:

| Type | Purpose | Format |
|------|---------|--------|
| SimpleRecord | Basic type with few fields | All formats |
| AllTypesRecord | One field per supported type | All formats |
| FixedWidthRecord | Fixed-width specific testing | Fixed-Width |
| XmlNestedRecord | XML nesting and attributes | XML |
| CompositeKeyRecord | Multiple @FileId fields | All formats |
| ConversionSourceRecord | Source for conversion tests | All formats |
| ConversionTargetRecord | Target with @MapsFrom | All formats |
| ValidationRecord | Fields with constraints | All formats |

### 14.3 Test Data Files

Test resources include sample files for:
- Each format (CSV, TSV, Pipe, Fixed-Width, XML, JSON, JSONL)
- Valid files with various record counts
- Invalid files for error testing
- Edge case files (empty, special characters, encoding)
- Large files for performance testing

---

## 15. Test Execution

### 15.1 Test Categories

| Category | Tag | Description |
|----------|-----|-------------|
| Unit | @Unit | Fast, isolated tests |
| Integration | @Integration | Tests with I/O |
| Performance | @Performance | Stress/load tests |
| All | (no tag) | All tests |

### 15.2 Running Tests

```
# Run all tests
./mvnw test

# Run unit tests only
./mvnw test -Dgroups=Unit

# Run integration tests only
./mvnw test -Dgroups=Integration

# Run performance tests
./mvnw test -Dgroups=Performance
```

### 15.3 Test Reports

Tests generate:
- JUnit XML reports
- HTML test reports
- Code coverage reports (JaCoCo)

---

## Appendix A: Test Case Count Summary

| Category | Count |
|----------|-------|
| Schema Definition | 32 |
| Repository Creation | 5 |
| Read Operations | 67 |
| Write Operations | 18 |
| Merge Operations | 43 |
| Split Operations | 38 |
| Convert Operations | 17 |
| Send Operations | 38 |
| Validation | 16 |
| Error Handling | 12 |
| Format Edge Cases | 23 |
| Performance | 9 |
| **Total** | **318** |

---

## Appendix B: Test Type Definitions

```
SimpleRecord
├── id: String (@FileId, order=1)
├── name: String (order=2)
├── amount: BigDecimal (order=3)
└── date: LocalDate (order=4)

AllTypesRecord
├── stringField: String (order=1)
├── intField: int (order=2)
├── longField: Long (order=3)
├── doubleField: double (order=4)
├── booleanField: Boolean (order=5)
├── bigDecimalField: BigDecimal (order=6)
├── localDateField: LocalDate (order=7)
├── localDateTimeField: LocalDateTime (order=8)
├── instantField: Instant (order=9)
└── enumField: Status (order=10)

FixedWidthRecord
├── code: String (order=1, length=6, padChar='0', padAlignment=RIGHT)
├── description: String (order=2, length=30, padAlignment=LEFT)
├── amount: BigDecimal (order=3, length=12, format="#.00")
└── date: LocalDate (order=4, length=8, format="yyyyMMdd")

CompositeKeyRecord
├── region: String (@FileId order=1, order=1)
├── customerId: String (@FileId order=2, order=2)
├── name: String (order=3)
└── balance: BigDecimal (order=4)
```
