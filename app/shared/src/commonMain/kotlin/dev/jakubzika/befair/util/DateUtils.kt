package dev.jakubzika.befair.util

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.todayIn

/**
 * ISO 8601 (yyyy-MM-dd) and epoch-day helpers. The Add Item screen collects a date as an ISO
 * string, while the server's item API speaks epoch days ([dev.jakubzika.befair.domain.model.CreateItemRequest.purchasedOn]),
 * so both directions are needed. Backed by kotlinx-datetime, which keeps this in `commonMain`
 * instead of the `java.time`-vs-`NSDateFormatter` expect/actual split it replaced.
 */

@OptIn(ExperimentalTime::class)
fun todayIso(): String = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

@OptIn(ExperimentalTime::class)
fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

/** Null when [iso] is not a well-formed yyyy-MM-dd date -- the value is user-typed. */
fun isoToEpochDay(iso: String): Long? =
    runCatching { LocalDate.parse(iso).toEpochDays() }.getOrNull()

/** Whole calendar months from [purchasedOn] (epoch day) until today, never negative. */
@OptIn(ExperimentalTime::class)
fun monthsOwned(purchasedOn: Long): Int =
    LocalDate.fromEpochDays(purchasedOn).monthsUntil(Clock.System.todayIn(TimeZone.currentSystemDefault())).coerceAtLeast(0)

fun epochDayToIso(epochDay: Long): String = LocalDate.fromEpochDays(epochDay).toString()
