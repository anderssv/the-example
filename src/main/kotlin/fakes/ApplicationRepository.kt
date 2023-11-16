package fakes

interface ApplicationRepository {
    fun addApplication(application: Application)
    fun getApplicationsForName(name: String): List<Application>
    fun allApplications(): List<Application>
    fun activeApplications(): List<Application>
    fun updateApplication(application: Application)

}

/**
 * This is normally your implemented JDBC/something repo
 */
class ApplicationRepositoryImpl: ApplicationRepository {
    override fun addApplication(application: Application) {
        TODO("Not yet implemented")
    }

    override fun getApplicationsForName(name: String): List<Application> {
        TODO("Not yet implemented")
    }

    override fun allApplications(): List<Application> {
        TODO("Not yet implemented")
    }

    override fun activeApplications(): List<Application> {
        TODO("Not yet implemented")
    }

    override fun updateApplication(application: Application) {
        TODO("Not yet implemented")
    }
}
