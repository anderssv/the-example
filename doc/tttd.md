Part of [TDD](tdd.md)

> Testing Through The Domain was kind of a joke because the name was a bit hard to pronounce. But it stuck for now. :smiley:

This one is a bit hard to see, but I know it raises the level for tests and maintenance in the long run.

I tend to write two or three types of tests:
- **IO tests** - No Fakes - Like testing HTTP calls to a third party provider. This makes sure the Client class performs as expected. Or Database repos.
- **Variation tests** - Fakes - Focus in on a part of the system. Test the available variations. Example: In generating mail content it should have this section included if the recipient has X attribute.
- **Outcome tests** - Fakes - This focuses on outcomes in interactions between components. Example: Was an email sent out with a rejected subject when the application was rejected?

Try to avoid verifying data in the database,
test whether the email was sent through the `EmailClient` instead of checking if the `sent=true` in the database.
:smiley:

Let me illustrate; consider two tests and their set-up with verifications:

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

Domain oriented set up:
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

Now consider the changed requirements: Approve needs to check whether you are still an active customer when approving the application.

The data-oriented test will fail on null-pointer in `service.approve(...)`. The domain-oriented test will work because the `service.registerInitialApplication` registers the customer, and then moves on.

You can have a look here to see the different tests (including the new one after the new requirement): [TestingThroughTheDomain.kt](../src/test/kotlin/tttd/TestingThroughTheDomainTest.kt)

In addition to this, the domain-oriented approach will:
- Evolve your domain as you write tests
- Make the domain clearer to understand

When you have changes in data structures or logic, how many places do you have to fix issues? TTTD will _reduce_ that. Object Mother helps, but this is the missing companion to that.

This is a small example, but these are the kind of changes that will ripple through your system and tests and make re-factoring slower and more error-prone. :smiley:

It will also update all the data-structures that you don't really need (or know that you need) in your tests such that you are testing a consistent state in your domain all the time. 

Especially in Kotlin, this makes it much easier to have non-nullable fields with real values all over your domain.
