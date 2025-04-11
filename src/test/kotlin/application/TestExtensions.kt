package application

import customer.Customer
import java.time.LocalDate
import java.util.*

fun Customer.Companion.valid() = Customer(
    id = UUID.randomUUID(),
    name = "Test Customer",
    active = true
)

// TODO Show how you can have helper parameters here that offloads work that would be hard to do with copy
fun Application.Companion.valid(
    customerId: UUID
) = Application(
    id = UUID.randomUUID(),
    customerId = customerId,
    name = "Tester One",
    birthDate = LocalDate.of(1978, 2, 23),
    applicationDate = LocalDate.now(),
    status = ApplicationStatus.ACTIVE
)
