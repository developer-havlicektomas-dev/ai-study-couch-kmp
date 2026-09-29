# Tutor data (KMP-04)

`KtorTutorRemoteDataSource` implements the domain interface using an injected,
application-owned `HttpClient` configured by `HttpClientFactory`. It posts only
`question`, `mode` and `level` to the relative route `v1/tutor/respond`, preserving
configured base paths and trimming surrounding question whitespace.

Internal Kotlinx Serialization DTOs map all snake_case fields and enum values
explicitly. The shared client tolerates unknown fields; required fields and enum
values remain strict. Optional fields and defaults match the FastAPI schema.
DTOs never cross the public domain boundary. The response mapper retains request
IDs, mock flags, point/choice order and optional follow-up/quiz payloads, then
calls domain validation to reject missing quiz payloads and invalid answer indices.

HTTP, transport and JSON errors use the KMP-03 safe-call helper; cancellation
propagates. A parsed but invalid quiz returns `INVALID_RESPONSE`, while malformed
JSON, unknown enum values and incomplete required payloads return `SERIALIZATION`.
No repository, per-request client or logging is introduced. Koin and UI wiring
remain in KMP-05.

The shared MockEngine contract tests use the real JSON client configuration and
exercise outgoing JSON, all modes and levels, DTO/domain mapping, optional and
unknown fields, quiz boundaries, HTTP/transport errors and cancellation.

```sh
./gradlew :feature:tutor:data:allTests \
  :feature:tutor:data:compileKotlinIosArm64 verifyModuleBoundaries
```
