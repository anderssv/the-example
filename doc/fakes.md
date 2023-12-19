Part of [TDD](tdd.md)

---

When writing tests, it is often important to isolate the tests.
This can be done in many ways,
but most of them are described in [this classic article by Martin Fowler
called Test Doubles](https://martinfowler.com/bliki/TestDouble.html).

> ✅ You can find the example code in [ApplicationFakeTest.kt](../src/test/kotlin/fakes/ApplicationFakeTest.kt)

# I make fakes like...
It is quite simple really.
Almost anything can be faked with a HashMap [[1](../src/test/kotlin/fakes/ApplicationRepositoryFake.kt)] [[2](../src/test/kotlin/fakes/UserNotificationClientFake.kt)].
Sometimes you need other solutions. 

Unlike Mocks, you don't really need a library.
Implement the interface you are faking, and put, update and fetch to/from the HashMap.
:rocket:

Sometimes the choice of "primary key" (the key in the hashmap) can be a bit awkward,
and implementing SQL like searches in a DbRepoFake can feel weird.
But it is all worth it. 

# Why fakes?

The different test doubles are all useful, but in general I tend to start with Fakes first.
For everything.
Even the local database.
It's not without its downsides, but at least it is fast.
I find Fakes to strike the best balance and be less vulnerable to changes that should not affect them:
- They are instantly re-usable across tests as they mimic the integration/component they represent. ⚙️
- Changes in data structures are easier as there are fewer tests that set up things in different ways. Hundreds of compiler errors when a central class changes are no longer something that happens.
- Changes in behavior fail fewer tests that should not be affected by the change. Twenty tests to fix because the data added in your real code doesn't match test setup anymore? It's gone. 😎
- No temporary issues outside your control. Network, third party downtime, test envs that are slow. 💥
- Blazing Speed. It's all in memory baby. ♥️ 🔥
- No Flakiness. If Fakes are flaky, you have other issues to figure out. 😉

# Verifying interactions and failures

When you write tests,
and you use Fakes, you might find that you are missing the good old verifications of your beloved Mock framework.
And there are cases where you need those,
but the short answer is that you should verify system state as a _result_ of interactions.

Let us say you are registering Applications,
and the registering process requires some updated info from an external system let's call it NationalCompanyRegistry.

You should:
- Add a specific company to the NationalCompanyRegistryFake.
- Verify that after system processing the Application is registered with information _from_ the registry.

If you really need to verify that the Fake received some information or was called, you can create custom methods
(only available in the Fake, not the interface) to verify those.
You can se an example of such a method here:
[https://github.com/anderssv/the-example/blob/main/src/test/kotlin/fakes/UserNotificationClientFake.kt#L13](https://github.com/anderssv/the-example/blob/main/src/test/kotlin/fakes/UserNotificationClientFake.kt#L13)

## Testing errors

Sometimes you will also need to test error situations.
I usually do this by creating a custom method and map in the Fake to register an error.

This way the test sets up the expectations, like ```nationalCompanyRegistryFake.failOnOrgNumber("...")```.

Some (very few times), I need to reliably test something like a 500-error code.
Then even I use mocks.
But I can honestly say that this has been max 5 times the last 5 years.

# Isn't this a lot of work?

Yes, a little bit. But it reduces the long time overhead and maintenance of tests. And it increases the speed you can run tests at right away. Running stuff in memory is infinitely faster, even if the DB is on localhost.

If you do TDD, you only implement the features (in the fakes) as you need them.
It is perfectly fine to use IntelliJs "implement interface"
function that leaves (exception throwing) TODOs for every method.
Then you fix the ones needed to get your test passing.
Rinse and repeat.
😄

Once the method is implemented (with a HashMap), it is reusable across all your tests. 🏆

# What about the rest?

It all has to be tested. 😄 By using fakes, I find that I do:

- Dedicated repository tests to check input/output. Without Fakes.
- Dedicated incoming tests for checking ok+error cases in APIs and HTTP endpoints, etc. With fakes.
- Domain oriented tests. With fakes.

These aren't always exclusive.
I actually run most-edge tests with a "full system", except for Fakes on any external dependencies (including DB).
They run blazing fast.
😄

The clue here is
to being able
to express and test all the unique combinations the system has to cater for in the domain-oriented tests with fakes,
and then have basic sanity checks for the "outer edges."

Also see the [TDD](tdd.md) page for some relevant considerations and articles about what to test when.

# Related reading
- [Martin Fowler: Test Doubles](https://martinfowler.com/bliki/TestDouble.html)
- [Test Doubles — Fakes, Mocks and Stubs](https://blog.pragmatists.com/test-doubles-fakes-mocks-and-stubs-1a7491dfa3da)
- [Test Doubles from the "Software Engineering at Google" book](https://abseil.io/resources/swe-book/html/ch13.html)
- [Mocks are bad... A quick summary](https://anderssv.medium.com/mocks-are-bad-a-quick-summary-7c70d9d3226c)
- [Prefer Fakes over Mocks](https://tyrrrz.me/blog/fakes-over-mocks)
