package dev.jakubzika.befair.config

import dev.jakubzika.befair.SERVER_PORT

/**
 * Resolves the backend base URL for [mode].
 *
 * DEV targets the developer machine over plain HTTP; PROD requires an `https://` [productionUrl]
 * and returns it without trailing slashes.
 */
fun resolveApiBaseUrl(mode: AppMode, devHost: String, productionUrl: String): String =
    when (mode) {
        AppMode.DEV -> "http://$devHost:$SERVER_PORT"
        AppMode.PROD -> {
            check(productionUrl.startsWith("https://")) { "Production URL is not configured" }
            productionUrl.trimEnd('/')
        }
    }
