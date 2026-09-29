package dev.havlicektomas.studycoach.tutor.data

import dev.havlicektomas.studycoach.core.data.safeCall
import dev.havlicektomas.studycoach.core.domain.DataError
import dev.havlicektomas.studycoach.core.domain.Result
import dev.havlicektomas.studycoach.tutor.domain.TutorRemoteDataSource
import dev.havlicektomas.studycoach.tutor.domain.TutorRequest
import dev.havlicektomas.studycoach.tutor.domain.TutorResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Reuses the application client configured by HttpClientFactory; ownership stays with the caller. */
class KtorTutorRemoteDataSource(private val client: HttpClient) : TutorRemoteDataSource {
    override suspend fun respond(request: TutorRequest): Result<TutorResponse, DataError.Network> {
        val result = safeCall<TutorResponseDto> {
            client.post("v1/tutor/respond") {
                contentType(ContentType.Application.Json)
                setBody(request.toDto())
            }
        }
        return when (result) {
            is Result.Success -> result.data.toDomain()
            is Result.Error -> result
        }
    }
}
