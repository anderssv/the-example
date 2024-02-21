Part of [TDD](tdd.md)

---

Tests should be easy to set up and write.
And implementing the actual features should be the focus.
By using some techniques, it is possible to ease writing and maintenance.
Luckily,
the elements that make tests easy to set up are also techniques that make tests easier to maintain.

Test data set up has to be:
- Fast to use. Writing new tests should be fast and let you focus on the features you need.
- Resilient to unrelated changes. When the system changes, tests that do not test relevant features should not be affected. If all tests set up data independently, all tests will have to be updated when the domain changes.

The most important techniques I use are:
- [Object Mother or similar patterns](https://martinfowler.com/bliki/ObjectMother.html) to set up test data in a way that is quick, but also isolates the individual tests against irrelevant changes in the domain and system. This is the main focus of this part.
- [Test Doubles, usually Fakes](fakes.md) to ease setup and also isolate individual tests from irrelevant changes in the system.
- [Testing Through The Domain](tttd.md) to make writing tests more aligned with the domain, more "natural" and easier to maintain.

# Object mother and more in Kotlin

I think Kotlin has some really nice features that help in this regard.

When it comes to set up test data, it is important that:
- It has a logical location. If not, people will duplicate a set up in many places.
- It is standardized, but flexible. Easy to get the defaults, modify easily for the common cases, and simple to modify for corner cases.

# The main parts of a set-up

There is usually two main things that is set up:
1. System - Think things like configuration and dependency injection. It Might include DB connections and pools, or you should probably use [Fakes](fakes.md) as you default.
2. State - Think rows in a database and/or storage, and any external service state in [test doubles](https://martinfowler.com/bliki/TestDouble.html).

Number 1 can be solved through things like Spring, or as I prefer: [Manual Dependency Injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403).

Number two can be solved with variations of Object Mother and [fakes](fakes.md).

# Examples

## Setting up test data

To make test data easy to find, I like to use extension methods in Kotlin.

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

Then, whenever you need an application in your tests, you write:

```kotlin
val application = Application.valid()
```

You usually know which class you need, and you can see from the auto-complete which methods are available.

There can be variations of this:
- The ```valid()``` method has parameters that make it easy to set up common variations. This is usually used for more complex setup that would be nesting a bit deep in the hierarchy. ```PaymentBasket.valid(numberOfTransactions = 4)``` is one example.
- You could have something like ```invalid()``` if that is a common case you need to test.
- You don't have to complicate these methods, as in Kotlin you can do this: ```Application.valid().copy(birthDate = LocalDate.of(1970, 2, 23)) ```. This helps clarify what the test really is about as you read it in the actual test.

Another technique you can consider looking into is creating a test DSL (Kotlin is great for DSLs).
But this can get really complex, really fast.
So I only do it if I have core logic that is complex.
I often find myself grasping for this tool when I have to deal with time.

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

The important part here is to notice that Clients and Repositories are overridden from the superclass,
and set up with Fakes instead of real implementations.
You can see the [super-class (production DI context) here](../src/main/kotlin/system/SystemContext.kt).

This way of creating a context with references enables us to use it in a test like this:

```kotlin
with(testContext) {
    ...
    repositories.applicationRepo.addApplication(application)
    applicationService.approveApplication(application.id)
    ...
}
```

Where both ```repositories.applicationRepo``` and ```applicationService``` are objects from the ApplicationTestContext.
The ApplicationTestContext is then used in many of the tests, making everything available with very little setup.

# Related reading
- [Easy and maintainable test data—The Kotlin way](https://anderssv.medium.com/easy-and-maintainable-test-data-the-kotlin-way-9ecbbf53d822)
