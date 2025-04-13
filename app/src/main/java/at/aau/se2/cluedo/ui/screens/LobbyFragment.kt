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
import com.example.myapplication.databinding.FragmentLobbyBinding
import kotlinx.coroutines.launch

class LobbyFragment : Fragment() {

    private var _binding: FragmentLobbyBinding? = null
    private val binding get() = _binding!!
    private val lobbyViewModel: LobbyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLobbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.lobbyInfoTextView.movementMethod = ScrollingMovementMethod()

        binding.connectButton.setOnClickListener {
            lobbyViewModel.connect()
        }
        binding.disconnectButton.setOnClickListener {
            lobbyViewModel.disconnect()
        }
        binding.createLobbyButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            if (username.isNotEmpty()) {
                lobbyViewModel.createLobby(username)
            } else {
                showToast("Please enter a username")
            }
        }
        binding.joinLobbyButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val lobbyId = binding.lobbyIdEditText.text.toString().trim()
            if (username.isNotEmpty() && lobbyId.isNotEmpty()) {
                lobbyViewModel.joinLobby(lobbyId, username)
            } else {
                showToast("Please enter username and lobby ID")
            }
        }
        binding.leaveLobbyButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val lobbyId = binding.lobbyIdEditText.text.toString().trim()
            if (username.isNotEmpty() && lobbyId.isNotEmpty()) {
                lobbyViewModel.leaveLobby(lobbyId, username)
            } else {
                showToast("Please enter username and lobby ID")
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    lobbyViewModel.isConnected.collect { isConnected ->
                        binding.statusTextView.text = if (isConnected) "Status: Connected" else "Status: Disconnected"
                        binding.connectButton.isEnabled = !isConnected
                        binding.disconnectButton.isEnabled = isConnected
                        binding.createLobbyButton.isEnabled = isConnected
                        binding.joinLobbyButton.isEnabled = isConnected
                        if (!isConnected) {
                            binding.lobbyInfoTextView.text = "-"
                            binding.createdLobbyIdTextView.text = "Created Lobby ID: -"
                        }
                    }
                }
                launch {
                    lobbyViewModel.createdLobbyId.collect { lobbyId ->
                        val displayId = lobbyId ?: "-"
                        binding.createdLobbyIdTextView.text = "Created Lobby ID: $displayId"
                        if (lobbyId != null) {
                            binding.lobbyIdEditText.setText(lobbyId)
                        }
                    }
                }
                launch {
                    lobbyViewModel.lobbyState.collect { lobby ->
                        if (lobby != null) {
                            val participants = lobby.participants.joinToString("\n  - ", prefix = "\n  - ")
                            binding.lobbyInfoTextView.text = """
                                Lobby ID: ${lobby.id}
                                Host: ${lobby.host}
                                Participants (${lobby.participants.size}):$participants
                            """.trimIndent()
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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
