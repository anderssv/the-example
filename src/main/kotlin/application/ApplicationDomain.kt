package application

import java.time.LocalDate
import java.util.*

enum class ApplicationStatus {
    ACTIVE,
    APPROVED,
    DENIED,
    EXPIRED
}

/**
 * Because this is a Data Class it is easy to use .copy(name = "Something") to create local
 * variations of test data.
 */
data class Application(val id: UUID, val name: String, val birthDate: LocalDate, val applicationDate: LocalDate, val status: ApplicationStatus) {
    companion object
}
