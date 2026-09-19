# AI Study Coach KMP Client — Milestone 1 Requirements

## 1. Purpose

Build a Kotlin Multiplatform mobile client for Android and iOS that connects to the existing mocked FastAPI service. A learner enters a question, selects how the tutor should respond and their knowledge level, submits the request, and reads an explanation, progressive hints, or a multiple-choice quiz.

This milestone exists to establish the complete mobile-to-backend flow before adding a real AI provider or product features such as accounts and history.

## 2. Product goals

- Deliver the same core experience on Android and iOS from shared Kotlin code.
- Demonstrate a clear KMP architecture with testable presentation, domain, and data boundaries.
- Treat the existing FastAPI schemas as the source of truth for the HTTP contract.
- Handle loading, validation, backend, connectivity, and serialization failures explicitly.
- Keep the milestone small enough to finish and understand.

## 3. Out of scope

The following are not part of Milestone 1:

- A real AI provider
- Authentication or user accounts
- Local or cloud response history
- Image, document, or voice input
- Response streaming
- Push notifications
- Analytics, subscriptions, or payments
- Offline response generation
- Web or desktop targets

## 4. Supported platforms

- Android phone and emulator
- iPhone and iOS Simulator
- Portrait layouts are required; landscape should remain usable but does not require a specialized layout.
- The application must use Compose Multiplatform for shared UI.

## 5. User flow

1. The app opens on the Tutor screen.
2. The learner enters a question.
3. The learner selects a response mode: Explain, Hint, or Quiz.
4. The learner selects a knowledge level: Beginner, Intermediate, or Advanced.
5. The learner taps **Ask tutor**.
6. The app validates the question and sends it to the FastAPI backend.
7. While waiting, the app shows progress and prevents duplicate submissions.
8. On success, the app renders the mode-appropriate response.
9. On failure, the app displays a useful message and allows retrying without losing the question or selections.

Milestone 1 is intentionally a single-screen flow. Navigation infrastructure and additional screens should be introduced when history or settings are added, rather than creating empty destinations now.

## 6. Functional requirements

### FR-1: Question input

- Show a multiline text field labeled **What would you like to learn?**
- Allow editing and pasting without silently truncating text; treat more than 2,000 characters after trimming as invalid.
- Show a character count as the limit is approached.
- Preserve the current question during recomposition, ordinary lifecycle changes, requests, and retry attempts.
- Do not clear the question after a successful request.

### FR-2: Client-side validation

- Trim surrounding whitespace before validation and submission.
- A valid question contains 3–2,000 characters after trimming.
- Disable **Ask tutor** when the question is invalid or a request is already running.
- If submission is attempted with invalid input, show an inline, localized validation message.
- The backend remains authoritative; client validation is for immediate feedback only.

### FR-3: Tutor mode

Provide exactly three mutually exclusive modes:

| UI label | API value | Meaning |
| --- | --- | --- |
| Explain | `explain` | Return an explanation with ordered points and a follow-up question. |
| Hint | `hint` | Return three progressively more helpful hints without a full answer. |
| Quiz | `quiz` | Return one multiple-choice question. |

The default mode is Explain.

### FR-4: Learner level

Provide exactly three mutually exclusive levels:

| UI label | API value |
| --- | --- |
| Beginner | `beginner` |
| Intermediate | `intermediate` |
| Advanced | `advanced` |

The default level is Beginner.

### FR-5: Submit request

- Submit `POST /v1/tutor/respond` with JSON content.
- Only one request may be active at a time.
- Show an in-context loading indicator and a loading label such as **Preparing your answer…**.
- The request must be cancellable with the ViewModel lifecycle.
- Retrying must submit the current question, mode, and level.

### FR-6: Explain response

For an `explain` response, show:

- A visible mock indicator when `is_mock` is `true`
- `title`
- `message`
- Every item in `points`, in the order received
- `follow_up_question` when present

### FR-7: Hint response

For a `hint` response, show:

- A visible mock indicator when `is_mock` is `true`
- `title`
- `message`
- The hints from `points`, clearly numbered and in the order received
- `follow_up_question` when present

The client must not invent or expand hints.

### FR-8: Quiz response

For a `quiz` response:

- Show `title`, `message`, and the nested quiz `question`.
- Show all choices in their received order as a single-choice control.
- Do not reveal `correct_choice_index` or `explanation` before the learner checks an answer.
- Disable **Check answer** until a choice is selected.
- When checked, mark the selected answer as correct or incorrect, identify the correct answer, and show `explanation`.
- Treat `correct_choice_index` as zero-based.
- Quiz selection and reveal state are local UI state and must reset when a new tutor request succeeds.

If a quiz response is missing its `quiz` payload, or its answer index is outside the choices list, treat the response as invalid data and show a recoverable error rather than crashing.

### FR-9: Error handling and retry

Provide localized, user-friendly messages for at least:

- No internet or unreachable development server
- Request timeout
- Invalid request (`400` or `422`)
- Unauthorized or forbidden response
- Too many requests
- Backend/server failure (`5xx`)
- Response deserialization or invalid response data
- Unknown failure

Expected failures must be represented as typed results/errors; they must not propagate as uncaught exceptions to the UI. Coroutine cancellation must be rethrown and must not be converted to a generic error.

The error UI must include a retry action for recoverable request failures. It must not expose stack traces, raw exception text, or the complete FastAPI validation body to users. Debug builds may log diagnostic details without logging sensitive content.

### FR-10: Development connectivity

The API base URL must be build-configurable and must not be scattered through feature code.

Recommended development defaults:

| Target | Base URL |
| --- | --- |
| Android emulator | `http://10.0.2.2:8000` |
| iOS Simulator | `http://127.0.0.1:8000` |
| Physical device | `http://<development-machine-LAN-IP>:8000` |

For physical-device testing, FastAPI must listen on `0.0.0.0`, and the phone and development machine must be on the same reachable network. Any cleartext HTTP allowance must be limited to development builds. Production configuration must require HTTPS.

## 7. Backend contract

The implemented backend in `../ai-study-couch-api` is the source of truth.

### Health endpoint

`GET /health`

Successful response:

```json
{
  "status": "ok"
}
```

The client may use this endpoint for development diagnostics. It is not required in the primary user flow.

### Tutor endpoint

`POST /v1/tutor/respond`

Request:

```json
{
  "question": "Why is the sky blue?",
  "mode": "explain",
  "level": "beginner"
}
```

Request rules:

- `question` is required and is trimmed by the backend.
- Question length after trimming is 3–2,000 characters.
- `mode` defaults to `explain` but the client should always send its explicit selection.
- `level` defaults to `beginner` but the client should always send its explicit selection.
- Unknown request fields are rejected.

Successful response:

```json
{
  "request_id": "0f0c2e1a-9d55-4a6e-9a6a-6cd9b3f7a1f2",
  "mode": "explain",
  "level": "beginner",
  "title": "Understanding: Why is the sky blue?",
  "message": "This is a mocked explanation for a beginner learner.",
  "points": [
    "First, identify the main idea in the question.",
    "Next, break the topic into smaller parts."
  ],
  "follow_up_question": "Can you explain the main idea in your own words?",
  "quiz": null,
  "is_mock": true
}
```

Quiz payload:

```json
{
  "question": "Which approach best helps you answer the question?",
  "choices": ["Choice A", "Choice B", "Choice C", "Choice D"],
  "correct_choice_index": 1,
  "explanation": "Explanation of the correct choice."
}
```

Validation failures return HTTP `422` with FastAPI's standard `detail` array. The client does not need to expose every backend validation detail in Milestone 1, but it must map `422` to an understandable invalid-request error.

### JSON mapping requirements

- Use Kotlinx Serialization.
- Use explicit `@SerialName` mappings for snake_case fields such as `request_id`, `follow_up_question`, `correct_choice_index`, and `is_mock`.
- Represent request/response transport objects as DTOs in the data layer.
- Keep DTOs separate from domain models and map between them in the data layer.
- Configure response parsing to tolerate unknown response fields for forward compatibility while keeping outgoing request DTOs explicit.

## 8. Architecture requirements

### 8.1 Module layout

Use feature-first, layered modules:

```text
:composeApp
:build-logic
:core:domain
:core:data
:core:presentation
:core:design-system
:feature:tutor:domain
:feature:tutor:data
:feature:tutor:presentation
```

The existing `iosApp` Xcode wrapper may remain outside the Gradle module list.

Responsibilities:

- `:core:domain`: generic typed `Result`, shared error contracts.
- `:core:data`: Ktor `HttpClient` factory, safe-call helpers, base URL handling.
- `:core:presentation`: `UiText` and shared presentation utilities.
- `:core:design-system`: theme and genuinely reusable UI primitives.
- `:feature:tutor:domain`: tutor domain models and `TutorRemoteDataSource` interface.
- `:feature:tutor:data`: API DTOs, DTO/domain mappers, and `KtorTutorRemoteDataSource`.
- `:feature:tutor:presentation`: state, actions, events, ViewModel, and Tutor composables.
- `:composeApp`: platform application entry points and dependency assembly.

Do not create a repository in this milestone: the tutor uses only one remote source. Introduce a repository later if remote responses are combined with local history or caching.

### 8.2 Dependency rules

- Presentation depends on its feature domain and relevant core modules, never feature data.
- Feature data depends on feature domain, `:core:domain`, and `:core:data`.
- Feature domain is pure Kotlin and depends only on `:core:domain`.
- Features must not depend directly on other features.
- Application modules wire concrete implementations to interfaces.
- Use a version catalog; do not hardcode dependency versions in module build files.
- Use Gradle convention plugins for repeated, non-trivial configuration.

### 8.3 Networking

- Use Ktor Client with platform engines supplied outside the shared client factory.
- Install JSON content negotiation with Kotlinx Serialization.
- Set `Content-Type: application/json` for tutor requests.
- Configure finite request/connect/socket timeouts.
- Inject the configured `HttpClient`; do not create a client per request.
- Map HTTP and transport failures into typed `DataError.Network` values.
- Do not log the learner's full question or response body in release builds.

### 8.4 Dependency injection

Use Koin with one module per layer only where dependencies exist:

- `coreDataModule` provides the `HttpClient` and shared networking configuration.
- `tutorDataModule` binds `KtorTutorRemoteDataSource` to `TutorRemoteDataSource`.
- `tutorPresentationModule` provides `TutorViewModel`.
- Assemble modules at the application boundary.
- Inject the ViewModel only in `TutorRoot`; do not pass it down the composable tree.

Prefer constructor-reference Koin declarations when possible.

### 8.5 Presentation model

Use an MVI-style contract:

- `TutorState`: question, selected mode, selected level, loading status, response UI model, inline validation state, quiz selection, and quiz reveal state.
- `TutorAction`: question changed, mode selected, level selected, submit clicked, retry clicked, quiz choice selected, and check answer clicked.
- `TutorEvent`: one-time effects such as showing an error snackbar. Keep persistent error content in state when retry UI must remain visible.
- `TutorViewModel`: exposes `StateFlow<TutorState>`, handles actions, calls the domain interface, and emits one-time events through a channel/flow.

Update state atomically with `MutableStateFlow.update`. Composables must render state and forward actions; they must not contain business rules, DTO mapping, or network calls.

Use a root/screen split in the same source file:

- `TutorRoot` obtains the ViewModel, collects state with lifecycle awareness, and observes events.
- `TutorScreen` receives only state and an action callback, making it previewable and UI-testable.

## 9. UI and visual requirements

- Use Material 3 components and a small app theme shared by Android and iOS.
- Use a scrollable layout so the keyboard and long responses do not hide content.
- Keep the question controls visible above the response.
- Clearly separate input, response content, hints/steps, follow-up question, and quiz content.
- Provide meaningful empty, loading, success, and error states.
- Make the mock status visible but visually secondary.
- Avoid decorative animation in this milestone. Any progress animation must respect platform accessibility settings.
- Provide meaningful previews for default, loading, explanation, hints, quiz-unanswered, quiz-answered, and error states.

## 10. Accessibility and localization

- All user-facing static text must come from shared/localizable resources.
- Interactive controls must have meaningful accessibility labels or content descriptions.
- Decorative images/icons must not be announced.
- Touch targets must meet platform accessibility sizing expectations.
- Do not rely on color alone to communicate quiz correctness or errors; include text and/or icons.
- Text must remain readable with enlarged system font settings.
- Focus order must follow the visual order.
- Loading and result changes should be announced appropriately to assistive technologies where supported.

English is the only required translation for Milestone 1, but the implementation must be ready for additional locales.

## 11. Testing requirements

### Domain and data tests

- Test DTO-to-domain mapping for explain, hint, and quiz responses.
- Test snake_case JSON serialization and deserialization against representative backend payloads.
- Test valid and invalid quiz index validation.
- Test `KtorTutorRemoteDataSource` with Ktor `MockEngine` for success, `422`, timeout/connectivity failure, `5xx`, and malformed JSON.
- Test that coroutine cancellation is not converted into a generic network error.

### ViewModel tests

Use fakes rather than mocking frameworks where practical. Cover at least:

- Initial default mode and level
- Question editing and trimming behavior
- Client validation boundaries at 2, 3, 2,000, and 2,001 characters
- Loading state and duplicate-submit prevention
- Explain, hint, and quiz success states
- Retry after a failure
- Error mapping to localized UI text
- Quiz choice selection, answer checking, and reset after a new response

Use `kotlinx-coroutines-test`; use Turbine or equivalent deterministic Flow assertions.

### Compose UI tests

Cover the critical user flow:

1. Enter a valid question.
2. Select mode and level.
3. Submit.
4. Observe loading.
5. Render the appropriate response.

Also test validation visibility, failure/retry UI, and quiz answer reveal. Use a robot abstraction if repeated interactions make three or more UI tests clearer.

### Manual verification

- Run the client against the real mocked FastAPI service on an Android emulator.
- Run it against the same service on an iOS Simulator.
- Verify all three modes and all three levels.
- Verify keyboard behavior, long questions, long responses, retry, and server-unavailable behavior.
- Confirm release builds do not contain production secrets or enable unrestricted cleartext traffic.

## 12. Non-functional requirements

- The UI must remain responsive during network operations.
- No expected network or parsing failure may crash the app.
- A request ID from the backend should be retained in the domain response and debug diagnostics, but it does not need to be prominent in the UI.
- No API-provider key or other backend secret may be stored in the client.
- The base URL may be configuration, but it is not a secret.
- Code should favor clear names and small, focused types over generic abstractions.
- Warnings introduced by the client implementation should be resolved rather than suppressed without justification.

## 13. Acceptance criteria

Milestone 1 is complete when:

- The same shared Compose UI runs on Android and iOS.
- A learner can submit a valid question to the mocked FastAPI endpoint.
- Explain, Hint, and Quiz responses render according to their contracts.
- Beginner, Intermediate, and Advanced values are sent correctly.
- Invalid input is stopped locally and backend `422` responses are handled safely.
- Loading prevents duplicate submissions.
- Connectivity, timeout, server, and malformed-response failures display recoverable UI.
- Quiz answers are hidden until checked and evaluated using the zero-based backend index.
- The app does not crash when optional response fields are absent or when a quiz payload is invalid.
- Networking, mapping, ViewModel, and critical Compose flow tests pass.
- The application has been manually verified against the running mock backend on both an Android emulator and an iOS Simulator.

## 14. Future milestones

After this milestone is stable, add features in this order:

1. Local response history and a History screen
2. Type-safe navigation between Tutor and History
3. A real AI-backed FastAPI service while preserving the client contract
4. Image-based questions
5. Accounts and cloud synchronization
6. Streaming responses, quotas, and production observability
