---
name: kotlin-tdd
description: Kotlin Test-Driven Development with fakes, object mothers, and Testing Through The Domain. Uses TestContext for dependency injection, extension functions for test data, and fakes instead of mocks. Use when writing Kotlin tests or setting up test infrastructure.
---

STARTER_CHARACTER = 🧪

Kotlin TDD approach built on three pillars: Test Setup, Fakes, and Testing Through The Domain (TTTD).

## Test qualities to aim for

- Predictable (not flaky)
- Readable
- Easy to write
- Maintainable (resistant to irrelevant changes)
- Fast (all in-memory)

## Three pillars

### 1. Test Setup

Extension functions on companion objects create test data with sensible defaults.

```kotlin
// In test source: TestExtensions.kt
fun Customer.Companion.valid() = Customer(
    id = UUID.randomUUID(),
    name = "Test Customer",
    active = true,
)

fun Application.Companion.valid(
    customerId: UUID,
    monthsOld: Long = 0,  // Helper for complex setup
) = Application(
    id = UUID.randomUUID(),
    customerId = customerId,
    name = "Tester One",
    birthDate = LocalDate.of(1978, 2, 23),
    applicationDate = LocalDate.now().minusMonths(monthsOld),
    status = ApplicationStatus.ACTIVE,
)
```

Use `.copy()` for simple variations:
```kotlin
val deniedApp = Application.valid(customer.id).copy(status = ApplicationStatus.DENIED)
```

Use helper parameters for complex variations that would be verbose with `.copy()`.

For detailed patterns, see [test-setup.md](test-setup.md).

### 2. Fakes

HashMap-based implementations that replace real dependencies. No mocking frameworks needed.

```kotlin
class ApplicationRepositoryFake : ApplicationRepository {
    private val db = mutableMapOf<UUID, Application>()

    override fun addApplication(application: Application) {
        db[application.id] = application
    }

    override fun getApplication(applicationId: UUID): Application = db[applicationId]!!

    override fun getApplicationsForName(name: String): List<Application> =
        db.values.filter { it.name == name }
}
```

For verification and error testing patterns, see [fakes.md](fakes.md).

### 3. Testing Through The Domain (TTTD)

Set up test state using domain operations, not direct data manipulation.

```kotlin
// Data-oriented (brittle)
repositories.applicationRepo.addApplication(application)
applicationService.approveApplication(application.id)

// Domain-oriented (resilient)
applicationService.registerInitialApplication(customer, application)
applicationService.approveApplication(application.id)
```

The domain-oriented version survives changes to domain logic. When `registerInitialApplication` adds customer validation, tests using the domain approach keep working.

For detailed explanation, see [tttd.md](tttd.md).

## SystemTestContext pattern

Central test context with fakes injected:

```kotlin
class SystemTestContext : SystemContext() {
    class Repositories : SystemContext.Repositories() {
        override val applicationRepo = ApplicationRepositoryFake()
        override val customerRepository = CustomerRegisterClientFake()
    }

    class Clients : SystemContext.Clients() {
        override val userNotificationClient = UserNotificationClientFake()
    }

    override val repositories = Repositories()
    override val clients = Clients()
    override val clock = TestClock.now()
}
```

Usage in tests:
```kotlin
class ApplicationTest {
    private val testContext = SystemTestContext()

    @Test
    fun shouldApproveApplication() {
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

## TestClock for time control

```kotlin
class TestClock private constructor(private var dateTime: ZonedDateTime) : Clock() {
    companion object {
        fun at(dateTime: ZonedDateTime): TestClock = TestClock(dateTime)
        fun now(): TestClock = at(ZonedDateTime.now())
    }

    override fun instant(): Instant = dateTime.toInstant()
    override fun withZone(zone: ZoneId?): Clock = TestClock(dateTime.withZoneSameInstant(zone ?: ZoneId.systemDefault()))
    override fun getZone(): ZoneId = dateTime.zone

    fun advance(duration: Duration) { dateTime = dateTime.plus(duration) }
    fun setTo(newDateTime: ZonedDateTime) { dateTime = newDateTime }
    fun setTo(localDate: LocalDate) { dateTime = localDate.atStartOfDay(dateTime.zone) }
}
```

Usage:
```kotlin
@Test
fun shouldExpireOldApplications() {
    with(testContext) {
        clock.setTo(LocalDate.of(2022, 1, 1))
        val customer = Customer.valid()
        val application = Application.valid(customer.id)
        applicationService.registerInitialApplication(customer, application)

        clock.advance(Duration.ofDays(7 * 30))  // 7 months later
        applicationService.expireApplications()

        assertThat(applicationService.activeApplicationFor(application.name))
            .doesNotContain(application)
    }
}
```

## Test types

- **Domain tests** (no fakes): Pure business logic, no I/O
- **IO tests** (no fakes): HTTP calls, SQL queries - verify adapter correctness
- **Variation tests** (with fakes): Edge cases, specific variations
- **Outcome tests** (with fakes): Interactions between components, end-to-end flows

## Anti-patterns

- Using mocks when fakes would work (fakes are reusable, mocks are not)
- Setting up test data directly in repositories instead of through domain operations
- Verifying method calls instead of system state
- Creating new test data factories for each test file (centralize in TestExtensions.kt)
- Testing DTOs at interface boundaries (interfaces should use domain objects)
