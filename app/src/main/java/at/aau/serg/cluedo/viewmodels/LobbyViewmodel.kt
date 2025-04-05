package at.aau.serg.cluedo.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.cluedo.models.CreateLobbyRequest
import at.aau.serg.cluedo.models.JoinLobbyRequest
import at.aau.serg.cluedo.models.Lobby
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompMessage
import ua.naiksoftware.stomp.dto.LifecycleEvent

class LobbyViewModel : ViewModel() {

    companion object {
        private const val SERVER_IP = "10.0.2.2"
        private const val SERVER_PORT = "8080"
        private const val CONNECTION_URL = "ws://$SERVER_IP:$SERVER_PORT/ws"
        private const val TOPIC_LOBBY_CREATED = "/topic/lobbyCreated"
        private const val TOPIC_LOBBY_UPDATES_PREFIX = "/topic/lobby/"
        private const val APP_CREATE_LOBBY = "/app/createLobby"
        private const val APP_JOIN_LOBBY_PREFIX = "/app/joinLobby/"
    }

    private val gson = Gson()
    private var stompClient: StompClient? = null
    private var currentLobbySubscriptionId: String? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _lobbyState = MutableStateFlow<Lobby?>(null)
    val lobbyState: StateFlow<Lobby?> = _lobbyState.asStateFlow()

    private val _createdLobbyId = MutableStateFlow<String?>(null)
    val createdLobbyId: StateFlow<String?> = _createdLobbyId.asStateFlow()

    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages: SharedFlow<String> = _errorMessages.asSharedFlow()


    init {
        setupStompClient()
    }

    private fun emitError(userMessage: String) {
        viewModelScope.launch { _errorMessages.emit(userMessage) }
    }

    private fun resetConnectionState() {
        _isConnected.value = false
        _lobbyState.value = null
        currentLobbySubscriptionId = null
        _createdLobbyId.value = null
    }

    @SuppressLint("CheckResult")
    private fun setupStompClient() {
        if (stompClient != null) {
            return
        }
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, CONNECTION_URL)

        stompClient?.lifecycle()?.subscribe(
            { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        _isConnected.value = true
                        subscribeToLobbyCreationTopic()
                    }
                    LifecycleEvent.Type.ERROR -> {
                        emitError("Connection Error: ${lifecycleEvent.exception?.message}")
                        resetConnectionState()
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        resetConnectionState()
                    }
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                        emitError("Server heartbeat failed")
                    }
                }
            },
            {
                emitError("Lifecycle Subscription Error")
            }
        )
    }

    fun connect() {
        if (stompClient == null) {
            setupStompClient()
        }
        if (_isConnected.value || stompClient?.isConnected == true) {
            return
        }
        stompClient?.connect()
    }

    fun disconnect() {
        stompClient?.disconnect()
        if (_isConnected.value) {
            resetConnectionState()
        }
    }

    @SuppressLint("CheckResult")
    private fun subscribeToLobbyCreationTopic() {
        stompClient?.topic(TOPIC_LOBBY_CREATED)?.subscribe(
            { stompMessage: StompMessage ->
                val newLobbyId = stompMessage.payload
                _createdLobbyId.value = newLobbyId
            },
            {
                emitError("Error receiving lobby creation confirmation")
            }
        )
    }

    @SuppressLint("CheckResult")
    private fun subscribeToLobbyUpdates(lobbyId: String) {
        val topicPath = "$TOPIC_LOBBY_UPDATES_PREFIX$lobbyId"

        if (topicPath == currentLobbySubscriptionId) {
            return
        }
        currentLobbySubscriptionId = topicPath

        stompClient?.topic(topicPath)?.subscribe(
            { stompMessage: StompMessage ->
                try {
                    val lobby = gson.fromJson(stompMessage.payload, Lobby::class.java)
                    _lobbyState.value = lobby
                } catch (e: Exception) {
                    emitError("Failed to parse lobby data")
                }
            },
            {
                emitError("Subscription error for lobby $lobbyId")
                if (currentLobbySubscriptionId == topicPath) {
                    currentLobbySubscriptionId = null
                    _lobbyState.value = null
                }
            }
        )
    }

    @SuppressLint("CheckResult")
    fun createLobby(username: String) {
        if (!_isConnected.value) {
            emitError("Not connected to server")
            return
        }
        val request = CreateLobbyRequest(username)
        val payload = gson.toJson(request)

        stompClient?.send(APP_CREATE_LOBBY, payload)?.subscribe()
    }

    @SuppressLint("CheckResult")
    fun joinLobby(lobbyId: String, username: String) {
        if (!_isConnected.value) {
            emitError("Not connected to server")
            return
        }
        if (lobbyId.isBlank()) {
            emitError("Lobby ID cannot be empty")
            return
        }

        subscribeToLobbyUpdates(lobbyId)

        val request = JoinLobbyRequest(username)
        val payload = gson.toJson(request)
        val destination = "$APP_JOIN_LOBBY_PREFIX$lobbyId"

        stompClient?.send(destination, payload)?.subscribe()
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}