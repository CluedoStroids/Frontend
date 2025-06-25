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
import at.aau.se2.cluedo.viewmodels.LobbyViewmodel
import at.aau.se2.cluedo.data.network.TurnBasedWebSocketService
import at.aau.se2.cluedo.data.network.WebSocketService
import com.example.myapplication.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import at.aau.se2.cluedo.data.models.Player
import at.aau.se2.cluedo.viewmodels.NavigationTarget
import com.example.myapplication.databinding.FragmentAccusationBinding
import com.example.myapplication.databinding.FragmentGameBinding

class AccusationFragment : Fragment() {

    private val lobbyViewModel: LobbyViewmodel by viewModels()
    private val turnBasedService = TurnBasedWebSocketService.getInstance()
    private val webSocketService = WebSocketService.getInstance()

    private var _binding: FragmentAccusationBinding? = null
    private val binding get() = _binding!!

    private var isPlayerEliminated: Boolean = false

    private lateinit var suspectSpinner: Spinner
    private lateinit var roomSpinner: Spinner
    private lateinit var weaponSpinner: Spinner
    private lateinit var accuseButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAccusationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suspectSpinner = binding.suspectSpinner
        roomSpinner = binding.roomSpinner
        weaponSpinner = binding.weaponSpinner
        accuseButton = binding.buttonSolveCase

        val cancelButton: Button = binding.buttonCancel
        cancelButton.setOnClickListener {
            animateAndClose(it)
            findNavController().navigate(R.id.gameBoardIMG)
        }

        accuseButton.setOnClickListener {
            makeAccusation()
        }

        setupSpinners()

        // Subscribe to accusation results for this lobby
        val lobbyId = lobbyViewModel.lobbyState.value?.id
        if (lobbyId != null) {
            lobbyViewModel.subscribeToAccusationResult(lobbyId)
        }

        lifecycleScope.launch {
            lobbyViewModel.navigationEvents.collectLatest { target ->
                Log.d("AccusationFragment", "Navigation event received: $target")
                val bundle = createResultBundle(
                    webSocketService.player.value ?: return@collectLatest,
                    webSocketService.player.value?.name ?: "Unknown"
                )

                when (target) {
                    is NavigationTarget.WinScreen -> {
                        Log.d("AccusationFragment", "Navigating to win screen")
                        findNavController().navigate(R.id.winScreenFragment, bundle)
                    }
                    is NavigationTarget.EliminationScreen -> {
                        Log.d("AccusationFragment", "Navigating to elimination screen - using eliminationUpdateFragment")
                        // Since eliminationScreenFragment doesn't exist, use eliminationUpdateFragment
                        findNavController().navigate(R.id.eliminationUpdateFragment, bundle)
                    }
                    is NavigationTarget.EliminationUpdate -> {
                        Log.d("AccusationFragment", "Navigating to elimination update for ${target.playerName}")
                        bundle.putString("eliminatedPlayer", target.playerName)
                        findNavController().navigate(R.id.eliminationUpdateFragment, bundle)
                    }
                    is NavigationTarget.InvestigationUpdate -> {
                        Log.d("AccusationFragment", "Navigating to investigation update for ${target.playerName}")
                        bundle.putString("winningPlayer", target.playerName)
                        // Since investigationUpdateFragment doesn't exist, use winScreenFragment
                        findNavController().navigate(R.id.winScreenFragment, bundle)
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
