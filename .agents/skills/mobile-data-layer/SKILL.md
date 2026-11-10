---
name: mobile-data-layer
description: >
  Add a mobile domain model, repository, or use-case to the BeFair app/shared module
  following Clean Architecture and manual DI. Use when adding a new tracked entity
  (e.g. ClothingItem, Tool, WashEvent), a new data source, or business logic that
  orchestrates repositories — anything in the mobile domain/data layers below the UI.
compatibility: Requires Kotlin Multiplatform and the BeFair app/shared module structure.
metadata:
  author: be-fair
  version: "1.0"
---

## When to use

- Adding a new domain model + repository for a tracked entity.
- Adding a use-case that enforces domain rules or orchestrates repositories.

Everything here lives in `app/shared` (mobile-only). If the server will also consume it,
stop and use the `core-shared-logic` skill instead — only genuinely multi-target code goes in `core`.

## Recipe A — Domain model + repository

- [ ] **Model**: `app/shared/src/commonMain/kotlin/dev/jakubzika/befair/domain/model/<Name>.kt`
  - Plain `data class`, no Android/iOS imports. Nullable fields use `= null` defaults.
- [ ] **Repository interface**: `app/shared/.../domain/repository/<Name>Repository.kt`
  - Observable state: `val items: StateFlow<List<Name>>`.
  - Mutations: `suspend fun refresh()`, `suspend fun add(item: Name)`, etc.
- [ ] **Repository implementation**: `app/shared/.../data/repository/<Name>RepositoryImpl.kt`
  - Constructor-inject `HttpClient` only — take one of `AppContainer`'s clients, never construct it.
    Use `authHttpClient` for anything behind a bearer token; `httpClient` only for unauthenticated calls.
  - Back state with `private val _items = MutableStateFlow<List<Name>>(emptyList())`.
  - Expose as `override val items = _items.asStateFlow()`. Never return raw values from a repository.
- [ ] **Wire DI** in `AppContainer` (`app/shared`):
  ```kotlin
  val <name>Repository: <Name>Repository by lazy {
      <Name>RepositoryImpl(authHttpClient)
  }
  ```
- [ ] **Validate**: `./gradlew :app:shared:testDebugUnitTest`

## Recipe B — Use-case

For business logic that orchestrates multiple repositories or enforces domain rules.

- [ ] Create `app/shared/.../domain/usecase/` if it doesn't exist yet.
- [ ] **Use-case**: `domain/usecase/<Name>UseCase.kt`
  - Constructor-inject only the repositories it needs.
  - Single entry point: `suspend operator fun invoke(…): Result`.
  - No UI imports, no `HttpClient` directly — delegate to repositories.
- [ ] **Wire DI** in `AppContainer` (`app/shared`):
  ```kotlin
  val <name>UseCase: <Name>UseCase by lazy {
      <Name>UseCase(<name>Repository)
  }
  ```
- [ ] **Validate**: `./gradlew :app:shared:testDebugUnitTest`
