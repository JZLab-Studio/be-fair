package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_profile_currency_default
import be_fair.app.shared.generated.resources.screen_profile_settings_summary
import be_fair.app.shared.generated.resources.screen_profile_tool_basis_default
import dev.jakubzika.befair.ui.LocalAppContainer
import org.jetbrains.compose.resources.stringResource
import dev.jakubzika.befair.ui.templates.ProfileTemplate
import kotlinx.coroutines.launch

/** Profile / settings screen. Name and email come from the on-device profile cache. */
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onOpenSettings: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val name = remember { container.userProfileStorage.getDisplayName().orEmpty() }
    val email = remember { container.userProfileStorage.getEmail().orEmpty() }

    // No settings store exists yet; the summary uses the defaults until Settings lands.
    val summary = stringResource(
        Res.string.screen_profile_settings_summary,
        stringResource(Res.string.screen_profile_currency_default),
        stringResource(Res.string.screen_profile_tool_basis_default),
    )

    ProfileTemplate(
        name = name,
        email = email,
        settingsSummary = summary,
        onOpenSettings = onOpenSettings,
        onSignOut = {
            scope.launch {
                container.authRepository.logout()
                onSignOut()
            }
        },
    )
}
