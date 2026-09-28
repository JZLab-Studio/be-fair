package dev.jakubzika.befair.data.repository

import dev.jakubzika.befair.data.local.ItemDao
import dev.jakubzika.befair.data.local.toEntity
import dev.jakubzika.befair.data.local.toModel
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Talks to the server's `/api/items` endpoints. Every route there is behind `auth-jwt`, so
 * [client] must be the auth-configured client from `AppContainer`, not core's bare one.
 *
 * The [items] cache is a Room table ([dao]) that keeps the server's `createdAt ASC` ordering. It
 * survives process death, so the Items screen shows the last known list on a cold start or offline,
 * and every successful server response is written through to it.
 */
class ItemRepositoryImpl(
    private val client: HttpClient,
    private val dao: ItemDao,
    scope: CoroutineScope,
) : ItemRepository {

    override val items: StateFlow<List<ItemResponse>> = dao.observeAll()
        .map { rows -> rows.map { it.toModel() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun refresh(): AppResult<Unit> = safeCall {
        // Without an updatedSince cursor the server already excludes deleted and archived rows.
        val response: ItemListResponse = client.get("$API_BASE_URL/api/items").body()
        dao.replaceAll(response.items.map { it.toEntity() })
    }

    override suspend fun addItem(request: CreateItemRequest): AppResult<ItemResponse> = safeCall {
        val created: ItemResponse = client.post("$API_BASE_URL/api/items") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        // Upsert by id: a retried request replays the stored row rather than creating a duplicate.
        dao.upsert(created.toEntity())
        created
    }

    override suspend fun quickLog(itemId: String, type: ItemEventType): AppResult<ItemResponse> = safeCall {
        val logged: EventLoggedResponse = client.post("$API_BASE_URL/api/items/$itemId/events") {
            contentType(ContentType.Application.Json)
            setBody(LogEventRequest(id = newItemId(), type = type))
        }.body()
        // Replace the row with the server's refreshed stats rather than recomputing them locally.
        dao.upsert(logged.item.toEntity())
        logged.item
    }
}
