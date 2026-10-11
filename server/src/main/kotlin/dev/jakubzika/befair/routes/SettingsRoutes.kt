package dev.jakubzika.befair.routes

import dev.jakubzika.befair.auth.JwtConfig
import dev.jakubzika.befair.data.db.SettingsRepository
import dev.jakubzika.befair.domain.model.GenericResponse
import dev.jakubzika.befair.domain.model.SettingsResponse
import dev.jakubzika.befair.domain.model.UpdateSettingsRequest
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
import java.util.Currency

private val currencyPattern = Regex("^[A-Za-z]{3}$")

/** Rejects both malformed codes and well-formed-but-unassigned ones like "ZZZ". */
private fun isValidCurrency(code: String): Boolean =
    currencyPattern.matches(code) && runCatching { Currency.getInstance(code.uppercase()) }.isSuccess

/**
 * Mounts `/api/settings`. Both routes require a valid access token and act on the JWT's user only.
 * GET returns defaults for a user who never saved; PUT replaces the whole settings object.
 */
fun Route.settingsRoutes(settingsRepository: SettingsRepository) {
    authenticate("auth-jwt") {
        route("/api/settings") {
            get {
                val userId = call.settingsUserId() ?: return@get call.respondUnknownUser()
                call.respond(settingsRepository.find(userId) ?: SettingsResponse())
            }
            put {
                val userId = call.settingsUserId() ?: return@put call.respondUnknownUser()
                val request = call.receive<UpdateSettingsRequest>()
                if (!isValidCurrency(request.currency)) {
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        GenericResponse(false, "Currency must be a valid ISO 4217 code."),
                    )
                }
                val saved = SettingsResponse(request.currency.uppercase(), request.toolBasis)
                settingsRepository.save(userId, saved)
                call.respond(saved)
            }
        }
    }
}

private fun ApplicationCall.settingsUserId(): Int? =
    principal<JWTPrincipal>()?.let { JwtConfig.userIdClaim(it.payload) }?.toIntOrNull()

private suspend fun ApplicationCall.respondUnknownUser() =
    respond(HttpStatusCode.Unauthorized, GenericResponse(false, "Unknown user."))
