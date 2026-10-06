package dev.jakubzika.befair.domain.model

import dev.jakubzika.befair.util.isoToEpochDay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OverviewSummaryTest {

    private fun item(
        id: String,
        kind: ItemKind,
        priceCents: Long,
        wears: Int = 0,
        perUse: Long? = null,
    ) = ItemResponse(
        id = id, kind = kind, name = id, category = "", priceCents = priceCents, currency = "EUR",
        purchasedOn = 0, archivedAt = null, deletedAt = null, createdAt = 0, updatedAt = 0,
        stats = ItemStats(
            wearCount = wears, washCount = 0, useCount = 0, maintenanceCents = 0, lastEventAt = null,
            costPerUseCents = perUse, costPerMonthCents = null,
        ),
    )

    // 1970-04-01: 90 days (12 whole weeks, 3 whole months) after the epoch-day-0 purchase.
    private val threeMonthsIn = isoToEpochDay("1970-04-01")!!

    private val toolsAndClothes = listOf(
        item("coat", ItemKind.CLOTHING, 20000, wears = 10, perUse = 2000),
        item("shirt", ItemKind.CLOTHING, 5000, wears = 40, perUse = 125),
        item("drill", ItemKind.TOOL, 12000),
        item("saw", ItemKind.TOOL, 3000),
    )

    private fun event(id: String, itemId: String, at: Long) =
        ItemEventResponse(id, itemId, ItemEventType.WEAR, at, null, null, at)

    @Test
    fun `aggregates invested and average per wear and tools per month`() {
        val summary = OverviewSummary.from(toolsAndClothes, ToolBasis.MONTH, todayEpochDay = threeMonthsIn)
        assertEquals(40000, summary.investedCents)
        assertEquals(500, summary.avgCostPerWearCents)
        // purchasedOn = 0, so 3 full months owned: 12000 / 3 + 3000 / 3.
        assertEquals(5000, summary.toolsCostCents)
        assertEquals("coat", summary.attention?.id)
    }

    @Test
    fun `tools cost follows the chosen basis`() {
        // 90 days owned: 12000 / 90 + 3000 / 90 (each rounded down); 12 weeks: 12000 / 12 + 3000 / 12.
        assertEquals(166, OverviewSummary.from(toolsAndClothes, ToolBasis.DAY, threeMonthsIn).toolsCostCents)
        assertEquals(1250, OverviewSummary.from(toolsAndClothes, ToolBasis.WEEK, threeMonthsIn).toolsCostCents)
        // Under a year owned still counts as one full year (minimum 1).
        assertEquals(15000, OverviewSummary.from(toolsAndClothes, ToolBasis.YEAR, threeMonthsIn).toolsCostCents)
    }

    @Test
    fun `no wears means no average and no attention`() {
        val summary = OverviewSummary.from(listOf(item("coat", ItemKind.CLOTHING, 20000)))
        assertNull(summary.avgCostPerWearCents)
        assertNull(summary.attention)
    }

    @Test
    fun `recent activity is newest first and capped and skips orphans`() {
        val items = listOf(item("a", ItemKind.CLOTHING, 100))
        val events = mapOf(
            "a" to listOf(event("1", "a", 1), event("2", "a", 3), event("3", "a", 2)),
            "gone" to listOf(event("4", "gone", 9)),
        )
        val result = recentActivity(items, events, limit = 2)
        assertEquals(listOf("2", "3"), result.map { it.event.id })
    }
}
