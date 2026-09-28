package dev.jakubzika.befair.data.local

import dev.jakubzika.befair.domain.model.ItemKind
import dev.jakubzika.befair.domain.model.ItemResponse
import dev.jakubzika.befair.domain.model.ItemStats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ItemEntityTest {

    private val item = ItemResponse(
        id = "5e6b4b7c-1f2a-4a3d-9c8e-0d1f2a3b4c5d",
        kind = ItemKind.CLOTHING,
        name = "Wool overcoat",
        category = "Outerwear",
        priceCents = 28000,
        currency = "EUR",
        purchasedOn = 19000,
        archivedAt = null,
        deletedAt = null,
        createdAt = 1,
        updatedAt = 2,
        stats = ItemStats(
            wearCount = 14,
            washCount = 2,
            useCount = 0,
            maintenanceCents = 500,
            lastEventAt = 42,
            costPerUseCents = 2000,
            costPerMonthCents = null,
        ),
    )

    @Test
    fun `item with stats round-trips through the entity`() {
        val entity = item.toEntity()

        assertTrue(entity.hasStats)
        assertEquals(item, entity.toModel())
    }

    @Test
    fun `item without stats round-trips as null stats`() {
        val noStats = item.copy(stats = null)
        val entity = noStats.toEntity()

        assertFalse(entity.hasStats)
        assertNull(entity.toModel().stats)
        assertEquals(noStats, entity.toModel())
    }

    @Test
    fun `zeroed stats stay distinct from missing stats`() {
        val zeroed = item.copy(
            stats = ItemStats(0, 0, 0, 0, null, null, null)
        )

        assertEquals(zeroed.stats, zeroed.toEntity().toModel().stats)
    }
}
