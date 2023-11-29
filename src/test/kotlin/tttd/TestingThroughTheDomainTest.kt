import application.Application
import application.ApplicationService
import application.ApplicationStatus
import customer.CustomerRepositoryFake
import fakes.ApplicationRepositoryFake
import fakes.UserNotificationFake
import fakes.valid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class TestingThroughTheDomainTest {
    private val applicationRepo = ApplicationRepositoryFake()
    private val notificationRepo = UserNotificationFake()
    private val customerRepo = CustomerRepositoryFake()
    private val appService = ApplicationService(applicationRepo, notificationRepo, customerRepo)

    /**
     * This test sets up the _data_ needed to run the test, and then verifies the result.
     *
     * This kind of test is more brittle when the system changes, and is currently failing
     * because storage of customers was introduced.
     */
    @Test
    fun testDataOrientedTest() {
        val application = Application.valid()

        // Data is set up, store directly in DB. Ignoring anything else the
        // system does to reach the state.
        applicationRepo.addApplication(application)
        appService.approveApplication(application.id)

        assertThat(applicationRepo.getApplication(application.id).status).isEqualTo(ApplicationStatus.APPROVED)
    }

    /**
     * This test sets up the _system_ in the correct state.
     *
     * It is less brittle when the system changes, and did not break when the storage
     * of customers was introduced.
     */
    @Test
    fun testDomainOrientedTest() {
        val application = Application.valid()

        // Start the process of registering application, thus manipulating through the system and
        // getting everything in a consistent state
        appService.registerInitialApplication(application)
        appService.approveApplication(application.id)

        assertThat(applicationRepo.getApplication(application.id).status).isEqualTo(ApplicationStatus.APPROVED)
    }

    /**
     * This is actually a bad test, but was used to introduce
     * customer storage, as well as making sure the two previous
     * ones did not have to change to show how they would break.
     */
    @Test
    fun testAddActiveCustomerWhenNewApplication() {
        val application = Application.valid()

        appService.registerInitialApplication(application)
        appService.approveApplication(application.id)

        assertThat(applicationRepo.getApplication(application.id).status).isEqualTo(ApplicationStatus.APPROVED)
        assertThat(customerRepo.getCustomer(application.name).name).isEqualTo(application.name)
    }
}