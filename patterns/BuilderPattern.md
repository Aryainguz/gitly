# Builder Pattern

## Overview

The Builder pattern is a creational design pattern that provides a flexible solution for constructing complex objects. It separates the construction of an object from its representation, allowing the same construction process to create different representations.

## Problem Statement

Creating objects with many optional parameters or configuration options can lead to:

- **Constructor Telescoping**: Multiple constructors with different parameter combinations
- **Unclear Code**: Hard to understand which value corresponds to which parameter
- **Immutability Issues**: Difficulty in creating immutable objects with many fields
- **Error-Prone**: Easy to pass parameters in the wrong order

### Example of the Problem:

```java
// Telescoping constructors - hard to read and maintain
ApiResponse response = new ApiResponse(
    ResponseStatus.SUCCESS,
    "Success message",
    data,
    null,
    LocalDateTime.now(),
    "/api/path"
);

// Which parameter is which? Easy to make mistakes!
```

## Solution: Builder Pattern

The Builder pattern solves these problems by:

1. Providing a fluent interface for object construction
2. Making code more readable with named methods
3. Supporting optional parameters gracefully
4. Enabling immutability after construction

## Implementation in Gitly

### Using Lombok's @Builder

In this project, we leverage Lombok's `@Builder` annotation which automatically generates the Builder pattern implementation. This eliminates boilerplate code while providing all the benefits of the pattern.

### Current Implementations

#### 1. ApiResponse Builder

**File**: `src/main/java/com/inguzdev/gitly/dto/ApiResponse.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private ResponseStatus status;
    private String message;
    private T data;
    private ApiError error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String path;
}
```

**Usage Examples:**

```java
// Building a complete response
ApiResponse<String> response = ApiResponse.<String>builder()
    .status(ResponseStatus.SUCCESS)
    .message("Operation completed")
    .data("result-data")
    .timestamp(LocalDateTime.now())
    .path("/api/endpoint")
    .build();

// Building with only required fields
ApiResponse<Void> simpleResponse = ApiResponse.<Void>builder()
    .status(ResponseStatus.SUCCESS)
    .message("Success")
    .build();

// Using factory methods (which internally use the builder)
ApiResponse<MyData> response = ApiResponse.success(data);
ApiResponse<MyData> response = ApiResponse.success("Custom message", data);
```

**Key Features:**

- Generic type support with `<T>`
- Optional fields (status, error, path)
- Default value for timestamp using `@Builder.Default`
- Factory methods that wrap the builder for common use cases
- Fluent, readable API

#### 2. ApiError Builder

**File**: `src/main/java/com/inguzdev/gitly/dto/ApiError.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private String code;
    private String details;
    private List<ValidationError> validationErrors;
    private Map<String, Object> metadata;
    private String stackTrace;
}
```

**Usage Examples:**

```java
// Building a simple error
ApiError error = ApiError.builder()
    .code("BUSINESS_ERROR")
    .details("Operation failed")
    .build();

// Building a detailed validation error
ApiError error = ApiError.builder()
    .code("VALIDATION_ERROR")
    .details("Request validation failed")
    .validationErrors(Arrays.asList(
        ValidationError.builder()
            .field("email")
            .rejectedValue(null)
            .message("Email is required")
            .build()
    ))
    .build();

// Building error with metadata
ApiError error = ApiError.builder()
    .code("RATE_LIMIT_EXCEEDED")
    .details("Too many requests")
    .metadata(Map.of(
        "limit", 100,
        "retryAfter", 60
    ))
    .build();
```

#### 3. ValidationError Builder (Nested)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public static class ValidationError {
    private String field;
    private Object rejectedValue;
    private String message;
}
```

**Usage Example:**

```java
ValidationError error = ValidationError.builder()
    .field("username")
    .rejectedValue("ab")
    .message("Username must be at least 3 characters")
    .build();
```

## Benefits Realized in This Project

### 1. **Readability**

```java
// Clear and self-documenting
ApiResponse<User> response = ApiResponse.<User>builder()
    .status(ResponseStatus.SUCCESS)
    .message("User created successfully")
    .data(user)
    .build();
```

### 2. **Flexibility**

```java
// Can include error details
ApiError error = ApiError.builder()
    .code("NOT_FOUND")
    .details("Resource not found")
    .build();

// Or include validation errors
ApiError errorWithValidation = ApiError.builder()
    .code("VALIDATION_ERROR")
    .validationErrors(errors)
    .build();
```

### 3. **Optional Parameters**

```java
// Only set what you need
ApiResponse<Void> minimal = ApiResponse.<Void>builder()
    .status(ResponseStatus.SUCCESS)
    .message("OK")
    .build();

// Full configuration when needed
ApiResponse<Data> full = ApiResponse.<Data>builder()
    .status(ResponseStatus.SUCCESS)
    .message("Complete")
    .data(data)
    .error(null)
    .timestamp(LocalDateTime.now())
    .path("/api/resource")
    .build();
```

### 4. **Type Safety**

```java
// Generic type is preserved
ApiResponse<List<User>> users = ApiResponse.<List<User>>builder()
    .data(userList)
    .build();
```

### 5. **Immutability**

Once built, the object can be made immutable (when used with `@Value` instead of `@Data`), preventing accidental modifications.

## Factory Methods + Builder Pattern

The project combines Builder pattern with Factory methods for best of both worlds:

```java
// Factory methods provide convenience
public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
            .status(ResponseStatus.SUCCESS)
            .message("Request processed successfully")
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
}

// Usage: Simple and clean
return ResponseEntity.ok(ApiResponse.success(data));

// But builder is still available for custom cases
return ResponseEntity.ok(
    ApiResponse.<MyData>builder()
        .status(ResponseStatus.SUCCESS)
        .message("Custom message")
        .data(data)
        .path(request.getRequestURI())
        .build()
);
```

## When to Use Builder Pattern

### ✅ Use Builder When:

1. **Multiple optional parameters** (3+ optional fields)
2. **Complex object construction** with many configuration options
3. **Need for immutability** after construction
4. **Readability is important** (self-documenting code)
5. **Telescoping constructors** would be needed
6. **Multiple representations** of the same object needed

### ❌ Don't Use Builder When:

1. **Simple objects** with 1-2 fields
2. **Required parameters only** (constructor is simpler)
3. **Mutable objects** that change frequently
4. **Performance critical** sections (minimal overhead, but exists)

## Lombok Annotations Explained

```java
@Data                    // Generates getters, setters, toString, equals, hashCode
@Builder                 // Generates builder pattern implementation
@NoArgsConstructor      // Generates no-argument constructor (required for Jackson)
@AllArgsConstructor     // Generates all-arguments constructor (required for Builder)
@JsonInclude(...)       // Jackson annotation to exclude null fields from JSON

public class MyClass {
    private String field1;

    @Builder.Default    // Sets default value in builder
    private String field2 = "default";
}
```

## Comparison: Before vs After

### Before Builder Pattern:

```java
// Hard to read, error-prone
public ApiResponse(ResponseStatus status, String message, T data,
                   ApiError error, LocalDateTime timestamp, String path) {
    this.status = status;
    this.message = message;
    this.data = data;
    this.error = error;
    this.timestamp = timestamp;
    this.path = path;
}

// Usage - what does each parameter mean?
new ApiResponse(
    ResponseStatus.SUCCESS,
    "Success",
    data,
    null,
    LocalDateTime.now(),
    "/api/path"
);
```

### After Builder Pattern:

```java
// Clear, readable, maintainable
ApiResponse.<T>builder()
    .status(ResponseStatus.SUCCESS)
    .message("Success")
    .data(data)
    .timestamp(LocalDateTime.now())
    .path("/api/path")
    .build();
```

## Real-World Examples from the Project

### Example 1: GlobalExceptionHandler

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiResponse<Void>> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request) {

    List<ValidationError> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::mapFieldError)
            .collect(Collectors.toList());

    // Clean builder usage for complex error
    ApiError error = ApiError.builder()
            .code("VALIDATION_ERROR")
            .details("Request validation failed")
            .validationErrors(validationErrors)
            .build();

    // Factory method uses builder internally
    ApiResponse<Void> response = ApiResponse.<Void>error("Validation failed", error)
            .withPath(request.getRequestURI());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
}
```

### Example 2: Building Validation Errors

```java
private ValidationError mapFieldError(FieldError fieldError) {
    return ValidationError.builder()
            .field(fieldError.getField())
            .rejectedValue(fieldError.getRejectedValue())
            .message(fieldError.getDefaultMessage())
            .build();
}
```

### Example 3: Controller Usage

```java
@GetMapping("/health")
public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
    Map<String, Object> healthData = new HashMap<>();
    healthData.put("status", "UP");
    healthData.put("service", "Gitly API");

    // Using factory method (which uses builder internally)
    ApiResponse<Map<String, Object>> response = ApiResponse.success(
        "Application is healthy and running",
        healthData
    );

    return ResponseEntity.ok(response);
}
```

## Testing with Builders

Builders make testing much easier:

```java
@Test
void testSuccessResponse() {
    ApiResponse<String> response = ApiResponse.<String>builder()
        .status(ResponseStatus.SUCCESS)
        .message("Test message")
        .data("test-data")
        .build();

    assertEquals(ResponseStatus.SUCCESS, response.getStatus());
    assertEquals("Test message", response.getMessage());
    assertEquals("test-data", response.getData());
}

@Test
void testErrorWithValidation() {
    ValidationError error = ValidationError.builder()
        .field("email")
        .message("Invalid email")
        .build();

    ApiError apiError = ApiError.builder()
        .code("VALIDATION_ERROR")
        .validationErrors(List.of(error))
        .build();

    assertNotNull(apiError.getValidationErrors());
    assertEquals(1, apiError.getValidationErrors().size());
}
```

## Best Practices

1. **Use `@Builder.Default`** for fields that should have default values
2. **Combine with factory methods** for common use cases
3. **Keep builders simple** - don't add complex logic in builder methods
4. **Consider immutability** - use `@Value` instead of `@Data` for true immutability
5. **Document builder usage** in complex cases
6. **Use generic builders** when working with different types
7. **Avoid null values** - use `@JsonInclude(JsonInclude.Include.NON_NULL)`

## Related Patterns

- **Factory Pattern**: Used alongside Builder for common object configurations
- **Fluent Interface**: Builder implements fluent interface for method chaining
- **Step Builder**: Advanced variant that enforces build order (not used here)
