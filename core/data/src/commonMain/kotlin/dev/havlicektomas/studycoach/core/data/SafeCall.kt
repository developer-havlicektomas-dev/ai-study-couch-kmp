package dev.havlicektomas.studycoach.core.data

import dev.havlicektomas.studycoach.core.domain.DataError
import dev.havlicektomas.studycoach.core.domain.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.JsonConvertException
import kotlinx.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException

/** Covers both executing the request and decoding its body. Never consumes cancellation. */
suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, DataError.Network> = try {
    val response = execute()
    val error = httpError(response.status.value)
    if (error != null) Result.Error(error) else Result.Success(response.body<T>())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    currentCoroutineContext().ensureActive()
    Result.Error(when (error) {
        is HttpRequestTimeoutException, is ConnectTimeoutException, is SocketTimeoutException -> DataError.Network.REQUEST_TIMEOUT
        is SerializationException, is JsonConvertException, is NoTransformationFoundException -> DataError.Network.SERIALIZATION
        else -> platformNetworkError(error) ?: if (error is IOException) DataError.Network.NO_INTERNET else DataError.Network.UNKNOWN
    })
}

fun httpError(status: Int): DataError.Network? = when (status) {
    in 200..299 -> null
    400, 422 -> DataError.Network.BAD_REQUEST
    401 -> DataError.Network.UNAUTHORIZED
    403 -> DataError.Network.FORBIDDEN
    408 -> DataError.Network.REQUEST_TIMEOUT
    429 -> DataError.Network.TOO_MANY_REQUESTS
    in 500..599 -> DataError.Network.SERVER_ERROR
    else -> DataError.Network.UNKNOWN
}

/** Engine-specific exceptions are translated without exposing their messages. */
expect fun platformNetworkError(error: Exception): DataError.Network?
