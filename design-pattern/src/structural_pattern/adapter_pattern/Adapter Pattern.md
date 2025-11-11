## Adapter Pattern - The Real Deal

### The Core Problem
You have **Client** code that expects **Interface A**, but you need to use **Library B** that implements **Interface B**. The interfaces are incompatible.

```java
// What your code expects
public interface PaymentProcessor {
    PaymentResult process(PaymentRequest request);
}

// What the external library provides
public class StripeApi {
    public StripeCharge createCharge(StripeChargeRequest charge) {
        // returns Stripe-specific response
    }
}

public class PayPalSdk {
    public Payment executePayment(Payment payment) {
        // returns PayPal-specific response  
    }
}
```

### The Adapter Solution
Create a wrapper that **implements your interface** but **delegates to the external library**, handling the conversion between interfaces.

## Real SE2 Implementation

### 1. Payment Gateway Integration

```java
// Your Domain Interfaces
public interface PaymentGateway {
    PaymentResult charge(ChargeRequest request);
    PaymentResult refund(RefundRequest request);
}

public record ChargeRequest(
    String orderId, 
    Money amount, 
    PaymentMethod paymentMethod,
    CustomerInfo customer
) {}

public record PaymentResult(
    String transactionId,
    PaymentStatus status,
    Instant processedAt
) {}

// External SDKs (incompatible interfaces)
public class StripeClient {
    public Charge createCharge(CreateChargeParams params) {
        // Stripe-specific charge creation
        return new Charge("ch_123", "succeeded");
    }
    
    public Refund createRefund(CreateRefundParams params) {
        return new Refund("re_123", "succeeded");
    }
}

public class PayPalHttpClient {
    @POST("/v2/payments")
    public PayPalPayment createPayment(PayPalPaymentRequest request) {
        // PayPal REST API call
        return new PayPalPayment("PAY-123", "COMPLETED");
    }
    
    @POST("/v2/payments/{id}/refund")  
    public PayPalRefund refundPayment(String paymentId) {
        return new PayPalRefund("REF-123", "COMPLETED");
    }
}

// Adapters
@Component
@Primary
public class StripePaymentAdapter implements PaymentGateway {
    private final StripeClient stripe;
    private final StripeConfig config;
    
    public StripePaymentAdapter(StripeClient stripe, StripeConfig config) {
        this.stripe = stripe;
        this.config = config;
    }
    
    @Override
    public PaymentResult charge(ChargeRequest request) {
        // Convert domain request to Stripe-specific format
        CreateChargeParams params = CreateChargeParams.builder()
            .amount(request.amount().toCents())
            .currency(request.amount().currency())
            .customer(extractCustomerId(request.customer()))
            .metadata(Map.of("order_id", request.orderId()))
            .build();
            
        // Call Stripe SDK
        Charge charge = stripe.createCharge(params);
        
        // Convert Stripe response to domain response
        return new PaymentResult(
            charge.getId(),
            mapStatus(charge.getStatus()),
            Instant.now()
        );
    }
    
    @Override
    public PaymentResult refund(RefundRequest request) {
        CreateRefundParams params = CreateRefundParams.builder()
            .charge(request.transactionId())
            .amount(request.amount().toCents())
            .build();
            
        Refund refund = stripe.createRefund(params);
        
        return new PaymentResult(
            refund.getId(),
            mapStatus(refund.getStatus()),
            Instant.now()
        );
    }
    
    private PaymentStatus mapStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "succeeded", "completed" -> PaymentStatus.SUCCEEDED;
            case "pending" -> PaymentStatus.PENDING;
            case "failed" -> PaymentStatus.FAILED;
            default -> PaymentStatus.UNKNOWN;
        };
    }
}

@Component
@Qualifier("paypal")
public class PayPalPaymentAdapter implements PaymentGateway {
    private final PayPalHttpClient paypal;
    private final PayPalConfig config;
    
    public PayPalPaymentAdapter(PayPalHttpClient paypal, PayPalConfig config) {
        this.paypal = paypal;
        this.config = config;
    }
    
    @Override
    public PaymentResult charge(ChargeRequest request) {
        // Convert to PayPal request format
        PayPalPaymentRequest paypalRequest = PayPalPaymentRequest.builder()
            .intent("CAPTURE")
            .purchaseUnits(List.of(
                PurchaseUnit.builder()
                    .amount(new Amount(
                        request.amount().currency(),
                        request.amount().toString()
                    ))
                    .customId(request.orderId())
                    .build()
            ))
            .build();
            
        // Call PayPal API
        PayPalPayment payment = paypal.createPayment(paypalRequest);
        
        // Convert PayPal response to domain response
        return new PaymentResult(
            payment.getId(),
            mapStatus(payment.getStatus()),
            Instant.now()
        );
    }
    
    // Similar implementation for refund...
}
```

### 2. Database Repository Adapters

```java
// Domain Repository Interface
public interface UserRepository {
    Optional<User> findById(UserId id);
    User save(User user);
    List<User> findByCriteria(UserSearchCriteria criteria);
    void delete(UserId id);
}

// JPA Entity (Infrastructure concern)
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private String id;
    private String email;
    private String name;
    private Instant createdAt;
    
    // JPA-specific annotations and methods
}

// Spring Data JPA Repository (External interface)
public interface JpaUserRepository extends JpaRepository<UserEntity, String> {
    List<UserEntity> findByEmailContainingIgnoreCase(String email);
    List<UserEntity> findByCreatedAtAfter(Instant date);
}

// Adapter
@Repository
@Transactional
public class JpaUserRepositoryAdapter implements UserRepository {
    private final JpaUserRepository jpaRepo;
    private final UserMapper mapper;
    
    public JpaUserRepositoryAdapter(JpaUserRepository jpaRepo, UserMapper mapper) {
        this.jpaRepo = jpaRepo;
        this.mapper = mapper;
    }
    
    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepo.findById(id.value())
            .map(mapper::toDomain);
    }
    
    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        UserEntity saved = jpaRepo.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public List<User> findByCriteria(UserSearchCriteria criteria) {
        Specification<UserEntity> spec = buildSpecification(criteria);
        return jpaRepo.findAll(spec).stream()
            .map(mapper::toDomain)
            .collect(toList());
    }
    
    @Override
    public void delete(UserId id) {
        jpaRepo.deleteById(id.value());
    }
    
    private Specification<UserEntity> buildSpecification(UserSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (criteria.email() != null) {
                predicates.add(cb.like(cb.lower(root.get("email")), 
                    "%" + criteria.email().toLowerCase() + "%"));
            }
            
            if (criteria.createdAfter() != null) {
                predicates.add(cb.greaterThan(root.get("createdAt"), criteria.createdAfter()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### 3. Message Queue Adapters

```java
// Domain Event Interface
public interface EventPublisher {
    void publish(DomainEvent event);
}

// AWS SNS (Cloud-specific)
public class AmazonSNSClient {
    public PublishResult publish(PublishRequest request) {
        // AWS SDK call
    }
}

// Kafka (Different messaging system)
public class KafkaTemplate<K, V> {
    public ListenableFuture<SendResult<K, V>> send(String topic, V data) {
        // Spring Kafka call
    }
}

// Adapters
@Component
@Profile("aws")
public class SnsEventPublisherAdapter implements EventPublisher {
    private final AmazonSNSClient sns;
    private final ObjectMapper mapper;
    private final String topicArn;
    
    public SnsEventPublisherAdapter(AmazonSNSClient sns, ObjectMapper mapper) {
        this.sns = sns;
        this.mapper = mapper;
        this.topicArn = "arn:aws:sns:us-east-1:123456789012:domain-events";
    }
    
    @Override
    public void publish(DomainEvent event) {
        try {
            String message = mapper.writeValueAsString(event);
            String subject = event.getClass().getSimpleName();
            
            PublishRequest request = new PublishRequest()
                .withTopicArn(topicArn)
                .withMessage(message)
                .withSubject(subject)
                .withMessageAttributes(createAttributes(event));
                
            sns.publish(request);
            
        } catch (JsonProcessingException e) {
            throw new EventPublishingException("Failed to serialize event", e);
        }
    }
    
    private Map<String, MessageAttributeValue> createAttributes(DomainEvent event) {
        Map<String, MessageAttributeValue> attributes = new HashMap<>();
        attributes.put("eventType", 
            new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(event.getClass().getName()));
        attributes.put("timestamp",
            new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(Instant.now().toString()));
        return attributes;
    }
}

@Component  
@Profile("kafka")
public class KafkaEventPublisherAdapter implements EventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper mapper;
    
    public KafkaEventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate, 
                                    ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
    }
    
    @Override
    public void publish(DomainEvent event) {
        String topic = "domain-events";
        String key = event.getAggregateId();
        
        EventEnvelope envelope = new EventEnvelope(
            event.getClass().getName(),
            event.getAggregateId(),
            event.getVersion(),
            Instant.now(),
            event
        );
        
        kafkaTemplate.send(topic, key, envelope)
            .addCallback(
                result -> log.debug("Event published successfully"),
                failure -> log.error("Failed to publish event", failure)
            );
    }
}
```

### 4. Service Client Adapters for Testing

```java
// Production HTTP Client
@Component
@Primary
public class RealShippingServiceAdapter implements ShippingService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    public RealShippingServiceAdapter(RestTemplate restTemplate, 
                                    @Value("${shipping.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }
    
    @Override
    public ShippingQuote getQuote(ShippingRequest request) {
        ShippingApiRequest apiRequest = mapToApiRequest(request);
        
        ResponseEntity<ShippingApiResponse> response = restTemplate.postForEntity(
            baseUrl + "/quotes", apiRequest, ShippingApiResponse.class);
            
        return mapToDomainQuote(response.getBody());
    }
    
    @Override
    public Shipment createShipment(CreateShipmentRequest request) {
        // Similar implementation...
    }
}

// Test Adapter (for integration tests)
@Component
@Profile("test")
public class MockShippingServiceAdapter implements ShippingService {
    private final Map<String, ShippingQuote> preconfiguredQuotes = new HashMap<>();
    private final List<CreateShipmentRequest> createdShipments = new ArrayList<>();
    
    public void configureQuote(String fromZip, String toZip, ShippingQuote quote) {
        preconfiguredQuotes.put(fromZip + ":" + toZip, quote);
    }
    
    @Override
    public ShippingQuote getQuote(ShippingRequest request) {
        String key = request.fromZip() + ":" + request.toZip();
        ShippingQuote quote = preconfiguredQuotes.get(key);
        
        if (quote == null) {
            throw new ShippingException("No quote configured for: " + key);
        }
        
        return quote;
    }
    
    @Override
    public Shipment createShipment(CreateShipmentRequest request) {
        createdShipments.add(request);
        return new Shipment(
            "mock-shipment-" + System.currentTimeMillis(),
            request.orderId(),
            "MOCK_TRACKING",
            ShipmentStatus.CREATED
        );
    }
    
    public List<CreateShipmentRequest> getCreatedShipments() {
        return List.copyOf(createdShipments);
    }
    
    public void reset() {
        preconfiguredQuotes.clear();
        createdShipments.clear();
    }
}
```

## Key Engineering Benefits

1. **Dependency Inversion**: Your domain code depends on abstractions, not concrete implementations
2. **Testability**: Easy to swap real adapters with test doubles
3. **Infrastructure Isolation**: Domain logic is separate from technical concerns
4. **Multi-Cloud/Provider Support**: Switch between AWS, Azure, GCP without changing business logic
5. **Technology Migration**: Gradually migrate from MongoDB to PostgreSQL without breaking changes

## Usage in Spring Applications

```java
@Configuration
public class PaymentConfig {
    
    @Bean
    @ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
    public PaymentGateway stripePaymentGateway(StripeClient stripe, StripeConfig config) {
        return new StripePaymentAdapter(stripe, config);
    }
    
    @Bean
    @ConditionalOnProperty(name = "payment.provider", havingValue = "paypal")  
    public PaymentGateway paypalPaymentGateway(PayPalHttpClient paypal, PayPalConfig config) {
        return new PayPalPaymentAdapter(paypal, config);
    }
}

@Service
public class OrderService {
    private final PaymentGateway paymentGateway;
    
    // Inject the appropriate adapter based on configuration
    public OrderService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
    
    public Order processPayment(Order order, PaymentMethod paymentMethod) {
        ChargeRequest chargeRequest = createChargeRequest(order, paymentMethod);
        PaymentResult result = paymentGateway.charge(chargeRequest);
        
        return order.withPayment(result.transactionId());
    }
}
```

The adapter pattern is fundamental in **clean architecture** and **hexagonal architecture** for keeping your core domain logic independent of external concerns like databases, APIs, and frameworks.
