package at.aau.se2.cluedo.data.network

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.se2.cluedo.data.models.TurnActionRequest
import at.aau.se2.cluedo.data.models.TurnStateResponse
import at.aau.se2.cluedo.data.models.TurnState
import at.aau.se2.cluedo.data.models.SuggestionRequest
import at.aau.se2.cluedo.data.models.AccusationRequest
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.data.models.SkipTurnRequest
import at.aau.se2.cluedo.data.models.SuggestionResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompMessage

class TurnBasedWebSocketService private constructor() {

    companion object {
        // Turn-based system constants
        private const val APP_INITIALIZE_TURNS = "/app/initializeTurns/"
        private const val TOPIC_TURNS_INITIALIZED = "/topic/turnsInitialized/"
        private const val APP_GET_TURN_STATE = "/app/getTurnState/"
        private const val TOPIC_CURRENT_TURN_STATE = "/topic/currentTurnState/"
        private const val TOPIC_TURN_STATE_CHANGED = "/topic/turnStateChanged/"
        private const val APP_ROLL_DICE_TURN = "/app/rollDice/"
        private const val TOPIC_DICE_ROLLED = "/topic/diceRolled/"
        private const val APP_COMPLETE_MOVEMENT = "/app/completeMovement/"
        private const val TOPIC_MOVEMENT_COMPLETED = "/topic/movementCompleted/"
        private const val APP_MAKE_SUGGESTION = "/app/makeSuggestion/"
        private const val APP_MAKE_SUGGESTION_RESPONSE = "/app/processSuggestion/"
        private const val TOPIC_SUGGESTION_MADE = "/topic/suggestionMade/"
        private const val TOPIC_SUGGESTION_HANDLE = "/topic/processSuggestion/"
        private const val TOPIC_SUGGESTION_RESULT = "/topic/resultSuggestion/"
        private const val APP_MAKE_ACCUSATION = "/app/makeAccusation/"
        private const val TOPIC_ACCUSATION_MADE = "/topic/accusationMade/"
        private const val APP_SKIP_TURN = "/app/skipTurn/"
        private const val TOPIC_TURN_SKIPPED = "/topic/turnSkipped/"

        @Volatile
        private var instance: TurnBasedWebSocketService? = null

        fun getInstance(): TurnBasedWebSocketService {
            return instance ?: synchronized(this) {
                instance ?: TurnBasedWebSocketService().also { instance = it }
            }
        }
    }

    private val gson = Gson()
    private var stompClient: StompClient? = null

    // Turn-based system state flows
    private var _currentTurnState = MutableStateFlow<TurnStateResponse?>(null)
    val currentTurnState: StateFlow<TurnStateResponse?> = _currentTurnState.asStateFlow()

    private var _isCurrentPlayerTurn = MutableStateFlow(false)
    val isCurrentPlayerTurn: StateFlow<Boolean> = _isCurrentPlayerTurn.asStateFlow()

    private var _diceOneResult = MutableStateFlow<Int?>(null)
    val diceOneResult: StateFlow<Int?> = _diceOneResult.asStateFlow()

    private var _diceTwoResult = MutableStateFlow<Int?>(null)
    val diceTwoResult: StateFlow<Int?> = _diceTwoResult.asStateFlow()

    private var _suggestionData = MutableStateFlow<SuggestionRequest?>(null)
    val suggestionData: StateFlow<SuggestionRequest?> = _suggestionData

    private var _processSuggestion = MutableStateFlow<Boolean>(false)
    val processSuggestion: StateFlow<Boolean> = _processSuggestion

    private var _resultSuggestion = MutableStateFlow<SuggestionResponse?>(null)
    val resultSuggestion: StateFlow<SuggestionResponse?> = _resultSuggestion

    private var currentPlayerName: String? = null
    private var currentPlayer: Player? = null

    fun initialize(stompClient: StompClient) {
        this.stompClient = stompClient
    }

    fun setCurrentPlayer(player: Player) {
        this.currentPlayerName = player.name
        this.currentPlayer = player

    }

    /**
     * Subscribe to all turn-based topics for a specific lobby
     */
    @SuppressLint("CheckResult")
    fun subscribeToTurnBasedTopics(lobbyId: String) {
        Log.d("TurnBasedWS", "Subscribing to turn-based topics for lobby: $lobbyId")

        // Subscribe to turn state updates
        stompClient?.topic("$TOPIC_CURRENT_TURN_STATE$lobbyId")?.subscribe { message ->
            val turnState = gson.fromJson(message.payload, TurnStateResponse::class.java)
            _currentTurnState.value = turnState
            updatePlayerTurnStatus(turnState)
        }

        // Subscribe to turn state changes (sent by backend when state changes)
        stompClient?.topic("$TOPIC_TURN_STATE_CHANGED$lobbyId")?.subscribe { message ->
            Log.d("TurnBasedWS", "Turn state changed received: ${message.payload}")
            handleTurnStateChange(message, lobbyId)
        }

        // Subscribe to turn initialization
        stompClient?.topic("$TOPIC_TURNS_INITIALIZED$lobbyId")?.subscribe { message ->
            val turnState = gson.fromJson(message.payload, TurnStateResponse::class.java)
            _currentTurnState.value = turnState
            updatePlayerTurnStatus(turnState)
        }

        // Subscribe to dice rolled responses
        stompClient?.topic("$TOPIC_DICE_ROLLED$lobbyId")?.subscribe { message ->
            handleDiceRollResponse(message, lobbyId)
        }

        // Subscribe to movement completion responses
        stompClient?.topic("$TOPIC_MOVEMENT_COMPLETED$lobbyId")?.subscribe { message ->
            val turnState = gson.fromJson(message.payload, TurnStateResponse::class.java)
            _currentTurnState.value = turnState
            updatePlayerTurnStatus(turnState)
        }

        // Subscribe to suggestion responses
        stompClient?.topic("$TOPIC_SUGGESTION_MADE$lobbyId")?.subscribe { message ->
            Log.d("TurnBasedWS", "Suggestion response received: ${message.payload}")
            handleSuggestionResponse(message, lobbyId)
        }

        // Subscribe to accusation responses
        stompClient?.topic("$TOPIC_ACCUSATION_MADE$lobbyId")?.subscribe { message ->
            Log.d("TurnBasedWS", "Accusation response received: ${message.payload}")
            handleAccusationResponse(message, lobbyId)
        }

        // Subscribe to skip turn responses
        stompClient?.topic("$TOPIC_TURN_SKIPPED$lobbyId")?.subscribe { message ->
            Log.d("TurnBasedWS", "Skip turn response received: ${message.payload}")
            // Skip turn should update the turn state to the next player
            val turnState = gson.fromJson(message.payload, TurnStateResponse::class.java)
            _currentTurnState.value = turnState
            updatePlayerTurnStatus(turnState)
        }
    }

    /**
     * Subscribe to all only player relevant topics.
     */
    @SuppressLint("CheckResult")
    fun subscribeToTurnBasedPlayerTopics(lobbyId: String, playerId: String) {
        Log.d("TurnBasedWS", "Subscribing to turn-based topics for lobby: $lobbyId")

        stompClient?.topic("$TOPIC_SUGGESTION_HANDLE$lobbyId/$playerId")?.subscribe { message ->
            val responseMap = gson.fromJson(message.payload, Map::class.java)
            _processSuggestion.value = responseMap["processSuggestion"] as Boolean;
        }

        stompClient?.topic("$TOPIC_SUGGESTION_RESULT$lobbyId/$playerId")?.subscribe { message ->
            val responseMap = gson.fromJson(message.payload, Map::class.java)
            _resultSuggestion.value = SuggestionResponse(playerName = responseMap["sendingPlayer"] as String,
                                                         cardName = responseMap["receivedCard"]as String)
        }

    }

    private fun handleTurnStateChange(message: StompMessage, lobbyId: String) {
        Log.d("TurnBasedWS", "Turn state change received: ${message.payload}")
        val responseMap = gson.fromJson(message.payload, Map::class.java)

        val turnState = responseMap["turnState"] as? String ?: ""
        val currentPlayer = responseMap["currentPlayer"] as? String ?: ""

        Log.d("TurnBasedWS", "Parsed turn state change: turnState=$turnState, currentPlayer=$currentPlayer")

        val currentTurnState = _currentTurnState.value
        val updatedTurnState = if (currentTurnState != null) {
            currentTurnState.copy(
                currentPlayerName = currentPlayer,
                turnState = turnState,
                canMakeSuggestion = turnState == TurnState.PLAYERS_TURN_SUGGEST.value
            )
        } else {
            TurnStateResponse(
                lobbyId = lobbyId,
                currentPlayerName = currentPlayer,
                turnState = turnState,
                canMakeSuggestion = turnState == TurnState.PLAYERS_TURN_SUGGEST.value
            )
        }

        Log.d("TurnBasedWS", "Updated turn state from change: ${updatedTurnState.turnState}, canMakeSuggestion: ${updatedTurnState.canMakeSuggestion}")
        _currentTurnState.value = updatedTurnState
        updatePlayerTurnStatus(updatedTurnState)
    }

    private fun handleSuggestionResponse(message: StompMessage, lobbyId: String) {
        Log.d("SUGGEST", "Suggestion response received: ${message.payload}")
        val responseMap = gson.fromJson(message.payload, Map::class.java)

        val success = responseMap["success"] as? Boolean ?: false
        val messageText = responseMap["message"] as? String ?: ""

        Log.d("SUGGEST", "Suggestion result: success=$success, message=$messageText")

        val suggestionData = SuggestionRequest(
            (responseMap["player"] as? String).toString(),
            (responseMap["playerId"] as? String).toString(),
            (responseMap["suspect"] as? String).toString(),
            (responseMap["weapon"] as? String).toString(),
            (responseMap["room"] as? String).toString()
            )

        _suggestionData.value = suggestionData

        if (success) {
            // After a successful suggestion, the turn should advance to the next player
            // The backend should send a turn state change, but we can also request the current state
            getTurnState(lobbyId)
        }
    }

    private fun handleDiceRollResponse(message: StompMessage, lobbyId: String) {
        Log.d("TurnBasedWS", "Dice roll response received: ${message.payload}")
        val responseMap = gson.fromJson(message.payload, Map::class.java)

        if (responseMap.containsKey("lobbyId") && responseMap.containsKey("currentPlayerName")) {
            // Full TurnStateResponse format
            val turnState = gson.fromJson(message.payload, TurnStateResponse::class.java)
            Log.d("TurnBasedWS", "Parsed as TurnStateResponse: turnState=${turnState.turnState}, player=${turnState.currentPlayerName}")
            _currentTurnState.value = turnState
            updatePlayerTurnStatus(turnState)

            if (turnState.diceValue > 0) {
                _diceOneResult.value = turnState.diceValue
                _diceTwoResult.value = 0
            }
        } else if (responseMap.containsKey("player") && responseMap.containsKey("diceValue") && responseMap.containsKey("turnState")) {
            // Simplified dice response format
            val diceValue = (responseMap["diceValue"] as? Double)?.toInt() ?: 0
            val turnStateStr = responseMap["turnState"]?.toString() ?: ""
            val player = responseMap["player"] as? String ?: ""

            Log.d("TurnBasedWS", "Parsed as simplified response: turnState=$turnStateStr, player=$player, diceValue=$diceValue")

            if (diceValue > 0) {
                _diceOneResult.value = diceValue
                _diceTwoResult.value = 0
            }

            val currentTurnState = _currentTurnState.value
            val updatedTurnState = if (currentTurnState != null) {
                currentTurnState.copy(
                    currentPlayerName = player,
                    turnState = turnStateStr,
                    diceValue = diceValue
                )
            } else {
                TurnStateResponse(
                    lobbyId = lobbyId,
                    currentPlayerName = player,
                    turnState = turnStateStr,
                    diceValue = diceValue
                )
            }

            Log.d("TurnBasedWS", "Updated turn state: ${updatedTurnState.turnState}")
            _currentTurnState.value = updatedTurnState
            updatePlayerTurnStatus(updatedTurnState)
        }
    }

    private fun handleAccusationResponse(message: StompMessage, lobbyId: String) {
        Log.d("TurnBasedWS", "Accusation response received: ${message.payload}")
        val responseMap = gson.fromJson(message.payload, Map::class.java)

        val success = responseMap["success"] as? Boolean ?: false
        val correct = responseMap["correct"] as? Boolean ?: false
        val playerEliminated = responseMap["playerEliminated"] as? Boolean ?: false
        val messageText = responseMap["message"] as? String ?: ""

        Log.d("TurnBasedWS", "Accusation result: success=$success, correct=$correct, playerEliminated=$playerEliminated, message=$messageText")

        getTurnState(lobbyId)
    }

    private fun updatePlayerTurnStatus(turnState: TurnStateResponse) {
        val isMyTurn = currentPlayerName == turnState.currentPlayerName
        _isCurrentPlayerTurn.value = isMyTurn

        Log.d("TurnBasedWS", "Turn update: ${turnState.currentPlayerName}'s turn (${turnState.turnState}), isMyTurn: $isMyTurn")
        Log.d("TurnBasedWS", "DEBUG: currentPlayerName (mine): '$currentPlayerName'")
        Log.d("TurnBasedWS", "DEBUG: turnState.currentPlayerName: '${turnState.currentPlayerName}'")
        Log.d("TurnBasedWS", "DEBUG: Names match: ${currentPlayerName == turnState.currentPlayerName}")

        // Force UI update after a small delay
        Handler(Looper.getMainLooper()).postDelayed({
            _isCurrentPlayerTurn.value = isMyTurn
        }, 50)
    }

    // ========== ACTION METHODS ==========

    @SuppressLint("CheckResult")
    fun initializeTurns(lobbyId: String) {
        stompClient?.send("$APP_INITIALIZE_TURNS$lobbyId", "")?.subscribe()
    }

    @SuppressLint("CheckResult")
    fun getTurnState(lobbyId: String) {
        stompClient?.send("$APP_GET_TURN_STATE$lobbyId", "")?.subscribe()
    }

    @SuppressLint("CheckResult")
    fun rollDiceForTurn(lobbyId: String, playerName: String) {
        val request = TurnActionRequest(
            playerName = playerName,
            actionType = "DICE_ROLL",
            diceValue = 0
        )
        val payload = gson.toJson(request)
        stompClient?.send("$APP_ROLL_DICE_TURN$lobbyId", payload)?.subscribe()
    }

    @SuppressLint("CheckResult")
    fun completeMovement(lobbyId: String, playerName: String) {
        val request = TurnActionRequest(
            playerName = playerName,
            actionType = "COMPLETE_MOVEMENT"
        )
        val payload = gson.toJson(request)
        stompClient?.send("$APP_COMPLETE_MOVEMENT$lobbyId", payload)?.subscribe()
    }

    @SuppressLint("CheckResult")
    fun makeSuggestion(lobbyId: String, playerName: String,playerId: String, suspect: String, weapon: String, room: String) {
        val request = SuggestionRequest(
            playerName = playerName,
            playerId = playerId,
            suspect = suspect,
            weapon = weapon,
            room = room
        )
        val payload = gson.toJson(request)
        Log.d("SUGGEST","Sent!: ${suspect} ,${weapon},"+
                " ${room} , ${playerName} , $playerId")
        stompClient?.send("$APP_MAKE_SUGGESTION$lobbyId", payload)?.subscribe()
    }

    @SuppressLint("CheckResult")
    fun makeSuggestionResponse(lobbyId: String, playerId: String, cardName: String) {
        val request = SuggestionResponse(
            playerId = playerId,
            cardName = cardName
        )
        val payload = gson.toJson(request)
        stompClient?.send("$APP_MAKE_SUGGESTION_RESPONSE$lobbyId", payload)?.subscribe()
    }

    @SuppressLint("CheckResult")
    fun makeAccusation(lobbyId: String, playerName: String, suspect: String, weapon: String, room: String) {
        val request = AccusationRequest(
            lobbyId = lobbyId,
            username = playerName,
            suspect = suspect,
            weapon = weapon,
            room = room
        )
        val payload = gson.toJson(request)
        stompClient?.send("$APP_MAKE_ACCUSATION$lobbyId", payload)?.subscribe()
    }

    @SuppressLint("CheckResult")
    fun skipTurn(lobbyId: String, playerName: String, reason: String = "Player manually skipped turn") {
        val request = SkipTurnRequest(
            playerName = playerName,
            reason = reason
        )
        val payload = gson.toJson(request)
        stompClient?.send("$APP_SKIP_TURN$lobbyId", payload)?.subscribe()
    }

    fun canPerformAction(action: String): Boolean {
        val turnState = _currentTurnState.value
        val isMyTurn = _isCurrentPlayerTurn.value

        Log.d("TurnBasedWS", "canPerformAction($action): isMyTurn=$isMyTurn, turnState=${turnState?.turnState}")

        if (!isMyTurn || turnState == null) {
            Log.d("TurnBasedWS", "canPerformAction($action): DENIED - isMyTurn=$isMyTurn, turnState=$turnState")
            return false
        }

        val result = when (action) {
            "ROLL_DICE" -> turnState.turnState == TurnState.PLAYERS_TURN_ROLL_DICE.value
            "MOVE" -> turnState.turnState == TurnState.PLAYERS_TURN_MOVE.value
            "SUGGEST" -> true//turnState.turnState == TurnState.PLAYERS_TURN_SUGGEST.value && (turnState.canMakeSuggestion == true)
            "ACCUSE" -> turnState.canMakeAccusation == true
            else -> false
        }

        Log.d("TurnBasedWS", "canPerformAction($action): RESULT=$result")
        return result
    }

    fun resetState() {
        _currentTurnState.value = null
        _isCurrentPlayerTurn.value = false
        _diceOneResult.value = null
        _diceTwoResult.value = null
    }
}
