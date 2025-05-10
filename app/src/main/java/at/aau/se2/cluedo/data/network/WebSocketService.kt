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
        private const val APP_GET_ACTIVE_LOBBIES = "/app/getActiveLobbies"
        private const val TOPIC_ACTIVE_LOBBIES = "/topic/activeLobbies"
        private const val APP_CAN_START_GAME_PREFIX = "/app/canStartGame/"
        private const val TOPIC_CAN_START_GAME_PREFIX = "/topic/canStartGame/"
        private const val APP_START_GAME_PREFIX = "/app/startGame/"
        private const val TOPIC_GAME_STARTED_PREFIX = "/topic/gameStarted/"
    }

    private val gson = Gson()
    private var stompClient: StompClient? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _lobbyState = MutableStateFlow<Lobby?>(null)
    val lobbyState: StateFlow<Lobby?> = _lobbyState.asStateFlow()

    private val _createdLobbyId = MutableStateFlow<String?>(null)
    val createdLobbyId: StateFlow<String?> = _createdLobbyId.asStateFlow()

    private val _canStartGame = MutableStateFlow(false)
    val canStartGame: StateFlow<Boolean> = _canStartGame.asStateFlow()

    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted.asStateFlow()

    private val _gameState = MutableStateFlow<GameStartedResponse?>(null)
    val gameState: StateFlow<GameStartedResponse?> = _gameState.asStateFlow()

    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages: SharedFlow<String> = _errorMessages.asSharedFlow()

    init {
        setupStompClient()
    }

    @SuppressLint("CheckResult")
    private fun setupStompClient() {
        if (stompClient != null && _isConnected.value == true) return
        stompClient?.disconnect()

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, CONNECTION_URL)
        stompClient?.lifecycle()?.subscribe(
            { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        _isConnected.value = true
                        subscribeToGeneralTopics()
                        _createdLobbyId.value?.takeIf { it.isNotBlank() }?.let { lobbyId ->
                            subscribeToSpecificLobbyTopics(lobbyId)
                        }
                    }
                    LifecycleEvent.Type.ERROR,
                    LifecycleEvent.Type.CLOSED,
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> resetConnectionState()
                }
            },
            { resetConnectionState() }
        )
    }

    fun connect() {
        if (_isConnected.value) return
        if (stompClient == null || stompClient?.isConnected == false) {
            setupStompClient()
        }
        if (stompClient?.isConnected == false) {
            stompClient?.connect()
        }
    }

    fun disconnect() {
        stompClient?.disconnect()
        if (_isConnected.value) {
            resetConnectionState()
        }
    }

    private fun resetConnectionState() {
        _isConnected.value = false
        _lobbyState.value = null
        _canStartGame.value = false
        _gameStarted.value = false
        _gameState.value = null
    }

    @SuppressLint("CheckResult")
    private fun subscribeToGeneralTopics() {
        stompClient?.topic(TOPIC_LOBBY_CREATED)?.subscribe { stompMessage: StompMessage ->
            val newLobbyId = stompMessage.payload
            if (!newLobbyId.isNullOrBlank()) {
                _createdLobbyId.value = newLobbyId
                subscribeToSpecificLobbyTopics(newLobbyId)
            }
        }

        stompClient?.topic(TOPIC_ACTIVE_LOBBIES)?.subscribe { stompMessage: StompMessage ->
            val response = gson.fromJson(stompMessage.payload, ActiveLobbiesResponse::class.java)
            response.lobbies.firstOrNull()?.id?.takeIf { it.isNotBlank() }?.let { lobbyId ->
                if (_createdLobbyId.value.isNullOrBlank() || _createdLobbyId.value != lobbyId) {
                    _createdLobbyId.value = lobbyId
                    subscribeToSpecificLobbyTopics(lobbyId)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    fun getActiveLobbies() {
        if (!_isConnected.value) return
        _createdLobbyId.value?.takeIf { it.isNotBlank() }?.let { lobbyId ->
            subscribeToSpecificLobbyTopics(lobbyId)
            return
        }
        val request = GetActiveLobbiesRequest()
        val payload = gson.toJson(request)
        stompClient?.send(APP_GET_ACTIVE_LOBBIES, payload)?.subscribe({}, {})
    }

    @SuppressLint("CheckResult")
    private fun subscribeToSpecificLobbyTopics(lobbyId: String) {
        val lobbyUpdateTopicPath = "$TOPIC_LOBBY_UPDATES_PREFIX$lobbyId"
        stompClient?.topic(lobbyUpdateTopicPath)?.subscribe({ stompMessage: StompMessage ->
            val lobby = gson.fromJson(stompMessage.payload, Lobby::class.java)
            _lobbyState.value = lobby
            if (lobby.id.isNotBlank() && lobby.id != "Creating...") {
                _createdLobbyId.value = lobby.id
            }
        }, {})

        val gameStartedTopicPath = "$TOPIC_GAME_STARTED_PREFIX$lobbyId"
        stompClient?.topic(gameStartedTopicPath)?.subscribe({ stompMessage: StompMessage ->
            val response = gson.fromJson(stompMessage.payload, GameStartedResponse::class.java)
            _gameState.value = response
            _gameStarted.value = true
        }, {})
    }

    @SuppressLint("CheckResult")
    private fun sendRequest(destination: String, payload: String, onSuccess: (() -> Unit)? = null) {
        if (!_isConnected.value) return
        stompClient?.send(destination, payload)?.subscribe(
            { onSuccess?.invoke() },
            { _errorMessages.tryEmit("Failed to send STOMP message to $destination") }
        )
    }

    fun createLobby(username: String, character: String = "Red", color: PlayerColor = PlayerColor.RED) {
        if (!_isConnected.value) return
        val player = Player(name = username, character = character, color = color)
        val request = CreateLobbyRequest(player)
        val payload = gson.toJson(request)

        _lobbyState.value = Lobby(id = "Creating...", host = player, players = listOf(player))
        _createdLobbyId.value = null
        sendRequest(APP_CREATE_LOBBY, payload)
    }

    fun joinLobby(lobbyId: String, username: String, character: String = "Blue", color: PlayerColor = PlayerColor.BLUE) {
        if (!_isConnected.value || lobbyId.isBlank()) return

        _createdLobbyId.value = lobbyId
        subscribeToSpecificLobbyTopics(lobbyId)

        val player = Player(name = username, character = character, color = color)
        val request = JoinLobbyRequest(player)
        val payload = gson.toJson(request)
        val destination = "$APP_JOIN_LOBBY_PREFIX$lobbyId"

        _lobbyState.value?.let { currentLobby ->
            if (currentLobby.id == lobbyId && !currentLobby.players.any { it.name == player.name }) {
                _lobbyState.value = currentLobby.copy(players = currentLobby.players + player)
            }
        }
        sendRequest(destination, payload)
    }

    fun leaveLobby(lobbyId: String, username: String, character: String = "Blue", color: PlayerColor = PlayerColor.BLUE) {
        if (!_isConnected.value || lobbyId.isBlank()) return
        val player = Player(name = username, character = character, color = color)
        val request = LeaveLobbyRequest(player)
        val payload = gson.toJson(request)
        val destination = "$APP_LEAVE_LOBBY_PREFIX$lobbyId"

        _lobbyState.value?.let { currentLobby ->
            if (currentLobby.id == lobbyId) {
                val updatedPlayers = currentLobby.players.filterNot { it.name == player.name }
                if (updatedPlayers.isEmpty() || currentLobby.host.name == username) {
                    _lobbyState.value = null
                    _createdLobbyId.value = null
                } else {
                    _lobbyState.value = currentLobby.copy(players = updatedPlayers)
                }
            }
        }
        sendRequest(destination, payload)
    }

    @SuppressLint("CheckResult")
    fun checkCanStartGame(lobbyId: String) {
        if (!_isConnected.value || lobbyId.isBlank()) return
        val destination = "$APP_CAN_START_GAME_PREFIX$lobbyId"
        val topicPath = "$TOPIC_CAN_START_GAME_PREFIX$lobbyId"

        stompClient?.topic(topicPath)?.subscribe { stompMessage: StompMessage ->
            val response = gson.fromJson(stompMessage.payload, CanStartGameResponse::class.java)
            _canStartGame.value = response.canStart
        }
        sendRequest(destination, "")
    }

    fun startGame(lobbyId: String, username: String, character: String, color: PlayerColor) {
        if (!_isConnected.value || lobbyId.isBlank()) return
        val player = Player(name = username, character = character, color = color)
        val request = StartGameRequest(player)
        val payload = gson.toJson(request)
        val destination = "$APP_START_GAME_PREFIX$lobbyId"

        _errorMessages.tryEmit("Sending start game request for lobby: $lobbyId")
        sendRequest(destination, payload)
    }
}