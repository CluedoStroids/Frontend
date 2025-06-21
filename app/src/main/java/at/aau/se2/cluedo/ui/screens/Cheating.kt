package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.viewmodels.LobbyViewmodel
import com.example.myapplication.databinding.FragmentCheatingBinding
import kotlinx.coroutines.launch

class CheatingSuspicionFragment : Fragment() {

    private var _binding: FragmentCheatingBinding? = null
    private val binding get() = _binding!!

    private val lobbyViewModel: LobbyViewmodel by viewModels()
    private var currentPlayers: List<Player> = emptyList()
    private var currentLobbyId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {

        binding.buttonSusbectCheating.setOnClickListener {
            submitCheatingSuspicion()
        }

        binding.buttonCancelCheating.setOnClickListener {
            dismiss()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            lobbyViewModel.lobbyState.collect { lobby ->
                lobby?.let {
                    currentLobbyId = it.id
                    currentPlayers = it.players
                    updatePlayerSpinner()
                }
            }
        }

        lifecycleScope.launch {
            lobbyViewModel.errorMessages.collect { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePlayerSpinner() {
        val currentPlayerName = lobbyViewModel.webSocketService.getPlayer()?.name

        val otherPlay = currentPlayers.filter { it.name != currentPlayerName }.map { "${it.name} (${it.character})" }

        val playerOptions = listOf("Select player to report...") + otherPlay

        val playerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            playerOptions
        )
        playerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.cheaterSpinner.adapter = playerAdapter
    }

    private fun submitCheatingSuspicion() {
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
            sendCheatingSuspicionToBackend(lobbyId, currentPlayer, suspectedPlayer)

            Toast.makeText(
                requireContext(),
                "Reporting ${suspectedPlayer.name} for cheating...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendCheatingSuspicionToBackend(lobbyId: String, suspect: Player, accuser: Player) {
        lobbyViewModel.webSocketService.reportCheating(lobbyId, suspect.name,accuser.name)

    }

    private fun dismiss() {
        binding.cheaterSpinner.setSelection(0)
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

