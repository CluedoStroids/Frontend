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
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import com.example.myapplication.R
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

        // Set default selection to Blue
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

            hasShownLobbyFoundToast = false
            lobbyViewModel.getActiveLobby()

            binding.root.postDelayed({
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
                        showToast("No active lobby found. Please create a new lobby first.")
                    }
                }

                checkAndEnableButtons()
            }, 500)
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
                    // Try to refresh lobby information first
                    lobbyViewModel.getActiveLobby()
                    binding.root.postDelayed({
                        checkAndEnableButtons()
                    }, 500)
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
                        if (isConnected) {
                            lobbyViewModel.getActiveLobby()
                        }
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
                            showToast(getString(R.string.game_started))
                            // Navigate to the game screen
                            findNavController().navigate(R.id.action_joinLobbyFragment_to_gameFragment)
                        }
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


        val lobbyId = when {
            currentLobbyId != null && currentLobbyId.isNotBlank() -> currentLobbyId
            currentLobbyState != null && currentLobbyState.id.isNotBlank() && currentLobbyState.id != "Creating..." -> currentLobbyState.id
            else -> null
        }

        if (lobbyId != null) {
            if (!hasShownLobbyFoundToast) {
                showToast("Found active lobby: $lobbyId")
                hasShownLobbyFoundToast = true
            }
            binding.joinLobbyButton.isEnabled = isConnected
            binding.leaveLobbyButton.isEnabled = isConnected
            binding.activeLobbyIdTextView.text = "Active Lobby ID: $lobbyId"
        } else {
            binding.joinLobbyButton.isEnabled = false
            binding.leaveLobbyButton.isEnabled = false
            binding.activeLobbyIdTextView.text = "Active Lobby ID: -"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
