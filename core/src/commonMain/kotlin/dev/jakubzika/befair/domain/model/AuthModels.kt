package dev.jakubzika.befair.domain.model

import kotlinx.serialization.Serializable

/**
 * Shared authentication DTOs used by both the Ktor server and the multiplatform clients.
 * Lives in :core so the exact same wire format is reused on both ends.
 */

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
)

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class GenericResponse(
    val success: Boolean,
    val message: String,
    val fields: Map<String, String>? = null,
)

/**
 * Payload returned by the protected GET /api/profile endpoint. Used to prove that the
 * client injects the Bearer token automatically.
 */
@Serializable
data class ProfileResponse(
    val userId: String,
    val email: String,
    val displayName: String,
)
