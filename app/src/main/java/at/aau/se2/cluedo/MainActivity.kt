package at.aau.se2.cluedo

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import androidx.activity.viewModels

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val lobbyViewModel: LobbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        Toast.makeText(this, message, duration).show()
    }
}