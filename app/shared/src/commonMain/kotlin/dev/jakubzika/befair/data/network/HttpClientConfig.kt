package dev.jakubzika.befair.data.network

import dev.jakubzika.befair.data.storage.TokenStorage
import dev.jakubzika.befair.domain.model.RefreshRequest
import dev.jakubzika.befair.domain.model.TokenResponse
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * Mobile client auth configuration, applied on top of core's [createHttpClient] base
 * (which already sets expectSuccess + timeouts). Installs:
 *  - JSON content negotiation
 *  - Bearer auth that loads tokens from [tokenStorage] and transparently refreshes a 401
 *    via POST /api/auth/refresh, persisting the rotated pair
 */
fun HttpClientConfig<*>.configureBeFair(
    tokenStorage: TokenStorage,
    // Invoked when the refresh call fails: the session is gone and all user data must be wiped.
    onSessionExpired: suspend () -> Unit = {},
) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            },
        )
    }

    install(Auth) {
        bearer {
            loadTokens {
                val access = tokenStorage.getAccessToken()
                val refresh = tokenStorage.getRefreshToken()
                if (access != null && refresh != null) {
                    BearerTokens(access, refresh)
                } else {
                    null
                }
            }
            refreshTokens {
                val refresh = tokenStorage.getRefreshToken() ?: return@refreshTokens null
                try {
                    val response: TokenResponse = client.post("$API_BASE_URL/api/auth/refresh") {
                        markAsRefreshTokenRequest()
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequest(refresh))
                    }.body()
                    tokenStorage.saveTokens(response.accessToken, response.refreshToken)
                    BearerTokens(response.accessToken, response.refreshToken)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: ResponseException) {
                    // Server rejected the refresh token: the session is over.
                    tokenStorage.clearTokens()
                    onSessionExpired()
                    null
                } catch (_: Exception) {
                    // Transient failure (e.g. offline): keep the session and data, fail this request only.
                    null
                }
            }
        }
    }
}
