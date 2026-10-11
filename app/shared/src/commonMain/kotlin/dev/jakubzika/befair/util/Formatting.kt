package dev.jakubzika.befair.util

import dev.jakubzika.befair.domain.model.AppSettings
import dev.jakubzika.befair.domain.model.CurrencyChoice
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val NonBreakingSpace = ' '

// Fixed "amount/100" formatting — a locale-aware NumberFormat isn't available in commonMain.
// The currency is a label only: EUR/USD are prefixed ("€12.50"), a custom name is suffixed
// after a non-breaking space ("12.50 CHF"). Amounts are never converted.
private fun labelled(sign: String, amount: String, settings: AppSettings): String = when (settings.currency) {
    CurrencyChoice.CUSTOM -> "$sign$amount$NonBreakingSpace${settings.currencyLabel}"
    else -> "$sign${settings.currencyLabel}$amount"
}

fun formatMoneyCents(cents: Long, settings: AppSettings): String {
    val sign = if (cents < 0) "-" else ""
    val absCents = if (cents < 0) -cents else cents
    val whole = absCents / 100
    val fraction = (absCents % 100).toString().padStart(2, '0')
    return labelled(sign, "$whole.$fraction", settings)
}

/** Whole-unit variant for totals such as the price paid ("€280"), rounded half up. */
fun formatMoneyWhole(cents: Long, settings: AppSettings): String {
    val sign = if (cents < 0) "-" else ""
    val absCents = if (cents < 0) -cents else cents
    return labelled(sign, ((absCents + 50) / 100).toString(), settings)
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
