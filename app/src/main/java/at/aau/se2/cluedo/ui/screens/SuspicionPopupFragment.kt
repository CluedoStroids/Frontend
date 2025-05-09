package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
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

        val suspectAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.suspect_options,
            R.layout.spinner_item
        ).also {
            it.setDropDownViewResource(R.layout.spinner_item)
            suspectSpinner.adapter = it
        }

        val roomAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.room_options,
            R.layout.spinner_item
        ).also {
            it.setDropDownViewResource(R.layout.spinner_item)
            roomSpinner.adapter = it
        }

        val weaponAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.weapon_options,
            R.layout.spinner_item
        ).also {
            it.setDropDownViewResource(R.layout.spinner_item)
            weaponSpinner.adapter = it
        }
    }
}
