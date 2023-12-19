package fakes

import application.Application
import system.SystemTestContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApplicationFakeTest {
    private val testContext = SystemTestContext()

    @Test
    fun shouldRegisterApplicationSuccessfullyAndRegisterOnPerson() {
        with(testContext) {
            val application = Application.valid()

            applicationService.registerInitialApplication(application)

            assertThat(applicationService.applicationsForName(application.name)).contains(application)
        }
    }

    @Test
    fun shouldRegisterApplicationSuccessfullyAndExpireWhenTooOld() {
        with(testContext) {
            val applications = (0..2).map { counter ->
                Application.valid(addToMonth = counter.toLong()).also {
                    applicationService.registerInitialApplication(it)
                }
            }

            applicationService.expireApplications()

            // Assertions
            applicationService.openApplicationsFor(applications.first().name).let {
                assertThat(it).doesNotContain(applications.first())
                // Spot the bug ;)
                //assertThat(it).contains(applications.last())
            }
        }
    }

    @Test
    fun shouldSendNotificationWhenApplicationIsExpired() {
        with(testContext) {
            val application = Application.valid(addToMonth = -6)
            applicationService.registerInitialApplication(application)

            applicationService.expireApplications()

            assertThat(applicationService.openApplicationsFor(application.name)).isEmpty()
            clients.userNotificationClient.getNotificationForUser(application.name).also {
                // Notice how this is a specific method in the Fake. In the case of something
                // like e-mail, there is no way of fetching the actual messages after the fact.
                // So this method is used to verify the outcome, which should be that notifications
                // are sent to the user.
                //
                // Try to focus on verifying the system results, but sometimes you need to validate
                // the interactions as well.
                assertThat(it).isNotEmpty
                assertThat(it).contains("Your application ${application.id} has expired")
            }
        }
    }
}