package dev.jakubzika.befair.data.network

import dev.jakubzika.befair.config.AppConfig

/**
 * Host the app uses to reach a backend running on the developer machine in DEV mode:
 *  - Android emulator: `10.0.2.2` is the host loopback alias.
 *  - iOS simulator: `localhost` reaches the host directly.
 */
expect val DEV_API_HOST: String

/** Base URL of the Ktor backend, resolved from the active [AppConfig] mode. */
val API_BASE_URL: String get() = AppConfig.apiBaseUrl
