package application

import customer.Customer
import java.time.LocalDate
import java.util.*

fun Customer.Companion.valid() = Customer(
    id = UUID.randomUUID(),
    name = "Test Customer",
    active = true
)

fun Application.Companion.valid(
    applicationDate: LocalDate = LocalDate.of(2022, 2, 15),
    customerId: UUID
) = Application(
    id = UUID.randomUUID(),
    customerId = customerId,
    name = "Tester One",
    birthDate = LocalDate.of(1978, 2, 23),
    applicationDate = applicationDate,
    status = ApplicationStatus.ACTIVE
)
