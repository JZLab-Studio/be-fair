package dev.jakubzika.befair.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_overview_empty_data_stays_on_device
import be_fair.app.shared.generated.resources.screen_overview_empty_description
import be_fair.app.shared.generated.resources.screen_overview_empty_how_it_works
import be_fair.app.shared.generated.resources.screen_overview_empty_step_1_description
import be_fair.app.shared.generated.resources.screen_overview_empty_step_1_title
import be_fair.app.shared.generated.resources.screen_overview_empty_step_2_description
import be_fair.app.shared.generated.resources.screen_overview_empty_step_2_title
import be_fair.app.shared.generated.resources.screen_overview_empty_step_3_description
import be_fair.app.shared.generated.resources.screen_overview_empty_step_3_title
import be_fair.app.shared.generated.resources.screen_overview_empty_welcome
import be_fair.app.shared.generated.resources.screen_overview_empty_welcome_no_name
import be_fair.app.shared.generated.resources.screen_overview_title
import dev.jakubzika.befair.ui.LocalAppSettings
import dev.jakubzika.befair.ui.per
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.LocalBeFairExtendedColors
import dev.jakubzika.befair.ui.organisms.EmptyStateCard
import dev.jakubzika.befair.ui.organisms.HowItWorksSection
import dev.jakubzika.befair.ui.organisms.HowItWorksStep
import org.jetbrains.compose.resources.stringResource

private val HeaderHeight = 56.dp

// Overview screen in the "no data" state per DESIGN.md Components > "Empty
// state (no data)": fixed header, the empty-state card, the "How it works"
// list, and a closing caption. No stats/activity are zero-filled — they are
// simply absent until the user has real data to measure.
@Composable
fun OverviewEmptyTemplate(
    userName: String,
    appName: String,
    onAddFirstItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.screen_overview_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BeFairDimension.Spacing.md)
        ) {
            EmptyStateCard(
                welcomeText = if (userName.isBlank()) {
                    stringResource(Res.string.screen_overview_empty_welcome_no_name)
                } else {
                    stringResource(Res.string.screen_overview_empty_welcome, userName)
                },
                description = stringResource(Res.string.screen_overview_empty_description, appName),
                onAddFirstItem = onAddFirstItem
            )

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.lg))

            HowItWorksSection(
                headerTitle = stringResource(Res.string.screen_overview_empty_how_it_works),
                steps = listOf(
                    HowItWorksStep(
                        title = stringResource(Res.string.screen_overview_empty_step_1_title),
                        description = stringResource(Res.string.screen_overview_empty_step_1_description)
                    ),
                    HowItWorksStep(
                        title = stringResource(Res.string.screen_overview_empty_step_2_title),
                        description = stringResource(Res.string.screen_overview_empty_step_2_description)
                    ),
                    HowItWorksStep(
                        title = stringResource(Res.string.screen_overview_empty_step_3_title),
                        description = stringResource(
                            Res.string.screen_overview_empty_step_3_description,
                            LocalAppSettings.current.toolBasis.per()
                        )
                    )
                )
            )

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.lg))

            Text(
                text = stringResource(Res.string.screen_overview_empty_data_stays_on_device),
                style = MaterialTheme.typography.labelMedium,
                color = LocalBeFairExtendedColors.current.ink3,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(BeFairDimension.Spacing.lg))
        }
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun OverviewEmptyTemplatePreview() {
    BeFairTheme {
        OverviewEmptyTemplate(
            userName = "Anna",
            appName = "BeFair",
            onAddFirstItem = {}
        )
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun OverviewEmptyTemplateNoNamePreview() {
    BeFairTheme {
        OverviewEmptyTemplate(
            userName = "",
            appName = "BeFair",
            onAddFirstItem = {}
        )
    }
}
