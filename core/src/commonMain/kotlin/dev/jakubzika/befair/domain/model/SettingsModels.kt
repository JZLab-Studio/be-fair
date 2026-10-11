package dev.jakubzika.befair.domain.model

import kotlinx.serialization.Serializable

/**
 * Per-user preferences DTOs shared by the Ktor server and the multiplatform clients.
 * Lives in :core so the exact same wire format is reused on both ends.
 */

const val DEFAULT_CURRENCY = "EUR"

/** Period over which a tool's cost is averaged. */
@Serializable
enum class ToolBasis { PER_MONTH, PER_YEAR }

/** The user's settings. Also the defaults a fresh account (or a failed first sync) falls back to. */
@Serializable
data class SettingsResponse(
    val currency: String = DEFAULT_CURRENCY,   // ISO 4217, "EUR"
    val toolBasis: ToolBasis = ToolBasis.PER_MONTH,
)

/** Full replacement of the user's settings (PUT semantics). */
@Serializable
data class UpdateSettingsRequest(
    val currency: String,
    val toolBasis: ToolBasis,
)
