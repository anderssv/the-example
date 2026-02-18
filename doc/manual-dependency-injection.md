Part of [System Design](system-design.md) and [TDD](tdd.md).

---

> I am an independent consultant and would love to help your team get better at continuous delivery.
> Reach out
> at [anders.sveen@mikill.no](mailto:anders.sveen@mikill.no) or go
> to [https://www.mikill.no](https://www.mikill.no/contact.html) to contact, follow on social media or to see more of
> my work.

Dependency Injection plays a key role in systems development, especially with automated testing and TDD.
It is all about molding an effective dependency graph for your application and tests.
Frameworks often seem appealing, but their automated nature can obscure vital feedback from your code.
This feedback is of immense value in enhancing your system structure.

If you are not writing tests, or mostly writing integration tests, you might not feel that DI is important.
But once you write fine-grained tests and isolate out infrastructure and dependencies, it becomes essential.

I usually do manual dependency injection.

> See the dependency injection example in [SystemContext.kt](../src/main/kotlin/system/SystemContext.kt)
and how to set up a separate context for testing in [SystemTestContext.kt](../src/test/kotlin/system/SystemTestContext.kt).

## The Pattern: Interface-First Contract

The core pattern uses an **interface** (`AppDependencies`) as the contract, with nested grouping interfaces for `Repositories`, `Clients`, and `Services`. Both production (`SystemContext`) and test (`SystemTestContext`) contexts implement it **independently** — no inheritance between them.

```kotlin
interface AppDependencies {
    interface Repositories {
        val applicationRepo: ApplicationRepository
    }

    interface Clients {
        val customerRepository: CustomerRegisterClient
        val userNotificationClient: UserNotificationClient
    }

    interface Services {
        val applicationService: ApplicationService
    }

    val repositories: Repositories
    val clients: Clients
    val services: Services
    val clock: Clock
}
```

### Production: SystemContext

A plain class — no `open`, no `lazy`, eager val initialization throughout. Infrastructure (like `DataSource`) lives directly on `SystemContext`, not exposed through `AppDependencies`. Grouping implementations are anonymous objects:

```kotlin
class SystemContext(
    private val dataSource: DataSource,
) : AppDependencies {

    override val clock: Clock = Clock.systemDefaultZone()

    override val repositories = object : AppDependencies.Repositories {
        override val applicationRepo: ApplicationRepository = ApplicationRepositoryImpl(dataSource)
    }

    override val clients = object : AppDependencies.Clients {
        override val customerRepository: CustomerRegisterClient = CustomerRegisterClientImpl()
        override val userNotificationClient: UserNotificationClient = UserNotificationClientImpl()
    }

    override val services = object : AppDependencies.Services {
        override val applicationService = ApplicationService(
            repositories.applicationRepo,
            clients.customerRepository,
            clients.userNotificationClient,
            clock,
        )
    }
}
```

### Why Interfaces Instead of Open Classes?

**The problem with open classes:**
```kotlin
// Don't do this
open class Repositories(private val dataSource: DataSource) {
    open val applicationRepo: ApplicationRepository = ApplicationRepositoryImpl(dataSource)
}
```

When you use open classes with constructor parameters, test subclasses are forced to satisfy those parameters even when test fakes never use them:

```kotlin
// Test subclass is forced to provide a dataSource it doesn't need
class TestRepositories : Repositories(DummyDataSource()) {  // <- Awkward!
    override val applicationRepo = ApplicationRepositoryFake()  // Doesn't even use dataSource!
}
```

**The problem with lazy in open classes:**

Even with `lazy`, overriding an eager val in a subclass does NOT prevent the base class initializer from running. This means production initialization code executes even in tests:

```kotlin
// Don't do this — lazy doesn't protect against production initialization in subclasses
open class SystemContext {
    open val repositories by lazy {
        object : Repositories {
            override val applicationRepo = ApplicationRepositoryImpl(dataSource) // Still runs!
        }
    }
}
```

This is why interface + standalone implementations was chosen over open class with overrides.

**The interfaces solution:**
- No constructor parameters to satisfy
- No dummy objects needed in tests
- Test implementations must explicitly provide every dependency (no hidden inherited behavior)
- Production wiring stays in one place (anonymous objects inside SystemContext)
- No lazy needed — each context handles its own initialization independently

### Test: SystemTestContext

A **standalone class** — does NOT extend `SystemContext`. Uses **inner classes** for groupings (allows access to enclosing context properties). **Covariant override inference** means `repositories.applicationRepo` resolves to `ApplicationRepositoryFake` in test scope — no dual-access needed:

```kotlin
class SystemTestContext(
    dataSource: DataSource? = null,
) : AppDependencies {

    override val clock = TestClock.now()

    inner class TestRepositories : AppDependencies.Repositories {
        override val applicationRepo = ApplicationRepositoryFake()  // Concrete type!
    }

    inner class TestClients : AppDependencies.Clients {
        override val customerRepository = CustomerRegisterClientFake()
        override val userNotificationClient = UserNotificationClientFake()
    }

    override val repositories =
        if (dataSource != null) {
            object : AppDependencies.Repositories {
                override val applicationRepo = ApplicationRepositoryImpl(dataSource)
            }
        } else {
            TestRepositories()
        }

    override val clients = TestClients()

    override val services = object : AppDependencies.Services {
        override val applicationService = ApplicationService(
            repositories.applicationRepo,
            clients.customerRepository,
            clients.userNotificationClient,
            clock,
        )
    }
}
```

### Covariant Override Inference

Because `SystemTestContext` declares `override val repositories = TestRepositories()`, Kotlin infers the property type as `TestRepositories` (the concrete type). When test code uses `with(SystemTestContext())`, the receiver type is `SystemTestContext`, and `repositories.applicationRepo` resolves to `ApplicationRepositoryFake` — giving direct access to fake methods without casting and without dual-access properties:

```kotlin
with(SystemTestContext()) {
    services.applicationService.registerApplication(app)

    // Direct access to fake-specific methods — no casting, no testRepositories!
    assertThat(repositories.applicationRepo.getAllApplications())
        .contains(app)
}
```

### Integration Tests with a Real Database

The interface pattern also solves a common problem with database integration tests: the test context should be **fresh per test** (to prevent state leakage), but the database connection pool should be **shared** (to avoid spinning up a new container for each test).

The solution uses a JUnit 5 `ParameterResolver` to own the shared `DataSource` and inject it into the test constructor. The test passes it into a fresh `SystemTestContext`:

```kotlin
@ExtendWith(SharedDataSourceParameterResolver::class)
class MyIntegrationTest(private val dataSource: DataSource) {

    @Test
    fun shouldStoreAndRetrieve() {
        with(SystemTestContext(dataSource = dataSource)) {
            val application = Application.valid(customerId = customer.id)
            repositories.applicationRepo.addApplication(application)

            val retrieved = repositories.applicationRepo.getApplication(application.id)
            assertThat(retrieved).isEqualTo(application)
        }
    }
}
```

This works because:
- **Interfaces have no constructor parameters** — `SystemTestContext()` (no args) never needs a `DataSource`
- **`with()` pattern is consistent** — integration tests look identical to fake-based tests
- **Connection pool is shared** — the `ParameterResolver` stores it in JUnit's root `ExtensionContext.Store`, so one Testcontainers Postgres + one HikariCP pool serves all test classes

> See [SharedDataSourceParameterResolver.kt](../src/test/kotlin/system/SharedDataSourceParameterResolver.kt) and [ApplicationRepositoryIntegrationTest.kt](../src/test/kotlin/application/ApplicationRepositoryIntegrationTest.kt) for the full implementation.

### E2E: Delegation for Partial Overrides

For end-to-end tests that need to replace specific services while keeping the rest intact, use Kotlin's delegation:

```kotlin
val testContext = SystemTestContext()
val dependencies = object : AppDependencies by testContext {
    override val services = object : AppDependencies.Services by testContext.services {
        override val applicationService = customService
    }
}
```

## Benefits of Manual DI

My reasons for this approach:
- I spend less time on Google figuring out which annotation or XML/JSON/YAML element to specify.
- It shows the different places I am injecting each object/service/repo/client.
- It makes it possible to inject whatever I want, whenever I want. Think Test Doubles like Fakes. Or even once in a while Mocks.
- I TDD a lot more. Because I control how much is being loaded and when, I only load what's necessary and it's fast. I can decide to make the feedback loop efficient even if I am writing automated tests that do actual HTTP and database calls.
- I re-factor more. Sometimes when I re-factor, the specific patterns of the framework get in the way. Without framework annoyances, it is easier and more fun to do refactorings.
- No casting needed in tests (covariant override inference gives you concrete fake types)
- Test contexts are lightweight — fresh context per test prevents state leakage
- No lazy pitfalls — each context initializes independently

# Related Reading
- [Rolling your own dependency injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403)
