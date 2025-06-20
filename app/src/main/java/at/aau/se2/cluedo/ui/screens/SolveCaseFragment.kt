package at.aau.se2.cluedo.ui.screens

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import at.aau.se2.cluedo.data.network.TurnBasedWebSocketService
import at.aau.se2.cluedo.data.network.WebSocketService
import com.example.myapplication.databinding.FragmentSolveCaseBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SolveCaseFragment : Fragment() {

    private val lobbyViewModel: LobbyViewModel by activityViewModels()
    private val turnBasedService = TurnBasedWebSocketService.getInstance()
    private val webSocketService = WebSocketService.getInstance()

    private var _binding: FragmentSolveCaseBinding? = null
    private val binding get() = _binding!!

    private var isPlayerEliminated: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSolveCaseBinding.inflate(inflater, container, false)

        binding.buttonSolveCase.setOnClickListener {
            solveCase()
        }

        binding.buttonCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            lobbyViewModel.lobbyState.collectLatest { lobby ->
                val currentUsername = lobbyViewModel.createdLobbyId.value
                val player = lobby?.players?.find { it.name == currentUsername }

                if (player != null) {
                    val navController = findNavController()
                    val bundle = Bundle().apply {
                        putString("winnerName", player.character.ifBlank { currentUsername })
                        putString("suspect", binding.suspectSpinner.selectedItem?.toString() ?: "N/A")
                        putString("room", binding.roomSpinner.selectedItem?.toString() ?: "N/A")
                        putString("weapon", binding.weaponSpinner.selectedItem?.toString() ?: "N/A")
                    }

                    when {
                        player.hasWon -> {
                            binding.buttonSolveCase.isEnabled = false
                            Toast.makeText(context, "You won! 🎉", Toast.LENGTH_LONG).show()

                            val winScreenId = resources.getIdentifier("winScreenFragment", "id", requireContext().packageName)
                            if (winScreenId != 0) {
                                navController.navigate(winScreenId, bundle)
                            }
                        }
                        lobby?.winnerUsername != null -> {
                            binding.buttonSolveCase.isEnabled = false
                            val updateScreenId = resources.getIdentifier("investigationUpdateFragment", "id", requireContext().packageName)
                            if (updateScreenId != 0) {
                                navController.navigate(updateScreenId, bundle)
                            }
                        }
                        !player.isActive -> {
                            binding.buttonSolveCase.isEnabled = false
                            isPlayerEliminated = true
                            Toast.makeText(context, "Wrong guess. You are eliminated! ❌", Toast.LENGTH_LONG).show()

                            val elimScreenId = resources.getIdentifier("eliminationScreenFragment", "id", requireContext().packageName)
                            if (elimScreenId != 0) {
                                navController.navigate(elimScreenId, bundle)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupSpinners() {
        if (_binding == null) {
            return
        }
        val context = requireContext()

        val suspectArray = arrayOf("Select a suspect", "Colonel Mustard", "Professor Plum", "Mr. Green", "Mrs. Peacock", "Miss Scarlett", "Mrs. White")
        val roomArray = arrayOf("Select a room", "Library", "Kitchen", "Ballroom", "Study", "Hall", "Billiard room", "Dining room", "Lounge", "Conservatory")
        val weaponArray = arrayOf("Select a weapon", "Candlestick", "Revolver", "Rope", "Lead Pipe", "Wrench", "Dagger")

        binding.suspectSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, suspectArray)
        binding.roomSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, roomArray)
        binding.weaponSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, weaponArray)
    }

    private fun solveCase() {
        if (_binding == null) {
            Toast.makeText(requireContext(), "Error: UI not ready.", Toast.LENGTH_SHORT).show()
            return
        }

        if (isPlayerEliminated) {
            Toast.makeText(context, "You are eliminated! You can't solve the case anymore.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedSuspect = binding.suspectSpinner.selectedItem.toString()
        val selectedRoom = binding.roomSpinner.selectedItem.toString()
        val selectedWeapon = binding.weaponSpinner.selectedItem.toString()

        if (selectedSuspect == "Select a suspect" || selectedRoom == "Select a room" || selectedWeapon == "Select a weapon") {
            Toast.makeText(context, "Please select all options.", Toast.LENGTH_SHORT).show()
            return
        }

        val lobbyId = lobbyViewModel.lobbyState.value?.id
        val username = webSocketService.player.value?.name

        if (lobbyId == null || username.isNullOrBlank()) {
            Toast.makeText(context, "Missing lobby or username info. Please rejoin.", Toast.LENGTH_SHORT).show()
            return
        }

        turnBasedService.makeAccusation(lobbyId, username, selectedSuspect, selectedWeapon, selectedRoom)

        // Show the chosen values via a Toast message
        Toast.makeText(
            requireContext(),
            "Solving case with: Suspect: $selectedSuspect, Room: $selectedRoom, Weapon: $selectedWeapon",
            Toast.LENGTH_LONG // Use LONG duration for better visibility
        ).show()

        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}