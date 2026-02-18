---
name: kotlin-context-di
description: Manual dependency injection using SystemContext (production) and TestContext (test doubles) patterns for Kotlin. Use when structuring service dependencies, wiring application components, or creating test contexts without DI frameworks.
---

STARTER_CHARACTER = 🔌

# Manual Dependency Injection with AppDependencies, SystemContext and TestContext

Structure Kotlin applications using manual DI with an interface-first contract (`AppDependencies`), a production context (`SystemContext`), and a standalone test context (`SystemTestContext`). This approach provides type-safe dependency management, full control over initialization, and excellent testability without framework overhead.

## Core Pattern: AppDependencies Interface

Define an interface as the contract for all application dependencies. Use nested interfaces to group related components:

```kotlin
interface AppDependencies {
    interface Repositories {
        val customerRepo: CustomerRepository
        val orderRepo: OrderRepository
    }

    interface Clients {
        val paymentClient: PaymentClient
        val emailClient: EmailClient
    }

    interface Services {
        val customerService: CustomerService
        val orderService: OrderService
    }

    val repositories: Repositories
    val clients: Clients
    val services: Services
    val clock: Clock
}
```

Both production and test contexts implement this interface **independently** — no inheritance between them.

**Why an interface (not an open class):**
- Open classes with constructor parameters force test subclasses to satisfy those parameters, even when test fakes never use them (e.g., creating a dummy `DataSource` just to satisfy a constructor)
- `lazy` doesn't protect against production initialization in subclasses — overriding an eager val in a subclass does NOT prevent the base class initializer from running
- Interfaces have no constructors and no inherited behavior — test implementations must explicitly provide every dependency
- Each context handles its own initialization independently — no lazy needed

## Production: SystemContext

A plain class implementing `AppDependencies` — no `open`, no `lazy`, eager val initialization throughout. Infrastructure (DataSource, credentials, JWKS) lives directly on `SystemContext`, not exposed through `AppDependencies`. Grouping implementations are anonymous objects:

```kotlin
class SystemContext(
    private val config: Config,
) : AppDependencies {
    // Infrastructure — not exposed through AppDependencies
    private val dataSource = HikariDataSource(config.dbConfig)

    override val clock: Clock = Clock.systemDefaultZone()

    override val repositories = object : AppDependencies.Repositories {
        override val customerRepo: CustomerRepository = CustomerRepositoryImpl(dataSource)
        override val orderRepo: OrderRepository = OrderRepositoryImpl(dataSource)
    }

    override val clients = object : AppDependencies.Clients {
        override val paymentClient: PaymentClient = PaymentClientImpl(config.paymentApiKey)
        override val emailClient: EmailClient = EmailClientImpl(config.smtpConfig)
    }

    override val services = object : AppDependencies.Services {
        override val customerService = CustomerService(repositories.customerRepo)
        override val orderService = OrderService(
            repositories.orderRepo,
            clients.paymentClient,
            clients.emailClient,
        )
    }
}
```

**Key characteristics:**
- No `open` — this class is not designed for extension
- No `lazy` — eager initialization throughout
- No default values in production config — all config must be explicit per environment
- Infrastructure captured by anonymous objects from the enclosing scope
- Anonymous objects keep production wiring in one place

## Test: SystemTestContext (Standalone)

A **standalone class** implementing `AppDependencies` — does NOT extend `SystemContext`. Uses **inner classes** for groupings (access to enclosing context properties). **Covariant override inference** means concrete fake types are available directly — no dual-access needed:

```kotlin
class SystemTestContext(
    dataSource: DataSource? = null,
) : AppDependencies {

    override val clock = TestClock.now()

    inner class TestRepositories : AppDependencies.Repositories {
        override val customerRepo = CustomerRepositoryFake()   // concrete type!
        override val orderRepo = OrderRepositoryFake()         // concrete type!
    }

    inner class TestClients : AppDependencies.Clients {
        override val paymentClient = PaymentClientFake()       // concrete type!
        override val emailClient = EmailClientFake()           // concrete type!
    }

    override val repositories =
        if (dataSource != null) {
            object : AppDependencies.Repositories {
                override val customerRepo = CustomerRepositoryImpl(dataSource)
                override val orderRepo = OrderRepositoryImpl(dataSource)
            }
        } else {
            TestRepositories()
        }

    override val clients = TestClients()

    override val services = object : AppDependencies.Services {
        override val customerService = CustomerService(repositories.customerRepo)
        override val orderService = OrderService(
            repositories.orderRepo,
            clients.paymentClient,
            clients.emailClient,
        )
    }
}
```

**Key characteristics:**
- Does NOT extend SystemContext — no inheritance between production and test
- **Inner class** for groupings — allows access to enclosing context properties
- **Covariant override inference** — `override val repositories = TestRepositories()` infers the concrete type, so `repositories.customerRepo` resolves to `CustomerRepositoryFake` in test scope
- **No dual-access needed** — no separate `testRepositories` vs `repositories`
- **Constructor injection with defaults** — `SystemTestContext(dataSource = realDs)` for integration, no-arg for unit tests

## Covariant Override Inference

This is the key mechanism that eliminates dual-access properties. When `SystemTestContext` declares:

```kotlin
override val repositories = TestRepositories()
```

Kotlin infers the property type as `TestRepositories` (the concrete type), not `AppDependencies.Repositories` (the interface type). When test code uses `with(SystemTestContext())`, the receiver type is `SystemTestContext`, and `repositories.customerRepo` resolves to `CustomerRepositoryFake`.

**In tests — direct access to fake methods, no casting needed:**

```kotlin
@Test
fun testOrderCreation() {
    with(SystemTestContext()) {
        // Act
        services.orderService.createOrder(customerId, items)

        // Assert — direct access to fake methods via covariant inference
        assertThat(repositories.orderRepo.getSavedOrders())
            .contains(order)
    }
}
```

## Fresh Context Per Test

**Create a fresh context per test when fakes are stateful (the common case):**

```kotlin
@Test
fun `should save order`() {
    with(SystemTestContext()) {
        services.orderService.createOrder(request)
        assertThat(repositories.orderRepo.getSavedOrders()).hasSize(1)
    }
}

@Test
fun `should not save order when payment fails`() {
    with(SystemTestContext()) {
        clients.paymentClient.failOnNextCharge()
        services.orderService.createOrder(request)
        assertThat(repositories.orderRepo.getSavedOrders()).isEmpty()
    }
}
```

**Why:** Fakes are stateful — `OrderRepositoryFake` accumulates saved orders, `EmailClientFake` accumulates sent emails. Sharing a context across tests causes state from one test to leak into the next, leading to order-dependent failures and flaky tests.

The `with(SystemTestContext()) { ... }` pattern is idiomatic, cheap (no real I/O), and prevents test pollution.

**Share a context only when fakes are truly stateless or when you have explicit reset logic** — this is uncommon.

## E2E: Delegation for Partial Overrides

For end-to-end tests that need to replace specific services while keeping the rest intact, use Kotlin's delegation:

```kotlin
val testContext = SystemTestContext()
val dependencies = object : AppDependencies by testContext {
    override val services = object : AppDependencies.Services by testContext.services {
        override val orderService = customOrderService
    }
}
```

This creates a new `AppDependencies` that delegates everything to `testContext` except `services.orderService`, which is replaced with a custom implementation.

## Nullable-to-Non-nullable Narrowing in Tests

When production interfaces have nullable dependencies (because configuration may be absent), test implementations can narrow them to non-nullable:

```kotlin
// Production interface — nullable because config may not exist
interface Clients {
    val authClient: AuthClient?
    val notificationClient: NotificationClient?
}

// Test implementation — non-nullable
inner class TestClients : AppDependencies.Clients {
    override val authClient = AuthClientStub()            // non-nullable!
    override val notificationClient = NotificationClientStub()  // non-nullable!
}
```

This is valid Kotlin because non-nullable types are subtypes of nullable types. Tests never need null checks when accessing test clients, even though production code handles the nullable case. This is a significant ergonomic win — test code stays clean and focused on behavior.

## Route Functions Accept AppDependencies

Route functions (or controllers) accept the `AppDependencies` interface, not the context object — destructure inside:

```kotlin
fun Application.orderRoutes(deps: AppDependencies) {
    routing {
        get("/orders/{id}") {
            val orderId = call.parameters["id"]!!
            val order = deps.services.orderService.getOrder(orderId)
            call.respond(order)
        }

        post("/orders") {
            val request = call.receive<CreateOrderRequest>()
            val order = deps.services.orderService.createOrder(request)
            call.respond(order)
        }
    }
}
```

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

The TestContext wires these Fakes and exposes them with concrete types via covariant override inference:

```kotlin
class SystemTestContext : AppDependencies {
    inner class TestRepositories : AppDependencies.Repositories {
        override val customerRepo = CustomerRepositoryFake()  // concrete type
    }

    override val repositories = TestRepositories()  // inferred as TestRepositories
}
```

Now `services.customerService` uses `CustomerRepositoryFake` automatically because it references `repositories.customerRepo`, and tests access fake-specific methods via `repositories.customerRepo` without casting — the covariant inference gives you the concrete type.

## Application Wiring

**Main entry point:**
```kotlin
fun main() {
    val context = SystemContext(Config.fromEnvironment())

    val app = Application(
        context.services.orderService,
        context.services.userService,
    )

    app.start()
}
```

**Web framework integration (Ktor example):**
```kotlin
fun Application.module() {
    val context = SystemContext(Config.fromEnvironment())

    orderRoutes(context)
    userRoutes(context)
}
```

Routes accept `AppDependencies`, not the context object. No framework-specific annotations or registrations needed.

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
- Compose contexts using delegation

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

**Avoid lazy in production context:**
```kotlin
// Don't do this — lazy doesn't protect against production initialization in subclasses
open class SystemContext {
    open val repositories by lazy { ... }
}
```

Lazy adds complexity and gives false security. With interface + standalone implementations, each context initializes independently.

**Avoid inheritance between production and test contexts:**
```kotlin
// Don't do this
class SystemTestContext : SystemContext() {  // Inherits production initialization!
    override val repositories = TestRepositories()
}
```

Use standalone classes that both implement the `AppDependencies` interface.

**Avoid casting to access test-specific methods:**
```kotlin
// Don't do this
val emailClient = clients.emailClient as EmailClientFake
assertThat(emailClient.sentEmails).hasSize(1)
```

Use covariant override inference — inner class groupings give you concrete types automatically.

**Avoid dual-access properties:**
```kotlin
// Don't do this
val testRepositories = TestRepositories()
override val repositories: Repositories get() = testRepositories
```

With standalone test context and covariant override inference, `repositories` already resolves to `TestRepositories`.

**Avoid deep context hierarchies:**
```kotlin
// Too complex
open class DatabaseContext : InfrastructureContext()
open class RepositoryContext : DatabaseContext()
open class ServiceContext : RepositoryContext()
open class SystemContext : ServiceContext()
```

Keep it flat: one AppDependencies interface with nested interface groups for organization.

**Don't mix with annotation-based DI:**
```kotlin
// Don't mix patterns
@Inject lateinit var customerService: CustomerService  // Framework DI
val orderService = OrderService(repositories.orderRepo)  // Manual DI
```

Choose one approach and stick with it.

## Migration Path

**Adding to existing project:**

1. Create `AppDependencies` interface with nested grouping interfaces
2. Create `SystemContext` implementing it with existing components
3. Wire main entry point to use context
4. Gradually move initialization logic into context
5. Create `SystemTestContext` and migrate tests incrementally

**From framework DI:**

1. Create parallel `AppDependencies` + `SystemContext` alongside framework
2. New code uses the interface-first pattern
3. Gradually migrate existing code
4. Remove framework once migration complete

No big-bang rewrite required. Adopt incrementally.
