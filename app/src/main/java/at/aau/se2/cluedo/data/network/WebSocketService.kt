package at.aau.se2.cluedo.data.network

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
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

    private val _errorMessages = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 10)
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
        if (!_isConnected.value) {
            _errorMessages.tryEmit("Not connected to server")
            return
        }

        // If we have a saved lobby ID, we subscribe to it
        _createdLobbyId.value?.takeIf { it.isNotBlank() }?.let { lobbyId ->
            subscribeToSpecificLobbyTopics(lobbyId)
            return
        }

        // Otherwise, request active lobby
        val request = GetActiveLobbiesRequest()
        val payload = gson.toJson(request)
        sendRequest(APP_GET_ACTIVE_LOBBIES, payload)
    }

    @SuppressLint("CheckResult")
    private fun subscribeToSpecificLobbyTopics(lobbyId: String) {
        logMessage("Subscribing to topics for lobby: $lobbyId")

        // Subscribe to lobby updates
        val lobbyUpdateTopicPath = "$TOPIC_LOBBY_UPDATES_PREFIX$lobbyId"
        stompClient?.topic(lobbyUpdateTopicPath)?.subscribe({ stompMessage: StompMessage ->
            try {
                val lobby = gson.fromJson(stompMessage.payload, Lobby::class.java)
                _lobbyState.value = lobby
                logMessage("Received lobby update for ${lobby.id} with ${lobby.players.size} players")

                if (lobby.id.isNotBlank() && lobby.id != "Creating...") {
                    _createdLobbyId.value = lobby.id

                    // Check if we need to subscribe to game started topic
                    subscribeToGameStartedTopic(lobby.id)
                }
            } catch (e: Exception) {
                logMessage("Error parsing lobby update: ${e.message}")
            }
        }, { error ->
            logMessage("Error in lobby subscription: ${error.message}")
        })

        // Always subscribe to game started topic
        subscribeToGameStartedTopic(lobbyId)
    }

    @SuppressLint("CheckResult")
    private fun subscribeToGameStartedTopic(lobbyId: String) {
        val gameStartedTopicPath = "$TOPIC_GAME_STARTED_PREFIX$lobbyId"
        logMessage("Subscribing to game started topic: $gameStartedTopicPath")

        stompClient?.topic(gameStartedTopicPath)?.subscribe({ stompMessage: StompMessage ->
            try {
                val response = gson.fromJson(stompMessage.payload, GameStartedResponse::class.java)
                logMessage("Received game started event for lobby ${response.lobbyId} with ${response.players.size} players")

                // Update game state for all players
                _gameState.value = response
                _gameStarted.value = true

                // Log all players in the game
                response.players.forEach { player ->
                    logMessage("Player in game: ${player.name} (${player.character})")
                }

                // Force a delay to ensure UI updates before navigation
                Handler(Looper.getMainLooper()).postDelayed({
                    // Double-check that we're still in the game state
                    if (_gameStarted.value) {
                        logMessage("Confirming game started state after delay")
                    }
                }, 500)
            } catch (e: Exception) {
                logMessage("Error parsing game started message: ${e.message}")
            }
        }, { error ->
            logMessage("Error in game started subscription: ${error.message}")
        })
    }

    fun logMessage(message: String) {
        _errorMessages.tryEmit(message)
    }

    fun setGameStarted(started: Boolean) {
        _gameStarted.value = started
        logMessage("Game started state set to: $started")
    }

    @SuppressLint("CheckResult")
    private fun sendRequest(destination: String, payload: String, onSuccess: (() -> Unit)? = null) {
        if (!_isConnected.value) {
            _errorMessages.tryEmit("Cannot send request: Not connected")
            return
        }
        stompClient?.send(destination, payload)?.subscribe(
            {
                onSuccess?.invoke()
                _errorMessages.tryEmit("Successfully sent message to $destination")
            },
            { error -> _errorMessages.tryEmit("Failed to send STOMP message to $destination: ${error.message}") }
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
        if (!_isConnected.value || lobbyId.isBlank()) {
            _errorMessages.tryEmit("Cannot start game: Not connected or invalid lobby ID")
            return
        }

        // Make sure we're subscribed to the game started topic for this lobby
        subscribeToSpecificLobbyTopics(lobbyId)

        val player = Player(name = username, character = character, color = color)
        val request = StartGameRequest(player)
        val payload = gson.toJson(request)
        val destination = "$APP_START_GAME_PREFIX$lobbyId"

        logMessage("Sending start game request for lobby: $lobbyId")

        // Create a temporary game state with the current lobby players
        // This helps ensure all players see the game state even if they miss the server message
        _lobbyState.value?.let { lobby ->
            if (lobby.players.size >= 3) {
                logMessage("Creating temporary game state with ${lobby.players.size} players")
                val tempGameState = GameStartedResponse(
                    lobbyId = lobbyId,
                    players = lobby.players
                )
                _gameState.value = tempGameState
                _gameStarted.value = true  // Set this to true immediately for all players

                // Broadcast to all players
                broadcastGameStarted(tempGameState)
            }
        }

        sendRequest(destination, payload) {
            logMessage("Game start request sent successfully")
        }
    }

    /**
     * Check if a game has started for the current lobby
     * This is especially useful for non-host players
     */
    fun checkGameStarted() {
        if (!_isConnected.value) {
            logMessage("Cannot check game started: Not connected")
            return
        }

        // If we already have a game state, use it
        if (_gameState.value != null) {
            _gameStarted.value = true
            return
        }

        // Try to use the lobby state
        _lobbyState.value?.let { lobby ->
            if (lobby.id.isNotBlank() && lobby.id != "Creating...") {
                logMessage("Checking if game has started for lobby: ${lobby.id}")

                // Make sure we're subscribed to the game started topic
                subscribeToGameStartedTopic(lobby.id)

                // Request the current game state
                val destination = "$APP_CAN_START_GAME_PREFIX${lobby.id}"
                sendRequest(destination, "") {
                    logMessage("Sent request to check if game has started")
                }
            }
        }
    }

    /**
     * Broadcast game started event to all players
     * This is a helper method to ensure all players receive the game state
     */
    private fun broadcastGameStarted(gameState: GameStartedResponse) {
        logMessage("Broadcasting game started to all players")

        // Set the game state for all players
        _gameState.value = gameState
        _gameStarted.value = true
    }
}