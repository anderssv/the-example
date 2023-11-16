package fakes

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApplicationFakeTest {

    @Test
    fun shouldRegisterApplicationSuccessfullyAndRegisterOnPerson() {
        val applicationRepo = ApplicationRepositoryFake()
        val appService = ApplicationService(applicationRepo)
        val application = Application.valid()

        appService.registerInitialApplication(application)

        assertThat(appService.applicationsForName(application.name)).contains(application)
    }

    @Test
    fun shouldRegisterApplicationSuccessfullyAndExpireWhenTooOld() {
        val applicationRepo = ApplicationRepositoryFake()
        val appService = ApplicationService(applicationRepo)

        val applications = (0..3).map {
            Application.valid(addToMonth = it.toLong())
        }

        applications.forEach { appService.registerInitialApplication(it) }

        appService.expireApplications()

        appService.ongoingApplicationsForName(applications.first().name).let {
            assertThat(it).doesNotContain(applications.first())
            //assertThat(it).contains(applications.last())
        }
    }

}