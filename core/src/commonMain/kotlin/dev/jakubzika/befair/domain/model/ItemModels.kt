package dev.jakubzika.befair.domain.model

import kotlinx.serialization.Serializable

/**
 * Shared item-tracking DTOs used by both the Ktor server and the multiplatform clients.
 * Lives in :core so the exact same wire format is reused on both ends.
 */

@Serializable
enum class ItemKind { CLOTHING, TOOL }

@Serializable
enum class ItemEventType { WEAR, WASH, USE, REPAIR }

/** Mirrors one row of the `items` table. `stats` is derived, never stored. */
@Serializable
data class ItemResponse(
    val id: String,
    val kind: ItemKind,
    val name: String,
    val category: String,
    val priceCents: Long,
    val currency: String,          // ISO 4217, "EUR"
    val purchasedOn: Long,         // epoch day
    val archivedAt: Long?,         // epoch millis, null = active
    val deletedAt: Long?,          // only non-null in an updatedSince delta
    val createdAt: Long,
    val updatedAt: Long,
    val stats: ItemStats? = null,  // computed from item_events
)

/** Aggregates over `item_events`; not columns. Null on create (no events yet). */
@Serializable
data class ItemStats(
    val wearCount: Int,
    val washCount: Int,
    val useCount: Int,
    val maintenanceCents: Long,
    val lastEventAt: Long?,
    val costPerUseCents: Long?,     // (price + maintenance) / wearCount|useCount, null while 0
    val costPerMonthCents: Long?,   // tools: price / months since purchasedOn
)

@Serializable
data class CreateItemRequest(
    val id: String,                 // client-generated UUID -> idempotent retry
    val kind: ItemKind,
    val name: String,
    val category: String? = null,   // null/blank -> "Clothing" | "Tool" server-side
    val priceCents: Long,
    val currency: String = "EUR",
    val purchasedOn: Long,
)

/** All fields optional; absent = unchanged. Kind is immutable once set. */
@Serializable
data class UpdateItemRequest(
    val name: String? = null,
    val category: String? = null,
    val priceCents: Long? = null,
    val currency: String? = null,
    val purchasedOn: Long? = null,
)

@Serializable
data class ItemListResponse(
    val items: List<ItemResponse>,
    val serverTime: Long,           // feed back as updatedSince on the next poll
)

@Serializable
data class ItemEventResponse(
    val id: String,
    val itemId: String,
    val type: ItemEventType,
    val occurredAt: Long,
    val costCents: Long?,
    val note: String?,
    val createdAt: Long,
)

@Serializable
data class LogEventRequest(
    val id: String,                 // client-generated UUID -> idempotent retry
    val type: ItemEventType,
    val occurredAt: Long? = null,   // null = now, server clock
    val costCents: Long? = null,
    val note: String? = null,
)

/** Event plus the item carrying refreshed stats, so one tap = one round-trip. */
@Serializable
data class EventLoggedResponse(
    val event: ItemEventResponse,
    val item: ItemResponse,
)

@Serializable
data class ItemEventListResponse(
    val events: List<ItemEventResponse>,
    val serverTime: Long,
)
