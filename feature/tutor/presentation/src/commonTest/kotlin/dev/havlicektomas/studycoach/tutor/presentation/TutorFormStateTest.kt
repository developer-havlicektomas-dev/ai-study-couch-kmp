package dev.havlicektomas.studycoach.tutor.presentation

import kotlin.test.*

class TutorFormStateTest {
    @Test fun countAppearsOnlyNearTrimmedLimit() {
        assertFalse(TutorState(question = "a".repeat(1799)).showCharacterCount)
        assertTrue(TutorState(question = "a".repeat(1800)).showCharacterCount)
        val padded = TutorState(question = "  " + "a".repeat(2000) + "  ")
        assertEquals(2000, padded.questionLength)
        assertTrue(padded.canSubmit)
        assertFalse(padded.showQuestionError)
    }
    @Test fun overlongInputIsRetainedAndRejected() {
        val input = "a".repeat(2001)
        val state = TutorState(question = input)
        assertEquals(input, state.question)
        assertEquals(2001, state.questionLength)
        assertTrue(state.showCharacterCount)
        assertTrue(state.showQuestionError)
        assertFalse(state.canSubmit)
    }
    @Test fun initialInputIsQuietButEditedInvalidInputHasFeedback() {
        assertFalse(TutorState().showQuestionError)
        for (input in listOf(" ", "a", "ab")) {
            assertTrue(TutorState(question = input).showQuestionError)
            assertFalse(TutorState(question = input).canSubmit)
        }
        assertTrue(TutorState(validationError = true).showQuestionError)
        assertFalse(TutorState(question = "abc").showQuestionError)
    }
    @Test fun unicodeCharactersMatchBackendLength() {
        assertFalse(TutorState(question = "😀😀").canSubmit)
        val state = TutorState(question = "😀".repeat(2000))
        assertEquals(2000, state.questionLength)
        assertTrue(state.canSubmit)
        assertFalse(TutorState(question = "😀".repeat(2001)).canSubmit)
    }
    @Test fun loadingBlocksOtherwiseValidInput() {
        assertFalse(TutorState(question = "A valid question", isLoading = true).canSubmit)
    }
}
