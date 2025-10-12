package application

import customer.Customer
import java.time.LocalDate
import java.util.*

fun Customer.Companion.valid() = Customer(
    id = UUID.randomUUID(),
    name = "Test Customer",
    active = true
)

/**
 * Helper parameters offload work that would be hard to do with copy.
 * For example, setting monthsOld would require calling .copy() with LocalDate.now().minusMonths(monthsOld),
 * which is verbose and less clear than passing monthsOld directly.
 *
 * Simple variations like status are better handled with .copy(status = ApplicationStatus.DENIED)
 */
fun Application.Companion.valid(
    customerId: UUID,
    monthsOld: Long = 0  // Helper: Sets applicationDate in the past, useful for expiration testing
) = Application(
    id = UUID.randomUUID(),
    customerId = customerId,
    name = "Tester One",
    birthDate = LocalDate.of(1978, 2, 23),
    applicationDate = LocalDate.now().minusMonths(monthsOld),
    status = ApplicationStatus.ACTIVE
)
