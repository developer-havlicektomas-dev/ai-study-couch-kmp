package dev.havlicektomas.studycoach.core.data

import dev.havlicektomas.studycoach.core.domain.DataError
import io.ktor.client.engine.darwin.DarwinHttpRequestException
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorTimedOut

@OptIn(ExperimentalForeignApi::class)
actual fun platformNetworkError(error: Exception): DataError.Network? {
    if (error !is DarwinHttpRequestException) return null
    return if (error.origin.domain == NSURLErrorDomain && error.origin.code == NSURLErrorTimedOut) {
        DataError.Network.REQUEST_TIMEOUT
    } else DataError.Network.NO_INTERNET
}
