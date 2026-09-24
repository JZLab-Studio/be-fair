package dev.jakubzika.befair.data.db

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

/**
 * Exposed table definition for tracked items (clothing/tools).
 *
 * `id` is a client-generated UUID string rather than an autoIncrement column so a retried
 * create after a flaky network never duplicates the row. `category` is a free-text column
 * for this ticket; a `category_id` FK to a future `categories` table is out of scope.
 */
object Items : Table("items") {
    val id = varchar("id", 36)
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val kind = varchar("kind", 16)
    val name = varchar("name", 120)
    val category = varchar("category", 60)
    val priceCents = long("price_cents")
    val currency = varchar("currency", 3).default("EUR")
    val purchasedOn = long("purchased_on")
    val archivedAt = long("archived_at").nullable()
    val deletedAt = long("deleted_at").nullable()
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, userId)
    }
}

/** Plain row carrier so query results can leave a transaction safely. */
data class ItemRow(
    val id: String,
    val userId: Int,
    val kind: String,
    val name: String,
    val category: String,
    val priceCents: Long,
    val currency: String,
    val purchasedOn: Long,
    val archivedAt: Long?,
    val deletedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
