package dev.havlicektomas.studycoach.tutor.data

import dev.havlicektomas.studycoach.core.data.HttpClientFactory
import dev.havlicektomas.studycoach.core.data.NetworkConfig
import dev.havlicektomas.studycoach.core.domain.DataError
import dev.havlicektomas.studycoach.core.domain.Result
import dev.havlicektomas.studycoach.tutor.domain.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.*
import kotlin.test.*

class TutorRemoteDataSourceTest {
    private val request = TutorRequest("Why is the sky blue?")
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun payload(mode: String = "explain", level: String = "beginner", extra: String = "") =
        """{"request_id":"id-123","mode":"$mode","level":"$level","title":"Title","message":"Message"$extra}"""

    private val quiz = """{"question":"Which?","choices":["A","B","C","D"],"correct_choice_index":1,"explanation":"Because B"}"""

    private suspend fun withSource(handler: MockRequestHandler, block: suspend (KtorTutorRemoteDataSource) -> Unit) {
        val engine = MockEngine(handler)
        val client = HttpClientFactory.create(engine, NetworkConfig("https://test.invalid/api/"))
        try { block(KtorTutorRemoteDataSource(client)) }
        finally { client.close(); engine.close() }
    }

    @Test
    fun sendsExactlyThreeFieldsForEveryModeAndLevelAndReusesClient() = runTest {
        val modes = listOf(TutorMode.EXPLAIN to "explain", TutorMode.HINT to "hint", TutorMode.QUIZ to "quiz")
        val levels = listOf(LearnerLevel.BEGINNER to "beginner", LearnerLevel.INTERMEDIATE to "intermediate", LearnerLevel.ADVANCED to "advanced")
        var received: JsonObject? = null
        var count = 0
        withSource({ http ->
            count++
            assertEquals(HttpMethod.Post, http.method)
            assertEquals("https://test.invalid/api/v1/tutor/respond", http.url.toString())
            assertEquals(ContentType.Application.Json, http.body.contentType)
            received = Json.parseToJsonElement(http.body.toByteArray().decodeToString()).jsonObject
            respond(payload(), headers = jsonHeaders)
        }) { source ->
            for ((mode, wireMode) in modes) for ((level, wireLevel) in levels) {
                assertIs<Result.Success<TutorResponse>>(source.respond(TutorRequest("  Why?\n", mode, level)))
                assertEquals(buildJsonObject { put("question", "Why?"); put("mode", wireMode); put("level", wireLevel) }, received)
            }
        }
        assertEquals(9, count)
    }

    @Test
    fun mapsExplainAndHintPreservingOrderOptionalFieldsAndMockFlag() = runTest {
        for ((wireMode, mode) in listOf("explain" to TutorMode.EXPLAIN, "hint" to TutorMode.HINT)) {
            withSource({ respond(payload(wireMode, "advanced", """, "points":["First","Second","Third"],"follow_up_question":"What next?","quiz":null,"is_mock":false,"future_field":42"""), headers = jsonHeaders) }) { source ->
                assertEquals(Result.Success(TutorResponse("id-123", mode, LearnerLevel.ADVANCED, "Title", "Message",
                    listOf("First", "Second", "Third"), "What next?", null, false)), source.respond(request))
            }
        }
    }

    @Test
    fun mapsQuizSnakeCaseAndToleratesNestedUnknownFields() = runTest {
        val nested = quiz.dropLast(1) + """, "future_field":"ignored"}"""
        withSource({ respond(payload("quiz", "intermediate", """, "quiz":$nested,"is_mock":true"""), headers = jsonHeaders) }) { source ->
            assertEquals(Result.Success(TutorResponse("id-123", TutorMode.QUIZ, LearnerLevel.INTERMEDIATE, "Title", "Message",
                quiz = TutorQuiz("Which?", listOf("A", "B", "C", "D"), 1, "Because B"), isMock = true)), source.respond(request))
        }
    }

    @Test
    fun omittedOptionalFieldsUseBackendDefaults() = runTest {
        withSource({ respond(payload(), headers = jsonHeaders) }) { source ->
            val response = assertIs<Result.Success<TutorResponse>>(source.respond(request)).data
            assertEquals(emptyList(), response.points)
            assertNull(response.followUpQuestion)
            assertNull(response.quiz)
            assertTrue(response.isMock)
        }
    }

    @Test
    fun rejectsMissingNullAndInvalidQuizPayloadsAsTypedErrors() = runTest {
        val extras = listOf("", """, "quiz":null""",
            """, "quiz":${quiz.replace(":1", ":-1")}""",
            """, "quiz":${quiz.replace(":1", ":4")}""",
            """, "quiz":${quiz.replace("[\"A\",\"B\",\"C\",\"D\"]", "[]")}""")
        for (extra in extras) {
            withSource({ respond(payload("quiz", extra = extra), headers = jsonHeaders) }) { source ->
                assertEquals(Result.Error(DataError.Network.INVALID_RESPONSE), source.respond(request))
            }
        }
    }

    @Test
    fun acceptsFirstAndLastQuizIndices() = runTest {
        for (index in listOf(0, 3)) {
            withSource({ respond(payload("quiz", extra = """, "quiz":${quiz.replace(":1", ":$index")}"""), headers = jsonHeaders) }) { source ->
                assertEquals(index, assertIs<Result.Success<TutorResponse>>(source.respond(request)).data.quiz?.correctChoiceIndex)
            }
        }
    }

    @Test
    fun malformedMissingAndUnknownRequiredDataAreSerializationErrors() = runTest {
        val bodies = listOf("{broken", "{}", payload("new-mode"), payload(level = "expert"),
            payload(extra = """, "points":null"""), payload("quiz", extra = """, "quiz":{"choices":[]}"""))
        for (body in bodies) {
            withSource({ respond(body, headers = jsonHeaders) }) { source ->
                assertEquals(Result.Error(DataError.Network.SERIALIZATION), source.respond(request))
            }
        }
    }

    @Test
    fun propagatesHttpErrorsWithoutParsingErrorBody() = runTest {
        for ((code, error) in listOf(422 to DataError.Network.BAD_REQUEST, 500 to DataError.Network.SERVER_ERROR)) {
            withSource({ respond("not a tutor payload", HttpStatusCode.fromValue(code), jsonHeaders) }) { source ->
                assertEquals(Result.Error(error), source.respond(request))
            }
        }
    }

    @Test
    fun propagatesTransportAndTimeoutErrors() = runTest {
        for ((failure, error) in listOf(IOException("offline") to DataError.Network.NO_INTERNET,
            HttpRequestTimeoutException("https://test.invalid", 30_000) to DataError.Network.REQUEST_TIMEOUT)) {
            withSource({ throw failure }) { source -> assertEquals(Result.Error(error), source.respond(request)) }
        }
    }

    @Test
    fun cancellationIsNotTurnedIntoFailure() = runTest {
        val entered = CompletableDeferred<Unit>()
        withSource({ entered.complete(Unit); awaitCancellation() }) { source ->
            val pending = async { source.respond(request) }
            entered.await()
            pending.cancel()
            assertFailsWith<CancellationException> { pending.await() }
        }
    }
}
