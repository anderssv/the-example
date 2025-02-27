package workshop


import application.*
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
 */
class Exercise2Test {
    private val testContext = SystemTestContext()

    // Helper method for DSL
    private fun SystemTestContext.application(configure: Application.() -> Application = { this }): Application {
        val defaultDate: LocalDate = LocalDate.of(2022, 2, 15)
        return Application.valid(applicationDate = defaultDate)
            .let(configure)
            .also { applicationService.registerInitialApplication(it) }
    }

    /**
     * Write a test that registers a new application and checks that it was stored and have the correct state.
     * Use a helper DSL to create an application and store it such that it is ready for testing.
     */
    @Test
    fun shouldStoreApplication() {
        with(testContext) {
            // Arrange: Using DSL for cleaner test setup
            val application = application {
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
     * Write a test that registers an application, checks that it expires after 6 months
     * and verifies that a notification was sent to the user telling them the application has expired.
     *
     * The fakes should help you verify that the notification was sent.
     */
    @Test
    fun shouldExpireApplicationAfter6Months() {
        with(testContext) {
            // Arrange: Create an application that will expire
            val application = application {
                copy(applicationDate = LocalDate.of(2023, 1, 1))
            }

            // Act: Advance time and expire applications
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
     */
    @Test
    fun shouldFailToNotifyForSpecificApplication() {
        with(testContext) {
            // Arrange: Create an application and register it for notification failure
            val application = application {
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

        // Create service with manual dependencies
        val applicationService = ApplicationService(
            applicationRepo = applicationRepo,
            customerRepository = customerRepo,
            userNotificationClient = notificationClient,
            clock = fixedClock
        )

        // Arrange: Set up test data
        val customDate = LocalDate.of(2023, 1, 1)
        val application = Application.valid(applicationDate = customDate)
            .copy(name = "Manual Setup Test")

        // Act: Perform the action being tested
        applicationService.registerInitialApplication(application)

        // Assert: Verify the expected outcome
        val storedApplication = applicationRepo.getApplication(application.id)
        assertThat(storedApplication.status).isEqualTo(ApplicationStatus.ACTIVE)
        assertThat(storedApplication.applicationDate).isEqualTo(customDate)
        assertThat(storedApplication.name).isEqualTo("Manual Setup Test")

        // Verify customer was created
        val customer = customerRepo.getCustomer("Manual Setup Test")
        assertThat(customer.active).isTrue()
    }

}
