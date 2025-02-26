package workshop

import application.Application
import application.ApplicationStatus
import application.valid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import system.SystemTestContext
import java.time.LocalDate

/**
 * Exercise 1 - Bootup, test data and arrange-assert-act
 */
class Exercise1Test {
    private val testContext = SystemTestContext()

    /**
     * Write a test that registers an application and verifies that it was stored correctly.
     */
    @Test
    fun shouldRegisterApplicationAndStoreCorrectly() {
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
     * Write a test that registers and application that is older than 6 months, expires it and verifies that it was expired.
     */
    @Test
    fun shouldRegisterAndApplicationAndModifyForTesting() {
        with(testContext) {
            // Arrange: Set up test data with custom values
            val application = Application.valid(
                applicationDate = LocalDate.of(2023, 1, 1)
            ).copy(
                name = "Custom Name"
            )
            applicationService.registerInitialApplication(application)

            // Act: Expire the application
            applicationService.expireApplications()

            // Assert: Verify the application is expired
            val storedApplication = repositories.applicationRepo.getApplication(application.id)
            assertThat(storedApplication.status).isEqualTo(ApplicationStatus.EXPIRED)
        }
    }

    /**
     * Write a test that verifies that the service throws a IllegalStateException when trying to approve a DENIED application.
     */
    @Test
    fun shouldThrowExceptionWhenApprovingDeniedApplication() {
        with(testContext) {
            // Arrange: Set up test data with DENIED status
            val application = Application.valid().copy(
                status = ApplicationStatus.DENIED
            )
            applicationService.registerInitialApplication(application)

            // Act & Assert: Verify that attempting to approve throws IllegalStateException
            org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException::class.java) {
                applicationService.approveApplication(application.id)
            }
        }
    }
}
