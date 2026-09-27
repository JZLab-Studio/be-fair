package dev.jakubzika.befair.domain.repository

import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.CreateItemRequest
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
}
