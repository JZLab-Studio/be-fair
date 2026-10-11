package dev.jakubzika.befair.data.repository

import dev.jakubzika.befair.data.local.SettingsDao
import dev.jakubzika.befair.data.local.toEntity
import dev.jakubzika.befair.data.local.toModel
import dev.jakubzika.befair.data.network.API_BASE_URL
import dev.jakubzika.befair.data.network.safeCall
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.SettingsResponse
import dev.jakubzika.befair.domain.model.UpdateSettingsRequest
import dev.jakubzika.befair.domain.repository.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Talks to the server's `/api/settings` endpoints (behind `auth-jwt`, so [client] must be the
 * auth-configured client). The Room row ([dao]) is what the UI observes; it survives process death,
 * so settings are available offline.
 */
class SettingsRepositoryImpl(
    private val client: HttpClient,
    private val dao: SettingsDao,
    scope: CoroutineScope,
) : SettingsRepository {

    override val settings: StateFlow<SettingsResponse> = dao.observe()
        .map { it?.toModel() ?: SettingsResponse() }
        .stateIn(scope, SharingStarted.Eagerly, SettingsResponse())

    override suspend fun sync(): AppResult<Unit> = safeCall {
        val local = dao.get()
        if (local?.dirty == true) {
            push(local.toModel())
        } else {
            val remote: SettingsResponse = client.get("$API_BASE_URL/api/settings").body()
            dao.upsert(remote.toEntity())
        }
    }

    override suspend fun update(settings: SettingsResponse) {
        dao.upsert(settings.toEntity(dirty = true))
    }

    override suspend fun pushPending(): AppResult<Unit> = safeCall {
        val local = dao.get()
        if (local?.dirty == true) push(local.toModel())
    }

    override suspend fun clear() {
        dao.clear()
    }

    private suspend fun push(settings: SettingsResponse) {
        val saved: SettingsResponse = client.put("$API_BASE_URL/api/settings") {
            contentType(ContentType.Application.Json)
            setBody(UpdateSettingsRequest(settings.currency, settings.toolBasis))
        }.body()
        // An edit made while the request was in flight stays pending rather than being overwritten.
        if (dao.get()?.toModel() == settings) dao.upsert(saved.toEntity())
    }
}
