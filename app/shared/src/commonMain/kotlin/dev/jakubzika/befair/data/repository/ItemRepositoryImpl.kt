package dev.jakubzika.befair.data.repository

import dev.jakubzika.befair.data.network.API_BASE_URL
import dev.jakubzika.befair.data.network.safeCall
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.CreateItemRequest
import dev.jakubzika.befair.domain.model.EventLoggedResponse
import dev.jakubzika.befair.domain.model.ItemEventType
import dev.jakubzika.befair.domain.model.ItemListResponse
import dev.jakubzika.befair.domain.model.ItemResponse
import dev.jakubzika.befair.domain.model.LogEventRequest
import dev.jakubzika.befair.domain.repository.ItemRepository
import dev.jakubzika.befair.util.newItemId
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Talks to the server's `/api/items` endpoints. Every route there is behind `auth-jwt`, so
 * [client] must be the auth-configured client from `AppContainer`, not core's bare one.
 *
 * The [items] cache keeps the server's `createdAt ASC` ordering and survives Android Activity
 * re-creation, so a newly created item shows up on the Items screen without a refetch.
 */
class ItemRepositoryImpl(private val client: HttpClient) : ItemRepository {

    private val _items = MutableStateFlow<List<ItemResponse>>(emptyList())
    override val items: StateFlow<List<ItemResponse>> = _items.asStateFlow()

    override suspend fun refresh(): AppResult<Unit> = safeCall {
        // Without an updatedSince cursor the server already excludes deleted and archived rows.
        val response: ItemListResponse = client.get("$API_BASE_URL/api/items").body()
        _items.value = response.items
    }

    override suspend fun addItem(request: CreateItemRequest): AppResult<ItemResponse> = safeCall {
        val created: ItemResponse = client.post("$API_BASE_URL/api/items") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        // Merge by id: a retried request replays the stored row rather than creating a duplicate.
        _items.value = _items.value.filterNot { it.id == created.id } + created
        created
    }

    override suspend fun quickLog(itemId: String, type: ItemEventType): AppResult<ItemResponse> = safeCall {
        val logged: EventLoggedResponse = client.post("$API_BASE_URL/api/items/$itemId/events") {
            contentType(ContentType.Application.Json)
            setBody(LogEventRequest(id = newItemId(), type = type))
        }.body()
        // Replace the row with the server's refreshed stats rather than recomputing them locally.
        _items.value = _items.value.map { if (it.id == logged.item.id) logged.item else it }
        logged.item
    }
}
