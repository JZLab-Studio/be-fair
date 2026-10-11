package dev.jakubzika.befair.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import dev.jakubzika.befair.di.AppContainer
import dev.jakubzika.befair.domain.model.AppSettings

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided")
}

/**
 * The live display preferences, provided once in `App.kt` from the settings repository. Every
 * price goes through `formatMoneyCents` / `formatMoneyWhole` with this value. Defaults keep
 * previews and tests working without a provider.
 */
val LocalAppSettings = compositionLocalOf { AppSettings() }
