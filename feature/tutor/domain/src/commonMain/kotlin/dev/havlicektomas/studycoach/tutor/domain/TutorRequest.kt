package dev.havlicektomas.studycoach.tutor.domain

/** Question validation and trimming belong to the submission flow. This is not a JSON DTO. */
data class TutorRequest(
    val question: String,
    val mode: TutorMode = TutorMode.EXPLAIN,
    val level: LearnerLevel = LearnerLevel.BEGINNER,
)
