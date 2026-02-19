package system

import application.ApplicationRepositoryFake
import application.ApplicationRepositoryImpl
import application.ApplicationService
import brreg.BrregClientFake
import customer.CustomerRegisterClientFake
import notifications.UserNotificationClientFake
import javax.sql.DataSource

/**
 * Test context — standalone class implementing [AppDependencies] independently.
 * Does NOT extend [SystemContext] — no inheritance between production and test contexts.
 *
 * Uses inner classes for groupings, which allows access to enclosing context properties.
 * Covariant override inference means `repositories.applicationRepo` resolves to
 * [ApplicationRepositoryFake] in test scope — no dual-access (testRepositories vs repositories) needed.
 *
 * Constructor injection with defaults for E2E flexibility:
 * ```
 * val testContext = SystemTestContext(dataSource = realDataSource)
 * ```
 *
 * For integration tests that need a real database, pass a [DataSource]:
 * ```
 * @ExtendWith(SharedDataSourceParameterResolver::class)
 * class MyIntegrationTest(private val dataSource: DataSource) {
 *     @Test
 *     fun shouldStoreAndRetrieve() {
 *         with(SystemTestContext(dataSource = dataSource)) {
 *             // ...
 *         }
 *     }
 * }
 * ```
 *
 * E2E partial overrides using delegation:
 * ```
 * val testContext = SystemTestContext()
 * val dependencies = object : AppDependencies by testContext {
 *     override val services = object : AppDependencies.Services by testContext.services {
 *         override val applicationService = customService
 *     }
 * }
 * ```
 *
 * See the production context:
 * https://github.com/anderssv/the-example/blob/main/src/main/kotlin/system/SystemContext.kt
 *
 * See usage examples:
 * https://github.com/anderssv/the-example/blob/main/src/test/kotlin/application/TestingThroughTheDomainTest.kt
 */
class SystemTestContext(
    dataSource: DataSource? = null,
) : AppDependencies {
    override val clock = TestClock.now()

    /**
     * Inner class for repository grouping — allows access to enclosing context properties.
     * Covariant override: [applicationRepo] is typed as [ApplicationRepositoryFake],
     * which satisfies the interface contract while giving tests direct access to fake methods.
     */
    inner class TestRepositories : AppDependencies.Repositories {
        override val applicationRepo = ApplicationRepositoryFake()
    }

    /**
     * Inner class for client grouping — allows access to enclosing context properties.
     * Each property is typed as the concrete fake, not the interface.
     */
    inner class TestClients : AppDependencies.Clients {
        override val customerRepository = CustomerRegisterClientFake()
        override val userNotificationClient = UserNotificationClientFake()
        override val brregClient = BrregClientFake()
    }

    /**
     * Override with test implementation.
     * When a [DataSource] is provided, uses real JDBC repositories backed by that DataSource.
     * Otherwise uses in-memory fakes via [TestRepositories].
     *
     * Covariant override inference: when no DataSource is provided, the inferred type is
     * [TestRepositories], so `repositories.applicationRepo` resolves to [ApplicationRepositoryFake].
     */
    override val repositories =
        if (dataSource != null) {
            object : AppDependencies.Repositories {
                override val applicationRepo = ApplicationRepositoryImpl(dataSource)
            }
        } else {
            TestRepositories()
        }

    /**
     * Covariant override inference: the inferred type is [TestClients],
     * so `clients.userNotificationClient` resolves to [UserNotificationClientFake].
     */
    override val clients = TestClients()

    override val services =
        object : AppDependencies.Services {
            override val applicationService =
                ApplicationService(
                    repositories.applicationRepo,
                    clients.customerRepository,
                    clients.userNotificationClient,
                    clock,
                )
        }
}
