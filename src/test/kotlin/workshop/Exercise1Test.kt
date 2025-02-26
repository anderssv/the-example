package workshop

import application.Application
import application.ApplicationStatus
import application.valid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import system.SystemTestContext
import java.time.LocalDate
import java.time.Duration

/**
 * Exercise 1 - Bootup, test data and arrange-assert-act
 */
class Exercise1Test {
    private val testContext = SystemTestContext()

    /**
     * Write a test that registers an application and verifies that it was stored correctly.
     */
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

    /**
     * Write a test that registers and application, expires it after 6 months and verifies that it was expired.
     */
    @Test
    fun shouldDemonstrateTestDataCustomization() {
        with(testContext) {
            // Arrange: Set up test data with custom values
            val customDate = LocalDate.of(2023, 1, 1)
            val application = Application.valid(applicationDate = customDate).copy(name = "Custom Name")

            // Act: Register application and advance time by 6 months
            applicationService.registerInitialApplication(application)

            // Verify initial state
            var storedApplication = repositories.applicationRepo.getApplication(application.id)
            assertThat(storedApplication.status).isEqualTo(ApplicationStatus.ACTIVE)
            assertThat(storedApplication.applicationDate).isEqualTo(customDate)
            assertThat(storedApplication.name).isEqualTo("Custom Name")

            // Advance time by 6 months and expire applications
            clock.advance(Duration.ofDays(180))
            applicationService.expireApplications()

            // Assert: Verify the application is expired
            storedApplication = repositories.applicationRepo.getApplication(application.id)
            assertThat(storedApplication.status).isEqualTo(ApplicationStatus.EXPIRED)
        }
    }
}
