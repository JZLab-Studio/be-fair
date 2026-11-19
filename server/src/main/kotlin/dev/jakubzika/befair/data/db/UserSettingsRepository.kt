package dev.jakubzika.befair.data.db

import dev.jakubzika.befair.domain.model.CurrencyChoice
import dev.jakubzika.befair.domain.model.ToolBasis
import dev.jakubzika.befair.domain.model.UserSettingsDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

/** An uploaded, already validated profile picture. */
class ProfilePicture(val bytes: ByteArray, val contentType: String)

/**
 * Data-access layer for [UserSettings]. Each call runs a blocking Exposed transaction on the IO
 * dispatcher so route handlers can stay `suspend`.
 */
class UserSettingsRepository {

    /** The user's saved settings, or the defaults when they never saved any. */
    suspend fun find(userId: Int): UserSettingsDto = dbQuery {
        // Project only the settings columns so the picture blob is never read here.
        UserSettings.select(UserSettings.currency, UserSettings.customCurrency, UserSettings.toolBasis)
            .where { UserSettings.userId eq userId }
            .map {
                UserSettingsDto(
                    currency = CurrencyChoice.valueOf(it[UserSettings.currency]),
                    customCurrency = it[UserSettings.customCurrency],
                    toolBasis = ToolBasis.valueOf(it[UserSettings.toolBasis]),
                )
            }
            .singleOrNull()
    } ?: UserSettingsDto()

    /** Replaces the user's settings, creating the row on first save. */
    suspend fun save(userId: Int, settings: UserSettingsDto) {
        dbQuery {
            UserSettings.upsert {
                it[UserSettings.userId] = userId
                it[currency] = settings.currency.name
                it[customCurrency] = settings.customCurrency
                it[toolBasis] = settings.toolBasis.name
                it[updatedAt] = System.currentTimeMillis()
            }
        }
    }

    /** The user's profile picture, or null when none was uploaded. */
    suspend fun findProfilePicture(userId: Int): ProfilePicture? = dbQuery {
        UserSettings.select(UserSettings.profilePicture, UserSettings.profilePictureType)
            .where { UserSettings.userId eq userId }
            .singleOrNull()
            ?.let {
                val bytes = it[UserSettings.profilePicture]
                val type = it[UserSettings.profilePictureType]
                if (bytes == null || type == null) null else ProfilePicture(bytes, type)
            }
    }

    /**
     * Stores the picture, creating the settings row with default settings when the user never
     * saved any. On conflict only the picture columns change, so saved settings stay intact.
     */
    suspend fun saveProfilePicture(userId: Int, picture: ProfilePicture) {
        dbQuery {
            val defaults = UserSettingsDto()
            UserSettings.upsert(
                onUpdateExclude = listOf(
                    UserSettings.currency, UserSettings.customCurrency, UserSettings.toolBasis,
                ),
            ) {
                it[UserSettings.userId] = userId
                it[currency] = defaults.currency.name
                it[customCurrency] = defaults.customCurrency
                it[toolBasis] = defaults.toolBasis.name
                it[updatedAt] = System.currentTimeMillis()
                it[profilePicture] = picture.bytes
                it[profilePictureType] = picture.contentType
            }
        }
    }

    /** Removes the picture; returns false when there was none. */
    suspend fun deleteProfilePicture(userId: Int): Boolean = dbQuery {
        UserSettings.update({ UserSettings.userId eq userId and UserSettings.profilePicture.isNotNull() }) {
            it[profilePicture] = null
            it[profilePictureType] = null
            it[updatedAt] = System.currentTimeMillis()
        } > 0
    }

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction { block() } }
}
