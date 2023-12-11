Part of [System Design](system-design.md) and [TDD](tdd.md).

---

Dependency Injection plays a key role in systems development, especially with automated testing and TDD.
It is all about molding an effective dependency graph for your application.
Frameworks often seem appealing, but their automated nature can obscure vital feedback from your code.
This feedback is of immense value in enhancing your system structure.
Remember, a framework isn't always necessary for these operations.

> :white_check_mark: You can see dependency injection in [ApplicationContext.kt](../src/main/kotlin/application/ApplicationContext.kt)
and how to set up an ApplicationTestContext in [TestingThroughTheDomain.kt](../src/test/kotlin/tttd/TestingThroughTheDomainTest.kt).


So I usually do manual dependency injection. Because:
- I spend less time on Google figuring out which annotation or XML/JSON/YAML element to specify.
- It shows how many places I am injecting different objects.
- It makes it possible to inject whatever I want, whenever I want.
- I TDD a lot more. Because I control how much is being loaded and when, I only load what’s necessary and it’s fast. I can decide to make the feedback loop efficient even if I am writing automated tests that do actual HTTP and database calls.
- I re-factor more. Sometimes when I re-factor, the specific patterns of the framework get in the way. Without framework annoyances, it is easier and more fun to do refactorings.

# Related Reading
- [Rolling your own dependency injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403)