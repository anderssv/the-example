package fakes

import java.time.LocalDate

class ApplicationService(private val applicationRepo: ApplicationRepository) {
    fun registerInitialApplication(application: Application) {
        applicationRepo.addApplication(application)
    }

    fun applicationsForName(name: String): List<Application> {
        return applicationRepo.getApplicationsForName(name)
    }

    fun expireApplications() {
        applicationRepo.activeApplications()
            .filter { it.applicationDate.isBefore(LocalDate.now().minusMonths(2)) }
            .forEach {
                applicationRepo.updateApplication(it.copy(status = ApplicationStatus.EXPIRED))
            }
    }

    fun ongoingApplicationsForName(name: String): List<Application> {
        return applicationRepo.getApplicationsForName(name).filter { it.status == ApplicationStatus.ACTIVE }
    }

}
