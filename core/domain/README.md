# Core domain

Pure Kotlin contracts shared across layers. `Result<D, E>` represents success or a
typed `Error`; `map`, `onSuccess`, `onFailure` and `asEmptyResult` preserve failures
and do not intercept exceptions or cancellation.

`DataError.Network` covers invalid requests (400/422), timeouts, unauthorized and
forbidden responses, rate limits, connectivity, server failures, serialization,
invalid response data and unknown failures. HTTP/exception mapping belongs to
KMP-03; localized messages belong to presentation. No local-storage errors are
introduced before local storage exists.
