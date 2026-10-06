package dev.jakubzika.befair.ui.templates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.screen_profile_account
import be_fair.app.shared.generated.resources.screen_profile_preferences
import be_fair.app.shared.generated.resources.screen_profile_settings
import be_fair.app.shared.generated.resources.screen_profile_sign_out
import be_fair.app.shared.generated.resources.screen_profile_title
import dev.jakubzika.befair.ui.atoms.BeFairDimension
import dev.jakubzika.befair.ui.atoms.BeFairTheme
import dev.jakubzika.befair.ui.atoms.DestructiveButton
import org.jetbrains.compose.resources.stringResource

private val HeaderHeight = 56.dp
private val RowHeight = 56.dp
private val ChevronSize = 20.dp
private val AvatarSize = 56.dp
private val CardShape = RoundedCornerShape(BeFairDimension.Radius.small)

private const val MaxInitials = 2

/** First letters of the first [MaxInitials] words of [name], uppercased. */
internal fun initialsOf(name: String): String =
    name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        .map { it.first() }
        .joinToString("")
        .take(MaxInitials)
        .uppercase()

// Profile: fixed header (no back), then a scrolling column — profile card, a Settings row
// summarising the live preferences, and the account section. Orange is the sign-out button only.
@Composable
fun ProfileTemplate(
    name: String,
    email: String,
    settingsSummary: String,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit,
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
                .heightIn(min = HeaderHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.screen_profile_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BeFairDimension.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.md)
        ) {
            ProfileCard(name = name, email = email)

            Section(title = stringResource(Res.string.screen_profile_preferences)) {
                SettingsRow(summary = settingsSummary, onClick = onOpenSettings)
            }

            Section(title = stringResource(Res.string.screen_profile_account)) {
                DestructiveButton(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(Res.string.screen_profile_sign_out),
                    onClick = onSignOut
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(name: String, email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, CardShape)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CardShape)
            .padding(BeFairDimension.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(AvatarSize)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
}

@Composable
private fun SettingsRow(summary: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, CardShape)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CardShape)
            .clip(CardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = RowHeight)
            .padding(horizontal = BeFairDimension.Spacing.md, vertical = BeFairDimension.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BeFairDimension.Spacing.md)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.screen_profile_settings),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(ChevronSize)
        )
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun ProfileTemplatePreview() {
    BeFairTheme {
        ProfileTemplate(
            name = "Jakub Zika",
            email = "jakub@example.com",
            settingsSummary = "Euro (€) · tools per month",
            onOpenSettings = {},
            onSignOut = {}
        )
    }
}

@Preview(backgroundColor = 0xF5F5F2, showBackground = true)
@Composable
private fun ProfileTemplateLongNamePreview() {
    BeFairTheme {
        ProfileTemplate(
            name = "Maximilian Alexander Von Hohenzollern-Sigmaringen",
            email = "maximilian.alexander.hohenzollern@example-long-domain.com",
            settingsSummary = "Custom (CHF) · tools per year",
            onOpenSettings = {},
            onSignOut = {}
        )
    }
}
