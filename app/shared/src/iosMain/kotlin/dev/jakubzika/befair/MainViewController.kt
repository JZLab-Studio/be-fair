package dev.jakubzika.befair

import androidx.compose.ui.window.ComposeUIViewController
import dev.jakubzika.befair.config.AppConfig
import dev.jakubzika.befair.config.AppMode

/** Initializes [AppConfig] from the host's `BeFairAppMode` value before `App()` builds the container. */
fun MainViewController(appMode: String?) = run {
    AppConfig.initialize(AppMode.parse(appMode))
    ComposeUIViewController { App() }
}
