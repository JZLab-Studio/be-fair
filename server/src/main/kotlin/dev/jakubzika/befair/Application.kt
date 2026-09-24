package dev.jakubzika.befair

import dev.jakubzika.befair.auth.JwtConfig
import dev.jakubzika.befair.data.db.DatabaseFactory
import dev.jakubzika.befair.data.db.ItemRepository
import dev.jakubzika.befair.data.db.UserRepository
import dev.jakubzika.befair.domain.model.GenericResponse
import dev.jakubzika.befair.routes.authRoutes
import dev.jakubzika.befair.routes.itemRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    val userRepository = UserRepository()
    val itemRepository = ItemRepository()

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.REALM
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (JwtConfig.isAccessToken(credential.payload) && JwtConfig.userIdClaim(credential.payload) != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    install(StatusPages) {
        // Ktor wraps deserialization failures in BadRequestException; without these handlers the
        // catch-all below would turn every malformed body into a 500.
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, GenericResponse(false, "Malformed request body."))
        }
        // Raised when no converter matched, i.e. the Content-Type is missing or not JSON. It is an
        // IOException, not a BadRequestException, so it needs its own branch.
        exception<ContentTransformationException> { call, _ ->
            call.respond(
                HttpStatusCode.UnsupportedMediaType,
                GenericResponse(false, "Content-Type must be application/json."),
            )
        }
        exception<Throwable> { call, cause ->
            // Log the detail instead of echoing it -- messages can carry SQL/driver internals.
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, GenericResponse(false, "Unexpected server error."))
        }
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        authRoutes(userRepository)
        itemRoutes(itemRepository)
    }
}
