# Test Setup

Test setup should be:
- Intuitive (focus on the feature, not setup)
- Resilient to unrelated changes
- In a logical location (easy to find via autocomplete)
- Standardized but flexible

## Object Mother pattern with extension functions

Extension functions on companion objects provide discoverable test data factories.

```kotlin
// TestExtensions.kt (in test source root)
package myapp

import java.time.LocalDate
import java.util.*

fun Customer.Companion.valid() = Customer(
    id = UUID.randomUUID(),
    name = "Test Customer",
    active = true,
)

fun Application.Companion.valid(
    customerId: UUID,
    monthsOld: Long = 0,
) = Application(
    id = UUID.randomUUID(),
    customerId = customerId,
    name = "Tester One",
    birthDate = LocalDate.of(1978, 2, 23),
    applicationDate = LocalDate.now().minusMonths(monthsOld),
    status = ApplicationStatus.ACTIVE,
)
```

Requires a companion object in the domain class:
```kotlin
data class Customer(
    val id: UUID,
    val name: String,
    val active: Boolean,
) {
    companion object  // Empty companion enables extension functions
}
```

## Variations

Three approaches, use what fits:

### 1. `.copy()` for simple changes
```kotlin
val deniedApp = Application.valid(customer.id).copy(status = ApplicationStatus.DENIED)
val inactiveCustomer = Customer.valid().copy(active = false)
```

Best when: The variation is clear from reading the test.

### 2. Helper parameters for complex setup
```kotlin
fun Application.Companion.valid(
    customerId: UUID,
    monthsOld: Long = 0,  // Sets applicationDate in the past
) = Application(
    // ...
    applicationDate = LocalDate.now().minusMonths(monthsOld),
)

// Usage
val oldApplication = Application.valid(customer.id, monthsOld = 8)
```

Best when: The calculation is verbose with `.copy()`.

### 3. Named factory methods for common patterns
```kotlin
fun Application.Companion.invalid() = Application(
    // Invalid state configuration
)

fun Customer.Companion.premium(
    name: String = "Premium Customer",
    tier: Tier = Tier.GOLD
) = Customer(
    id = UUID.randomUUID(),
    name = name,
    active = true,
    tier = tier,
)

fun Customer.Companion.unverified(
    name: String = "Unverified Customer"
) = Customer(
    id = UUID.randomUUID(),
    name = name,
    active = true,
    verified = false,
)

fun PaymentBasket.Companion.valid(numberOfTransactions: Int = 1) = PaymentBasket(
    transactions = (1..numberOfTransactions).map { Transaction.valid() }
)
```

Best when: You need the same variation in many tests.

**When to extract a named method:**
1. The scenario appears in 2+ tests
2. It represents a meaningful domain state (premium, expired, minor, rejected)
3. The setup involves complex logic or multiple related objects

Otherwise, use `.valid().copy()` inline in the test.

## Composing Object Mothers for relationships

When objects have relationships, compose Object Mothers:

```kotlin
fun Account.Companion.valid(
    owner: Customer = Customer.valid(),
    balance: BigDecimal = BigDecimal("1000.00"),
    transactions: List<Transaction> = emptyList()
) = Account(
    id = UUID.randomUUID(),
    owner = owner,
    balance = balance,
    transactions = transactions,
)

fun Account.Companion.withTransactions(
    owner: Customer = Customer.valid(),
    count: Int = 3
): Account {
    val transactions = (1..count).map {
        Transaction.valid(amount = BigDecimal("100.$it"))
    }
    return Account(
        id = UUID.randomUUID(),
        owner = owner,
        balance = BigDecimal.ZERO,
        transactions = transactions,
    )
}
```

Usage:
```kotlin
@Test
fun accountSummaryIncludesOwnerName() {
    val customer = Customer.valid().copy(name = "Alice")
    val account = Account.valid(owner = customer)

    val summary = formatter.format(account)

    assertThat(summary).contains("Alice")
}
```

## Object Mother anti-patterns

**Avoid builder complexity:**
```kotlin
// BAD: Over-engineered
Customer.builder()
    .withName("Alice")
    .withAge(30)
    .build()

// GOOD: Simple factory
Customer.valid(name = "Alice", age = 30)
```

**Avoid randomization:**
```kotlin
// BAD: Non-deterministic, hard to debug
fun Customer.Companion.random() = Customer(
    name = UUID.randomUUID().toString(),
    age = Random.nextInt(0, 100)
)

// GOOD: Predictable defaults
fun Customer.Companion.valid(
    name: String = "Alice Smith",
    age: Int = 30
) = Customer(name, age)
```

**Avoid generic naming:**
```kotlin
// BAD: Unclear intent
fun Customer.Companion.create() = ...

// GOOD: Explicit scenarios
fun Customer.Companion.valid() = ...
fun Customer.Companion.invalid() = ...
```

## SystemTestContext pattern

Central dependency injection for tests:

```kotlin
// Production context
open class SystemContext {
    open class Repositories {
        open val applicationRepo: ApplicationRepository by lazy { ApplicationRepositoryImpl(db) }
        open val customerRepository: CustomerRepository by lazy { CustomerRepositoryImpl(db) }
    }

    open class Clients {
        open val userNotificationClient: UserNotificationClient by lazy { UserNotificationClientImpl() }
    }

    open val repositories = Repositories()
    open val clients = Clients()
    open val clock: Clock = Clock.systemDefaultZone()

    val applicationService by lazy {
        ApplicationService(repositories.applicationRepo, repositories.customerRepository, clock)
    }
}
```

```kotlin
// Test context - overrides with fakes
class SystemTestContext : SystemContext() {
    class Repositories : SystemContext.Repositories() {
        override val applicationRepo = ApplicationRepositoryFake()
        override val customerRepository = CustomerRepositoryFake()
    }

    class Clients : SystemContext.Clients() {
        override val userNotificationClient = UserNotificationClientFake()
    }

    override val repositories = Repositories()
    override val clients = Clients()
    override val clock = TestClock.now()
}
```

Key points:
- Override only repositories/clients with fakes
- Services are injected via `lazy` in the superclass (not faked)
- Services use the overridden fakes automatically
- One test context instance per test class

## Usage with `with()` scope function

```kotlin
class ApplicationTest {
    private val testContext = SystemTestContext()

    @Test
    fun shouldRegisterAndApproveApplication() {
        with(testContext) {
            val customer = Customer.valid()
            val application = Application.valid(customer.id)

            applicationService.registerInitialApplication(customer, application)
            applicationService.approveApplication(application.id)

            assertThat(repositories.applicationRepo.getApplication(application.id).status)
                .isEqualTo(ApplicationStatus.APPROVED)
        }
    }
}
```

The `with()` scope function makes all context properties available without prefixing.

## File organization

```
src/test/kotlin/
├── myapp/
│   ├── TestExtensions.kt          # Object mothers for all domain classes
│   ├── ApplicationRepositoryFake.kt
│   └── ApplicationTest.kt
├── system/
│   ├── SystemTestContext.kt       # Central test DI context
│   └── TestClock.kt               # Controllable clock
└── notifications/
    └── UserNotificationClientFake.kt
```

Keep all `valid()` extension functions in one file per module. Fakes live near their interface or in a test package.
