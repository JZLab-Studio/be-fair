package dev.jakubzika.befair.domain.model

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
    /** Σ(price ÷ months owned) over tools. */
    val toolsPerMonthCents: Long,
    /** The worn garment with the highest cost per wear; null when there is none. */
    val attention: ItemResponse?,
) {
    companion object {
        fun from(items: List<ItemResponse>): OverviewSummary {
            val clothes = items.filter { it.kind == ItemKind.CLOTHING }
            val totalWears = clothes.sumOf { (it.stats?.wearCount ?: 0).toLong() }
            return OverviewSummary(
                investedCents = items.sumOf { it.priceCents },
                avgCostPerWearCents = if (totalWears > 0) clothes.sumOf { it.priceCents } / totalWears else null,
                toolsPerMonthCents = items.filter { it.kind == ItemKind.TOOL }
                    .sumOf { it.stats?.costPerMonthCents ?: 0L },
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
