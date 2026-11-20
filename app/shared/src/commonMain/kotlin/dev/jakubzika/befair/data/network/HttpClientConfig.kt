package dev.jakubzika.befair.data.network

import dev.jakubzika.befair.config.AppConfig
import dev.jakubzika.befair.config.consoleLog
import dev.jakubzika.befair.data.storage.TokenStorage
import dev.jakubzika.befair.domain.model.RefreshRequest
import dev.jakubzika.befair.domain.model.TokenResponse
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * Mobile client auth configuration, applied on top of core's [createHttpClient] base
 * (which already sets expectSuccess + timeouts). Installs:
 *  - JSON content negotiation
 *  - Bearer auth that loads tokens from [tokenStorage] and transparently refreshes a 401
 *    via POST /api/auth/refresh, persisting the rotated pair.
 *    When the server rejects the refresh token, tokens are cleared and [onSessionExpired] is invoked
 */
fun HttpClientConfig<*>.configureBeFair(
    tokenStorage: TokenStorage,
    onSessionExpired: () -> Unit = {},
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
                } catch (e: ClientRequestException) {
                    // The server rejected the refresh token: the session is over.
                    tokenStorage.clearTokens()
                    onSessionExpired()
                    null
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Network/server trouble: keep the tokens so a later request can retry.
                    null
                }
            }
        }
    }
}

/**
 * Installs request/response logging in DEV only. Level is HEADERS so bodies (passwords, OTP codes)
 * are never logged, and the Authorization header value is redacted.
 */
fun HttpClientConfig<*>.configureLogging(
    isDev: Boolean = AppConfig.isDev,
    sink: (String) -> Unit = ::consoleLog,
) {
    if (!isDev) return
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) = sink(message)
        }
        level = LogLevel.HEADERS
        sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
    }
}
