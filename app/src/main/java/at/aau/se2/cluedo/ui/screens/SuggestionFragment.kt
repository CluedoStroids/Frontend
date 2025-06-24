package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import at.aau.se2.cluedo.viewmodels.LobbyViewmodel
import com.example.myapplication.R

import at.aau.se2.cluedo.data.network.TurnBasedWebSocketService
import at.aau.se2.cluedo.data.network.WebSocketService
import com.example.myapplication.databinding.FragmentSuggestionBinding

class SuggestionFragment : Fragment() {

    private var _binding: FragmentSuggestionBinding? = null
    private val binding get() = _binding!!

    private val lobbyViewModel: LobbyViewmodel by activityViewModels()
    private val turnBasedService = TurnBasedWebSocketService.getInstance()
    private val webSocketService = WebSocketService.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuggestionBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentPlayer = lobbyViewModel.lobbyState.value?.players?.find { it.isCurrentPlayer == true }

        /*
        if (!lobbyViewModel.isPlayerInRoom(currentPlayer) || !lobbyViewModel.canMakeSuggestion()) {
            Toast.makeText(context, "You can't make another suggestion in this room.", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }
         */


        val suggestionButton: Button = binding.buttonMakeSuspicion
        val suspectSpinner: Spinner = binding.suspectSpinner
        val roomSpinner: Spinner = binding.roomSpinner
        val weaponSpinner: Spinner = binding.weaponSpinner
        val cancelButton: Button = binding.buttonCancel

        setUpSpinner(suspectSpinner, R.array.suspect_options)
        setUpSpinner(roomSpinner, R.array.room_options)
        setUpSpinner(weaponSpinner, R.array.weapon_options)

        suggestionButton.setOnClickListener {
            val suspect = suspectSpinner.selectedItem.toString()
            val room = roomSpinner.selectedItem.toString()
            val weapon = weaponSpinner.selectedItem.toString()

            if (suspect == "Select a suspect" || room == "Select a room" || weapon == "Select a weapon") {
                Toast.makeText(context, "Please select all options.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get lobby ID and player name
            val lobbyId = lobbyViewModel.lobbyState.value?.id
            val playerName = webSocketService.player.value?.name
            val playerId = webSocketService.player.value?.playerID

            if (lobbyId.isNullOrBlank() || playerName.isNullOrBlank() || playerId.isNullOrBlank()) {
                Toast.makeText(context, "No active lobby or player found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val suspicion = "$suspect — in the $room — with the $weapon"
            lobbyViewModel.addSuggestionNote(suspicion)
            lobbyViewModel.markSuggestionMade()

            // Send suggestion to backend
            turnBasedService.makeSuggestion(lobbyId, playerName,playerId,suspect, weapon, room)

            // Also save to notes for backward compatibility
            val suggestion = "$suspect — in the $room — with the $weapon"
            lobbyViewModel.addSuggestionNote(suggestion)

            Toast.makeText(context, "Suggestion sent!", Toast.LENGTH_SHORT).show()

            findNavController().navigateUp()
        }

        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }


    private fun setUpSpinner(spinner: Spinner, arrayResId: Int) {
        ArrayAdapter.createFromResource(
            requireContext(),
            arrayResId,
            R.layout.spinner_item
        ).also {
            it.setDropDownViewResource(R.layout.spinner_item)
            spinner.adapter = it
        }
    }



}
