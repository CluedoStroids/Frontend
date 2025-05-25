package at.aau.se2.cluedo


import at.aau.se2.cluedo.data.models.GameStartedResponse
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.models.PlayerColor
import at.aau.se2.cluedo.data.network.WebSocketService
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Assertions.*


class LobbyViewModelTests {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockWebSocketService: WebSocketService
    private lateinit var viewModel: LobbyViewModel

    private val isConnectedFlow = MutableStateFlow(true)
    private val lobbyStateFlow = MutableStateFlow<Lobby?>(null)
    private val createdLobbyIdFlow = MutableStateFlow<String?>(null)
    private val canStartGameFlow = MutableStateFlow(false)
    private val gameStartedFlow = MutableStateFlow(false)
    private val gameStateFlow = MutableStateFlow<GameStartedResponse?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockWebSocketService = mock(WebSocketService::class.java)

        whenever(mockWebSocketService.isConnected).thenReturn(isConnectedFlow)
        whenever(mockWebSocketService.lobbyState).thenReturn(lobbyStateFlow)
        whenever(mockWebSocketService.createdLobbyId).thenReturn(createdLobbyIdFlow)
        whenever(mockWebSocketService.canStartGame).thenReturn(canStartGameFlow)
        whenever(mockWebSocketService.gameStarted).thenReturn(gameStartedFlow)
        whenever(mockWebSocketService.gameState).thenReturn(gameStateFlow)

        viewModel = LobbyViewModel(mockWebSocketService)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testConnect() {
        viewModel.connect()

        verify(mockWebSocketService, times(1)).connect()
        verify(mockWebSocketService, times(1)).getActiveLobbies()
    }

    @Test
    fun testgetActiveLobby() {
        viewModel.getActiveLobbies()

        verify(mockWebSocketService, times(1)).getActiveLobbies()
    }

    @Test
    fun testDisconnect() {
        viewModel.disconnect()

        verify(mockWebSocketService, times(1)).disconnect()
    }

    @Test
    fun testCreateLobby() {
        val username = "John"
        val character = "Blue"

        viewModel.createLobby(username, character)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockWebSocketService).createLobby(username, character, PlayerColor.BLUE)
    }

    @Test
    fun testJoinLobby() {
        val lobbyId = "SampleLobbyId"
        val username = "John"
        val character = "Blue"

        viewModel.joinLobby(lobbyId, username, character)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockWebSocketService).joinLobby(lobbyId, username, character, PlayerColor.BLUE)
    }

    @Test
    fun testLeaveLobby() {
        val lobbyId = "SampleLobbyId"
        val username = "John"
        val character = "Blue"

        viewModel.leaveLobby(lobbyId, username, character)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockWebSocketService).leaveLobby(lobbyId, username, character, PlayerColor.BLUE)
    }

    @Test
    fun testCheckCanStartGame() {
        val lobbyId = "SampleLobbyId"

        viewModel.checkCanStartGame(lobbyId)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockWebSocketService).checkCanStartGame(lobbyId)
    }

    @Test
    fun testStartGame() {
        val lobbyId = "SampleLobbyId"
        val username = "John"
        val character = "Green"

        viewModel.startGame(lobbyId, username, character)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockWebSocketService).startGame(lobbyId, username, character, PlayerColor.GREEN)
    }

    @Test
    fun testSetGameStarted_whenLobbyExists() {
        val host = Player(name = "Host", character = "Red", color = PlayerColor.RED)
        val player = Player(name = "Player", character = "Blue", color = PlayerColor.BLUE)
        val lobby = Lobby(id = "test-lobby", host = host, players = listOf(host, player))
        lobbyStateFlow.value = lobby

        viewModel.setGameStarted(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockWebSocketService).startGame("test-lobby", "Host", "Red", PlayerColor.RED)
    }

    @Test
    fun testSetGameStarted_whenGameStateExists() {
        gameStateFlow.value = GameStartedResponse(lobbyId = "existing-game")

        viewModel.setGameStarted(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockWebSocketService, times(0)).startGame(
            anyString(),
            anyString(),
            anyString(),
            any()
        )
    }

    @Test
    fun testCheckGameStarted_whenGameStateExists() {
        gameStateFlow.value = GameStartedResponse(lobbyId = "existing-game")
        gameStartedFlow.value = false

        viewModel.checkGameStarted()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockWebSocketService, times(0)).checkCanStartGame(anyString())
    }

    @Test
    fun testCheckGameStarted_whenLobbyHasEnoughPlayers() {
        val host = Player(name = "Host", character = "Red", color = PlayerColor.RED)
        val player1 = Player(name = "Player1", character = "Blue", color = PlayerColor.BLUE)
        val player2 = Player(name = "Player2", character = "Green", color = PlayerColor.GREEN)
        val lobby = Lobby(id = "test-lobby", host = host, players = listOf(host, player1, player2))
        lobbyStateFlow.value = lobby

        viewModel.checkGameStarted()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockWebSocketService).checkCanStartGame("test-lobby")
    }
    @Test
    fun testSetNote_storesCheckboxValueCorrectly() {
        viewModel.setNote("Candlestick", "Green", true)

        val result = viewModel.isNoteChecked("Candlestick", "Green")
        assertTrue(result, "Note should be marked as checked.")
    }

    @Test
    fun testSetNote_unchecksNoteCorrectly() {
        viewModel.setNote("Kitchen", "Red", true)
        viewModel.setNote("Kitchen", "Red", false)

        val result = viewModel.isNoteChecked("Kitchen", "Red")
        assertFalse(result, "Note should be unchecked.")
    }

    @Test
    fun testIsNoteChecked_returnsFalseIfNotSet() {
        val result = viewModel.isNoteChecked("Ballroom", "Blue")
        assertFalse(result, "Unset note should return false by default.")
    }

    @Test
    fun testAddSuspicionNote_addsNoteToList() {
        val note = "Miss Scarlett — in the Kitchen — with the Dagger"
        viewModel.addSuspicionNote(note)

        val suspicionNotes = viewModel.suspicionNotes.value
        assertTrue(suspicionNotes.contains(note), "Suspicion note should be stored in the list.")
    }

    @Test
    fun testAddSuspicionNote_multipleNotes() {
        val note1 = "Professor Plum — in the Study — with the Revolver"
        val note2 = "Colonel Mustard — in the Lounge — with the Wrench"

        viewModel.addSuspicionNote(note1)
        viewModel.addSuspicionNote(note2)

        val suspicionNotes = viewModel.suspicionNotes.value
        assertEquals(2, suspicionNotes.size, "There should be 2 suspicion notes stored.")
        assertTrue(suspicionNotes.contains(note1))
        assertTrue(suspicionNotes.contains(note2))
    }

    @Test
    fun `initial state of a note should be unchecked`() {
        val result = viewModel.isNoteChecked("Candlestick", "Green")
        assertFalse(result)
    }

    @Test
    fun `checking a note should persist correctly`() {
        viewModel.setNote("Candlestick", "Green", true)
        val result = viewModel.isNoteChecked("Candlestick", "Green")
        assertTrue(result)
    }

    @Test
    fun `unchecking a previously checked note should be false`() {
        viewModel.setNote("Candlestick", "Green", true)
        viewModel.setNote("Candlestick", "Green", false)
        val result = viewModel.isNoteChecked("Candlestick", "Green")
        assertFalse(result)
    }

    @Test
    fun `independent notes should not affect each other`() {
        viewModel.setNote("Rope", "Red", true)
        assertTrue(viewModel.isNoteChecked("Rope", "Red"))
        assertFalse(viewModel.isNoteChecked("Candlestick", "Red"))
        assertFalse(viewModel.isNoteChecked("Rope", "Green"))
    }

}



