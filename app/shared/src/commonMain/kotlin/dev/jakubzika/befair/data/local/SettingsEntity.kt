package dev.jakubzika.befair.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.jakubzika.befair.domain.model.AppSettings
import dev.jakubzika.befair.domain.model.CurrencyChoice
import dev.jakubzika.befair.domain.model.CustomCurrencyMaxLength
import dev.jakubzika.befair.domain.model.ToolBasis
import dev.jakubzika.befair.domain.model.UserSettingsDto

/** The signed-in user's settings: a single row, so [id] is always [SETTINGS_ROW_ID]. */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = SETTINGS_ROW_ID,
    val currency: String,
    val customCurrency: String,
    val toolBasis: String,
    /** True while a local edit has not been accepted by the server yet. */
    val dirty: Boolean,
)

const val SETTINGS_ROW_ID = 0

// Unrecognised stored names fall back to the defaults instead of crashing.
fun SettingsEntity.toModel(): AppSettings {
    val defaults = AppSettings()
    return AppSettings(
        currency = CurrencyChoice.entries.firstOrNull { it.name == currency } ?: defaults.currency,
        customCurrency = customCurrency.take(CustomCurrencyMaxLength),
        toolBasis = ToolBasis.entries.firstOrNull { it.name == toolBasis } ?: defaults.toolBasis,
    )
}

fun AppSettings.toEntity(dirty: Boolean) = SettingsEntity(
    currency = currency.name,
    customCurrency = customCurrency.take(CustomCurrencyMaxLength),
    toolBasis = toolBasis.name,
    dirty = dirty,
)

fun AppSettings.toDto() = UserSettingsDto(currency, customCurrency.trim(), toolBasis)

fun UserSettingsDto.toModel() = AppSettings(currency, customCurrency, toolBasis)
