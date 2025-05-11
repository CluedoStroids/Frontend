package at.aau.se2.cluedo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.se2.cluedo.data.models.GameStartedResponse
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.PlayerColor
import at.aau.se2.cluedo.data.network.WebSocketService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LobbyViewModel(val webSocketService: WebSocketService = WebSocketService.getInstance()) : ViewModel() {

    val isConnected: StateFlow<Boolean> = webSocketService.isConnected
    val lobbyState: StateFlow<Lobby?> = webSocketService.lobbyState
    val createdLobbyId: StateFlow<String?> = webSocketService.createdLobbyId
    // Create our own error messages flow since WebSocketService doesn't have one
    private val _errorMessages = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 10)
    val errorMessages: SharedFlow<String> = _errorMessages
    val canStartGame: StateFlow<Boolean> = webSocketService.canStartGame
    val gameStarted: StateFlow<Boolean> = webSocketService.gameStarted
    val gameState: StateFlow<GameStartedResponse?> = webSocketService.gameState

    fun connect() {
        webSocketService.connect()
        webSocketService.getActiveLobbies()
    }

    fun getActiveLobbies() {
        webSocketService.getActiveLobbies()
    }

    fun disconnect() {
        webSocketService.disconnect()
    }

    fun createLobby(username: String, character: String = "Red") {
        viewModelScope.launch {
            val color = getColorForCharacter(character)
            webSocketService.createLobby(username, character, color)
        }
    }

    fun joinLobby(lobbyId: String, username: String, character: String = "Blue") {
        viewModelScope.launch {
            val color = getColorForCharacter(character)
            webSocketService.joinLobby(lobbyId, username, character, color)
        }
    }

    fun leaveLobby(lobbyId: String, username: String, character: String = "Blue") {
        viewModelScope.launch {
            val color = getColorForCharacter(character)
            webSocketService.leaveLobby(lobbyId, username, character, color)
        }
    }

    fun checkCanStartGame(lobbyId: String) {
        viewModelScope.launch {
            webSocketService.checkCanStartGame(lobbyId)
        }
    }

    fun startGame(lobbyId: String, username: String, character: String) {
        viewModelScope.launch {
            val color = getColorForCharacter(character)
            webSocketService.startGame(lobbyId, username, character, color)
        }
    }

    fun setGameStarted(started: Boolean) {
        viewModelScope.launch {
            if (started) {
                // If we're setting game started to true, make sure we have a game state
                lobbyState.value?.let { lobby ->
                    if (gameState.value == null) {
                        // Create a temporary game state from the lobby
                        val tempGameState = GameStartedResponse(
                            lobbyId = lobby.id,
                            players = lobby.players
                        )
                        // We need to manually set the game state in WebSocketService
                        // This is a workaround since we don't have a direct setter
                        webSocketService.startGame(lobby.id, lobby.host.name, lobby.host.character, lobby.host.color)
                    }
                }
            }
        }
    }

    fun logMessage(message: String) {
        // Log to console and emit to error messages flow
        println("[LobbyViewModel] $message")
        viewModelScope.launch {
            _errorMessages.emit(message)
        }
    }

    fun checkGameStarted() {
        viewModelScope.launch {
            // Check if we have a game state
            if (gameState.value != null) {
                // If we have a game state, make sure gameStarted is true
                if (!gameStarted.value) {
                    setGameStarted(true)
                }
            } else {
                // If we don't have a game state, check if we have a lobby
                lobbyState.value?.let { lobby ->
                    // If we have a lobby with at least 3 players, we can start the game
                    if (lobby.players.size >= 3) {
                        // Check if we can start the game
                        checkCanStartGame(lobby.id)
                    }
                }
            }
        }
    }

    val availableCharacters = listOf("Red", "Blue", "Green", "Yellow", "Purple", "White")

    private fun getColorForCharacter(character: String): PlayerColor {
        return try {
            PlayerColor.valueOf(character.uppercase())
        } catch (e: IllegalArgumentException) {
            PlayerColor.RED
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}