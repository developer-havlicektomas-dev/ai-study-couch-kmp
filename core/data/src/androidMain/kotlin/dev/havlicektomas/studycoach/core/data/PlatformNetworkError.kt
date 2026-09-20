package dev.havlicektomas.studycoach.core.data

import dev.havlicektomas.studycoach.core.domain.DataError
import java.net.SocketTimeoutException

actual fun platformNetworkError(error: Exception): DataError.Network? =
    if (error is SocketTimeoutException) DataError.Network.REQUEST_TIMEOUT else null
