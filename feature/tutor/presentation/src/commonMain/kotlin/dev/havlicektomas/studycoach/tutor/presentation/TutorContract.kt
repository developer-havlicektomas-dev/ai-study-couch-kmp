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
    val questionLength: Int get() = question.trim().unicodeLength()
    val showCharacterCount: Boolean get() = questionLength >= 1800
    val showQuestionError: Boolean get() = validationError || (question.isNotEmpty() && questionLength !in 3..2000)
    val canSubmit: Boolean get() = !isLoading && questionLength in 3..2000
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

/** Match the backend character count: a supplementary Unicode character counts once. */
private fun String.unicodeLength(): Int {
    var count = 0
    var index = 0
    while (index < length) {
        val current = this[index++]
        if (current.isHighSurrogate() && index < length && this[index].isLowSurrogate()) index++
        count++
    }
    return count
}
