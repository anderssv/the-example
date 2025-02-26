package workshop


import application.*
import customer.CustomerRepositoryFake
import notifications.UserNotificationClientFake
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import system.SystemTestContext
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

    @Test
    fun shouldDemonstrateDslUsage() {
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

    @Test
    fun shouldDemonstrateMultipleApplicationsWithDsl() {
        with(testContext) {
            // Arrange: Set initial date and create applications
            clock.setTo(LocalDate.of(2023, 1, 1))

            // Create applications with different dates (1 month apart)
            val applications = listOf(
                application { copy(applicationDate = LocalDate.now(clock)) },
                application { copy(applicationDate = LocalDate.now(clock).plusMonths(1)) },
                application { copy(applicationDate = LocalDate.now(clock).plusMonths(2)) }
            )

            // Act: Move time forward 7 months (past 6-month expiration for first application)
            clock.advance(Duration.ofDays(7 * 30))
            applicationService.expireApplications()

            // Assert: Verify the expected outcomes
            // First application (Jan) should be expired (7 months > 6 months)
            assertThat(repositories.applicationRepo.getApplication(applications[0].id).status)
                .isEqualTo(ApplicationStatus.EXPIRED)
            // Last application (Mar) should still be active (5 months < 6 months)
            assertThat(repositories.applicationRepo.getApplication(applications[2].id).status)
                .isEqualTo(ApplicationStatus.ACTIVE)
        }
    }

    @Test
    fun shouldDemonstrateNotificationVerification() {
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

    @Test
    fun shouldDemonstrateComplexScenario() {
        with(testContext) {
            // Arrange: Set up multiple applications with different states
            val activeApp = application {
                copy(
                    applicationDate = LocalDate.now(clock),
                    name = "Active User"
                )
            }

            val expiredApp = application {
                copy(
                    applicationDate = LocalDate.now(clock).minusMonths(7),
                    name = "Expired User"
                )
            }

            // Act: Process applications
            applicationService.expireApplications()

            // Assert: Verify complex scenario outcomes
            // Active application should remain active
            assertThat(repositories.applicationRepo.getApplication(activeApp.id).status)
                .isEqualTo(ApplicationStatus.ACTIVE)

            // Expired application should be expired and notification sent
            val expiredStatus = repositories.applicationRepo.getApplication(expiredApp.id).status
            assertThat(expiredStatus).isEqualTo(ApplicationStatus.EXPIRED)

            val notifications = clients.userNotificationClient.getNotificationForUser("Expired User")
            assertThat(notifications).contains("Your application ${expiredApp.id} has expired")
        }
    }

    /**
     * This test demonstrates how to set up the system without relying on the system context as a helper class.
     */
    @Test
    fun shouldWorkWithoutSystemContext() {
        // Arrange: Set up all dependencies manually
        val applicationRepo = ApplicationRepositoryFake()
        val customerRepo = CustomerRepositoryFake()
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
