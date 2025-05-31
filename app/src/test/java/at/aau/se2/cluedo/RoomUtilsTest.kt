package at.aau.se2.cluedo

import at.aau.se2.cluedo.viewmodels.RoomUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RoomUtilsTest {

    @Test
    fun `returns Kueche for its coordinate pairs`() {
        listOf(
            Pair(0, 0), Pair(1, 0), Pair(2, 0),
            Pair(0, 1), Pair(1, 1), Pair(2, 1)
        ).forEach {
            assertEquals("Küche", RoomUtils.getRoomNameFromCoordinates(it.first, it.second))
        }
    }

    @Test
    fun `returns Speisezimmer for its coordinate pairs`() {
        listOf(
            Pair(0, 5), Pair(1, 5), Pair(2, 5),
            Pair(0, 6), Pair(1, 6), Pair(2, 6)
        ).forEach {
            assertEquals("Speisezimmer", RoomUtils.getRoomNameFromCoordinates(it.first, it.second))
        }
    }

    @Test
    fun `returns Salon for its coordinate pairs`() {
        listOf(
            Pair(0, 10), Pair(1, 10), Pair(2, 10),
            Pair(0, 11), Pair(1, 11), Pair(2, 11)
        ).forEach {
            assertEquals("Salon", RoomUtils.getRoomNameFromCoordinates(it.first, it.second))
        }
    }

    @Test
    fun `returns Musikzimmer for its coordinate pairs`() {
        listOf(
            Pair(4, 0), Pair(5, 0), Pair(6, 0),
            Pair(4, 1), Pair(5, 1), Pair(6, 1)
        ).forEach {
            assertEquals("Musikzimmer", RoomUtils.getRoomNameFromCoordinates(it.first, it.second))
        }
    }

    @Test
    fun `returns Halle for its coordinate pairs`() {
        listOf(
            Pair(4, 10), Pair(5, 10), Pair(6, 10),
            Pair(4, 11), Pair(5, 11), Pair(6, 11)
        ).forEach {
            assertEquals("Halle", RoomUtils.getRoomNameFromCoordinates(it.first, it.second))
        }
    }

    @Test
    fun `returns Wintergarten for its coordinate pairs`() {
        listOf(
            Pair(8, 0), Pair(9, 0), Pair(10, 0),
            Pair(8, 1), Pair(9, 1), Pair(10, 1)
        ).forEach {
            assertEquals("Wintergarten", RoomUtils.getRoomNameFromCoordinates(it.first, it.second))
        }
    }

    @Test
    fun `returns Billardzimmer for its coordinate pairs`() {
        listOf(
            Pair(8, 4), Pair(9, 4), Pair(10, 4),
            Pair(8, 5), Pair(9, 5), Pair(10, 5)
        ).forEach {
            assertEquals("Billardzimmer", RoomUtils.getRoomNameFromCoordinates(it.first, it.second))
        }
    }

    @Test
    fun `returns Bibliothek for its coordinate pairs`() {
        listOf(
            Pair(8, 6), Pair(9, 6), Pair(10, 6),
            Pair(8, 7), Pair(9, 7), Pair(10, 7)
        ).forEach {
            assertEquals("Bibliothek", RoomUtils.getRoomNameFromCoordinates(it.first, it.second))
        }
    }

    @Test
    fun `returns Arbeitszimmer for its coordinate pairs`() {
        listOf(
            Pair(8, 10), Pair(9, 10), Pair(10, 10),
            Pair(8, 11), Pair(9, 11), Pair(10, 11)
        ).forEach {
            assertEquals("Arbeitszimmer", RoomUtils.getRoomNameFromCoordinates(it.first, it.second))
        }
    }

    @Test
    fun `returns null for coordinates not matching any room`() {
        listOf(
            Pair(3, 3), Pair(99, 99), Pair(-1, -1), Pair(null, null), Pair(7, 7)
        ).forEach {
            assertNull(RoomUtils.getRoomNameFromCoordinates(it.first, it.second))
        }
    }
}