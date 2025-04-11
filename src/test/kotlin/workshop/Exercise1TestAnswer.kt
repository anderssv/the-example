package workshop

import application.Application
import application.ApplicationStatus
import application.valid
import customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import system.SystemTestContext
import java.time.LocalDate

/**
 * Exercise 1 - Bootup, test data and arrange-assert-act
 *
 * We use the testContext to get access to the system under test.
 * We will dive into the setup of the testContext in a later exercise.
 */
class Exercise1TestAnswer {
    private val testContext = SystemTestContext()

    /**
     * Write a test that registers an application and verifies that it was stored correctly.
     *
     * Hint: Use the applicationService to register the application and the applicationRepo to verify that it was stored correctly.
     *
     * Questions:
     * - How do we set up and re-use test data?
     * - What is the best abstraction to verify that the application was stored correctly?
     */
    @Test
    fun shouldRegisterApplicationAndStoreCorrectly() {
        with(testContext) {
            // Arrange: Set up test data. In this case, an application
            val customer = Customer.valid()
            val application = Application.valid(customerId = customer.id)

            // Act: Register the application, use applicationService
            applicationService.registerInitialApplication(customer, application)

            // Assert: Verify that the application was stored correctly in the repository
            val storedApplication = repositories.applicationRepo.getApplication(application.id)
            assertThat(storedApplication.status).isEqualTo(ApplicationStatus.ACTIVE)
            assertThat(storedApplication.name).isEqualTo("Tester One")
        }
    }

    /**
     * Write a test that registers and application that is older than 6 months, expires it and verifies that it was expired.
     *
     * Questions:
     * - How can we re-use test data setup and make in tunable in each test?
     * - How do we signify what are the important changes for that test?
     * - Should we use data on the application to verify or ask questions?
     */
    @Test
    fun shouldRegisterAndApplicationAndModifyForTesting() {
        with(testContext) {
            // Arrange: Set up test data with an application older than 6 months
            val customer = Customer.valid()
            val application = Application.valid(customerId = customer.id).copy(
                name = "Custom Name",
                applicationDate = LocalDate.of(2023, 1, 1)
            )
            applicationService.registerInitialApplication(customer, application)

            // Act: Expire the application through domain logic in applicationService
            applicationService.expireApplications()

            // Assert: Verify the application is expired in the repository
            val storedApplication = repositories.applicationRepo.getApplication(application.id)
            assertThat(storedApplication.status).isEqualTo(ApplicationStatus.EXPIRED)
        }
    }

    /**
     * Write a test that verifies that the service throws an IllegalStateException when trying to approve a DENIED application.
     *
     * Questions:
     * - Is this a normal control flow or an exceptional case?
     * - What solutions are alternatives here?
     * - Can or should we encode the arrange, act assert steps?
     */
    @Test
    fun shouldThrowExceptionWhenApprovingDeniedApplication() {
        with(testContext) {
            // Arrange: Set up a test data application and store it in the system
            val customer = Customer.valid()
            val application = Application.valid(customerId = customer.id)

            applicationService.registerInitialApplication(customer, application)
            applicationService.rejectApplication(application.id)

            // Act & Assert: Verify that attempting to approve throws IllegalStateException
            assertThrows(IllegalStateException::class.java) {
                applicationService.approveApplication(application.id)
            }
        }
    }
}
