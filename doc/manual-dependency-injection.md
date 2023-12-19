Part of [System Design](system-design.md) and [TDD](tdd.md).

---

Dependency Injection plays a key role in systems development, especially with automated testing and TDD.
It is all about molding an effective dependency graph for your application and tests.
Frameworks often seem appealing, but their automated nature can obscure vital feedback from your code.
This feedback is of immense value in enhancing your system structure.

If you are not writing tests, or mostly writing integration tests, you might not feel that DI is important.
But once you write fine-grained tests and isolate out infrastructure and dependencies, it becomes essential.

I usually do manual dependency injection. 🚀

> ✅ You can see a dependency injection example in [SystemContext.kt](../src/main/kotlin/system/SystemContext.kt)
and how to set up a separate context for testing in [SystemTestContext.kt](../src/test/kotlin/system/SystemTestContext.kt).


My reasons for this are:
- I spend less time on Google figuring out which annotation or XML/JSON/YAML element to specify.
- It shows the different places I am injecting each object/service/repo/client.
- It makes it possible to inject whatever I want, whenever I want. Think Test Doubles like Fakes. Or even once in a while Mocks.
- I TDD a lot more. Because I control how much is being loaded and when, I only load what’s necessary and it’s fast. I can decide to make the feedback loop efficient even if I am writing automated tests that do actual HTTP and database calls.
- I re-factor more. Sometimes when I re-factor, the specific patterns of the framework get in the way. Without framework annoyances, it is easier and more fun to do refactorings.

# Related Reading
- [Rolling your own dependency injection](https://anderssv.medium.com/rolling-your-own-dependency-injection-7045f8b64403)
