package dev.jakubzika.befair.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

/**
 * Central JWT configuration. Secret/issuer/audience are hard-coded dev values — move them
 * to environment configuration before any real deployment.
 *
 * Access tokens expire in 15 minutes; refresh tokens in 30 days. Refresh tokens carry a
 * `type=refresh` claim so they cannot be presented as access tokens to protected routes.
 */
object JwtConfig {

    const val ISSUER = "befair-server"
    const val AUDIENCE = "befair-app"
    const val REALM = "befair"

    private const val SECRET = "dev-secret-change-me-please-0123456789"
    private const val CLAIM_USER_ID = "userId"
    private const val CLAIM_TYPE = "type"
    private const val TYPE_ACCESS = "access"
    private const val TYPE_REFRESH = "refresh"

    private const val ACCESS_VALIDITY_MILLIS = 15 * 60 * 1000L          // 15 minutes
    private const val REFRESH_VALIDITY_MILLIS = 30L * 24 * 60 * 60 * 1000 // 30 days

    private val algorithm = Algorithm.HMAC256(SECRET)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateAccessToken(userId: String): String = JWT.create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim(CLAIM_USER_ID, userId)
        .withClaim(CLAIM_TYPE, TYPE_ACCESS)
        .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_VALIDITY_MILLIS))
        .sign(algorithm)

    fun generateRefreshToken(userId: String): String = JWT.create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim(CLAIM_USER_ID, userId)
        .withClaim(CLAIM_TYPE, TYPE_REFRESH)
        .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_VALIDITY_MILLIS))
        .sign(algorithm)

    /** Reads the userId claim from an already-verified JWT payload. */
    fun userIdClaim(payload: com.auth0.jwt.interfaces.Payload): String? =
        payload.getClaim(CLAIM_USER_ID).asString()

    /** True only for access tokens; refresh tokens must never authenticate protected routes. */
    fun isAccessToken(payload: com.auth0.jwt.interfaces.Payload): Boolean =
        payload.getClaim(CLAIM_TYPE).asString() == TYPE_ACCESS

    /**
     * Verifies a refresh token and returns its userId, or null if the token is invalid,
     * expired, or is not actually a refresh token.
     */
    fun verifyRefreshToken(token: String): String? = try {
        val decoded = verifier.verify(token)
        if (decoded.getClaim(CLAIM_TYPE).asString() == TYPE_REFRESH) {
            decoded.getClaim(CLAIM_USER_ID).asString()
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
