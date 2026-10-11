package dev.jakubzika.befair.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_settings_back
import be_fair.app.shared.generated.resources.screen_settings_basis_day_meta
import be_fair.app.shared.generated.resources.screen_settings_basis_month_meta
import be_fair.app.shared.generated.resources.screen_settings_basis_section
import be_fair.app.shared.generated.resources.screen_settings_basis_week_meta
import be_fair.app.shared.generated.resources.screen_settings_basis_year_meta
import be_fair.app.shared.generated.resources.screen_settings_currency_custom
import be_fair.app.shared.generated.resources.screen_settings_currency_custom_meta_empty
import be_fair.app.shared.generated.resources.screen_settings_currency_eur
import be_fair.app.shared.generated.resources.screen_settings_currency_hint
import be_fair.app.shared.generated.resources.screen_settings_currency_section
import be_fair.app.shared.generated.resources.screen_settings_currency_usd
import be_fair.app.shared.generated.resources.screen_settings_custom_error
import be_fair.app.shared.generated.resources.screen_settings_custom_label
import be_fair.app.shared.generated.resources.screen_settings_custom_placeholder
import be_fair.app.shared.generated.resources.screen_settings_footer
import be_fair.app.shared.generated.resources.screen_settings_title
import dev.jakubzika.befair.domain.model.AppSettings
import dev.jakubzika.befair.domain.model.CurrencyChoice
import dev.jakubzika.befair.domain.model.CustomCurrencyMaxLength
import dev.jakubzika.befair.domain.model.ToolBasis
import dev.jakubzika.befair.ui.adjective
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTextField
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors
import dev.jakubzika.befair.ui.molecules.ChoiceList
import dev.jakubzika.befair.ui.molecules.ChoiceOption
import dev.jakubzika.befair.util.formatMoneyCents
import org.jetbrains.compose.resources.stringResource

private val HeaderHeight = 56.dp
private val HitTarget = 44.dp

/** Sample amount (€12.50) used to show how each currency labels a price. */
private const val ExampleCents = 1250L

// Settings: fixed header with back (no tab bar), then a scrolling column — the currency choice
// list, the custom-name field while Custom is selected, and the tool cost basis list with a live
// preview. No accent color: selection is the ink radio dot alone; orange is the custom-name error.
@Composable
fun SettingsTemplate(
    settings: AppSettings,
    basisPreview: String?,
    onSelectCurrency: (CurrencyChoice) -> Unit,
    onCustomCurrencyChange: (String) -> Unit,
    onSelectBasis: (ToolBasis) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var customName by remember { mutableStateOf(settings.customCurrency) }
    var touched by remember { mutableStateOf(false) }
    val ink3 = LocalBeFairExtendedColors.current.ink3
    val currencyTitle = stringResource(Res.string.screen_settings_currency_section)
    val basisTitle = stringResource(Res.string.screen_settings_basis_section)

    val customMeta = if (customName.isBlank()) {
        stringResource(Res.string.screen_settings_currency_custom_meta_empty)
    } else {
        formatMoneyCents(ExampleCents, AppSettings(CurrencyChoice.CUSTOM, customName))
    }
    val currencyOptions = listOf(
        ChoiceOption(
            CurrencyChoice.EUR,
            stringResource(Res.string.screen_settings_currency_eur),
            formatMoneyCents(ExampleCents, AppSettings(CurrencyChoice.EUR))
        ),
        ChoiceOption(
            CurrencyChoice.USD,
            stringResource(Res.string.screen_settings_currency_usd),
            formatMoneyCents(ExampleCents, AppSettings(CurrencyChoice.USD))
        ),
        ChoiceOption(CurrencyChoice.CUSTOM, stringResource(Res.string.screen_settings_currency_custom), customMeta),
    )
    val basisOptions = listOf(
        ChoiceOption(ToolBasis.DAY, ToolBasis.DAY.adjective(), stringResource(Res.string.screen_settings_basis_day_meta)),
        ChoiceOption(ToolBasis.WEEK, ToolBasis.WEEK.adjective(), stringResource(Res.string.screen_settings_basis_week_meta)),
        ChoiceOption(ToolBasis.MONTH, ToolBasis.MONTH.adjective(), stringResource(Res.string.screen_settings_basis_month_meta)),
        ChoiceOption(ToolBasis.YEAR, ToolBasis.YEAR.adjective(), stringResource(Res.string.screen_settings_basis_year_meta)),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HeaderHeight)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = BeFairDimension.Spacing.sm)
                    .size(HitTarget)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.screen_settings_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = stringResource(Res.string.screen_settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BeFairDimension.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.sm)
        ) {
            SectionHead(currencyTitle)
            ChoiceList(
                options = currencyOptions,
                selected = settings.currency,
                onSelect = {
                    touched = false
                    onSelectCurrency(it)
                },
                groupDescription = currencyTitle
            )

            if (settings.currency == CurrencyChoice.CUSTOM) {
                BeFairTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = customName,
                    onValueChange = {
                        val trimmed = it.take(CustomCurrencyMaxLength)
                        customName = trimmed
                        touched = true
                        onCustomCurrencyChange(trimmed)
                    },
                    label = stringResource(Res.string.screen_settings_custom_label),
                    placeholder = stringResource(Res.string.screen_settings_custom_placeholder),
                    isError = touched && customName.isBlank(),
                    errorMessage = stringResource(Res.string.screen_settings_custom_error)
                )
            }

            Hint(stringResource(Res.string.screen_settings_currency_hint), ink3)

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))
            SectionHead(basisTitle)
            ChoiceList(
                options = basisOptions,
                selected = settings.toolBasis,
                onSelect = onSelectBasis,
                groupDescription = basisTitle
            )
            if (basisPreview != null) Hint(basisPreview, ink3)

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.sm))
            Hint(stringResource(Res.string.screen_settings_footer), ink3)
        }
    }
}

@Composable
private fun SectionHead(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Hint(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color
    )
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun SettingsTemplatePreview() {
    BeFairTheme {
        SettingsTemplate(
            settings = AppSettings(),
            basisPreview = "Cordless drill: €4.37 per month",
            onSelectCurrency = {},
            onCustomCurrencyChange = {},
            onSelectBasis = {},
            onBack = {}
        )
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun SettingsTemplateCustomPreview() {
    BeFairTheme {
        SettingsTemplate(
            settings = AppSettings(CurrencyChoice.CUSTOM, "CHF", ToolBasis.WEEK),
            basisPreview = null,
            onSelectCurrency = {},
            onCustomCurrencyChange = {},
            onSelectBasis = {},
            onBack = {}
        )
    }
}
