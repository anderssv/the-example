package workshop


import application.*
import customer.Customer
import customer.CustomerRegisterClientFake
import notifications.NotificationSendException
import notifications.UserNotificationClientFake
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import system.SystemTestContext
import java.io.IOException
import java.time.*

/**
 * Exercise 2 - Fakes, helpers, and DSLs
 *
 * We want to look at the tools available for making test writing easier and more maintainable.
 */
class Exercise2TestAnswer {
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
            // Arrange: Create an application and store it with a DSL
            val application = withStoredApplication {
                copy(
                    name = "DSL Test User",
                    applicationDate = LocalDate.of(2023, 3, 1)
                )
            }

            // Assert: Verify the application was created correctly
            val storedApplication = repositories.applicationRepo.getApplication(application.id)
            assertThat(storedApplication.name).isEqualTo("DSL Test User")
            assertThat(storedApplication.applicationDate).isEqualTo(LocalDate.of(2023, 3, 1))
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
            // Arrange: Create an application that will expire
            val application = withStoredApplication {
                copy(applicationDate = LocalDate.of(2023, 1, 1))
            }

            // Act: Advance time and expire applications,
            // you could have just set time back in time but wouldn't show the usage
            clock.advance(Duration.ofDays(180))
            applicationService.expireApplications()

            // Assert: Verify notification was sent
            val notifications = clients.userNotificationClient.getNotificationForUser(application.name)
            assertThat(notifications).contains("Your application ${application.id} has expired")
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
            // Arrange: Create an application and register it for notification failure
            val application = withStoredApplication {
                copy(applicationDate = LocalDate.of(2023, 1, 1))
            }
            clients.userNotificationClient.registerApplicationIdForFailure(application.id)

            // Act & Assert: Verify that approval attempt throws NotificationSendException
            val exception = assertThrows(NotificationSendException::class.java) {
                applicationService.approveApplication(application.id)
            }

            // Verify that the underlying cause is IOException
            assertThat(exception.cause).isInstanceOf(IOException::class.java)
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
        // Arrange: Set up all dependencies manually
        val applicationRepo = ApplicationRepositoryFake()
        val customerRepo = CustomerRegisterClientFake()
        val notificationClient = UserNotificationClientFake()
        val fixedClock = Clock.fixed(
            Instant.parse("2023-01-01T10:00:00Z"),
            ZoneId.systemDefault()
        )

        // Create a service with manual dependencies
        val applicationService = ApplicationService(
            applicationRepo = applicationRepo,
            customerRepository = customerRepo,
            userNotificationClient = notificationClient,
            clock = fixedClock
        )

        // Arrange: Set up test data
        val customDate = LocalDate.of(2023, 1, 1)
        val customer = Customer.valid()
        val application = Application.valid(applicationDate = customDate, customerId = customer.id)
            .copy(name = "Manual Setup Test")

        // Act: Perform the action being tested
        applicationService.registerInitialApplication(customer, application)

        // Assert: Verify the expected outcome
        val storedApplication = applicationRepo.getApplication(application.id)
        assertThat(storedApplication.status).isEqualTo(ApplicationStatus.ACTIVE)
        assertThat(storedApplication.applicationDate).isEqualTo(customDate)
        assertThat(storedApplication.name).isEqualTo("Manual Setup Test")
    }

}