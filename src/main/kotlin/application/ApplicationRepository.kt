package application

import java.util.UUID

interface ApplicationRepository {
    fun getApplicationsForName(name: String): List<Application>

    fun getAllApplications(statuses: List<ApplicationStatus>): List<Application>

    fun addApplication(application: Application)

    fun updateApplication(application: Application)

    fun getApplication(applicationId: UUID): Application
}

/**
 * This is normally your implemented JDBC/something repo
 */
class ApplicationRepositoryImpl : ApplicationRepository {
    override fun addApplication(application: Application) {
        TODO("Not yet implemented")
    }

    override fun getApplicationsForName(name: String): List<Application> {
        TODO("Not yet implemented")
    }

    override fun getAllApplications(statuses: List<ApplicationStatus>): List<Application> {
        TODO("Not yet implemented")
    }

    override fun updateApplication(application: Application) {
        TODO("Not yet implemented")
    }

    override fun getApplication(applicationId: UUID): Application {
        TODO("Not yet implemented")
    }
}
