package dev.havlicektomas.studycoach.tutor.presentation

import androidx.lifecycle.*
import dev.havlicektomas.studycoach.core.domain.DataError
import dev.havlicektomas.studycoach.core.domain.Result
import dev.havlicektomas.studycoach.tutor.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class TutorViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @BeforeTest fun setup() { Dispatchers.setMain(dispatcher) }
    @AfterTest fun teardown() { Dispatchers.resetMain() }

    private class Source : TutorRemoteDataSource {
        val requests = mutableListOf<TutorRequest>()
        var answer: suspend () -> Result<TutorResponse, DataError.Network> = { Result.Success(response()) }
        override suspend fun respond(request: TutorRequest): Result<TutorResponse, DataError.Network> {
            requests += request
            return answer()
        }
    }
    private companion object {
        fun response(mode: TutorMode = TutorMode.EXPLAIN) = TutorResponse("id", mode, LearnerLevel.BEGINNER, "Title", "Message",
            quiz = if (mode == TutorMode.QUIZ) TutorQuiz("Question", listOf("A", "B"), 0, "Explanation") else null, isMock = true)
    }
    private fun vm(source: Source = Source(), saved: SavedStateHandle = SavedStateHandle()) = TutorViewModel(source, saved)

    @Test fun defaultsAndSavedInputAreRestoredWithoutLoading() {
        assertEquals(TutorState(), vm().state.value)
        val saved = SavedStateHandle()
        val first = vm(saved = saved)
        first.onAction(TutorAction.QuestionChanged("  Keep me  "))
        first.onAction(TutorAction.ModeSelected(TutorMode.HINT))
        first.onAction(TutorAction.LevelSelected(LearnerLevel.ADVANCED))
        assertEquals(first.state.value, vm(saved = saved).state.value)
    }

    @Test fun validatesBoundariesAndTrimsRequestOnly() = runTest(dispatcher) {
        for (size in listOf(2, 3, 2000, 2001)) {
            val source = Source(); val vm = vm(source)
            val input = "  " + "a".repeat(size) + "  "
            vm.onAction(TutorAction.QuestionChanged(input))
            vm.onAction(TutorAction.SubmitClicked)
            runCurrent()
            assertEquals(size in 3..2000, source.requests.isNotEmpty())
            assertEquals(size !in 3..2000, vm.state.value.validationError)
            assertEquals(input, vm.state.value.question)
            if (source.requests.isNotEmpty()) assertEquals("a".repeat(size), source.requests.single().question)
        }
    }

    @Test fun preventsDuplicateRequestsAndRetainsEditsDuringRequest() = runTest(dispatcher) {
        val source = Source(); val gate = CompletableDeferred<Result<TutorResponse, DataError.Network>>()
        source.answer = { gate.await() }; val vm = vm(source)
        vm.onAction(TutorAction.QuestionChanged("Original"))
        vm.onAction(TutorAction.SubmitClicked)
        vm.onAction(TutorAction.SubmitClicked)
        assertTrue(vm.state.value.isLoading)
        runCurrent()
        assertEquals(1, source.requests.size)
        vm.onAction(TutorAction.QuestionChanged("Edited while loading"))
        gate.complete(Result.Success(response())); runCurrent()
        assertFalse(vm.state.value.isLoading)
        assertEquals("Edited while loading", vm.state.value.question)
    }

    @Test fun retryUsesCurrentQuestionModeAndLevelAndEmitsErrorEvent() = runTest(dispatcher) {
        val source = Source(); source.answer = { Result.Error(DataError.Network.NO_INTERNET) }
        val vm = vm(source)
        vm.onAction(TutorAction.QuestionChanged("First question")); vm.onAction(TutorAction.SubmitClicked); runCurrent()
        assertEquals(DataError.Network.NO_INTERNET, vm.state.value.error)
        assertEquals(TutorEvent.RequestFailed(DataError.Network.NO_INTERNET), vm.events.first())
        vm.onAction(TutorAction.QuestionChanged("Next question"))
        vm.onAction(TutorAction.ModeSelected(TutorMode.HINT)); vm.onAction(TutorAction.LevelSelected(LearnerLevel.ADVANCED))
        source.answer = { Result.Success(response(TutorMode.HINT)) }
        vm.onAction(TutorAction.RetryClicked); runCurrent()
        assertEquals(TutorRequest("Next question", TutorMode.HINT, LearnerLevel.ADVANCED), source.requests.last())
        assertNull(vm.state.value.error)
    }

    @Test fun everySuccessfulResponseResetsQuizState() = runTest(dispatcher) {
        val source = Source(); val vm = vm(source)
        vm.onAction(TutorAction.QuestionChanged("Quiz question"))
        for (mode in TutorMode.entries) {
            source.answer = { Result.Success(response(TutorMode.QUIZ)) }
            vm.onAction(TutorAction.SubmitClicked); runCurrent()
            vm.onAction(TutorAction.QuizChoiceSelected(1)); vm.onAction(TutorAction.CheckAnswerClicked)
            assertTrue(vm.state.value.quizRevealed)
            source.answer = { Result.Success(response(mode)) }
            vm.onAction(TutorAction.SubmitClicked); runCurrent()
            assertNull(vm.state.value.quizSelection); assertFalse(vm.state.value.quizRevealed)
            assertEquals(mode, vm.state.value.response?.mode)
        }
    }

    @Test fun invalidQuizActionsAreIgnored() = runTest(dispatcher) {
        val vm = vm()
        vm.onAction(TutorAction.QuizChoiceSelected(-1)); vm.onAction(TutorAction.CheckAnswerClicked)
        assertNull(vm.state.value.quizSelection); assertFalse(vm.state.value.quizRevealed)
    }

    @Test fun clearingViewModelCancelsActiveRequestWithoutError() = runTest(dispatcher) {
        val source = Source(); var cancelled = false
        source.answer = { try { awaitCancellation() } finally { cancelled = true } }
        val vm = vm(source); val store = ViewModelStore(); store.put("tutor", vm)
        vm.onAction(TutorAction.QuestionChanged("Cancel me")); vm.onAction(TutorAction.SubmitClicked); runCurrent()
        store.clear(); runCurrent()
        assertTrue(cancelled); assertFalse(vm.state.value.isLoading); assertNull(vm.state.value.error)
    }
}
