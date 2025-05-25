package at.aau.se2.cluedo.data.models

import com.example.myapplication.R
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue


class BasicCardTest {

    @Test
    fun testGetAllWeapons() {
        val expectedWeapons = listOf("Pipe", "Rope", "Wrench", "Pistol", "Dagger", "Candlestick")

        expectedWeapons.forEach { weapon ->
            assertTrue(BasicCard.cardImageToPNG.containsKey(weapon))
            assertNotNull(BasicCard.cardImageToPNG[weapon])
        }
    }

    @Test
    fun testGetAllRooms() {
        val expectedRooms = listOf("Kitchen", "Wintergarden", "Music room", "Billard room", "Dining room", "Hall", "Library", "Salon", "Office")

        expectedRooms.forEach { room ->
            assertTrue(BasicCard.cardImageToPNG.containsKey(room))
            assertNotNull(BasicCard.cardImageToPNG[room])
        }
    }

    @Test
    fun testGetAllCharacters() {
        val expectedCharacters = listOf("Miss Scarlet", "Colonel Mustard", "Mrs. White", "Mr. Green", "Mrs. Peacock", "Professor Plum")

        expectedCharacters.forEach { character ->
            assertTrue(BasicCard.cardImageToPNG.containsKey(character))
            assertNotNull(BasicCard.cardImageToPNG[character])
        }
    }

    @Test
    fun testMappingOfWeaponsToDrawables() {
        assertEquals(R.drawable.card_pipe, BasicCard.cardImageToPNG["Pipe"])
        assertEquals(R.drawable.card_rope, BasicCard.cardImageToPNG["Rope"])
        assertEquals(R.drawable.card_wrench, BasicCard.cardImageToPNG["Wrench"])
        assertEquals(R.drawable.card_pistol, BasicCard.cardImageToPNG["Pistol"])
        assertEquals(R.drawable.card_dagger, BasicCard.cardImageToPNG["Dagger"])
        assertEquals(R.drawable.card_candlestick, BasicCard.cardImageToPNG["Candlestick"])
    }

    @Test
    fun testMappingOfRoomsToDrawables() {
        assertEquals(R.drawable.kitchen, BasicCard.cardImageToPNG["Kitchen"])
        assertEquals(R.drawable.wintergarden, BasicCard.cardImageToPNG["Wintergarden"])
        assertEquals(R.drawable.music_room, BasicCard.cardImageToPNG["Music room"])
        assertEquals(R.drawable.billard_room, BasicCard.cardImageToPNG["Billard room"])
        assertEquals(R.drawable.dining_room, BasicCard.cardImageToPNG["Dining room"])
        assertEquals(R.drawable.hall, BasicCard.cardImageToPNG["Hall"])
        assertEquals(R.drawable.library, BasicCard.cardImageToPNG["Library"])
        assertEquals(R.drawable.salon, BasicCard.cardImageToPNG["Salon"])
        assertEquals(R.drawable.office, BasicCard.cardImageToPNG["Office"])
    }

    @Test
    fun testMappingOfCharactersToDrawables() {
        assertEquals(R.drawable.playercard_red, BasicCard.cardImageToPNG["Miss Scarlet"])
        assertEquals(R.drawable.playercard_yellow, BasicCard.cardImageToPNG["Colonel Mustard"])
        assertEquals(R.drawable.playercard_white, BasicCard.cardImageToPNG["Mrs. White"])
        assertEquals(R.drawable.playercard_green, BasicCard.cardImageToPNG["Mr. Green"])
        assertEquals(R.drawable.playercard_blue, BasicCard.cardImageToPNG["Mrs. Peacock"])
        assertEquals(R.drawable.playercard_purple, BasicCard.cardImageToPNG["Professor Plum"])
    }

    @Test
    fun testSizeOfMapping() {
        assertEquals(21, BasicCard.cardImageToPNG.size)
    }

    @Test
    fun testNoCardsFound() {
        val result1 = BasicCard.getCardIDs(null)
        assertTrue(result1.isEmpty())

        val result2 = BasicCard.getCardIDs(emptyList<BasicCard>())
        assertTrue(result2.isEmpty())
    }

    @Test
    fun testGetCardIDs() {

        val cards = listOf(
            BasicCard("Pipe", CardType.WEAPON),
            BasicCard("Kitchen", CardType.ROOM),
            BasicCard("Miss Scarlet", CardType.CHARACTER)
        )

        val result = BasicCard.getCardIDs(cards)

        assertEquals(3, result.size)
        assertTrue(result.contains(R.drawable.card_pipe))
        assertTrue(result.contains(R.drawable.kitchen))
        assertTrue(result.contains(R.drawable.playercard_red))
    }

    @Test
    fun testNameOfCardType() {
        assertEquals("WEAPON", CardType.WEAPON.toString())
        assertEquals("ROOM", CardType.ROOM.toString())
        assertEquals("CHARACTER", CardType.CHARACTER.toString())
    }

    @Test
    fun testTypesOfCardType() {
        assertEquals(CardType.WEAPON, CardType.valueOf("WEAPON"))
        assertEquals(CardType.ROOM, CardType.valueOf("ROOM"))
        assertEquals(CardType.CHARACTER, CardType.valueOf("CHARACTER"))
    }

}