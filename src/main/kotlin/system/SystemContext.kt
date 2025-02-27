package system

import application.ApplicationRepository
import application.ApplicationRepositoryImpl
import application.ApplicationService
import customer.CustomerRegisterClient
import customer.CustomerRegisterClientImpl
import notifications.UserNotificationClient
import notifications.UserNotificationClientImpl
import java.time.Clock

/**
 * This represents the main DI context.
 *
 * Using lazy for the implementations here as we don't want them to load if overridden.
 * This can be important for third party integrations and DBs.
 * If you know a better way, please let me know.
 *
 * See here for a subclass that overrides the relevant parts with fakes (in the test scope):
 * https://github.com/anderssv/the-example/blob/main/src/test/kotlin/system/SystemTestContext.kt
 */
@Suppress("LeakingThis")
open class SystemContext { // You can pass things like config and DB in here, YMMV
    // Just some namespacing
    open class Repositories {
        // Can be overridden in the subclass
        open val applicationRepo: ApplicationRepository by lazy { ApplicationRepositoryImpl() }
        open val customerRepository: CustomerRegisterClient by lazy { CustomerRegisterClientImpl() }
    }

    // Just some namespacing
    open class Clients {
        open val userNotificationClient: UserNotificationClient by lazy { UserNotificationClientImpl() }
    }

    // Open because they will be overridden with Fakes
    open val repositories = Repositories()
    open val clients = Clients()
    open val clock: Clock = Clock.systemDefaultZone()

    // The main components using the IO stuff that can be faked
    // Using lazy here to get the overridden values from the subclass that overrides clients and repos
    val applicationService by lazy {
        ApplicationService(
            repositories.applicationRepo,
            repositories.customerRepository,
            clients.userNotificationClient,
            clock
        )
    }
}
