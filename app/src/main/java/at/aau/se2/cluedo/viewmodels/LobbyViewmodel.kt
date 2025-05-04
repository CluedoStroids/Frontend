package at.aau.se2.cluedo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.network.WebSocketService
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class LobbyViewModel : ViewModel() {

    private val webSocketService = WebSocketService()

    val isConnected: StateFlow<Boolean> = webSocketService.isConnected
    val lobbyState: StateFlow<Lobby?> = webSocketService.lobbyState
    val createdLobbyId: StateFlow<String?> = webSocketService.createdLobbyId
    val errorMessages: SharedFlow<String> = webSocketService.errorMessages

    fun connect() {
        webSocketService.connect()
    }

    fun disconnect() {
        webSocketService.disconnect()
    }

    fun createLobby(username: String) {
        viewModelScope.launch {
            webSocketService.createLobby(username)
        }
    }

    fun joinLobby(lobbyId: String, username: String) {
        viewModelScope.launch {
            webSocketService.joinLobby(lobbyId, username)
        }
    }

    fun leaveLobby(lobbyId: String, username: String) {
        viewModelScope.launch {
            webSocketService.leaveLobby(lobbyId, username)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    fun solveCase(lobbyId: String, username: String, suspect: String, room: String, weapon: String) {
        viewModelScope.launch {
            webSocketService.solveCase(lobbyId, username, suspect, room, weapon)
        }
    }

    init {
        viewModelScope.launch {
            lobbyState.collectLatest { lobby ->
                android.util.Log.d("LOBBY_JSON", "Lobby update: $lobby")
            }
        }
    }
}