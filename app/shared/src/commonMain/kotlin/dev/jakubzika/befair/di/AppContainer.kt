package dev.jakubzika.befair.di

import dev.jakubzika.befair.data.local.BeFairDatabase
import dev.jakubzika.befair.data.local.createBeFairDatabase
import dev.jakubzika.befair.data.network.configureBeFair
import dev.jakubzika.befair.data.network.createHttpClient
import dev.jakubzika.befair.data.repository.AuthRepositoryImpl
import dev.jakubzika.befair.data.repository.ItemRepositoryImpl
import dev.jakubzika.befair.data.repository.ProfileRepositoryImpl
import dev.jakubzika.befair.data.repository.SettingsRepositoryImpl
import dev.jakubzika.befair.data.storage.TokenStorage
import dev.jakubzika.befair.data.storage.UserProfileStorage
import dev.jakubzika.befair.domain.repository.AuthRepository
import dev.jakubzika.befair.domain.repository.ItemRepository
import dev.jakubzika.befair.domain.repository.ProfileRepository
import dev.jakubzika.befair.domain.repository.SettingsRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Mobile dependency container. Holds mobile platform use-cases, repositories, and controllers.
 *
 * Everything the app needs is mobile-only — auth, secure token storage, the HTTP client and
 * its engines, the Room cache — so it all lives here. `core` contributes shared DTOs and
 * constants, which need no wiring.
 */
class AppContainer {
    // Secure, platform-backed token store (EncryptedSharedPreferences / Keychain).
    val tokenStorage: TokenStorage by lazy { TokenStorage() }

    // Platform-backed user profile storage (EncryptedSharedPreferences / UserDefaults / Preferences).
    val userProfileStorage: UserProfileStorage by lazy { UserProfileStorage() }

    // Client carrying JSON + bearer auth (with transparent refresh) for authenticated calls.
    private val authHttpClient: HttpClient by lazy {
        createHttpClient { configureBeFair(tokenStorage) }
    }

    // Unconfigured client for calls that carry no credentials.
    private val httpClient: HttpClient by lazy { createHttpClient() }

    // On-device Room cache; the server stays the source of truth.
    val database: BeFairDatabase by lazy { createBeFairDatabase() }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            authHttpClient, tokenStorage, userProfileStorage, database.itemDao(), database.settingsDao(),
        ) {
            // Prefetch items and settings once the user is signed in; failures fall back to the cache.
            itemRepository.refresh()
            settingsRepository.sync()
        }
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(httpClient)
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Display preferences (currency label, tool cost basis): cached in Room, synced with the server.
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(authHttpClient, database.settingsDao(), appScope)
    }

    val itemRepository: ItemRepository by lazy {
        ItemRepositoryImpl(authHttpClient, database.itemDao(), appScope)
    }
}
