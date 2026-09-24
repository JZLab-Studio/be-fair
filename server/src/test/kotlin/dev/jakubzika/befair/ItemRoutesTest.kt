package dev.jakubzika.befair

import dev.jakubzika.befair.auth.JwtConfig
import dev.jakubzika.befair.data.db.UserRepository
import dev.jakubzika.befair.domain.model.CreateItemRequest
import dev.jakubzika.befair.domain.model.EventLoggedResponse
import dev.jakubzika.befair.domain.model.GenericResponse
import dev.jakubzika.befair.domain.model.ItemEventListResponse
import dev.jakubzika.befair.domain.model.ItemEventType
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.domain.model.ItemListResponse
import dev.jakubzika.befair.domain.model.ItemResponse
import dev.jakubzika.befair.domain.model.LogEventRequest
import dev.jakubzika.befair.domain.model.UpdateItemRequest
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ItemRoutesTest {

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }

    /** Boots the app/DB via a real request, then mints a token for a freshly inserted user row. */
    private suspend fun ApplicationTestBuilder.authedUser(): String {
        client.get("/")
        val email = "item-test-${UUID.randomUUID()}@example.com"
        val userId = UserRepository().create(email, "Test User", "hash")
        return JwtConfig.generateAccessToken(userId.toString())
    }

    private fun HttpRequestBuilder.bearer(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    private fun sampleCreateRequest(id: String = UUID.randomUUID().toString()) = CreateItemRequest(
        id = id,
        kind = ItemKind.CLOTHING,
        name = "Navy chinos",
        category = "Trousers",
        priceCents = 8900,
        currency = "EUR",
        purchasedOn = LocalDate.now().toEpochDay(),
    )

    @Test
    fun `create item happy path returns 201 with the persisted row`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val request = sampleCreateRequest()

        val response = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<ItemResponse>()
        assertEquals(request.id, body.id)
        assertEquals("Trousers", body.category)
        assertNull(body.archivedAt)
        assertNull(body.deletedAt)
        assertNull(body.stats)
    }

    @Test
    fun `create item defaults blank category by kind`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val request = sampleCreateRequest().copy(category = null, kind = ItemKind.TOOL)

        val response = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Tool", response.body<ItemResponse>().category)
    }

    @Test
    fun `create item validation failure returns 400 with field errors`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val request = sampleCreateRequest().copy(priceCents = 0)

        val response = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<GenericResponse>()
        assertFalse(body.success)
        assertTrue(body.fields?.containsKey("priceCents") == true)
    }

    @Test
    fun `create item without a token is unauthorized`() = testApplication {
        application { module() }
        val client = jsonClient()

        val response = client.post("/api/items") {
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `create item replay of a stored id is idempotent`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val request = sampleCreateRequest()

        val first = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val second = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            // Different payload for the same id -- the stored row must win, not this one.
            setBody(request.copy(name = "Different name"))
        }

        assertEquals(HttpStatusCode.Created, first.status)
        assertEquals(HttpStatusCode.OK, second.status)
        assertEquals(request.name, second.body<ItemResponse>().name)
    }

    @Test
    fun `create item with an over-long category returns 400 without leaking db internals`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest().copy(category = "x".repeat(61)))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("category"))
        assertFalse(body.contains("exceeds length"))
    }

    @Test
    fun `create item with a non-uuid id returns 400`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest(id = "x".repeat(50)))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.body<GenericResponse>().fields?.containsKey("id") == true)
    }

    @Test
    fun `create item with an out-of-range purchase date returns 400`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest().copy(purchasedOn = Long.MIN_VALUE))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.body<GenericResponse>().fields?.containsKey("purchasedOn") == true)
    }

    @Test
    fun `create item with an unassigned currency code returns 400`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest().copy(currency = "ZZZ"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.body<GenericResponse>().fields?.containsKey("currency") == true)
    }

    @Test
    fun `malformed json body returns 400 not 500`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody("{\"id\":")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `request body without a content type returns 415`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.post("/api/items") {
            bearer(token)
            setBody("{}")
        }

        assertEquals(HttpStatusCode.UnsupportedMediaType, response.status)
    }

    @Test
    fun `update item with an over-long category returns 400`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val response = client.patch("/api/items/${created.id}") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(UpdateItemRequest(category = "x".repeat(61)))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.body<GenericResponse>().fields?.containsKey("category") == true)
    }

    @Test
    fun `malformed updatedSince cursor returns 400`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.get("/api/items?updatedSince=abc") { bearer(token) }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `malformed event limit returns 400`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val response = client.get("/api/items/${created.id}/events?limit=abc") { bearer(token) }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `log event with a negative cost returns 400`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val response = client.post("/api/items/${created.id}/events") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(
                LogEventRequest(
                    id = UUID.randomUUID().toString(),
                    type = ItemEventType.REPAIR,
                    costCents = -100,
                ),
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.body<GenericResponse>().fields?.containsKey("costCents") == true)
    }

    @Test
    fun `log event with a non-uuid id returns 400`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val response = client.post("/api/items/${created.id}/events") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(LogEventRequest(id = "x".repeat(50), type = ItemEventType.WEAR))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.body<GenericResponse>().fields?.containsKey("id") == true)
    }

    @Test
    fun `event pagination keeps same-millisecond events across pages`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()
        val occurredAt = System.currentTimeMillis() - 1_000
        val loggedIds = (1..3).map {
            client.post("/api/items/${created.id}/events") {
                bearer(token)
                contentType(ContentType.Application.Json)
                setBody(
                    LogEventRequest(
                        id = UUID.randomUUID().toString(),
                        type = ItemEventType.WEAR,
                        occurredAt = occurredAt,
                    ),
                )
            }.body<EventLoggedResponse>().event.id
        }

        val firstPage = client.get("/api/items/${created.id}/events?limit=2") { bearer(token) }
            .body<ItemEventListResponse>().events
        val cursor = firstPage.last()
        val secondPage = client.get(
            "/api/items/${created.id}/events?limit=2&before=${cursor.occurredAt}&beforeId=${cursor.id}",
        ) { bearer(token) }.body<ItemEventListResponse>().events

        assertEquals(2, firstPage.size)
        assertEquals(loggedIds.sorted(), (firstPage + secondPage).map { it.id }.sorted())
    }

    @Test
    fun `get item for another user returns 404`() = testApplication {
        application { module() }
        val client = jsonClient()
        val ownerToken = authedUser()
        val otherToken = authedUser()
        val created = client.post("/api/items") {
            bearer(ownerToken)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val response = client.get("/api/items/${created.id}") { bearer(otherToken) }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `get unknown item id returns 404`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()

        val response = client.get("/api/items/${UUID.randomUUID()}") { bearer(token) }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `list items only returns the caller's active items`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val otherToken = authedUser()
        client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }
        client.post("/api/items") {
            bearer(otherToken)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }

        val response = client.get("/api/items") { bearer(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, response.body<ItemListResponse>().items.size)
    }

    @Test
    fun `update item happy path applies partial changes`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val response = client.patch("/api/items/${created.id}") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(UpdateItemRequest(name = "Grey chinos"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ItemResponse>()
        assertEquals("Grey chinos", body.name)
        assertEquals(created.category, body.category)
        assertNotNull(body.stats)
    }

    @Test
    fun `update item validation failure returns 400`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val response = client.patch("/api/items/${created.id}") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(UpdateItemRequest(priceCents = -1))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `update item after soft delete returns 409`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()
        client.delete("/api/items/${created.id}") { bearer(token) }

        val response = client.patch("/api/items/${created.id}") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(UpdateItemRequest(name = "Should not apply"))
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `archive endpoint toggles archived state`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val archived = client.post("/api/items/${created.id}/archive") { bearer(token) }.body<ItemResponse>()
        val unarchived = client.post("/api/items/${created.id}/archive") { bearer(token) }.body<ItemResponse>()

        assertNotNull(archived.archivedAt)
        assertNull(unarchived.archivedAt)
    }

    @Test
    fun `delete item soft deletes and tombstones the delta feed`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val delete = client.delete("/api/items/${created.id}") { bearer(token) }
        val getAfterDelete = client.get("/api/items/${created.id}") { bearer(token) }
        val delta = client.get("/api/items?updatedSince=0") { bearer(token) }.body<ItemListResponse>()

        assertEquals(HttpStatusCode.NoContent, delete.status)
        assertEquals(HttpStatusCode.NotFound, getAfterDelete.status)
        assertEquals(1, delta.items.size)
        assertNotNull(delta.items.single().deletedAt)
    }

    @Test
    fun `log event happy path updates item stats`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val response = client.post("/api/items/${created.id}/events") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(LogEventRequest(id = UUID.randomUUID().toString(), type = ItemEventType.WEAR))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<EventLoggedResponse>()
        assertEquals(1, body.item.stats?.wearCount)
        assertEquals(created.priceCents, body.item.stats?.costPerUseCents)
    }

    @Test
    fun `log event with a type incompatible with the item kind returns 400`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest()) // CLOTHING
        }.body<ItemResponse>()

        val response = client.post("/api/items/${created.id}/events") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(LogEventRequest(id = UUID.randomUUID().toString(), type = ItemEventType.USE))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.body<GenericResponse>().fields?.containsKey("type") == true)
    }

    @Test
    fun `log event replay of a stored id does not double count`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()
        val eventRequest = LogEventRequest(id = UUID.randomUUID().toString(), type = ItemEventType.WEAR)

        val first = client.post("/api/items/${created.id}/events") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(eventRequest)
        }
        val second = client.post("/api/items/${created.id}/events") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(eventRequest)
        }

        assertEquals(HttpStatusCode.Created, first.status)
        assertEquals(HttpStatusCode.OK, second.status)
        assertEquals(1, second.body<EventLoggedResponse>().item.stats?.wearCount)
    }

    @Test
    fun `events on another user's item return 404`() = testApplication {
        application { module() }
        val client = jsonClient()
        val ownerToken = authedUser()
        val otherToken = authedUser()
        val created = client.post("/api/items") {
            bearer(ownerToken)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()

        val response = client.post("/api/items/${created.id}/events") {
            bearer(otherToken)
            contentType(ContentType.Application.Json)
            setBody(LogEventRequest(id = UUID.randomUUID().toString(), type = ItemEventType.WEAR))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `event history excludes an undone event`() = testApplication {
        application { module() }
        val client = jsonClient()
        val token = authedUser()
        val created = client.post("/api/items") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(sampleCreateRequest())
        }.body<ItemResponse>()
        val keep = client.post("/api/items/${created.id}/events") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(LogEventRequest(id = UUID.randomUUID().toString(), type = ItemEventType.WEAR))
        }.body<EventLoggedResponse>().event
        val undo = client.post("/api/items/${created.id}/events") {
            bearer(token)
            contentType(ContentType.Application.Json)
            setBody(LogEventRequest(id = UUID.randomUUID().toString(), type = ItemEventType.WASH))
        }.body<EventLoggedResponse>().event

        val deleteResponse = client.delete("/api/items/${created.id}/events/${undo.id}") { bearer(token) }
        val history = client.get("/api/items/${created.id}/events") { bearer(token) }.body<ItemEventListResponse>()

        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)
        assertEquals(listOf(keep.id), history.events.map { it.id })
    }
}
