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

class SolveCaseFragment : Fragment() {

    private var isPlayerEliminated: Boolean = false

    private lateinit var suspectSpinner: Spinner
    private lateinit var roomSpinner: Spinner
    private lateinit var weaponSpinner: Spinner
    private lateinit var solveButton: Button

    private val correctSolution = Triple("Professor Plum", "Library", "Candlestick")

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
            animateAndClose(view)
        }

        return view
    }

    private fun setupSpinners() {
        val suspectArray = arrayOf(
            "Select a suspect",
            "Miss Scarlet",
            "Professor Plum",
            "Colonel Mustard",
            "Mrs. Peacock"
        )

        val roomArray = arrayOf(
            "Select a room",
            "Library",
            "Kitchen",
            "Ballroom",
            "Study",
            "Hall",
            "Billiard room",
            "Dining room",
            "Lounge",
            "Conservatory"
        )

        val weaponArray = arrayOf(
            "Select a weapon",
            "Candlestick",
            "Revolver",
            "Rope",
            "Lead Pipe",
            "Wrench",
            "Dagger"
        )

        val context = requireContext()

        val suspectAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            suspectArray
        )
        suspectSpinner.adapter = suspectAdapter

        val roomAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            roomArray
        )
        roomSpinner.adapter = roomAdapter

        val weaponAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            weaponArray
        )
        weaponSpinner.adapter = weaponAdapter
    }

    private fun solveCase() {
        if (isPlayerEliminated) {
            Toast.makeText(context, "You are eliminated! You can't solve the case anymore.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedSuspect = suspectSpinner.selectedItem.toString()
        val selectedRoom = roomSpinner.selectedItem.toString()
        val selectedWeapon = weaponSpinner.selectedItem.toString()

        if (Triple(selectedSuspect, selectedRoom, selectedWeapon) == correctSolution) {
            endGame()
        } else {
            eliminatePlayer()
        }
    }

    private fun endGame() {
        Toast.makeText(context, "Congratulations! You solved the case and won the game!", Toast.LENGTH_LONG).show()
    }

    private fun eliminatePlayer() {
        isPlayerEliminated = true
        Toast.makeText(context, "Wrong guess! You are eliminated. You can still show your cards.", Toast.LENGTH_LONG).show()
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
