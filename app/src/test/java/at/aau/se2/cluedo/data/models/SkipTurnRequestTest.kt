package at.aau.se2.cluedo.data.models

import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SkipTurnRequestTest {

    private val gson = Gson()

    @Test
    fun testDefaultValuesAreCorrect() {
        val request = SkipTurnRequest()
        
        assertEquals("", request.playerName)
        assertEquals("Player manually skipped turn", request.reason)
    }

    @Test
    fun testConstructorWithAllParameters() {
        val request = SkipTurnRequest(
            playerName = "TestPlayer",
            reason = "Custom skip reason"
        )
        
        assertEquals("TestPlayer", request.playerName)
        assertEquals("Custom skip reason", request.reason)
    }

    @Test
    fun testConstructorWithPlayerNameOnlyUsesDefaultReason() {
        val request = SkipTurnRequest(
            playerName = "PlayerOnly"
        )
        
        assertEquals("PlayerOnly", request.playerName)
        assertEquals("Player manually skipped turn", request.reason)
    }

    @Test
    fun testJsonSerializationWithAllFields() {
        val request = SkipTurnRequest(
            playerName = "SkipPlayer",
            reason = "Network connection issues"
        )
        
        val json = gson.toJson(request)
        assertTrue(json.contains("\"playerName\":\"SkipPlayer\""))
        assertTrue(json.contains("\"reason\":\"Network connection issues\""))
    }

    @Test
    fun testResponseFromServerJsonConversionWithReason() {
        val json = """
            {
                "playerName": "DeserialPlayer",
                "reason": "Player AFK"
            }
        """.trimIndent()
        
        val request = gson.fromJson(json, SkipTurnRequest::class.java)
        
        assertEquals("DeserialPlayer", request.playerName)
        assertEquals("Player AFK", request.reason)
    }

    @Test
    fun testResponseFromServerJsonConversionWOReason() {
        val json = """
            {
                "playerName": "MinimalPlayer"
            }
        """.trimIndent()
        
        val request = gson.fromJson(json, SkipTurnRequest::class.java)
        
        assertEquals("MinimalPlayer", request.playerName)
        assertEquals("Player manually skipped turn", request.reason)
    }

    @Test
    fun testVariousSkipReasons() {
        val skipReasons = listOf(
            "Player manually skipped turn",
            "Network timeout",
            "Player disconnected",
            "Idle timeout",
            "Emergency skip",
            "Turn timeout expired",
            "Player requested skip",
            "System forced skip"
        )
        
        skipReasons.forEach { reason ->
            val request = SkipTurnRequest(
                playerName = "TestPlayer",
                reason = reason
            )
            
            assertEquals(reason, request.reason)
        }
    }

    @Test
    fun testEqualityAndHashCode() {
        val request1 = SkipTurnRequest(
            playerName = "Player1",
            reason = "Same reason"
        )
        
        val request2 = SkipTurnRequest(
            playerName = "Player1",
            reason = "Same reason"
        )
        
        val request3 = SkipTurnRequest(
            playerName = "Player2",
            reason = "Same reason"
        )
        
        assertEquals(request1, request2)
        assertEquals(request1.hashCode(), request2.hashCode())
        assertNotEquals(request1, request3)
    }

    @Test
    fun testToString() {
        val request = SkipTurnRequest(
            playerName = "ToStringPlayer",
            reason = "Testing toString method"
        )
        
        val toString = request.toString()
        assertTrue(toString.contains("ToStringPlayer"))
        assertTrue(toString.contains("Testing toString method"))
    }

    @Test
    fun testCopyFunctionWorksCorrectly() {
        val original = SkipTurnRequest(
            playerName = "OriginalPlayer",
            reason = "Original reason"
        )
        
        val copied = original.copy(
            reason = "Updated reason"
        )

        assertEquals("OriginalPlayer", original.playerName)
        assertEquals("Original reason", original.reason)

        assertEquals("OriginalPlayer", copied.playerName)
        assertEquals("Updated reason", copied.reason)
    }

    @Test
    fun testEmptyAndNullStringHandling() {
        val emptyPlayerRequest = SkipTurnRequest(
            playerName = "",
            reason = "Valid reason"
        )
        
        val emptyReasonRequest = SkipTurnRequest(
            playerName = "ValidPlayer",
            reason = ""
        )
        
        assertEquals("", emptyPlayerRequest.playerName)
        assertEquals("Valid reason", emptyPlayerRequest.reason)
        
        assertEquals("ValidPlayer", emptyReasonRequest.playerName)
        assertEquals("", emptyReasonRequest.reason)
    }

    @Test
    fun testSkipRequestSerializationPreservesWhitespace() {
        val request = SkipTurnRequest(
            playerName = "  Spaced Player  ",
            reason = "  Reason with spaces  "
        )
        
        val json = gson.toJson(request)
        val deserialized = gson.fromJson(json, SkipTurnRequest::class.java)
        
        assertEquals("  Spaced Player  ", deserialized.playerName)
        assertEquals("  Reason with spaces  ", deserialized.reason)
    }


    @Test
    fun testSkipRequestUseCases() {
        val manualSkip = SkipTurnRequest(
            playerName = "Player1",
            reason = "Player manually skipped turn"
        )

        val timeoutSkip = SkipTurnRequest(
            playerName = "Player2",
            reason = "Turn timeout expired"
        )

        val connectionSkip = SkipTurnRequest(
            playerName = "Player3",
            reason = "Network connection lost"
        )

        val emergencySkip = SkipTurnRequest(
            playerName = "Player4",
            reason = "Emergency skip requested"
        )
        
        assertEquals("Player manually skipped turn", manualSkip.reason)
        assertEquals("Turn timeout expired", timeoutSkip.reason)
        assertEquals("Network connection lost", connectionSkip.reason)
        assertEquals("Emergency skip requested", emergencySkip.reason)
    }

    @Test
    fun testSkipRequestValidationReadiness() {
        val validRequest = SkipTurnRequest(
            playerName = "ValidPlayer",
            reason = "Valid reason"
        )
        
        val invalidPlayerRequest = SkipTurnRequest(
            playerName = "",
            reason = "Valid reason"
        )
        
        assertTrue(validRequest.playerName.isNotBlank())
        assertTrue(validRequest.reason.isNotBlank())
        
        assertTrue(invalidPlayerRequest.playerName.isBlank())
        assertTrue(invalidPlayerRequest.reason.isNotBlank())
    }

    @Test
    fun testDefaultSkipReasonIsMeaningful() {
        val request = SkipTurnRequest(playerName = "TestPlayer")
        
        assertEquals("Player manually skipped turn", request.reason)
        assertTrue(request.reason.contains("Player"))
        assertTrue(request.reason.contains("skip"))
        assertTrue(request.reason.length > 10)
    }
} 