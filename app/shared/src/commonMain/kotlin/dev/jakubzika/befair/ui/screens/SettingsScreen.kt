package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_settings_basis_preview
import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.domain.model.primaryCostCents
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.LocalAppSettings
import dev.jakubzika.befair.ui.per
import dev.jakubzika.befair.ui.templates.SettingsTemplate
import dev.jakubzika.befair.util.formatMoneyCents
import dev.jakubzika.befair.util.todayEpochDay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/** Settings: changes apply and save immediately — there is no Save button. */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val repository = container.settingsRepository
    val scope = rememberCoroutineScope()
    val settings = LocalAppSettings.current
    val items by container.itemRepository.items.collectAsState()

    // The preview uses the first tool; with no tools it is hidden.
    val firstTool = remember(items) { items.firstOrNull { it.kind == ItemKind.TOOL } }
    val preview = firstTool?.let { tool ->
        stringResource(
            Res.string.screen_settings_basis_preview,
            tool.name,
            formatMoneyCents(tool.primaryCostCents(settings.toolBasis, todayEpochDay()) ?: 0L, settings),
            settings.toolBasis.per()
        )
    }

    SettingsTemplate(
        settings = settings,
        basisPreview = preview,
        onSelectCurrency = { currency -> scope.launch { repository.update { it.copy(currency = currency) } } },
        onCustomCurrencyChange = { name -> scope.launch { repository.update { it.copy(customCurrency = name) } } },
        onSelectBasis = { basis -> scope.launch { repository.update { it.copy(toolBasis = basis) } } },
        onBack = onBack
    )
}
