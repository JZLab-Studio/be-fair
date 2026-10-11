package dev.jakubzika.befair

import dev.jakubzika.befair.auth.JwtConfig
import dev.jakubzika.befair.data.db.UserRepository
import dev.jakubzika.befair.domain.model.SettingsResponse
import dev.jakubzika.befair.domain.model.ToolBasis
import dev.jakubzika.befair.domain.model.UpdateSettingsRequest
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

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }

    /** Boots the app/DB via a real request, then mints a token for a freshly inserted user row. */
    private suspend fun ApplicationTestBuilder.authedUser(): String {
        client.get("/")
        val email = "settings-test-${UUID.randomUUID()}@example.com"
        val userId = UserRepository().create(email, "Test User", "hash")
        return JwtConfig.generateAccessToken(userId.toString())
    }

    private fun HttpRequestBuilder.bearer(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    @Test
    fun `get returns defaults for a user who never saved`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.get("/api/settings") { bearer(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(SettingsResponse(), response.body<SettingsResponse>())
    }

    @Test
    fun `put persists settings and get returns them`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val put = client.put("/api/settings") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(UpdateSettingsRequest("chf", ToolBasis.PER_YEAR))
        }
        val loaded = client.get("/api/settings") { bearer(token) }

        assertEquals(HttpStatusCode.OK, put.status)
        val expected = SettingsResponse("CHF", ToolBasis.PER_YEAR)
        assertEquals(expected, put.body<SettingsResponse>())
        assertEquals(expected, loaded.body<SettingsResponse>())
    }

    @Test
    fun `put rejects an unknown currency`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.put("/api/settings") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(UpdateSettingsRequest("ZZZ", ToolBasis.PER_MONTH))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `settings require authentication`() = testApplication {
        application { module() }
        client.get("/")

        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/settings").status)
    }
}
