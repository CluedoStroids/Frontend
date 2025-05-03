package at.aau.se2.cluedo

import at.aau.se2.cluedo.data.models.PlayerColor
import at.aau.se2.cluedo.data.network.WebSocketService
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LobbyViewModelUnitTests {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockWebSocketService: WebSocketService
    private lateinit var viewModel: LobbyViewModel

    private val lobbyStateFlow = MutableStateFlow(null as at.aau.se2.cluedo.data.models.Lobby?)
    private val createdLobbyIdFlow = MutableStateFlow(null as String?)
    private val errorMessagesFlow = MutableSharedFlow<String>()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockWebSocketService = mock(WebSocketService::class.java)

        /*
           mockWebSocketService = mock {

            on { lobbyState } doReturn lobbyStateFlow
            on { createdLobbyId } doReturn createdLobbyIdFlow
            on { errorMessages } doReturn errorMessagesFlow
        }


         */

        viewModel = LobbyViewModel(mockWebSocketService)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testConnect() {
        whenever(mockWebSocketService.isConnected).thenReturn(MutableStateFlow(false))

        viewModel.connect()

        verify(mockWebSocketService, times(1)).connect()
        verify(mockWebSocketService, times(1)).getActiveLobby()
    }

    @Test
    fun testgetActiveLobby() {
        whenever(mockWebSocketService.isConnected).thenReturn(MutableStateFlow(false))

        viewModel.getActiveLobby()

        verify(mockWebSocketService, times(1)).getActiveLobby()
    }

    @Test
    fun testDisconnect() {
        whenever(mockWebSocketService.isConnected).thenReturn(MutableStateFlow(true))

        viewModel.disconnect()

        verify(mockWebSocketService, times(1)).disconnect()
    }

    @Test
    fun testCreateLobby() {
        val username = "John"
        val character = "Blue"

        viewModel.createLobby(username, character)
        testDispatcher.scheduler.advanceUntilIdle() //because createLobby() calls suspend function (launch)

        verify(mockWebSocketService).createLobby(username, character, PlayerColor.BLUE)
    }

    @Test
    fun testJoinLobby() {
        val lobbyId = "SampleLobbyId"
        val username = "John"
        val character = "Blue"

        viewModel.joinLobby(lobbyId, username, character)
        testDispatcher.scheduler.advanceUntilIdle() //because joinLobby() calls suspend function (launch)

        verify(mockWebSocketService).joinLobby(lobbyId, username, character, PlayerColor.BLUE)
    }

    @Test
    fun testLeaveLobby() {
        val lobbyId = "SampleLobbyId"
        val username = "John"
        val character = "Blue"

        viewModel.leaveLobby(lobbyId, username, character)
        testDispatcher.scheduler.advanceUntilIdle() //because leaveLobby() calls suspend function (launch)

        verify(mockWebSocketService).leaveLobby(lobbyId, username, character, PlayerColor.BLUE)
    }

}