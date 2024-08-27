package system

import customer.CustomerRepositoryFake
import application.ApplicationRepositoryFake
import notifications.UserNotificationClientFake

/**
 * Overrides the relevant properties to make them Fakes
 *
 * Notice that this Context only overrides the Repos/Clients with fakes.
 * The actual injection of Services (that I usually don't fake) is done in the superclass via the lazy construct.
 *
 * See here for the super class that this test context inherits/overrides:
 * https://github.com/anderssv/the-example/blob/main/src/main/kotlin/system/SystemContext.kt
 * 
 * To see usage, you can see here: https://github.com/anderssv/the-example/blob/main/src/test/kotlin/tttd/TestingThroughTheDomainTest.kt#L26
 */
class SystemTestContext : SystemContext() {
    class Repositories : SystemContext.Repositories() {
        override val applicationRepo = ApplicationRepositoryFake()
        override val customerRepository = CustomerRepositoryFake()
    }

    class Clients : SystemContext.Clients() {
        override val userNotificationClient = UserNotificationClientFake()
    }

    // Override the contexts with Fakes
    override val repositories = SystemTestContext.Repositories()
    override val clients = SystemTestContext.Clients()
}
