Part of [TDD](tdd.md)

> Testing Through The Domain was kind of a joke because the name was a bit hard to pronounce. But it stuck for now. :smile:

This one is a bit hard to see, but I know it raises the level for tests and maintenance in the long run.
It reduces irrelevant failures in tests and makes re-factoring easier.
To try and explain the concept, I categorize tests into two categories:

- Data oriented tests—Sets up the expected data and verifies the state in storage is correct afterward.
- Domain oriented tests—Sets up input data, modifies through steps in the system and verifies that the system responds correctly at the end.

You can see [TDD](tdd.md) for a description om some other test categories and levels.

## An example
Consider two tests and their set-up with verifications:

Data oriented setup:
```kotlin
@Test
fun testDataOrientedTest() {
    val application = Application.valid()

    applicationRepo.addApplication(application)
    appService.approveApplication(application.id)

    assertThat(applicationRepo.getApplication(application.id).status).isEqualTo(ApplicationStatus.APPROVED)
}
```

Domain oriented setup:
```kotlin
@Test
fun testDomainOrientedTest() {
    val application = Application.valid()

    appService.registerInitialApplication(application)
    appService.approveApplication(application.id)

    assertThat(applicationRepo.getApplication(application.id).status).isEqualTo(ApplicationStatus.APPROVED)
}
```

They both pass, and everything is green. There is just a tiny difference, the usage of the service to change the state of the domain. :white_check_mark:

### New requirements

Now consider the changed requirements: __Approval needs to check whether you are still an active customer when approving the application.__ 

> The data-oriented test will fail on null-pointer in `service.approve(...)`. The domain-oriented test will work because the `service.registerInitialApplication` registers the customer, and then moves on.

You can have a look here to see the different tests (including the new one after the new requirement): [TestingThroughTheDomainTest.kt](../src/test/kotlin/tttd/TestingThroughTheDomainTest.kt)

### The benefits

In addition to avoiding failures like this, the domain-oriented approach will:
- Evolve your domain as you write tests
- Make the domain clearer to understand

When you have changes in data structures or logic, how many places do you have to fix issues? TTTD will _reduce_ that. [Object Mother](https://martinfowler.com/bliki/ObjectMother.html) helps, but this is the missing companion to that.

This is a small example, but these are the kind of changes that will usually ripple through your system and tests and make re-factoring slower and more error-prone :smiley: By employing TTTD, it will update the data-structures that you don't really need (or know that you need) in your tests such that you are testing a consistent state in your domain all the time. 

Especially in Kotlin, this makes it much easier to have non-nullable fields with real values all over your domain. :rocket:
