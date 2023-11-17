package fakes

import application.Application
import application.ApplicationRepository
import application.ApplicationStatus
import java.util.*

class ApplicationRepositoryFake : ApplicationRepository {
    private val db = mutableMapOf<UUID, Application>()

    override fun addApplication(application: Application) {
        db[application.id] = application
    }

    override fun getApplicationsForName(name: String): List<Application> {
        return db.values.filter { it.name == name }
    }

    override fun getAllApplications(): List<Application> {
        return db.values.toList()
    }

    override fun getAllActiveApplications(): List<Application> {
        return db.values.filter { it.status == ApplicationStatus.ACTIVE }
    }

    override fun updateApplication(application: Application) {
        db[application.id] = application
    }

}
