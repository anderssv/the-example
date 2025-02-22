package system

import java.time.*

class TestClock private constructor(private var dateTime: ZonedDateTime) : Clock() {
    companion object {
        fun at(dateTime: ZonedDateTime): TestClock = TestClock(dateTime)
        fun now(): TestClock = at(ZonedDateTime.now())
    }

    override fun instant(): Instant = dateTime.toInstant()
    override fun withZone(zone: ZoneId?): Clock = TestClock(dateTime.withZoneSameInstant(zone ?: ZoneId.systemDefault()))
    override fun getZone(): ZoneId = dateTime.zone

    fun advance(duration: Duration) {
        dateTime = dateTime.plus(duration)
    }

    fun setTo(newDateTime: ZonedDateTime) {
        dateTime = newDateTime
    }

    fun setTo(localDate: LocalDate) {
        dateTime = localDate.atStartOfDay(dateTime.zone)
    }
}