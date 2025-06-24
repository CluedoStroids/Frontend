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
import at.aau.se2.cluedo.data.models.Lobby
import at.aau.se2.cluedo.data.models.LobbyStatus
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentJoinLobbyBinding
import kotlinx.coroutines.launch

class JoinLobbyFragment : Fragment() {

    private var _binding: FragmentJoinLobbyBinding? = null
    private val binding get() = _binding!!
    private val lobbyViewModel: LobbyViewModel by viewModels()
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
                    showToast("Found active lobby: $currentLobbyId")
                    activeLobbyId = currentLobbyId

                    // Update the UI with the lobby information
                    updateLobbyInfoUI()

                    // Join the lobby
                    showToast("Joining lobby: $currentLobbyId")
                    lobbyViewModel.joinLobby(currentLobbyId, username, character)
                } else {
                    // No active lobby found
                    showToast("No active lobby found. Please create a new lobby first.")
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

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    lobbyViewModel.isConnected.collect { isConnected ->
                        checkAndEnableButtons()
                    }
                }

                launch {
                    lobbyViewModel.createdLobbyId.collect { _ ->
                        checkAndEnableButtons()
                    }
                }

                launch {
                    lobbyViewModel.lobbyState.collect { lobby ->
                        if (lobby != null) {
                            val playersList = lobby.players.joinToString("\n") { player ->
                                "  - ${player.name} (${player.character}, ${player.color})"
                            }
                            binding.lobbyInfoTextView.text = """
                                Lobby ID: ${lobby.id}
                                Host: ${lobby.host.name} (${lobby.host.character}, ${lobby.host.color})
                                Players (${lobby.players.size}):
                                $playersList
                            """.trimIndent()
                        } else {
                            binding.lobbyInfoTextView.text = "-"
                        }
                        binding.lobbyInfoTextView.scrollTo(0, 0)
                        checkAndEnableButtons()
                    }
                }

                launch {
                    lobbyViewModel.errorMessages.collect { errorMessage ->
                        showToast(errorMessage, Toast.LENGTH_LONG)
                    }
                }

                launch {
                    lobbyViewModel.gameStarted.collect { gameStarted ->
                        if (gameStarted) {
                            showToast("Game started! Navigating to game screen...")
                            try {
                                findNavController().navigate(R.id.action_joinLobbyFragment_to_gameBoardIMG)
                            } catch (e: Exception) {
                                showToast("Error navigating to game: ${e.message}")
                            }
                        }
                    }
                }

                // Also check the game state directly
                launch {
                    lobbyViewModel.gameState.collect { gameState ->
                        if (gameState != null) {
                            showToast("Game state received with ${gameState.players.size} players")
                            if (!lobbyViewModel.gameStarted.value) {
                                // If we have a game state but gameStarted is false, set it to true
                                lobbyViewModel.setGameStarted(true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message, duration).show()
    }

    @SuppressLint("SetTextI1B")
    private fun updateLobbyInfoUI() {
        val lobby = lobbyViewModel.lobbyState.value

        lobby?.let {
            // 1. Update Lobby Info Text View
            binding.lobbyInfoTextView.text = formatLobbyInfo(it)

            // 2. Auto-fill Username
            autoFillUsername(it.players)

            // 3. Select Available Character
            selectAvailableCharacter(it.players)
        }
    }

    /**
     * Formats the Lobby object into a displayable string for the UI.
     */
    private fun formatLobbyInfo(lobby: Lobby): String {
        val playersList = lobby.players.joinToString("\n") { player ->
            "  - ${player.name} (${player.character}, ${player.color})"
        }

        return """
        Lobby ID: ${lobby.id}
        Host: ${lobby.host.name} (${lobby.host.character}, ${lobby.host.color})
        Players (${lobby.players.size}):
        $playersList
    """.trimIndent()
    }

    /**
     * Auto-fills the username field with a unique name if it's currently empty.
     */
    private fun autoFillUsername(currentPlayers: List<Player>) {
        if (binding.joinUsernameEditText.text.isNullOrBlank()) {
            val existingNames = currentPlayers.map { it.name }.toSet() // Use a Set for faster lookups
            val baseName = "Player"
            var counter = currentPlayers.size + 1 // Start counter slightly higher for common case
            var newName: String

            // Generate a unique name
            do {
                newName = "$baseName$counter"
                counter++
            } while (existingNames.contains(newName)) // Loop until a unique name is found

            binding.joinUsernameEditText.setText(newName)
        }
    }

    /**
     * Selects an available character in the spinner that is not already taken by other players.
     */
    private fun selectAvailableCharacter(currentPlayers: List<Player>) {
        val availableCharacters = lobbyViewModel.availableCharacters // Assuming this is a List<String> or similar
        val usedCharacters = currentPlayers.map { it.character }.toSet() // Use a Set for faster lookups

        val firstAvailableChar = availableCharacters.firstOrNull { !usedCharacters.contains(it) }
        // Fallback to the first available character if all are used (or none are available)
        val characterToSelect = firstAvailableChar ?: availableCharacters.firstOrNull()

        characterToSelect?.let { charName ->
            val adapter = binding.joinCharacterSpinner.adapter
            if (adapter is ArrayAdapter<*>) {
                // Find the position of the character to select
                for (i in 0 until adapter.count) {
                    val item = adapter.getItem(i)
                    if (item is String && item == charName) {
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
            binding.activeLobbyIdTextView.text = "Active Lobby ID: $activeLobbyId"
        } else {
            // If we're connected but don't have a lobby, we should still enable the join button
            binding.joinLobbyButton.isEnabled = isConnected
            binding.leaveLobbyButton.isEnabled = false
            binding.activeLobbyIdTextView.text = "Active Lobby ID: -"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
