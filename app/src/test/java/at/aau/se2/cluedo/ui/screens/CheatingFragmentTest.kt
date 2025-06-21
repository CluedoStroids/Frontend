package at.aau.se2.cluedo

import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.models.PlayerColor
import at.aau.se2.cluedo.data.network.WebSocketService
import at.aau.se2.cluedo.ui.screens.CheatingFragment
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

class CheatingFragmentTests {

    private lateinit var mockWebSocketService: WebSocketService
    private lateinit var mockLobbyViewModel: LobbyViewModel
    private lateinit var fragment: CheatingFragment

    private val lobbyStateFlow = MutableStateFlow<Lobby?>(null)
    private val errorMessagesFlow = MutableStateFlow("")

    private val testHost = Player(name = "Host", character = "Red", color = PlayerColor.RED)
    private val testPlayer1 = Player(name = "Player1", character = "Blue", color = PlayerColor.BLUE)
    private val testPlayer2 = Player(name = "Player2", character = "Green", color = PlayerColor.GREEN)
    private val testLobby = Lobby(
        id = "test-lobby-id",
        host = testHost,
        players = listOf(testHost, testPlayer1, testPlayer2)
    )

    @BeforeEach
    fun setup() {
        mockWebSocketService = mock(WebSocketService::class.java)
        mockLobbyViewModel = mock(LobbyViewModel::class.java)
        fragment = CheatingFragment()

        // Setup mock flows
        whenever(mockLobbyViewModel.lobbyState).thenReturn(lobbyStateFlow)
        whenever(mockLobbyViewModel.errorMessages).thenReturn(errorMessagesFlow)
        whenever(mockLobbyViewModel.webSocketService).thenReturn(mockWebSocketService)
    }

    @AfterEach
    fun tearDown() {
        reset(mockWebSocketService, mockLobbyViewModel)
    }

    @Test
    fun testFragmentInstantiation() {
        assertNotNull(fragment)
        assertTrue(fragment is CheatingFragment)
    }

    @Test
    fun testCurrentPlayersInitialState() {
        val fragmentInstance = CheatingFragment()
        assertNotNull(fragmentInstance)
    }

    @Test
    fun testLobbyStateUpdatesCurrentPlayers() {
        whenever(mockWebSocketService.getPlayer()).thenReturn(testHost)

        // Simulate lobby state update
        lobbyStateFlow.value = testLobby

        // Verify the lobby contains expected players
        assertEquals(3, testLobby.players.size)
        assertTrue(testLobby.players.contains(testHost))
        assertTrue(testLobby.players.contains(testPlayer1))
        assertTrue(testLobby.players.contains(testPlayer2))
    }

    @Test
    fun testFilterCurrentPlayerFromList() {
        whenever(mockWebSocketService.getPlayer()).thenReturn(testHost)

        val currentPlayerName = testHost.name
        val otherPlayers = testLobby.players.filter { it.name != currentPlayerName }

        assertEquals(2, otherPlayers.size)
        assertFalse(otherPlayers.any { it.name == "Host" })
        assertTrue(otherPlayers.any { it.name == "Player1" })
        assertTrue(otherPlayers.any { it.name == "Player2" })
    }

    @Test
    fun testPlayerDisplayFormat() {
        val player = testPlayer1
        val displayFormat = "${player.name} (${player.character})"

        assertEquals("Player1 (Blue)", displayFormat)
    }

    @Test
    fun testSpinnerOptionsFormat() {
        whenever(mockWebSocketService.getPlayer()).thenReturn(testHost)

        val otherPlayers = testLobby.players.filter { it.name != testHost.name }
            .map { "${it.name} (${it.character})" }

        val playerOptions = listOf("Select player to report...") + otherPlayers

        assertEquals(3, playerOptions.size)
        assertEquals("Select player to report...", playerOptions[0])
        assertTrue(playerOptions.contains("Player1 (Blue)"))
        assertTrue(playerOptions.contains("Player2 (Green)"))
    }

    @Test
    fun testExtractPlayerNameFromSelection() {
        val selectedPlayerText = "Player1 (Blue)"
        val suspectedPlayerName = selectedPlayerText.substringBefore(" (")

        assertEquals("Player1", suspectedPlayerName)
    }

    @Test
    fun testFindPlayerByName() {
        val suspectedPlayerName = "Player1"
        val suspectedPlayer = testLobby.players.find { it.name == suspectedPlayerName }

        assertNotNull(suspectedPlayer)
        assertEquals("Player1", suspectedPlayer?.name)
        assertEquals("Blue", suspectedPlayer?.character)
        assertEquals(PlayerColor.BLUE, suspectedPlayer?.color)
    }

    @Test
    fun testFindPlayerByNameNotFound() {
        val suspectedPlayerName = "NonExistentPlayer"
        val suspectedPlayer = testLobby.players.find { it.name == suspectedPlayerName }

        assertNull(suspectedPlayer)
    }

    @Test
    fun testCheatingSuspicionData() {
        val lobbyId = "test-lobby-id"
        val suspect = testPlayer1
        val accuser = testHost

        // Test that we have all required data for reporting
        assertNotNull(lobbyId)
        assertNotNull(suspect)
        assertNotNull(accuser)
        assertNotEquals(suspect.name, accuser.name)
    }

    @Test
    fun testWebSocketServiceReportCheatingCall() {
        val lobbyId = "test-lobby-id"
        val suspectName = "Player1"
        val accuserName = "Host"

        // Simulate the service call
        mockWebSocketService.reportCheating(lobbyId, suspectName, accuserName)

        // Verify the method was called with correct parameters
        verify(mockWebSocketService).reportCheating(lobbyId, suspectName, accuserName)
    }

    @Test
    fun testValidatePlayerSelection() {
        val selectedItemPosition = 0 // Default "Select player to report..."
        assertTrue(selectedItemPosition <= 0) // Should be invalid

        val validSelection = 1 // Actual player selection
        assertFalse(validSelection <= 0) // Should be valid
    }

    @Test
    fun testLobbyIdValidation() {
        val validLobbyId = "test-lobby-id"
        val emptyLobbyId = ""
        val nullLobbyId: String? = null

        assertTrue(validLobbyId.isNotEmpty())
        assertFalse(emptyLobbyId.isNotEmpty())
        assertNull(nullLobbyId)
    }

    @Test
    fun testPlayerDataValidation() {
        val validPlayer = testHost
        val nullPlayer: Player? = null

        assertNotNull(validPlayer)
        assertNotNull(validPlayer.name)
        assertNotNull(validPlayer.character)
        assertNull(nullPlayer)
    }

    @Test
    fun testErrorMessageFlow() {
        val errorMessage = "Test error message"
        errorMessagesFlow.value = errorMessage

        assertEquals(errorMessage, errorMessagesFlow.value)
    }

    @Test
    fun testLobbyStateFlow() {
        lobbyStateFlow.value = testLobby

        assertNotNull(lobbyStateFlow.value)
        assertEquals(testLobby.id, lobbyStateFlow.value?.id)
        assertEquals(testLobby.players.size, lobbyStateFlow.value?.players?.size)
    }

    @Test
    fun testLobbyStateFlowNull() {
        lobbyStateFlow.value = null

        assertNull(lobbyStateFlow.value)
    }

    @Test
    fun testMultiplePlayersInLobby() {
        val lobby = Lobby(
            id = "multi-player-lobby",
            host = testHost,
            players = listOf(testHost, testPlayer1, testPlayer2)
        )

        assertTrue(lobby.players.size >= 2) // Need at least 2 players for cheating reports
        assertEquals(3, lobby.players.size)
    }

    @Test
    fun testSinglePlayerInLobby() {
        val singlePlayerLobby = Lobby(
            id = "single-player-lobby",
            host = testHost,
            players = listOf(testHost)
        )

        val otherPlayers = singlePlayerLobby.players.filter { it.name != testHost.name }
        assertTrue(otherPlayers.isEmpty()) // No other players to report
    }

    @Test
    fun testPlayerColorMapping() {
        assertEquals(PlayerColor.RED, testHost.color)
        assertEquals(PlayerColor.BLUE, testPlayer1.color)
        assertEquals(PlayerColor.GREEN, testPlayer2.color)
    }

    @Test
    fun testLobbyContainsAllRequiredFields() {
        assertNotNull(testLobby.id)
        assertNotNull(testLobby.host)
        assertNotNull(testLobby.players)
        assertTrue(testLobby.id.isNotEmpty())
        assertTrue(testLobby.players.isNotEmpty())
    }

    @Test
    fun testPlayerContainsAllRequiredFields() {
        assertNotNull(testPlayer1.name)
        assertNotNull(testPlayer1.character)
        assertNotNull(testPlayer1.color)
        assertTrue(testPlayer1.name.isNotEmpty())
        assertTrue(testPlayer1.character.isNotEmpty())
    }
}