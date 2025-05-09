package at.aau.se2.cluedo.data.network

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import at.aau.se2.cluedo.data.models.ActiveLobbiesResponse
import at.aau.se2.cluedo.data.models.CanStartGameResponse
import at.aau.se2.cluedo.data.models.CreateLobbyRequest
import at.aau.se2.cluedo.data.models.GameStartedResponse
import at.aau.se2.cluedo.data.models.GetActiveLobbiesRequest
import at.aau.se2.cluedo.data.models.JoinLobbyRequest
import at.aau.se2.cluedo.data.models.LeaveLobbyRequest
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.models.PlayerColor
import at.aau.se2.cluedo.data.models.StartGameRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private var currentLobbySubscriptionId: String? = null

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
                        resetConnectionState()
                    }

                    LifecycleEvent.Type.CLOSED -> {
                        resetConnectionState()
                    }

                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                        resetConnectionState()
                    }
                }
            },
            {
                resetConnectionState()
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

        Handler(Looper.getMainLooper()).postDelayed({
            if (stompClient?.isConnected == true && !_isConnected.value) {
                _isConnected.value = true
                subscribeToLobbyCreationTopic()
            }
        }, 2000)
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
        _createdLobbyId.value = null
        _canStartGame.value = false
        _gameStarted.value = false
        _gameState.value = null
        currentLobbySubscriptionId = null
    }

    @SuppressLint("CheckResult")
    private fun subscribeToLobbyCreationTopic() {
        stompClient?.topic(TOPIC_LOBBY_CREATED)?.subscribe(
            { stompMessage: StompMessage ->
                val newLobbyId = stompMessage.payload
                _createdLobbyId.value = newLobbyId
                if (newLobbyId != null && newLobbyId.isNotBlank()) {
                    subscribeToLobbyUpdates(newLobbyId)
                    getActiveLobby()
                }
            },
            {}
        )

        stompClient?.topic(TOPIC_ACTIVE_LOBBIES)?.subscribe(
            { stompMessage: StompMessage ->
                try {
                    val response = gson.fromJson(stompMessage.payload, ActiveLobbiesResponse::class.java)

                    if (response.lobbies.isNotEmpty()) {
                        val firstLobby = response.lobbies[0]
                        _createdLobbyId.value = firstLobby.id
                        subscribeToLobbyUpdates(firstLobby.id)
                    }
                } catch (e: Exception) {
                }
            },
            {}
        )
    }

    @SuppressLint("CheckResult")
    fun getActiveLobby() {
        if (!_isConnected.value) {
            return
        }

        if (_lobbyState.value != null && _lobbyState.value?.id?.isNotBlank() == true) {
            _createdLobbyId.value = _lobbyState.value?.id
            return
        }

        if (_createdLobbyId.value != null && _createdLobbyId.value?.isNotBlank() == true) {
            subscribeToLobbyUpdates(_createdLobbyId.value!!)
            return
        }

        val request = GetActiveLobbiesRequest()
        val payload = gson.toJson(request)
        stompClient?.send(APP_GET_ACTIVE_LOBBIES, payload)?.subscribe({}, {})
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

                    if (lobby.id.isNotBlank() && lobby.id != "Creating...") {
                        _createdLobbyId.value = lobby.id
                    }
                } catch (e: Exception) {
                }
            },
            {
                if (currentLobbySubscriptionId == topicPath) {
                    currentLobbySubscriptionId = null
                    _lobbyState.value = null
                }
            }
        )

        subscribeToGameStartedEvents(lobbyId)
    }

    @SuppressLint("CheckResult")
    private fun subscribeToGameStartedEvents(lobbyId: String) {
        val topicPath = "$TOPIC_GAME_STARTED_PREFIX$lobbyId"

        stompClient?.topic(topicPath)?.subscribe(
            { stompMessage: StompMessage ->
                try {
                    val response = gson.fromJson(stompMessage.payload, GameStartedResponse::class.java)
                    _gameState.value = response
                    _gameStarted.value = true
                    _errorMessages.tryEmit("Game started with ${response.players.size} players")
                } catch (e: Exception) {
                    // Silently fail on parse errors
                }
            },
            {}
        )
    }


    @SuppressLint("CheckResult")
    private fun sendRequest(destination: String, payload: String, onSuccess: () -> Unit = {}) {
        if (!_isConnected.value) {
            return
        }
        stompClient?.send(destination, payload)?.subscribe(
            { onSuccess() },
            {}
        )
    }

    @SuppressLint("CheckResult")
    fun createLobby(username: String, character: String = "Red", color: PlayerColor = PlayerColor.RED) {
        if (!_isConnected.value) {
            return
        }
        val player = Player(name = username, character = character, color = color)
        val request = CreateLobbyRequest(player)
        val payload = gson.toJson(request)

        sendRequest(APP_CREATE_LOBBY, payload) {
            val tempLobby = Lobby(
                id = "Creating...",
                host = player,
                players = listOf(player)
            )
            _lobbyState.value = tempLobby
        }
    }

    @SuppressLint("CheckResult")
    fun joinLobby(lobbyId: String, username: String, character: String = "Blue", color: PlayerColor = PlayerColor.BLUE) {
        if (!_isConnected.value || lobbyId.isBlank()) {
            return
        }

        subscribeToLobbyUpdates(lobbyId)

        val player = Player(name = username, character = character, color = color)
        val request = JoinLobbyRequest(player)
        val payload = gson.toJson(request)
        val destination = "$APP_JOIN_LOBBY_PREFIX$lobbyId"

        // Optimistically update the UI
        val currentLobby = _lobbyState.value
        if (currentLobby != null) {
            val updatedPlayers = currentLobby.players.toMutableList()
            if (!updatedPlayers.any { it.name == player.name }) {
                updatedPlayers.add(player)
                _lobbyState.value = currentLobby.copy(players = updatedPlayers)
            }
        }

        sendRequest(destination, payload)
    }

    @SuppressLint("CheckResult")
    fun leaveLobby(lobbyId: String, username: String, character: String = "Blue", color: PlayerColor = PlayerColor.BLUE) {
        if (!_isConnected.value || lobbyId.isBlank()) {
            return
        }

        val player = Player(name = username, character = character, color = color)
        val request = LeaveLobbyRequest(player)
        val payload = gson.toJson(request)
        val destination = "$APP_LEAVE_LOBBY_PREFIX$lobbyId"

        val currentLobby = _lobbyState.value
        if (currentLobby != null) {
            val updatedPlayers = currentLobby.players.filter { it.name != player.name }
            _lobbyState.value = currentLobby.copy(players = updatedPlayers)
        }

        sendRequest(destination, payload)
    }

    @SuppressLint("CheckResult")
    fun checkCanStartGame(lobbyId: String) {
        if (!_isConnected.value || lobbyId.isBlank()) {
            return
        }

        val destination = "$APP_CAN_START_GAME_PREFIX$lobbyId"
        val topicPath = "$TOPIC_CAN_START_GAME_PREFIX$lobbyId"

        stompClient?.topic(topicPath)?.subscribe(
            { stompMessage: StompMessage ->
                try {
                    val response = gson.fromJson(stompMessage.payload, CanStartGameResponse::class.java)
                    _canStartGame.value = response.canStart
                } catch (e: Exception) {
                }
            },
            {}
        )

        sendRequest(destination, "")
    }

    @SuppressLint("CheckResult")
    fun startGame(lobbyId: String, username: String, character: String, color: PlayerColor) {
        if (!_isConnected.value || lobbyId.isBlank()) {
            return
        }

        val player = Player(name = username, character = character, color = color)
        val request = StartGameRequest(player)
        val payload = gson.toJson(request)
        val destination = "$APP_START_GAME_PREFIX$lobbyId"

        _errorMessages.tryEmit("Sending start game request for lobby: $lobbyId")
        sendRequest(destination, payload)
    }
}
