package at.aau.se2.cluedo.data.models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LobbyTests {

    @Test
    fun testLobbyConstructorEmpty() {
        val lobby = Lobby()

        assertEquals("", lobby.id)

        assertEquals("", lobby.host.name)
        assertEquals("", lobby.host.character)

        assertEquals(emptyList<String>(), lobby.participants)
        assertEquals(emptyList<Player>(), lobby.players)
        assertNull(lobby.winnerUsername)
    }

    @Test
    fun testLobbyConstructorValues() {
        val host = Player(name = "Host", character = "Red", color = PlayerColor.RED)
        val player1 = Player(name = "Player1", character = "RED", color = PlayerColor.RED) // TODO: fix bug that allows same color more than once
        val player2 = Player(name = "Player2", character = "Green", color = PlayerColor.GREEN)
        val players = listOf(host, player1, player2)
        val participants = listOf("Host", "Player1", "Player2")

        val lobby = Lobby(
            id = "lobby",
            host = host,
            participants = participants,
            players = players,
            winnerUsername = "Player1"
        )

        assertEquals("lobby", lobby.id)
        assertEquals(host, lobby.host)
        assertEquals(participants, lobby.participants)
        assertEquals(players, lobby.players)
        assertEquals("Player1", lobby.winnerUsername)
    }


    @Test
    fun testLobbyStatusEnum() {
        assertEquals("Creating...", LobbyStatus.CREATING.text)
    }
}
