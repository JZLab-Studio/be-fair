package dev.jakubzika.befair.data.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

/**
 * One settings row per user; absent until the user first saves, in which case defaults apply.
 * Rows are removed together with the user.
 */
object UserSettings : Table("user_settings") {
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val currency = varchar("currency", 3)
    val toolBasis = varchar("tool_basis", 20)
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(userId)
}
