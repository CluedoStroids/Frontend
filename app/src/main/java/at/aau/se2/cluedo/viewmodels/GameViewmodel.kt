package at.aau.se2.cluedo.viewmodels

import androidx.lifecycle.ViewModel
import at.aau.se2.cluedo.data.models.GameStartedResponse
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.network.WebSocketService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class GameViewmodel(val webSocketService: WebSocketService = WebSocketService.getInstance()) : ViewModel() {

    val isConnected: StateFlow<Boolean> = webSocketService.isConnected
    val lobbyState: StateFlow<Lobby?> = webSocketService.lobbyState
    val createdLobbyId: StateFlow<String?> = webSocketService.createdLobbyId
    // Create our own error messages flow since WebSocketService doesn't have one
    private val _errorMessages = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 10)
    val errorMessages: SharedFlow<String> = _errorMessages
    val canStartGame: StateFlow<Boolean> = webSocketService.canStartGame
    val gameStarted: StateFlow<Boolean> = webSocketService.gameStarted
    val gameState: StateFlow<GameStartedResponse?> = webSocketService.gameState

    //todo gamelogic I guess

}