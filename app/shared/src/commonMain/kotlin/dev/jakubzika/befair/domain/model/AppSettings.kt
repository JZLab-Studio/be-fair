package dev.jakubzika.befair.domain.model

import dev.jakubzika.befair.util.periodsOwned

/** Which label prices carry. Amounts are never converted — the currency is a label only. */
enum class CurrencyChoice { EUR, USD, CUSTOM }

/** The period a tool's cost is spread over. */
enum class ToolBasis { DAY, WEEK, MONTH, YEAR }

/** Longest custom currency name that is stored. */
const val CustomCurrencyMaxLength = 12

/** Shown in place of a custom currency name while it is blank. */
const val CustomCurrencyFallback = "¤"

/**
 * App-wide display preferences. Mobile-only, so it lives in app/shared rather than `core`.
 * Defaults: euro, no custom name, monthly tool cost.
 */
data class AppSettings(
    val currency: CurrencyChoice = CurrencyChoice.EUR,
    val customCurrency: String = "",
    val toolBasis: ToolBasis = ToolBasis.MONTH,
) {
    /** The symbol or name labelling a price: "€", "$", the custom name, or [CustomCurrencyFallback]. */
    val currencyLabel: String
        get() = when (currency) {
            CurrencyChoice.EUR -> "€"
            CurrencyChoice.USD -> "$"
            CurrencyChoice.CUSTOM -> customCurrency.trim().ifEmpty { CustomCurrencyFallback }
        }
}

/** Cost of one period of ownership: price ÷ [periods], where [periods] is at least 1. */
fun toolCostCents(priceCents: Long, periods: Int): Long = priceCents / periods.coerceAtLeast(1)

/**
 * A tool's cost per [basis] period, or the item's cost per use for clothing; null when there is
 * nothing to show yet (clothing never worn). [todayEpochDay] is injected so the result is testable.
 */
fun ItemResponse.primaryCostCents(basis: ToolBasis, todayEpochDay: Long): Long? = when (kind) {
    ItemKind.CLOTHING -> stats?.costPerUseCents
    ItemKind.TOOL -> toolCostCents(priceCents, periodsOwned(purchasedOn, basis, todayEpochDay))
}
