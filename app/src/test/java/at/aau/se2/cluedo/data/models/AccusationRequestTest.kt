package at.aau.se2.cluedo.data.models

    import org.junit.jupiter.api.Assertions.assertEquals
    import org.junit.jupiter.api.Test
    import org.junit.jupiter.api.Assertions.assertTrue



    class AccusationRequestTest {

        @Test
        fun `AccusationRequest should hold correct values`() {
            val request = AccusationRequest(
                lobbyId = "lobby123",
                playerName = "Matthias",
                suspect = "Miss Scarlet",
                weapon = "Rope",
                room = "Kitchen"
            )

            assertEquals("lobby123", request.lobbyId)
            assertEquals("Matthias", request.playerName)
            assertEquals("Miss Scarlet", request.suspect)
            assertEquals("Rope", request.weapon)
            assertEquals("Kitchen", request.room)
        }

        @Test
        fun `default constructor should set all fields to empty strings`() {
            val request = AccusationRequest()

            assertEquals("", request.lobbyId)
            assertEquals("", request.playerName)
            assertEquals("", request.suspect)
            assertEquals("", request.weapon)
            assertEquals("", request.room)
        }

        @Test
        fun `toString should contain all fields`() {
            val request = AccusationRequest("lobby123", "Matthias", "Miss Scarlet", "Rope", "Kitchen")
            val output = request.toString()

            assertTrue(output.contains("lobby123"))
            assertTrue(output.contains("Matthias"))
            assertTrue(output.contains("Miss Scarlet"))
            assertTrue(output.contains("Rope"))
            assertTrue(output.contains("Kitchen"))
        }

        @Test
        fun `copy should retain the same values`() {
            val original = AccusationRequest("lobby123", "Matthias", "Miss Scarlet", "Rope", "Kitchen")
            val copy = original.copy()

            assertEquals(original, copy)
        }


    }
