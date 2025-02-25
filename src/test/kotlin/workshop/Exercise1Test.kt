package workshop
import application.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import customer.CustomerRepositoryFake
import notifications.UserNotificationClientFake
import system.SystemTestContext
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class Exercise1Test {
    private val testContext = SystemTestContext()

    @Test
    fun shouldDemonstrateArrangeActAssertPattern() {
        with(testContext) {
            // Arrange: Set up test data and initial conditions
            val application = Application.valid()

            // Act: Perform the action being tested
            applicationService.registerInitialApplication(application)

            // Assert: Verify the expected outcome
            val storedApplication = repositories.applicationRepo.getApplication(application.id)
            assertThat(storedApplication.status).isEqualTo(ApplicationStatus.ACTIVE)
            assertThat(storedApplication.name).isEqualTo("Tester One")
            assertThat(storedApplication.applicationDate).isEqualTo(LocalDate.of(2022, 2, 15))
        }
    }

    @Test
    fun shouldDemonstrateTestDataCustomization() {
        with(testContext) {
            // Arrange: Set up test data with custom values
            val customDate = LocalDate.of(2023, 1, 1)
            val application = Application.valid(applicationDate = customDate).copy(name = "Custom Name")

            // Act: Perform the action being tested
            applicationService.registerInitialApplication(application)

            // Assert: Verify the expected outcome with custom values
            val storedApplication = repositories.applicationRepo.getApplication(application.id)
            assertThat(storedApplication.status).isEqualTo(ApplicationStatus.ACTIVE)
            assertThat(storedApplication.applicationDate).isEqualTo(customDate)
            assertThat(storedApplication.name).isEqualTo("Custom Name")
        }
    }

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
