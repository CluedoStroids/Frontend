package at.aau.se2.cluedo.ui.screens

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
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
import com.example.myapplication.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.viewmodels.NavigationTarget

class AccusationFragment : Fragment() {

    private val lobbyViewModel: LobbyViewModel by viewModels()
    private val turnBasedService = TurnBasedWebSocketService.getInstance()
    private val webSocketService = WebSocketService.getInstance()

    private var isPlayerEliminated: Boolean = false

    private lateinit var suspectSpinner: Spinner
    private lateinit var roomSpinner: Spinner
    private lateinit var weaponSpinner: Spinner
    private lateinit var accuseButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val layoutId = context.resources.getIdentifier("fragment_accusation", "layout", context.packageName)
        val view = inflater.inflate(layoutId, container, false)

        val suspectSpinnerId = context.resources.getIdentifier("suspectSpinner", "id", context.packageName)
        val roomSpinnerId = context.resources.getIdentifier("roomSpinner", "id", context.packageName)
        val weaponSpinnerId = context.resources.getIdentifier("weaponSpinner", "id", context.packageName)
        val accuseButtonId = context.resources.getIdentifier("button_accuse", "id", context.packageName)
        val cancelButtonId = context.resources.getIdentifier("button_cancel", "id", context.packageName)

        suspectSpinner = view.findViewById(suspectSpinnerId)
        roomSpinner = view.findViewById(roomSpinnerId)
        weaponSpinner = view.findViewById(weaponSpinnerId)
        accuseButton = view.findViewById(accuseButtonId)

        val cancelButton: Button = view.findViewById(cancelButtonId)
        cancelButton.setOnClickListener {
            animateAndClose(it)
            it.postDelayed({
                findNavController().navigate(R.id.gameBoardIMG)
            }, 300)
        }

        accuseButton.setOnClickListener {
            makeAccusation()
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
            lobbyViewModel.navigationEvents.collectLatest { target ->
                val bundle = createResultBundle(
                    webSocketService.player.value ?: return@collectLatest,
                    webSocketService.player.value?.name ?: "Unknown"
                )

                when (target) {
                    is NavigationTarget.WinScreen -> navigateIfExists("winScreenFragment", bundle)
                    is NavigationTarget.EliminationScreen -> navigateIfExists("eliminationScreenFragment", bundle)
                    is NavigationTarget.EliminationUpdate -> {
                        bundle.putString("eliminatedPlayer", target.playerName)
                        navigateIfExists("eliminationUpdateFragment", bundle)
                    }
                    is NavigationTarget.InvestigationUpdate -> {
                        bundle.putString("winningPlayer", target.playerName)
                        navigateIfExists("investigationUpdateFragment", bundle)
                    }
                }
            }
        }
    }

    private fun createResultBundle(player: Player, username: String): Bundle {
        return Bundle().apply {
            putString("winnerName", player.character.ifBlank { username })
            putString("suspect", suspectSpinner.selectedItem.toString())
            putString("room", roomSpinner.selectedItem.toString())
            putString("weapon", weaponSpinner.selectedItem.toString())
        }
    }

    private fun navigateIfExists(fragmentName: String, bundle: Bundle) {
        val id = resources.getIdentifier(fragmentName, "id", requireContext().packageName)
        if (id != 0) {
            findNavController().navigate(id, bundle)
        }
    }

    private fun setupSpinners() {
        val context = requireContext()

        val suspectArray = arrayOf("Select a suspect", "Colonel Mustard", "Professor Plum", "Mr. Green", "Mrs. Peacock", "Miss Scarlet", "Mrs. White")
        val roomArray = arrayOf("Select a room", "Library", "Kitchen", "Ballroom", "Study", "Hall", "Billiard room", "Dining room", "Lounge", "Conservatory")
        val weaponArray = arrayOf("Select a weapon", "Candlestick", "Revolver", "Rope", "Lead Pipe", "Wrench", "Dagger")

        suspectSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, suspectArray)
        roomSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, roomArray)
        weaponSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, weaponArray)
    }

    private fun makeAccusation() {
        if (isPlayerEliminated) {
            Toast.makeText(context, "You are eliminated! You can't accuse anymore.", Toast.LENGTH_SHORT).show()
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

        Log.d("Accusation", "Sending accusation: $selectedSuspect, $selectedRoom, $selectedWeapon, by $username")

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
