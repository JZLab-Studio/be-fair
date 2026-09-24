package dev.jakubzika.befair

import dev.jakubzika.befair.auth.JwtConfig
import dev.jakubzika.befair.data.db.UserRepository
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import java.util.UUID
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Ktor: ${Greeting().greet()}", response.bodyAsText())
    }

    @Test
    fun `refresh token is rejected by auth-jwt protected routes`() = testApplication {
        application { module() }
        // Boot the app so the DB is initialised
        client.get("/")
        val email = "refresh-reject-${UUID.randomUUID()}@example.com"
        val userId = UserRepository().create(email, "Test User", "hash")
        val refreshToken = JwtConfig.generateRefreshToken(userId.toString())

        val itemsResponse = client.get("/api/items") {
            header(HttpHeaders.Authorization, "Bearer $refreshToken")
        }
        val profileResponse = client.get("/api/profile") {
            header(HttpHeaders.Authorization, "Bearer $refreshToken")
        }

        assertEquals(HttpStatusCode.Unauthorized, itemsResponse.status)
        assertEquals(HttpStatusCode.Unauthorized, profileResponse.status)
    }
}