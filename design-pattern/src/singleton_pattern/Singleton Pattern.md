# Singleton Design Pattern

## Table of Contents
- [What is the Singleton Pattern?](#what-is-the-singleton-pattern)
- [Real-World Analogy](#real-world-analogy)
- [Code Examples](#code-examples)
    - [Basic Singleton (Eager Initialization)](#1-basic-singleton-eager-initialization)
    - [Lazy Initialization (Thread-Safe)](#2-lazy-initialization-thread-safe)
    - [Holder Idiom (Recommended)](#3-best-practice---holder-idiom-recommended)
    - [Double-Checked Locking](#4-double-checked-locking)
- [Why Do We Need Singleton Pattern?](#why-do-we-need-singleton-pattern)
    - [Resource Management](#1-resource-management)
    - [Configuration Management](#2-configuration-management)
    - [Logging Service](#3-logging-service)
- [When to Use Singleton Pattern](#when-to-use-singleton-pattern)
    - [Appropriate Use Cases](#-appropriate-use-cases)
    - [When NOT to Use](#-when-not-to-use)
- [Key Benefits](#key-benefits)
- [Important Considerations](#important-considerations)

## What is the Singleton Pattern?

The Singleton pattern ensures that a class has only **one instance** and provides a **global point of access** to that instance.

## Real-World Analogy

Think of a **government** - there's only one central government that controls everything. Different departments don't create their own governments; they all access the same one.

## Code Examples

### 1. Basic Singleton (Eager Initialization)

```java
public class DatabaseConnection {
    // Single instance created eagerly when class loads
    private static final DatabaseConnection instance = new DatabaseConnection();
    
    private DatabaseConnection() {
        // Private constructor prevents external instantiation
        System.out.println("Database connection established");
    }
    
    public static DatabaseConnection getInstance() {
        return instance;
    }
    
    public void query(String sql) {
        System.out.println("Executing: " + sql);
    }
}
```

### 2. Lazy Initialization (Thread-Safe)

```java
public class Logger {
    private static Logger instance;
    
    private Logger() {
        System.out.println("Logger initialized");
    }
    
    // Synchronized method - thread safe but slower
    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
    
    public void log(String message) {
        System.out.println("LOG: " + message);
    }
}
```

### 3. Best Practice - Holder Idiom (Recommended)

```java
public class ConfigurationManager {
    private Map<String, String> configs = new HashMap<>();
    
    private ConfigurationManager() {
        // Load configurations from file/database
        configs.put("database.url", "localhost:3306/mydb");
        configs.put("app.version", "1.0.0");
        System.out.println("Configuration loaded");
    }
    
    // Holder class for lazy initialization
    private static class Holder {
        static final ConfigurationManager INSTANCE = new ConfigurationManager();
    }
    
    public static ConfigurationManager getInstance() {
        return Holder.INSTANCE;
    }
    
    public String getConfig(String key) {
        return configs.get(key);
    }
}
```

### 4. Double-Checked Locking

```java
public class CacheManager {
    private static volatile CacheManager instance;
    private Map<String, Object> cache = new HashMap<>();
    
    private CacheManager() {
        System.out.println("Cache manager initialized");
    }
    
    public static CacheManager getInstance() {
        if (instance == null) { // First check (unsynchronized)
            synchronized (CacheManager.class) {
                if (instance == null) { // Second check (synchronized)
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    public Object get(String key) {
        return cache.get(key);
    }
}
```

## Why Do We Need Singleton Pattern?

### 1. Resource Management

```java
// Without Singleton - multiple database connections
DatabaseConnection db1 = new DatabaseConnection();
DatabaseConnection db2 = new DatabaseConnection();
// ❌ Wastes resources, creates multiple connections

// With Singleton - single connection
DatabaseConnection db1 = DatabaseConnection.getInstance();
DatabaseConnection db2 = DatabaseConnection.getInstance();
// ✅ Same instance, efficient resource usage
```

### 2. Configuration Management

```java
public class App {
    public static void main(String[] args) {
        ConfigurationManager config1 = ConfigurationManager.getInstance();
        ConfigurationManager config2 = ConfigurationManager.getInstance();
        
        System.out.println(config1.getConfig("app.version"));
        System.out.println(config2.getConfig("app.version"));
        
        // Both references point to the same object
        System.out.println(config1 == config2); // true
    }
}
```

### 3. Logging Service

```java
public class Application {
    public static void main(String[] args) {
        // All parts of application use the same logger
        Logger logger = Logger.getInstance();
        logger.log("Application started");
        
        UserService userService = new UserService();
        userService.createUser("John");
        
        PaymentService paymentService = new PaymentService();
        paymentService.processPayment(100.0);
    }
}

class UserService {
    public void createUser(String name) {
        Logger.getInstance().log("Creating user: " + name);
    }
}

class PaymentService {
    public void processPayment(double amount) {
        Logger.getInstance().log("Processing payment: $" + amount);
    }
}
```

## When to Use Singleton Pattern

### ✅ Appropriate Use Cases:

- **Database connections** - expensive to create multiple times
- **Logging services** - all components should log to same destination
- **Configuration managers** - single source of truth for settings
- **Cache systems** - shared cache across application
- **Thread pools** - manage limited resources efficiently
- **Hardware access** (printers, file systems)

### ❌ When NOT to Use:

- For objects that maintain state (can cause hidden dependencies)
- When you need multiple instances with different configurations
- When testing (singletons can make unit testing difficult)

## Key Benefits

1. **Controlled Access** - single point of access to the instance
2. **Memory Efficiency** - avoids creating multiple instances
3. **Resource Management** - efficient use of expensive resources
4. **Global State** - shared state across application
5. **Lazy Initialization** - instance created only when needed

## Important Considerations

- **Thread Safety** - ensure singleton works correctly in multi-threaded environments
- **Serialization** - implement `readResolve()` method if serializable
- **Reflection** - prevent reflection attacks in security-sensitive applications
- **Testing** - consider using dependency injection for better testability

---

**Note:** The Singleton pattern is powerful but should be used judiciously, as it introduces global state into your application.