package system

import application.ApplicationRepository
import application.ApplicationRepositoryImpl
import application.ApplicationService
import brreg.BrregClient
import brreg.BrregClientImpl
import customer.CustomerRegisterClient
import customer.CustomerRegisterClientImpl
import notifications.UserNotificationClient
import notifications.UserNotificationClientImpl
import java.time.Clock
import javax.sql.DataSource

/**
 * Interface-first contract for application dependencies.
 *
 * Both production ([SystemContext]) and test ([SystemTestContext][system.SystemTestContext]) contexts
 * implement this interface independently — no inheritance between them.
 *
 * Benefits of using an interface (not an open class):
 * - No constructor parameters to satisfy in test implementations
 * - Test implementations can narrow return types (covariant override inference)
 * - No lazy needed — overriding an eager val in a subclass does NOT prevent the base class
 *   initializer from running, which is why interface + standalone implementations was chosen
 * - Production wiring stays in one place
 *
 * See the test context that provides fakes:
 * https://github.com/anderssv/the-example/blob/main/src/test/kotlin/system/SystemTestContext.kt
 */
interface AppDependencies {
    interface Repositories {
        val applicationRepo: ApplicationRepository
    }

    interface Clients {
        val customerRepository: CustomerRegisterClient
        val userNotificationClient: UserNotificationClient
        val brregClient: BrregClient
    }

    interface Services {
        val applicationService: ApplicationService
    }

    val repositories: Repositories
    val clients: Clients
    val services: Services
    val clock: Clock
}

/**
 * Production context — plain class, no open, no lazy.
 *
 * Eager val initialization throughout. Infrastructure (DataSource) lives directly
 * on SystemContext, not exposed through [AppDependencies].
 *
 * Grouping implementations are anonymous objects that capture infrastructure
 * from the enclosing scope.
 */
class SystemContext(
    private val dataSource: DataSource,
) : AppDependencies {
    override val clock: Clock = Clock.systemDefaultZone()

    override val repositories =
        object : AppDependencies.Repositories {
            override val applicationRepo: ApplicationRepository = ApplicationRepositoryImpl(dataSource)
        }

    override val clients =
        object : AppDependencies.Clients {
            override val customerRepository: CustomerRegisterClient = CustomerRegisterClientImpl()
            override val userNotificationClient: UserNotificationClient = UserNotificationClientImpl()
            override val brregClient: BrregClient = BrregClientImpl()
        }

    override val services =
        object : AppDependencies.Services {
            override val applicationService =
                ApplicationService(
                    repositories.applicationRepo,
                    clients.customerRepository,
                    clients.userNotificationClient,
                    clock,
                )
        }
}
