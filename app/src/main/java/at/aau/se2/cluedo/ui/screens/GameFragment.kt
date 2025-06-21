package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import at.aau.se2.cluedo.data.models.BasicCard
import at.aau.se2.cluedo.data.models.GameStartedResponse
import at.aau.se2.cluedo.viewmodels.LobbyViewmodel
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGameBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import at.aau.se2.cluedo.data.network.WebSocketService
import at.aau.se2.cluedo.viewmodels.CardAdapter
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameFragment : Fragment() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

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

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val lobbyViewModel: LobbyViewmodel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Actions happening when View is created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()

        observeViewModel()

        // Check if we have a game state and log it
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

    }

    /**
     * UI setup comes here
     */
    private fun setupUI() {

      /*  binding.notesButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameFragment_to_notesFragment)
        }

        binding.solveCaseButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameFragment_to_solveCaseFragment)
        }

        binding.makeSuspicionButton.setOnClickListener {
            findNavController().navigate(R.id.action_gameFragment_to_suspicionPopupFragment)
        }
*/

        binding.playersListTextView.movementMethod = ScrollingMovementMethod()
        binding.gameInfoTextView.movementMethod = ScrollingMovementMethod()

        binding.rollDiceButton.setOnClickListener {
            rollDice()
        }

        // Update UI with game state if available
        val gameState = lobbyViewModel.gameState.value
        if (gameState != null) {
            updatePlayersUI(gameState)
        }

        //BottomSheet to show cards
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.cardsOpenButton.setOnClickListener {
            toggleBottomSheet()
        }

        //Change Icon of FloatingActionButton (openCardsButton) depending on state of BottomSheet
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        var cards = WebSocketService.getInstance().player.value?.cards
        recyclerView.adapter = CardAdapter(BasicCard.getCardIDs(cards))

    }

    private fun updatePlayersUI(gameState: GameStartedResponse) {
        // Update players list
        val playersList = gameState.players.joinToString("\n") { player ->
            val currentPlayerMark = if (player.isCurrentPlayer) " (Current Turn)" else ""
            "  - ${player.name} (${player.character})$currentPlayerMark"
        }
        binding.playersListTextView.text = playersList

        // Update game status
        val currentPlayer = gameState.players.find { it.isCurrentPlayer }
        if (currentPlayer != null) {
            binding.gameStatusTextView.text = getString(R.string.current_turn, currentPlayer.name)
        } else {
            binding.gameStatusTextView.text = getString(R.string.game_in_progress)
        }

        // Log for debugging
        lobbyViewModel.logMessage("Updated UI with ${gameState.players.size} players")
    }

    private fun rollDice() {
        val diceValue = Random.nextInt(1, 7)
        binding.diceResultTextView.text = getString(R.string.dice_result, diceValue)
        binding.gameInfoTextView.text = getString(R.string.you_rolled, diceValue)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe game state changes
                launch {
                    lobbyViewModel.gameState.collect { gameState ->
                        if (gameState != null) {
                            lobbyViewModel.logMessage("Game state updated: ${gameState.players.size} players in lobby ${gameState.lobbyId}")
                            updatePlayersUI(gameState)
                        } else {
                            lobbyViewModel.logMessage("Game state is null in collector")
                            binding.playersListTextView.text = "No players available"
                            binding.gameStatusTextView.text = getString(R.string.game_in_progress)

                            // Try to check if a game has started
                            lobbyViewModel.checkGameStarted()
                        }
                    }
                }

                // Also observe the gameStarted flag
                launch {
                    lobbyViewModel.gameStarted.collect { gameStarted ->
                        if (gameStarted) {
                            lobbyViewModel.logMessage("Game started flag is true")
                            // If the game is started but we don't have game state, try to get it
                            if (lobbyViewModel.gameState.value == null) {
                                lobbyViewModel.logMessage("Game started but no game state, trying to get it")
                                // Try to use the lobby state players as a fallback
                                val lobbyState = lobbyViewModel.lobbyState.value
                                if (lobbyState != null) {
                                    lobbyViewModel.logMessage("Using lobby state as fallback with ${lobbyState.players.size} players")
                                    val playersList = lobbyState.players.joinToString("\n") { player ->
                                        "  - ${player.name} (${player.character})"
                                    }
                                    binding.playersListTextView.text = playersList
                                }
                            }
                        }
                    }
                }

                launch {
                    lobbyViewModel.errorMessages.collect { errorMessage ->
                        showToast(errorMessage)
                    }
                }

                launch {
                    lobbyViewModel.lobbyState.collect { lobby ->
                        val currentPlayer = lobby?.players?.find { it.isCurrentPlayer == true }
                        val isInRoom = roomCoordinates.contains(Pair(currentPlayer?.x, currentPlayer?.y))
                        binding.makeSuspicionButton.isEnabled = isInRoom
                    }
                }


            }
        }
    }

/**
 * Toggles Bottom Sheet Open/Close
 */
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

}