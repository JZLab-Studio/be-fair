package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_profile_currency_custom
import be_fair.app.shared.generated.resources.screen_profile_currency_eur
import be_fair.app.shared.generated.resources.screen_profile_currency_usd
import be_fair.app.shared.generated.resources.screen_profile_settings_summary
import dev.jakubzika.befair.domain.model.CurrencyChoice
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.LocalAppSettings
import dev.jakubzika.befair.ui.per
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

    val settings = LocalAppSettings.current
    val currencyName = when (settings.currency) {
        CurrencyChoice.EUR -> stringResource(Res.string.screen_profile_currency_eur)
        CurrencyChoice.USD -> stringResource(Res.string.screen_profile_currency_usd)
        CurrencyChoice.CUSTOM -> stringResource(Res.string.screen_profile_currency_custom, settings.currencyLabel)
    }
    val summary = stringResource(
        Res.string.screen_profile_settings_summary,
        currencyName,
        settings.toolBasis.per(),
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
