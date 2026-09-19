package dev.havlicektomas.studycoach.tutor.domain

import dev.havlicektomas.studycoach.core.domain.DataError
import dev.havlicektomas.studycoach.core.domain.Result

data class TutorResponse(
    val requestId: String,
    val mode: TutorMode,
    val level: LearnerLevel,
    val title: String,
    val message: String,
    val points: List<String> = emptyList(),
    val followUpQuestion: String? = null,
    val quiz: TutorQuiz? = null,
    val isMock: Boolean,
)

data class TutorQuiz(
    val question: String,
    /** Preserve the received order; do not assume a fixed number of choices. */
    val choices: List<String>,
    /** Zero-based; presentation must conceal this and the explanation until answer checking. */
    val correctChoiceIndex: Int,
    val explanation: String,
)

/** Called by the data-layer mapper before exposing a successfully parsed response. */
fun TutorResponse.validated(): Result<TutorResponse, DataError.Network> =
    if ((mode == TutorMode.QUIZ && quiz == null) ||
        (quiz != null && quiz.correctChoiceIndex !in quiz.choices.indices)
    ) {
        Result.Error(DataError.Network.INVALID_RESPONSE)
    } else {
        Result.Success(this)
    }
