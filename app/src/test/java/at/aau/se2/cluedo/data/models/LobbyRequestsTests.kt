package at.aau.se2.cluedo.data.models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LobbyRequestsTests {

    @Test
    fun testCreateLobbyRequest() {
        val player = Player(name = "Player", character = "Red", color = PlayerColor.RED)
        val request = CreateLobbyRequest(player)

        assertEquals(player, request.player)
        assertTrue(request is PlayerRequest)
    }

    @Test
    fun testJoinLobbyRequest() {
        val player = Player(name = "Player", character = "Blue", color = PlayerColor.BLUE)
        val request = JoinLobbyRequest(player)

        assertEquals(player, request.player)
        assertTrue(request is PlayerRequest)
    }

    @Test
    fun testLeaveLobbyRequest() {
        val player = Player(name = "Player", character = "Blue", color = PlayerColor.BLUE)
        val request = LeaveLobbyRequest(player)

        assertEquals(player, request.player)
        assertTrue(request is PlayerRequest)
    }

    @Test
    fun testGetActiveLobbiesRequest() {
        val request = GetActiveLobbiesRequest()
        assertEquals("", request.dummy)

        val customRequest = GetActiveLobbiesRequest("custom")
        assertEquals("custom", customRequest.dummy)
    }

    @Test
    fun testStartGameRequest() {
        val player = Player(name = "player", character = "Yellow", color = PlayerColor.YELLOW)
        val request = StartGameRequest(player)

        assertEquals(player, request.player)
        assertTrue(request is PlayerRequest)
    }

    @Test
    fun testIsWallRequest() {
        val defaultRequest = IsWallRequest()
        assertEquals(0, defaultRequest.x)
        assertEquals(0, defaultRequest.y)

        val customRequest = IsWallRequest(x = 5, y = 10)
        assertEquals(5, customRequest.x)
        assertEquals(10, customRequest.y)
    }
}
