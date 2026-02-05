Part of [TDD](tdd.md)

---

> I am an independent consultant and would love to help your team get better at continuous delivery.
> Reach out
> at [anders.sveen@mikill.no](mailto:anders.sveen@mikill.no) or go
> to [https://www.mikill.no](https://www.mikill.no/contact.html) to contact, follow on social media or to see more of
> my work.

Tests should be easy to set up and write.
So you can focus on implementing the actual features.
By using certain techniques, it is possible to make writing and maintenance easier.
Luckily,
the elements that make tests easy to write are also techniques that make tests easier to maintain over time.

# Quick, and maintainable

When I write tests, I try to make the set-up:

- Intuitive. You should not need to think very hard about the set-up. The feature should be what you focus on.
- Resilient to unrelated changes.
  When the system changes, tests that do not test relevant features should not have to
  change.
  If all tests set up its data independently, all tests will have to be updated when the domain changes.
- In a logical location. If not, people will duplicate a set-up in many places.
- Standardized, but flexible.
  Quick to get the defaults, modify easily for the common cases, and simple to modify for
  corner cases.

In Kotlin, I think [extension functions](https://kotlinlang.org/docs/extensions.html)
and [data classes (with copy methods)](https://kotlinlang.org/docs/data-classes.html) are perfect
for achieving some of this.

The example in this repo is a distillation of the techniques I use the most.
Go to [TDD](tdd.md)
to see the full context and description of patterns like [Fakes for Test Doubles](fakes.md)
and [Testing Through The Domain](tttd.md).

The below examples will mainly focus on the context and test data set-up.

# Setting up both the data and the system

Getting the right test data set-up is just half the job.
You also need to set the system to use those data.

The set-up has two parts:

1. System—Things like configuration and dependency injection.
   It Might include DB connections and pools, or you should
   probably use [Fakes](fakes.md) as you default (yes, even for most DB Repositories).
2. State—Like rows in a database and/or storage, and any external service state
   in [test doubles](https://martinfowler.com/bliki/TestDouble.html).

Number 1 can be solved through things like Spring, or as I
prefer: [Manual Dependency Injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403).

Number two can be solved with variations of Object Mother, [Testing Through The Domain](tttd.md) and [fakes](fakes.md).

## Setting up test data

[Object Mother or similar patterns](https://martinfowler.com/bliki/ObjectMother.html) are ways to re-use and maintain
test data set-up.
Like I mentioned,
Kotlin has some really nice features that help in this regard with extension functions and data classes.

This is an extension method (only in test scope) that is available for an Application:

```kotlin
fun Application.Companion.valid(addToMonth: Long = 0) = Application(
    id = UUID.randomUUID(),
    name = "Tester One",
    birthDate = LocalDate.of(1978, 2, 23),
    applicationDate = LocalDate.of(2022, 2, 15).plusMonths(addToMonth),
    status = ApplicationStatus.ACTIVE
)
```

See [TestExtensions.kt](../src/test/kotlin/application/TestExtensions.kt) for the source.

You can see it has "sensible" defaults that can be used in most places,
and a parameter for a simpler manipulation of the application date.

Then, whenever you need an application set up with data in your tests, you write:

```kotlin
val application = Application.valid()
```

Putting it as an extension method on the class makes it really easy to find.
You usually know which class you need, and through auto-complete you see which methods are available.

There can be variations of this:

- The ```valid()``` method has parameters that make it easy to set up common variations.
  I usually use this for more
  complex setup that would be nesting a bit deep in the hierarchy. ```PaymentBasket.valid(numberOfTransactions = 4)```
  is one example.
  This of course also uses ```Transaction.valid()``` inside it.
- You could have something like ```invalid()``` if that is a common case you need to test.
- You don't have to complicate these methods for every variation your need.
  In Kotlin, you can do
  this: ```Application.valid().copy(birthDate = LocalDate.of(1970, 2, 23)) ```.
  This helps clarify what the test really
  is about, as you read it in the actual test.

## Setting up the system

Setting up a dependency-injected test context should be as easy as:

```kotlin
private val testContext = SystemTestContext()
```

There are always some DB dependencies etc. to set up too, but that will have to wait for a later post.

The SystemTestContext then looks like this:

```kotlin
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
}
```

The important part here is to notice that Clients and Repositories are overridden from the superclass.
It is set up with Fakes instead of real implementations.
You can see the [superclass (production DI context) here](../src/main/kotlin/system/SystemContext.kt).

This way of creating a context with references enables us to use it in a test like this:

```kotlin
with(testContext) {
    // ...
    repositories.applicationRepo.addApplication(application)
    applicationService.approveApplication(application.id)
    // ...
}
```

Both ```repositories.applicationRepo``` and ```applicationService``` are objects from the ApplicationTestContext.
The ApplicationTestContext is then used in many of the tests, making everything available with very little setup.

The ```with()``` function is one of
the [scope functions in Kotlin](https://kotlinlang.org/docs/scope-functions.html#functions)
that makes this kind of code nice.
But you can survive fine without.
😊

# Parallel-safe assertions

Tests in this project run in parallel by default. This means you cannot rely on absolute counts or assume your test is the only one creating data.

**Never check absolute counts:**
```kotlin
// BAD - fails when other tests create data concurrently
assertThat(repository.getAllApplications()).hasSize(1)
assertThat(repository.count()).isEqualTo(5)
```

**Always query by the specific ID you created:**
```kotlin
// GOOD - verify specific data using its unique ID
val application = Application.valid()  // Creates with UUID.randomUUID()
applicationService.register(application)

val stored = repository.getApplication(application.id)
assertThat(stored).isNotNull
assertThat(stored.name).isEqualTo("Tester One")
```

**When checking lists, filter to your data:**
```kotlin
// GOOD - filter to your specific test data
assertThat(repository.getAllApplications())
    .anyMatch { it.id == application.id }

// GOOD - use contains for specific items
assertThat(applicationService.activeApplications())
    .contains(application)
```

Key principles:
- Each test creates its own data with unique IDs (UUIDs)
- Query by the specific ID you created, not by position or count
- Use `anyMatch`/`contains` instead of `hasSize` when checking lists
- The `SystemTestContext` creates fresh fakes per test instance, ensuring isolation

# Other techniques to consider

Another technique you can consider looking into is creating a test
DSL ([Kotlin is great for DSLs](https://kotlinlang.org/docs/type-safe-builders.html#how-it-works)).
But this can get really complex, really fast.
So I only do it if I have core logic that is complex and needs really through testing.
I often find myself grasping for this tool when I have to deal with time variations.

# Related reading

- [Easy and maintainable test data—The Kotlin way](https://anderssv.medium.com/easy-and-maintainable-test-data-the-kotlin-way-9ecbbf53d822)
- [Manual Dependency Injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403)