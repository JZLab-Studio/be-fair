package dev.jakubzika.befair.config

import dev.jakubzika.befair.data.network.DEV_API_HOST

/**
 * Single source of truth for the app mode. The host (Android/iOS) calls [initialize] once at
 * startup, before any `AppContainer` exists. Reads before [initialize] report [AppMode.PROD].
 */
object AppConfig {
    private var mode: AppMode? = null

    fun initialize(mode: AppMode) {
        val current = this.mode
        check(current == null || current == mode) { "AppConfig already initialized as $current" }
        this.mode = mode
    }

    val appMode: AppMode get() = mode ?: AppMode.PROD

    val isDev: Boolean get() = appMode == AppMode.DEV

    val apiBaseUrl: String
        get() = resolveApiBaseUrl(appMode, DEV_API_HOST, GeneratedBuildConfig.PRODUCTION_URL)

    internal fun resetForTest() {
        mode = null
    }
}
