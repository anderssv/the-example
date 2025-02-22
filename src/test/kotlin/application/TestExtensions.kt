package application

import java.time.LocalDate
import java.util.*

fun Application.Companion.valid(applicationDate: LocalDate = LocalDate.of(2022, 2, 15)) = Application(
    id = UUID.randomUUID(),
    name = "Tester One",
    birthDate = LocalDate.of(1978, 2, 23),
    applicationDate = applicationDate,
    status = ApplicationStatus.ACTIVE
)