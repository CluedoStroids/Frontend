package at.aau.se2.cluedo.viewmodels

class RoomUtils {
    companion object {
        @JvmStatic
        fun getRoomNameFromCoordinates(x: Int?, y: Int?): String? {
            return when (Pair(x, y)) {
                // Küche (top-left)
                Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(0, 1), Pair(1, 1), Pair(2, 1) -> "Küche"
                // Speisezimmer
                Pair(0, 5), Pair(1, 5), Pair(2, 5), Pair(0, 6), Pair(1, 6), Pair(2, 6) -> "Speisezimmer"
                // Salon
                Pair(0, 10), Pair(1, 10), Pair(2, 10), Pair(0, 11), Pair(1, 11), Pair(2, 11) -> "Salon"
                // Musikzimmer
                Pair(4, 0), Pair(5, 0), Pair(6, 0), Pair(4, 1), Pair(5, 1), Pair(6, 1) -> "Musikzimmer"
                // Halle
                Pair(4, 10), Pair(5, 10), Pair(6, 10), Pair(4, 11), Pair(5, 11), Pair(6, 11) -> "Halle"
                // Wintergarten
                Pair(8, 0), Pair(9, 0), Pair(10, 0), Pair(8, 1), Pair(9, 1), Pair(10, 1) -> "Wintergarten"
                // Billardzimmer
                Pair(8, 4), Pair(9, 4), Pair(10, 4), Pair(8, 5), Pair(9, 5), Pair(10, 5) -> "Billardzimmer"
                // Bibliothek
                Pair(8, 6), Pair(9, 6), Pair(10, 6), Pair(8, 7), Pair(9, 7), Pair(10, 7) -> "Bibliothek"
                // Arbeitszimmer
                Pair(8, 10), Pair(9, 10), Pair(10, 10), Pair(8, 11), Pair(9, 11), Pair(10, 11) -> "Arbeitszimmer"
                else -> null
            }
        }
    }
}
