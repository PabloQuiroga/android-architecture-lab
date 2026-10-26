package com.architecturelab.taskapp.domain.time

import java.time.Instant
import java.time.ZoneId

interface TimeProvider {
    fun now(): Instant

    fun timeZone(): ZoneId
}

class SystemTimeProvider(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : TimeProvider {
    override fun now(): Instant = Instant.now()

    override fun timeZone(): ZoneId = zoneId
}

class FixedTimeProvider(
    private val fixedInstant: Instant,
    private val zoneId: ZoneId = ZoneId.of("UTC"),
) : TimeProvider {
    override fun now(): Instant = fixedInstant

    override fun timeZone(): ZoneId = zoneId
}
