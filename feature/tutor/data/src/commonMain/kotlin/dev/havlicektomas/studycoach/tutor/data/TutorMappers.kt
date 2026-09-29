package dev.havlicektomas.studycoach.tutor.data

import dev.havlicektomas.studycoach.core.domain.DataError
import dev.havlicektomas.studycoach.core.domain.Result
import dev.havlicektomas.studycoach.tutor.domain.*

internal fun TutorRequest.toDto() = TutorRequestDto(
    question = question.trim(),
    mode = when (mode) {
        TutorMode.EXPLAIN -> TutorModeDto.EXPLAIN
        TutorMode.HINT -> TutorModeDto.HINT
        TutorMode.QUIZ -> TutorModeDto.QUIZ
    },
    level = when (level) {
        LearnerLevel.BEGINNER -> LearnerLevelDto.BEGINNER
        LearnerLevel.INTERMEDIATE -> LearnerLevelDto.INTERMEDIATE
        LearnerLevel.ADVANCED -> LearnerLevelDto.ADVANCED
    },
)

internal fun TutorResponseDto.toDomain(): Result<TutorResponse, DataError.Network> = TutorResponse(
    requestId = requestId,
    mode = when (mode) {
        TutorModeDto.EXPLAIN -> TutorMode.EXPLAIN
        TutorModeDto.HINT -> TutorMode.HINT
        TutorModeDto.QUIZ -> TutorMode.QUIZ
    },
    level = when (level) {
        LearnerLevelDto.BEGINNER -> LearnerLevel.BEGINNER
        LearnerLevelDto.INTERMEDIATE -> LearnerLevel.INTERMEDIATE
        LearnerLevelDto.ADVANCED -> LearnerLevel.ADVANCED
    },
    title = title,
    message = message,
    points = points,
    followUpQuestion = followUpQuestion,
    quiz = quiz?.let { TutorQuiz(it.question, it.choices, it.correctChoiceIndex, it.explanation) },
    isMock = isMock,
).validated()
