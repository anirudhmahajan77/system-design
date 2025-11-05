# Builder Pattern with Class Member

## Core Concept
The builder pattern uses a **mutable builder object** that holds the target class's state, then constructs an **immutable instance** from that state.

## Basic Structure

```java
public class Product {
    private final String requiredField;
    private final String optionalField;
    private final int number;
    
    // Private constructor - only Builder can create instances
    private Product(Builder builder) {
        this.requiredField = builder.product.requiredField;
        this.optionalField = builder.product.optionalField;
        this.number = builder.product.number;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters...
    public String getRequiredField() { return requiredField; }
    public String getOptionalField() { return optionalField; }
    public int getNumber() { return number; }
    
    // Builder class
    public static class Builder {
        // Holds an instance of the product being built
        private Product product;
        
        public Builder() {
            this.product = new Product();
        }
        
        public Builder requiredField(String value) {
            product.requiredField = value;
            return this;
        }
        
        public Builder optionalField(String value) {
            product.optionalField = value;
            return this;
        }
        
        public Builder number(int value) {
            product.number = value;
            return this;
        }
        
        public Product build() {
            validate();
            return new Product(this);
        }
        
        private void validate() {
            if (product.requiredField == null) {
                throw new IllegalStateException("Required field must be set");
            }
        }
    }
    
    // Private no-arg constructor for the builder's internal use
    private Product() {
        // Initialize with default values
        this.requiredField = null;
        this.optionalField = "default";
        this.number = 0;
    }
}
```

## Complete Real-World Example: Database Configuration

```java
public class DatabaseConfig {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final int poolSize;
    private final boolean ssl;
    private final int timeout;
    private final String schema;
    
    // Main constructor - builds from builder's internal state
    private DatabaseConfig(Builder builder) {
        // Copy all values from builder's internal config
        this.host = builder.config.host;
        this.port = builder.config.port;
        this.username = builder.config.username;
        this.password = builder.config.password;
        this.poolSize = builder.config.poolSize;
        this.ssl = builder.config.ssl;
        this.timeout = builder.config.timeout;
        this.schema = builder.config.schema;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getPoolSize() { return poolSize; }
    public boolean isSsl() { return ssl; }
    public int getTimeout() { return timeout; }
    public String getSchema() { return schema; }
    
    // Builder class
    public static class Builder {
        // Internal mutable instance that holds the state
        private DatabaseConfig config;
        
        public Builder() {
            this.config = new DatabaseConfig();
        }
        
        // Fluent setters - modify the internal instance and return 'this'
        public Builder host(String host) {
            config.host = host;
            return this;
        }
        
        public Builder port(int port) {
            config.port = port;
            return this;
        }
        
        public Builder credentials(String username, String password) {
            config.username = username;
            config.password = password;
            return this;
        }
        
        public Builder poolSize(int poolSize) {
            config.poolSize = poolSize;
            return this;
        }
        
        public Builder ssl(boolean ssl) {
            config.ssl = ssl;
            return this;
        }
        
        public Builder timeout(int timeout) {
            config.timeout = timeout;
            return this;
        }
        
        public Builder schema(String schema) {
            config.schema = schema;
            return this;
        }
        
        // Construct the immutable instance
        public DatabaseConfig build() {
            validate();
            return new DatabaseConfig(this);
        }
        
        private void validate() {
            if (config.host == null || config.host.trim().isEmpty()) {
                throw new IllegalStateException("Host is required");
            }
            if (config.port <= 0 || config.port > 65535) {
                throw new IllegalStateException("Invalid port number");
            }
            if (config.username == null || config.username.trim().isEmpty()) {
                throw new IllegalStateException("Username is required");
            }
        }
    }
    
    // Private no-arg constructor - only used by Builder
    private DatabaseConfig() {
        // Set default values
        this.host = null;
        this.port = 5432;
        this.username = null;
        this.password = null;
        this.poolSize = 10;
        this.ssl = false;
        this.timeout = 30;
        this.schema = "public";
    }
}
```

## Usage

```java
// Full configuration
DatabaseConfig config = DatabaseConfig.builder()
    .host("localhost")
    .port(5432)
    .credentials("admin", "secret123")
    .poolSize(20)
    .ssl(true)
    .timeout(60)
    .schema("myapp")
    .build();

// Minimal configuration (uses defaults)
DatabaseConfig minimalConfig = DatabaseConfig.builder()
    .host("db.example.com")
    .credentials("user", "pass")
    .build();

// Partial configuration
DatabaseConfig partialConfig = DatabaseConfig.builder()
    .host("192.168.1.100")
    .port(3306)
    .credentials("root", "password")
    .ssl(true)
    .build();
```

## Advanced Features

### 1. Copy Constructor in Builder

```java
public static class Builder {
    private DatabaseConfig config;
    
    public Builder() {
        this.config = new DatabaseConfig();
    }
    
    // Copy from existing instance
    public Builder(DatabaseConfig existing) {
        this.config = new DatabaseConfig();
        this.config.host = existing.host;
        this.config.port = existing.port;
        this.config.username = existing.username;
        this.config.password = existing.password;
        this.config.poolSize = existing.poolSize;
        this.config.ssl = existing.ssl;
        this.config.timeout = existing.timeout;
        this.config.schema = existing.schema;
    }
    
    // Usage: modify existing configuration
    DatabaseConfig newConfig = DatabaseConfig.builder(existingConfig)
        .poolSize(50)
        .ssl(true)
        .build();
}
```

### 2. Complex Validation with Helper Methods

```java
public static class Builder {
    private DatabaseConfig config;
    
    public Builder() {
        this.config = new DatabaseConfig();
    }
    
    public Builder connectionString(String connectionString) {
        // Parse connection string and set multiple fields
        ConnectionInfo info = parseConnectionString(connectionString);
        config.host = info.host;
        config.port = info.port;
        config.username = info.username;
        config.password = info.password;
        return this;
    }
    
    public Builder forEnvironment(Environment env) {
        switch (env) {
            case DEVELOPMENT:
                config.poolSize = 5;
                config.timeout = 10;
                break;
            case PRODUCTION:
                config.poolSize = 50;
                config.ssl = true;
                config.timeout = 120;
                break;
        }
        return this;
    }
}
```

### 3. HTTP Request Builder Example

```java
public class HttpRequest {
    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;
    private final Duration timeout;
    
    private HttpRequest(Builder builder) {
        this.method = builder.request.method;
        this.url = builder.request.url;
        this.headers = Map.copyOf(builder.request.headers);
        this.body = builder.request.body;
        this.timeout = builder.request.timeout;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private HttpRequest request;
        
        public Builder() {
            this.request = new HttpRequest();
        }
        
        public Builder method(String method) {
            request.method = method;
            return this;
        }
        
        public Builder url(String url) {
            request.url = url;
            return this;
        }
        
        public Builder header(String name, String value) {
            request.headers.put(name, value);
            return this;
        }
        
        public Builder body(String body) {
            request.body = body;
            return this;
        }
        
        public Builder timeout(Duration timeout) {
            request.timeout = timeout;
            return this;
        }
        
        public HttpRequest build() {
            validate();
            return new HttpRequest(this);
        }
        
        private void validate() {
            if (request.method == null) {
                throw new IllegalStateException("HTTP method is required");
            }
            if (request.url == null) {
                throw new IllegalStateException("URL is required");
            }
        }
    }
    
    private HttpRequest() {
        this.method = "GET";
        this.url = null;
        this.headers = new HashMap<>();
        this.body = null;
        this.timeout = Duration.ofSeconds(30);
    }
}
```

## Key Advantages of This Approach

1. **No Field Duplication**: Builder doesn't redeclare all the fields
2. **Centralized Defaults**: Default values set in one place (private constructor)
3. **Easy Modifications**: Builder works directly with the object structure
4. **Clean Copy Operations**: Easy to implement copy constructors
5. **Maintainable**: Adding new fields only requires modifying the main class

## How It Works

1. **Builder constructor** creates a mutable instance of the target class
2. **Fluent methods** modify this mutable instance
3. **Build method** validates and creates an immutable copy
4. **Private constructors** enforce the pattern and provide defaults

This approach is particularly clean and maintainable for complex objects with many configuration options.
