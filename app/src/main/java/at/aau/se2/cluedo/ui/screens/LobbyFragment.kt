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
import at.aau.se2.cluedo.data.models.LobbyStatus
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import com.example.myapplication.databinding.FragmentLobbyBinding
import kotlinx.coroutines.launch

class LobbyFragment : Fragment() {

    private var _binding: FragmentLobbyBinding? = null
    private val binding get() = _binding!!
    private val lobbyViewModel: LobbyViewModel by viewModels()

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
                lobbyViewModel.createLobby(username, character)
            } else {
                showToast("Please enter a username")
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {observeConnection()}

                launch { observeCreation() }

                launch {observeLobbyState()}

                launch {
                    lobbyViewModel.errorMessages.collect { errorMessage ->
                        showToast(errorMessage, Toast.LENGTH_LONG)
                    }
                }
            }
        }
    }

    suspend fun observeConnection(){
        lobbyViewModel.isConnected.collect { isConnected ->
            binding.statusTextView.text =
                if (isConnected) "Status: Connected" else "Status: Connecting..."

            binding.createLobbyButton.isEnabled = isConnected

            if (!isConnected) {
                binding.lobbyInfoTextView.text = "-"
                binding.activeLobbyIdTextView.text = "Active Lobby ID: -"
                showToast("Not connected to server. Please check your connection.")
            } else {
                lobbyViewModel.getActiveLobby()
            }
        }
    }

    suspend fun observeCreation(){
        lobbyViewModel.createdLobbyId.collect { lobbyId ->
            val displayId = lobbyId ?: "-"
            binding.activeLobbyIdTextView.text = "Active Lobby ID: $displayId"
        }
    }

    @SuppressLint("SetTextI18n")
    suspend fun observeLobbyState(){
        lobbyViewModel.lobbyState.collect { lobby ->
            if (lobby != null) {
                val playersList = lobby.players.joinToString("\n") { player ->
                    "  - ${player.name} (${player.character}, ${player.color})"
                }
                binding.lobbyInfoTextView.text = """
                                Lobby ID: ${lobby.id}
                                Host: ${lobby.host.name} (${lobby.host.character}, ${lobby.host.color})
                                Players (${lobby.players.size}):$playersList
                            """.trimIndent()

                if (lobby.id != LobbyStatus.CREATING.text) {
                    binding.activeLobbyIdTextView.text = "Active Lobby ID: ${lobby.id}"
                }
            } else {
                binding.lobbyInfoTextView.text = "-"
            }
            binding.lobbyInfoTextView.scrollTo(0, 0)
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
