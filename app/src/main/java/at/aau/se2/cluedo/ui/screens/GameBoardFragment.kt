package at.aau.se2.cluedo.ui.screens

import android.hardware.Sensor
import android.hardware.SensorManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.aau.se2.cluedo.data.models.BasicCard
import at.aau.se2.cluedo.data.models.GameStartedResponse
import at.aau.se2.cluedo.data.models.TurnState
import at.aau.se2.cluedo.data.models.TurnStateResponse
import at.aau.se2.cluedo.data.network.WebSocketService
import at.aau.se2.cluedo.data.network.TurnBasedWebSocketService
import at.aau.se2.cluedo.viewmodels.CardAdapter
import at.aau.se2.cluedo.viewmodels.GameBoard
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import at.aau.se2.cluedo.ui.ShakeEventListener
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGameBoardBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [gameBoard.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameBoardFragment : Fragment() {

    private lateinit var gameBoard: GameBoard
    var webSocketService: WebSocketService? = null
    private val turnBasedService = TurnBasedWebSocketService.getInstance()
    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    private var _binding: FragmentGameBoardBinding? = null
    private val binding get() = _binding!!

    private val roomCoordinates = setOf(
        Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, 1), // Küche
        Pair(0, 4), Pair(1, 4), Pair(0, 5), Pair(1, 5), // Speisezimmer
        Pair(0, 9), Pair(1, 9), Pair(0, 10), Pair(1, 10), // Salon
        Pair(4, 0), Pair(5, 0), Pair(4, 1), Pair(5, 1), // Musikzimmer
        Pair(4, 9), Pair(5, 9), Pair(4, 10), Pair(5, 10), // Halle
        Pair(8, 0), Pair(9, 0), Pair(8, 1), Pair(9, 1), // Wintergarten
        Pair(8, 4), Pair(9, 4), Pair(8, 5), Pair(9, 5), // Billardzimmer
        Pair(8, 6), Pair(9, 6), Pair(8, 7), Pair(9, 7), // Bibliothek
        Pair(8, 9), Pair(9, 9), Pair(8, 10), Pair(9, 10) // Arbeitszimmer
    )

    private var diceOneValue = 0
    private var diceTwoValue = 0
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val shakeListener = ShakeEventListener()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var cardsRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        println("HI")
        webSocketService = WebSocketService.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGameBoardBinding.inflate(inflater, container, false)
        // binding =_binding!!

        // Inflate the layout for this fragment
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webSocketService?.connect()
        //println("gameBoard Hi")
        gameBoard = view.findViewById(R.id.gameBoardView) as GameBoard
        // Check if we have a game state and log it

        //Solve Case Buttons
        binding.notesButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameBoardIMG_to_notesFragment)
        }

        binding.solveCaseButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameBoardIMG_to_accusationFragment)
            if (turnBasedService.canPerformAction("ACCUSE")) {
                findNavController().navigate(R.id.action_gameBoardIMG_to_accusationFragment)
            } else {
                showToast("Cannot make accusation - not your turn or invalid state")
            }
        }

        binding.makeSuspicionButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameBoardIMG_to_suggestionFragment)
            if (turnBasedService.canPerformAction("SUGGEST")) {
                findNavController().navigate(R.id.action_gameBoardIMG_to_suggestionFragment)
            } else {
                showToast("Cannot make suggestion - not your turn or not in a room")
            }
        }
        binding.cheatingButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameBoardIMG_to_cheatingSuspicionFragment)
        }

        //BottomSheet to show cards
        val bottomSheet = view.findViewById<NestedScrollView>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN


        binding.cardsOpenButton.setOnClickListener {
            toggleBottomSheet()
        }
        //Change Icon of FloatingActionButton (openCardsButton) depending on state of BottomSheet
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.cardsOpenButton.setImageResource(R.drawable.cards_close_icon)
                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.cardsOpenButton.setImageResource(R.drawable.cards_open_icon)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // not needed but must be overridden
            }

        })

        val recyclerView = binding.playerCardsRecyclerview
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        var cards = WebSocketService.getInstance().player.value?.cards
        recyclerView.adapter = CardAdapter(BasicCard.getCardIDs(cards))


        val gameState = lobbyViewModel.gameState.value
        if (gameState != null) {
            showToast("Game state available: ${gameState.players.size} players")

            // Log all players to help with debugging
            gameState.players.forEach { player ->
                lobbyViewModel.logMessage("Player in game: ${player.name} (${player.character})")
            }

        } else {
            showToast("No game state available yet")
            lobbyViewModel.logMessage("Game state is null in GameFragment")

            // Try to get the game state from the lobby state
            val lobbyState = lobbyViewModel.lobbyState.value
            if (lobbyState != null) {
                lobbyViewModel.logMessage("Lobby state available with ${lobbyState.players.size} players")

                // Create a temporary game state from the lobby state
                val tempGameState = GameStartedResponse(
                    lobbyId = lobbyState.id,
                    players = lobbyState.players
                )

                // Update the UI with the lobby players
                updatePlayersUI(tempGameState)
            } else {
                lobbyViewModel.logMessage("Both game state and lobby state are null")
            }

            // Try to check if a game has started
            lobbyViewModel.checkGameStarted()
        }


        gameBoard.init()

        // Observe view model changes first
        observeViewModel()

        // Initialize turn-based system after a short delay to ensure game state is available
        lifecycleScope.launch {
            kotlinx.coroutines.delay(1000) // Wait 1 second for game state to be established
            initializeTurnBasedSystem()
        }

        //println(gameBoard)
        val moveButton = view.findViewById<Button>(R.id.movebutton)
        val upButton = view.findViewById<Button>(R.id.moveUp)
        val downButton = view.findViewById<Button>(R.id.moveDown)
        val leftButton = view.findViewById<Button>(R.id.moveLeft)
        val rightButton = view.findViewById<Button>(R.id.moveRight)
        val doneButton = view.findViewById<Button>(R.id.Done)

        moveButton.setOnClickListener {
            if (turnBasedService.canPerformAction("MOVE")) {
                moveButton.visibility = View.GONE
                upButton.visibility = View.VISIBLE
                downButton.visibility = View.VISIBLE
                leftButton.visibility = View.VISIBLE
                rightButton.visibility = View.VISIBLE
                doneButton.visibility = View.VISIBLE
                gameBoard.performMoveClicked()
            } else {
                showToast("Cannot move - not your turn or invalid state")
            }
        }
        upButton.setOnClickListener {
            gameBoard.moveUp()
            subtractMovement()
        }
        downButton.setOnClickListener {
            gameBoard.moveDown()
            subtractMovement()
        }
        leftButton.setOnClickListener {
            gameBoard.moveLeft()
            subtractMovement()
        }
        rightButton.setOnClickListener {
            gameBoard.moveRight()
            subtractMovement()
        }
        doneButton.setOnClickListener {
            gameBoard.done()
            moveButton.visibility = View.VISIBLE
            upButton.visibility = View.GONE
            downButton.visibility = View.GONE
            leftButton.visibility = View.GONE
            rightButton.visibility = View.GONE
            doneButton.visibility = View.GONE

            // Complete movement using turn-based systemx
            val lobbyId = lobbyViewModel.createdLobbyId.value
            val playerName = webSocketService?.player?.value?.name
            turnBasedService.completeMovement(lobbyId.toString(), playerName.toString())
        }

        binding.rollDice.setOnClickListener {
            webSocketService?.rollDice()
            if (turnBasedService.canPerformAction("ROLL_DICE")) {
                val lobbyId = lobbyViewModel.createdLobbyId.value
                val playerName = webSocketService?.player?.value?.name
                turnBasedService.rollDiceForTurn(lobbyId.toString(), playerName.toString())
            } else {
                showToast("Cannot roll dice - not your turn or invalid state")
            }
        }

        binding.skipTurnButton.setOnClickListener {
            val lobbyId = lobbyViewModel.createdLobbyId.value
            val playerName = webSocketService?.player?.value?.name
            turnBasedService.skipTurn(
                lobbyId.toString(),
                playerName.toString(),
                "Player manually skipped turn"
            )
            showToast("Turn skipped")

        }

        lifecycleScope.launch {
            launch {
                webSocketService?.diceOneResult?.collect { value ->
                    value?.let {
                        diceOneValue = it // store locally
                        binding.diceOneValueTextView2.text = diceOneValue.toString()
                    }
                }
            }
            launch {
                webSocketService?.diceTwoResult?.collect { value ->
                    value?.let {
                        diceTwoValue = it //store locally
                        binding.diceTwoValueTextView2.text = diceTwoValue.toString()
                    }
                }
            }
        }

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeListener.setOnShakeListener {
            binding.rollDice.performClick()
        }

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    lobbyViewModel.errorMessages.collect { errorMessage ->
                        showToast(errorMessage)
                    }
                }

                launch {
                    lobbyViewModel.lobbyState.collect { lobby ->
                        val currentPlayer = lobby?.players?.find { it.isCurrentPlayer == true }
                        val isInRoom =
                            roomCoordinates.contains(Pair(currentPlayer?.x, currentPlayer?.y))
                        // Note: Turn-based validation will be handled by canPerformAction
                        // This is kept for backward compatibility
                        binding.makeSuspicionButton.isEnabled = isInRoom
                    }
                }

                // Observe turn state changes
                launch {
                    turnBasedService.currentTurnState.collect { turnState ->
                        turnState?.let {
                            updateUIForTurnState(it)
                            // Also update button states when turn state changes
                            val isMyTurn = turnBasedService.isCurrentPlayerTurn.value
                            updateButtonStates(isMyTurn)
                        }
                    }
                }

                // Observe if it's current player's turn
                launch {
                    turnBasedService.isCurrentPlayerTurn.collect { isMyTurn ->
                        Log.d(
                            "GameBoardFragment",
                            "DEBUG: isCurrentPlayerTurn flow emitted: $isMyTurn"
                        )

                        // Force UI update on main thread
                        requireActivity().runOnUiThread {
                            updateButtonStates(isMyTurn)
                        }
                    }
                }

                // Observe game state changes to initialize turns
                launch {
                    lobbyViewModel.gameState.collect { gameState ->
                        gameState?.let {
                            // Initialize turns when game state is available
                            val lobbyId = it.lobbyId
                            if (lobbyId.isNotBlank()) {
                                turnBasedService.initializeTurns(lobbyId)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize the turn-based system when the game starts
     */
    private fun initializeTurnBasedSystem() {
        val lobbyId = lobbyViewModel.createdLobbyId.value
        if (!lobbyId.isNullOrBlank()) {
            turnBasedService.initializeTurns(lobbyId)
            turnBasedService.getTurnState(lobbyId)
        }
    }

    /**
     * Update UI elements based on current turn state
     */
    private fun updateUIForTurnState(turnState: TurnStateResponse) {
        // Update turn state display
        val message = getTurnStateMessage(turnState.turnState)
        showToast("${turnState.currentPlayerName}: $message")

        // The button states will be updated automatically when isCurrentPlayerTurn changes
    }

    /**
     * Update button states based on whether it's the current player's turn
     */
    private fun updateButtonStates(isMyTurn: Boolean) {
        val currentTurnState = turnBasedService.currentTurnState.value
        val playerName = webSocketService?.player?.value?.name

        // Debug logging can be removed
        Log.d(
            "GameBoardFragment",
            "DEBUG: updateButtonStates called - isMyTurn: $isMyTurn, playerName: $playerName, currentPlayer: ${currentTurnState?.currentPlayerName}, turnState: ${currentTurnState?.turnState}"
        )

        // Skip turn always enabled when it's the player's turn
        binding.skipTurnButton.isEnabled = isMyTurn

        if (isMyTurn == false) {
            // Disable all action buttons if it's not the player's turn
            binding.rollDice.isEnabled = false
            binding.solveCaseButton.isEnabled = false
            binding.makeSuspicionButton.isEnabled = false
            view?.findViewById<Button>(R.id.movebutton)?.isEnabled = false
            Log.d("GameBoardFragment", "DEBUG: Buttons disabled - not my turn")
        } else {
            // Enable buttons based on turn state and game logic
            val canRollDice = turnBasedService.canPerformAction("ROLL_DICE")
            val canAccuse = turnBasedService.canPerformAction("ACCUSE")
            val canSuggest = turnBasedService.canPerformAction("SUGGEST")
            val canMove = turnBasedService.canPerformAction("MOVE")

            binding.rollDice.isEnabled = canRollDice
            binding.solveCaseButton.isEnabled = canAccuse
            binding.makeSuspicionButton.isEnabled = canSuggest
            view?.findViewById<Button>(R.id.movebutton)?.isEnabled = canMove

            Log.d(
                "GameBoardFragment",
                "DEBUG: Buttons enabled - canRollDice: $canRollDice, canMove: $canMove, canSuggest: $canSuggest, canAccuse: $canAccuse, skipTurn: $isMyTurn"
            )
        }

        // Force a visual refresh of the buttons
        binding.rollDice.invalidate()
        binding.solveCaseButton.invalidate()
        binding.makeSuspicionButton.invalidate()
        binding.skipTurnButton.invalidate()
        view?.findViewById<Button>(R.id.movebutton)?.invalidate()
    }

    /**
     * Get a user-friendly message for the turn state
     */
    private fun getTurnStateMessage(turnState: String): String {
        return when (turnState) {
            TurnState.PLAYERS_TURN_ROLL_DICE.value -> "Roll dice to start turn"
            TurnState.PLAYERS_TURN_MOVE.value -> "Move your piece"
            TurnState.PLAYERS_TURN_SUGGEST.value -> "Make a suggestion"
            TurnState.PLAYERS_TURN_SOLVE.value -> "Make an accusation"
            TurnState.PLAYERS_TURN_END.value -> "Turn ending..."
            TurnState.PLAYER_HAS_WON.value -> "Game over!"
            TurnState.WAITING_FOR_PLAYERS.value -> "Waiting for players"
            TurnState.WAITING_FOR_START.value -> "Waiting to start"
            else -> "Unknown state"
        }
    }


    private fun updatePlayersUI(gameState: GameStartedResponse) {
        // Update players list
        val playersList = gameState.players.joinToString("\n") { player ->
            val currentPlayerMark = if (player.isCurrentPlayer) " (Current Turn)" else ""
            "  - ${player.name} (${player.character})$currentPlayerMark"
        }


        // Log for debugging
        lobbyViewModel.logMessage("Updated UI with ${gameState.players.size} players")
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message, duration).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun subtractMovement() {
        if (diceOneValue > 0) {
            diceOneValue -= 1
            binding.diceOneValueTextView2.text = diceOneValue.toString()
        } else {
            diceTwoValue -= 1
            binding.diceTwoValueTextView2.text = diceTwoValue.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { acc ->
            sensorManager.registerListener(shakeListener, acc, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeListener)
    }

}