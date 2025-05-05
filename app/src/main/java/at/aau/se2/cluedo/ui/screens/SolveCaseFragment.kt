package at.aau.se2.cluedo.ui.screens

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
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log
import androidx.navigation.fragment.findNavController


class SolveCaseFragment : Fragment() {

    private val lobbyViewModel: LobbyViewModel by viewModels()

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

        setupSpinners()

        solveButton.setOnClickListener {
            solveCase()
        }

        cancelButton.setOnClickListener {
            val navController = findNavController()
            val destinationId = resources.getIdentifier(
                "boardPlaceholderFragment", "id", requireContext().packageName
            )
            if (destinationId != 0) {
                navController.navigate(destinationId)
            } else {
                Toast.makeText(context, "Board screen not found in navigation graph.", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            lobbyViewModel.lobbyState.collectLatest { lobby ->
                val currentUsername = lobbyViewModel.createdLobbyId.value
                val player = lobby?.players?.find { it.username == currentUsername }

                if (player != null) {
                    if (player.hasWon) {
                        solveButton.isEnabled = false
                        Toast.makeText(context, "You won! 🎉", Toast.LENGTH_LONG).show()
                    } else if (player.isEliminated) {
                        solveButton.isEnabled = false
                        isPlayerEliminated = true
                        Toast.makeText(context, "Wrong guess. You are eliminated! ❌", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupSpinners() {
        val context = requireContext()

        val suspectArray = arrayOf(
            "Select a suspect", "Miss Scarlet", "Professor Plum", "Colonel Mustard", "Mrs. Peacock", "Mrs. White", "Mr. Green"
        )

        val roomArray = arrayOf(
            "Select a room", "Library", "Kitchen", "Ballroom", "Study", "Hall",
            "Billiard room", "Dining room", "Lounge", "Conservatory"
        )

        val weaponArray = arrayOf(
            "Select a weapon", "Candlestick", "Revolver", "Rope", "Lead Pipe", "Wrench", "Dagger"
        )

        suspectSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, suspectArray)
        roomSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, roomArray)
        weaponSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, weaponArray)

        Log.d("SPINNER_CHECK", "Suspects: ${suspectArray.joinToString()}")
        Log.d("SPINNER_CHECK", "Rooms: ${roomArray.joinToString()}")
        Log.d("SPINNER_CHECK", "Weapons: ${weaponArray.joinToString()}")
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
        val username = lobbyViewModel.createdLobbyId.value

        if (lobbyId == null) {
            Toast.makeText(context, "Lobby ID is missing. Please rejoin the lobby.", Toast.LENGTH_SHORT).show()
            Log.e("SOLVE_CASE", "Missing lobby ID")
            return
        }

        if (username.isNullOrBlank()) {
            Toast.makeText(context, "Username is missing. Please set your username.", Toast.LENGTH_SHORT).show()
            Log.e("SOLVE_CASE", "Missing username")
            return
        }


        Log.d("SOLVE_CASE", "Attempting solution: $selectedSuspect, $selectedRoom, $selectedWeapon by $username in $lobbyId")

        lobbyViewModel.solveCase(lobbyId, username, selectedSuspect, selectedRoom, selectedWeapon)
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