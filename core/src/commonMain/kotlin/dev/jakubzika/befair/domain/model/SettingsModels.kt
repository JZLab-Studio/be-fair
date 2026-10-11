package dev.jakubzika.befair.domain.model

import kotlinx.serialization.Serializable

/**
 * User-settings DTOs and option enums shared by the Ktor server and the multiplatform clients,
 * so the exact same wire format is used on both ends.
 */

/** Which label prices carry. Amounts are never converted — the currency is a label only. */
@Serializable
enum class CurrencyChoice { EUR, USD, CUSTOM }

/** The period a tool's cost is spread over. */
@Serializable
enum class ToolBasis { DAY, WEEK, MONTH, YEAR }

/** Longest custom currency name that is stored. */
const val CustomCurrencyMaxLength = 12

/** Body of `PUT /api/settings` and response of `GET`/`PUT /api/settings`. Replaces the whole set. */
@Serializable
data class UserSettingsDto(
    val currency: CurrencyChoice = CurrencyChoice.EUR,
    val customCurrency: String = "",
    val toolBasis: ToolBasis = ToolBasis.MONTH,
)
