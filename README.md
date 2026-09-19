# AI Study Coach — KMP client

KMP-01 foundation: Android and iOS launch the same localized Compose Multiplatform
welcome screen. KMP-02 adds typed domain models, results, errors and the remote
data-source contract. Tutor input, networking, Koin and MVI follow in later cards. The backend is not needed to run this scaffold.

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
| `core:data` | Future shared networking infrastructure |
| `core:presentation` | Future shared presentation utilities |
| `core:design-system` | Shared Material 3 theme |
| `feature:tutor:domain` | Tutor request/response models, response validation and data-source interface |
| `feature:tutor:data` | Future transport DTOs, mappers and remote implementation |
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
for the remaining work. There are no provider secrets, network permissions or
cleartext-network exceptions in the scaffold.

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
