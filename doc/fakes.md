When writing tests it is often important to isolate the tests. This can be done in many ways, but most of them are described in [this classic article by Martin Fowler called Test Doubles](https://martinfowler.com/bliki/TestDouble.html).

You can find some example code here: [ApplicationFakeTest.kt](../src/test/kotlin/fakes/ApplicationFakeTest.kt)

# I make fakes like...

It is quite simple really. Almost anything can be faked with a HashMap. Sometimes you need other solutions. 

Unlike Mocks you don't really need a library. Just implement the interface you are faking, and put, update and fetch to/from the HashMap. 

Sometimes the choice of "primary key" (the key in the hashmap) can be a bit awkward, and implementing SQL like searches in a DbRepoFake can be weird. But it is all worth it. 

# Why fakes?

The different test doubles are all useful, but in general I tend to start with Fakes first. For everything. Even the local database. It's not without its downsides, but at leas it is fast. I find Fakes to strike the best balance and be less vulnerable to the following things:
- Changes in data structures. 100 compile errors when a central class changes?
- Changes in behaviour. 20 tests to fix because the data addded in your real code doesn't match test setup anymore?
- Temporary issues outside your control. Network, 3rd party downtime, test envs that are slow
- Slow Speed. It's all in memory baby.
- Flakyness. If fakes become flaky you have other issues. ;)

# Isn't this a lot of work?

A little bit, yes. But it reduces the long time overhead and maintenance of tests. And it increases the speed you can run tests at right away. Running stuff in memory is infinitely faster, even if the DB is on localhost.

And if you do TDD, you only implement the features (in the fakes) as you need them. It is perfectly fin to use IDEAS "implement interface" function that leaves (exception throwing) TODOs for every method. Then you just fix the ones needed to get your test passing. Rinse and repeat. :)

# Related reading
- [Mocks are bad... A quick summary](https://anderssv.medium.com/mocks-are-bad-a-quick-summary-7c70d9d3226c)
- [Martin Fowler: Test Doubles](https://martinfowler.com/bliki/TestDouble.html)
- [Easy and maintainable test data - The Kotlin way](https://anderssv.medium.com/easy-and-maintainable-test-data-the-kotlin-way-9ecbbf53d822)

