package application

interface ApplicationRepository {
    fun getApplicationsForName(name: String): List<Application>
    fun getAllApplications(): List<Application>

    /**
     * Whether you want separate methods for each filter or do some andvanced filtering
     * depends a bit on your data volumes and DB design. YMMV. :)
     */
    fun getAllActiveApplications(): List<Application>
    fun addApplication(application: Application)
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

    override fun getAllApplications(): List<Application> {
        TODO("Not yet implemented")
    }

    override fun getAllActiveApplications(): List<Application> {
        TODO("Not yet implemented")
    }

    override fun updateApplication(application: Application) {
        TODO("Not yet implemented")
    }
}
