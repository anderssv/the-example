package application

import application.Application
import application.ApplicationStatus
import java.time.LocalDate
import java.util.*

fun Application.Companion.valid(addToMonth: Long = 0) = Application(
    id = UUID.randomUUID(),
    name = "Tester One",
    birthDate = LocalDate.of(1978, 2, 23),
    applicationDate = LocalDate.of(2022, 2, 15).plusMonths(addToMonth),
    status = ApplicationStatus.ACTIVE
)