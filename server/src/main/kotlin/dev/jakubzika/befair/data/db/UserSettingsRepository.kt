package dev.jakubzika.befair.data.db

import dev.jakubzika.befair.domain.model.CurrencyChoice
import dev.jakubzika.befair.domain.model.ToolBasis
import dev.jakubzika.befair.domain.model.UserSettingsDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

/**
 * Data-access layer for [UserSettings]. Each call runs a blocking Exposed transaction on the IO
 * dispatcher so route handlers can stay `suspend`.
 */
class UserSettingsRepository {

    /** The user's saved settings, or the defaults when they never saved any. */
    suspend fun find(userId: Int): UserSettingsDto = dbQuery {
        UserSettings.selectAll()
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

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction { block() } }
}
