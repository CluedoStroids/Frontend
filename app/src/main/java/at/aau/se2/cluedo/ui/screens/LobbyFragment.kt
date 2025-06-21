package at.aau.se2.cluedo.ui.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import at.aau.se2.cluedo.data.models.LobbyStatus
import at.aau.se2.cluedo.viewmodels.LobbyViewmodel
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLobbyBinding
import kotlinx.coroutines.launch

class LobbyFragment : Fragment() {

    private var _binding: FragmentLobbyBinding? = null
    private val binding get() = _binding!!
    private val lobbyViewModel: LobbyViewmodel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLobbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()

        lobbyViewModel.connect()
    }

    private fun setupUI() {
        binding.lobbyInfoTextView.movementMethod = ScrollingMovementMethod()

        val characterAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            lobbyViewModel.availableCharacters
        )
        binding.createCharacterSpinner.adapter = characterAdapter

        val redIndex = lobbyViewModel.availableCharacters.indexOf("Red")
        if (redIndex >= 0) {
            binding.createCharacterSpinner.setSelection(redIndex)
        }

        binding.createLobbyButton.setOnClickListener {
            val username = binding.createUsernameEditText.text.toString().trim()
            val character = binding.createCharacterSpinner.selectedItem.toString()
            if (username.isNotEmpty()) {
                println("Created")
                lobbyViewModel.createLobby(username, character)
            } else {
                showToast("Please enter a username")
            }
        }

        binding.startGameButton.setOnClickListener {
            val username = binding.createUsernameEditText.text.toString().trim()
            val character = binding.createCharacterSpinner.selectedItem.toString()
            val lobbyId = lobbyViewModel.createdLobbyId.value

            if (username.isEmpty()) {
                showToast("Please enter a username")
                return@setOnClickListener
            }

            if (lobbyId.isNullOrBlank()) {
                showToast("No active lobby found")
                return@setOnClickListener
            }

            if (!lobbyViewModel.canStartGame.value) {
                showToast(getString(R.string.not_enough_players))
                return@setOnClickListener
            }

            lobbyViewModel.startGame(lobbyId, username, character)
            //findNavController().navigate(R.id.action_lobbyFragment_to_gameBoardIMG)
            showToast("Starting game...")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeConnectionState() }
                launch { observeLobbyId() }
                launch { observeLobbyState() }
                launch { observeErrorMessages() }
                launch { observeCanStartGame() }
                launch { observeGameStarted() }
                launch { observeGameState() }
            }
        }
    }

    private suspend fun observeConnectionState() {
        lobbyViewModel.isConnected.collect { isConnected ->
            binding.createLobbyButton.isEnabled = isConnected
            if (isConnected) {
                lobbyViewModel.getActiveLobbies()
            }
        }
    }

    private suspend fun observeLobbyId() {
        lobbyViewModel.createdLobbyId.collect { lobbyId ->
            val displayId = lobbyId ?: "-"
            binding.activeLobbyIdTextView.text = "Active Lobby ID: $displayId"
        }
    }

    private suspend fun observeLobbyState() {
        lobbyViewModel.lobbyState.collect { lobby ->
            if (lobby != null) {
                updateLobbyDisplay(lobby)
                handleStartGameButtonState(lobby)
            } else {
                resetLobbyDisplay()
            }
            binding.lobbyInfoTextView.scrollTo(0, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLobbyDisplay(lobby: at.aau.se2.cluedo.data.models.Lobby) {
        val playersList = buildPlayersList(lobby.players)
        binding.lobbyInfoTextView.text = """
            Lobby ID: ${lobby.id}
            Host: ${lobby.host.name} (${lobby.host.character}, ${lobby.host.color})
            Players (${lobby.players.size}):$playersList
        """.trimIndent()
    }

    private fun buildPlayersList(players: List<at.aau.se2.cluedo.data.models.Player>): String {
        return players.joinToString("\n") { player ->
            "  - ${player.name} (${player.character}, ${player.color})"
        }
    }

    private fun handleStartGameButtonState(lobby: at.aau.se2.cluedo.data.models.Lobby) {
        val canCheckStartGame = lobby.id != LobbyStatus.CREATING.text && lobby.players.size >= 3
        if (canCheckStartGame) {
            lobbyViewModel.checkCanStartGame(lobby.id)
        } else {
            binding.startGameButton.isEnabled = false
        }
    }

    private fun resetLobbyDisplay() {
        binding.lobbyInfoTextView.text = "-"
        binding.startGameButton.isEnabled = false
    }

    private suspend fun observeErrorMessages() {
        lobbyViewModel.errorMessages.collect { errorMessage ->
            showToast(errorMessage, Toast.LENGTH_SHORT)
        }
    }

    private suspend fun observeCanStartGame() {
        lobbyViewModel.canStartGame.collect { canStart ->
            binding.startGameButton.isEnabled = canStart
        }
    }

    private suspend fun observeGameStarted() {
        lobbyViewModel.gameStarted.collect { gameStarted ->
            if (gameStarted) {
                handleGameStartNavigation()
            }
        }
    }

    private fun handleGameStartNavigation() {
        showToast("Game started! Navigating to game screen...")
        try {
            findNavController().navigate(R.id.action_lobbyFragment_to_gameBoardIMG)
        } catch (e: Exception) {
            showToast("Error navigating to game: ${e.message}")
        }
    }

    private suspend fun observeGameState() {
        lobbyViewModel.gameState.collect { gameState ->
            if (gameState != null) {
                handleGameStateReceived(gameState)
            }
        }
    }

    private fun handleGameStateReceived(gameState: at.aau.se2.cluedo.data.models.GameStartedResponse) {
        showToast("Game state received with ${gameState.players.size} players")
        if (!lobbyViewModel.gameStarted.value) {
            lobbyViewModel.setGameStarted(true)
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
