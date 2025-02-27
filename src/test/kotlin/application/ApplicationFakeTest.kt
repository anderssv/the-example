package application

import customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import system.SystemTestContext
import java.time.Duration
import java.time.LocalDate

class ApplicationFakeTest {
    private val testContext = SystemTestContext()

    @Test
    fun shouldRegisterApplicationSuccessfullyAndRegisterOnPerson() {
        with(testContext) {
            val customer = Customer.valid()
            repositories.customerRepository.addCustomer(customer)

            val application = Application.valid(customerId = customer.id)
            applicationService.registerInitialApplication(application)
            assertThat(applicationService.applicationsForName(application.name)).contains(application)
        }
    }

    @Test
    fun shouldRegisterApplicationSuccessfullyAndExpireWhenTooOld() {
        with(testContext) {
            // Set initial date
            clock.setTo(LocalDate.of(2022, 1, 1))

            // Create applications with different dates
            val applications = (0..2).map { counter ->
                val customer = Customer.valid()
                repositories.customerRepository.addCustomer(customer)

                Application.valid(
                    applicationDate = LocalDate.now(clock).plusMonths(counter.toLong()),
                    customer.id
                ).also {
                    applicationService.registerInitialApplication(it)
                }
            }

            // Move time forward to where first application is expired but last is still valid
            // First app: 2022-01-01 + 6 months = 2022-07-01 (expired)
            // Last app: 2022-03-01 + 6 months = 2022-09-01 (still valid)
            clock.advance(Duration.ofDays(7 * 30))  // Advance 7 months
            applicationService.expireApplications()

            // Assertions
            applicationService.activeApplicationFor(applications.first().name).let {
                assertThat(it).doesNotContain(applications.first())  // First application should be expired
                assertThat(it).contains(applications.last())         // Last application should still be active
            }
        }
    }

    @Test
    fun shouldSendNotificationWhenApplicationIsExpired() {
        with(testContext) {
            // Set initial date and create application using current time
            clock.setTo(LocalDate.of(2022, 1, 1))

            val customer = Customer.valid()
            repositories.customerRepository.addCustomer(customer)

            val application = Application.valid(applicationDate = LocalDate.now(clock), customer.id)
            applicationService.registerInitialApplication(application)

            // Move time forward past expiration
            clock.advance(Duration.ofDays(7 * 30))  // Advance 7 months
            applicationService.expireApplications()

            assertThat(applicationService.activeApplicationFor(application.name)).isEmpty()
            clients.userNotificationClient.getNotificationForUser(application.name).also {
                // Notice how this is a specific method in the Fake. In the case of something
                // like e-mail, there is no way of fetching the actual messages after the fact.
                // So this method is used to verify the outcome, which should be that notifications
                // are sent to the user.
                //
                // Try to focus on verifying the system results, but sometimes you need to validate
                // the interactions as well.
                assertThat(it).isNotEmpty
                assertThat(it).contains("Your application ${application.id} has expired")
            }
        }
    }
}
