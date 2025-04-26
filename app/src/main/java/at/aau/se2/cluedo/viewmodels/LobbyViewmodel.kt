package at.aau.se2.cluedo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.network.WebSocketService
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LobbyViewModel : ViewModel() {

    private val webSocketService = WebSocketService()

    val isConnected: StateFlow<Boolean> = webSocketService.isConnected
    val lobbyState: StateFlow<Lobby?> = webSocketService.lobbyState
    val createdLobbyId: StateFlow<String?> = webSocketService.createdLobbyId
    val errorMessages: SharedFlow<String> = webSocketService.errorMessages

    fun connect() {
        webSocketService.connect()
        webSocketService.getActiveLobby()
    }

    fun getActiveLobby() {
        webSocketService.getActiveLobby()
    }

    fun disconnect() {
        webSocketService.disconnect()
    }

    fun createLobby(username: String, character: String = "Red") {
        viewModelScope.launch {
            webSocketService.createLobby(username, character)
        }
    }

    fun joinLobby(lobbyId: String, username: String, character: String = "Blue") {
        viewModelScope.launch {
            webSocketService.joinLobby(lobbyId, username, character)
        }
    }

    fun leaveLobby(lobbyId: String, username: String, character: String = "Blue") {
        viewModelScope.launch {
            webSocketService.leaveLobby(lobbyId, username, character)
        }
    }

    val availableCharacters = listOf("Red", "Blue", "Green", "Yellow", "Purple", "White")

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}