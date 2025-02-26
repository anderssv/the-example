package application

import customer.Customer
import customer.CustomerRepository
import notifications.NotificationSendException
import notifications.UserNotificationClient
import java.io.IOException
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
            .forEach { application ->
                applicationRepo.updateApplication(application.copy(status = ApplicationStatus.EXPIRED))
                userNotificationClient.notifyUser(application.id, application.name, "Your application ${application.id} has expired")
            }
    }

    fun activeApplicationFor(name: String): List<Application> {
        return applicationRepo.getApplicationsForName(name).filter { it.status == ApplicationStatus.ACTIVE }
    }

    fun approveApplication(applicationId: UUID) {
        val application = applicationRepo.getApplication(applicationId)
        if (!customerRepository.getCustomer(application.name).active) throw IllegalStateException("Customer not active")
        if (application.status == ApplicationStatus.DENIED) throw IllegalStateException("Cannot approve a denied application")

        applicationRepo.updateApplication(application.copy(status = ApplicationStatus.APPROVED))
        try {
            userNotificationClient.notifyUser(application.id, application.name, "Your application ${application.id} has been approved")
        } catch (e: IOException) {
            throw NotificationSendException("Failed to send notification for application ${application.id}", e)
        }
    }
}
