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
import at.aau.se2.cluedo.data.models.GameStartedResponse
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGameBinding
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    private fun setupUI() {
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
            }
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
