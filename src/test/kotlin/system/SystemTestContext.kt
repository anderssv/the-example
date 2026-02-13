package system

import application.ApplicationRepositoryFake
import brreg.BrregClientFake
import customer.CustomerRegisterClientFake
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
    }

    class Clients : SystemContext.Clients() {
        override val customerRepository = CustomerRegisterClientFake()
        override val userNotificationClient = UserNotificationClientFake()
        override val brregClient = BrregClientFake()
    }

    // Override the contexts with Fakes
    override val repositories = Repositories()
    override val clients = Clients()
    override val clock = TestClock.now()
}
