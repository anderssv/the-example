package application

import java.util.UUID
import javax.sql.DataSource

interface ApplicationRepository {
    fun getApplicationsForName(name: String): List<Application>

    fun getAllApplications(statuses: List<ApplicationStatus>): List<Application>

    fun addApplication(application: Application)

    fun updateApplication(application: Application)

    fun getApplication(applicationId: UUID): Application
}

/**
 * JDBC-backed implementation of [ApplicationRepository].
 *
 * Requires a [DataSource] and creates the necessary table on initialization.
 */
class ApplicationRepositoryImpl(
    private val dataSource: DataSource,
) : ApplicationRepository {
    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS applications (
                        id UUID PRIMARY KEY,
                        customer_id UUID NOT NULL,
                        name VARCHAR(255) NOT NULL,
                        birth_date DATE NOT NULL,
                        application_date DATE NOT NULL,
                        status VARCHAR(50) NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }
    }

    override fun addApplication(application: Application) {
        dataSource.connection.use { conn ->
            conn
                .prepareStatement(
                    "INSERT INTO applications (id, customer_id, name, birth_date, application_date, status) VALUES (?, ?, ?, ?, ?, ?)",
                ).use { stmt ->
                    stmt.setObject(1, application.id)
                    stmt.setObject(2, application.customerId)
                    stmt.setString(3, application.name)
                    stmt.setObject(4, application.birthDate)
                    stmt.setObject(5, application.applicationDate)
                    stmt.setString(6, application.status.name)
                    stmt.executeUpdate()
                }
        }
    }

    override fun getApplicationsForName(name: String): List<Application> =
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM applications WHERE name = ?").use { stmt ->
                stmt.setString(1, name)
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(rs.toApplication())
                        }
                    }
                }
            }
        }

    override fun getAllApplications(statuses: List<ApplicationStatus>): List<Application> {
        val placeholders = statuses.joinToString(",") { "?" }
        return dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM applications WHERE status IN ($placeholders)").use { stmt ->
                statuses.forEachIndexed { index, status ->
                    stmt.setString(index + 1, status.name)
                }
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(rs.toApplication())
                        }
                    }
                }
            }
        }
    }

    override fun updateApplication(application: Application) {
        dataSource.connection.use { conn ->
            conn
                .prepareStatement(
                    "UPDATE applications SET customer_id = ?, name = ?, birth_date = ?, application_date = ?, status = ? WHERE id = ?",
                ).use { stmt ->
                    stmt.setObject(1, application.customerId)
                    stmt.setString(2, application.name)
                    stmt.setObject(3, application.birthDate)
                    stmt.setObject(4, application.applicationDate)
                    stmt.setString(5, application.status.name)
                    stmt.setObject(6, application.id)
                    stmt.executeUpdate()
                }
        }
    }

    override fun getApplication(applicationId: UUID): Application =
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM applications WHERE id = ?").use { stmt ->
                stmt.setObject(1, applicationId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        rs.toApplication()
                    } else {
                        throw NoSuchElementException("Application not found: $applicationId")
                    }
                }
            }
        }
}

private fun java.sql.ResultSet.toApplication() =
    Application(
        id = getObject("id", UUID::class.java),
        customerId = getObject("customer_id", UUID::class.java),
        name = getString("name"),
        birthDate = getDate("birth_date").toLocalDate(),
        applicationDate = getDate("application_date").toLocalDate(),
        status = ApplicationStatus.valueOf(getString("status")),
    )
