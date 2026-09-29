package dev.havlicektomas.studycoach.tutor.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.havlicektomas.studycoach.core.domain.Result
import dev.havlicektomas.studycoach.tutor.domain.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TutorViewModel(
    private val source: TutorRemoteDataSource,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(TutorState(
        question = savedStateHandle["question"] ?: "",
        mode = TutorMode.entries.find { it.name == savedStateHandle.get<String>("mode") } ?: TutorMode.EXPLAIN,
        level = LearnerLevel.entries.find { it.name == savedStateHandle.get<String>("level") } ?: LearnerLevel.BEGINNER,
    ))
    val state = mutableState.asStateFlow()
    private val eventChannel = Channel<TutorEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    /** UI actions are dispatched on the main thread. Loading is set before launching work. */
    fun onAction(action: TutorAction) {
        when (action) {
            is TutorAction.QuestionChanged -> {
                savedStateHandle["question"] = action.question
                mutableState.update { it.copy(question = action.question, validationError = false) }
            }
            is TutorAction.ModeSelected -> {
                savedStateHandle["mode"] = action.mode.name
                mutableState.update { it.copy(mode = action.mode) }
            }
            is TutorAction.LevelSelected -> {
                savedStateHandle["level"] = action.level.name
                mutableState.update { it.copy(level = action.level) }
            }
            TutorAction.SubmitClicked, TutorAction.RetryClicked -> submit()
            is TutorAction.QuizChoiceSelected -> mutableState.update {
                if (!it.isLoading && !it.quizRevealed && action.index in (it.response?.quiz?.choices?.indices ?: IntRange.EMPTY))
                    it.copy(quizSelection = action.index) else it
            }
            TutorAction.CheckAnswerClicked -> mutableState.update {
                if (!it.isLoading && it.quizSelection != null) it.copy(quizRevealed = true) else it
            }
        }
    }

    private fun submit() {
        val input = state.value
        if (input.isLoading) return
        if (!input.canSubmit) {
            mutableState.update { it.copy(validationError = true) }
            return
        }
        val request = TutorRequest(input.question.trim(), input.mode, input.level)
        mutableState.update { it.copy(isLoading = true, error = null, validationError = false) }
        viewModelScope.launch {
            try {
                when (val result = source.respond(request)) {
                    is Result.Success -> mutableState.update { it.copy(
                        response = result.data, error = null, quizSelection = null, quizRevealed = false,
                    ) }
                    is Result.Error -> {
                        mutableState.update { it.copy(error = result.error) }
                        eventChannel.trySend(TutorEvent.RequestFailed(result.error))
                    }
                }
            } finally {
                mutableState.update { it.copy(isLoading = false) }
            }
        }
    }

    override fun onCleared() { eventChannel.close() }
}
