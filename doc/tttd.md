> Testing Through The Domain was kind of a joke because the name was a bit hard to pronounce. But it stuck for now. :smiley:

This one is a bit harder to see, but I do think it raises the level for tests and maintenance in the long run.

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

They both pass, and everything is green. There is just a tiny differnece, the usage of the service to chnage the state of the domain. :white_check_mark:

Now consider the changed requirements: Approve needs to check whether you are still an active customer when approving the application.

The data oriented test will fail on null-pointer in `service.approve(...)`. The domain oriented test will work because the `service.registerInitialApplication` registers the customer, and then moves on.

You can have a look here to see the different tests (including the new one after the new requirement): [TestingThroughTheDomain.kt](../src/test/kotlin/tttd/TestingThroughTheDomainTest.kt)

In addition to this the domain oriented approach will:
- Evolve your domain as you write tests
- Make the domain clearer to understand

When you have changes in data structures or logic how many places do you have to fix issues? TTTD will _reduce_ that. Object Mother helps, but this is the missing companion to that.

This is a small example, but these are the kind of changes that will ripple through your system and tests and make re-factorings slower and more error prone. :smiley:

It will also update all the data-structures that you don't really need (or know that you need) in your tests such that you are testing a much more consistent state in your domain all the time. 

Especially in Kotlin this makes it much easier to have non-nullable fields with real values all over your domain.
