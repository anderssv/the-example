package application

import customer.Customer
import customer.CustomerRepository
import notifications.UserNotificationClient
import java.time.Clock
import java.time.LocalDate
import java.util.*

class ApplicationService(
    private val applicationRepo: ApplicationRepository,
    private val customerRepository: CustomerRepository,
    private val userNotificationClient: UserNotificationClient,
    private val clock: Clock
) {
    fun registerInitialApplication(application: Application) {
        applicationRepo.addApplication(application)
        customerRepository.addCustomer(Customer(application.name, true))
    }

    fun applicationsForName(name: String): List<Application> {
        return applicationRepo.getApplicationsForName(name)
    }

    fun expireApplications() {
        val currentDate = LocalDate.now(clock)
        applicationRepo.getAllApplications(listOf(ApplicationStatus.ACTIVE))
            .filter { !it.isValid(currentDate) }
            .forEach {
                applicationRepo.updateApplication(it.copy(status = ApplicationStatus.EXPIRED))
                userNotificationClient.notifyUser(it.name, "Your application ${it.id} has expired")
            }
    }

    fun activeApplicationFor(name: String): List<Application> {
        return applicationRepo.getApplicationsForName(name).filter { it.status == ApplicationStatus.ACTIVE }
    }

    fun approveApplication(applicationId: UUID) {
        val application = applicationRepo.getApplication(applicationId)
        if (!customerRepository.getCustomer(application.name).active) throw IllegalStateException("Customer not active")

        applicationRepo.updateApplication(application.copy(status = ApplicationStatus.APPROVED))
    }
}