---
name: core-shared-logic
description: >
  Add shared logic to the BeFair core module — code consumed by more than one target
  (mobile AND server), including DTOs/models, constants, pure domain logic, or an
  expect/actual platform abstraction. Use only when both mobile and server need the code.
compatibility: Requires Kotlin Multiplatform and the BeFair core module structure.
metadata:
  author: be-fair
  version: "1.0"
---

## When to use

Only when the logic is genuinely needed by **both** mobile and server. In practice the server
consumes just DTOs, models, constants, and pure domain logic from `core` — no Compose, no Ktor
*client*, no secure storage. If you'd have to write a throwaway `actual` for a target that never
uses the code, it's misplaced → put it in `app/shared` or `server` instead.

## Step-by-step

- [ ] **File**: `core/src/commonMain/kotlin/dev/jakubzika/befair/<package>/<Name>.kt` — always
  `commonMain`. Every file in `core` lives there; its platform source sets are empty by design.
- [ ] **Platform split**: don't. `core` has no `expect`/`actual`, and an abstraction that needs one
  is almost always mobile client code → put it in `app/shared` (`commonMain` + `androidMain`/`iosMain`),
  as `HttpClientFactory` does. Add one here only if the server calls the JVM `actual`.
- [ ] **Constants** go in `core/.../Constants.kt` and are referenced by name — never inline magic values.
- [ ] **Dependencies**: `core`'s `commonMain` carries `kotlinx-serialization-json` and nothing else.
  Adding to it also adds to the server's runtime classpath and Docker image — name the server code
  that needs it first, or put the feature in `app/shared`.
- [ ] **Wire DI**: nothing in `core` is instantiated, so there is no container here. Dependencies go in
  `AppContainer` (`app/shared`).
- [ ] **Test**: prefer `commonTest`. Add platform-specific tests only when behavior differs per platform.
- [ ] **Validate**: `./gradlew :core:jvmTest`
  - All targets compile: `./gradlew :core:compileAndroidMain :core:compileKotlinIosArm64 :core:compileKotlinJvm`
