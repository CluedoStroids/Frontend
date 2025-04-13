package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentDiceRollerBinding
import kotlin.random.Random

class DiceRollerFragment : Fragment() {

    private var _binding: FragmentDiceRollerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiceRollerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rollDiceButton.setOnClickListener {
            rollDice()
        }
    }

    private fun rollDice() {
        // Generate a random number between 1 and 12
        val diceValue = Random.nextInt(1, 13)

        binding.diceValueTextView.text = diceValue.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
