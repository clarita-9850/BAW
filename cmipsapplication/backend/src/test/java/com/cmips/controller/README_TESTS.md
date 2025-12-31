# Unit Tests for AuthController - Login Authentication

## Overview

This directory contains comprehensive unit tests for the `AuthController` login authentication functionality. The tests use **JUnit 5** and **Mockito** to verify all login scenarios without requiring an actual Keycloak server.

## Test File

- **AuthControllerTest.java** - Complete unit test suite for login authentication

## Running the Tests

### Using Maven

```bash
# Run all tests
mvn test

# Run only AuthControllerTest
mvn test -Dtest=AuthControllerTest

# Run with verbose output
mvn test -Dtest=AuthControllerTest -X
```

### Using IDE (IntelliJ IDEA / Eclipse)

1. Right-click on `AuthControllerTest.java`
2. Select "Run 'AuthControllerTest'"
3. Or use the keyboard shortcut (Ctrl+Shift+F10 in IntelliJ)

## Test Coverage

The test suite covers the following scenarios:

### ✅ Successful Login Scenarios

1. **testSuccessfulLogin** - Verifies successful login with valid credentials
   - Returns HTTP 200 OK
   - Contains access_token, refresh_token, expires_in, token_type
   - Verifies RestTemplate is called with correct parameters

### ❌ Validation Error Scenarios

2. **testLoginWithMissingUsername** - Missing username parameter
   - Returns HTTP 400 Bad Request
   - Error message: "Username and password are required"
   - RestTemplate is NOT called

3. **testLoginWithMissingPassword** - Missing password parameter
   - Returns HTTP 400 Bad Request
   - Error message: "Username and password are required"
   - RestTemplate is NOT called

4. **testLoginWithMissingCredentials** - Both username and password missing
   - Returns HTTP 400 Bad Request
   - Error message: "Username and password are required"

5. **testLoginWithNullUsername** - Null username value
   - Returns HTTP 400 Bad Request

6. **testLoginWithNullPassword** - Null password value
   - Returns HTTP 400 Bad Request

### 🔐 Authentication Failure Scenarios

7. **testLoginWithInvalidCredentials** - Invalid username/password
   - Keycloak returns HTTP 401 Unauthorized
   - Returns HTTP 401 with error: "Invalid credentials"

8. **testLoginWithKeycloakError** - Keycloak server error
   - Keycloak returns non-OK status (e.g., 500)
   - Returns HTTP 401 with error: "Invalid credentials"

### ⚠️ Exception Handling Scenarios

9. **testLoginWithRestClientException** - Network/connection errors
   - RestTemplate throws RestClientException
   - Returns HTTP 401 with error message containing exception details

10. **testLoginWithGenericException** - Unexpected errors
    - RestTemplate throws generic RuntimeException
    - Returns HTTP 401 with error message containing exception details

### 🔧 Technical Verification Tests

11. **testKeycloakTokenUrlConstruction** - Verifies correct URL construction
    - Ensures Keycloak token endpoint URL is correctly formatted

12. **testRequestHeaders** - Verifies correct HTTP headers
    - Ensures Content-Type is set to APPLICATION_FORM_URLENCODED

13. **testRequestBodyParameters** - Verifies request body parameters
    - Ensures all required parameters are included (username, password, grant_type, client_id, client_secret)

14. **testLoginWithEmptyUsername** - Edge case: empty string username
    - Empty string is not null, so request proceeds to Keycloak

15. **testLoginWithEmptyPassword** - Edge case: empty string password
    - Empty string is not null, so request proceeds to Keycloak

## Test Structure

### Mocking Strategy

- **RestTemplate** is mocked to avoid actual HTTP calls to Keycloak
- **Configuration values** are injected using ReflectionTestUtils
- All external dependencies are isolated using Mockito

### Test Constants

```java
KEYCLOAK_AUTH_SERVER_URL = "http://localhost:8085/auth/"
REALM = "cmips"
VALID_USERNAME = "testuser"
VALID_PASSWORD = "testpassword"
ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
REFRESH_TOKEN = "refresh_token_value"
```

## Dependencies

The tests require the following dependencies (already in `pom.xml`):

- `spring-boot-starter-test` - Includes JUnit 5, Mockito, AssertJ
- `spring-security-test` - For security testing (if needed)

## Best Practices Used

1. ✅ **Isolated Unit Tests** - No external dependencies (Keycloak is mocked)
2. ✅ **Comprehensive Coverage** - Tests all code paths and edge cases
3. ✅ **Clear Test Names** - Descriptive test method names using @DisplayName
4. ✅ **AAA Pattern** - Arrange, Act, Assert structure
5. ✅ **Verify Behavior** - Verifies interactions with mocked dependencies
6. ✅ **Edge Case Testing** - Tests null values, empty strings, exceptions
7. ✅ **Clean Code** - Well-organized, readable test code

## Expected Test Results

When all tests pass, you should see:

```
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

## Troubleshooting

### Test Failures

1. **Missing Dependencies**: Ensure `pom.xml` has all required test dependencies
2. **Mockito Issues**: Verify Mockito annotations (@Mock, @InjectMocks) are correctly placed
3. **Reflection Issues**: Check that ReflectionTestUtils is setting fields correctly

### Common Issues

- **NullPointerException**: Ensure all mocked objects are properly initialized in @BeforeEach
- **Verification Errors**: Check that mock interactions match the actual implementation
- **Assertion Failures**: Review expected vs actual values in test assertions

## Adding More Tests

To add additional test cases:

1. Create a new `@Test` method with a descriptive name
2. Use `@DisplayName` for better test reporting
3. Follow the AAA pattern (Arrange, Act, Assert)
4. Mock external dependencies using `@Mock`
5. Verify interactions using Mockito's `verify()`

## Integration Tests

For integration tests that require a real Keycloak instance, create separate test classes:
- Use `@SpringBootTest` annotation
- Configure test Keycloak instance
- Use `@TestPropertySource` to override configuration

---

**Note**: These are unit tests that mock all external dependencies. For integration testing with a real Keycloak server, create separate integration test classes.







