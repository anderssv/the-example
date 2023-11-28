This one is a bit hard to see, but I do think it raises the level for tests and maintenance in the long run.

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

They both pass, and everything is green. :white_check_mark:

Now consider the changed requirements: Approve check whether you are still an active customer when approving the application.

Data oriented will fail on null-pointer in `service.approve(...)`. Domain oriented will work because the `service.registerInitialApplication` registers the customer, and then moves on.

In addition to this the domain oriented approach will:
- Evolve your domain as you write tests
- Make the domain clearer to understand

When you have changes in data structures or logic how many places do you have to fix issues? TTTD will _reduce_ that. Object Mother helps, but this is the missing companion to that.

It will also update all the data-structures that you don't really need (or know that you need) in your tests such that you are testing a much more consistent state in your domain all the time. 

Especially in Kotlin this makes it much easier to have non-nullable fields with real values all over your domain.