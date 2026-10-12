package dev.jakubzika.befair.data.repository

import dev.jakubzika.befair.data.local.SettingsDao
import dev.jakubzika.befair.data.local.toDto
import dev.jakubzika.befair.data.local.toEntity
import dev.jakubzika.befair.data.local.toModel
import dev.jakubzika.befair.data.network.API_BASE_URL
import dev.jakubzika.befair.data.network.safeCall
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.AppSettings
import dev.jakubzika.befair.domain.model.CustomCurrencyMaxLength
import dev.jakubzika.befair.domain.model.UserSettingsDto
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Settings live in a Room row ([dao]) so they work offline, and are mirrored to the server's
 * `/api/settings` ([client] must be the auth-configured one). Local edits are flagged `dirty`
 * until the server accepts them, so a download never overwrites an edit that has not been
 * uploaded yet.
 */
class SettingsRepositoryImpl(
    private val client: HttpClient,
    private val dao: SettingsDao,
    private val scope: CoroutineScope,
) : SettingsRepository {

    // Uploads and downloads never overlap.
    private val syncLock = Mutex()

    override val settings: StateFlow<AppSettings> = dao.observe()
        .map { it?.toModel() ?: AppSettings() }
        .stateIn(scope, SharingStarted.Eagerly, AppSettings())

    override suspend fun update(transform: (AppSettings) -> AppSettings) = syncLock.withLock {
        val current = dao.get()?.toModel() ?: AppSettings()
        val next = transform(current).let {
            it.copy(customCurrency = it.customCurrency.take(CustomCurrencyMaxLength))
        }
        dao.upsert(next.toEntity(dirty = true))
    }

    override suspend fun sync(): AppResult<Unit> = syncLock.withLock {
        if (dao.get()?.dirty == true) push() else pull()
    }

    override fun pushPending() {
        scope.launch {
            syncLock.withLock {
                if (dao.get()?.dirty == true) push()
            }
        }
    }

    private suspend fun pull(): AppResult<Unit> = safeCall {
        val downloaded: UserSettingsDto = client.get("$API_BASE_URL/api/settings").body()
        dao.replaceIfClean(downloaded.toModel().toEntity(dirty = false))
    }

    private suspend fun push(): AppResult<Unit> = safeCall {
        val pending = dao.get() ?: return@safeCall
        client.put("$API_BASE_URL/api/settings") {
            contentType(ContentType.Application.Json)
            setBody(pending.toModel().toDto())
        }
        dao.markClean(pending)
    }
}
