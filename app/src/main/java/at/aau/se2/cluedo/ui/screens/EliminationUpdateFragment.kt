package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class EliminationUpdateFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layoutId = requireContext().resources.getIdentifier(
            "fragment_elimination_update", "layout", requireContext().packageName
        )
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val navController = findNavController()

        val updateTextId = context.resources.getIdentifier("updateText", "id", context.packageName)
        val backButtonId = context.resources.getIdentifier("button_back_to_lobby", "id", context.packageName)
        val exitButtonId = context.resources.getIdentifier("button_exit_game", "id", context.packageName)

        val updateText: TextView = view.findViewById(updateTextId)
        val backButton: Button = view.findViewById(backButtonId)
        val exitButton: Button = view.findViewById(exitButtonId)

        val args = arguments
        val player = args?.getString("winnerName") ?: "Miss Scarlet"
        val suspect = args?.getString("suspect") ?: "Mrs. White"
        val room = args?.getString("room") ?: "Kitchen"
        val weapon = args?.getString("weapon") ?: "Rope"

        val message = """
            Investigation Update:

            $player made a false accusation:

            $suspect — in the $room — with the $weapon.

            She has been removed from the investigation.
        """.trimIndent()

        updateText.text = message

        val mainMenuId = context.resources.getIdentifier("mainMenuFragment", "id", context.packageName)

        backButton.setOnClickListener {
            if (mainMenuId != 0) {
                navController.navigate(mainMenuId)
            } else {
                Toast.makeText(context, "Main menu not found.", Toast.LENGTH_SHORT).show()
            }
        }

        exitButton.setOnClickListener {
            requireActivity().finishAffinity()
        }
    }
}
