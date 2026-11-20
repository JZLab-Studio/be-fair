package dev.jakubzika.befair.ui.atoms

import androidx.compose.runtime.Composable
import dev.jakubzika.befair.config.AppConfig

/**
 * Renders [content] only in DEV mode ([AppConfig.isDev]). Emits nothing in PROD.
 * Adds no styling of its own; [content] should use `MaterialTheme.*` tokens.
 */
@Composable
fun DevOnly(content: @Composable () -> Unit) {
    if (AppConfig.isDev) content()
}
