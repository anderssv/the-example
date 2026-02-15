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

I usually do manual dependency injection. 🚀

> ✅ You can see a dependency injection example in [SystemContext.kt](../src/main/kotlin/system/SystemContext.kt)
and how to set up a separate context for testing in [SystemTestContext.kt](../src/test/kotlin/system/SystemTestContext.kt).

## The Pattern: Interfaces with Anonymous Objects

The core pattern uses **interfaces** for dependency grouping, implemented as **anonymous objects** inside the context:

```kotlin
interface Repositories {
    val applicationRepo: ApplicationRepository
}

open val repositories: Repositories by lazy {
    object : Repositories {
        override val applicationRepo by lazy { ApplicationRepositoryImpl() }
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

This became a real problem in production code when repositories started needing database connections, configuration objects, or HTTP clients in their constructors. Test code had to create dummy instances of these just to satisfy the superclass constructor, even though fakes are in-memory and never touch databases or HTTP.

**The interfaces solution:**
- No constructor parameters
- No dummy objects needed in tests
- Test implementations must explicitly provide every dependency (no hidden inherited behavior)
- Production wiring stays in one place (anonymous objects inside SystemContext)

### Typed Test Implementations Pattern

The test context provides **two access paths** using Kotlin's covariant return types:

```kotlin
class SystemTestContext : SystemContext() {
    class TestRepositories : Repositories {
        override val applicationRepo = ApplicationRepositoryFake()  // Concrete type!
    }

    val testRepositories = TestRepositories()
    override val repositories: Repositories get() = testRepositories
}
```

In tests:
- `repositories.applicationRepo` - typed as `ApplicationRepository` (interface, used by production code)
- `testRepositories.applicationRepo` - typed as `ApplicationRepositoryFake` (concrete, for test assertions)

No casting needed:
```kotlin
with(SystemTestContext()) {
    applicationService.registerApplication(app)

    // Access fake-specific methods directly - no casting!
    assertThat(testRepositories.applicationRepo.getAllApplications())
        .contains(app)
}
```

## Benefits of Manual DI

My reasons for this approach:
- I spend less time on Google figuring out which annotation or XML/JSON/YAML element to specify.
- It shows the different places I am injecting each object/service/repo/client.
- It makes it possible to inject whatever I want, whenever I want. Think Test Doubles like Fakes. Or even once in a while Mocks.
- I TDD a lot more. Because I control how much is being loaded and when, I only load what's necessary and it's fast. I can decide to make the feedback loop efficient even if I am writing automated tests that do actual HTTP and database calls.
- I re-factor more. Sometimes when I re-factor, the specific patterns of the framework get in the way. Without framework annoyances, it is easier and more fun to do refactorings.
- No casting needed in tests (typed test implementations give you concrete fake types)
- Test contexts are lightweight - fresh context per test prevents state leakage

# Related Reading
- [Rolling your own dependency injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403)
