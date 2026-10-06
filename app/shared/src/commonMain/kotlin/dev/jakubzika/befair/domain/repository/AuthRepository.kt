package dev.jakubzika.befair.domain.repository

import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.ProfileResponse

/**
 * Client-side authentication operations. Implementations talk to the Ktor backend and
 * persist tokens securely on success.
 */
interface AuthRepository {

    /** Registers a new account with a display name and triggers an OTP email. Does not log the user in. */
    suspend fun register(name: String, email: String, password: String): AppResult<Unit>

    /** Verifies the emailed OTP; on success persists the returned tokens and caches the profile. */
    suspend fun verifyOtp(email: String, otp: String): AppResult<Unit>

    /** Logs in a verified user; on success persists the returned tokens. */
    suspend fun login(email: String, password: String): AppResult<Unit>

    /**
     * Asks the backend to email a password reset link. Account-enumeration safe: any HTTP
     * response (even an error status) is reported as success; only a network failure is an error.
     */
    suspend fun requestPasswordReset(email: String): AppResult<Unit>

    /** Calls the protected profile endpoint (proves Bearer injection). */
    suspend fun fetchProfile(): AppResult<ProfileResponse>

    /** Clears persisted tokens, profile data and the on-device item cache. */
    suspend fun logout()

    /** Whether a token pair is currently stored. */
    fun isLoggedIn(): Boolean
}
