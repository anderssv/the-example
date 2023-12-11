package system

import application.ApplicationRepository
import application.ApplicationRepositoryImpl
import application.ApplicationService
import customer.CustomerRepository
import customer.CustomerRepositoryImpl
import notifications.UserNotificationClient
import notifications.UserNotificationClientImpl

/**
 * This represents the main DI context and should not be on the test scope. Leaving here for now.
 *
 * Using lazy for the implementations here as we don't want them to load if overridden.
 * This can be important for third party integrations and DBs.
 * If you know a better way, please let me know.
 */
@Suppress("LeakingThis")
open class SystemContext { // You can pass things like config and DB in here, YMMV
    // Just some namespacing
    open class Repositories {
        // Can be overridden in the subclass
        open val applicationRepo: ApplicationRepository by lazy { ApplicationRepositoryImpl() }
        open val customerRepository: CustomerRepository by lazy { CustomerRepositoryImpl() }
    }

    // Just some namespacing
    open class Clients {
        open val userNotificationClient: UserNotificationClient by lazy { UserNotificationClientImpl() }
    }

    // Open because they will be overridden with Fakes
    open val repositories = Repositories()
    open val clients = Clients()

    // The main components using the IO stuff that can be faked
    // Using lazy here to get the overridden values from the subclass that overrides clients and repos
    val applicationService by lazy {
        ApplicationService(
            repositories.applicationRepo,
            repositories.customerRepository,
            clients.userNotificationClient
        )
    }
}
