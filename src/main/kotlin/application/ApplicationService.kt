package application

import notifications.UserNotificationClient
import java.time.LocalDate

class ApplicationService(private val applicationRepo: ApplicationRepository, val userNotificationClient: UserNotificationClient) {
    fun registerInitialApplication(application: Application) {
        applicationRepo.addApplication(application)
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

}
