package at.aau.se2.cluedo.data.models

import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TurnStateResponseTest {

    private val gson = Gson()

    @Test
    fun testDefaultValuesAreCorrect() {
        val response = TurnStateResponse()
        
        assertEquals("", response.lobbyId)
        assertEquals("", response.currentPlayerName)
        assertEquals("", response.turnState)
        assertEquals(0, response.diceValue)
        assertNull(response.canMakeSuggestion)
        assertNull(response.canMakeAccusation)
        assertNull(response.message)
    }

    @Test
    fun testConstructorWithAllParameters() {
        val response = TurnStateResponse(
            lobbyId = "test-lobby",
            currentPlayerName = "TestPlayer",
            turnState = TurnState.PLAYERS_TURN_ROLL_DICE.value,
            diceValue = 6,
            canMakeSuggestion = true,
            canMakeAccusation = false,
            message = "Test message"
        )
        
        assertEquals("test-lobby", response.lobbyId)
        assertEquals("TestPlayer", response.currentPlayerName)
        assertEquals(TurnState.PLAYERS_TURN_ROLL_DICE.value, response.turnState)
        assertEquals(6, response.diceValue)
        assertEquals(true, response.canMakeSuggestion)
        assertEquals(false, response.canMakeAccusation)
        assertEquals("Test message", response.message)
    }

    @Test
    fun testFullServerResponse() {
        val json = """
            {
                "lobbyId": "test-lobby-456",
                "currentPlayerName": "PlayerTwo",
                "turnState": "PLAYERS_TURN_MOVE",
                "diceValue": 3,
                "canMakeSuggestion": false,
                "canMakeAccusation": true,
                "message": "Move your piece"
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, TurnStateResponse::class.java)
        
        assertEquals("test-lobby-456", response.lobbyId)
        assertEquals("PlayerTwo", response.currentPlayerName)
        assertEquals("PLAYERS_TURN_MOVE", response.turnState)
        assertEquals(3, response.diceValue)
        assertEquals(false, response.canMakeSuggestion)
        assertEquals(true, response.canMakeAccusation)
        assertEquals("Move your piece", response.message)
    }

    @Test
    fun testPartialServerResponse() {
        val json = """
            {
                "lobbyId": "minimal-lobby",
                "currentPlayerName": "MinimalPlayer",
                "turnState": "PLAYERS_TURN_END"
            }
        """.trimIndent()
        
        val response = gson.fromJson(json, TurnStateResponse::class.java)
        
        assertEquals("minimal-lobby", response.lobbyId)
        assertEquals("MinimalPlayer", response.currentPlayerName)
        assertEquals("PLAYERS_TURN_END", response.turnState)
        assertEquals(0, response.diceValue)
        assertNull(response.canMakeSuggestion)
        assertNull(response.canMakeAccusation)
        assertNull(response.message)
    }

    @Test
    fun testCopyFunctionWorksCorrectly() {
        val original = TurnStateResponse(
            lobbyId = "original-lobby",
            currentPlayerName = "OriginalPlayer",
            turnState = TurnState.PLAYERS_TURN_ROLL_DICE.value,
            diceValue = 5
        )
        
        val copied = original.copy(
            currentPlayerName = "NewPlayer",
            turnState = TurnState.PLAYERS_TURN_MOVE.value,
            diceValue = 8
        )
        
        assertEquals("original-lobby", original.lobbyId)
        assertEquals("OriginalPlayer", original.currentPlayerName)
        assertEquals(TurnState.PLAYERS_TURN_ROLL_DICE.value, original.turnState)
        assertEquals(5, original.diceValue)
        
        assertEquals("original-lobby", copied.lobbyId)
        assertEquals("NewPlayer", copied.currentPlayerName)
        assertEquals(TurnState.PLAYERS_TURN_MOVE.value, copied.turnState)
        assertEquals(8, copied.diceValue)
    }

    @Test
    fun testEqualityAndHashCode() {
        val response1 = TurnStateResponse(
            lobbyId = "test",
            currentPlayerName = "player",
            turnState = TurnState.PLAYERS_TURN_SUGGEST.value,
            diceValue = 2
        )
        
        val response2 = TurnStateResponse(
            lobbyId = "test",
            currentPlayerName = "player",
            turnState = TurnState.PLAYERS_TURN_SUGGEST.value,
            diceValue = 2
        )
        
        val response3 = TurnStateResponse(
            lobbyId = "different",
            currentPlayerName = "player",
            turnState = TurnState.PLAYERS_TURN_SUGGEST.value,
            diceValue = 2
        )
        
        assertEquals(response1, response2)
        assertEquals(response1.hashCode(), response2.hashCode())
        assertNotEquals(response1, response3)
    }

    @Test
    fun testToString() {
        val response = TurnStateResponse(
            lobbyId = "test-lobby",
            currentPlayerName = "TestPlayer",
            turnState = TurnState.PLAYERS_TURN_ROLL_DICE.value,
            diceValue = 6,
            canMakeSuggestion = true,
            canMakeAccusation = false,
            message = "Test message"
        )
        
        val toString = response.toString()
        assertTrue(toString.contains("test-lobby"))
        assertTrue(toString.contains("TestPlayer"))
        assertTrue(toString.contains("PLAYERS_TURN_ROLL_DICE"))
        assertTrue(toString.contains("6"))
        assertTrue(toString.contains("true"))
        assertTrue(toString.contains("false"))
        assertTrue(toString.contains("Test message"))
    }

    @Test
    fun testAllTurnStatesWorkCorrectly() {
        TurnState.values().forEach { turnState ->
            val response = TurnStateResponse(
                lobbyId = "test",
                currentPlayerName = "player",
                turnState = turnState.value
            )
            
            assertEquals(turnState.value, response.turnState)
        }
    }
} 