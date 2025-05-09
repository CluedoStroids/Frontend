package at.aau.se2.cluedo.data.network

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import at.aau.se2.cluedo.data.models.ActiveLobbiesResponse
import at.aau.se2.cluedo.data.models.CreateLobbyRequest
import at.aau.se2.cluedo.data.models.DiceResult
import at.aau.se2.cluedo.data.models.GetActiveLobbiesRequest
import at.aau.se2.cluedo.data.models.JoinLobbyRequest
import at.aau.se2.cluedo.data.models.LeaveLobbyRequest
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.models.PlayerColor
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

        private const val TOPIC_DICE_RESULT = "/topic/diceResult"
        private const val APP_ROLL_DICE = "/app/rollDice"
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
                        subscribeToDiceResultTopic()
                    }

                    LifecycleEvent.Type.ERROR -> {
                        _errorMessages.tryEmit("Connection Error: ${lifecycleEvent.exception?.message}")
                        resetConnectionState()
                    }

                    LifecycleEvent.Type.CLOSED -> {
                        resetConnectionState()
                    }

                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                        _errorMessages.tryEmit("Server heartbeat failed")
                    }
                }
            },
            {
                _errorMessages.tryEmit("Lifecycle Subscription Error")
            }
        )
    }

    fun connect() {
        if (stompClient == null) {
            setupStompClient()
        }

        if (_isConnected.value || stompClient?.isConnected == true) {
            _errorMessages.tryEmit("Already connected to server")
            return
        }

        _errorMessages.tryEmit("Connecting to server...")
        stompClient?.connect()

        Handler(Looper.getMainLooper()).postDelayed({
            if (stompClient?.isConnected == true && !_isConnected.value) {
                _isConnected.value = true
                _errorMessages.tryEmit("Connection established manually")
                subscribeToLobbyCreationTopic()
                subscribeToDiceResultTopic()
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
            {
                _errorMessages.tryEmit("Error receiving lobby creation confirmation")
            }
        )

        stompClient?.topic(TOPIC_ACTIVE_LOBBIES)?.subscribe(
            { stompMessage: StompMessage ->
                try {
                    val response =
                        gson.fromJson(stompMessage.payload, ActiveLobbiesResponse::class.java)
                    _errorMessages.tryEmit("Received ${response.lobbies.size} active lobbies")

                    if (response.lobbies.isNotEmpty()) {
                        val firstLobby = response.lobbies[0]
                        _createdLobbyId.value = firstLobby.id
                        _errorMessages.tryEmit("Active lobby found: ${firstLobby.id} (Host: ${firstLobby.hostName}, Players: ${firstLobby.playerCount})")
                        subscribeToLobbyUpdates(firstLobby.id)
                    } else {
                        _errorMessages.tryEmit("No active lobbies found")
                    }
                } catch (e: Exception) {
                    _errorMessages.tryEmit("Error parsing active lobbies: ${e.message}")
                }
            },
            {
                _errorMessages.tryEmit("Error receiving active lobbies information")
            }
        )
    }

    @SuppressLint("CheckResult")
    fun getActiveLobby() {
        if (!_isConnected.value) {
            _errorMessages.tryEmit("Not connected to server")
            return
        }

        _errorMessages.tryEmit("Requesting active lobbies information...")

        if (_lobbyState.value != null && _lobbyState.value?.id?.isNotBlank() == true) {
            _createdLobbyId.value = _lobbyState.value?.id
            _errorMessages.tryEmit("Using existing lobby: ${_lobbyState.value?.id}")
            return
        }

        if (_createdLobbyId.value != null && _createdLobbyId.value?.isNotBlank() == true) {
            subscribeToLobbyUpdates(_createdLobbyId.value!!)
            _errorMessages.tryEmit("Resubscribing to lobby: ${_createdLobbyId.value}")
            return
        }

        val request = GetActiveLobbiesRequest()
        val payload = gson.toJson(request)

        stompClient?.send(APP_GET_ACTIVE_LOBBIES, payload)?.subscribe(
            {
                _errorMessages.tryEmit("Active lobbies request sent successfully")
            },
            { error ->
                _errorMessages.tryEmit("Failed to request active lobbies: ${error.message}")
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

                    if (lobby.id.isNotBlank() && lobby.id != "Creating...") {
                        _createdLobbyId.value = lobby.id
                        _errorMessages.tryEmit("Lobby updated: ${lobby.id}")
                    }
                } catch (e: Exception) {
                    _errorMessages.tryEmit("Failed to parse lobby data: ${e.message}")
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

        stompClient?.send(APP_CREATE_LOBBY, payload)?.subscribe(
            {
                val tempLobby = Lobby(
                    id = "Creating...",
                    host = player,
                    players = listOf(player)
                )
                _lobbyState.value = tempLobby
            },
            { error ->
                _errorMessages.tryEmit("Failed to create lobby: ${error.message}")
            }
        )
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

        val currentLobby = _lobbyState.value
        if (currentLobby != null) {
            val updatedPlayers = currentLobby.players.toMutableList()
            if (!updatedPlayers.any { it.name == player.name }) {
                updatedPlayers.add(player)
                _lobbyState.value = currentLobby.copy(players = updatedPlayers)
            }
        }

        stompClient?.send(destination, payload)?.subscribe(
            {
            },
            { error ->
                _errorMessages.tryEmit("Failed to join lobby: ${error.message}")
            }
        )
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

        val currentLobby = _lobbyState.value
        if (currentLobby != null) {
            val updatedPlayers = currentLobby.players.filter { it.name != player.name }
            _lobbyState.value = currentLobby.copy(players = updatedPlayers)
        }

        stompClient?.send(destination, payload)?.subscribe(
            {
            },
            { error ->
                _errorMessages.tryEmit("Failed to leave lobby: ${error.message}")
            }
        )
    }

    private val _diceOneResult = MutableStateFlow<Int?>(null)
    private val _diceTwoResult = MutableStateFlow<Int?>(null)

    val diceOneResult: StateFlow<Int?> = _diceOneResult
    val diceTwoResult: StateFlow<Int?> = _diceTwoResult

    @SuppressLint("CheckResult")
    private fun subscribeToDiceResultTopic() {
        stompClient?.topic(TOPIC_DICE_RESULT)?.subscribe({ stompMessage ->
            try {
                val result = gson.fromJson(stompMessage.payload, DiceResult::class.java)
                _diceOneResult.value = result.diceOne
                _diceTwoResult.value = result.diceTwo
            } catch (e: Exception) {
                _errorMessages.tryEmit("Invalid result format: ${e.message}")
            }
        }, {
            _errorMessages.tryEmit("Error subscribing to diceResult topic")
        })
    }

    @SuppressLint("CheckResult")
    fun rollDice() {
        if (!_isConnected.value) {
            _errorMessages.tryEmit("Not connected to server")
            return
        }

        stompClient?.send(APP_ROLL_DICE, "")?.subscribe({
            _errorMessages.tryEmit("Dice requested")
        }, { error ->
            _errorMessages.tryEmit("Error from rolling the dice: ${error.message}")
        })
    }
}
