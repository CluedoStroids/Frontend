package at.aau.se2.cluedo.data.models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LobbyResponsesTests {

    @Test
    fun testActiveLobbiesResponse() {
        val emptyResponse = ActiveLobbiesResponse()
        assertTrue(emptyResponse.lobbies.isEmpty())

        val lobbyInfo1 = ActiveLobbiesResponse.LobbyInfo(
            id = "lobby-1",
            hostName = "Host1",
            playerCount = 2
        )
        val lobbyInfo2 = ActiveLobbiesResponse.LobbyInfo(
            id = "lobby-2",
            hostName = "Host2",
            playerCount = 3
        )
        val response = ActiveLobbiesResponse(listOf(lobbyInfo1, lobbyInfo2))

        assertEquals(2, response.lobbies.size)
        assertEquals("lobby-1", response.lobbies[0].id)
        assertEquals("Host1", response.lobbies[0].hostName)
        assertEquals(2, response.lobbies[0].playerCount)
        assertEquals("lobby-2", response.lobbies[1].id)
    }

    @Test
    fun testActiveLobbiesResponse_LobbyInfo() {
        val defaultInfo = ActiveLobbiesResponse.LobbyInfo()
        assertEquals("", defaultInfo.id)
        assertEquals("", defaultInfo.hostName)
        assertEquals(0, defaultInfo.playerCount)

        val customInfo = ActiveLobbiesResponse.LobbyInfo(
            id = "custom-lobby",
            hostName = "host",
            playerCount = 4
        )
        assertEquals("custom-lobby", customInfo.id)
        assertEquals("host", customInfo.hostName)
        assertEquals(4, customInfo.playerCount)
    }

    @Test
    fun testCanStartGameResponse() {
        val defaultResponse = CanStartGameResponse()
        assertFalse(defaultResponse.canStart)

        val customResponse = CanStartGameResponse(true)
        assertTrue(customResponse.canStart)
    }

    @Test
    fun testGameStartedResponse() {
        val defaultResponse = GameStartedResponse()
        assertEquals("", defaultResponse.lobbyId)
        assertTrue(defaultResponse.players.isEmpty())

        val player1 = Player(name = "Player1", character = "Red", color = PlayerColor.RED)
        val player2 = Player(name = "Player2", character = "Blue", color = PlayerColor.BLUE)
        val players = listOf(player1, player2)

        val customResponse = GameStartedResponse(
            lobbyId = "lobby-123",
            players = players
        )

        assertEquals("lobby-123", customResponse.lobbyId)
        assertEquals(2, customResponse.players.size)
        assertEquals(player1, customResponse.players[0])
        assertEquals(player2, customResponse.players[1])
    }

    @Test
    fun testPerformMoveResponse() {
        val defaultResponse = PerformMoveResponse()
        assertEquals("", defaultResponse.player.name)
        assertEquals("", defaultResponse.player.character)
        assertTrue(defaultResponse.moves.isEmpty())

        val player = Player(name = "TestPlayer", character = "Green", color = PlayerColor.GREEN)
        val moves = listOf("up", "right", "down")
        val customResponse = PerformMoveResponse(player = player, moves = moves)

        assertEquals(player, customResponse.player)
        assertEquals(3, customResponse.moves.size)
        assertEquals("up", customResponse.moves[0])
        assertEquals("right", customResponse.moves[1])
        assertEquals("down", customResponse.moves[2])
    }
} 