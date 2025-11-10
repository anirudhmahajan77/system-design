# Factory Pattern - For Engineers

## The Core Concept

**Factory pattern decouples object creation from object usage.** Instead of using `new` directly, you delegate instantiation to a factory.

## The Problem

```java
// Tight coupling - you depend on concrete implementations
public class PaymentService {
    public void processPayment(PaymentType type, double amount) {
        Payment payment;
        
        if (type == PaymentType.CREDIT_CARD) {
            payment = new CreditCardPayment();  // Direct dependency
        } else if (type == PaymentType.PAYPAL) {
            payment = new PayPalPayment();      // Direct dependency  
        } else if (type == PaymentType.CRYPTO) {
            payment = new CryptoPayment();      // Direct dependency
        }
        
        payment.process(amount);
    }
}
```

**Issues:**
- Violates Open/Closed Principle
- Hard to test (can't mock dependencies)
- Creation logic scattered everywhere
- Difficult to add new payment types

## The Solution

### 1. Simple Factory
```java
public class PaymentFactory {
    public static Payment createPayment(PaymentType type) {
        return switch (type) {
            case CREDIT_CARD -> new CreditCardPayment();
            case PAYPAL -> new PayPalPayment();
            case CRYPTO -> new CryptoPayment();
        };
    }
}

// Usage - now decoupled
public class PaymentService {
    public void processPayment(PaymentType type, double amount) {
        Payment payment = PaymentFactory.createPayment(type);
        payment.process(amount);
    }
}
```

### 2. Factory Method - When you need polymorphism in creation
```java
public abstract class DocumentProcessor {
    // Factory method - subclasses override this
    protected abstract Document createDocument();
    
    public void process(String content) {
        Document doc = createDocument();  // Deferred to subclass
        doc.parse(content);
        doc.validate();
        doc.save();
    }
}

public class PdfProcessor extends DocumentProcessor {
    @Override
    protected Document createDocument() {
        return new PdfDocument();  // PDF-specific creation
    }
}

public class WordProcessor extends DocumentProcessor {
    @Override  
    protected Document createDocument() {
        return new WordDocument();  // Word-specific creation
    }
}
```

### 3. Abstract Factory - For families of related objects
```java
// When you need to create compatible object families
public interface CloudProviderFactory {
    Compute createCompute();
    Storage createStorage();
    Networking createNetworking();
}

public class AwsFactory implements CloudProviderFactory {
    public Compute createCompute() { return new Ec2Instance(); }
    public Storage createStorage() { return new S3Storage(); }
    public Networking createNetworking() { return new AwsVpc(); }
}

public class AzureFactory implements CloudProviderFactory {
    public Compute createCompute() { return new AzureVm(); }
    public Storage createStorage() { return new BlobStorage(); } 
    public Networking createNetworking() { return new AzureVnet(); }
}

// Ensures all components work together
public class CloudDeployment {
    private CloudProviderFactory factory;
    
    public CloudDeployment(CloudProviderFactory factory) {
        this.factory = factory;
    }
    
    public void deploy() {
        Compute compute = factory.createCompute();
        Storage storage = factory.createStorage();
        Networking networking = factory.createNetworking();
        
        // All components are compatible
        networking.configure();
        compute.attachStorage(storage);
    }
}
```

## Real-World Use Cases

### Database Abstraction
```java
public interface DbFactory {
    Connection createConnection();
    QueryBuilder createQueryBuilder();
    Transaction createTransaction();
}

// Different factories for different databases
public class PostgresFactory implements DbFactory { ... }
public class MySQLFactory implements DbFactory { ... }
public class OracleFactory implements DbFactory { ... }
```

### Testing with Factories
```java
public class UserServiceTest {
    @Test
    void shouldProcessPayment() {
        // Mock factory for testing
        PaymentFactory mockFactory = mock(PaymentFactory.class);
        when(mockFactory.createPayment(any())).thenReturn(mockPayment);
        
        UserService service = new UserService(mockFactory);
        service.processOrder();
        
        verify(mockPayment).process(anyDouble());
    }
}
```

## Advanced Patterns

### Cached Factory
```java
public class ThreadPoolFactory {
    private final Map<String, ExecutorService> pools = new ConcurrentHashMap<>();
    
    public ExecutorService getCachedThreadPool(String name) {
        return pools.computeIfAbsent(name, k -> 
            Executors.newCachedThreadPool()
        );
    }
}
```

### Parameterized Factory
```java
public class ConnectionFactory {
    public Connection createConnection(DataSourceConfig config) {
        return switch (config.getType()) {
            case MYSQL -> new MySqlConnection(config);
            case POSTGRES -> new PostgresConnection(config);
            case ORACLE -> new OracleConnection(config);
        };
    }
}
```

## When to Use Which

- **Simple Factory**: Basic object creation, few types, no need for inheritance
- **Factory Method**: Need different creation logic in subclasses, framework development
- **Abstract Factory**: Multiple related object families, ensuring compatibility

## Key Benefits

1. **Loose Coupling**: Client code depends on interfaces, not concrete classes
2. **Single Responsibility**: Creation logic isolated in one place
3. **Open/Closed**: Easy to add new types without modifying existing code
4. **Testability**: Easy to mock factories in tests
5. **Consistency**: Centralized creation logic ensures consistent object configuration

## Common Pitfalls

```java
// ❌ God Factory - does too much
public class UniversalFactory {
    public Object create(String type) { ... }  // Creates everything
}

// ✅ Separate by domain/bounded context
public class PaymentFactory { ... }
public class UserFactory { ... }
public class NotificationFactory { ... }
```

The factory pattern is essentially about **separating the concerns of object usage from object creation**. It's particularly valuable in dependency injection, testing, and when working with complex object graphs.
