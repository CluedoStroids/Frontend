package at.aau.se2.cluedo.data.network

import android.annotation.SuppressLint
import at.aau.se2.cluedo.data.models.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage

class WebSocketService {
    companion object {
        private const val SERVER_IP = "10.0.2.2"
        private const val SERVER_PORT = "8080"
        private const val CONNECTION_URL = "ws://$SERVER_IP:$SERVER_PORT/ws"
        private const val TOPIC_LOBBY_CREATED = "/topic/lobbyCreated"
        private const val TOPIC_LOBBY_UPDATES_PREFIX = "/topic/lobby/"
        private const val APP_CREATE_LOBBY = "/app/createLobby"
        private const val APP_JOIN_LOBBY_PREFIX = "/app/joinLobby/"
        private const val APP_LEAVE_LOBBY_PREFIX = "/app/leaveLobby/"
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

    @SuppressLint("CheckResult")
    private fun setupStompClient() {
        if (stompClient != null) return

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, CONNECTION_URL)

        stompClient?.lifecycle()?.subscribe(
            { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        _isConnected.value = true
                        subscribeToLobbyCreationTopic()
                    }
                    LifecycleEvent.Type.ERROR -> {
                        _errorMessages.tryEmit("Connection Error: ${lifecycleEvent.exception?.message}")
                        resetConnectionState()
                    }
                    LifecycleEvent.Type.CLOSED -> resetConnectionState()
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> _errorMessages.tryEmit("Server heartbeat failed")
                }
            },
            {
                _errorMessages.tryEmit("Lifecycle Subscription Error")
            }
        )
    }

    fun connect() {
        if (stompClient == null) setupStompClient()
        if (_isConnected.value || stompClient?.isConnected == true) return
        stompClient?.connect()
    }

    fun disconnect() {
        stompClient?.disconnect()
        if (_isConnected.value) resetConnectionState()
    }

    private fun resetConnectionState() {
        _isConnected.value = false
        _lobbyState.value = null
        _createdLobbyId.value = null
        currentLobbySubscriptionId = null
    }

    @SuppressLint("CheckResult")
    private fun subscribeToLobbyCreationTopic() {
        stompClient?.topic(TOPIC_LOBBY_CREATED)?.subscribe(
            { stompMessage: StompMessage ->
                val newLobbyId = stompMessage.payload
                _createdLobbyId.value = newLobbyId
            },
            {
                _errorMessages.tryEmit("Error receiving lobby creation confirmation")
            }
        )
    }

    @SuppressLint("CheckResult")
    private fun subscribeToLobbyUpdates(lobbyId: String) {
        val topicPath = "$TOPIC_LOBBY_UPDATES_PREFIX$lobbyId"
        if (topicPath == currentLobbySubscriptionId) return
        currentLobbySubscriptionId = topicPath

        stompClient?.topic(topicPath)?.subscribe(
            { stompMessage ->
                try {
                    val lobby = gson.fromJson(stompMessage.payload, Lobby::class.java)
                    _lobbyState.value = lobby
                } catch (e: Exception) {
                    _errorMessages.tryEmit("Failed to parse lobby data")
                }
            },
            {
                _errorMessages.tryEmit("Subscription error for lobby $lobbyId")
                if (currentLobbySubscriptionId == topicPath) {
                    currentLobbySubscriptionId = null
                    _lobbyState.value = null
                }
            }
        )
    }

    @SuppressLint("CheckResult")
    fun createLobby(username: String, character: String = "Red", color: PlayerColor = PlayerColor.RED) {
        if (!_isConnected.value) {
            _errorMessages.tryEmit("Not connected to server")
            return
        }

        val player = Player(name = username, character = character, color = color)
        val request = CreateLobbyRequest(player)
        val payload = gson.toJson(request)

        stompClient?.send(APP_CREATE_LOBBY, payload)?.subscribe()
    }

    @SuppressLint("CheckResult")
    fun joinLobby(lobbyId: String, username: String, character: String = "Blue", color: PlayerColor = PlayerColor.BLUE) {
        if (!_isConnected.value) {
            _errorMessages.tryEmit("Not connected to server")
            return
        }
        if (lobbyId.isBlank()) {
            _errorMessages.tryEmit("Lobby ID cannot be empty")
            return
        }

        subscribeToLobbyUpdates(lobbyId)

        val player = Player(name = username, character = character, color = color)
        val request = JoinLobbyRequest(player)
        val payload = gson.toJson(request)
        val destination = "$APP_JOIN_LOBBY_PREFIX$lobbyId"

        stompClient?.send(destination, payload)?.subscribe()
    }

    @SuppressLint("CheckResult")
    fun leaveLobby(lobbyId: String, username: String, character: String = "Blue", color: PlayerColor = PlayerColor.BLUE) {
        if (!_isConnected.value) {
            _errorMessages.tryEmit("Not connected to server")
            return
        }
        if (lobbyId.isBlank()) {
            _errorMessages.tryEmit("Lobby ID cannot be empty")
            return
        }

        val player = Player(name = username, character = character, color = color)
        val request = LeaveLobbyRequest(player)
        val payload = gson.toJson(request)
        val destination = "$APP_LEAVE_LOBBY_PREFIX$lobbyId"

        stompClient?.send(destination, payload)?.subscribe()
    }

    fun solveCase(lobbyId: String, username: String, suspect: String, room: String, weapon: String) {

    }
}

