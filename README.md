# AI Study Coach — KMP client

KMP-01 foundation: Android and iOS launch the same localized Compose Multiplatform
welcome screen. KMP-02 adds typed domain models, results, errors and the remote
data-source contract. KMP-03 adds shared networking and platform configuration. Tutor input, Koin and
MVI follow in later cards. The backend is not needed to run this scaffold.

## Requirements

- JDK 21 (set `JAVA_HOME`; the default system JDK may be too new for Gradle).
- Android SDK platform 36, with `sdk.dir` in an untracked `local.properties`
  or `ANDROID_HOME` set.
- macOS with Xcode and an installed iOS Simulator runtime for iOS builds.
- Internet access on the first build to resolve the pinned dependencies.

The Gradle wrapper pins Gradle 9.1.0. Dependency and plugin versions live in
`gradle/libs.versions.toml`.

## Android

Open this directory in Android Studio, sync, and run the **androidApp** configuration
on an emulator or device (Android 7 / API 24 or newer).

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 21) # macOS
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

## iOS

Open `iosApp/iosApp.xcodeproj`, choose the shared **iosApp** scheme and an iPhone
Simulator, then Run. The build phase compiles and embeds the Kotlin framework.
For a physical iPhone, select your own signing team in Xcode. The deployment
target is iOS 18.5 (matching the bundled native ICU library); shared native targets are `iosArm64` and `iosSimulatorArm64`
(Apple Silicon simulator).

```sh
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -configuration Debug -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath build/ios CODE_SIGNING_ALLOWED=NO build
```

The Xcode script resolves JDK 21 when `JAVA_HOME` is unset. Kotlin framework builds
need access to Gradle caches, so user-script sandboxing is disabled for this build
phase, as required by direct Kotlin/Xcode integration.

## Modules and boundaries

| Module | Responsibility |
| --- | --- |
| `androidApp` | Thin Android launcher and APK configuration |
| `composeApp` | Shared application assembly, `App`, and iOS framework entry point |
| `core:domain` | Framework-free typed results and network errors |
| `core:data` | Ktor client factory, safe calls and validated server configuration |
| `core:presentation` | Future shared presentation utilities |
| `core:design-system` | Shared Material 3 theme |
| `feature:tutor:domain` | Tutor request/response models, response validation and data-source interface |
| `feature:tutor:data` | Serializable DTOs, validated mappers and Ktor remote source |
| `feature:tutor:presentation` | Shared welcome screen and localized resources; later Tutor UI |
| `build-logic` (included build) | KMP, Compose and Android application conventions |
| `iosApp` (Xcode) | Thin SwiftUI host for the shared Compose controller |

`androidApp` is an intentional addition to the requirements' module list:
AGP 9's supported KMP library plugin requires the Android application to be a
separate module. `composeApp` remains the shared application boundary. Build logic
is an included Gradle build, not a runtime module.

Domain source code stays pure Kotlin, despite publishing Android-compatible and
iOS variants. Presentation depends on feature domain and core UI modules; data
depends on domain/core data. Neither presentation nor domain can depend on feature
data. There is no repository abstraction or navigation scaffold in this milestone.

```sh
./gradlew verifyModuleBoundaries
```

This task rejects forbidden project dependencies. Keep implementation-specific
libraries out of domain code and add dependencies only as subsequent tasks need them.

## Scope and verification

KMP-01 is a launchable foundation, not the completed tutor feature. Consult
`REQUIREMENTS.md` and the [KMP board](https://github.com/users/developer-havlicektomas-dev/projects/4)
for the remaining work. There are no provider secrets. Network access is configured by KMP-03; HTTP
allowances exist only in debug builds.

### KMP-01 verification — 2026-09-18

- Android debug APK assembled; shared welcome screen launched and visually checked
  on the Pixel_10a emulator.
- Simulator framework linked; the full Xcode app built and displayed the same
  screen on the iPhone 18 Pro simulator (iOS 27.0).
- iPhone arm64 source compilation and the module-boundary check passed.
- Empty core/feature data modules compiled for Android.
- Android lint completed with **0 errors and 8 advisories**, all concerning newer
  tool/dependency releases or target SDK 36. No lint rules are suppressed. The
  scaffold pins a verified AGP 9.0.1 / Gradle 9.1.0 / Compose 1.11.1 combination;
  upgrading to target SDK 37 should be done together with AGP and Compose and
  followed by both platform launch checks.
- Xcode emits its standard App Intents metadata notice because this app does not
  use App Intents. No app source/compiler errors remain.

Physical-device signing and launch were not tested. The iOS deployment floor is
18.5 to match the native ICU object bundled with this Compose runtime; lowering
it requires selecting and testing a compatible native dependency set.

### KMP-02 verification — 2026-09-19

- Added framework-free tutor models, the suspend remote-source interface and shared
  typed results/errors. `TutorResponse.validated()` rejects missing quiz payloads
  and invalid zero-based answer indices without throwing.
- All 10 shared domain tests passed on both Android's host test runner and the
  iOS Simulator (20 successful executions). iPhone arm64 compilation and module
  boundary verification passed. No new external dependencies were needed.
- Networking implementations, JSON DTOs and presentation wiring remain in the
  following cards. This step does not change the welcome screen.

```sh
./gradlew :core:domain:allTests :feature:tutor:domain:allTests \
  :feature:tutor:domain:compileKotlinIosArm64 verifyModuleBoundaries
```

## Networking configuration (KMP-03)

One reusable client per application uses OkHttp on Android and Darwin on iOS.
The shared factory accepts its engine externally for testing. `StudyCoachApplication.httpClient`
and `IosNetworking.client` own lazy application-scoped instances; KMP-05 will wire
these into Koin. Networking is not called by the welcome screen yet.

| Build | Default / override |
| --- | --- |
| Android debug | `http://10.0.2.2:8000`; override with `-PstudyCoach.devApiUrl=http://192.168.1.20:8000` |
| iOS debug | `http://127.0.0.1:8000`; override Xcode's `STUDY_COACH_API_URL` build setting |
| Android release | Must supply `-PstudyCoach.apiUrl=https://your-api-host` |
| iOS release | Must supply `STUDY_COACH_API_URL=https://your-api-host` |

Release builds intentionally fail with the placeholder address until a production
HTTPS endpoint is supplied. Configuration rejects credentials, query strings and
fragments. The shared client blocks non-HTTPS release requests even if a caller
passes an absolute URL, and does not follow redirects. Debug Android permits HTTP;
debug iOS allows local networking only. Release manifests/plists omit development
allowances. No request/response logging plugin is installed in either build.

For a physical device, set the debug URL to the development machine's reachable
LAN address and run the existing FastAPI service on `0.0.0.0:8000`. Keep the phone
and development machine on the same network. iOS may ask for local-network access.
Use an HTTPS server for non-local iOS debug endpoints.

Requests use JSON and response parsing tolerates unknown fields. Use relative
routes such as `v1/tutor/respond` (without a leading slash) to retain any configured
base path. Inject the existing client into data sources and call
`safeCall<ResponseDto> { client.post("v1/tutor/respond") { setBody(dto) } }`.
The helper handles execution and decoding, maps HTTP/transport/parsing failures,
and propagates coroutine cancellation. The KMP-04 mapper handles semantic response
validation separately.

Request and socket timeouts are 30 seconds; connection timeout is 10 seconds on
supported engines. Darwin does not support Ktor's independent connection timeout;
the overall 30-second request deadline still bounds connection attempts. See
[Ktor's timeout support](https://ktor.io/docs/client-timeout.html).

```sh
./gradlew :core:data:allTests :androidApp:assembleDebug \
  :composeApp:compileKotlinIosArm64 verifyModuleBoundaries
```

### KMP-03 verification — 2026-09-20

- Nine networking tests passed on Android host and iOS Simulator (18 executions):
  JSON request/response, base-path handling, HTTP status errors, malformed JSON,
  unexpected content type, transport/timeout/unknown failures, cancellation,
  URL validation and blocking explicit HTTP requests in release clients.
- Android debug APK and full Xcode simulator app built; iPhone arm64 sources and
  module-boundary checks passed. Built iOS plist contains the simulator URL and
  the debug-only local networking allowance.
- Android merged release manifest disables cleartext. Android and iOS release
  validation both rejected an HTTP URL as expected.
- Android lint: zero errors; 14 tool/version/target-SDK advisories. Ktor 3.5.2 is
  pinned to the version verified across both platforms here; 3.6.0 is available.
  No source/compiler warnings remain; no lint rules were suppressed.
- Live backend requests from the app remain for KMP-04/05 wiring and KMP-13
  integration. No production endpoint, release signing or physical device was used.

## Tutor remote data source (KMP-04)

The tutor data module now implements `TutorRemoteDataSource` with the configured
Ktor client. Requests contain exactly the three API fields; response DTOs map to
separate domain models, preserving optional data and rejecting invalid quizzes.
The serialization compiler plugin is supplied through `study.serialization` in
build-logic to share the Kotlin plugin classloader with the other conventions.
See `feature/tutor/data/README.md` for the contract and test command.

This step leaves the welcome screen unchanged. KMP-05 will bind the remote source
and ViewModel; full live backend verification remains part of KMP-13.

### KMP-04 verification — 2026-09-29

- All 10 shared contract tests passed on Android host and iOS Simulator (20 executions).
- iPhone arm64 compilation and module-boundary verification passed.
- No source/compiler warnings or whitespace errors were reported.
- Work is local on `kmp-04`; no app/backend integration run or push was performed.
