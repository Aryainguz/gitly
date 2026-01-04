# Virtual Threads Implementation Guide (Java 21 + Spring Boot 3.2+)

## 🚀 Overview

Virtual threads (Project Loom) enable **massive concurrency** with blocking I/O, achieving the scalability of reactive programming with the simplicity of traditional blocking code.

### What Changed

- **Traditional (Platform) Threads**: Limited by OS (~1-2 threads per CPU core), expensive (1-2MB stack)
- **Virtual Threads**: Millions of lightweight threads (few KB each), managed by JVM

## ✅ Configuration

### 1. Enable Virtual Threads in `application.properties`

```properties
# Simple one-line configuration (Spring Boot 3.2+)
spring.threads.virtual.enabled=true
```

### 2. Configure Tomcat for Virtual Threads (VirtualThreadConfig.java)

Already implemented in `src/main/java/com/inguzdev/gitly/config/VirtualThreadConfig.java`

This configuration:

- Uses virtual threads for all incoming HTTP requests
- Enables `@Async` methods to run on virtual threads
- Replaces default platform thread pool with virtual thread executor

## 🎯 When to Use Virtual Threads

### ✅ PERFECT For (I/O-Bound Operations)

1. **Database Queries**: JDBC blocking calls

   ```java
   List<User> users = userRepository.findAll(); // Blocks safely!
   ```

2. **REST API Calls**: HTTP client blocking requests

   ```java
   String response = restClient.get()
       .uri("/api/data")
       .retrieve()
       .body(String.class); // Blocking is fine!
   ```

3. **File I/O**: Reading/writing files

   ```java
   String content = Files.readString(Path.of("data.txt")); // Blocks safely
   ```

4. **Message Queues**: Blocking receive operations
   ```java
   Message msg = jmsTemplate.receive(); // Blocks efficiently
   ```

### ❌ NOT Ideal For

1. **CPU-Bound Work**: Intensive calculations, data processing

   ```java
   // ❌ Don't do this on virtual threads
   for (int i = 0; i < 1_000_000_000; i++) {
       sum += Math.sqrt(i) * Math.cos(i);
   }
   // Use platform threads or parallel streams instead
   ```

2. **Synchronized Blocks**: Pins the carrier thread

   ```java
   // ⚠️ Avoid synchronized with virtual threads
   synchronized(lock) {
       // This pins the carrier thread!
       Thread.sleep(1000);
   }

   // ✅ Use ReentrantLock instead
   lock.lock();
   try {
       Thread.sleep(1000); // Virtual thread can be unmounted
   } finally {
       lock.unlock();
   }
   ```

## 🔥 Key Benefits

### 1. **Simplicity**: Traditional blocking code

```java
@GetMapping("/user/{id}")
public User getUser(@PathVariable Long id) {
    // Just write blocking code - no reactive complexity!
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));

    // Call external API - blocks safely
    String enrichedData = restClient.get()
        .uri("/external/user/" + id)
        .retrieve()
        .body(String.class);

    user.setEnrichedData(enrichedData);
    return user;
}
```

### 2. **High Concurrency**: Handle millions of requests

- Before: ~200 concurrent requests (with 200 platform threads)
- After: Millions of concurrent requests (with virtual threads)
- Memory: ~2KB per virtual thread vs ~2MB per platform thread

### 3. **Better Debugging**: Clear stack traces

- Virtual threads have normal stack traces
- No complex reactive chains to debug

## ⚠️ Critical Pitfalls

### 1. **Thread Pinning** (Reduces Performance)

Virtual threads get "pinned" to carrier threads when:

#### a) Using `synchronized` blocks

```java
// ❌ BAD: Pins the carrier thread
synchronized(this) {
    Thread.sleep(1000); // Blocks carrier thread!
}

// ✅ GOOD: Use ReentrantLock
private final ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    Thread.sleep(1000); // Virtual thread can be unmounted
} finally {
    lock.unlock();
}
```

#### b) Native method calls or foreign function calls

```java
// ⚠️ Some JNI calls may pin threads
// Check JVM logs with: -Djdk.tracePinnedThreads=full
```

### 2. **Thread-Local Storage** (Memory Leak Risk)

```java
// ❌ BAD: ThreadLocal with millions of virtual threads = memory leak
private static final ThreadLocal<ExpensiveObject> threadLocal =
    new ThreadLocal<>();

@GetMapping("/endpoint")
public void handle() {
    threadLocal.set(new ExpensiveObject()); // Millions of these!
    // Memory leak if not cleaned up
}

// ✅ GOOD: Use scoped values (Java 21+) or pass context explicitly
private static final ScopedValue<ExpensiveObject> scopedValue =
    ScopedValue.newInstance();
```

### 3. **Connection Pool Sizing**

Virtual threads don't need large connection pools!

```properties
# ❌ Before (platform threads): Large pool needed
spring.datasource.hikari.maximum-pool-size=100

# ✅ After (virtual threads): Small pool is fine
spring.datasource.hikari.maximum-pool-size=10
# Virtual threads efficiently share fewer connections
```

**Why?** Virtual threads block efficiently, so fewer connections can serve more requests.

### 4. **CPU-Bound Work Detection**

```java
// ⚠️ This pins the carrier thread for a long time
@GetMapping("/heavy-computation")
public Result compute() {
    // CPU-intensive work on virtual thread = carrier pinned!
    return heavyCalculation();
}

// ✅ Better: Use ForkJoinPool for CPU-bound work
@GetMapping("/heavy-computation")
public Result compute() {
    return CompletableFuture.supplyAsync(
        this::heavyCalculation,
        ForkJoinPool.commonPool() // Use platform threads
    ).join();
}
```

## 📊 Monitoring Virtual Threads

### Enable JVM Diagnostic Flags

```bash
# Detect thread pinning
java -Djdk.tracePinnedThreads=full -jar app.jar

# Monitor virtual thread creation
java -Djdk.virtualThreadScheduler.parallelism=8 \
     -Djdk.virtualThreadScheduler.maxPoolSize=8 \
     -jar app.jar
```

### Actuator Metrics

```java
@GetMapping("/actuator/metrics/virtual-threads")
public Map<String, Object> virtualThreadMetrics() {
    return Map.of(
        "activeVirtualThreads", Thread.activeCount(),
        "carrierThreads", Runtime.getRuntime().availableProcessors()
    );
}
```

## 🧪 Testing Your Implementation

### Test Endpoint

```bash
# Test blocking I/O
curl http://localhost:8080/api/virtual-threads/blocking-io

# Check thread info
curl http://localhost:8080/api/virtual-threads/thread-info

# Test sequential blocking
curl http://localhost:8080/api/virtual-threads/sequential-blocking
```

### Load Test

```bash
# Test with Apache Bench
ab -n 10000 -c 1000 http://localhost:8080/api/virtual-threads/blocking-io

# Or use hey
hey -n 10000 -c 1000 http://localhost:8080/api/virtual-threads/blocking-io
```

## 📚 Best Practices Summary

| ✅ DO                        | ❌ DON'T                              |
| ---------------------------- | ------------------------------------- |
| Use for I/O-bound operations | Use for CPU-bound work                |
| Write simple blocking code   | Use reactive patterns (unless needed) |
| Use `ReentrantLock`          | Use `synchronized` blocks             |
| Keep connection pools small  | Over-provision connection pools       |
| Monitor thread pinning       | Ignore JVM diagnostics                |
| Use `ScopedValue`            | Overuse `ThreadLocal`                 |

## 🔧 Migration Checklist

- [x] Update to Java 21+
- [x] Update to Spring Boot 3.2+ (or 4.0+)
- [x] Add `spring.threads.virtual.enabled=true`
- [x] Configure `TomcatProtocolHandlerCustomizer`
- [ ] Replace `synchronized` with `ReentrantLock` in hot paths
- [ ] Reduce database connection pool size
- [ ] Add JVM monitoring flags
- [ ] Load test to verify improved throughput
- [ ] Check for thread pinning in production logs

## 🎓 Additional Resources

- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [Spring Boot 3.2 Virtual Threads Support](https://spring.io/blog/2023/09/09/all-together-now-spring-boot-3-2-graalvm-native-images-java-21-and-virtual)
- [Inside Java: Project Loom](https://inside.java/tag/loom)

---

**Remember**: Virtual threads are perfect for I/O-bound web applications. They let you write simple, readable blocking code that scales to millions of concurrent requests! 🚀
