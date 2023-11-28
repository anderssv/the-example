This one is a bit hard to see, but I do think it raises the level for tests and maintenance in the long run.

Consider two tests and their set-up with verifications:

Data oriented setup:
- Set up Application with `Application.valid().copy(status=Approved)`
- Test that the application can be re-opened via `service.reopen(application)`
- Assert that the `application.status==Reopened`

Domain oriented set up:
- Set up initial Application via `application.valid()`
- Register application through `service.registerNewApplication(application)`
- Approve application through `service.approveApplicationWithOutConditions(application)`
- Test that the application can be re-opened via `service.reopen(application)`
- Assert that the `application.isReopened()`

They both pass, and everything is green.

Now consider the changed requirements:
- Reopen checks if you are still a resident in Norway.
- Reopen needs to check if the user has more than one application in external register.

Data oriented will fail on null-pointer in service.reopen(application). Domain oriented will work because the registerNewApplication registers the application first, and then moves on.

In addition the domain oriented approach will:
- Evolve your domain as you write tests
- Make the domain clearer to understand

When you have changes in data structures or logic how many places do you have to fix issues? TTTD will _reduce_ that. Object Mother helps, but this is the missing companion to that.

It will also update all the data-structures that you don't really need (or know that you need) in your tests such that you are testing a much more consistent state in your domain all the time. Especially in Kotlin this makes it much easier to have non-nullable fields with real values all over your domain.


