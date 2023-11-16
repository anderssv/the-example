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
    private val appService = ApplicationService(applicationRepo)

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
            //assertThat(it).contains(applications.last())
        }
    }

}