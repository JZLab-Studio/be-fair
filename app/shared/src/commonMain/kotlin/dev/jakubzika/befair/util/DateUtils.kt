package dev.jakubzika.befair.util

import dev.jakubzika.befair.domain.model.ToolBasis
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

@OptIn(ExperimentalTime::class)
fun todayEpochDay(): Long = Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochDays()

/** Whole calendar months from [purchasedOn] (epoch day) until [todayEpochDay], never negative. */
fun monthsOwned(purchasedOn: Long, todayEpochDay: Long = todayEpochDay()): Int =
    LocalDate.fromEpochDays(purchasedOn).monthsUntil(LocalDate.fromEpochDays(todayEpochDay)).coerceAtLeast(0)

/**
 * Full [basis] periods owned since [purchasedOn], minimum 1: whole days, whole days ÷ 7,
 * [monthsOwned], or [monthsOwned] ÷ 12.
 */
fun periodsOwned(purchasedOn: Long, basis: ToolBasis, todayEpochDay: Long = todayEpochDay()): Int {
    val days = (todayEpochDay - purchasedOn).toInt().coerceAtLeast(0)
    val periods = when (basis) {
        ToolBasis.DAY -> days
        ToolBasis.WEEK -> days / 7
        ToolBasis.MONTH -> monthsOwned(purchasedOn, todayEpochDay)
        ToolBasis.YEAR -> monthsOwned(purchasedOn, todayEpochDay) / 12
    }
    return periods.coerceAtLeast(1)
}

fun epochDayToIso(epochDay: Long): String = LocalDate.fromEpochDays(epochDay).toString()
