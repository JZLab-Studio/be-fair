package dev.jakubzika.befair.data.repository

import dev.jakubzika.befair.data.network.API_BASE_URL
import dev.jakubzika.befair.data.network.safeCall
import dev.jakubzika.befair.data.storage.TokenStorage
import dev.jakubzika.befair.data.storage.UserProfileStorage
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.GenericResponse
import dev.jakubzika.befair.domain.model.LoginRequest
import dev.jakubzika.befair.domain.model.ProfileResponse
import dev.jakubzika.befair.domain.model.RegisterRequest
import dev.jakubzika.befair.domain.model.TokenResponse
import dev.jakubzika.befair.domain.model.VerifyOtpRequest
import dev.jakubzika.befair.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthRepositoryImpl(
    private val client: HttpClient,
    private val tokenStorage: TokenStorage,
    private val userProfileStorage: UserProfileStorage,
) : AuthRepository {

    override suspend fun register(name: String, email: String, password: String): AppResult<Unit> = safeCall {
        val response: GenericResponse = client.post("$API_BASE_URL/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password, name))
        }.body()
        check(response.success) { response.message }
    }

    override suspend fun verifyOtp(email: String, otp: String): AppResult<Unit> = safeCall {
        val tokens: TokenResponse = client.post("$API_BASE_URL/api/auth/verify") {
            contentType(ContentType.Application.Json)
            setBody(VerifyOtpRequest(email, otp))
        }.body()
        tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
        // Fetch and cache the user profile after successful verification
        val profile = fetchProfileInternal()
        userProfileStorage.saveProfile(profile.displayName, profile.email)
    }

    override suspend fun login(email: String, password: String): AppResult<Unit> = safeCall {
        val tokens: TokenResponse = client.post("$API_BASE_URL/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
        tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
        val profile = fetchProfileInternal()
        userProfileStorage.saveProfile(profile.displayName, profile.email)
    }

    override suspend fun fetchProfile(): AppResult<ProfileResponse> = safeCall {
        fetchProfileInternal()
    }

    override fun logout() {
        tokenStorage.clearTokens()
        userProfileStorage.clearProfile()
    }

    override fun isLoggedIn(): Boolean =
        tokenStorage.getAccessToken() != null && tokenStorage.getRefreshToken() != null

    private suspend fun fetchProfileInternal(): ProfileResponse =
        client.get("$API_BASE_URL/api/profile").body()
}
