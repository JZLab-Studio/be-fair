package dev.jakubzika.befair.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Fixed "€cents/100" formatting — currency is EUR-only for now (see ItemModels.kt),
// so a locale-aware NumberFormat isn't needed and isn't available in commonMain.
fun formatEuroCents(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val absCents = if (cents < 0) -cents else cents
    val whole = absCents / 100
    val fraction = (absCents % 100).toString().padStart(2, '0')
    return "$sign€$whole.$fraction"
}

/** Whole-euro variant for totals such as the price paid ("€280"), rounded half up. */
fun formatEuroWhole(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val absCents = if (cents < 0) -cents else cents
    return "$sign€${(absCents + 50) / 100}"
}

// Abbreviations are fixed rather than locale-aware: kotlinx-datetime has no month-name formatting.
private val monthAbbreviations = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

/** Renders an epoch-day (see ItemModels.kt) as "MMM yyyy". */
fun formatMonthYear(epochDay: Long): String {
    val date = LocalDate.fromEpochDays(epochDay)
    return "${monthAbbreviations[date.month.ordinal]} ${date.year}"
}

/** Renders an epoch-day as "d MMM yyyy". */
fun formatDate(epochDay: Long): String = formatDate(LocalDate.fromEpochDays(epochDay))

/** Renders an epoch-millis timestamp as "d MMM yyyy" in the device's time zone. */
@OptIn(ExperimentalTime::class)
fun formatDateFromMillis(epochMillis: Long): String =
    formatDate(Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault()).date)

private fun formatDate(date: LocalDate): String =
    "${date.day} ${monthAbbreviations[date.month.ordinal]} ${date.year}"
