package at.aau.se2.cluedo

import at.aau.se2.cluedo.viewmodels.GameViewModel
import at.aau.se2.cluedo.data.models.*
import at.aau.se2.cluedo.data.network.TurnBasedWebSocketService
import at.aau.se2.cluedo.data.network.WebSocketService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.* // Import all Mockito-Kotlin functions

@ExperimentalCoroutinesApi
class GameViewModelTest {

    private lateinit var mockWebSocketService: WebSocketService
    private lateinit var mockTurnBasedWebSocketService: TurnBasedWebSocketService
    private lateinit var mockPlayer: Player

    private val mockSuggestionDataFlow = MutableStateFlow<SuggestionRequest?>(null)
    private val mockProcessSuggestionFlow = MutableStateFlow<Boolean>(false)
    private val mockResultSuggestionFlow = MutableStateFlow<SuggestionResponse?>(null)
    private lateinit var viewModel: GameViewModel

    private val testDispatcher =  StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockWebSocketService = mock(WebSocketService::class.java)
        mockTurnBasedWebSocketService = mock(TurnBasedWebSocketService::class.java)
        mockPlayer = mock<Player>()

        whenever(mockWebSocketService.player).thenReturn(MutableStateFlow(mockPlayer))

        doReturn(mockSuggestionDataFlow).whenever(mockTurnBasedWebSocketService).suggestionData
        doReturn(mockProcessSuggestionFlow).whenever(mockTurnBasedWebSocketService).processSuggestion
        doReturn(mockResultSuggestionFlow).whenever(mockTurnBasedWebSocketService).resultSuggestion

        whenever(mockTurnBasedWebSocketService.suggestionData).thenReturn(mockSuggestionDataFlow)
        whenever(mockTurnBasedWebSocketService.processSuggestion).thenReturn(mockProcessSuggestionFlow)
        whenever(mockTurnBasedWebSocketService.resultSuggestion).thenReturn(mockResultSuggestionFlow)


        viewModel = GameViewModel(mockWebSocketService, mockTurnBasedWebSocketService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


}