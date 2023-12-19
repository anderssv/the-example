package system

import customer.CustomerRepositoryFake
import fakes.ApplicationRepositoryFake
import fakes.UserNotificationClientFake

/**
 * Overrides the relevant properties to make them Fakes
 *
 * Notice that this Context only overrides the Repos/Clients with fakes.
 * The actual injection of Services (that I usually don't fake) is done in the superclass via the lazy construct.
 *
 * See here for the super class that this test context inherits/overrides:
 * https://github.com/anderssv/the-example/blob/main/src/main/kotlin/system/SystemContext.kt
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
