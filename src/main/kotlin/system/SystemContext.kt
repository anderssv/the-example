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
open class SystemContext {
    open class Repositories {
        // Can be overridden in the subclass
        open val applicationRepo: ApplicationRepository = ApplicationRepositoryImpl()
        open val customerRepository: CustomerRepository = CustomerRepositoryImpl()
    }

    open class Clients {
        open val userNotificationClient: UserNotificationClient = UserNotificationClientImpl()
    }

    open val repositories = Repositories()
    open val clients = Clients()

    // The main components using the IO stuff that can be faked
    val applicationService = ApplicationService(
        repositories.applicationRepo,
        clients.userNotificationClient,
        repositories.customerRepository
    )
}
