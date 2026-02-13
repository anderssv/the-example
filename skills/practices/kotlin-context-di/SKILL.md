---
name: kotlin-context-di
description: Manual dependency injection using SystemContext (production) and TestContext (test doubles) patterns for Kotlin. Use when structuring service dependencies, wiring application components, or creating test contexts without DI frameworks.
---

STARTER_CHARACTER = 🔌

# Manual Dependency Injection with SystemContext and TestContext

Structure Kotlin applications using manual DI with SystemContext (production) and TestContext (test) patterns. This approach provides type-safe dependency management, full control over initialization, and excellent testability without framework overhead.

## Core Pattern: SystemContext

Create an open class that holds all application dependencies. Use **interfaces** to group related components, and implement them as **anonymous objects** inside the context.

```kotlin
open class SystemContext {
    interface Repositories {
        val customerRepo: CustomerRepository
        val orderRepo: OrderRepository
    }
    
    open val repositories: Repositories by lazy {
        object : Repositories {
            override val customerRepo by lazy { CustomerRepositoryImpl(dataSource) }
            override val orderRepo by lazy { OrderRepositoryImpl(dataSource) }
        }
    }
    
    open val customerService by lazy { 
        CustomerService(repositories.customerRepo) 
    }
    
    open val orderService by lazy {
        OrderService(repositories.orderRepo, customerService)
    }
}
```

**Key characteristics:**
- Group related dependencies using **interfaces** (not open classes — see rationale below)
- Implement production wiring as **anonymous objects** inside the context
- Services reference repositories and other services directly
- Use `lazy` for initialization that depends on other context properties or is expensive
- Use direct instantiation when initialization is trivial and has no dependencies

**Why interfaces instead of open classes:**
- Open classes with constructor parameters force test subclasses to satisfy those parameters, even when test fakes never use them (e.g., creating a dummy `DataSource` just to satisfy a constructor)
- Open classes carry implicit coupling: test subclasses inherit production defaults, which can mask test setup errors
- Interfaces have no constructors and no inherited behavior — test implementations must explicitly provide every dependency

**Why anonymous objects for production wiring:**
- Production wiring stays in one place (`SystemContext`)
- Implementation details are private to the context
- The anonymous object naturally captures `dataSource` and other context properties from the enclosing scope
- No need for constructor parameters on the grouping interface

## Test Pattern: TestContext with Typed Test Implementations

Extend SystemContext and override dependency groups with typed test implementations. Use **concrete types** in test implementations to avoid casting.

```kotlin
class SystemTestContext : SystemContext() {
    class TestRepositories : Repositories {
        override val customerRepo = CustomerRepositoryFake()   // concrete type!
        override val orderRepo = OrderRepositoryFake()         // concrete type!
    }
    
    val testRepositories = TestRepositories()
    override val repositories: Repositories get() = testRepositories
}
```

This exploits Kotlin's **covariant return types**: `TestRepositories.customerRepo` has type `CustomerRepositoryFake` (concrete), while still satisfying the interface contract `CustomerRepository` (abstract).

**Two access paths exist in tests:**
- `repositories.customerRepo` — typed as `CustomerRepository` (used by production code)
- `testRepositories.customerRepo` — typed as `CustomerRepositoryFake` (for test assertions and setup)

**In tests — create a fresh context per test:**

```kotlin
@Test
fun testOrderCreation() {
    with(SystemTestContext()) {
        // Arrange - use service methods to set up state
        customerService.registerCustomer(Customer.valid())
        
        // Act
        val order = orderService.createOrder(customerId, items)
        
        // Assert — direct access to fake methods, no casting needed
        assertThat(testRepositories.orderRepo.getSavedOrders())
            .contains(order)
    }
}
```

The `with(SystemTestContext()) { ... }` pattern creates a fresh context per test, provides clean access to all services and repositories, and prevents state leakage between tests.

## Type Safety Benefits

**Compile-time checking:**
- Typos caught immediately
- Refactoring tools work perfectly (rename, move, find usages)
- Missing dependencies fail at compile time, not runtime

**IDE support:**
- Full autocomplete for all dependencies
- Jump to definition works seamlessly
- No string-based lookups or reflection

**Clear dependency graph:**
- Constructor parameters show exact dependencies
- Easy to trace where any component is used
- No hidden framework magic

## Initialization Control

**Direct instantiation** (default):
```kotlin
open val customerService = CustomerService(repositories.customerRepo)
```
Use when initialization is cheap and there are no circular dependencies.

**Lazy initialization:**
```kotlin
open val customerService by lazy {
    CustomerService(repositories.customerRepo)
}
```
Use when:
- Circular dependencies exist (A needs B, B needs A)
- Initialization is expensive (database connections, HTTP clients)
- Component may not be used in all test scenarios
- The value depends on other context properties that may be overridden

**When in doubt, start with direct instantiation.** Add `lazy` only when needed.

## Grouping Dependencies

Organize dependencies by layer or concern using interfaces:

```kotlin
open class SystemContext(private val config: Config) {
    // Data layer
    interface Repositories {
        val userRepo: UserRepository
        val productRepo: ProductRepository
    }
    
    // External services
    interface Clients {
        val paymentClient: PaymentClient
        val emailClient: EmailClient
    }
    
    // Infrastructure
    interface Infrastructure {
        val database: Database
        val cache: Cache
    }
    
    open val infrastructure: Infrastructure by lazy {
        object : Infrastructure {
            override val database by lazy { DatabaseImpl(config.dbUrl) }
            override val cache by lazy { RedisCache(config.redisUrl) }
        }
    }
    
    open val repositories: Repositories by lazy {
        object : Repositories {
            override val userRepo by lazy { UserRepositoryImpl(infrastructure.database) }
            override val productRepo by lazy { ProductRepositoryImpl(infrastructure.database) }
        }
    }
    
    open val clients: Clients by lazy {
        object : Clients {
            override val paymentClient by lazy { PaymentClientImpl(config.paymentApiKey) }
            override val emailClient by lazy { EmailClientImpl(config.smtpConfig) }
        }
    }
    
    // Business logic
    open val userService by lazy { UserService(repositories.userRepo) }
    open val orderService by lazy {
        OrderService(
            repositories.productRepo,
            clients.paymentClient,
            clients.emailClient
        )
    }
}
```

This structure:
- Makes test overriding straightforward (override entire groups)
- Clarifies architectural layers
- Keeps production wiring in one place
- Anonymous objects capture config and other context properties from the enclosing scope

## Test Context: Full Example

```kotlin
class SystemTestContext : SystemContext(Config.test()) {
    class TestRepositories : Repositories {
        override val userRepo = UserRepositoryFake()        // concrete type
        override val productRepo = ProductRepositoryFake()  // concrete type
    }
    
    class TestClients : Clients {
        override val paymentClient = PaymentClientFake()    // concrete type
        override val emailClient = EmailClientFake()        // concrete type
    }
    
    val testRepositories = TestRepositories()
    override val repositories: Repositories get() = testRepositories
    
    val testClients = TestClients()
    override val clients: Clients get() = testClients
}
```

**In tests — no casting needed:**

```kotlin
@Test
fun testEmailSent() {
    with(SystemTestContext()) {
        orderService.completeOrder(orderId)
        
        // Direct access to fake methods via testClients — no casting
        assertThat(testClients.emailClient.sentEmails).hasSize(1)
        assertThat(testClients.emailClient.sentEmails[0].subject)
            .contains("Order Confirmed")
    }
}

@Test
fun testPaymentFailure() {
    with(SystemTestContext()) {
        // Configure fake behavior — no casting
        testClients.paymentClient.failOnNextCharge()
        
        val result = orderService.createOrder(request)
        
        assertThat(result.status).isEqualTo(OrderStatus.PAYMENT_FAILED)
    }
}
```

## Fresh Context Per Test

**Create a fresh context per test when fakes are stateful (the common case):**

```kotlin
@Test
fun `should save order`() {
    with(SystemTestContext()) {
        // Fresh fakes with no accumulated state
        orderService.createOrder(request)
        assertThat(testRepositories.orderRepo.getSavedOrders()).hasSize(1)
    }
}

@Test
fun `should not save order when payment fails`() {
    with(SystemTestContext()) {
        // Independent from the test above
        testClients.paymentClient.failOnNextCharge()
        orderService.createOrder(request)
        assertThat(testRepositories.orderRepo.getSavedOrders()).isEmpty()
    }
}
```

**Why:** Fakes are stateful — `OrderRepositoryFake` accumulates saved orders, `EmailClientFake` accumulates sent emails. Sharing a context across tests causes state from one test to leak into the next, leading to order-dependent failures and flaky tests.

The `with(SystemTestContext()) { ... }` pattern is idiomatic, cheap (no real I/O), and prevents test pollution.

**Share a context only when fakes are truly stateless or when you have explicit reset logic** — this is uncommon.

## Nullable-to-Non-nullable Narrowing in Tests

When production interfaces have nullable dependencies (because configuration may be absent), test implementations can narrow them to non-nullable:

```kotlin
// Production interface — nullable because config may not exist
interface Clients {
    val authClient: AuthClient?
    val notificationClient: NotificationClient?
}

// Test implementation — non-nullable
class TestClients : Clients {
    override val authClient = AuthClientStub()            // non-nullable!
    override val notificationClient = NotificationClientStub()  // non-nullable!
}
```

This is valid Kotlin because non-nullable types are subtypes of nullable types. Tests never need null checks when accessing test clients, even though production code handles the nullable case. This is a significant ergonomic win — test code stays clean and focused on behavior.

## Integration with Test Doubles

TestContext typically contains Fakes (in-memory implementations of interfaces):

```kotlin
class CustomerRepositoryFake : CustomerRepository {
    private val db = mutableMapOf<String, Customer>()
    
    override fun save(customer: Customer) {
        db[customer.id] = customer
    }
    
    override fun findById(id: String): Customer? {
        return db[id]
    }
    
    // Test-specific methods (not in interface)
    fun getSavedCustomers(): List<Customer> = db.values.toList()
    fun failOnNextSave() { /* ... */ }
}
```

The TestContext wires these Fakes and exposes them with concrete types:

```kotlin
class SystemTestContext : SystemContext() {
    class TestRepositories : Repositories {
        override val customerRepo = CustomerRepositoryFake()  // concrete type
    }
    
    val testRepositories = TestRepositories()
    override val repositories: Repositories get() = testRepositories
}
```

Now `customerService` uses `CustomerRepositoryFake` automatically because it references `repositories.customerRepo`, and tests access fake-specific methods via `testRepositories.customerRepo` without casting.

## Application Wiring

**Main entry point:**
```kotlin
fun main() {
    val context = SystemContext(Config.fromEnvironment())
    
    // Start application with context
    val app = Application(
        context.orderService,
        context.userService
    )
    
    app.start()
}
```

**Web framework integration (Ktor example):**
```kotlin
fun Application.module() {
    val context = SystemContext(Config.fromEnvironment())
    
    routing {
        get("/orders/{id}") {
            val orderId = call.parameters["id"]!!
            val order = context.orderService.getOrder(orderId)
            call.respond(order)
        }
        
        post("/orders") {
            val request = call.receive<CreateOrderRequest>()
            val order = context.orderService.createOrder(request)
            call.respond(order)
        }
    }
}
```

Routes access services directly from the context. No framework-specific annotations or registrations needed.

## Why This Pattern Works

**Simplicity:**
- No annotations to learn
- No configuration files
- No classpath scanning or reflection
- Plain Kotlin code

**Debuggability:**
- Step through initialization in debugger
- Set breakpoints in context creation
- No framework magic hiding behavior

**Readability:**
- Dependencies visible in one place
- Constructor calls show exactly what's needed
- No surprising behavior from framework lifecycle

**Test control:**
- Full control over what gets loaded
- Fast test startup (only load what you need)
- Easy to inject test doubles
- No special test runners or annotations
- No casting needed to access test-specific methods

**Flexibility:**
- Change initialization order easily
- Add conditional logic (feature flags, environment checks)
- Compose contexts (production + feature flags)

**Scalability:**
- Pattern stays simple as project grows
- More dependencies just mean more properties in context classes
- No framework limitations or architectural constraints

## Anti-patterns

**Avoid using open classes for dependency grouping:**
```kotlin
// Don't do this — forces test subclasses to satisfy constructor parameters
open class Repositories(private val dataSource: DataSource) {
    open val customerRepo: CustomerRepository = CustomerRepositoryImpl(dataSource)
}
```

Use interfaces instead — they have no constructors and force explicit implementation.

**Avoid casting to access test-specific methods:**
```kotlin
// Don't do this
val emailClient = clients.emailClient as EmailClientFake
assertThat(emailClient.sentEmails).hasSize(1)
```

Use typed test implementations with dual access (`testClients.emailClient`) instead.

**Avoid making everything lazy:**
```kotlin
// Don't do this unless needed
open val customerService by lazy { CustomerService(...) }
open val orderService by lazy { OrderService(...) }
open val productService by lazy { ProductService(...) }
```

Lazy adds complexity. Use direct instantiation unless circular dependencies or expensive initialization require it.

**Avoid deep context hierarchies:**
```kotlin
// Too complex
open class DatabaseContext : InfrastructureContext()
open class RepositoryContext : DatabaseContext()
open class ServiceContext : RepositoryContext()
open class SystemContext : ServiceContext()
```

Keep it flat: one SystemContext with nested interface groups for organization.

**Don't mix with annotation-based DI:**
```kotlin
// Don't mix patterns
@Inject lateinit var customerService: CustomerService  // Framework DI
val orderService = OrderService(repositories.orderRepo)  // Manual DI
```

Choose one approach and stick with it.

## Migration Path

**Adding to existing project:**

1. Create SystemContext with existing components
2. Wire main entry point to use context
3. Gradually move initialization logic into context
4. Create TestContext and migrate tests incrementally

**From framework DI:**

1. Create parallel SystemContext alongside framework
2. New code uses SystemContext
3. Gradually migrate existing code
4. Remove framework once migration complete

No big-bang rewrite required. Adopt incrementally.
