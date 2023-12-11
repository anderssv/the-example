Part of [TDD](tdd.md)

---

Tests should be easy to set up and write.
Luckily,
some of the elements that make tests easy to set up is also techniques that make tests easier to maintain as well.

When I write tests, there are usually a few techniques that I use.
Some of them require a bit of investment at first,
but when you get to something like test five and beyond, it really starts to pay off.

The main techniques are:
- [Object Mother or similar patterns](https://martinfowler.com/bliki/ObjectMother.html) to set up test data in a way that is quick, but also isolates the individual tests against irrelevant changes in the domain and system. This is the main focus of this part.
- [Test Doubles, usually Fakes](fakes.md) to ease setup and also isolate individual tests from irrelevant changes in the system.
- [Testing Through The Domain](tttd.md) to make writing tests more aligned with the domain, more "natural" and easier to maintain.

# The main parts of a set-up

There is usually two main things that is set up:
1. System - Think things like configuration and dependency injection. It Might include DB connections and pools, or you should probably use [Fakes](fakes.md) as you default.
2. State - Think rows in a database and/or storage, and any external service state in [test doubles](https://martinfowler.com/bliki/TestDouble.html).

Number 1 can be solved through things like Spring, or as I prefer: [Manual Dependency Injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403).

Number two can be solved with variations of Object Mother and [fakes](fakes.md).

# Related reading
- [Easy and maintainable test data—The Kotlin way](https://anderssv.medium.com/easy-and-maintainable-test-data-the-kotlin-way-9ecbbf53d822)
