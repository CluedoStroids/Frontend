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

class EliminationScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layoutId = requireContext().resources.getIdentifier(
            "fragment_elimination_screen", "layout", requireContext().packageName
        )
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val navController = findNavController()

        val eliminationTextId = context.resources.getIdentifier("eliminationText", "id", context.packageName)
        val backButtonId = context.resources.getIdentifier("button_back_to_lobby", "id", context.packageName)
        val exitButtonId = context.resources.getIdentifier("button_exit_game", "id", context.packageName)

        val eliminationText: TextView = view.findViewById(eliminationTextId)
        val backButton: Button = view.findViewById(backButtonId)
        val exitButton: Button = view.findViewById(exitButtonId)

        val args = arguments
        val suspect = args?.getString("suspect") ?: "Mrs. White"
        val room = args?.getString("room") ?: "Kitchen"
        val weapon = args?.getString("weapon") ?: "Rope"

        val message = """
            ❌ Your accusation was incorrect.

            You guessed:
            $suspect — in the $room — with the $weapon.

            You are now eliminated from the investigation.
        """.trimIndent()

        eliminationText.text = message

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
