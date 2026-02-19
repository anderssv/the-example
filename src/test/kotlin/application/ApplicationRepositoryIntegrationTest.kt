package application

import customer.Customer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import system.SharedDataSourceParameterResolver
import system.SystemTestContext
import javax.sql.DataSource

@Tag("database")
@ExtendWith(SharedDataSourceParameterResolver::class)
class ApplicationRepositoryIntegrationTest(
    private val dataSource: DataSource,
) {
    @Test
    fun shouldStoreAndRetrieveApplication() {
        with(SystemTestContext(dataSource)) {
            val customer = Customer.valid()
            val application = Application.valid(customerId = customer.id)

            repositories.applicationRepo.addApplication(application)

            val retrieved = repositories.applicationRepo.getApplication(application.id)
            assertThat(retrieved).isEqualTo(application)
        }
    }

    @Test
    fun shouldRetrieveApplicationsByName() {
        with(SystemTestContext(dataSource)) {
            val customer = Customer.valid()
            val application = Application.valid(customerId = customer.id)

            repositories.applicationRepo.addApplication(application)

            val applications = repositories.applicationRepo.getApplicationsForName(application.name)
            assertThat(applications).contains(application)
        }
    }

    @Test
    fun shouldRetrieveApplicationsByStatus() {
        with(SystemTestContext(dataSource)) {
            val customer = Customer.valid()
            val activeApp = Application.valid(customerId = customer.id)
            val deniedApp = Application.valid(customerId = customer.id).copy(status = ApplicationStatus.DENIED)

            repositories.applicationRepo.addApplication(activeApp)
            repositories.applicationRepo.addApplication(deniedApp)

            val activeApps = repositories.applicationRepo.getAllApplications(listOf(ApplicationStatus.ACTIVE))
            assertThat(activeApps).contains(activeApp)
            assertThat(activeApps).doesNotContain(deniedApp)
        }
    }

    @Test
    fun shouldUpdateApplication() {
        with(SystemTestContext(dataSource)) {
            val customer = Customer.valid()
            val application = Application.valid(customerId = customer.id)

            repositories.applicationRepo.addApplication(application)

            val updated = application.copy(status = ApplicationStatus.EXPIRED)
            repositories.applicationRepo.updateApplication(updated)

            val retrieved = repositories.applicationRepo.getApplication(application.id)
            assertThat(retrieved).isEqualTo(updated)
        }
    }
}
