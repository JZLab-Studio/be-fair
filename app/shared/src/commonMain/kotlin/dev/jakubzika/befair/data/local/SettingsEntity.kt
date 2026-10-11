package dev.jakubzika.befair.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.jakubzika.befair.domain.model.SettingsResponse
import dev.jakubzika.befair.domain.model.ToolBasis

/** Id of the only row in `settings`; the table holds the signed-in user's settings. */
const val SETTINGS_ROW_ID = 0

/**
 * On-device copy of the user's [SettingsResponse]. [dirty] marks local edits the server has not
 * accepted yet, so a later download must not overwrite them.
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = SETTINGS_ROW_ID,
    val currency: String,
    val toolBasis: ToolBasis,
    val dirty: Boolean = false,
)

fun SettingsResponse.toEntity(dirty: Boolean = false) =
    SettingsEntity(currency = currency, toolBasis = toolBasis, dirty = dirty)

fun SettingsEntity.toModel() = SettingsResponse(currency = currency, toolBasis = toolBasis)
