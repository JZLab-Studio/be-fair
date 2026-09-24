package dev.jakubzika.befair.data.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

/**
 * Exposed table definition for item usage events (wear/wash/use/repair).
 *
 * One table with a `type` discriminator covers all event kinds so cost-per-use and
 * cost-per-month can be derived without per-kind counter columns on [Items].
 */
object ItemEvents : Table("item_events") {
    val id = varchar("id", 36)
    val itemId = varchar("item_id", 36).references(Items.id, onDelete = ReferenceOption.CASCADE)
    val type = varchar("type", 16)
    val occurredAt = long("occurred_at")
    val costCents = long("cost_cents").nullable()
    val note = varchar("note", 255).nullable()
    val createdAt = long("created_at")
    val deletedAt = long("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, itemId, occurredAt)
    }
}

/** Plain row carrier so query results can leave a transaction safely. */
data class ItemEventRow(
    val id: String,
    val itemId: String,
    val type: String,
    val occurredAt: Long,
    val costCents: Long?,
    val note: String?,
    val createdAt: Long,
    val deletedAt: Long?,
)
