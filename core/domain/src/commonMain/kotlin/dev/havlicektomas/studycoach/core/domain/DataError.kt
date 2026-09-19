package dev.havlicektomas.studycoach.core.domain

sealed interface DataError : Error {
    /** Data-layer implementations map HTTP, transport and parsing failures to these values. */
    enum class Network : DataError {
        /** HTTP 400 or 422. */
        BAD_REQUEST,
        REQUEST_TIMEOUT,
        UNAUTHORIZED,
        FORBIDDEN,
        TOO_MANY_REQUESTS,
        /** Includes an unreachable development server, not just offline devices. */
        NO_INTERNET,
        /** HTTP 5xx. */
        SERVER_ERROR,
        SERIALIZATION,
        /** Parsed successfully but violates the response contract. */
        INVALID_RESPONSE,
        UNKNOWN,
    }
}
