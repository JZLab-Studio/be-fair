package dev.jakubzika.befair.routes

import dev.jakubzika.befair.auth.JwtConfig
import dev.jakubzika.befair.data.db.UserSettingsRepository
import dev.jakubzika.befair.domain.model.CustomCurrencyMaxLength
import dev.jakubzika.befair.domain.model.GenericResponse
import dev.jakubzika.befair.domain.model.UserSettingsDto
import dev.jakubzika.befair.PROFILE_PICTURE_MAX_BYTES
import dev.jakubzika.befair.data.db.ProfilePicture
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.contentLength
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.delete
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * Mounts the user-settings endpoints under /api/settings. Both require a valid access token;
 * the owner is always the JWT `userId` claim.
 */
fun Route.settingsRoutes(repo: UserSettingsRepository) {
    authenticate("auth-jwt") {
        route("/api/settings") {
            get {
                val userId = call.settingsUserId() ?: return@get call.respondUnknownUser()
                call.respond(repo.find(userId))
            }
            profilePictureRoutes(repo)
            put {
                val userId = call.settingsUserId() ?: return@put call.respondUnknownUser()
                val request = call.receive<UserSettingsDto>()
                val settings = request.copy(customCurrency = request.customCurrency.trim())
                if (settings.customCurrency.length > CustomCurrencyMaxLength) {
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(
                            false,
                            "Validation failed.",
                            mapOf("customCurrency" to "Custom currency must be at most $CustomCurrencyMaxLength characters."),
                        ),
                    )
                }
                repo.save(userId, settings)
                call.respond(settings)
            }
        }
    }
}

private fun ApplicationCall.settingsUserId(): Int? =
    principal<JWTPrincipal>()?.let { JwtConfig.userIdClaim(it.payload) }?.toIntOrNull()

private suspend fun ApplicationCall.respondUnknownUser() =
    respond(HttpStatusCode.Unauthorized, GenericResponse(false, "Unknown user."))

private val JpegMagic = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
private val PngMagic = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

/** Content type from the file signature (never from the client's header), or null if unsupported. */
internal fun sniffImageType(bytes: ByteArray): String? = when {
    bytes.startsWith(JpegMagic) -> "image/jpeg"
    bytes.startsWith(PngMagic) -> "image/png"
    else -> null
}

private fun ByteArray.startsWith(prefix: ByteArray) =
    size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

/** `/api/settings/profile-picture`: raw JPEG/PNG bytes in, raw bytes out. Mounted inside the authenticated block. */
private fun Route.profilePictureRoutes(repo: UserSettingsRepository) {
    route("/profile-picture") {
        get {
            val userId = call.settingsUserId() ?: return@get call.respondUnknownUser()
            val picture = repo.findProfilePicture(userId)
                ?: return@get call.respond(HttpStatusCode.NotFound, GenericResponse(false, "No profile picture."))
            call.respondBytes(picture.bytes, ContentType.parse(picture.contentType))
        }
        put {
            val userId = call.settingsUserId() ?: return@put call.respondUnknownUser()
            val declared = call.request.contentLength()
            if (declared != null && declared > PROFILE_PICTURE_MAX_BYTES) {
                return@put call.respondPictureError(HttpStatusCode.PayloadTooLarge, "Picture is too large.")
            }
            // Bounded read: never buffer more than the cap + 1 byte, even for chunked bodies.
            val bytes = call.receiveBoundedBytes(PROFILE_PICTURE_MAX_BYTES)
            if (bytes.size > PROFILE_PICTURE_MAX_BYTES) {
                return@put call.respondPictureError(HttpStatusCode.PayloadTooLarge, "Picture is too large.")
            }
            val type = sniffImageType(bytes)
                ?: return@put call.respondPictureError(
                    HttpStatusCode.UnsupportedMediaType, "Picture must start with a JPEG or PNG signature.",
                )
            repo.saveProfilePicture(userId, ProfilePicture(bytes, type))
            call.respond(HttpStatusCode.NoContent)
        }
        delete {
            val userId = call.settingsUserId() ?: return@delete call.respondUnknownUser()
            repo.deleteProfilePicture(userId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

/**
 * Reads at most [limit] + 1 bytes of the request body (so a result larger than [limit] signals
 * an oversized body) and cancels the rest of the input instead of buffering it.
 */
private suspend fun ApplicationCall.receiveBoundedBytes(limit: Int): ByteArray {
    val channel = receiveChannel()
    val bytes = channel.readRemaining(limit.toLong() + 1).readByteArray()
    if (bytes.size > limit) channel.cancel(null)
    return bytes
}

private suspend fun ApplicationCall.respondPictureError(status: HttpStatusCode, message: String) =
    respond(status, GenericResponse(false, message))
