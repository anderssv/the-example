package fakes

import application.Application
import application.ApplicationRepository
import application.ApplicationRepositoryImpl
import application.ApplicationService
import customer.CustomerRepository
import customer.CustomerRepositoryFake
import customer.CustomerRepositoryImpl
import notifications.UserNotificationClient
import notifications.UserNotificationClientImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * This represents the main DI context and should not be on the test scope. Leaving here for now.
 */
@Suppress("LeakingThis")
open class DependencyInjectionContext {
    // Can be overridden in the subclass
    open val applicationRepo: ApplicationRepository = ApplicationRepositoryImpl()
    open val userNotificationRepo: UserNotificationClient = UserNotificationClientImpl()
    open val customerRepository: CustomerRepository = CustomerRepositoryImpl()

    // The main components using the IO stuff that can be faked
    val applicationService = ApplicationService(applicationRepo, userNotificationRepo, customerRepository)
}

/**
 * Overrides the relevant properties to make them Fakes
 */
class DependencyInjectionTestContext : DependencyInjectionContext() {
    override val applicationRepo = ApplicationRepositoryFake()
    override val userNotificationRepo = UserNotificationClientFake()
    override val customerRepository = CustomerRepositoryFake()
}


class ApplicationFakeTest {
    private val testContext = DependencyInjectionTestContext()

    @Test
    fun shouldRegisterApplicationSuccessfullyAndRegisterOnPerson() {
        with(testContext) {
            val application = Application.valid()

            applicationService.registerInitialApplication(application)

            assertThat(applicationService.applicationsForName(application.name)).contains(application)
        }
    }

    @Test
    fun shouldRegisterApplicationSuccessfullyAndExpireWhenTooOld() {
        with(testContext) {
            val applications = (0..2).map { counter ->
                Application.valid(addToMonth = counter.toLong()).also {
                    applicationService.registerInitialApplication(it)
                }
            }

            applicationService.expireApplications()

            // Assertions
            applicationService.openApplicationsFor(applications.first().name).let {
                assertThat(it).doesNotContain(applications.first())
                // Spot the bug ;)
                //assertThat(it).contains(applications.last())
            }
        }
    }

    @Test
    fun shouldSendNotificationWhenApplicationIsExpired() {
        with(testContext) {
            val application = Application.valid(addToMonth = -6)
            applicationService.registerInitialApplication(application)

            applicationService.expireApplications()

            assertThat(applicationService.openApplicationsFor(application.name)).isEmpty()
            userNotificationRepo.getNotificationForUser(application.name).also {
                // Notice how this is a specific method in the Fake. In the case of something
                // like e-mail, there is no way of fetching the actual messages after the fact.
                // So this method is used to verify the outcome, which should be that notifications
                // are sent to the user.
                assertThat(it).isNotEmpty
                assertThat(it).contains("Your application ${application.id} has expired")
            }
        }
    }
}