package dev.jakubzika.befair.data.db

import dev.jakubzika.befair.TestDatabase
import java.sql.SQLException
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ItemRepositoryTest {

    init {
        TestDatabase.ensureReady()
    }

    @Test
    fun `create rejects an item with a nonexistent user`() {
        val error = assertFailsWith<Throwable> {
            runBlocking { ItemRepository().create(sampleItem(userId = Int.MAX_VALUE)) }
        }

        assertForeignKeyViolation(error)
    }

    @Test
    fun `create event rejects an event with a nonexistent item`() {
        val error = assertFailsWith<Throwable> {
            runBlocking {
                ItemRepository().createEventAndTouch(
                    ItemEventRow(
                        id = UUID.randomUUID().toString(),
                        itemId = UUID.randomUUID().toString(),
                        type = "WEAR",
                        occurredAt = System.currentTimeMillis(),
                        costCents = null,
                        note = null,
                        createdAt = System.currentTimeMillis(),
                        deletedAt = null,
                    ),
                    now = System.currentTimeMillis(),
                )
            }
        }

        assertForeignKeyViolation(error)
    }

    private fun assertForeignKeyViolation(error: Throwable) {
        assertTrue(
            generateSequence(error) { it.cause }
                .filterIsInstance<SQLException>()
                .any { it.sqlState == "23503" },
            "Expected a PostgreSQL foreign-key violation",
        )
    }

    private fun sampleItem(userId: Int) = ItemRow(
        id = UUID.randomUUID().toString(),
        userId = userId,
        kind = "CLOTHING",
        name = "Navy chinos",
        category = "Trousers",
        priceCents = 8900,
        currency = "EUR",
        purchasedOn = LocalDate.now().toEpochDay(),
        archivedAt = null,
        deletedAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
    )
}
