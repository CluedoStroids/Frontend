package at.aau.se2.cluedo.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import at.aau.se2.cluedo.data.network.WebSocketService
import at.aau.se2.cluedo.data.network.TurnBasedWebSocketService
import com.example.myapplication.databinding.FragmentDiceRollerBinding
import kotlinx.coroutines.launch
//TODO: remove this fragment, because it's not needed? @Katharina Krassnitzer
class DiceRollerFragment : Fragment() {

    private var _binding: FragmentDiceRollerBinding? = null
    private val binding get() = _binding!!

    private val webSocketService = WebSocketService.getInstance()
    private val turnBasedService = TurnBasedWebSocketService.getInstance()

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

        webSocketService.connect()

        binding.rollDiceButton.setOnClickListener {
            // Note: This fragment might be using the old dice system
            // For turn-based dice rolling, use turnBasedService.rollDiceForTurn() instead
            webSocketService.rollDice()
        }

        lifecycleScope.launch {
            launch {
                turnBasedService.diceOneResult.collect { value ->
                    value?.let {
                        binding.diceOneValueTextView.text = it.toString()
                    }
                }
            }
            launch {
                turnBasedService.diceTwoResult.collect { value ->
                    value?.let {
                        binding.diceTwoValueTextView.text = it.toString()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        webSocketService.disconnect()
    }
}
