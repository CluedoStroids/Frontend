package at.aau.se2.cluedo.data.models

import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TurnActionRequestTest {

    private val gson = Gson()

    @Test
    fun testDefaultValuesAreCorrect() {
        val request = TurnActionRequest()
        
        assertEquals("", request.playerName)
        assertEquals("", request.actionType)
        assertEquals(0, request.diceValue)
    }

    @Test
    fun testConstructorWithAllParameters() {
        val request = TurnActionRequest(
            playerName = "TestPlayer",
            actionType = "DICE_ROLL",
            diceValue = 6
        )
        
        assertEquals("TestPlayer", request.playerName)
        assertEquals("DICE_ROLL", request.actionType)
        assertEquals(6, request.diceValue)
    }

    @Test
    fun testConstructorWithSomeParameters() {
        val request = TurnActionRequest(
            playerName = "Player1",
            actionType = "COMPLETE_MOVEMENT"
        )
        
        assertEquals("Player1", request.playerName)
        assertEquals("COMPLETE_MOVEMENT", request.actionType)
        assertEquals(0, request.diceValue)
    }

    @Test
    fun testJsonSerializationWithAllFields() {
        val request = TurnActionRequest(
            playerName = "SerialPlayer",
            actionType = "DICE_ROLL",
            diceValue = 4
        )
        
        val json = gson.toJson(request)
        assertTrue(json.contains("\"playerName\":\"SerialPlayer\""))
        assertTrue(json.contains("\"actionType\":\"DICE_ROLL\""))
        assertTrue(json.contains("\"diceValue\":4"))
    }

    @Test
    fun testServerResponseCompleteMovement() {
        val json = """
            {
                "playerName": "DeserialPlayer",
                "actionType": "COMPLETE_MOVEMENT",
                "diceValue": 5
            }
        """.trimIndent()
        
        val request = gson.fromJson(json, TurnActionRequest::class.java)
        
        assertEquals("DeserialPlayer", request.playerName)
        assertEquals("COMPLETE_MOVEMENT", request.actionType)
        assertEquals(5, request.diceValue)
    }

    @Test
    fun testServerResponseMovement() {
        val json = """
            {
                "playerName": "MinimalPlayer",
                "actionType": "MOVE"
            }
        """.trimIndent()
        
        val request = gson.fromJson(json, TurnActionRequest::class.java)
        
        assertEquals("MinimalPlayer", request.playerName)
        assertEquals("MOVE", request.actionType)
        assertEquals(0, request.diceValue)
    }

    @Test
    fun testValidActionTypes() {
        val validActionTypes = listOf(
            "DICE_ROLL",
            "COMPLETE_MOVEMENT",
            "MOVE",
            "SUGGEST",
            "ACCUSE"
        )
        
        validActionTypes.forEach { actionType ->
            val request = TurnActionRequest(
                playerName = "TestPlayer",
                actionType = actionType,
                diceValue = 3
            )
            
            assertEquals(actionType, request.actionType)
        }
    }

    @Test
    fun testDiceRollActionWithDiceValue() {
        val request = TurnActionRequest(
            playerName = "DicePlayer",
            actionType = "DICE_ROLL",
            diceValue = 13
        )
        
        assertEquals("DicePlayer", request.playerName)
        assertEquals("DICE_ROLL", request.actionType)
        assertEquals(13, request.diceValue)
    }

    @Test
    fun testMovementCompletionAction() {
        val request = TurnActionRequest(
            playerName = "MovePlayer",
            actionType = "COMPLETE_MOVEMENT",
            diceValue = 14
        )
        
        assertEquals("MovePlayer", request.playerName)
        assertEquals("COMPLETE_MOVEMENT", request.actionType)
        assertEquals(14, request.diceValue)
    }

    @Test
    fun testEqualityAndHashCode() {
        val request1 = TurnActionRequest(
            playerName = "Player1",
            actionType = "DICE_ROLL",
            diceValue = 4
        )
        
        val request2 = TurnActionRequest(
            playerName = "Player1",
            actionType = "DICE_ROLL",
            diceValue = 4
        )
        
        val request3 = TurnActionRequest(
            playerName = "Player2",
            actionType = "DICE_ROLL",
            diceValue = 4
        )
        
        assertEquals(request1, request2)
        assertEquals(request1.hashCode(), request2.hashCode())
        assertNotEquals(request1, request3)
    }

    @Test
    fun testToString() {
        val request = TurnActionRequest(
            playerName = "ToStringPlayer",
            actionType = "DICE_ROLL",
            diceValue = 3
        )
        
        val toString = request.toString()
        assertTrue(toString.contains("ToStringPlayer"))
        assertTrue(toString.contains("DICE_ROLL"))
        assertTrue(toString.contains("3"))
    }

    @Test
    fun testCopyFunctionWorksCorrectly() {
        val original = TurnActionRequest(
            playerName = "OriginalPlayer",
            actionType = "DICE_ROLL",
            diceValue = 2
        )
        
        val copied = original.copy(
            actionType = "COMPLETE_MOVEMENT",
            diceValue = 0
        )
        
        assertEquals("OriginalPlayer", original.playerName)
        assertEquals("DICE_ROLL", original.actionType)
        assertEquals(2, original.diceValue)
        
        assertEquals("OriginalPlayer", copied.playerName)
        assertEquals("COMPLETE_MOVEMENT", copied.actionType)
        assertEquals(0, copied.diceValue)
    }

    @Test
    fun testDiceValueValidationBoundaries() {
        for (diceValue in 1..12) {
            val request = TurnActionRequest(
                playerName = "DicePlayer",
                actionType = "DICE_ROLL",
                diceValue = diceValue
            )
            
            assertEquals(diceValue, request.diceValue)
        }
        
        val zeroRequest = TurnActionRequest(
            playerName = "ZeroPlayer",
            actionType = "COMPLETE_MOVEMENT",
            diceValue = 0
        )
        assertEquals(0, zeroRequest.diceValue)
    }

    @Test
    fun testEmptyAndNullStringHandling() {
        val emptyRequest = TurnActionRequest(
            playerName = "",
            actionType = ""
        )
        
        assertEquals("", emptyRequest.playerName)
        assertEquals("", emptyRequest.actionType)
        assertEquals(0, emptyRequest.diceValue)
    }
} 