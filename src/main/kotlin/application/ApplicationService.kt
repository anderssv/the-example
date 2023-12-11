package application

import customer.Customer
import customer.CustomerRepository
import notifications.UserNotificationClient
import java.time.LocalDate
import java.util.*

class ApplicationService(
    private val applicationRepo: ApplicationRepository,
    private val customerRepository: CustomerRepository,
    private val userNotificationClient: UserNotificationClient
) {
    fun registerInitialApplication(application: Application) {
        applicationRepo.addApplication(application)
        customerRepository.addCustomer(Customer(application.name, true))
    }

    fun applicationsForName(name: String): List<Application> {
        return applicationRepo.getApplicationsForName(name)
    }

    fun expireApplications() {
        applicationRepo.getAllActiveApplications()
            .filter { it.applicationDate.isBefore(LocalDate.now().minusMonths(2)) }
            .forEach {
                applicationRepo.updateApplication(it.copy(status = ApplicationStatus.EXPIRED))
                userNotificationClient.notifyUser(it.name, "Your application ${it.id} has expired")
            }
    }

    fun openApplicationsFor(name: String): List<Application> {
        return applicationRepo.getApplicationsForName(name).filter { it.status == ApplicationStatus.ACTIVE }
    }

    fun approveApplication(applicationId: UUID) {
        val application = applicationRepo.getApplication(applicationId)
        if (!customerRepository.getCustomer(application.name).active) throw IllegalStateException("Customer not active")

        applicationRepo.updateApplication(application.copy(status = ApplicationStatus.APPROVED))
    }

}
