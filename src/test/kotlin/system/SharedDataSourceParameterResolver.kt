package system

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.testcontainers.postgresql.PostgreSQLContainer
import javax.sql.DataSource

/**
 * JUnit 5 ParameterResolver that provides a shared [DataSource] backed by a
 * Testcontainers PostgreSQL instance with a HikariCP connection pool.
 *
 * The container and connection pool are created once per test run (stored in
 * the JUnit root [ExtensionContext.Store]) and shared across all test classes
 * that use this resolver. This avoids spinning up a new container for every
 * test class while still giving each test a real database.
 *
 * Usage:
 * ```
 * @ExtendWith(SharedDataSourceParameterResolver::class)
 * class MyIntegrationTest(private val dataSource: DataSource) {
 *     private val testContext = SystemTestContext(dataSource)
 *
 *     @Test
 *     fun myTest() {
 *         with(testContext) { ... }
 *     }
 * }
 * ```
 *
 * The resolver injects the [DataSource] into test constructor parameters.
 * The test then passes it into [SystemTestContext], which wires it into
 * the repository layer — keeping the connection pool shared while the
 * test context (and its fakes) remain per-test.
 */
class SharedDataSourceParameterResolver : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        parameterContext.parameter.type == DataSource::class.java

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any =
        getOrCreateDataSource(extensionContext)

    private fun getOrCreateDataSource(extensionContext: ExtensionContext): DataSource {
        val store = extensionContext.root.getStore(NAMESPACE)
        return store.getOrComputeIfAbsent(
            DATASOURCE_KEY,
            { createDataSource() },
            CloseableDataSource::class.java,
        ).dataSource
    }

    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create(SharedDataSourceParameterResolver::class.java)
        private const val DATASOURCE_KEY = "shared-datasource"

        private fun createDataSource(): CloseableDataSource {
            val container = PostgreSQLContainer("postgres:17-alpine").apply {
                start()
            }

            val hikariDataSource = HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = container.jdbcUrl
                    username = container.username
                    password = container.password
                    maximumPoolSize = 5
                },
            )

            return CloseableDataSource(hikariDataSource, container)
        }
    }

    /**
     * Wraps the [HikariDataSource] and [PostgreSQLContainer] so JUnit's
     * [ExtensionContext.Store.CloseableResource] shuts them down when the
     * test run finishes.
     */
    private class CloseableDataSource(
        val dataSource: HikariDataSource,
        private val container: PostgreSQLContainer,
    ) : ExtensionContext.Store.CloseableResource {
        override fun close() {
            dataSource.close()
            container.close()
        }
    }
}
