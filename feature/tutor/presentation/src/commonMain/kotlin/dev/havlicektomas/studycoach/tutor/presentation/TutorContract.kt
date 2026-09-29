package dev.havlicektomas.studycoach.tutor.presentation

import dev.havlicektomas.studycoach.core.domain.DataError
import dev.havlicektomas.studycoach.tutor.domain.*

data class TutorState(
    val question: String = "",
    val mode: TutorMode = TutorMode.EXPLAIN,
    val level: LearnerLevel = LearnerLevel.BEGINNER,
    val isLoading: Boolean = false,
    val response: TutorResponse? = null,
    val error: DataError.Network? = null,
    val validationError: Boolean = false,
    val quizSelection: Int? = null,
    val quizRevealed: Boolean = false,
) {
    val canSubmit: Boolean get() = !isLoading && question.trim().length in 3..2000
}

sealed interface TutorAction {
    data class QuestionChanged(val question: String) : TutorAction
    data class ModeSelected(val mode: TutorMode) : TutorAction
    data class LevelSelected(val level: LearnerLevel) : TutorAction
    data object SubmitClicked : TutorAction
    data object RetryClicked : TutorAction
    data class QuizChoiceSelected(val index: Int) : TutorAction
    data object CheckAnswerClicked : TutorAction
}

sealed interface TutorEvent {
    data class RequestFailed(val error: DataError.Network) : TutorEvent
}
