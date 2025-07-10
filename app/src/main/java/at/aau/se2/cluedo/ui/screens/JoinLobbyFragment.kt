package at.aau.se2.cluedo.ui.screens

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
import com.example.myapplication.databinding.FragmentJoinLobbyBinding
import kotlinx.coroutines.launch

class JoinLobbyFragment : Fragment() {

    private var _binding: FragmentJoinLobbyBinding? = null
    private val binding get() = _binding!!
    private val lobbyViewModel: LobbyViewmodel by viewModels()
    private var activeLobbyId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinLobbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        lobbyViewModel.connect()

        // Check if a game has already started
        view.postDelayed({
            lobbyViewModel.checkGameStarted()
        }, 1000)
    }

    private fun setupUI() {
        binding.lobbyInfoTextView.movementMethod = ScrollingMovementMethod()
        val characterAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            lobbyViewModel.availableCharacters
        )
        binding.joinCharacterSpinner.adapter = characterAdapter


        val blueIndex = lobbyViewModel.availableCharacters.indexOf("Blue")
        if (blueIndex >= 0) {
            binding.joinCharacterSpinner.setSelection(blueIndex)
        }

        binding.joinLobbyButton.setOnClickListener {
            val username = binding.joinUsernameEditText.text.toString().trim()
            val character = binding.joinCharacterSpinner.selectedItem.toString()

            if (username.isEmpty()) {
                showToast("Please enter a username")
                return@setOnClickListener
            }

            // Show a toast to indicate we're checking for active lobbies
            showToast("Checking for active lobbies...")

            // Request active lobbies from the server
            lobbyViewModel.getActiveLobbies()

            // Wait a moment for the server to respond
            binding.root.postDelayed({
                // Check if we have an active lobby ID
                val currentLobbyId = lobbyViewModel.createdLobbyId.value

                if (currentLobbyId != null && currentLobbyId.isNotBlank()) {
                    // We found an active lobby, update the UI
                    showToast(getString(R.string.found_active_lobby, currentLobbyId))
                    activeLobbyId = currentLobbyId

                    // Update the UI with the lobby information
                    updateLobbyInfoUI()

                    // Join the lobby
                    showToast(getString(R.string.joining_lobby, currentLobbyId))
                    lobbyViewModel.joinLobby(currentLobbyId, username, character)
                } else {
                    // No active lobby found
                    showToast(getString(R.string.no_active_lobby_found))
                }
            }, 1000) // Increased delay to give more time for server response
        }

        binding.leaveLobbyButton.setOnClickListener {
            val username = binding.joinUsernameEditText.text.toString().trim()
            val character = binding.joinCharacterSpinner.selectedItem.toString()
            if (activeLobbyId != null) {
                lobbyViewModel.leaveLobby(activeLobbyId!!, username, character)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeConnectionState() }
                launch { observeCreatedLobbyId() }
                launch { observeLobbyState() }
                launch { observeErrorMessages() }
                launch { observeGameStarted() }
                launch { observeGameState() }
            }
        }
    }

    private suspend fun observeConnectionState() {
        lobbyViewModel.isConnected.collect { _ ->
            checkAndEnableButtons()
        }
    }

    private suspend fun observeCreatedLobbyId() {
        lobbyViewModel.createdLobbyId.collect { _ ->
            checkAndEnableButtons()
        }
    }

    private suspend fun observeLobbyState() {
        lobbyViewModel.lobbyState.collect { lobby ->
            updateLobbyInfoDisplay(lobby)
            binding.lobbyInfoTextView.scrollTo(0, 0)
            checkAndEnableButtons()
        }
    }

    private suspend fun observeErrorMessages() {
        lobbyViewModel.errorMessages.collect { errorMessage ->
            showToast(errorMessage, Toast.LENGTH_LONG)
        }
    }

    private suspend fun observeGameStarted() {
        lobbyViewModel.gameStarted.collect { gameStarted ->
            if (gameStarted) {
                handleGameStarted()
            }
        }
    }

    private suspend fun observeGameState() {
        lobbyViewModel.gameState.collect { gameState ->
            if (gameState != null) {
                showToast(resources.getQuantityString(R.plurals.game_state_players, gameState.players.size, gameState.players.size))
                if (!lobbyViewModel.gameStarted.value) {
                    lobbyViewModel.setGameStarted(true)
                }
            }
        }
    }

    private fun updateLobbyInfoDisplay(lobby: at.aau.se2.cluedo.data.models.Lobby?) {
        if (lobby != null) {
            val playersList = lobby.players.joinToString("\n") { player ->
                getString(R.string.player_list_item, player.name, player.character, player.color)
            }
            binding.lobbyInfoTextView.text = getString(
                R.string.lobby_info_format,
                lobby.id,
                lobby.host.name,
                lobby.host.character,
                lobby.host.color,
                lobby.players.size,
                playersList
            )
        } else {
            binding.lobbyInfoTextView.text = getString(R.string.lobby_info_empty)
        }
    }

    private fun handleGameStarted() {
        showToast(getString(R.string.game_started_navigating))
        try {
            findNavController().navigate(R.id.action_joinLobbyFragment_to_gameBoardIMG)
        } catch (e: Exception) {
            showToast(getString(R.string.error_navigating_to_game, e.message.orEmpty()))
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message, duration).show()
    }

    private fun updateLobbyInfoUI() {
        val lobby = lobbyViewModel.lobbyState.value
        if (lobby != null) {
            val playersList = lobby.players.joinToString("\n") { player ->
                getString(R.string.player_list_item, player.name, player.character, player.color)
            }
            binding.lobbyInfoTextView.text = getString(
                R.string.lobby_info_format,
                lobby.id,
                lobby.host.name,
                lobby.host.character,
                lobby.host.color,
                lobby.players.size,
                playersList
            )

            // Auto-fill the username field with a unique name if it's empty
            if (binding.joinUsernameEditText.text.isNullOrBlank()) {
                val existingNames = lobby.players.map { it.name }
                var counter = lobby.players.size + 1
                var newName = getString(R.string.default_player_name, counter)

                // Make sure the name is unique
                while (existingNames.contains(newName)) {
                    counter++
                    newName = getString(R.string.default_player_name, counter)
                }

                binding.joinUsernameEditText.setText(newName)
            }

            // Select a character that's not already taken
            val availableCharacters = lobbyViewModel.availableCharacters
            val usedCharacters = lobby.players.map { it.character }
            val availableCharacter = availableCharacters.firstOrNull { !usedCharacters.contains(it) } ?: availableCharacters.first()

            val adapter = binding.joinCharacterSpinner.adapter
            if (adapter is ArrayAdapter<*>) {
                // Type-safe approach: use adapter.getItem() to get actual items and compare them
                for (i in 0 until adapter.count) {
                    val item = adapter.getItem(i)
                    if (item is String && item == availableCharacter) {
                        binding.joinCharacterSpinner.setSelection(i)
                        break
                    }
                }
            }
        }
    }

    private fun checkAndEnableButtons() {
        val createdId = lobbyViewModel.createdLobbyId.value
        val lobbyId = lobbyViewModel.lobbyState.value?.id
        val isConnected = lobbyViewModel.isConnected.value

        // Check both createdLobbyId and lobbyState.id
        activeLobbyId = when {
            createdId != null && createdId.isNotBlank() -> createdId
            lobbyId != null && lobbyId.isNotBlank() && lobbyId != LobbyStatus.CREATING.text -> lobbyId
            else -> null
        }

        // Update UI based on whether we have an active lobby
        if (activeLobbyId != null) {
            binding.joinLobbyButton.isEnabled = isConnected
            binding.leaveLobbyButton.isEnabled = isConnected
            binding.activeLobbyIdTextView.text = getString(R.string.active_lobby_id, activeLobbyId)
        } else {
            // If we're connected but don't have a lobby, we should still enable the join button
            binding.joinLobbyButton.isEnabled = isConnected
            binding.leaveLobbyButton.isEnabled = false
            binding.activeLobbyIdTextView.text = getString(R.string.active_lobby_id_empty)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}