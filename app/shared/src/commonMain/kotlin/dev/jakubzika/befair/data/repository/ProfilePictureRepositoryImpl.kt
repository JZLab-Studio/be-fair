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

    override suspend fun refresh(): AppResult<Unit> = safeCall {
        _picture.value = try {
            client.get("$API_BASE_URL/api/settings/profile-picture").body<ByteArray>()
        } catch (e: ResponseException) {
            if (e.response.status != HttpStatusCode.NotFound) throw e
            null
        }
    }

    override suspend fun upload(bytes: ByteArray): AppResult<Unit> {
        if (bytes.isEmpty() || bytes.size > PROFILE_PICTURE_MAX_BYTES) {
            return AppResult.Error("The picture is too large.")
        }
        return safeCall {
            client.put("$API_BASE_URL/api/settings/profile-picture") {
                // The server sniffs the real type from the bytes; the header only labels the body.
                contentType(ContentType.Application.OctetStream)
                setBody(bytes)
            }
            _picture.value = bytes
        }
    }

    override fun clear() {
        _picture.value = null
    }
}
