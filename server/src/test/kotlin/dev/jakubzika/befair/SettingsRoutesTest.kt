package dev.jakubzika.befair

import dev.jakubzika.befair.auth.JwtConfig
import dev.jakubzika.befair.data.db.UserRepository
import dev.jakubzika.befair.domain.model.CurrencyChoice
import dev.jakubzika.befair.domain.model.ToolBasis
import dev.jakubzika.befair.domain.model.UserSettingsDto
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRoutesTest {

    init {
        TestDatabase.ensureReady()
    }

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }

    /** Boots the app/DB via a real request, then mints a token for a freshly inserted user row. */
    private suspend fun ApplicationTestBuilder.authedUser(): String {
        client.get("/")
        val userId = UserRepository().create("settings-${UUID.randomUUID()}@example.com", "Test User", "hash")
        return JwtConfig.generateAccessToken(userId.toString())
    }

    private fun HttpRequestBuilder.bearer(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    @Test
    fun `get returns defaults before anything is saved`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.get("/api/settings") { bearer(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(UserSettingsDto(), response.body<UserSettingsDto>())
    }

    @Test
    fun `put then get round-trips the saved settings`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val settings = UserSettingsDto(CurrencyChoice.CUSTOM, "CZK", ToolBasis.YEAR)

        val put = client.put("/api/settings") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(settings)
        }
        assertEquals(HttpStatusCode.OK, put.status)

        // A second save replaces the row rather than failing on the existing key.
        val replacement = UserSettingsDto(CurrencyChoice.USD, "", ToolBasis.WEEK)
        client.put("/api/settings") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(replacement)
        }
        assertEquals(replacement, client.get("/api/settings") { bearer(token) }.body<UserSettingsDto>())
    }

    @Test
    fun `put rejects an over-long custom currency`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.put("/api/settings") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(UserSettingsDto(CurrencyChoice.CUSTOM, "x".repeat(13), ToolBasis.MONTH))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `put rejects an unknown option as a malformed body`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.put("/api/settings") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody("""{"currency":"GBP"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `settings require a token`() = testApplication {
        application { module() }
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/settings").status)
    }
}
