# Tutor domain

Pure Kotlin `TutorMode`, `LearnerLevel`, `TutorRequest`, `TutorResponse` and
`TutorQuiz`, plus the suspend `TutorRemoteDataSource.respond` contract. The public
API exposes core results/errors, so this module exports `core:domain` with `api`.

The models retain request IDs, response order, optional follow-up/quiz payloads,
and the mock flag. Quiz indices are zero-based. `TutorResponse.validated()` returns
`INVALID_RESPONSE` for a missing quiz in quiz mode or an out-of-range answer index
(including an empty choices list). The KMP-04 mapper must call this before returning
success. No fixed choice count is imposed: the backend describes four choices but
does not enforce that constraint, and the client requirements say to render all.

JSON names/serialization and transport objects belong to the data layer. Request
trimming/length validation belongs to the later submission flow; quiz selection
and answer-reveal state belong to presentation. Implementations must propagate
coroutine cancellation. No repository is needed for the single remote source.

Shared tests cover result propagation and response validity on Android and iOS.
