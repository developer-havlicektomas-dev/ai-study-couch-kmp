package dev.havlicektomas.studycoach.tutor.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TutorRequestDto(
    val question: String,
    val mode: TutorModeDto,
    val level: LearnerLevelDto,
)

@Serializable
internal enum class TutorModeDto {
    @SerialName("explain") EXPLAIN,
    @SerialName("hint") HINT,
    @SerialName("quiz") QUIZ,
}

@Serializable
internal enum class LearnerLevelDto {
    @SerialName("beginner") BEGINNER,
    @SerialName("intermediate") INTERMEDIATE,
    @SerialName("advanced") ADVANCED,
}

@Serializable
internal data class TutorResponseDto(
    @SerialName("request_id") val requestId: String,
    val mode: TutorModeDto,
    val level: LearnerLevelDto,
    val title: String,
    val message: String,
    val points: List<String> = emptyList(),
    @SerialName("follow_up_question") val followUpQuestion: String? = null,
    val quiz: QuizDto? = null,
    @SerialName("is_mock") val isMock: Boolean = true,
)

@Serializable
internal data class QuizDto(
    val question: String,
    val choices: List<String>,
    @SerialName("correct_choice_index") val correctChoiceIndex: Int,
    val explanation: String,
)
