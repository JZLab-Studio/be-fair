package dev.jakubzika.befair.di

import dev.jakubzika.befair.data.local.BeFairDatabase
import dev.jakubzika.befair.data.local.createBeFairDatabase
import dev.jakubzika.befair.data.network.configureBeFair
import dev.jakubzika.befair.data.network.createHttpClient
import dev.jakubzika.befair.data.repository.AuthRepositoryImpl
import dev.jakubzika.befair.data.repository.ItemRepositoryImpl
import dev.jakubzika.befair.data.repository.ProfileRepositoryImpl
import dev.jakubzika.befair.data.repository.SettingsRepositoryImpl
import dev.jakubzika.befair.data.storage.SettingsStorage
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
 * Mobile-specific dependency container. Holds mobile platform use-cases, repositories,
 * and controllers on top of the shared [CoreContainer].
 *
 * Auth, secure token storage, and the auth-configured HTTP client are mobile-only and so
 * live here rather than in `core`.
 */
class AppContainer(
    val coreContainer: CoreContainer = CoreContainer()
) {
    // Secure, platform-backed token store (EncryptedSharedPreferences / Keychain).
    val tokenStorage: TokenStorage by lazy { TokenStorage() }

    // Platform-backed user profile storage (EncryptedSharedPreferences / UserDefaults / Preferences).
    val userProfileStorage: UserProfileStorage by lazy { UserProfileStorage() }

    // Client carrying JSON + bearer auth (with transparent refresh) for authenticated calls.
    private val authHttpClient: HttpClient by lazy {
        createHttpClient { configureBeFair(tokenStorage) }
    }

    // On-device Room cache; the server stays the source of truth.
    val database: BeFairDatabase by lazy { createBeFairDatabase() }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authHttpClient, tokenStorage, userProfileStorage, database.itemDao()) {
            // Prefetch items once the user is signed in; failures fall back to the cached list.
            itemRepository.refresh()
        }
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(coreContainer.httpClient)
    }

    // On-device display preferences (currency label, tool cost basis).
    val settingsStorage: SettingsStorage by lazy { SettingsStorage() }

    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(settingsStorage) }

    private val appScope =CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val itemRepository: ItemRepository by lazy {
        ItemRepositoryImpl(authHttpClient, database.itemDao(), appScope)
    }
}
