package fakes

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApplicationFakeTest {
    /*
     * This is basically dependency injection and should be centralized in some way
     *
     * We do this by setting up a common test context that also eases access to fakes
     * etc. Will have to try and show at a later time.
     */
    private val applicationRepo = ApplicationRepositoryFake()
    private val notificationRepo = UserNotificationFake()
    private val appService = ApplicationService(applicationRepo, notificationRepo)

    @Test
    fun shouldRegisterApplicationSuccessfullyAndRegisterOnPerson() {
        val application = Application.valid()

        appService.registerInitialApplication(application)

        assertThat(appService.applicationsForName(application.name)).contains(application)
    }

    @Test
    fun shouldRegisterApplicationSuccessfullyAndExpireWhenTooOld() {
        val applications = (0..2).map { counter ->
            Application.valid(addToMonth = counter.toLong()).also {
                appService.registerInitialApplication(it)
            }
        }

        appService.expireApplications()

        // Assertions
        appService.openApplicationsFor(applications.first().name).let {
            assertThat(it).doesNotContain(applications.first())
            // Spot the bug ;)
            //assertThat(it).contains(applications.last())
        }
    }

    @Test
    fun shouldSendNotificationWhenApplicationIsExpired() {
        val application = Application.valid(addToMonth = -6)
        appService.registerInitialApplication(application)

        appService.expireApplications()

        assertThat(appService.openApplicationsFor(application.name)).isEmpty()
        notificationRepo.getNotificationForUser(application.name).also {
            // Notice how this is a specific method in the Fake. In the case of something
            // like e-mail, there is no way of fetching the actual messages after the fact.
            // So this method is used to verify the outcome, which should be that notifications
            // are sent to the user.
            assertThat(it).isNotEmpty
            assertThat(it).contains("Your application ${application.id} has expired")
        }
    }
}