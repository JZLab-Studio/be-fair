package dev.jakubzika.befair.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.domain.model.ItemResponse
import dev.jakubzika.befair.domain.model.ItemStats

/** On-device copy of one [ItemResponse]; `stats` is flattened into columns. */
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val kind: ItemKind,
    val name: String,
    val category: String,
    val priceCents: Long,
    val currency: String,
    val purchasedOn: Long,
    val archivedAt: Long?,
    val deletedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val hasStats: Boolean,
    val wearCount: Int,
    val washCount: Int,
    val useCount: Int,
    val maintenanceCents: Long,
    val lastEventAt: Long?,
    val costPerUseCents: Long?,
    val costPerMonthCents: Long?,
)

fun ItemResponse.toEntity() = ItemEntity(
    id = id,
    kind = kind,
    name = name,
    category = category,
    priceCents = priceCents,
    currency = currency,
    purchasedOn = purchasedOn,
    archivedAt = archivedAt,
    deletedAt = deletedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    hasStats = stats != null,
    wearCount = stats?.wearCount ?: 0,
    washCount = stats?.washCount ?: 0,
    useCount = stats?.useCount ?: 0,
    maintenanceCents = stats?.maintenanceCents ?: 0,
    lastEventAt = stats?.lastEventAt,
    costPerUseCents = stats?.costPerUseCents,
    costPerMonthCents = stats?.costPerMonthCents,
)

fun ItemEntity.toModel() = ItemResponse(
    id = id,
    kind = kind,
    name = name,
    category = category,
    priceCents = priceCents,
    currency = currency,
    purchasedOn = purchasedOn,
    archivedAt = archivedAt,
    deletedAt = deletedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    stats = if (hasStats) {
        ItemStats(
            wearCount = wearCount,
            washCount = washCount,
            useCount = useCount,
            maintenanceCents = maintenanceCents,
            lastEventAt = lastEventAt,
            costPerUseCents = costPerUseCents,
            costPerMonthCents = costPerMonthCents,
        )
    } else {
        null
    },
)
