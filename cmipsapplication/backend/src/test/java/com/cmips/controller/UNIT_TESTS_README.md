# Unit Tests Documentation - CMIPS Backend

## Overview

This directory contains comprehensive unit tests for all controllers in the CMIPS backend application. The tests use **JUnit 5** and **Mockito** to verify controller functionality without requiring external dependencies like databases or Keycloak servers.

## Test Structure

All unit tests follow the same pattern:
- **Framework**: JUnit 5 with Mockito Extension
- **Pattern**: AAA (Arrange, Act, Assert)
- **Mocking**: External dependencies are mocked (services, repositories)
- **Isolation**: Tests are independent and can run in any order

## Test Files

### ✅ Completed Tests

1. **AuthControllerTest.java**
   - 15 test cases covering login and token refresh
   - Tests successful login, validation errors, authentication failures, exception handling

2. **TimesheetControllerTest.java**
   - Comprehensive tests for timesheet CRUD operations
   - Tests create, read, update, delete, submit, approve, reject
   - Tests role-based access control (PROVIDER, CASE_WORKER, RECIPIENT)
   - Tests field-level authorization

3. **TaskControllerTest.java**
   - Tests for task management endpoints
   - Tests CRUD operations, status updates, queue operations
   - Tests user task retrieval with subscribed queues

4. **NotificationControllerTest.java**
   - Tests for notification management
   - Tests create, read, update (mark as read), delete operations
   - Tests unread count functionality

### 📝 Tests to be Created

5. **CaseControllerTest.java**
   - Test address change submission
   - Test case workflow operations

6. **WorkQueueControllerTest.java**
   - Test queue catalog retrieval
   - Test subscription management
   - Test queue summary operations

7. **AnalyticsControllerTest.java**
   - Test real-time metrics retrieval
   - Test analytics filtering

8. **BusinessIntelligenceControllerTest.java**
   - Test report generation
   - Test job status tracking
   - Test report download

9. **EVVControllerTest.java**
   - Test check-in operations
   - Test check-out operations
   - Test EVV record retrieval

10. **ProviderRecipientControllerTest.java**
    - Test provider-recipient relationship retrieval
    - Test relationship management

11. **FieldMaskingControllerTest.java**
    - Test field masking interface retrieval
    - Test field masking application

12. **KeycloakAdminControllerTest.java**
    - Test user management (create, delete, list)
    - Test role management
    - Test group management (location-based)
    - Test policy and permission management
    - Requires ADMIN role mocking

13. **GenericResourceControllerTest.java**
    - Test generic CRUD operations
    - Test Keycloak-based authorization

## Running Tests

### Run All Tests

```bash
cd /Users/sajeev/Documents/CMIPS/cmipsapplication/backend
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=TimesheetControllerTest
mvn test -Dtest=TaskControllerTest
mvn test -Dtest=NotificationControllerTest
```

### Run All Controller Tests

```bash
mvn test -Dtest="*ControllerTest"
```

### Run Tests with Verbose Output

```bash
mvn test -Dtest=AuthControllerTest -X
```

### Run Tests and Generate Coverage Report

```bash
mvn test jacoco:report
```

The coverage report will be generated at: `target/site/jacoco/index.html`

## Test Coverage Goals

### Target Coverage Metrics

- **Line Coverage**: > 80%
- **Branch Coverage**: > 75%
- **Method Coverage**: > 85%

### Current Coverage Status

- ✅ **AuthController**: 15 tests covering all code paths
- ✅ **TimesheetController**: Comprehensive coverage of all endpoints
- ✅ **TaskController**: Full CRUD and workflow coverage
- ✅ **NotificationController**: Complete notification lifecycle coverage
- ⏳ **Other Controllers**: Tests to be implemented

## Test Categories

### 1. Happy Path Tests
Tests that verify successful execution of operations:
- Successful creation, retrieval, update, deletion
- Successful authentication and authorization
- Successful workflow state transitions

### 2. Validation Tests
Tests that verify input validation:
- Missing required fields
- Invalid data types
- Out-of-range values
- Null/empty values

### 3. Authorization Tests
Tests that verify access control:
- Role-based access control (RBAC)
- Permission-based access control
- Field-level authorization
- Unauthorized access attempts

### 4. Error Handling Tests
Tests that verify error scenarios:
- Not found scenarios (404)
- Validation errors (400)
- Authorization failures (403)
- Server errors (500)
- Exception handling

### 5. Edge Case Tests
Tests that verify boundary conditions:
- Empty collections
- Maximum values
- Minimum values
- Null handling
- Concurrent operations

## Mocking Strategy

### Services Mocked

All service dependencies are mocked to ensure:
- **Isolation**: Tests don't depend on service implementation
- **Speed**: No database or external API calls
- **Control**: Predictable test behavior

### Common Mock Patterns

```java
@Mock
private SomeService someService;

@InjectMocks
private SomeController someController;

// Arrange
when(someService.method(any())).thenReturn(expectedResult);

// Act
ResponseEntity<?> response = someController.endpoint(request);

// Assert
assertNotNull(response);
assertEquals(HttpStatus.OK, response.getStatusCode());
verify(someService, times(1)).method(any());
```

## Security Context Mocking

For controllers that require authentication, we mock the security context:

```java
private void setupMockSecurityContext() {
    Authentication authentication = mock(Authentication.class);
    Jwt jwt = mock(Jwt.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    
    when(jwt.getSubject()).thenReturn("testuser");
    when(authentication.getPrincipal()).thenReturn(jwt);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
}
```

## Test Utilities

### TestUtils.java

A utility class providing helper methods:
- `createMockJwtToken()` - Create mock JWT tokens
- `setupSecurityContext()` - Setup mock security context
- `clearSecurityContext()` - Clean up after tests

## Best Practices

### 1. Test Naming Convention

Use descriptive test names following the pattern:
```java
@Test
@DisplayName("Should [expected behavior] when [condition]")
void testMethodName_Condition() {
    // Test implementation
}
```

### 2. Test Organization

Organize tests by:
- Feature area (controller)
- Test category (happy path, validation, error)
- Use `@DisplayName` for readable test reports

### 3. Test Independence

Each test should:
- Be independent of other tests
- Not rely on test execution order
- Clean up after itself
- Use fresh mocks for each test

### 4. Assertion Quality

Use specific assertions:
```java
// Good
assertEquals(HttpStatus.OK, response.getStatusCode());
assertNotNull(response.getBody());

// Avoid
assertTrue(response != null);
```

### 5. Verification

Verify interactions with mocked dependencies:
```java
verify(service, times(1)).method(any());
verify(service, never()).otherMethod();
```

## Continuous Integration

Tests should be run:
- Before every commit
- In CI/CD pipeline
- Before deployment
- As part of code review

## Troubleshooting

### Common Issues

1. **Test Failures**
   - Check mock setup in `@BeforeEach`
   - Verify method signatures match
   - Check exception handling

2. **NullPointerException**
   - Ensure all mocks are initialized
   - Check security context setup
   - Verify dependencies are injected

3. **Verification Errors**
   - Verify mock interactions match actual calls
   - Check method call counts
   - Ensure no unexpected calls

### Debugging Tips

1. Use `@DisplayName` for readable test output
2. Add logging to understand test flow
3. Use debugger for complex scenarios
4. Check mock verification messages

## Test Data

### Test Constants

Common test constants are defined in each test class:
```java
private static final String TEST_USER_ID = "testuser";
private static final String TEST_USERNAME = "testuser";
private static final Long TEST_TIMESHEET_ID = 1L;
```

### Test Data Builders

Helper methods create test objects:
```java
private TimesheetResponse createMockTimesheetResponse(Long id) {
    // Create and return mock timesheet
}
```

## Future Enhancements

1. **Integration Tests**: Add tests with real database
2. **Performance Tests**: Add load and stress tests
3. **Contract Tests**: Add API contract tests
4. **Coverage Reports**: Generate and track coverage metrics
5. **Mutation Testing**: Use PIT or similar tools

## Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Test Documentation](https://docs.spring.io/spring-framework/reference/testing.html)

---

**Last Updated**: January 2025  
**Test Framework**: JUnit 5.10.0  
**Mocking Framework**: Mockito 5.3.0







