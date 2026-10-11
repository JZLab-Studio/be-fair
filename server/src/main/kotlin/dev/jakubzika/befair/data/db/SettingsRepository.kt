package dev.jakubzika.befair.data.db

import dev.jakubzika.befair.domain.model.SettingsResponse
import dev.jakubzika.befair.domain.model.ToolBasis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

/** Data-access layer for [UserSettings]; each call runs a blocking transaction on the IO dispatcher. */
class SettingsRepository {

    /** The stored settings, or null if the user never saved any. */
    suspend fun find(userId: Int): SettingsResponse? = dbQuery {
        UserSettings.selectAll()
            .where { UserSettings.userId eq userId }
            .map { row ->
                SettingsResponse(
                    currency = row[UserSettings.currency],
                    toolBasis = ToolBasis.valueOf(row[UserSettings.toolBasis]),
                )
            }
            .singleOrNull()
    }

    suspend fun save(userId: Int, settings: SettingsResponse) {
        dbQuery {
            UserSettings.upsert {
                it[UserSettings.userId] = userId
                it[currency] = settings.currency
                it[toolBasis] = settings.toolBasis.name
                it[updatedAt] = System.currentTimeMillis()
            }
        }
    }

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction { block() } }
}
