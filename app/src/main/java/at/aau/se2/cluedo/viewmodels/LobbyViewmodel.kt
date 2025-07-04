package at.aau.se2.cluedo.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.models.PlayerColor
import at.aau.se2.cluedo.data.network.WebSocketService
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import at.aau.se2.cluedo.data.models.GameStartedResponse
import at.aau.se2.cluedo.data.network.TurnBasedWebSocketService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject



class LobbyViewmodel(val webSocketService: WebSocketService = WebSocketService.getInstance(),
                     val turnBasedWebSocketService: TurnBasedWebSocketService = TurnBasedWebSocketService.getInstance()) :
    ViewModel() {

    val isConnected: StateFlow<Boolean> = webSocketService.isConnected
    val lobbyState: StateFlow<Lobby?> = webSocketService.lobbyState
    val createdLobbyId: StateFlow<String?> = webSocketService.createdLobbyId


    // Create our own error messages flow since WebSocketService doesn't have one
    private val _errorMessages = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 10)
    val errorMessages: SharedFlow<String> = _errorMessages
    val canStartGame: StateFlow<Boolean> = webSocketService.canStartGame
    val gameStarted: StateFlow<Boolean> = webSocketService.gameStarted
    val gameState: StateFlow<GameStartedResponse?> = webSocketService.gameState
    private val _navigationEvents = MutableSharedFlow<NavigationTarget>()
    val navigationEvents: SharedFlow<NavigationTarget> = _navigationEvents

    // Notes, category, player isChecked
    private val _playerNotes = MutableStateFlow(
        mutableMapOf<String, MutableMap<String, Boolean>>() // category -> (player -> checked)
    )
    val playerNotes: StateFlow<MutableMap<String, MutableMap<String, Boolean>>> = _playerNotes

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
            webSocketService.setPlayer(Player(name = username, character = character, color = color))
        }
    }

    fun joinLobby(lobbyId: String, username: String, character: String = "Blue") {
        viewModelScope.launch {
            val color = getColorForCharacter(character)
            webSocketService.joinLobby(lobbyId, username, character, color)
            webSocketService.setPlayer(Player(name = username, character = character, color = color))
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
                        webSocketService.startGame(
                            lobby.id,
                            lobby.host.name,
                            lobby.host.character,
                            lobby.host.color
                        )
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

    private fun getLocalPlayerName(): String {
        return webSocketService.player.value?.name ?: ""
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

    private fun getColorForCharacter(character: String): PlayerColor {
        return try {
            PlayerColor.valueOf(character.uppercase())
        } catch (e: IllegalArgumentException) {
            PlayerColor.RED
        }
    }

    // Save a checkbox tick for a category + player
    fun setNote(category: String, player: String, checked: Boolean) {
        val notes = _playerNotes.value.toMutableMap()
        val playerMap = notes.getOrPut(category) { mutableMapOf() }
        playerMap[player] = checked
        _playerNotes.value = notes
    }

    // Check if a specific note is checked
    fun isNoteChecked(category: String, player: String): Boolean {
        return _playerNotes.value[category]?.get(player) == true
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }


    private val _suggestionNotes = MutableStateFlow<List<String>>(emptyList())
    val suggestionNotes: StateFlow<List<String>> = _suggestionNotes

    fun addSuggestionNote(note: String) {
        _suggestionNotes.value = _suggestionNotes.value + note
    }

    private var lastRoomEntered: String? = null
    private var hasSuggestedInThisRoom: Boolean = false

    fun updateRoomEntry(currentRoom: String?) {
        if (currentRoom != lastRoomEntered) {
            lastRoomEntered = currentRoom
            hasSuggestedInThisRoom = false
        }
    }

    fun canMakeSuggestion(): Boolean = !hasSuggestedInThisRoom

    fun markSuggestionMade() {
        hasSuggestedInThisRoom = true
    }

    fun isPlayerInRoom(player: Player?): Boolean {
        return player != null && RoomUtils.getRoomNameFromCoordinates(player.x, player.y) != null
    }

    fun subscribeToAccusationResult(lobbyId: String) {
        webSocketService.subscribe("/topic/accusationMade/$lobbyId") { message ->
            Log.d("LobbyViewModel", "Accusation result received: $message")
            val json = JSONObject(message)
            val correct = json.optBoolean("correct", false)
            val player = json.optString("player", "")
            val eliminated = json.optBoolean("playerEliminated", false)

            Log.d("LobbyViewModel", "Parsed accusation result - correct: $correct, player: $player, eliminated: $eliminated, localPlayer: ${getLocalPlayerName()}")

            viewModelScope.launch {
                when {
                    correct && player == getLocalPlayerName() -> {
                        Log.d("LobbyViewModel", "Navigating to WinScreen")
                        _navigationEvents.emit(NavigationTarget.WinScreen)
                    }
                    eliminated && player == getLocalPlayerName() -> {
                        Log.d("LobbyViewModel", "Navigating to EliminationScreen")
                        _navigationEvents.emit(NavigationTarget.EliminationScreen)
                    }
                    eliminated && player != getLocalPlayerName() -> {
                        Log.d("LobbyViewModel", "Navigating to EliminationUpdate for $player")
                        _navigationEvents.emit(NavigationTarget.EliminationUpdate(player))
                    }
                    correct && player != getLocalPlayerName() -> {
                        Log.d("LobbyViewModel", "Navigating to InvestigationUpdate for $player")
                        _navigationEvents.emit(NavigationTarget.InvestigationUpdate(player))
                    }
                }
            }
        }
    }
    val availableCharacters = listOf("Red", "Blue", "Green", "Yellow", "Purple", "White")

}


sealed class NavigationTarget {
    object WinScreen : NavigationTarget()
    object EliminationScreen : NavigationTarget()
    data class EliminationUpdate(val playerName: String) : NavigationTarget()
    data class InvestigationUpdate(val playerName: String) : NavigationTarget()
}


