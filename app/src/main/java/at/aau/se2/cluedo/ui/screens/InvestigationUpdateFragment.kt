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
import com.example.myapplication.databinding.FragmentEliminationScreenBinding
import com.example.myapplication.databinding.FragmentEliminationUpdateBinding
import com.example.myapplication.databinding.FragmentInvestigationUpdateBinding

class InvestigationUpdateFragment : Fragment() {

    private var _binding: FragmentInvestigationUpdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvestigationUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val navController = findNavController()

        val updateText: TextView = binding.updateText
        val backButton: Button = binding.buttonBackToLobby
        val exitButton: Button = binding.buttonExitGame

        val args = arguments
        val winner = args?.getString("winnerName") ?: "Miss Scarlet"
        val suspect = args?.getString("suspect") ?: "Colonel Mustard"
        val room = args?.getString("room") ?: "Study"
        val weapon = args?.getString("weapon") ?: "Candlestick"

        val message = """
            Investigation Update:
            
            $winner has solved the case!
            
            $suspect — in the $room — with the $weapon.
            
            Game over. 
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
