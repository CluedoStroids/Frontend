package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import com.example.myapplication.databinding.FragmentCheatingBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CheatingFragment : Fragment() {

    private var _binding: FragmentCheatingBinding? = null
    private val binding get() = _binding!!


    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    private var currentPlayers: List<Player> = emptyList()
    private var currentLobbyId: String? = null


    private val TAG = "CheatingFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Fragment created.")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Inflating layout.")
        _binding = FragmentCheatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View created. Binding present: ${_binding != null}")

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        Log.d(TAG, "setupUI: Setting up UI listeners.")
        binding.buttonSusbectCheating.setOnClickListener {
            Log.d(TAG, "Suspect Cheating button clicked.")
            submitCheatingSuspicion()
        }

        binding.buttonCancelCheating.setOnClickListener {
            Log.d(TAG, "Cancel button clicked. Attempting to pop back stack.")
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel: Starting observers.")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    lobbyViewModel.lobbyState.collectLatest { lobby ->
                        Log.d(TAG, "lobbyState collected: $lobby")
                        lobby?.let {
                            currentLobbyId = it.id
                            currentPlayers = it.players
                            Log.d(TAG, "Lobby data updated. Current players: ${currentPlayers.size}")
                            updatePlayerSpinner()
                        } ?: Log.d(TAG, "lobbyState collected: lobby is null")
                    }
                }


                launch {
                    lobbyViewModel.errorMessages.collectLatest { message ->
                        Log.e(TAG, "Error message: $message")
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updatePlayerSpinner() {
        if (_binding == null) {
            Log.e(TAG, "updatePlayerSpinner: _binding is null, cannot update spinner.")
            return
        }
        val currentPlayerName = lobbyViewModel.webSocketService.getPlayer()?.name
        Log.d(TAG, "updatePlayerSpinner: Current player name: $currentPlayerName")

        val otherPlayers = currentPlayers.filter { it.name != currentPlayerName }
            .map { "${it.name} (${it.character})" }

        val playerOptions = listOf("Select player to report...") + otherPlayers
        Log.d(TAG, "updatePlayerSpinner: Spinner options: ${playerOptions.size}")

        val playerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            playerOptions
        )
        playerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.cheaterSpinner.adapter = playerAdapter
    }

    private fun submitCheatingSuspicion() {
        Log.d(TAG, "submitCheatingSuspicion: Submitting suspicion.")
        if (_binding == null) {
            Log.e(TAG, "submitCheatingSuspicion: _binding is null, cannot submit.")
            Toast.makeText(requireContext(), "Error: UI not ready.", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedPlayerIndex = binding.cheaterSpinner.selectedItemPosition

        if (selectedPlayerIndex <= 0) {
            Toast.makeText(requireContext(), "Please select a player to report", Toast.LENGTH_SHORT).show()
            return
        }

        val currentPlayer = lobbyViewModel.webSocketService.getPlayer()
        if (currentPlayer == null) {
            Toast.makeText(requireContext(), "Error: Current player data not found", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPlayerText = binding.cheaterSpinner.selectedItem.toString()
        val suspectedPlayerName = selectedPlayerText.substringBefore(" (")
        val suspectedPlayer = currentPlayers.find { it.name == suspectedPlayerName }

        if (suspectedPlayer == null) {
            Toast.makeText(requireContext(), "Error: Selected player data not found", Toast.LENGTH_SHORT).show()
            return
        }

        currentLobbyId?.let { lobbyId ->
            sendCheatingSuspicionToBackend(lobbyId, suspectedPlayer, currentPlayer)

            Toast.makeText(
                requireContext(),
                "Reporting ${suspectedPlayer.name} for cheating...",
                Toast.LENGTH_SHORT
            ).show()

            Log.d(TAG, "submitCheatingSuspicion: Suspicion sent. Popping back stack.")
            parentFragmentManager.popBackStack()
        } ?: run {
            Toast.makeText(requireContext(), "Error: Lobby ID not available", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "submitCheatingSuspicion: Lobby ID is null.")
        }
    }

    private fun sendCheatingSuspicionToBackend(lobbyId: String, suspect: Player, accuser: Player) {
        Log.d(TAG, "sendCheatingSuspicionToBackend: Reporting ${suspect.name} by ${accuser.name} in lobby $lobbyId")
        lobbyViewModel.webSocketService.reportCheating(lobbyId, suspect.name, accuser.name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: View destroyed. Setting _binding to null.")
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Fragment destroyed.")
    }
}