package dev.havlicektomas.studycoach.core.data

import dev.havlicektomas.studycoach.core.domain.DataError
import dev.havlicektomas.studycoach.core.domain.Result
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import kotlinx.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

class NetworkingTest {
    private val config = NetworkConfig("http://localhost:8000/api", development = true)

    @Test
    fun sendsJsonToConfiguredBaseAndReadsResponse() = runTest {
        val engine = MockEngine { request ->
            assertEquals("http://localhost:8000/api/v1/tutor/respond", request.url.toString())
            assertEquals(HttpMethod.Post, request.method)
            assertEquals(ContentType.Application.Json, request.body.contentType)
            respond("""{"status":"ok","future_field":true}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClientFactory.create(engine, config)
        try {
            val result = safeCall<JsonObject> {
                client.post("v1/tutor/respond") { setBody(buildJsonObject { put("question", "Why?") }) }
            }
            assertEquals("ok", assertIs<Result.Success<JsonObject>>(result).data["status"]?.jsonPrimitive?.content)
        } finally { client.close(); engine.close() }
    }

    @Test
    fun mapsHttpFailuresWithoutTryingToParseTheirBodies() = runTest {
        val cases = mapOf(400 to DataError.Network.BAD_REQUEST, 422 to DataError.Network.BAD_REQUEST,
            401 to DataError.Network.UNAUTHORIZED, 403 to DataError.Network.FORBIDDEN,
            408 to DataError.Network.REQUEST_TIMEOUT, 429 to DataError.Network.TOO_MANY_REQUESTS,
            500 to DataError.Network.SERVER_ERROR, 503 to DataError.Network.SERVER_ERROR,
            404 to DataError.Network.UNKNOWN, 302 to DataError.Network.UNKNOWN)
        for ((status, expected) in cases) {
            val engine = MockEngine { respond("not JSON", HttpStatusCode.fromValue(status)) }
            val client = HttpClientFactory.create(engine, config)
            try { assertEquals(Result.Error(expected), safeCall<JsonObject> { client.get("health") }) }
            finally { client.close(); engine.close() }
        }
    }

    @Test
    fun malformedJsonBecomesSerializationFailure() = runTest {
        val engine = MockEngine { respond("{broken", headers = headersOf(HttpHeaders.ContentType, "application/json")) }
        val client = HttpClientFactory.create(engine, config)
        try { assertEquals(Result.Error(DataError.Network.SERIALIZATION), safeCall<JsonObject> { client.get("health") }) }
        finally { client.close(); engine.close() }
    }

    @Test
    fun unexpectedContentTypeBecomesSerializationFailure() = runTest {
        val engine = MockEngine { respond("<html>Bad gateway</html>", headers = headersOf(HttpHeaders.ContentType, "text/html")) }
        val client = HttpClientFactory.create(engine, config)
        try { assertEquals(Result.Error(DataError.Network.SERIALIZATION), safeCall<JsonObject> { client.get("health") }) }
        finally { client.close(); engine.close() }
    }

    @Test
    fun transportAndTimeoutFailuresAreTyped() = runTest {
        for ((error, expected) in listOf(
            IOException("unreachable") to DataError.Network.NO_INTERNET,
            HttpRequestTimeoutException("http://localhost", 30_000) to DataError.Network.REQUEST_TIMEOUT,
            IllegalStateException("unexpected") to DataError.Network.UNKNOWN,
        )) {
            val engine = MockEngine { throw error }
            val client = HttpClientFactory.create(engine, config)
            try { assertEquals(Result.Error(expected), safeCall<JsonObject> { client.get("health") }) }
            finally { client.close(); engine.close() }
        }
    }

    @Test
    fun cancellationPropagatesFromActiveRequest() = runTest {
        val entered = CompletableDeferred<Unit>()
        val engine = MockEngine { entered.complete(Unit); awaitCancellation() }
        val client = HttpClientFactory.create(engine, config)
        try {
            val request = async { safeCall<JsonObject> { client.get("health") } }
            entered.await()
            request.cancel()
            assertFailsWith<CancellationException> { request.await() }
        } finally { client.close(); engine.close() }
    }

    @Test
    fun directlyThrownCancellationIsNotConvertedToError() = runTest {
        val cancellation = CancellationException("cancelled")
        assertSame(cancellation, assertFailsWith<CancellationException> {
            safeCall<JsonObject> { throw cancellation }
        })
    }

    @Test
    fun blocksExplicitHttpRequestInReleaseClient() = runTest {
        var reachedEngine = false
        val engine = MockEngine { reachedEngine = true; respond("{}") }
        val client = HttpClientFactory.create(engine, NetworkConfig("https://host"))
        try {
            assertIs<Result.Error<*>>(safeCall<JsonObject> { client.get("http://localhost/health") })
            assertFalse(reachedEngine)
        } finally { client.close(); engine.close() }
    }

    @Test
    fun rejectsInsecureOrAmbiguousReleaseConfiguration() {
        for (url in listOf("http://localhost:8000", "ftp://host", "host", "https://user:pass@host", "https://host?key=value", "https://host#fragment")) {
            assertFailsWith<IllegalArgumentException>(url) { NetworkConfig(url) }
        }
        assertEquals("https://host/api/", NetworkConfig("https://host/api/").baseUrl)
        assertEquals("http://localhost:8000/api/", config.baseUrl)
    }
}
