package dev.jakubzika.befair.domain.repository

import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.CreateItemRequest
import dev.jakubzika.befair.domain.model.ItemEventResponse
import dev.jakubzika.befair.domain.model.ItemEventType
import dev.jakubzika.befair.domain.model.ItemResponse
import kotlinx.coroutines.flow.StateFlow

/**
 * Items are held in the core wire DTO [ItemResponse] rather than a separate mobile model, so the
 * same shape flows from the server through the repository into `ItemRow` without a mapping layer.
 */
interface ItemRepository {
    val items: StateFlow<List<ItemResponse>>

    /** Reloads the signed-in user's items from the server into [items]. */
    suspend fun refresh(): AppResult<Unit>

    suspend fun addItem(request: CreateItemRequest): AppResult<ItemResponse>

    /** Logs a [type] event for today against [itemId] (the Items list "+1") and refreshes its stats in [items]. */
    suspend fun quickLog(itemId: String, type: ItemEventType): AppResult<ItemResponse>

    /** The newest [limit] events of [itemId], newest first. Not cached: the history is only shown on item detail. */
    suspend fun events(itemId: String, limit: Int): AppResult<List<ItemEventResponse>>

    /** Removes one logged event and replaces the item in [items] with its recomputed stats. */
    suspend fun deleteEvent(itemId: String, eventId: String): AppResult<ItemResponse>

    /** Deletes the item server-side and drops it from [items]. */
    suspend fun deleteItem(itemId: String): AppResult<Unit>
}
