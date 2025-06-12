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
import at.aau.se2.cluedo.viewmodels.LobbyViewModel
import com.example.myapplication.R


class SuggestionFragment : Fragment() {

    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_suggestion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentPlayer = lobbyViewModel.lobbyState.value?.players?.find { it.isCurrentPlayer == true }

        if (!lobbyViewModel.isPlayerInRoom(currentPlayer) || !lobbyViewModel.canMakeSuggestion()) {
            Toast.makeText(context, "You can't make another suggestion in this room.", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }


        val suspectSpinner: Spinner = view.findViewById(R.id.suspectSpinner)
        val roomSpinner: Spinner = view.findViewById(R.id.roomSpinner)
        val weaponSpinner: Spinner = view.findViewById(R.id.weaponSpinner)
        val suggestionButton: Button = view.findViewById(R.id.submitButton)
        val cancelButton: Button = view.findViewById(R.id.button_cancel)

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

            val suspicion = "$suspect — in the $room — with the $weapon"
            lobbyViewModel.addSuspicionNote(suspicion)
            lobbyViewModel.markSuggestionMade()

            val currentLobbyId = lobbyViewModel.lobbyState.value?.id
            val currentPlayerName = currentPlayer?.name
            if (currentLobbyId != null && currentPlayerName != null) {
                lobbyViewModel.sendSuggestion(suspect, weapon, room)
            }


            Toast.makeText(context, "Suspicion saved to notes.", Toast.LENGTH_SHORT).show()
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
