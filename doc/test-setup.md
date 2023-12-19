Part of [TDD](tdd.md)

---

Tests should be easy to set up and write.
And implementing the actual features should be the focus.
So by using some techniques, it is possible to ease writing and maintenance.
Luckily,
some of the elements that make tests easy to set up are also techniques that make tests easier to maintain.

Test data set up has to be:
- Fast to use. Writing new tests should be fast and let you focus on making the features you need.
- Resilient to unrelated changes. When the system changes, tests that do not test relevant features should not be affected. If all tests set up data independently, all tests will have to be updated when prerequisites change.

The most important techniques I use are:
- [Object Mother or similar patterns](https://martinfowler.com/bliki/ObjectMother.html) to set up test data in a way that is quick, but also isolates the individual tests against irrelevant changes in the domain and system. This is the main focus of this part.
- [Test Doubles, usually Fakes](fakes.md) to ease setup and also isolate individual tests from irrelevant changes in the system.
- [Testing Through The Domain](tttd.md) to make writing tests more aligned with the domain, more "natural" and easier to maintain.

# Object mother and more in Kotlin

I think Kotlin has some really nice features that help in this regard.
When it comes to set up test data, it is important:
- It has a logical location. If not, people will duplicate a set up in many places.
- Standardized, but flexible. Easy to get the standard, modify easily for the common cases, and simple to isolate for corner cases.



# The main parts of a set-up

There is usually two main things that is set up:
1. System - Think things like configuration and dependency injection. It Might include DB connections and pools, or you should probably use [Fakes](fakes.md) as you default.
2. State - Think rows in a database and/or storage, and any external service state in [test doubles](https://martinfowler.com/bliki/TestDouble.html).

Number 1 can be solved through things like Spring, or as I prefer: [Manual Dependency Injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403).

Number two can be solved with variations of Object Mother and [fakes](fakes.md).

# Related reading
- [Easy and maintainable test data—The Kotlin way](https://anderssv.medium.com/easy-and-maintainable-test-data-the-kotlin-way-9ecbbf53d822)
