package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class WinScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layoutId = requireContext().resources.getIdentifier(
            "fragment_win_screen", "layout", requireContext().packageName
        )
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        val winTitleId = context.resources.getIdentifier("winTitle", "id", context.packageName)
        val winDetailsId = context.resources.getIdentifier("winDetails", "id", context.packageName)
        val backButtonId = context.resources.getIdentifier("button_back_to_lobby", "id", context.packageName)
        val exitButtonId = context.resources.getIdentifier("button_exit_game", "id", context.packageName)

        val winTitle: TextView = view.findViewById(winTitleId)
        val winDetails: TextView = view.findViewById(winDetailsId)
        val backButton: Button = view.findViewById(backButtonId)
        val exitButton: Button = view.findViewById(exitButtonId)

        val args = arguments
        val winnerName = args?.getString("winnerName") ?: "Miss Scarlet"
        val suspect = args?.getString("suspect") ?: "Colonel Mustard"
        val room = args?.getString("room") ?: "Study"
        val weapon = args?.getString("weapon") ?: "Candlestick"

        Log.d("WinScreenFragment", "Showing result: $winnerName, $suspect, $room, $weapon")

        winTitle.text = " $winnerName has cracked the case!"
        winDetails.text = "\n$suspect — in the $room — with the $weapon.\n\nJustice is served. "

        val navController = findNavController()
        val destId = context.resources.getIdentifier("mainMenuFragment", "id", context.packageName)

        backButton.setOnClickListener {
            if (destId != 0) {
                navController.navigate(destId)
            } else {
                Toast.makeText(context, "Main menu not found in nav graph", Toast.LENGTH_SHORT).show()
            }
        }

        exitButton.setOnClickListener {
            requireActivity().finishAffinity()
        }
    }
}
