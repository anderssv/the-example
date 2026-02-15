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

/**
 * Manual dependency injection using interfaces and anonymous objects.
 *
 * This pattern uses interfaces for dependency grouping instead of open classes with constructor parameters.
 * The reason: open classes with constructor parameters force test subclasses to satisfy those parameters,
 * even when test fakes never use them (e.g., creating a dummy DataSource just to satisfy a constructor).
 *
 * Benefits of using interfaces:
 * - No constructor parameters to satisfy in test subclasses
 * - Test implementations can have concrete types (e.g., ApplicationRepositoryFake instead of ApplicationRepository)
 * - No casting needed in tests to access fake-specific methods
 * - Production wiring stays in one place (anonymous objects inside this context)
 *
 * See the test context that provides fakes:
 * https://github.com/anderssv/the-example/blob/main/src/test/kotlin/system/SystemTestContext.kt
 */
open class SystemContext { // You can pass things like config and DB in here
    /**
     * Interface for repository dependencies.
     * Using an interface (not an open class) means:
     * - No constructor parameters
     * - Test implementations don't inherit production defaults
     * - Tests must explicitly provide every dependency
     */
    interface Repositories {
        val applicationRepo: ApplicationRepository
    }

    /**
     * Interface for external client dependencies.
     * Using an interface (not an open class) means:
     * - No constructor parameters
     * - Test implementations don't inherit production defaults
     * - Tests must explicitly provide every dependency
     */
    interface Clients {
        val customerRepository: CustomerRegisterClient
        val userNotificationClient: UserNotificationClient
        val brregClient: BrregClient
    }

    /**
     * Production implementation of repositories using anonymous objects.
     * The anonymous object captures context properties (like dataSource if we had one).
     * Using lazy prevents initialization if overridden in test contexts.
     */
    open val repositories: Repositories by lazy {
        object : Repositories {
            override val applicationRepo: ApplicationRepository by lazy { ApplicationRepositoryImpl() }
        }
    }

    /**
     * Production implementation of clients using anonymous objects.
     * The anonymous object captures context properties (like config if we had one).
     * Using lazy prevents initialization if overridden in test contexts.
     */
    open val clients: Clients by lazy {
        object : Clients {
            override val customerRepository: CustomerRegisterClient by lazy { CustomerRegisterClientImpl() }
            override val userNotificationClient: UserNotificationClient by lazy { UserNotificationClientImpl() }
            override val brregClient: BrregClient by lazy { BrregClientImpl() }
        }
    }

    /**
     * Clock for time-based operations. Overridden with TestClock in tests.
     */
    open val clock: Clock = Clock.systemDefaultZone()

    /**
     * The main application service using the IO dependencies.
     * Using lazy here ensures we get the overridden values from test subclasses.
     */
    val applicationService by lazy {
        ApplicationService(
            repositories.applicationRepo,
            clients.customerRepository,
            clients.userNotificationClient,
            clock,
        )
    }
}
