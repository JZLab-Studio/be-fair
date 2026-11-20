package dev.jakubzika.befair.data.repository

import dev.jakubzika.befair.PROFILE_PICTURE_MAX_BYTES
import dev.jakubzika.befair.data.network.API_BASE_URL
import dev.jakubzika.befair.data.network.safeCall
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.repository.ProfilePictureRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Talks to `/api/settings/profile-picture`; [client] must be the auth-configured one. Kept in memory only. */
class ProfilePictureRepositoryImpl(private val client: HttpClient) : ProfilePictureRepository {

    private val _picture = MutableStateFlow<ByteArray?>(null)
    override val picture: StateFlow<ByteArray?> = _picture.asStateFlow()

    // Bumped by every local mutation (upload, clear). A refresh that started before a mutation
    // is stale by the time it returns and must not overwrite the newer value.
    private var generation = 0

    override suspend fun refresh(): AppResult<Unit> {
        val startedAt = generation
        return safeCall {
            val fetched = try {
                client.get("$API_BASE_URL/api/settings/profile-picture").body<ByteArray>()
            } catch (e: ResponseException) {
                if (e.response.status != HttpStatusCode.NotFound) throw e
                null
            }
            if (generation == startedAt) _picture.value = fetched
        }
    }

    override suspend fun upload(bytes: ByteArray): AppResult<Unit> {
        if (bytes.isEmpty() || bytes.size > PROFILE_PICTURE_MAX_BYTES) {
            return AppResult.Error("Invalid picture size.")
        }
        return safeCall {
            client.put("$API_BASE_URL/api/settings/profile-picture") {
                // The server sniffs the real type from the bytes; the header only labels the body.
                contentType(ContentType.Application.OctetStream)
                setBody(bytes)
            }
            generation++
            _picture.value = bytes
        }
    }

    override fun clear() {
        generation++
        _picture.value = null
    }
}
