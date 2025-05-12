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

class SuspicionPopupFragment : Fragment() {

    private val lobbyViewModel: LobbyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layoutId = resources.getIdentifier("fragment_suspicion_popup", "layout", requireContext().packageName)
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentPlayer = lobbyViewModel.lobbyState.value?.players?.find {
            it.name == lobbyViewModel.createdLobbyId.value
        }
        val isInRoom = currentPlayer?.x == -1 && currentPlayer.y == -1

        if (!isInRoom) {
            Toast.makeText(context, "You must be in a room to make a suspicion!", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }

        val suspectSpinner: Spinner = view.findViewById(getId("suspectSpinner"))
        val roomSpinner: Spinner = view.findViewById(getId("roomSpinner"))
        val weaponSpinner: Spinner = view.findViewById(getId("weaponSpinner"))
        val makeSuspicionButton: Button = view.findViewById(getId("button_make_suspicion"))
        val cancelButton: Button = view.findViewById(getId("button_cancel"))

        setUpSpinner(suspectSpinner, "suspect_options")
        setUpSpinner(roomSpinner, "room_options")
        setUpSpinner(weaponSpinner, "weapon_options")

        makeSuspicionButton.setOnClickListener {
            val suspect = suspectSpinner.selectedItem.toString()
            val room = roomSpinner.selectedItem.toString()
            val weapon = weaponSpinner.selectedItem.toString()

            if (suspect == "Select a suspect" || room == "Select a room" || weapon == "Select a weapon") {
                Toast.makeText(context, "Please select all options.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val suspicion = "$suspect — in the $room — with the $weapon"
            lobbyViewModel.addSuspicionNote(suspicion)

            Toast.makeText(context, "Suspicion saved to notes.", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setUpSpinner(spinner: Spinner, arrayName: String) {
        val arrayId = resources.getIdentifier(arrayName, "array", requireContext().packageName)
        val itemLayout = resources.getIdentifier("spinner_item", "layout", requireContext().packageName)

        ArrayAdapter.createFromResource(
            requireContext(),
            arrayId,
            itemLayout
        ).also {
            it.setDropDownViewResource(itemLayout)
            spinner.adapter = it
        }
    }

    private fun getId(idName: String): Int {
        return resources.getIdentifier(idName, "id", requireContext().packageName)
    }
}
