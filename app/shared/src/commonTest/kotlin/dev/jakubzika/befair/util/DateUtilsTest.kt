package dev.jakubzika.befair.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateUtilsTest {

    @Test
    fun `isoToEpochDay maps the epoch itself to zero`() {
        assertEquals(0L, isoToEpochDay("1970-01-01"))
    }

    @Test
    fun `isoToEpochDay handles dates before the epoch`() {
        assertEquals(-1L, isoToEpochDay("1969-12-31"))
    }

    @Test
    fun `isoToEpochDay counts leap days`() {
        // 2024 is a leap year, so Mar 1 is 60 days after Jan 1 rather than 59.
        val janFirst = isoToEpochDay("2024-01-01")!!
        val marchFirst = isoToEpochDay("2024-03-01")!!
        assertEquals(60L, marchFirst - janFirst)
    }

    @Test
    fun `isoToEpochDay returns null for malformed input`() {
        assertNull(isoToEpochDay(""))
        assertNull(isoToEpochDay("not a date"))
        assertNull(isoToEpochDay("2024-13-01"))
        assertNull(isoToEpochDay("2023-02-29"))
        assertNull(isoToEpochDay("01/02/2024"))
    }

    @Test
    fun `epochDayToIso round-trips with isoToEpochDay`() {
        listOf("1970-01-01", "1900-01-01", "2024-02-29", "2026-09-26").forEach { iso ->
            assertEquals(iso, epochDayToIso(isoToEpochDay(iso)!!))
        }
    }

    @Test
    fun `todayIso is a parseable ISO date`() {
        val today = todayIso()
        assertEquals(today, epochDayToIso(isoToEpochDay(today)!!))
    }
}
