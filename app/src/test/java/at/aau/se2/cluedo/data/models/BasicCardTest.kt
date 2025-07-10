package at.aau.se2.cluedo.data.models

import com.example.myapplication.R
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue


class BasicCardTest {


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
    fun testGetCardIDsNotFound() {

        val cards = listOf(
            BasicCard("", CardType.WEAPON),
            BasicCard("Kitchen", CardType.ROOM),
        )

        val result = BasicCard.getCardIDs(cards)

        assertEquals(1, result.size)
        assertTrue(result.contains(R.drawable.kitchen))

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