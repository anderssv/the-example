# Fakes

Fakes are working implementations that take shortcuts (like using HashMap instead of a database). They're the default test double for everything, including databases.

## Why fakes over mocks

- **Reusable**: One fake works across all tests
- **Less brittle**: Tests verify state, not implementation details
- **Faster**: All in-memory, no I/O
- **No library needed**: Just implement the interface
- **No flakiness**: Deterministic behavior

## Basic fake pattern

Almost everything can be faked with a HashMap:

```kotlin
class ApplicationRepositoryFake : ApplicationRepository {
    private val db = mutableMapOf<UUID, Application>()

    override fun addApplication(application: Application) {
        db[application.id] = application
    }

    override fun getApplication(applicationId: UUID): Application = db[applicationId]!!

    override fun updateApplication(application: Application) {
        db[application.id] = application
    }

    override fun getApplicationsForName(name: String): List<Application> =
        db.values.filter { it.name == name }

    override fun getAllApplications(statuses: List<ApplicationStatus>): List<Application> =
        db.values.filter { it.status in statuses }.toList()
}
```

Start with IntelliJ's "implement interface" (exception-throwing TODOs), then implement methods as tests require them.

## Domain objects at interface boundaries

Interfaces should accept and return domain objects, not DTOs. This makes fakes simple:

```kotlin
// Interface uses domain objects
interface CustomerClient {
    fun getCustomer(id: String): Customer
}

// Real implementation handles DTO conversion
class CustomerClientImpl : CustomerClient {
    override fun getCustomer(id: String): Customer {
        val dto = httpClient.get<CustomerDTO>("/customers/$id")
        return dto.toDomain()  // Conversion inside adapter
    }
}

// Fake is trivial - just a HashMap
class CustomerClientFake : CustomerClient {
    private val customers = mutableMapOf<String, Customer>()

    fun addCustomer(customer: Customer) {
        customers[customer.id.toString()] = customer
    }

    override fun getCustomer(id: String): Customer = customers[id]!!
}
```

## Verification methods

Add fake-only methods to verify interactions:

```kotlin
class UserNotificationClientFake : UserNotificationClient {
    private val notifications = mutableMapOf<String, MutableList<String>>()

    override fun notifyUser(applicationId: UUID, name: String, message: String) {
        notifications.getOrPut(name, ::mutableListOf).add(message)
    }

    // Fake-only method for verification
    fun getNotificationForUser(name: String): List<String> =
        notifications.getOrDefault(name, emptyList())
}
```

Usage:
```kotlin
@Test
fun shouldNotifyUserWhenApplicationExpires() {
    with(testContext) {
        // ... setup and expire application ...

        assertThat(clients.userNotificationClient.getNotificationForUser(application.name))
            .contains("Your application ${application.id} has expired")
    }
}
```

## Error simulation

Register errors for specific cases:

```kotlin
class UserNotificationClientFake : UserNotificationClient {
    private val notifications = mutableMapOf<String, MutableList<String>>()
    private val failingApplicationIds = mutableSetOf<UUID>()

    fun registerApplicationIdForFailure(applicationId: UUID) {
        failingApplicationIds.add(applicationId)
    }

    override fun notifyUser(applicationId: UUID, name: String, message: String) {
        if (failingApplicationIds.contains(applicationId)) {
            throw IOException("Simulated notification failure for application $applicationId")
        }
        notifications.getOrPut(name, ::mutableListOf).add(message)
    }
}
```

Usage:
```kotlin
@Test
fun shouldHandleNotificationFailure() {
    with(testContext) {
        clients.userNotificationClient.registerApplicationIdForFailure(application.id)

        // ... perform operation that triggers notification ...

        // Assert error handling behavior
    }
}
```

## When to use real implementations

Fakes aren't for everything. Use real implementations (IO tests) when:

- Testing SQL queries in repositories
- Testing HTTP client response parsing
- Testing serialization/deserialization
- Validating integration with external APIs

```kotlin
// IO test with MockEngine for HTTP
@Test
fun shouldFetchEntityByOrganizationNumber() = runTest {
    val mockEngine = MockEngine { request ->
        assertThat(request.url.toString())
            .isEqualTo("https://api.example.com/entities/112233445")
        respond(
            content = responseBody,
            status = HttpStatusCode.OK,
            headers = responseHeaders
        )
    }
    val client = EntityClientImpl(EntityClientImpl.client(mockEngine))

    val entity = client.getEntity("112233445")

    assertThat(entity).isNotNull
}
```

## Fakes vs other test doubles

| Test Double | State | Verification | Reusability | When to use |
|-------------|-------|--------------|-------------|-------------|
| **Dummy** | None (never used) | None | N/A | Fill parameter lists |
| **Fake** | Working implementation | Via state | High | Default choice |
| **Stub** | Canned responses | None | Medium | Simple return values |
| **Spy** | Real + recording | Interaction | Low | Testing framework internals |
| **Mock** | Records calls | Interaction | Low | Very rare (5 times in 5 years) |

Default to fakes. Use mocks only for testing error codes (like HTTP 500) that are hard to simulate otherwise.

### State vs behavior verification

**State verification** (preferred): Check what happened to the system's state:
```kotlin
@Test
fun orderReducesInventory() {
    val warehouse = WarehouseFake()
    warehouse.add("Whiskey", 50)

    order.fill(warehouse)

    assertThat(warehouse.inventory("Whiskey")).isEqualTo(0)
}
```

**Behavior verification** (avoid): Check that methods were called:
```kotlin
// Coupled to implementation - breaks on refactoring
val warehouse = mock<Warehouse>()
verify(warehouse, times(1)).remove("Whiskey", 50)
```

State verification works with Fakes, Stubs, and Spies. Behavior verification requires Mocks. Prefer state verification because it survives refactoring.

## Advanced fake patterns

### Composite keys

When entities have composite keys:

```kotlin
data class OrderKey(val customerId: String, val orderNumber: Int)

class OrderRepositoryFake : OrderRepository {
    private val db = mutableMapOf<OrderKey, Order>()

    override fun save(order: Order) {
        db[OrderKey(order.customerId, order.orderNumber)] = order
    }

    override fun findByCustomerAndNumber(
        customerId: String,
        orderNumber: Int
    ): Order? = db[OrderKey(customerId, orderNumber)]
}
```

### Collections and filtering

```kotlin
class ProductRepositoryFake : ProductRepository {
    private val db = mutableMapOf<String, Product>()

    override fun findByCategory(category: String): List<Product> =
        db.values.filter { it.category == category }

    override fun findInStock(): List<Product> =
        db.values.filter { it.quantity > 0 }

    override fun search(query: String): List<Product> =
        db.values.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }
}
```

### Global failure mode

Simulate catastrophic failures (connection loss, service outage):

```kotlin
class CustomerRepositoryFake : CustomerRepository {
    private val db = mutableMapOf<String, Customer>()
    private var connectionLost = false

    fun simulateConnectionLoss() { connectionLost = true }
    fun restoreConnection() { connectionLost = false }

    override fun save(customer: Customer) {
        checkConnection()
        db[customer.id] = customer
    }

    override fun findById(id: String): Customer? {
        checkConnection()
        return db[id]
    }

    private fun checkConnection() {
        if (connectionLost) throw ConnectionException("Database unavailable")
    }
}
```

### Configurable responses

Return different values on successive calls:

```kotlin
class RandomGeneratorFake : RandomGenerator {
    private val queue = mutableListOf<Int>()

    fun queueValues(vararg values: Int) {
        queue.addAll(values.toList())
    }

    override fun nextInt(): Int {
        if (queue.isEmpty()) throw IllegalStateException("No more values queued")
        return queue.removeFirst()
    }
}
```

### When fakes get too complex

If your fake approaches the complexity of the real implementation:

1. **Reconsider the interface** - Maybe it's doing too much
2. **Use the real implementation** - With test containers or embedded versions
3. **Split the interface** - Separate concerns into smaller interfaces
4. **Consider a mock** - For truly complex third-party interfaces

Fakes should be simpler than production. If they're not, something needs to change.

## Getting started in existing codebases

1. Find code that makes an important business decision
2. Pick the one with fewest dependencies
3. Write a test that passes for the logic
4. Write a test for the adapter being exercised
5. Create an interface for the adapter
6. Create a fake
7. Plug in the fake instead of real implementation
8. Clean up
