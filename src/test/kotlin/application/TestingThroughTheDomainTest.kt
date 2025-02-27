package application

import customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import system.SystemTestContext

/**
 * This test shows concepts for Testing Through The Domain,
 * have a look at the theoretical description in doc/tttd.md in this repo.
 */
class TestingThroughTheDomainTest {
    private val testContext = SystemTestContext()

    /**
     * This test sets up the _data_ needed to run the test and then verifies the result.
     *
     * It is currently failing because new requirements for storage of customers were introduced after writing this test.
     *
     * These kinds of tests are more brittle when the system changes.
     */
    @Test
    @Disabled("This test is disabled because relies on the internals of the system through data. It is an example of what breaks when you don't use TTTD.")
    fun testDataOrientedTest() {
        with(testContext) {
            val customer = Customer.valid()
            repositories.customerRepository.addCustomer(customer)

            val application = Application.valid(customer = customer)

            // Data is set up, store directly in DB. Ignoring anything else the
            // system does to reach the state.
            repositories.applicationRepo.addApplication(application)
            applicationService.approveApplication(application.id)

            assertThat(repositories.applicationRepo.getApplication(application.id).status).isEqualTo(ApplicationStatus.APPROVED)
        }
    }

    /**
     * This test sets up the _system_ in the correct state.
     *
     * It is less brittle when the system changes and did not break when the storage
     * of customers was introduced.
     */
    @Test
    fun testDomainOrientedTest() {
        with(testContext) {
            val customer = Customer.valid()
            repositories.customerRepository.addCustomer(customer)

            val application = Application.valid(customer = customer)

            // Start the process of registering application, thus manipulating through the system and
            // getting everything in a consistent state
            applicationService.registerInitialApplication(application)
            applicationService.approveApplication(application.id)

            assertThat(repositories.applicationRepo.getApplication(application.id).status).isEqualTo(ApplicationStatus.APPROVED)
        }
    }

    /**
     * This is actually a bad test but was used to introduce customer storage.
     * I kept the two previous ones unchanged to prove the point that the data-oriented one would break.
     */
    @Test
    fun testAddActiveCustomerWhenNewApplication() {
        with(testContext) {
            val application = Application.valid()
            val customer = Customer(
                id = application.customerId,
                name = application.name,
                active = true
            )
            repositories.customerRepository.addCustomer(customer)

            applicationService.registerInitialApplication(application)
            applicationService.approveApplication(application.id)

            val storedApplication = repositories.applicationRepo.getApplication(application.id)
            assertThat(storedApplication.status).isEqualTo(ApplicationStatus.APPROVED)

            val storedCustomer = repositories.customerRepository.getCustomer(storedApplication.customerId)
            assertThat(storedCustomer.name).isEqualTo(application.name)
            assertThat(storedCustomer.active).isTrue()
        }
    }
}
