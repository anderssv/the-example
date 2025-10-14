package application

import java.util.*

class ApplicationRepositoryFake : ApplicationRepository {
    private val db = mutableMapOf<UUID, Application>()

    override fun updateApplication(application: Application) {
        db[application.id] = application
    }

    override fun getApplication(applicationId: UUID): Application = db[applicationId]!!

    override fun addApplication(application: Application) {
        db[application.id] = application
    }

    override fun getApplicationsForName(name: String): List<Application> = db.values.filter { it.name == name }

    override fun getAllApplications(statuses: List<ApplicationStatus>): List<Application> =
        db.values.filter { it.status in statuses }.toList()
}
