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


# Breaking Singleton Pattern - Methods and Code Examples

There are several ways to break the singleton pattern. Here are the main approaches with code examples:

## 1. Reflection Attack

**Most common way to break singleton** - Using Java Reflection API to access private constructor.

```java
import java.lang.reflect.Constructor;

public class ReflectionBreak {
    public static void main(String[] args) throws Exception {
        Singleton singleton1 = Singleton.getInstance();
        Singleton singleton2 = Singleton.getInstance();
        
        System.out.println("Normal case - Same instance? " + (singleton1 == singleton2)); // true
        
        // Breaking using Reflection
        Constructor<Singleton> constructor = Singleton.class.getDeclaredConstructor();
        constructor.setAccessible(true); // Make private constructor accessible
        Singleton brokenSingleton = constructor.newInstance();
        
        System.out.println("After reflection - Same instance? " + (singleton1 == brokenSingleton)); // false
    }
}

class Singleton {
    private static Singleton instance;
    
    private Singleton() {
        System.out.println("Singleton constructor called");
    }
    
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
```

## 2. Serialization Attack

If singleton implements `Serializable`, we can break it through serialization/deserialization.

```java
import java.io.*;

public class SerializationBreak {
    public static void main(String[] args) throws Exception {
        SerializableSingleton singleton1 = SerializableSingleton.getInstance();
        
        // Serialize
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("singleton.ser"));
        out.writeObject(singleton1);
        out.close();
        
        // Deserialize - creates new instance!
        ObjectInputStream in = new ObjectInputStream(new FileInputStream("singleton.ser"));
        SerializableSingleton singleton2 = (SerializableSingleton) in.readObject();
        in.close();
        
        System.out.println("Same instance after serialization? " + (singleton1 == singleton2)); // false
    }
}

class SerializableSingleton implements Serializable {
    private static SerializableSingleton instance;
    
    private SerializableSingleton() {
        System.out.println("SerializableSingleton constructor called");
    }
    
    public static SerializableSingleton getInstance() {
        if (instance == null) {
            instance = new SerializableSingleton();
        }
        return instance;
    }
}
```

## 3. Clone Attack

If singleton implements `Cloneable`, cloning can create duplicate instances.

```java
public class CloneBreak {
    public static void main(String[] args) throws Exception {
        CloneableSingleton singleton1 = CloneableSingleton.getInstance();
        CloneableSingleton singleton2 = (CloneableSingleton) singleton1.clone();
        
        System.out.println("Same instance after clone? " + (singleton1 == singleton2)); // false
    }
}

class CloneableSingleton implements Cloneable {
    private static CloneableSingleton instance;
    
    private CloneableSingleton() {
        System.out.println("CloneableSingleton constructor called");
    }
    
    public static CloneableSingleton getInstance() {
        if (instance == null) {
            instance = new CloneableSingleton();
        }
        return instance;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); // This creates new instance!
    }
}
```

## 4. Multithreading Attack

If not properly synchronized, multiple threads can create multiple instances.

```java
public class MultithreadingBreak {
    public static void main(String[] args) throws InterruptedException {
        final Set<ThreadUnsafeSingleton> instances = Collections.synchronizedSet(new HashSet<>());
        
        // Create multiple threads
        Thread[] threads = new Thread[100];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                ThreadUnsafeSingleton instance = ThreadUnsafeSingleton.getInstance();
                instances.add(instance);
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("Number of instances created: " + instances.size()); // Could be more than 1!
    }
}

class ThreadUnsafeSingleton {
    private static ThreadUnsafeSingleton instance;
    
    private ThreadUnsafeSingleton() {
        System.out.println("ThreadUnsafeSingleton created by: " + Thread.currentThread().getName());
    }
    
    // NOT thread-safe!
    public static ThreadUnsafeSingleton getInstance() {
        if (instance == null) {
            // Simulate some processing time to increase race condition chance
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            instance = new ThreadUnsafeSingleton();
        }
        return instance;
    }
}
```

## 5. Classloader Attack

Using multiple classloaders can create multiple singleton instances.

```java
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderBreak {
    public static void main(String[] args) throws Exception {
        Singleton singleton1 = Singleton.getInstance();
        
        // Create new classloader and load same class
        URL classUrl = Singleton.class.getProtectionDomain().getCodeSource().getLocation();
        URLClassLoader otherClassLoader = new URLClassLoader(new URL[]{classUrl});
        
        Class<?> otherSingletonClass = otherClassLoader.loadClass("Singleton");
        java.lang.reflect.Method getInstanceMethod = otherSingletonClass.getMethod("getInstance");
        Object singleton2 = getInstanceMethod.invoke(null);
        
        System.out.println("Same instance with different classloaders? " + 
                          (singleton1 == singleton2)); // false
        System.out.println("Same class? " + 
                          (singleton1.getClass() == singleton2.getClass())); // false
    }
}
```

## 6. Inheritance Attack

If constructor is not properly secured, subclasses can create instances.

```java
public class InheritanceBreak {
    public static void main(String[] args) {
        ParentSingleton instance1 = ParentSingleton.getInstance();
        ParentSingleton instance2 = new ChildSingleton(); // Oops!
        
        System.out.println("Same instance? " + (instance1 == instance2)); // false
    }
}

class ParentSingleton {
    private static ParentSingleton instance;
    
    // Package-private - vulnerable!
    ParentSingleton() {
        System.out.println("ParentSingleton constructor called");
    }
    
    public static ParentSingleton getInstance() {
        if (instance == null) {
            instance = new ParentSingleton();
        }
        return instance;
    }
}

class ChildSingleton extends ParentSingleton {
    public ChildSingleton() {
        System.out.println("ChildSingleton constructor called");
    }
}
```

## 7. Garbage Collection Attack

In some cases, if singleton reference is lost and recreated.

```java
public class GarbageCollectionBreak {
    public static void main(String[] args) throws Exception {
        WeakReference<Singleton> weakRef;
        
        // First instance
        Singleton singleton1 = Singleton.getInstance();
        weakRef = new WeakReference<>(singleton1);
        
        // Remove strong references
        singleton1 = null;
        
        // Force garbage collection
        System.gc();
        Thread.sleep(1000);
        
        // If GC collected it, we can create new instance
        if (weakRef.get() == null) {
            Singleton singleton2 = Singleton.getInstance();
            System.out.println("New instance created after GC!");
        }
    }
}
```

## Prevention Methods

### 1. Protection Against Reflection

```java
class ReflectionSafeSingleton {
    private static ReflectionSafeSingleton instance;
    
    private ReflectionSafeSingleton() {
        // Prevent reflection attack
        if (instance != null) {
            throw new IllegalStateException("Singleton already initialized!");
        }
    }
    
    public static ReflectionSafeSingleton getInstance() {
        if (instance == null) {
            instance = new ReflectionSafeSingleton();
        }
        return instance;
    }
}
```

### 2. Protection Against Serialization

```java
class SerializationSafeSingleton implements Serializable {
    private static SerializationSafeSingleton instance;
    
    private SerializationSafeSingleton() {}
    
    public static SerializationSafeSingleton getInstance() {
        if (instance == null) {
            instance = new SerializationSafeSingleton();
        }
        return instance;
    }
    
    // This method is called during deserialization
    protected Object readResolve() {
        return getInstance(); // Return the existing instance
    }
}
```

### 3. Protection Against Cloning

```java
class CloneSafeSingleton implements Cloneable {
    private static CloneSafeSingleton instance;
    
    private CloneSafeSingleton() {}
    
    public static CloneSafeSingleton getInstance() {
        if (instance == null) {
            instance = new CloneSafeSingleton();
        }
        return instance;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Singleton cannot be cloned");
    }
}
```

### 4. Enum Singleton (Most Secure)

```java
enum EnumSingleton {
    INSTANCE;
    
    private String data = "Singleton Data";
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public void businessMethod() {
        System.out.println("Business method called");
    }
}

// Usage
public class EnumSingletonDemo {
    public static void main(String[] args) {
        EnumSingleton singleton1 = EnumSingleton.INSTANCE;
        EnumSingleton singleton2 = EnumSingleton.INSTANCE;
        
        System.out.println("Same instance? " + (singleton1 == singleton2)); // true
        singleton1.businessMethod();
    }
}
```

## Summary of Breaking Methods

| Method | How it Breaks | Prevention |
|--------|---------------|------------|
| Reflection | Accesses private constructor | Throw exception in constructor |
| Serialization | Creates new instance during deserialization | Implement `readResolve()` |
| Cloning | Creates copy through `clone()` | Override `clone()` to throw exception |
| Multithreading | Race condition creates multiple instances | Use proper synchronization |
| Classloader | Different classloaders create different instances | Use enum or control classloading |
| Inheritance | Subclass can instantiate parent | Make constructor private |
| Garbage Collection | Lost reference allows recreation | Use static final or enum |

The **enum approach** is generally considered the most secure way to implement singletons in Java, as it's inherently protected against all these attacks.
