package dev.jakubzika.befair.data.repository

import dev.jakubzika.befair.PROFILE_PICTURE_MAX_BYTES
import dev.jakubzika.befair.domain.AppResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.callContext
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/** Answers every request through [handler]; avoids adding a mock-engine dependency. */
@OptIn(InternalAPI::class)
private class FakeEngine(
    private val handler: suspend (HttpRequestData) -> Pair<HttpStatusCode, ByteArray>,
) : HttpClientEngineBase("fake") {
    override val config = HttpClientEngineConfig()

    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val (status, body) = handler(data)
        return HttpResponseData(
            status, GMTDate(), Headers.Empty, HttpProtocolVersion.HTTP_1_1, ByteReadChannel(body), callContext(),
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfilePictureRepositoryImplTest {

    private val old = byteArrayOf(1, 2, 3)
    private val fresh = byteArrayOf(9, 8, 7)

    private fun repo(handler: suspend (HttpRequestData) -> Pair<HttpStatusCode, ByteArray>) =
        ProfilePictureRepositoryImpl(HttpClient(FakeEngine(handler)) { expectSuccess = true })

    private val noContent = HttpStatusCode.NoContent to ByteArray(0)

    @Test
    fun `refresh stores the fetched picture`() = runTest {
        val repo = repo { HttpStatusCode.OK to old }
        assertIs<AppResult.Success<Unit>>(repo.refresh())
        assertContentEquals(old, repo.picture.value)
    }

    @Test
    fun `refresh clears the picture on 404`() = runTest {
        val repo = repo { req ->
            if (req.method == HttpMethod.Put) noContent else HttpStatusCode.NotFound to ByteArray(0)
        }
        repo.upload(fresh)
        // A fresh refresh sees the server's current state: no picture.
        assertIs<AppResult.Success<Unit>>(repo.refresh())
        assertNull(repo.picture.value)
    }

    @Test
    fun `failed refresh reports an error and keeps the current picture`() = runTest {
        val repo = repo { req ->
            if (req.method == HttpMethod.Put) noContent else HttpStatusCode.InternalServerError to ByteArray(0)
        }
        repo.upload(fresh)
        assertIs<AppResult.Error>(repo.refresh())
        assertContentEquals(fresh, repo.picture.value)
    }

    @Test
    fun `refresh that finishes after an upload does not overwrite it`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repo = repo { req ->
            if (req.method == HttpMethod.Put) {
                noContent
            } else {
                gate.await() // the GET was sent before the upload and answers after it
                HttpStatusCode.OK to old
            }
        }
        val refresh = async { repo.refresh() }
        runCurrent()
        assertIs<AppResult.Success<Unit>>(repo.upload(fresh))
        gate.complete(Unit)
        refresh.await()
        assertContentEquals(fresh, repo.picture.value)
    }

    @Test
    fun `refresh that finishes after clear does not resurrect the picture`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repo = repo { gate.await(); HttpStatusCode.OK to old }
        val refresh = async { repo.refresh() }
        runCurrent()
        repo.clear()
        gate.complete(Unit)
        refresh.await()
        assertNull(repo.picture.value)
    }

    @Test
    fun `upload rejects empty and oversized pictures without a request`() = runTest {
        var requests = 0
        val repo = repo { requests++; noContent }
        assertIs<AppResult.Error>(repo.upload(ByteArray(0)))
        assertIs<AppResult.Error>(repo.upload(ByteArray(PROFILE_PICTURE_MAX_BYTES + 1)))
        assertEquals(0, requests)
        assertNull(repo.picture.value)
    }

    @Test
    fun `failed upload keeps the previous picture`() = runTest {
        var fail = false
        val repo = repo { if (fail) HttpStatusCode.PayloadTooLarge to ByteArray(0) else noContent }
        repo.upload(old)
        fail = true
        assertIs<AppResult.Error>(repo.upload(fresh))
        assertContentEquals(old, repo.picture.value)
    }
}
