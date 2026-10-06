package dev.jakubzika.befair.domain.model

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
        perMonth: Long? = null,
    ) = ItemResponse(
        id = id, kind = kind, name = id, category = "", priceCents = priceCents, currency = "EUR",
        purchasedOn = 0, archivedAt = null, deletedAt = null, createdAt = 0, updatedAt = 0,
        stats = ItemStats(
            wearCount = wears, washCount = 0, useCount = 0, maintenanceCents = 0, lastEventAt = null,
            costPerUseCents = perUse, costPerMonthCents = perMonth,
        ),
    )

    private fun event(id: String, itemId: String, at: Long) =
        ItemEventResponse(id, itemId, ItemEventType.WEAR, at, null, null, at)

    @Test
    fun `aggregates invested and average per wear and tools per month`() {
        val summary = OverviewSummary.from(
            listOf(
                item("coat", ItemKind.CLOTHING, 20000, wears = 10, perUse = 2000),
                item("shirt", ItemKind.CLOTHING, 5000, wears = 40, perUse = 125),
                item("drill", ItemKind.TOOL, 12000, perMonth = 850),
                item("saw", ItemKind.TOOL, 3000, perMonth = 150),
            )
        )
        assertEquals(40000, summary.investedCents)
        assertEquals(500, summary.avgCostPerWearCents)
        assertEquals(1000, summary.toolsPerMonthCents)
        assertEquals("coat", summary.attention?.id)
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
