package at.aau.se2.cluedo.ui.screens

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import at.aau.se2.cluedo.data.network.TurnBasedWebSocketService
import at.aau.se2.cluedo.data.network.WebSocketService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SolveCaseFragment : Fragment() {

    private val lobbyViewModel: LobbyViewModel by viewModels()
    private val turnBasedService = TurnBasedWebSocketService.getInstance()
    private val webSocketService = WebSocketService.getInstance()

    private var isPlayerEliminated: Boolean = false

    private lateinit var suspectSpinner: Spinner
    private lateinit var roomSpinner: Spinner
    private lateinit var weaponSpinner: Spinner
    private lateinit var solveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val layoutId = context.resources.getIdentifier("fragment_solve_case", "layout", context.packageName)
        val view = inflater.inflate(layoutId, container, false)

        val suspectSpinnerId = context.resources.getIdentifier("suspectSpinner", "id", context.packageName)
        val roomSpinnerId = context.resources.getIdentifier("roomSpinner", "id", context.packageName)
        val weaponSpinnerId = context.resources.getIdentifier("weaponSpinner", "id", context.packageName)
        val solveButtonId = context.resources.getIdentifier("button_solve_case", "id", context.packageName)
        val cancelButtonId = context.resources.getIdentifier("button_cancel", "id", context.packageName)

        suspectSpinner = view.findViewById(suspectSpinnerId)
        roomSpinner = view.findViewById(roomSpinnerId)
        weaponSpinner = view.findViewById(weaponSpinnerId)
        solveButton = view.findViewById(solveButtonId)
        val cancelButton: Button = view.findViewById(cancelButtonId)

        solveButton.setOnClickListener {
            solveCase()
        }


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()

        lifecycleScope.launch {
            lobbyViewModel.lobbyState.collectLatest { lobby ->
                val currentUsername = lobbyViewModel.createdLobbyId.value
                val player = lobby?.players?.find { it.name == currentUsername }

                if (player != null) {
                    val navController = findNavController()
                    val bundle = Bundle().apply {
                        putString("winnerName", player.character.ifBlank { currentUsername })
                        putString("suspect", suspectSpinner.selectedItem.toString())
                        putString("room", roomSpinner.selectedItem.toString())
                        putString("weapon", weaponSpinner.selectedItem.toString())
                    }

                    when {
                        player.hasWon -> {
                            solveButton.isEnabled = false
                            Toast.makeText(context, "You won! 🎉", Toast.LENGTH_LONG).show()

                            val winScreenId = resources.getIdentifier("winScreenFragment", "id", requireContext().packageName)
                            if (winScreenId != 0) {
                                navController.navigate(winScreenId, bundle)
                            }
                        }
                        lobby?.winnerUsername != null -> {
                            val updateScreenId = resources.getIdentifier("investigationUpdateFragment", "id", requireContext().packageName)
                            if (updateScreenId != 0) {
                                navController.navigate(updateScreenId, bundle)
                            }
                        }
                        !player.isActive -> {
                            solveButton.isEnabled = false
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
        val context = requireContext()

        val suspectArray = arrayOf("Select a suspect", "Colonel Mustard", "Professor Plum", "Mr. Green", "Mrs. Peacock", "Miss Scarlett", "Mrs. White")
        val roomArray = arrayOf("Select a room", "Library", "Kitchen", "Ballroom", "Study", "Hall", "Billiard room", "Dining room", "Lounge", "Conservatory")
        val weaponArray = arrayOf("Select a weapon", "Candlestick", "Revolver", "Rope", "Lead Pipe", "Wrench", "Dagger")

        suspectSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, suspectArray)
        roomSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, roomArray)
        weaponSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, weaponArray)
    }

    private fun solveCase() {
        if (isPlayerEliminated) {
            Toast.makeText(context, "You are eliminated! You can't solve the case anymore.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedSuspect = suspectSpinner.selectedItem.toString()
        val selectedRoom = roomSpinner.selectedItem.toString()
        val selectedWeapon = weaponSpinner.selectedItem.toString()

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

        // Use the new accusation system instead of the old solveCase
        turnBasedService.makeAccusation(lobbyId, username, selectedSuspect, selectedWeapon, selectedRoom)
    }

    private fun animateAndClose(view: View) {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 300
        fadeOut.fillAfter = true
        view.startAnimation(fadeOut)

        view.postDelayed({
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }, 300)
    }
}
