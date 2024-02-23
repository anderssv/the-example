Part of [TDD](tdd.md)

---

Tests should be easy to set up and write.
So you can focus on implementing the actual features.
By using certain techniques, it is possible to make writing and maintenance easier.
Luckily,
the elements that make tests easy to write are also techniques that make tests easier to maintain over time.

Test set up has to be:
- Intuitive. You should not need to think hard about the set-up. The feature should be the hard part.
- Resilient to unrelated changes. When the system changes, tests that do not test relevant features should not have to change. If all tests set up data independently, all tests will have to be updated when the domain changes.

The most important techniques I use are:
- [Object Mother or similar patterns](https://martinfowler.com/bliki/ObjectMother.html) to set up test data in a way that is quick, but also isolates the individual tests against irrelevant changes in the domain and system. This is the main focus of this part.
- [Test Doubles, usually Fakes](fakes.md) to ease setup and also isolate individual tests from irrelevant changes in the system.
- [Testing Through The Domain](tttd.md) to make writing tests more aligned with the domain, more "natural" and easier to maintain.

A more indepth description of each can be in the links.
This article will mainly focus on the context and test data set-up.

# Object mother and more in Kotlin

I think Kotlin has some really nice features that help in this regard.

When it comes to test set-up, it is important that:
- It has a logical location. If not, people will duplicate a set-up in many places.
- It is standardized, but flexible. Easy to get the defaults, modify easily for the common cases, and simple to modify for corner cases.

# Setting up both the data and the system

Getting the right test data set-up is just half the job.
You also need to set the system to use those data.

I usually think the set-up have two parts:
1. System—Things like configuration and dependency injection. It Might include DB connections and pools, or you should probably use [Fakes](fakes.md) as you default.
2. State—Like rows in a database and/or storage, and any external service state in [test doubles](https://martinfowler.com/bliki/TestDouble.html).

Number 1 can be solved through things like Spring, or as I prefer: [Manual Dependency Injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403).

Number two can be solved with variations of Object Mother, [Testing Through The Domain](tttd.md) and [fakes](fakes.md).

# Example

This is only part of the code in the GitHub repository. Follow the links to browse around the full classes and setup.

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
See [TestExtensions.kt](../src/test/kotlin/application/TestExtensions.kt) for the source.

Then, whenever you need an application in your tests, you write:

```kotlin
val application = Application.valid()
```

You usually know which class you need, and through auto-complete you see which methods are available.

There can be variations of this:
- The ```valid()``` method has parameters that make it easy to set up common variations. I usually use this for more complex setup that would be nesting a bit deep in the hierarchy. ```PaymentBasket.valid(numberOfTransactions = 4)``` is one example.
- You could have something like ```invalid()``` if that is a common case you need to test.
- You don't have to complicate these methods, as in Kotlin you can do this: ```Application.valid().copy(birthDate = LocalDate.of(1970, 2, 23)) ```. This helps clarify what the test really is about as you read it in the actual test.

Another technique you can consider looking into is creating a test DSL ([Kotlin is great for DSLs](https://kotlinlang.org/docs/type-safe-builders.html#how-it-works)).
But this can get really complex, really fast.
So I only do it if I have core logic that is complex and needs really through testing.
I often find myself grasping for this tool when I have to deal with time variations.

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

The ```with()``` function is one of the [scope functions in Kotlin](https://kotlinlang.org/docs/scope-functions.html#functions)
that makes this kind of code nice.
But you can survive fine without.
😊

# Related reading
- [Easy and maintainable test data—The Kotlin way](https://anderssv.medium.com/easy-and-maintainable-test-data-the-kotlin-way-9ecbbf53d822)
- [Manual Dependency Injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403)