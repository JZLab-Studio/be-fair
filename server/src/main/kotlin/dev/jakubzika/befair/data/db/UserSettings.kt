package dev.jakubzika.befair.data.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

/**
 * One row of settings per user, keyed by `user_id`. A user who never saved has no row; the
 * routes answer with defaults in that case. Rows are deleted with the user.
 */
object UserSettings : Table("user_settings") {
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val currency = varchar("currency", 8)
    val customCurrency = varchar("custom_currency", 12)
    val toolBasis = varchar("tool_basis", 8)
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(userId)
}
