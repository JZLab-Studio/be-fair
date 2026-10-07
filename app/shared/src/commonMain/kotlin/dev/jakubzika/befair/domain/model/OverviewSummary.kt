package dev.jakubzika.befair.domain.model

import dev.jakubzika.befair.util.todayEpochDay

/** One line of the Overview "Recent activity" list: a logged [event] resolved to its [item]. */
data class ActivityEntry(
    val item: ItemResponse,
    val event: ItemEventResponse,
)

/**
 * Figures for the Overview stats card, derived from the item list. Per-item numbers come from the
 * server-computed [ItemStats]; this only aggregates them. Mobile-only, so it lives in app/shared.
 */
data class OverviewSummary(
    val investedCents: Long,
    /** Clothing spend ÷ total wears; null while nothing has been worn. */
    val avgCostPerWearCents: Long?,
    /** Σ(price ÷ full periods owned) over tools, per the chosen [ToolBasis]. */
    val toolsCostCents: Long,
    /** The worn garment with the highest cost per wear; null when there is none. */
    val attention: ItemResponse?,
) {
    companion object {
        fun from(
            items: List<ItemResponse>,
            basis: ToolBasis = ToolBasis.MONTH,
            todayEpochDay: Long = todayEpochDay(),
        ): OverviewSummary {
            val clothes = items.filter { it.kind == ItemKind.CLOTHING }
            val totalWears = clothes.sumOf { (it.stats?.wearCount ?: 0).toLong() }
            return OverviewSummary(
                investedCents = items.sumOf { it.priceCents },
                avgCostPerWearCents = if (totalWears > 0) clothes.sumOf { it.priceCents } / totalWears else null,
                toolsCostCents = items.filter { it.kind == ItemKind.TOOL }
                    .sumOf { it.primaryCostCents(basis, todayEpochDay) ?: 0L },
                attention = clothes
                    .filter { (it.stats?.wearCount ?: 0) > 0 && it.stats?.costPerUseCents != null }
                    .maxByOrNull { it.stats?.costPerUseCents ?: 0L },
            )
        }
    }
}

/** Number of rows shown in Overview "Recent activity". */
const val RecentActivityLimit = 5

/**
 * The newest [limit] events across all items, newest first. Events whose item is gone are skipped.
 */
fun recentActivity(
    items: List<ItemResponse>,
    eventsByItem: Map<String, List<ItemEventResponse>>,
    limit: Int = RecentActivityLimit,
): List<ActivityEntry> {
    val byId = items.associateBy { it.id }
    return eventsByItem.values.flatten()
        .sortedByDescending { it.occurredAt }
        .mapNotNull { event -> byId[event.itemId]?.let { ActivityEntry(it, event) } }
        .take(limit)
}
