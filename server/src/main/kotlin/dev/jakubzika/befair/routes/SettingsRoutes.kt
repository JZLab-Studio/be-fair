package dev.jakubzika.befair.routes

import dev.jakubzika.befair.auth.JwtConfig
import dev.jakubzika.befair.data.db.UserSettingsRepository
import dev.jakubzika.befair.domain.model.CustomCurrencyMaxLength
import dev.jakubzika.befair.domain.model.GenericResponse
import dev.jakubzika.befair.domain.model.UserSettingsDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
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
