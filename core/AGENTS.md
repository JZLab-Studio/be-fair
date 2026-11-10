# AGENTS.md — core module

Scoped guidance for the `core` module.
Read the root [`AGENTS.md`](../AGENTS.md) first for project-wide rules.

## Purpose

`core` is the **shared foundation** for all platforms (Android, iOS, JVM/server).
Place logic here only when it is genuinely needed by more than one target.
Mobile-only domain/data belongs in `app/shared`; server-only code belongs in `server`.

**Does NOT belong in `core`** (put it in `app/shared`): secure token storage, Ktor *client* auth/refresh config, anything using `EncryptedSharedPreferences`/Keychain, Compose UI, and any client-only repository. A reliable smell: if a `core` target needs a throwaway `actual` that nothing consumes (e.g. a JVM stub for the server), the code is misplaced — move it to `app/shared`.

## Package Layout

```
core/src/
  commonMain/kotlin/dev/jakubzika/befair/
    domain/model/
      AuthModels.kt             ← @Serializable auth DTOs
      ItemModels.kt             ← @Serializable item DTOs + pure stats logic
      SettingsModels.kt         ← @Serializable settings DTOs + option enums
    Constants.kt                ← shared constants (SERVER_PORT, etc.)
  commonTest/…                  ← shared tests
```

**Every file in `core` is in `commonMain`.** The `android`, `ios*`, and `jvm` targets exist
only so the module is *consumable* by `app/shared` (Android + iOS) and `server` (JVM) — they
hold no source of their own.

## No Dependency Container Here

`core` has no DI container, because nothing in it is instantiated — DTOs, enums, and constants
need no wiring. `AppContainer` in `app/shared` is the project's only container.

If you are tempted to add a container or a service to `core`, check who constructs it. If the
answer is "the app", it belongs in `AppContainer`.

## Dependencies: Keep This Module Thin

`commonMain` depends on **`kotlinx-serialization-json` and nothing else**. Treat that as a
budget, not a coincidence:

- `:server` consumes `:core`, so every dependency added here lands on the server's
  `runtimeClasspath` and inside its Docker image, whether or not the server calls it.
- Before adding a dependency, name the server code that needs it. If there is none, the
  feature belongs in `app/shared`.

This is why the Ktor client lives in `app/shared`: the engines (`okhttp`, `darwin`) are
mobile-only, and hosting them here shipped `ktor-client-cio` to the server for nothing.

## expect / actual Pattern — Prefer `app/shared`

`core` currently has **no** `expect`/`actual` declarations, and that is the goal state. An
abstraction needing a per-platform implementation is almost always platform *client* code, so
it belongs in `app/shared` (`commonMain` + `androidMain`/`iosMain`) — see
`app/shared/.../data/network/HttpClientFactory.kt`.

Only add one here if the **server** genuinely calls it, which means writing a real JVM `actual`
the server invokes. A JVM `actual` with no caller is the signal the code is misplaced.

## Constants

Add project-wide constants to `Constants.kt`. Reference them by name everywhere — never inline magic numbers or strings.

## Build & Test

```shell
# Unit tests (runs on JVM)
./gradlew :core:jvmTest

# Check compilation for all targets
./gradlew :core:compileAndroidMain :core:compileKotlinIosArm64 :core:compileKotlinJvm
```
