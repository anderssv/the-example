import application.Application
import application.ApplicationService
import application.ApplicationStatus
import fakes.ApplicationRepositoryFake
import fakes.UserNotificationFake
import fakes.valid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class TestingThroughTheDomainTest {
    private val applicationRepo = ApplicationRepositoryFake()
    private val notificationRepo = UserNotificationFake()
    private val appService = ApplicationService(applicationRepo, notificationRepo)

    @Test
    fun testDataOrientedTest() {
        val application = Application.valid()

        applicationRepo.addApplication(application)
        appService.approveApplication(application.id)

        assertThat(applicationRepo.getApplication(application.id).status).isEqualTo(ApplicationStatus.APPROVED)
    }

    @Test
    fun testDomainOrientedTest() {
        val application = Application.valid()

        appService.registerInitialApplication(application)
        appService.approveApplication(application.id)

        assertThat(applicationRepo.getApplication(application.id).status).isEqualTo(ApplicationStatus.APPROVED)
    }

}