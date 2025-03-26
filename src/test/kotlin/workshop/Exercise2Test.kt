package workshop


import application.Application
import application.valid
import customer.Customer
import org.junit.jupiter.api.Test
import system.SystemTestContext
import java.time.LocalDate

/**
 * Exercise 2 - Fakes, helpers, and DSLs
 *
 * We want to look at the tools available for making test writing easier and more maintainable.
 */
class Exercise2Test {
    private val testContext = SystemTestContext()

    // Helper method for Application DSL
    private fun SystemTestContext.withStoredApplication(
        /*
         * A (optional, because it has { this } as the default) lambda that can be
         * used to modify the application before storing it.
         */
        configure: Application.() -> Application = { this }
    ): Application {
        val defaultDate: LocalDate = LocalDate.of(2022, 2, 15)
        val customer = Customer.valid()
        return Application.valid(applicationDate = defaultDate, customerId = customer.id)
            .let(configure)
            .also { applicationService.registerInitialApplication(customer, it) }
    }

    /**
     * Write a test that registers a new application and checks that it was stored and have the correct state.
     * Use a helper DSL to create an application and store it such that it is ready for testing.
     *
     * Questions:
     * - When is this worth it?
     * - Could it be done with the assertions as well?
     */
    @Test
    fun shouldStoreApplication() {
        with(testContext) {
            // TODO: Implement this test
            // 1. Create an application and store it with the DSL
            // 2. Verify the application was created correctly
        }
    }

    /**
     * Write a test that registers an application, checks that it expires after 6 months,
     * and verifies that a notification was sent to the user telling them the application has expired.
     *
     * The fakes should help you verify that the notification was sent.
     *
     * Questions:
     * - How would you do this with mocks?
     * - How would mocks fare if something changed?
     */
    @Test
    fun shouldExpireApplicationAfter6Months() {
        with(testContext) {
            // TODO: Implement this test
            // 1. Create an application that will expire
            // 2. Advance time and expire applications
            // 3. Verify notification was sent
        }
    }

    /**
     * Write a test that demonstrates notification failure for a specific application ID
     *
     * The fakes should help you provoke the failure.
     *
     * Questions:
     * - How does this differ from stubs?
     * - How would you do this with mocks?
     */
    @Test
    fun shouldFailToNotifyForSpecificApplication() {
        with(testContext) {
            // TODO: Implement this test
            // 1. Create an application and register it for notification failure
            // 2. Verify that approval attempt throws NotificationSendException
            // 3. Verify that the underlying cause is IOException
        }
    }

    /**
     * Write a test that sets up all dependencies without the SystemContext.
     *
     * Questions:
     * - What is the benefit of using the SystemContext?
     * - What is the benefit of setting up all dependencies manually?
     * - How much should you set up manually?
     */
    @Test
    fun shouldStoreApplicationWithoutUsingSystemContext() {
        // TODO: Implement this test
        // 1. Set up all dependencies manually (applicationRepo, customerRepo, notificationClient, clock)
        // 2. Create a service with manual dependencies
        // 3. Set up test data
        // 4. Perform the action being tested
        // 5. Verify the expected outcome
    }
}
