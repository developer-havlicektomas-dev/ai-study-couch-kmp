package dev.havlicektomas.studycoach.tutor.domain

import dev.havlicektomas.studycoach.core.domain.DataError
import dev.havlicektomas.studycoach.core.domain.Result

interface TutorRemoteDataSource {
    /**
     * Returns a validated response or a typed HTTP, transport or parsing failure.
     * Implementations must propagate coroutine cancellation instead of converting it to an error.
     * Milestone 1 has one remote source, so it does not need a repository abstraction.
     */
    suspend fun respond(request: TutorRequest): Result<TutorResponse, DataError.Network>
}
