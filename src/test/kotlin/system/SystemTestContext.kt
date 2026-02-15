package system

import application.ApplicationRepositoryFake
import brreg.BrregClientFake
import customer.CustomerRegisterClientFake
import notifications.UserNotificationClientFake

/**
 * Test context with fakes using typed test implementations pattern.
 *
 * This pattern provides two access paths:
 * 1. repositories.applicationRepo - typed as ApplicationRepository (interface type, used by services)
 * 2. testRepositories.applicationRepo - typed as ApplicationRepositoryFake (concrete type, for test assertions)
 *
 * Benefits:
 * - No casting needed to access fake-specific methods
 * - Type safety: IDE autocomplete shows fake methods when using testRepositories
 * - Clear separation: production code uses abstract interfaces, tests use concrete fakes
 *
 * Usage in tests:
 * ```
 * with(SystemTestContext()) {
 *     // Arrange
 *     applicationService.registerInitialApplication(customer, application)
 *
 *     // Assert - direct access to fake methods, no casting needed
 *     assertThat(testRepositories.applicationRepo.getAllApplications())
 *         .contains(application)
 * }
 * ```
 *
 * See the production context:
 * https://github.com/anderssv/the-example/blob/main/src/main/kotlin/system/SystemContext.kt
 *
 * See usage examples:
 * https://github.com/anderssv/the-example/blob/main/src/test/kotlin/application/TestingThroughTheDomainTest.kt
 */
class SystemTestContext : SystemContext() {
    /**
     * Test implementation with concrete fake types.
     * Properties are typed as ApplicationRepositoryFake (not ApplicationRepository),
     * which allows accessing fake-specific methods without casting.
     */
    class TestRepositories : Repositories {
        override val applicationRepo = ApplicationRepositoryFake()
    }

    /**
     * Test implementation with concrete fake types.
     * Properties are typed as *ClientFake (not the interface),
     * which allows accessing fake-specific methods without casting.
     */
    class TestClients : Clients {
        override val customerRepository = CustomerRegisterClientFake()
        override val userNotificationClient = UserNotificationClientFake()
        override val brregClient = BrregClientFake()
    }

    /**
     * Typed test repositories - use this in test assertions to access fake-specific methods.
     * Type: TestRepositories (concrete class with ApplicationRepositoryFake properties)
     */
    val testRepositories = TestRepositories()

    /**
     * Typed test clients - use this in test assertions to access fake-specific methods.
     * Type: TestClients (concrete class with *ClientFake properties)
     */
    val testClients = TestClients()

    /**
     * Override abstract interface property with test implementation.
     * Production code accesses this as Repositories (abstract interface).
     * Type: Repositories (interface)
     */
    override val repositories: Repositories get() = testRepositories

    /**
     * Override abstract interface property with test implementation.
     * Production code accesses this as Clients (abstract interface).
     * Type: Clients (interface)
     */
    override val clients: Clients get() = testClients

    /**
     * Override clock with TestClock for time control in tests.
     */
    override val clock = TestClock.now()
}
