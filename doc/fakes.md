Part of [TDD](tdd.md)

When writing tests, it is often important to isolate the tests.
This can be done in many ways,
but most of them are described in [this classic article by Martin Fowler
called Test Doubles](https://martinfowler.com/bliki/TestDouble.html).

> You can find the example code here: [ApplicationFakeTest.kt](../src/test/kotlin/fakes/ApplicationFakeTest.kt)

# I make fakes like...
It is quite simple really.
Almost anything can be faked with a HashMap [[1](../src/test/kotlin/fakes/ApplicationRepositoryFake.kt)] [[2](../src/test/kotlin/fakes/UserNotificationClientFake.kt)].
Sometimes you need other solutions. 

Unlike Mocks, you don't really need a library.
Implement the interface you are faking, and put, update and fetch to/from the HashMap.
:Rocket:

Sometimes the choice of "primary key" (the key in the hashmap) can be a bit awkward,
and implementing SQL like searches in a DbRepoFake can feel weird.
But it is all worth it. 

# Why fakes?

The different test doubles are all useful, but in general I tend to start with Fakes first. For everything. Even the local database. It's not without its downsides, but at least it is fast. I find Fakes to strike the best balance and be less vulnerable to the following things:
- Changes in data structures. Hundreds of compiler errors when a central class changes?
- Changes in behavior. Twenty tests to fix because the data added in your real code doesn't match test setup anymore?
- Temporary issues outside your control. Network, third party downtime, test envs that are slow :boom:
- Speed. It's all in memory baby. :heart: :fire:
- Flakiness. If Fakes are flaky, you have other issues to figure out. :wink:

# Isn't this a lot of work?

Yes, a little bit. But it reduces the long time overhead and maintenance of tests. And it increases the speed you can run tests at right away. Running stuff in memory is infinitely faster, even if the DB is on localhost.

If you do TDD, you only implement the features (in the fakes) as you need them.
It is perfectly fine to use IntelliJs "implement interface"
function that leaves (exception throwing) TODOs for every method.
Then you fix the ones needed to get your test passing.
Rinse and repeat.
:smile:

Once the method is implemented (with a HashMap), it is reusable across all your tests. :trophy:

# What about the rest?

It all has to be tested. :smiley: By using fakes, I find that I do:

- Dedicated repository tests to check input/output. Without Fakes.
- Dedicated incoming tests for checking ok+error cases in APIs and HTTP endpoints, etc. With fakes.
- Domain oriented tests. With fakes.

These aren't always exclusive.
I actually run most edge tests with a "full system", except for Fakes on any external dependencies (including DB).
They run blazing fast.
:smiley:

The clue here is
to being able
to express and test all the unique combinations the system has to cater for in the domain-oriented tests with fakes,
and then have basic sanity checks for the "outer edges".

Also see [TDD](tdd.md) for some relevant considerations and articles about what to test when.

# Related reading
- [Martin Fowler: Test Doubles](https://martinfowler.com/bliki/TestDouble.html)
- [Test Doubles — Fakes, Mocks and Stubs](https://blog.pragmatists.com/test-doubles-fakes-mocks-and-stubs-1a7491dfa3da)
- [Test Doubles from the "Software Engineering at Google" book](https://abseil.io/resources/swe-book/html/ch13.html)
- [Mocks are bad... A quick summary](https://anderssv.medium.com/mocks-are-bad-a-quick-summary-7c70d9d3226c)
