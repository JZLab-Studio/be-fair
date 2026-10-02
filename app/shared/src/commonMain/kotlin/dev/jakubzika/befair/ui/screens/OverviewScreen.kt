package dev.jakubzika.befair.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import be_fair.app.shared.generated.resources.Res
import be_fair.app.shared.generated.resources.brand_wordmark
import dev.jakubzika.befair.domain.AppResult
import dev.jakubzika.befair.domain.model.ItemEventResponse
import dev.jakubzika.befair.domain.model.OverviewSummary
import dev.jakubzika.befair.domain.model.RecentActivityLimit
import dev.jakubzika.befair.domain.model.recentActivity
import dev.jakubzika.befair.ui.LocalAppContainer
import dev.jakubzika.befair.ui.templates.OverviewEmptyTemplate
import dev.jakubzika.befair.ui.templates.OverviewTemplate
import org.jetbrains.compose.resources.stringResource

@Composable
fun OverviewScreen(
    onNavToAddNewItemScreen: () -> Unit,
    onNavToItemDetailScreen: (id: String) -> Unit,
    onNavToItemsScreen: () -> Unit
) {
    val container = LocalAppContainer.current
    val userName = remember { container.userProfileStorage.getDisplayName().orEmpty() }
    val items by container.itemRepository.items.collectAsState()
    var eventsByItem by remember { mutableStateOf<Map<String, List<ItemEventResponse>>>(emptyMap()) }

    LaunchedEffect(Unit) { container.itemRepository.refresh() }

    // The newest events overall can only come from the items with the newest activity, so only
    // those need their history fetched. Re-runs whenever an item's stats (and so lastEventAt) change.
    LaunchedEffect(items) {
        eventsByItem = items
            .filter { it.stats?.lastEventAt != null }
            .sortedByDescending { it.stats?.lastEventAt }
            .take(RecentActivityLimit)
            .associate { item ->
                val events = (container.itemRepository.events(item.id, RecentActivityLimit) as? AppResult.Success)?.data
                item.id to events.orEmpty()
            }
    }

    if (items.isEmpty()) {
        // Nothing to measure: per DESIGN.md "Never fake a statistic", show the empty state only.
        OverviewEmptyTemplate(
            userName = userName,
            appName = stringResource(Res.string.brand_wordmark),
            onAddFirstItem = onNavToAddNewItemScreen
        )
    } else {
        OverviewTemplate(
            summary = remember(items) { OverviewSummary.from(items) },
            recent = remember(items, eventsByItem) { recentActivity(items, eventsByItem) },
            onOpenItem = onNavToItemDetailScreen,
            onShowAllItems = onNavToItemsScreen
        )
    }
}
