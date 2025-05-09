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

        val gameState = lobbyViewModel.gameState.value
        if (gameState != null) {
            showToast("Game state available: ${gameState.players.size} players")
        } else {
            showToast("No game state available yet")
        }
    }

    private fun setupUI() {
        binding.playersListTextView.movementMethod = ScrollingMovementMethod()
        binding.gameInfoTextView.movementMethod = ScrollingMovementMethod()

        binding.rollDiceButton.setOnClickListener {
            rollDice()
        }

        val gameState = lobbyViewModel.gameState.value
        if (gameState != null) {
            val playersList = gameState.players.joinToString("\n") { player ->
                val currentPlayerMark = if (player.isCurrentPlayer) " (Current Turn)" else ""
                "  - ${player.name} (${player.character})$currentPlayerMark"
            }
            binding.playersListTextView.text = playersList

            val currentPlayer = gameState.players.find { it.isCurrentPlayer }
            if (currentPlayer != null) {
                binding.gameStatusTextView.text =
                    getString(R.string.current_turn, currentPlayer.name)
            } else {
                binding.gameStatusTextView.text = getString(R.string.game_in_progress)
            }
        }
    }

    private fun rollDice() {
        val diceValue = Random.nextInt(1, 7)
        binding.diceResultTextView.text = getString(R.string.dice_result, diceValue)
        binding.gameInfoTextView.text = getString(R.string.you_rolled, diceValue)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    lobbyViewModel.gameState.collect { gameState ->
                        if (gameState != null) {
                            showToast("Game state updated: ${gameState.players.size} players")

                            val playersList = gameState.players.joinToString("\n") { player ->
                                val currentPlayerMark =
                                    if (player.isCurrentPlayer) " (Current Turn)" else ""
                                "  - ${player.name} (${player.character})$currentPlayerMark"
                            }
                            binding.playersListTextView.text = playersList

                            val currentPlayer = gameState.players.find { it.isCurrentPlayer }
                            if (currentPlayer != null) {
                                binding.gameStatusTextView.text =
                                    getString(R.string.current_turn, currentPlayer.name)
                            } else {
                                binding.gameStatusTextView.text =
                                    getString(R.string.game_in_progress)
                            }
                        } else {
                            showToast("Game state is null")
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
