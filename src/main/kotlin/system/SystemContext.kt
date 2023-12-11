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
 */
@Suppress("LeakingThis")
open class SystemContext { // You can pass things like config and DB in here, YMMV
    // Just some namespacing
    open class Repositories {
        // Can be overridden in the subclass
        open val applicationRepo: ApplicationRepository = ApplicationRepositoryImpl()
        open val customerRepository: CustomerRepository = CustomerRepositoryImpl()
    }

    // Just some namespacing
    open class Clients {
        open val userNotificationClient: UserNotificationClient = UserNotificationClientImpl()
    }

    // Open because they will be overridden with Fakes
    open val repositories = Repositories()
    open val clients = Clients()

    // The main components using the IO stuff that can be faked
    // Using lazy here to get the overridden values from the subclass that overrides clients and repos
    val applicationService by lazy {
        ApplicationService(
            repositories.applicationRepo,
            clients.userNotificationClient,
            repositories.customerRepository
        )
    }
}
