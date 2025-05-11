package at.aau.se2.cluedo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.PlayerColor
import at.aau.se2.cluedo.data.network.WebSocketService
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log


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

    fun addSuspicionNote(note: String) {
        Log.d("SuspicionNote", note)
    }

    fun leaveLobby(lobbyId: String, username: String, character: String = "Blue") {
        viewModelScope.launch {
            val color = getColorForCharacter(character)
            webSocketService.leaveLobby(lobbyId, username, character, color)
        }
    }

    val availableCharacters = listOf("Red", "Blue", "Green", "Yellow", "Purple", "White")

    private fun getColorForCharacter(character: String): PlayerColor {
        return when (character) {
            "Red" -> PlayerColor.RED
            "Blue" -> PlayerColor.BLUE
            "Green" -> PlayerColor.GREEN
            "Yellow" -> PlayerColor.YELLOW
            "Purple" -> PlayerColor.PURPLE
            "White" -> PlayerColor.WHITE
            else -> PlayerColor.RED
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    fun solveCase(lobbyId: String, username: String, suspect: String, room: String, weapon: String) {
        webSocketService.solveCase(lobbyId, username, suspect, room, weapon)
    }

    fun getActiveLobbies() {
        TODO("Not yet implemented")
    }
}