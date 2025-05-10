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
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R

class SuspicionPopupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_suspicion_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val suspectSpinner: Spinner = view.findViewById(R.id.suspectSpinner)
        val roomSpinner: Spinner = view.findViewById(R.id.roomSpinner)
        val weaponSpinner: Spinner = view.findViewById(R.id.weaponSpinner)
        val makeSuspicionButton: Button = view.findViewById(R.id.button_make_suspicion)
        val cancelButton: Button = view.findViewById(R.id.button_cancel)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.suspect_options,
            R.layout.spinner_item
        ).also {
            it.setDropDownViewResource(R.layout.spinner_item)
            suspectSpinner.adapter = it
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.room_options,
            R.layout.spinner_item
        ).also {
            it.setDropDownViewResource(R.layout.spinner_item)
            roomSpinner.adapter = it
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.weapon_options,
            R.layout.spinner_item
        ).also {
            it.setDropDownViewResource(R.layout.spinner_item)
            weaponSpinner.adapter = it
        }

        makeSuspicionButton.setOnClickListener {
            val suspect = suspectSpinner.selectedItem.toString()
            val room = roomSpinner.selectedItem.toString()
            val weapon = weaponSpinner.selectedItem.toString()

            if (suspect == "Select a suspect" || room == "Select a room" || weapon == "Select a weapon") {
                Toast.makeText(context, "Please select all options.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putString("revealedCard", weapon)
                putString("revealedBy", "Player 3")
            }

            val popupId = resources.getIdentifier("suspiciousPopupFragment", "id", requireContext().packageName)
            if (popupId != 0) {
                findNavController().navigate(popupId, bundle)
            } else {
                Toast.makeText(context, "Suspicious popup not found", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
