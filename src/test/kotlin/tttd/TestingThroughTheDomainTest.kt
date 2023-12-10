package tttd

import application.Application
import application.ApplicationService
import application.ApplicationStatus
import customer.CustomerRepositoryFake
import fakes.ApplicationRepositoryFake
import fakes.UserNotificationClientFake
import fakes.valid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * This test shows concepts for Testing Through The Domain,
 * have a look at the theoretical description @see <a href="../../../../doc/tttd.md">of this here</a>
 * [Here?](../../../../doc/tttd.md)
 */
class TestingThroughTheDomainTest {
    private val applicationRepo = ApplicationRepositoryFake()
    private val notificationRepo = UserNotificationClientFake()
    private val customerRepo = CustomerRepositoryFake()
    private val appService = ApplicationService(applicationRepo, notificationRepo, customerRepo)

    /**
     * This test sets up the _data_ needed to run the test, and then verifies the result.
     *
     * It is currently failing because new requirements for storage of customers were introduced after writing this test.
     *
     * These kinds of tests are more brittle when the system changes.
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
     * This is actually a bad test, but was used to introduce customer storage.
     * And keep the two previous ones unchanged to prove the point that the data-oriented one would change.
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
