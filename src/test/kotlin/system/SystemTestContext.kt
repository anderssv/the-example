package system

import customer.CustomerRepositoryFake
import fakes.ApplicationRepositoryFake
import fakes.UserNotificationClientFake

/**
 * Overrides the relevant properties to make them Fakes
 */
class SystemTestContext : SystemContext() {
    class Repositories : SystemContext.Repositories() {
        override val applicationRepo = ApplicationRepositoryFake()
        override val customerRepository = CustomerRepositoryFake()
    }

    class Clients : SystemContext.Clients() {
        override val userNotificationClient = UserNotificationClientFake()
    }

    override val repositories = Repositories()
    override val clients = Clients()
}
