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
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import com.example.myapplication.databinding.FragmentJoinLobbyBinding
import kotlinx.coroutines.launch

class JoinLobbyFragment : Fragment() {

    private var _binding: FragmentJoinLobbyBinding? = null
    private val binding get() = _binding!!
    private val lobbyViewModel: LobbyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinLobbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()

        lobbyViewModel.connect()

        view.postDelayed({
            lobbyViewModel.getActiveLobby()
            checkAndEnableButtons()
        }, 500)

        view.postDelayed({
            lobbyViewModel.getActiveLobby()
            checkAndEnableButtons()
        }, 1000)

        view.postDelayed({
            lobbyViewModel.getActiveLobby()
            checkAndEnableButtons()
        }, 2000)

        view.postDelayed({
            lobbyViewModel.getActiveLobby()
            checkAndEnableButtons()
        }, 3000)

        view.postDelayed({
            checkAndEnableButtons()
        }, 3500)
    }

    private fun setupUI() {
        binding.lobbyInfoTextView.movementMethod = ScrollingMovementMethod()

        val characterAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            lobbyViewModel.availableCharacters
        )
        binding.joinCharacterSpinner.adapter = characterAdapter

        // Set default selection to Blue
        val blueIndex = lobbyViewModel.availableCharacters.indexOf("Blue")
        if (blueIndex >= 0) {
            binding.joinCharacterSpinner.setSelection(blueIndex)
        }

        binding.refreshButton.setOnClickListener {
            showToast("Refreshing lobby information...")

            hasShownLobbyFoundToast = false

            lobbyViewModel.getActiveLobby()

            binding.root.postDelayed({
                lobbyViewModel.getActiveLobby()
                checkAndEnableButtons()
            }, 500)

            binding.root.postDelayed({
                lobbyViewModel.getActiveLobby()
                checkAndEnableButtons()
            }, 1000)

            checkAndEnableButtons()
        }

        binding.joinLobbyButton.setOnClickListener {
            val username = binding.joinUsernameEditText.text.toString().trim()
            val character = binding.joinCharacterSpinner.selectedItem.toString()

            if (username.isEmpty()) {
                showToast("Please enter a username")
                return@setOnClickListener
            }

            val activeLobbyId = lobbyViewModel.createdLobbyId.value
            val lobbyStateId = lobbyViewModel.lobbyState.value?.id

            when {
                activeLobbyId != null && activeLobbyId.isNotBlank() -> {
                    showToast("Joining lobby: $activeLobbyId")
                    lobbyViewModel.joinLobby(activeLobbyId, username, character)
                }

                lobbyStateId != null && lobbyStateId.isNotBlank() && lobbyStateId != "Creating..." -> {
                    showToast("Joining lobby from state: $lobbyStateId")
                    lobbyViewModel.joinLobby(lobbyStateId, username, character)
                }

                else -> {
                    lobbyViewModel.getActiveLobby()
                    showToast("No active lobby found. Please create a new lobby first or use the refresh button.")
                }
            }
        }

        binding.leaveLobbyButton.setOnClickListener {
            val username = binding.joinUsernameEditText.text.toString().trim()
            val character = binding.joinCharacterSpinner.selectedItem.toString()

            if (username.isEmpty()) {
                showToast("Please enter a username")
                return@setOnClickListener
            }

            val activeLobbyId = lobbyViewModel.createdLobbyId.value
            val lobbyStateId = lobbyViewModel.lobbyState.value?.id

            when {
                activeLobbyId != null && activeLobbyId.isNotBlank() -> {
                    showToast("Leaving lobby: $activeLobbyId")
                    lobbyViewModel.leaveLobby(activeLobbyId, username, character)
                }

                lobbyStateId != null && lobbyStateId.isNotBlank() && lobbyStateId != "Creating..." -> {
                    showToast("Leaving lobby from state: $lobbyStateId")
                    lobbyViewModel.leaveLobby(lobbyStateId, username, character)
                }

                else -> {
                    showToast("No active lobby found. You need to join a lobby before leaving it.")
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    lobbyViewModel.isConnected.collect { isConnected ->
                        binding.statusTextView.text =
                            if (isConnected) "Status: Connected" else "Status: Connecting..."


                        val activeLobbyId = lobbyViewModel.createdLobbyId.value
                        val lobbyStateId = lobbyViewModel.lobbyState.value?.id

                        val hasValidLobby = (activeLobbyId != null && activeLobbyId.isNotBlank()) ||
                                (lobbyStateId != null && lobbyStateId.isNotBlank() && lobbyStateId != "Creating...")

                        binding.joinLobbyButton.isEnabled = isConnected && hasValidLobby
                        binding.leaveLobbyButton.isEnabled = isConnected && hasValidLobby

                        if (!isConnected) {
                            if (binding.statusTextView.text != "Status: Connecting...") {
                                showToast("Not connected to server. Please check your connection.")
                            }
                        } else {
                            lobbyViewModel.getActiveLobby()

                            checkAndEnableButtons()
                        }
                    }
                }
                launch {
                    lobbyViewModel.createdLobbyId.collect { lobbyId ->
                        val displayId = lobbyId ?: "-"
                        binding.activeLobbyIdTextView.text = "Active Lobby ID: $displayId"

                        val isConnected = lobbyViewModel.isConnected.value
                        val shouldEnableButtons =
                            lobbyId != null && lobbyId.isNotBlank() && isConnected

                        binding.joinLobbyButton.isEnabled = shouldEnableButtons
                        binding.leaveLobbyButton.isEnabled = shouldEnableButtons

                        if (shouldEnableButtons) {
                            showToast("Active lobby found: $lobbyId")
                        }
                    }
                }
                launch {
                    lobbyViewModel.lobbyState.collect { lobby ->
                        if (lobby != null) {
                            val playersList = lobby.players.joinToString("\n") { player ->
                                "  - ${player.name} (${player.character})"
                            }
                            binding.lobbyInfoTextView.text = """
                                Lobby ID: ${lobby.id}
                                Host: ${lobby.host.name} (${lobby.host.character})
                                Players (${lobby.players.size}):
$playersList
                            """.trimIndent()

                            if (lobby.id != "Creating...") {
                                binding.activeLobbyIdTextView.text = "Active Lobby ID: ${lobby.id}"

                                if (lobbyViewModel.createdLobbyId.value != lobby.id) {
                                    lobbyViewModel.getActiveLobby()
                                }

                                binding.joinLobbyButton.isEnabled = lobbyViewModel.isConnected.value
                                binding.leaveLobbyButton.isEnabled =
                                    lobbyViewModel.isConnected.value
                            }
                        } else {
                            binding.lobbyInfoTextView.text = "-"
                        }
                        binding.lobbyInfoTextView.scrollTo(0, 0)
                    }
                }
                launch {
                    lobbyViewModel.errorMessages.collect { errorMessage ->
                        showToast(errorMessage, Toast.LENGTH_LONG)
                    }
                }
            }
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message, duration).show()
    }

    private var hasShownLobbyFoundToast = false

    private fun checkAndEnableButtons() {
        val currentLobbyId = lobbyViewModel.createdLobbyId.value
        val currentLobbyState = lobbyViewModel.lobbyState.value
        val isConnected = lobbyViewModel.isConnected.value

        if (!isConnected) {
            binding.statusTextView.text = "Status: Connecting..."
            return
        }

        binding.statusTextView.text = "Status: Connected"

        if (currentLobbyId != null && currentLobbyId.isNotBlank()) {
            // Only show the toast once to avoid spamming
            if (!hasShownLobbyFoundToast) {
                showToast("Found active lobby ID: $currentLobbyId")
                hasShownLobbyFoundToast = true
            }
            binding.joinLobbyButton.isEnabled = true
            binding.leaveLobbyButton.isEnabled = true
            binding.activeLobbyIdTextView.text = "Active Lobby ID: $currentLobbyId"
        } else if (currentLobbyState != null && currentLobbyState.id.isNotBlank() && currentLobbyState.id != "Creating...") {
            if (!hasShownLobbyFoundToast) {
                showToast("Found active lobby from state: ${currentLobbyState.id}")
                hasShownLobbyFoundToast = true
            }
            binding.joinLobbyButton.isEnabled = true
            binding.leaveLobbyButton.isEnabled = true
            binding.activeLobbyIdTextView.text = "Active Lobby ID: ${currentLobbyState.id}"
        } else {
            binding.activeLobbyIdTextView.text = "Active Lobby ID: -"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
