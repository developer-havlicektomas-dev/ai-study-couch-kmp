package dev.havlicektomas.studycoach.tutor.domain

import dev.havlicektomas.studycoach.core.domain.DataError
import dev.havlicektomas.studycoach.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals

class TutorResponseTest {
    private fun response(mode: TutorMode, quiz: TutorQuiz? = null) = TutorResponse(
        requestId = "request-123", mode = mode, level = LearnerLevel.ADVANCED,
        title = "Title", message = "Message", points = listOf("First", "Second"),
        quiz = quiz, isMock = false,
    )

    private fun quiz(index: Int, choices: List<String> = listOf("A", "B")) =
        TutorQuiz("Question?", choices, index, "Because...")

    @Test
    fun acceptsExplainAndHintWithoutOptionalFieldsAndPreservesData() {
        for (mode in listOf(TutorMode.EXPLAIN, TutorMode.HINT)) {
            val response = response(mode)
            assertEquals(Result.Success(response), response.validated())
        }
    }

    @Test
    fun acceptsFirstAndLastZeroBasedQuizIndices() {
        for (index in listOf(0, 1)) {
            val response = response(TutorMode.QUIZ, quiz(index))
            assertEquals(Result.Success(response), response.validated())
        }
    }

    @Test
    fun rejectsMissingQuizPayload() {
        assertEquals(Result.Error(DataError.Network.INVALID_RESPONSE), response(TutorMode.QUIZ).validated())
    }

    @Test
    fun rejectsNegativeOutOfRangeAndEmptyQuizChoices() {
        for (quiz in listOf(quiz(-1), quiz(2), quiz(0, emptyList()))) {
            assertEquals(Result.Error(DataError.Network.INVALID_RESPONSE), response(TutorMode.QUIZ, quiz).validated())
        }
    }

    @Test
    fun rejectsInvalidOptionalQuizEvenForOtherModes() {
        assertEquals(Result.Error(DataError.Network.INVALID_RESPONSE), response(TutorMode.EXPLAIN, quiz(2)).validated())
    }
}
